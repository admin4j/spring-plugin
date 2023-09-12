package com.admin4j.spring.plugin.provider;

/**
 * @author andanyang
 * @since 2022/6/26 13:50
 */
public interface Provider<T> {

    /**
     * 支持什么类型
     *
     * @return
     */
    T support();
}
