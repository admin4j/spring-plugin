package com.admin4j.spring.plugin.autoconfigure;

import com.admin4j.spring.plugin.provider.manager.ProviderManager;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring Plugin 自动配置类。
 * <p>
 * 在 Spring Boot 应用启动时自动注入 ApplicationContext 到 ProviderManager，
 * 实现零配置使用策略模式。
 * </p>
 *
 * <h3>自动配置条件：</h3>
 * <ul>
 *     <li>存在 ApplicationContext 类（Spring 环境）</li>
 *     <li>引入 spring-plugin 包</li>
 * </ul>
 *
 * <h3>使用方式：</h3>
 * <p>
 * 在 Spring Boot 项目中引入 spring-plugin 依赖后，无需任何配置即可使用：
 * </p>
 * <pre>{@code
 * @Service
 * public class PayService {
 *     public String pay(String channel, ChargeQuery query) {
 *         // 无需手动配置 ApplicationContext，直接使用
 *         PayWayHandler handler = ProviderManager.loadOrThrow(PayWayHandler.class, channel);
 *         return handler.handler(query);
 *     }
 * }
 * }</pre>
 *
 * @author andanyang
 * @since 2026/6/8
 * @see ProviderManager
 */
@AutoConfiguration
@ConditionalOnClass(ApplicationContext.class)
public class PluginAutoConfiguration implements ApplicationContextAware {


    /**
     * 注入 ApplicationContext 到 ProviderManager。
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ProviderManager.setApplicationContext(applicationContext);
    }
}