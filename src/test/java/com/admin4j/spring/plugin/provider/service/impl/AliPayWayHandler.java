package com.admin4j.spring.plugin.provider.service.impl;

import com.admin4j.spring.plugin.provider.service.ChargeQuery;
import com.admin4j.spring.plugin.provider.service.PayWayHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author andanyang
 * @since 2023/9/12 15:07
 */
@Service
@Order(9)
public class AliPayWayHandler implements PayWayHandler {

    /**
     * 支持什么类型
     *
     * @return
     */
    @Override
    public String support() {
        return "Ali";
    }

    @Override
    public String handler(ChargeQuery query) {
        System.out.println("query = " + query);
        return "使用了" + support() + "支付了";
    }
}
