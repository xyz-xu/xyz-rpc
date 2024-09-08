package xyz.rpc.framework;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import xyz.rpc.common.RpcCenterServerNodeBO;
import xyz.rpc.common.RpcCommonConstants;
import xyz.rpc.common.RpcNodeHttpApi;
import xyz.rpc.common.SupportRecall;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 通过动态代理创建rpc-client对象
 *
 * @author xin.xu
 */
@Slf4j
class RpcClientProxyInterceptor implements MethodInterceptor {

    /**
     * 拦截方法，发起远程调用
     */
    @Override
    @SneakyThrows
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
        String requestId = UUID.randomUUID().toString();
        String interfaceName = method.getDeclaringClass().getCanonicalName();

        // 封装远程调用参数（接口类，方法，重载的参数类型）
        RpcCallHeaderBO headerBO = new RpcCallHeaderBO();
        headerBO.setRequestId(requestId);
        headerBO.setInterfaceName(interfaceName);
        headerBO.setMethodName(method.getName());
        headerBO.setParameterTypes(method.getParameterTypes());

        // build request header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(RpcCommonConstants.HEADER_PARAM_BO, ToolUtils.serializeHeaderBO(headerBO));

        // build body
        Object body;
        if (ToolUtils.isStreamParameter(method.getParameterTypes())) {
            // change content-type
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // build body
            RpcCallResource resource = new RpcCallResource((InputStream) objects[0], RpcCommonConstants.STREAM_RESOURCE_FILE_NAME);
            LinkedMultiValueMap<String, Object> linkedMultiValueMap = new LinkedMultiValueMap<>();
            linkedMultiValueMap.set(RpcCommonConstants.BODY_PARAM, resource);
            body = linkedMultiValueMap;
        } else {
            // 封装远程调用参数（参数值）
            RpcCallBodyBO bo = new RpcCallBodyBO();
            bo.setArgs(objects);

            body = ToolUtils.serializeBodyBO(bo);
        }

        // build request entity
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, httpHeaders);

        // first call
        List<RpcCenterServerNodeBO> serverNodeList = RpcClientServerNodeHolder.getServerNodeList(interfaceName);
        RpcCenterServerNodeBO serverNode = RpcClientServerNodeHolder.getServerNodeByLoadBalancing(serverNodeList);
        Assert.notNull(serverNode, "missing server node, i=" + interfaceName);

        // first execute
        ResponseEntity<Resource> responseEntity;
        try {
            responseEntity = execute(serverNode, requestEntity);
        } catch (RuntimeException e) {
            // 获取注解，默认不重复调用
            SupportRecall anno = RpcClientUtils.getSupportRecallAnnotation(method);
            int maxRepeat = Optional.ofNullable(anno).map(SupportRecall::value).orElse(1);

            if (maxRepeat > 1) {
                // 远程调用失败重试
                responseEntity = recall(interfaceName, serverNodeList, serverNode, maxRepeat, requestEntity);
            } else {
                throw e;
            }
        }

        // 反序列化
        if (ToolUtils.isVoidReturnType(method.getReturnType())) {
            log.info("NO RETURN TYPE");
            return null;
        } else if (ToolUtils.isStreamReturnType(method.getReturnType())) {
            return responseEntity.getBody().getInputStream();
        } else {
            byte[] bytes = IOUtils.toByteArray(responseEntity.getBody().getInputStream());
            return ToolUtils.deserializeResult(bytes, method.getReturnType());
        }

        // TODO 异步接口封装
    }

    /**
     * 重复调用
     */
    private ResponseEntity<Resource> recall(String interfaceName,
                                            List<RpcCenterServerNodeBO> serverNodeList,
                                            RpcCenterServerNodeBO calledNode,
                                            int maxRepeat,
                                            HttpEntity<Object> requestEntity
    ) {
        // called set
        HashSet<RpcCenterServerNodeBO> calledNodeSet = new HashSet<>();
        calledNodeSet.add(calledNode);

        // repeat
        int repeat = 1;
        while (maxRepeat >= ++repeat) {
            // get server node
            RpcCenterServerNodeBO serverNode = RpcClientServerNodeHolder.getServerNodeSkipCalled(serverNodeList, calledNodeSet);
            Assert.notNull(serverNode, "recall missing server node, i=" + interfaceName);

            // add to called
            calledNodeSet.add(serverNode);

            // execute
            try {
                return execute(serverNode, requestEntity);
            } catch (Exception ignored) {
            }
        }

        throw new RuntimeException("exceeded retry attempts,i=" + interfaceName);
    }

    private ResponseEntity<Resource> execute(RpcCenterServerNodeBO serverNode, HttpEntity<Object> requestEntity) {
        // 远程调用
        String url = "http://" + serverNode.getIp() + ":" + serverNode.getPort() + RpcNodeHttpApi.RPC_CALL;
        ResponseEntity<Resource> responseEntity = REST_TEMPLATE.postForEntity(url, requestEntity, Resource.class);

        // check http status
        int value = responseEntity.getStatusCodeValue();
        Assert.isTrue(RpcClientUtils.isHttpSuccess(value), "http status " + value);

        return responseEntity;
    }

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

}
