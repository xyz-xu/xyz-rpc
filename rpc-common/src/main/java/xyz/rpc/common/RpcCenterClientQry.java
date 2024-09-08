package xyz.rpc.common;

import lombok.Data;

import java.util.List;

/**
 * rpc-client 从center查询server 发起的请求参数
 *
 * @author xin.xu
 */
@Data
public class RpcCenterClientQry {

    private RpcCenterNodeBO clientNode;

    private List<String> packageNameList;

}
