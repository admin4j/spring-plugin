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
     * Spring 的 IOC 容器
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
    public static <T extends Provider<?>> T load(Class<T> providerClass, String support) {

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
