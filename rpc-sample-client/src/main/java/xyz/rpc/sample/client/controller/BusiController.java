package xyz.rpc.sample.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import xyz.rpc.sample.api.UserService;
import xyz.rpc.sample.api.vo.FundVO;
import xyz.rpc.sample.api.vo.MapperVO;
import xyz.rpc.sample.api.vo.ResultVO;

import java.time.LocalDateTime;

/**
 * 业务 controller
 *
 * @author xin.xu
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class BusiController {

    private final UserService userService;

    /**
     * 业务接口 1
     */
    @GetMapping("/getVersion")
    public String getVersion() {
        return userService.getVersion();
    }

    /**
     * 业务接口2
     */
    @PostMapping("/updateVersion")
    public String updateVersion(String format, String value) {
        if (StringUtils.hasText(format)) {
            Object[] splits = value.split(",");
            userService.updateVersion(format, splits);
        } else {
            userService.updateVersion(value);
        }
        return "SUCCESS";
    }

    /**
     * 业务接口3
     */
    @PostMapping("/busi3")
    public ResultVO busi3(@RequestBody MapperVO mapperVO) {
        FundVO fundVO = new FundVO();
        BeanUtils.copyProperties(mapperVO, fundVO);
        fundVO.setBytes("123".getBytes());
        fundVO.setO(new Object());
        fundVO.setDateTime(LocalDateTime.now());

        // call
        ResultVO resultVO = userService.busi(fundVO, mapperVO);

        return resultVO;
    }

}
