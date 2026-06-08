package com.admin4j.spring.plugin.provider.manager;

import com.admin4j.spring.plugin.exception.ProviderNotFoundException;
import com.admin4j.spring.plugin.provider.Provider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * 策略提供者管理器，用于加载和管理所有实现了 {@link Provider} 接口的策略类。
 * <p>
 * 通过此管理器，可以用一行代码实现策略模式，避免重复的 if/else 或 for 循环逻辑。
 * 支持 Spring Boot 自动配置，也可手动配置 ApplicationContext。
 * </p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li>{@link #load(Class, Object)} - 加载单个策略，找不到返回 null</li>
 *     <li>{@link #loadOptional(Class, Object)} - 加载单个策略，返回 Optional</li>
 *     <li>{@link #loadOrThrow(Class, Object)} - 加载单个策略，找不到抛异常</li>
 *     <li>{@link #loadWithMatcher(Class, Object, BiPredicate)} - 使用自定义匹配逻辑</li>
 *     <li>{@link #loadAll(Class, Object)} - 加载所有匹配的策略</li>
 *     <li>{@link #all(Class)} - 获取所有策略实现</li>
 *     <li>{@link #clearCache()} - 清理缓存</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 基本用法 - 加载策略
 * PayWayHandler handler = ProviderManager.load(PayWayHandler.class, "Ali");
 * if (handler != null) {
 *     handler.handler(query);
 * }
 *
 * // 2. 使用 Optional 避免空指针
 * Optional<PayWayHandler> optional = ProviderManager.loadOptional(PayWayHandler.class, "Ali");
 * optional.ifPresent(h -> h.handler(query));
 *
 * // 3. 使用异常处理
 * try {
 *     PayWayHandler handler = ProviderManager.loadOrThrow(PayWayHandler.class, "Ali");
 *     handler.handler(query);
 * } catch (ProviderNotFoundException e) {
 *     log.error("找不到支付方式");
 * }
 *
 * // 4. 使用默认策略
 * PayWayHandler handler = ProviderManager.loadOrDefault(
 *     PayWayHandler.class,
 *     "Ali",
 *     defaultHandler
 * );
 *
 * // 5. 使用自定义匹配逻辑（如正则匹配）
 * Optional<PayWayHandler> handler = ProviderManager.loadWithMatcher(
 *     PayWayHandler.class,
 *     "AliPay_v2",
 *     (support, request) -> request.startsWith(support)
 * );
 *
 * // 6. 获取所有匹配的策略
 * List<PayWayHandler> handlers = ProviderManager.loadAll(PayWayHandler.class, "Ali");
 *
 * // 7. 清理缓存（动态刷新策略）
 * ProviderManager.clearCache(PayWayHandler.class);
 *
 * // 8. 验证策略配置（启动时检查重复）
 * ProviderManager.validateUniqueSupports(PayWayHandler.class);
 * }</pre>
 *
 * @author andanyang
 * @since 2022/6/26 13:50
 * @see Provider
 * @see ProviderNotFoundException
 */
public class ProviderManager {

    private ProviderManager() {
    }

    /**
     * Spring 的 IOC 容器。
     * <p>
     * 使用 Spring Boot 自动配置注入
     * 如果都不可用，需要手动通过 {@link #setApplicationContext(ApplicationContext)} 设置。
     * </p>
     */
    public static ApplicationContext APPLICATION_CONTEXT;

    /**
     * Provider 缓存，避免重复从 Spring 容器中查找
     */
    private static final Map<Class<?>, Collection<? extends Provider<?>>> CACHED_PROVIDERS = new ConcurrentHashMap<>();

    /**
     * 手动设置 ApplicationContext（用于非自动配置场景）
     * <p>
     * 当不使用 Spring Boot 或 admin4j-common-spring 包不可用时，
     * 可以通过此方法手动注入 ApplicationContext。
     * </p>
     *
     * @param applicationContext Spring 应用上下文
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
    }

    /**
     * 加载指定类型的 Provider，返回匹配策略标识的实现。
     * <p>
     * 如果找不到匹配的 Provider，返回 null。
     * 推荐使用 {@link #loadOptional(Class, Object)} 或 {@link #loadOrThrow(Class, Object)} 替代。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param support       策略标识
     * @param <T>           Provider 类型
     * @param <S>           策略标识类型
     * @return 匹配的 Provider 实现，找不到返回 null
     * @see #loadOptional(Class, Object)
     * @see #loadOrThrow(Class, Object)
     */
    public static <T extends Provider<S>, S> T load(Class<T> providerClass, S support) {
        Collection<T> providers = loadProvider(providerClass);
        for (Provider<?> provider : providers) {
            if (Objects.equals(provider.support(), support)) {
                return (T) provider;
            }
        }
        return null;
    }

    /**
     * 加载指定类型的 Provider，找不到时返回默认 Provider。
     * <p>
     * 适用于有默认策略的场景，确保总能返回一个可用的 Provider。
     * </p>
     *
     * @param providerClass    Provider 接口的 Class 对象
     * @param support          策略标识
     * @param defaultProvider  默认 Provider 实例（找不到时返回此实例）
     * @param <T>              Provider 类型
     * @param <S>              策略标识类型
     * @return 匹配的 Provider 实现，找不到返回 defaultProvider
     */
    public static <T extends Provider<S>, S> T load(Class<T> providerClass, S support, T defaultProvider) {
        T provider = load(providerClass, support);
        return provider != null ? provider : defaultProvider;
    }


    /**
     * 加载指定类型的 Provider，找不到时使用默认策略标识再查找一次。
     * <p>
     * 适用于有默认策略标识的场景，如找不到指定的策略，会尝试使用默认标识查找。
     * </p>
     *
     * @param providerClass  Provider 接口的 Class 对象
     * @param support        策略标识
     * @param defaultSupport 默认策略标识（找不到指定策略时使用此标识查找）
     * @param <T>            Provider 类型
     * @param <S>            策略标识类型
     * @return 匹配的 Provider 实现，找不到返回 null
     */
    public static <T extends Provider<S>, S> T loadWithDefaultSupport(Class<T> providerClass, S support, S defaultSupport) {
        T provider = load(providerClass, support);
        return provider != null ? provider : load(providerClass, defaultSupport);
    }

    /**
     * 加载指定类型的 Provider，找不到时抛出 {@link ProviderNotFoundException}。
     * <p>
     * 适用于必须找到策略的场景，明确表达找不到策略是一种异常情况。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param support       策略标识
     * @param <T>           Provider 类型
     * @param <S>           策略标识类型
     * @return 匹配的 Provider 实现
     * @throws ProviderNotFoundException 找不到匹配的 Provider
     * @see #loadOptional(Class, Object)
     */
    public static <T extends Provider<S>, S> T loadOrThrow(Class<T> providerClass, S support)
            throws ProviderNotFoundException {
        T provider = load(providerClass, support);
        if (provider == null) {
            throw new ProviderNotFoundException(providerClass, support);
        }
        return provider;
    }

    /**
     * 加载指定类型的 Provider，返回 Optional 避免空指针异常。
     * <p>
     * 推荐使用此方法替代 {@link #load(Class, Object)}，可以更安全地处理找不到策略的情况。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param support       策略标识
     * @param <T>           Provider 类型
     * @param <S>           策略标识类型
     * @return Optional 包装的 Provider 实现
     * @see #loadOrThrow(Class, Object)
     */
    public static <T extends Provider<S>, S> Optional<T> loadOptional(Class<T> providerClass, S support) {
        return Optional.ofNullable(load(providerClass, support));
    }

    /**
     * 使用自定义匹配逻辑加载 Provider。
     * <p>
     * 适用于需要复杂匹配逻辑的场景，如正则匹配、前缀匹配、范围匹配等。
     * </p>
     *
     * <h3>示例：</h3>
     * <pre>{@code
     * // 正则匹配
     * Optional<PayWayHandler> handler = ProviderManager.loadWithMatcher(
     *     PayWayHandler.class,
     *     "AliPay_v2",
     *     (support, request) -> Pattern.matches(support + ".*", request)
     * );
     *
     * // 前缀匹配
     * Optional<PayWayHandler> handler = ProviderManager.loadWithMatcher(
     *     PayWayHandler.class,
     *     "Ali",
     *     (support, request) -> request.startsWith(support)
     * );
     * }</pre>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param support       策略标识（用于匹配）
     * @param matcher       自定义匹配器，BiPredicate<Provider的support, 请求的support>
     * @param <T>           Provider 类型
     * @param <S>           策略标识类型
     * @return Optional 包装的 Provider 实现
     */
    public static <T extends Provider<S>, S> Optional<T> loadWithMatcher(
            Class<T> providerClass, S support, BiPredicate<S, S> matcher) {
        Collection<T> providers = loadProvider(providerClass);
        for (T provider : providers) {
            if (matcher.test(provider.support(), support)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }

    /**
     * 查找所有匹配指定策略标识的 Provider。
     * <p>
     * 适用于一对多的场景，即同一个策略标识可能有多个实现。
     * 返回的列表按 Spring 的 @Order 注解排序。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param support       策略标识
     * @param <T>           Provider 类型
     * @param <S>           策略标识类型
     * @return 所有匹配的 Provider 列表（按 Order 排序）
     */
    public static <T extends Provider<S>, S> List<T> loadAll(Class<T> providerClass, S support) {
        Collection<T> providers = loadProvider(providerClass);
        return providers.stream()
                .filter(p -> Objects.equals(p.support(), support))
                .collect(Collectors.toList());
    }

    /**
     * 返回指定类型的所有 Provider 实现。
     * <p>
     * 返回的集合按 Spring 的 @Order 注解排序。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param <T>           Provider 类型
     * @return 所有 Provider 实现的集合（按 Order 排序）
     */
    public static <T extends Provider<?>> Collection<T> all(Class<T> providerClass) {
        return loadProvider(providerClass);
    }

    /**
     * 清理所有 Provider 缓存。
     * <p>
     * 适用于动态刷新策略的场景，如策略配置发生变化时，
     * 清理缓存后下次加载会重新从 Spring 容器查找。
     * </p>
     */
    public static void clearCache() {
        CACHED_PROVIDERS.clear();
    }

    /**
     * 清理指定类型的 Provider 缓存。
     * <p>
     * 适用于只刷新特定策略的场景。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param <T>           Provider 类型
     */
    public static <T extends Provider<?>> void clearCache(Class<T> providerClass) {
        CACHED_PROVIDERS.remove(providerClass);
    }


    /**
     * 通过 class 获取所有的 Provider beans（带缓存）。
     * <p>
     * 内部方法，优先从缓存获取，缓存不存在则从 Spring 容器查找并缓存。
     * </p>
     *
     * @param providerClass Provider 接口的 Class 对象
     * @param <T>           Provider 类型
     * @return 所有 Provider 实现的集合（按 Order 排序）
     */
    private static <T extends Provider<?>> Collection<T> loadProvider(Class<T> providerClass) {
        return (Collection<T>) CACHED_PROVIDERS.computeIfAbsent(providerClass, key -> {
            // 确保ApplicationContext可用
            ensureApplicationContext();

            Map<String, T> beansOfType = APPLICATION_CONTEXT.getBeansOfType(providerClass);
            Collection<T> values = beansOfType.values();
            ArrayList<T> ts = new ArrayList<>(values);
            AnnotationAwareOrderComparator.sort(ts);
            return ts;
        });
    }

    /**
     * 确保 ApplicationContext 可用。
     * <p>
     * 内部方法，处理多种 ApplicationContext 来源：
     * 1. 自动配置注入（优先）
     * 3. 手动设置
     * </p>
     */
    private static void ensureApplicationContext() {
        if (APPLICATION_CONTEXT == null) {

            throw new IllegalStateException(
                    "ApplicationContext 未配置。请使用以下方式之一配置：" +
                            "\n1. 使用 Spring Boot 自动配置（推荐）" +
                            "\n2. 引入 admin4j-common-spring 依赖" +
                            "\n3. 手动调用 ProviderManager.setApplicationContext(context)");
        }
    }
}