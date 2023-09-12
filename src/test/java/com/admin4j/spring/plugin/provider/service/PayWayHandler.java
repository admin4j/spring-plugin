package com.admin4j.spring.plugin.provider.service;

import com.admin4j.spring.plugin.provider.StringProvider;

/**
 * @author andanyang
 * @since 2023/9/12 15:03
 */
public interface PayWayHandler extends StringProvider {
    String handler(ChargeQuery query);
}
