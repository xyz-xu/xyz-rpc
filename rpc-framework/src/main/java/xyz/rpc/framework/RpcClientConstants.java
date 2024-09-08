package xyz.rpc.framework;

import lombok.experimental.UtilityClass;

/**
 * 常量参数
 *
 * @author xin.xu
 */
@UtilityClass
public class RpcClientConstants {

    /**
     * rpc-client 扫描的包 --> 对应的application.yml中的配置名
     */
    public final String CLIENT_SCAN_PACKAGE_PARAM = "xyz.rpc.client.package-name";

    /**
     * 配置测rpc-center
     * 形如 127.0.0.1:8080
     */
    public final String CLIENT_CENTER_URL_PARAM = "xyz.rpc.client.center-url";

    public final String SERVER_PORT_PARAM = "server.port";

}
