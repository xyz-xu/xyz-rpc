package xyz.rpc.framework;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import xyz.rpc.common.RpcCenterServerNodeBO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * server node 存储类
 *
 * @author xin.xu
 */
@Slf4j
@UtilityClass
public class RpcClientServerNodeHolder {

    /**
     * 通过负载均衡获取节点信息
     *
     * @param list 所有的服务节点列表
     * @return 获取到的节点
     */
    public RpcCenterServerNodeBO getServerNodeByLoadBalancing(List<RpcCenterServerNodeBO> list) {
        if (list.isEmpty()) {
            // not match
            return null;
        } else {
            return list.get(nextSeq() % list.size());
        }
    }

    /**
     * 失败充实时从除已调用节点之外的节点任意获取一个
     *
     * @param list      所有的服务节点列表
     * @param calledSet 已经调用过的节点
     * @return 获取到的节点
     */
    public RpcCenterServerNodeBO getServerNodeSkipCalled(List<RpcCenterServerNodeBO> list, Set<RpcCenterServerNodeBO> calledSet) {
        return list.stream().filter(t -> !calledSet.contains(t)).findAny().orElse(null);
    }

    /**
     * 查询接口关联的server节点信息
     *
     * @param interfaceName 接口全名
     * @return server 节点
     */
    public List<RpcCenterServerNodeBO> getServerNodeList(String interfaceName) {
        String packageName = removeLast(interfaceName);
        return listByPackageName(packageName);
    }

    /**
     * 移除字符串最后一组 .
     * 例如 xyz.adb.qwe.wqq --> xyz.adb.qwe
     */
    private String removeLast(String str) {
        int index = str.lastIndexOf('.');
        if (index >= 0) {
            return str.substring(0, index);
        } else {
            return "";
        }
    }

    private final Map<String, List<RpcCenterServerNodeBO>> SERVER_NODE_MAP = new ConcurrentHashMap<>();

    private List<RpcCenterServerNodeBO> listByPackageName(String packageName) {
        return SERVER_NODE_MAP.getOrDefault(packageName, Collections.emptyList());
    }

    /**
     * 更新server nodes
     */
    private void updateServerNodeMap(List<RpcCenterServerNodeBO> serverNodes) {
        // log
        log.info("updateServerNodeMap:l={}", serverNodes);

        // grouping
        Map<String, List<RpcCenterServerNodeBO>> groupingMap = serverNodes
                .stream()
                .collect(Collectors.groupingBy(RpcCenterServerNodeBO::getPackageName));

        // replace
        SERVER_NODE_MAP.putAll(groupingMap);

        // remove not exists
        for (String key : SERVER_NODE_MAP.keySet()) {
            if (!groupingMap.containsKey(key)) {
                SERVER_NODE_MAP.remove(key);
                log.info("updateServerNodeMap:r={}", key);
            }
        }
    }

    /**
     * 更新server nodes
     */
    public void updateServerNodeMap(RpcCenterServerNodeBO[] serverNodes) {
        updateServerNodeMap(Arrays.asList(serverNodes));
    }

    public void removeAllServerNode() {
        SERVER_NODE_MAP.clear();
        log.info("removeAllServerNode");
    }

    private final AtomicInteger SEQ = new AtomicInteger();

    private int nextSeq() {
        int seq = SEQ.getAndIncrement();
        if (seq < 0) {
            SEQ.set(0);
            return 0;
        } else {
            return seq;
        }
    }

}
