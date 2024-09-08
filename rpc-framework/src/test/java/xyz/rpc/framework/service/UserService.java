package xyz.rpc.framework.service;


/**
 * 远程调用接口类
 *
 * @author xin.xu
 */
public interface UserService {
    String getVersion();

    void updateVersion(String version);

    void updateVersion(String format, Object value);
}
