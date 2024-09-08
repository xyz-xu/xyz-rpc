package xyz.rpc.framework;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import xyz.rpc.common.RpcCenterHttpApi;
import xyz.rpc.common.RpcCenterServerNodeBO;
import xyz.rpc.common.RpcCommonConstants;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * rpc-server 向 rpc-center 注册的job
 *
 * @author xin.xu
 */
@Slf4j
@UtilityClass
public class RpcServerRegisterJob {

    @SneakyThrows
    public void start(RpcServerProperties properties) {
        // schedule executor
        ScheduledExecutorService jobExecutor = new ScheduledThreadPoolExecutor(
                1,
                new CustomizableThreadFactory("register_job")
        );

        // rest template
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5000);
        simpleClientHttpRequestFactory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);

        // server node info
        List<RpcCenterServerNodeBO> list = new ArrayList<>();
        for (String packageName : properties.getPackageName()) {
            RpcCenterServerNodeBO node = new RpcCenterServerNodeBO();
            node.setIp(InetAddress.getLocalHost().getHostAddress());
            node.setPort(properties.getHttpPort());
            node.setPackageName(packageName);

            list.add(node);
        }

        // log
        log.info("register_job:server_nodes={}", list);

        jobExecutor.scheduleWithFixedDelay(
                () -> {
                    // build url
                    String url = "http://" + properties.getCenterUrl() + RpcCenterHttpApi.SERVER_REGISTER;

                    // do request
                    try {
                        String result = restTemplate.postForObject(url, list, String.class);
                        Assert.isTrue(Objects.equals(result, RpcCommonConstants.SUCCESS), result);
                        log.debug("register job success,url={}", url);
                    } catch (Exception e) {
                        log.error("register job failed,url={},error={}", url, e.getMessage());
                    }
                },
                0,
                10,
                TimeUnit.SECONDS
        );
    }
}
