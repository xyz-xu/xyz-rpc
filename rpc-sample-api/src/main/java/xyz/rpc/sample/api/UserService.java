package xyz.rpc.sample.api;


import xyz.rpc.sample.api.vo.FundVO;
import xyz.rpc.sample.api.vo.MapperVO;
import xyz.rpc.sample.api.vo.ResultVO;

/**
 * 远程调用接口类
 *
 * @author xin.xu
 */
public interface UserService {

    String getVersion();

    void updateVersion(String version);

    void updateVersion(String format, Object[] args);

    ResultVO busi(FundVO fundVO, MapperVO mapperVO);

}
