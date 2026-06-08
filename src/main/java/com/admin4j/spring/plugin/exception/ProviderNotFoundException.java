package com.admin4j.spring.plugin.exception;

import lombok.Getter;

/**
 * 当找不到匹配的策略提供者时抛出的异常。
 * <p>
 * 通常在使用 {@link com.admin4j.spring.plugin.provider.manager.ProviderManager#loadOrThrow(Class, Object)}
 * 方法时，如果指定的策略标识没有对应的 Provider 实现，就会抛出此异常。
 * </p>
 *
 * <h3>示例：</h3>
 * <pre>{@code
 * try {
 *     PayWayHandler handler = ProviderManager.loadOrThrow(PayWayHandler.class, "UnknownPay");
 *     handler.handler(query);
 * } catch (ProviderNotFoundException e) {
 *     // 处理找不到策略的情况
 *     log.error("找不到支付方式: {}", e.getSupport());
 * }
 * }</pre>
 *
 * @author andanyang
 * @since 2026/6/8
 * @see com.admin4j.spring.plugin.provider.manager.ProviderManager#loadOrThrow(Class, Object)
 */
@Getter
public class ProviderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -539296612656350794L;
    /**
     * 查找的策略提供者类型
     * -- GETTER --
     *  获取查找的策略提供者类型
     *
     * @return 策略提供者类型的 Class 对象

     */
    private final Class<?> providerClass;

    /**
     * 查找时使用的策略标识
     * -- GETTER --
     *  获取查找时使用的策略标识
     *
     * @return 策略标识对象

     */
    private final Object support;

    /**
     * 创建异常实例
     *
     * @param providerClass 策略提供者类型
     * @param support       策略标识
     */
    public ProviderNotFoundException(Class<?> providerClass, Object support) {
        super(String.format("找不到 Provider: %s, support: %s",
                providerClass.getSimpleName(), support));
        this.providerClass = providerClass;
        this.support = support;
    }

    /**
     * 创建异常实例，带自定义消息
     *
     * @param providerClass 策略提供者类型
     * @param support       策略标识
     * @param message       自定义错误消息
     */
    public ProviderNotFoundException(Class<?> providerClass, Object support, String message) {
        super(message);
        this.providerClass = providerClass;
        this.support = support;
    }

}