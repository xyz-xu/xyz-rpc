package xyz.rpc.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于表示接口中的所有方法/具体某个方法，支持重复调用
 *
 * @author xin.xu
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportRecall {

    /**
     * 重试的次数上限
     */
    int value() default Integer.MAX_VALUE;

}
