package xyz.rpc.framework;

import lombok.Data;

/**
 * 远程调用的参数bo类(非stream)
 *
 * @author xin.xu
 */
@Data
class RpcCallBodyBO {

    private Object[] args;

}
