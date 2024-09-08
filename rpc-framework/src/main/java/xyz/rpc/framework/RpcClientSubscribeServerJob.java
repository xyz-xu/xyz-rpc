package xyz.rpc.framework;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import xyz.rpc.common.RpcCenterClientQry;
import xyz.rpc.common.RpcCenterHttpApi;
import xyz.rpc.common.RpcCenterNodeBO;
import xyz.rpc.common.RpcCenterServerNodeBO;

import java.net.InetAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * rpc-client 从 rpc-center 获取server节点 的job
 *
 * @author xin.xu
 */
@Slf4j
@UtilityClass
public class RpcClientSubscribeServerJob {

    @SneakyThrows
    public void start(RpcClientProperties properties) {
        // schedule executor
        ScheduledExecutorService jobExecutor = new ScheduledThreadPoolExecutor(
                1,
                new CustomizableThreadFactory("subscribe_server_job")
        );

        // rest template
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5000);
        simpleClientHttpRequestFactory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);

        // client node info
        RpcCenterNodeBO node = new RpcCenterNodeBO();
        node.setIp(InetAddress.getLocalHost().getHostAddress());
        node.setPort(properties.getHttpPort());

        // client qry info
        RpcCenterClientQry qry = new RpcCenterClientQry();
        qry.setPackageNameList(properties.getPackageName());
        qry.setClientNode(node);

        // log
        log.info("subscribe_server_job:qry={}", qry);

        jobExecutor.scheduleWithFixedDelay(
                () -> {
                    // build url
                    String url = "http://" + properties.getCenterUrl() + RpcCenterHttpApi.CLIENT_GET_SERVER;

                    // do request
                    try {
                        // do request
                        RpcCenterServerNodeBO[] result = restTemplate.postForObject(url, qry, RpcCenterServerNodeBO[].class);
                        Assert.isTrue(result != null && result.length > 0, "empty server nodes");

                        // update
                        RpcClientServerNodeHolder.updateServerNodeMap(result);

                        log.debug("subscribe_server_job success,url={}", url);
                    } catch (Exception e) {
                        log.warn("subscribe_server_job failed,url={},error={}", url, e.getMessage());
                    }
                },
                0,
                10,
                TimeUnit.SECONDS
        );
    }
}
