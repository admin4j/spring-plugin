package com.admin4j.spring.plugin.provider;

/**
 * 策略提供者接口，用于实现策略模式。
 * <p>
 * 所有策略实现类都需要实现此接口，并通过 {@link #support()} 方法声明自己支持的策略类型。
 * {@link com.admin4j.spring.plugin.provider.manager.ProviderManager} 会根据 support() 返回值来匹配和加载对应的策略实现。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 定义支付策略接口
 * public interface PayWayHandler extends Provider<String> {
 *     String handler(ChargeQuery query);
 * }
 *
 * // 实现支付宝支付策略
 * @Service
 * public class AliPayWayHandler implements PayWayHandler {
 *     @Override
 *     public String support() {
 *         return "Ali";  // 声明支持支付宝
 *     }
 *
 *     @Override
 *     public String handler(ChargeQuery query) {
 *         return "使用支付宝支付";
 *     }
 * }
 *
 * // 使用 ProviderManager 加载策略
 * PayWayHandler handler = ProviderManager.load(PayWayHandler.class, "Ali");
 * String result = handler.handler(query);
 * }</pre>
 *
 * @param <T> 策略标识的类型，可以是 String、Integer、Enum 等任意类型
 * @author andanyang
 * @since 2022/6/26 13:50
 * @see com.admin4j.spring.plugin.provider.manager.ProviderManager
 * @see StringProvider
 */
public interface Provider<T> {

    /**
     * 返回此策略提供者支持的策略标识。
     * <p>
     * {@link com.admin4j.spring.plugin.provider.manager.ProviderManager} 会使用此返回值来匹配对应的策略实现。
     * </p>
     *
     * @return 支持的策略标识，类型由泛型参数 T 决定
     */
    T support();
}
