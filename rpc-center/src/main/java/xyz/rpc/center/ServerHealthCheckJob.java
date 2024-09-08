package xyz.rpc.center;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import xyz.rpc.common.RpcCenterNodeBO;
import xyz.rpc.common.RpcCenterServerNodeBO;
import xyz.rpc.common.RpcCommonConstants;
import xyz.rpc.common.RpcNodeHttpApi;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * server 节点 健康检查
 *
 * @author xin.xu
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ServerHealthCheckJob {

    private final RestTemplate restTemplate;
    private final ServerPoolProcessor serverPoolProcessor;

    @Scheduled(fixedDelay = 5000)
    public void healthCheck() {
        // get server node
        List<RpcCenterNodeBO> serverNodeList = serverPoolProcessor
                .getAllNodes()
                .stream()
                .map(this::toNode)
                .distinct()
                .collect(Collectors.toList());

        // get for health check
        for (RpcCenterNodeBO node : serverNodeList) {
            try {
                getForHealthCheck(node);
            } catch (Exception e) {
                // health check failed
                log.error("health check failed, server_node=" + node.toString(), e);
                serverPoolProcessor.removeNode(node);
            }
        }
    }

    private RpcCenterNodeBO toNode(RpcCenterServerNodeBO bo) {
        RpcCenterNodeBO node = new RpcCenterNodeBO();
        node.setIp(bo.getIp());
        node.setPort(bo.getPort());
        return node;
    }

    private void getForHealthCheck(RpcCenterNodeBO node) {
        String url = "http://" + node.getIp() + ":" + node.getPort() + RpcNodeHttpApi.RPC_HEALTH_CHECK;
        String result = restTemplate.getForObject(url, String.class);
        Assert.isTrue(
                Objects.equals(result, RpcCommonConstants.SUCCESS),
                "failed"
        );
    }

}
