package xyz.rpc.framework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import xyz.rpc.framework.service.UserService;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class BusiServiceImpl {

    @Autowired
    private UserService typeInjectService;

    @Autowired
    @Qualifier("userService")
    private UserService nameInjectService;

    @PostConstruct
    public void init() {
        Assert.notNull(typeInjectService, "type service null");
        Assert.notNull(nameInjectService, "name service null");
        log.info("init pass");
    }

}
