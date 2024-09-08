package xyz.rpc.framework;

import lombok.Data;

/**
 * 远程调用时放到header中的参数
 *
 * @author xin.xu
 */
@Data
public class RpcCallHeaderBO {
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
}
