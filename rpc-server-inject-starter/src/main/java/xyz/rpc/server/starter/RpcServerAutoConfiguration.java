package xyz.rpc.server.starter;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.rpc.framework.RpcServerCallProcessor;
import xyz.rpc.framework.RpcServerController;

/**
 * rpc-server 配置类
 *
 * @author xin.xu
 */
@Configuration
@AutoConfigureAfter(RpcServerCallProcessorAutoConfiguration.class)
public class RpcServerAutoConfiguration {
    /**
     * 构建rpc-server controller
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcServerController rpcServerController(RpcServerCallProcessor rpcServerCallProcessor) {
        return new RpcServerController(rpcServerCallProcessor);
    }

}
