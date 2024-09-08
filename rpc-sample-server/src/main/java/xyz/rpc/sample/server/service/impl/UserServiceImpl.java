package xyz.rpc.sample.server.service.impl;

import org.springframework.stereotype.Service;
import xyz.rpc.sample.api.UserService;
import xyz.rpc.sample.api.vo.FundVO;
import xyz.rpc.sample.api.vo.MapperVO;
import xyz.rpc.sample.api.vo.ResultVO;

/**
 * 接口实现类
 *
 * @author xin.xu
 */
@Service
public class UserServiceImpl implements UserService {

    private String version = "1.0.0";

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void updateVersion(String version) {
        this.version = version;
    }

    @Override
    public void updateVersion(String format, Object[] args) {
        this.version = String.format(format, args);
    }

    @Override
    public ResultVO busi(FundVO fundVO, MapperVO mapperVO) {
        ResultVO resultVO = new ResultVO();
        resultVO.setFundVO(fundVO);
        resultVO.setMapperVO(mapperVO);
        resultVO.setResult(fundVO.toString() + mapperVO.toString());
        return resultVO;
    }

}
