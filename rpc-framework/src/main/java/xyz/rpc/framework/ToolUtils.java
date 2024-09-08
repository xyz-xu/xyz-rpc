package xyz.rpc.framework;

import cn.hutool.core.util.ClassUtil;
import com.alibaba.fastjson.JSON;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工具类
 *
 * @author xin.xu
 */
@UtilityClass
public class ToolUtils {

    /**
     * 扫描给定包下有哪些接口类
     *
     * @param packageName 包名
     */
    public Set<Class<?>> scanInterfaces(String packageName) {
        return ClassUtil
                .scanPackage(packageName)
                .stream()
                .filter(Class::isInterface)
                .collect(Collectors.toSet());
    }

    /**
     * 将给定字符串首字母改为小写
     *
     * @param str 字符串
     */
    public String firstCharToLowercase(String str) {
        if (StringUtils.hasText(str)) {
            return str.substring(0, 1).toLowerCase() + str.substring(1);
        } else {
            return str;
        }
    }

    /**
     * 序列化
     *
     * @return
     */
    public byte[] serializeBodyBO(RpcCallBodyBO bo) {
        return JSON.toJSONBytes(bo);
    }

    /**
     * 反序列化
     */
    public RpcCallBodyBO deserializeBodyBO(byte[] bytes) {
        return JSON.parseObject(bytes, RpcCallBodyBO.class);
    }

    /**
     * 序列化
     */
    public String serializeHeaderBO(RpcCallHeaderBO bo) {
        return JSON.toJSONString(bo);
    }

    /**
     * 反序列化
     */
    public RpcCallHeaderBO deserializeHeaderBO(String str) {
        return JSON.parseObject(str, RpcCallHeaderBO.class);
    }

    public byte[] serializeResult(Object result) {
        return JSON.toJSONBytes(result);
    }

    public <T> T deserializeResult(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }

    public boolean isStreamParameter(Class<?>[] parameterTypes) {
        return parameterTypes != null && parameterTypes.length > 0 && Objects.equals(parameterTypes[0], InputStream.class);
    }

    public boolean isStreamReturnType(Class<?> clazz) {
        return Objects.equals(clazz, InputStream.class);
    }

    public boolean isVoidReturnType(Class<?> clazz) {
        return Objects.equals(clazz, Void.class) || Objects.equals(clazz, Void.TYPE);
    }

}
