package xyz.rpc.server.starter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.rpc.framework.RpcServerCallProcessor;
import xyz.rpc.framework.RpcServerProperties;

/**
 * rpc-server processor 自动装配
 *
 * @author xin.xu
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({
        RpcServerProperties.class,
        ServerProperties.class
})
@AutoConfigureBefore(RpcServerAutoConfiguration.class)
public class RpcServerCallProcessorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcServerCallProcessor rpcServerCallProcessor(RpcServerProperties properties, ServerProperties serverProperties) {
        properties.setHttpPort(serverProperties.getPort());
        log.info("RpcServerCallProcessorAutoConfiguration:{}", properties);
        return new RpcServerCallProcessor(properties);
    }

}
