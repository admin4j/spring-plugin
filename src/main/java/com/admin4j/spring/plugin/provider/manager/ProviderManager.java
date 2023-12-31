package com.admin4j.spring.plugin.provider.manager;

import com.admin4j.spring.plugin.provider.Provider;
import com.admin4j.spring.util.SpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author andanyang
 * @since 2022/6/26 13:50
 */
public class ProviderManager {

    private ProviderManager() {
    }

    /**
     * Spring 的 IOC 容器,默认为空。
     * 默认使用了 SpringUtils（admin4j-common-spring包里） 来获取ApplicationContext。如果不想多引入`admin4j-common-spring`包的话，可将他排除
     */
    public static ApplicationContext APPLICATION_CONTEXT;
    private static final Map<Class<Provider<?>>, Collection<Provider<?>>> CACHED_PROVIDERS = new ConcurrentHashMap<>();

    /**
     * 加载  Provider
     *
     * @param providerClass 给定的Class
     * @param support       支持的策略
     * @return 最终的支持策略的 Provider
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
     * 返回所有的  Provider
     *
     * @param providerClass 给定的Class
     * @return 所有的 Provider
     */
    public static <T extends Provider<?>> Collection<T> all(Class<T> providerClass) {

        return loadProvider(providerClass);
    }

    /**
     * 通过class获取所有的 beans
     */
    private static <T extends Provider<?>> Collection<T> loadProvider(Class<T> providerClass) {

        return (Collection<T>) CACHED_PROVIDERS.computeIfAbsent((Class<Provider<?>>) providerClass, key -> {

            if (APPLICATION_CONTEXT == null) {
                APPLICATION_CONTEXT = SpringUtils.getApplicationContext();
            }
            Map<String, T> beansOfType = APPLICATION_CONTEXT.getBeansOfType(providerClass);
            Collection<T> values = beansOfType.values();
            ArrayList<T> ts = new ArrayList<>(values);
            AnnotationAwareOrderComparator.sort(ts);
            return (Collection<Provider<?>>) ts;
        });
    }
}
