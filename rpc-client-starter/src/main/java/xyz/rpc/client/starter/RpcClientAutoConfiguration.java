package xyz.rpc.client.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.rpc.framework.RpcClientBeanRegistryPostProcessor;

/**
 * rpc-client 配置
 *
 * @author xin.xu
 */
@Configuration
public class RpcClientAutoConfiguration {

    /**
     * rpc-client registry
     */
    @Bean
    @ConditionalOnMissingBean(RpcClientBeanRegistryPostProcessor.class)
    public static RpcClientBeanRegistryPostProcessor rpcClientBeanRegistryPostProcessor() {
        return new RpcClientBeanRegistryPostProcessor();
    }

}
