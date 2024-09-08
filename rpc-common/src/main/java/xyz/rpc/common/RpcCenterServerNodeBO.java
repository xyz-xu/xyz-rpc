package xyz.rpc.common;

import lombok.Data;

/**
 * rpc-center 节点信息
 *
 * @author xin.xu
 */
@Data
public class RpcCenterServerNodeBO {

    private String ip;

    private Integer port;

    private String packageName;

}
