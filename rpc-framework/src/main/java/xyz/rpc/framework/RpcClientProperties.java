package xyz.rpc.framework;

import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc-client 配置参数
 * 手动设置参数 prefix = "xyz.rpc.client"
 *
 * @author xin.xu
 */
@Data
public class RpcClientProperties {

    private RpcClientProperties() {
    }

    private static final String KEY = "KEY";
    private static final ConcurrentHashMap<String, RpcClientProperties> INSTANCE_MAP = new ConcurrentHashMap<>();

    public static RpcClientProperties getInstance() {
        if (INSTANCE_MAP.containsKey(KEY)) {
            return INSTANCE_MAP.get(KEY);
        }

        INSTANCE_MAP.putIfAbsent(KEY, new RpcClientProperties());
        return INSTANCE_MAP.get(KEY);
    }


    private String centerUrl;

    private List<String> packageName;

    private Integer httpPort;

}
