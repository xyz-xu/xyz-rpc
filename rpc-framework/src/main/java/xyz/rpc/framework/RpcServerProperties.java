package xyz.rpc.framework;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * rpc-server 参数
 *
 * @author xin.xu
 */
@Data
@ConfigurationProperties(prefix = "xyz.rpc.server")
public class RpcServerProperties {

    /**
     * 包名
     */
    private List<String> packageName;

    private Integer httpPort;

    /**
     * 配置center的地址
     * ip:port的格式
     * <p>
     * 在运行过程中，可能由于rpc-center主备切换，bean中的该字段值会修改
     */
    private String centerUrl;

}
