package xyz.rpc.center;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.rpc.common.RpcCenterClientQry;
import xyz.rpc.common.RpcCenterHttpApi;
import xyz.rpc.common.RpcCenterServerNodeBO;
import xyz.rpc.common.RpcCommonConstants;

import java.util.List;

/**
 * rpc-center
 *
 * @author xin.xu
 */
@RestController
@RequiredArgsConstructor
public class RpcCenterController {

    private final ServerPoolProcessor serverPoolProcessor;

    /**
     * server节点注册
     */
    @PostMapping(RpcCenterHttpApi.SERVER_REGISTER)
    public String serverRegister(@RequestBody List<RpcCenterServerNodeBO> list) {
        serverPoolProcessor.addNodes(list);
        return RpcCommonConstants.SUCCESS;
    }

    /**
     * client获取远程信息
     */
    @PostMapping(RpcCenterHttpApi.CLIENT_GET_SERVER)
    public List<RpcCenterServerNodeBO> clientGetServer(@RequestBody RpcCenterClientQry clientQry) {
        return serverPoolProcessor.getServerNodeList(clientQry);
    }

}
