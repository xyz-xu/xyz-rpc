package xyz.rpc.common;

import lombok.experimental.UtilityClass;

/**
 * rpc client/server api
 *
 * @author xin.xu
 */
@UtilityClass
public class RpcNodeHttpApi {

    public final String RPC_HEALTH_CHECK = "/rpc/health";

    public final String RPC_CALL = "/rpc/call";

}
