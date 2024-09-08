package xyz.rpc.framework;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.io.ClassPathResource;
import xyz.rpc.common.SupportRecall;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 工具类
 *
 * @author xin.xu
 */
@UtilityClass
public class RpcClientUtils {

    /**
     * 用于保证client-proxy是单例
     */
    private final Map<Class<?>, Object> CLIENT_PROXY_MAP = new HashMap<>();

    public synchronized <T> T getClientProxy(Class<T> interfaceClass) {
        if (CLIENT_PROXY_MAP.containsKey(interfaceClass)) {
            return (T) CLIENT_PROXY_MAP.get(interfaceClass);
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setInterfaces(new Class[]{interfaceClass});
            enhancer.setCallback(new RpcClientProxyInterceptor());
            T t = (T) enhancer.create();
            CLIENT_PROXY_MAP.put(interfaceClass, t);
            return t;
        }
    }

    public RpcClientProperties loadProperties() {
        // properties instance
        RpcClientProperties properties = RpcClientProperties.getInstance();

        // build yaml
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(new ClassPathResource("application.yml"));
        Properties yaml = Objects.requireNonNull(yamlPropertiesFactoryBean.getObject());

        // read RpcClientConstants.CLIENT_SCAN_PACKAGE_PARAM
        properties.setPackageName(readStringList(yaml, RpcClientConstants.CLIENT_SCAN_PACKAGE_PARAM));

        // read
        properties.setCenterUrl(yaml.getProperty(RpcClientConstants.CLIENT_CENTER_URL_PARAM));
        properties.setHttpPort(readInteger(yaml, RpcClientConstants.SERVER_PORT_PARAM));

        return properties;
    }

    private List<String> readStringList(Properties yaml, String name) {
        List<String> list = new ArrayList<>();
        for (String propertyName : yaml.stringPropertyNames()) {
            if (propertyName.startsWith(name)) {
                list.add(yaml.getProperty(propertyName));
            }
        }
        return list;
    }

    private Integer readInteger(Properties yaml, String name) {
        return (Integer) yaml.get(name);
    }

    /**
     * 获取支持重复调用的注解
     */
    public SupportRecall getSupportRecallAnnotation(Method method) {
        SupportRecall annotation = method.getAnnotation(SupportRecall.class);

        if (annotation != null) {
            return annotation;
        }

        return method.getDeclaringClass().getAnnotation(SupportRecall.class);
    }

    public boolean isHttpSuccess(int value) {
        // 2XX SUCCESS
        return value >= 200 && value < 300;
    }

}
