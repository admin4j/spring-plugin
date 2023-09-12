package com.admin4j.spring.plugin.provider;

import com.admin4j.spring.plugin.BeanConfig;
import com.admin4j.spring.plugin.provider.manager.ProviderManager;
import com.admin4j.spring.plugin.provider.service.ChargeQuery;
import com.admin4j.spring.plugin.provider.service.PayWayHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * @author andanyang
 * @since 2023/9/12 14:55
 */
public class ProviderManagerTest {

    @Test
    public void testUsually() {
        ApplicationContext context = new AnnotationConfigApplicationContext(BeanConfig.class);

        Map<String, PayWayHandler> payWayHandlerMap = context.getBeansOfType(PayWayHandler.class);

        ChargeQuery chargeQuery = new ChargeQuery(new BigDecimal(100), "Ali");
        for (PayWayHandler payWayHandler : payWayHandlerMap.values()) {
            if (chargeQuery.getChannel().equals(payWayHandler.support())) {
                payWayHandler.handler(chargeQuery);
                break;
            }
        }
    }

    @Test
    public void testPayWay() {
        ApplicationContext context = new AnnotationConfigApplicationContext(BeanConfig.class);
        ProviderManager.APPLICATION_CONTEXT = context;

        //"Ali" 支付
        ChargeQuery aliChargeQuery = new ChargeQuery(new BigDecimal(100), "Ali");
        String handler = ProviderManager.load(PayWayHandler.class, aliChargeQuery.getChannel()).handler(aliChargeQuery);
        System.out.println("handler = " + handler);
        assert "使用了Ali支付了".equals(handler);

        //Wx 支付
        ChargeQuery wxChargeQuery = new ChargeQuery(new BigDecimal(101), "Wx");
        handler = ProviderManager.load(PayWayHandler.class, "Wx").handler(wxChargeQuery);
        System.out.println("handler = " + handler);
        assert "使用了Wx支付了".equals(handler);
    }

    @Test
    public void testAll() {
        ApplicationContext context = new AnnotationConfigApplicationContext(BeanConfig.class);
        ProviderManager.APPLICATION_CONTEXT = context;


        Collection<PayWayHandler> all = ProviderManager.all(PayWayHandler.class);

        for (PayWayHandler handler : all) {
            System.out.println("handler = " + handler.support());
        }

    }

}
