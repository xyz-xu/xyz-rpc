package xyz.rpc.center;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.rpc.common.RpcCenterClientQry;
import xyz.rpc.common.RpcCenterNodeBO;
import xyz.rpc.common.RpcCenterServerNodeBO;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * server节点信息处理
 *
 * @author xin.xu
 */
@Slf4j
@Service
public class ServerPoolProcessor {

    private final Set<RpcCenterServerNodeBO> serverNodeSet = new ConcurrentHashSet<>();

    public Set<RpcCenterServerNodeBO> getAllNodes() {
        return serverNodeSet;
    }

    public void addNodes(List<RpcCenterServerNodeBO> nodes) {
        serverNodeSet.addAll(nodes);
        log.info("addNodes,{}", nodes);
    }

    public boolean removeNode(RpcCenterNodeBO node) {
        boolean remove = serverNodeSet.removeIf(
                t -> Objects.equals(t.getIp(), node.getIp()) &&
                        Objects.equals(t.getPort(), node.getPort())
        );
        log.info("removeNode,{}", node);
        return remove;
    }

    private List<RpcCenterServerNodeBO> getByPackageName(String packageName) {
        return serverNodeSet
                .stream()
                .filter(t -> Objects.equals(t.getPackageName(), packageName))
                .collect(Collectors.toList());
    }

    public List<RpcCenterServerNodeBO> getServerNodeList(RpcCenterClientQry clientQry) {
        return clientQry
                .getPackageNameList()
                .stream()
                .map(this::getByPackageName)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
