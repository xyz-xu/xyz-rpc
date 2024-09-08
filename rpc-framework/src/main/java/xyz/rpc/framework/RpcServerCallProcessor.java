package xyz.rpc.framework;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartRequest;
import xyz.rpc.common.RpcCommonConstants;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * rpc-server 远程调用处理方法
 *
 * @author xin.xu
 */
@Slf4j
public class RpcServerCallProcessor implements ApplicationContextAware {

    private final RpcServerProperties properties;

    public RpcServerCallProcessor(RpcServerProperties properties) {
        this.properties = properties;
    }

    /**
     * interfaceName --> Bean
     */
    private final Map<String, Object> rpcBeanMap = new HashMap<>();

    @PostConstruct
    public void init() {
        if (Objects.isNull(applicationContext)) {
            log.error("application context is null");
            return;
        }

        if (properties.getPackageName() == null || properties.getPackageName().isEmpty()) {
            log.warn("scan server packages is empty");
            return;
        }

        // load bean
        Iterator<String> iterator = properties.getPackageName().iterator();
        while (iterator.hasNext()) {
            String packageName = iterator.next();

            // load bean
            Map<String, Object> map = loadBeanByPackageName(packageName);

            if (map.isEmpty()) {
                iterator.remove();
            } else {
                // put to bean map
                rpcBeanMap.putAll(map);
            }
        }

        // job
        if (!rpcBeanMap.isEmpty()) {
            RpcServerRegisterJob.start(properties);
        }
    }

    private Map<String, Object> loadBeanByPackageName(String packageName) {
        // check
        if (!StringUtils.hasText(packageName)) {
            log.info("scan server package [{}] is blank", packageName);
            return new HashMap<>(0);
        }

        // scan package
        Set<Class<?>> interfaceSet = ToolUtils.scanInterfaces(packageName);

        // map
        Map<String, Object> map = new HashMap<>(interfaceSet.size());

        // 获取type对应的bean
        for (Class<?> interfaceClass : interfaceSet) {
            try {
                Object bean = applicationContext.getBean(interfaceClass);
                map.put(interfaceClass.getCanonicalName(), bean);
            } catch (NoSuchBeanDefinitionException e) {
                log.warn("interface [{}] does not has bean", interfaceClass.getCanonicalName());
            }
        }

        // log
        log.info("RpcServerCallProcessor_init:rpc scan package [{}] bean count is {}", packageName, map.size());

        return map;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 执行远程调用
     */
    @SneakyThrows
    public void execute(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        // get headers
        String headerParamString = httpServletRequest.getHeader(RpcCommonConstants.HEADER_PARAM_BO);
        RpcCallHeaderBO headerBO = ToolUtils.deserializeHeaderBO(headerParamString);

        // get bean
        Object bean = rpcBeanMap.get(headerBO.getInterfaceName());
        Assert.notNull(bean, "no such bean");

        // find method
        Method method = null;
        for (Method m : bean.getClass().getMethods()) {
            // match name
            if (!Objects.equals(m.getName(), headerBO.getMethodName())) {
                continue;
            }

            // match param length
            if (!Objects.equals(m.getParameterTypes().length, headerBO.getParameterTypes().length)) {
                continue;
            }

            // match param type
            boolean matchType = true;
            for (int i = 0; i < m.getParameterTypes().length; ++i) {
                if (!Objects.equals(m.getParameterTypes()[i], headerBO.getParameterTypes()[i])) {
                    matchType = false;
                    break;
                }
            }
            if (!matchType) {
                continue;
            }

            // match
            method = m;
            break;
        }

        // assert method
        Assert.notNull(method, "no such method");

        InputStream inputStream = getRequestInputStream(httpServletRequest);
        if (ToolUtils.isStreamParameter(method.getParameterTypes())) {
            // execute stream call
            executeStreamParameter(httpServletResponse, bean, method, inputStream);
        } else {
            // execute bytes call
            executeBytesParameter(httpServletResponse, bean, method, IOUtils.toByteArray(inputStream));
        }
    }

    private InputStream getRequestInputStream(HttpServletRequest httpServletRequest) throws IOException {
        if (httpServletRequest instanceof MultipartRequest) {
            MultipartRequest multipartRequest = (MultipartRequest) httpServletRequest;
            return multipartRequest.getFile(RpcCommonConstants.BODY_PARAM).getInputStream();
        } else {
            return httpServletRequest.getInputStream();
        }
    }

    /**
     * 执行远程调用（byte[]）
     */
    private void executeBytesParameter(HttpServletResponse httpServletResponse, Object bean, Method method, byte[] bytes) throws InvocationTargetException, IllegalAccessException, IOException {
        // deserialize
        RpcCallBodyBO bo = ToolUtils.deserializeBodyBO(bytes);
        int argLen = Optional.ofNullable(method.getParameterTypes()).map(t -> t.length).orElse(0);
        for (int i = 0; i < argLen; ++i) {
            bo.getArgs()[i] = JSON.parseObject(JSON.toJSONBytes(bo.getArgs()[i]), method.getParameterTypes()[i]);
        }

        // invoke
        Object result = method.invoke(bean, bo.getArgs());

        // write output stream
        writeOutputStream(httpServletResponse, method, result);
    }

    /**
     * 执行远程调用（InputStream）
     */
    private void executeStreamParameter(HttpServletResponse httpServletResponse, Object bean, Method method, InputStream inputStream) throws InvocationTargetException, IllegalAccessException, IOException {
        // invoke
        Object result = method.invoke(bean, inputStream);

        // write output stream
        writeOutputStream(httpServletResponse, method, result);
    }

    private void writeOutputStream(HttpServletResponse httpServletResponse, Method method, Object result) throws IOException {
        if (Objects.equals(method.getReturnType(), InputStream.class)) {
            InputStream is = (InputStream) result;
            IOUtils.copy(is, httpServletResponse.getOutputStream());
        } else {
            httpServletResponse.getOutputStream().write(ToolUtils.serializeResult(result));
        }
    }

}
