package xyz.rpc.framework;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.rpc.common.RpcCommonConstants;
import xyz.rpc.common.RpcNodeHttpApi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * rpc-server 提供的接口
 *
 * @author xin.xu
 */
@RequestMapping
public class RpcServerController {

    public RpcServerController(RpcServerCallProcessor processor) {
        this.processor = Objects.requireNonNull(processor);
    }

    private final RpcServerCallProcessor processor;

    /**
     * 远程调用
     */
    @SneakyThrows
    @PostMapping(RpcNodeHttpApi.RPC_CALL)
    public void rpcCall(HttpServletResponse httpServletResponse,
                        HttpServletRequest httpServletRequest) {
        processor.execute(httpServletResponse, httpServletRequest);
    }

    @SneakyThrows
    @GetMapping(RpcNodeHttpApi.RPC_HEALTH_CHECK)
    public void rpcHealthCheck(HttpServletResponse httpServletResponse) {
        httpServletResponse.getOutputStream().write(RpcCommonConstants.SUCCESS.getBytes());
    }

}
