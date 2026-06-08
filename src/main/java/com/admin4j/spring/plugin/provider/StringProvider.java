package com.admin4j.spring.plugin.provider;

/**
 * 字符串类型的策略提供者接口。
 * <p>
 * 这是一个便捷接口，等同于 {@code Provider<String>}。
 * 当策略标识为字符串类型时，可以直接使用此接口，减少泛型声明的繁琐。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 定义支付策略接口
 * public interface PayWayHandler extends StringProvider {
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
 * }</pre>
 *
 * @author andanyang
 * @since 2022/6/26 13:50
 * @see Provider
 * @see com.admin4j.spring.plugin.provider.manager.ProviderManager
 */
public interface StringProvider extends Provider<String> {


}
