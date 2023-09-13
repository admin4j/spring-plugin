# 一行代码搞定Spring策略模式

在Spring中大量使用策略模式来简化`if/else`代码,比如Spring Security 的各种`AuthenticationProvider`等等，但是实现方式过于麻烦，使用重复套路来实现。

## 一般基于Spring的策略实现

> 场景：关于用户订单充值（订单支付同理），我们都知道，现今的支付方式是非常的多的，例如：支付宝、微信、银联、钱包（各个APP的账户余额）等等。

**查询实体Query：**

```
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeQuery {
    private BigDecimal money;
    private String channel;
}
```

**支付接口**

```
public interface PayWayHandler {
    String handler(ChargeQuery query);
}
```

**阿里支付功能实现**

```
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
```

**微信支付实现**

```
@Service
@Order(12)
public class WxPayWayHandler implements PayWayHandler {

    /**
     * 支持什么类型
     *
     * @return
     */
    @Override
    public String support() {
        return "Wx";
    }

    @Override
    public String handler(ChargeQuery query) {
        System.out.println("query = " + query);
        return "使用了" + support() + "支付了";
    }
}
```

### **支付调用**

```
        Map<String, PayWayHandler> payWayHandlerMap = context.getBeansOfType(PayWayHandler.class);

        ChargeQuery chargeQuery = new ChargeQuery(new BigDecimal(100), "Ali");
        for (PayWayHandler payWayHandler : payWayHandlerMap.values()) {
        //匹配到支持的渠道类型
            if (chargeQuery.getChannel().equals(payWayHandler.support())) {
                payWayHandler.handler(chargeQuery);
                break;
            }
        }
```

可以看到这边通过for循环来查找实现bean，虽然相比不用策略模式使用`if/else`
简单多了。但是遇到其他的业务或者其他的策略模式，这边的for循环逻辑还是需要再写一遍。基于封装原则，为了减少重复代码，我为大家提供了`spring-plugin`
工具类，实现一行代码搞定列策略模式。下面进入正文。

## spring-plugin实现策略模式

### POM引入

```
<dependency>
    <groupId>com.admin4j.spring</groupId>
    <artifactId>spring-plugin</artifactId>
    <version>0.8.1</version>
</dependency> 
```

[最新版查看：https://central.sonatype.com/artifact/com.admin4j.spring/spring-plugin](https://central.sonatype.com/artifact/com.admin4j.spring/spring-plugin)

### 实现代码

```
//"Ali" 支付
ChargeQuery aliChargeQuery = new ChargeQuery(new BigDecimal(100), "Ali");
String handler = ProviderManager.load(PayWayHandler.class, aliChargeQuery.getChannel()).handler(aliChargeQuery);
```

最终使用`String handler = ProviderManager.load(PayWayHandler.class, aliChargeQuery.getChannel()).handler(aliChargeQuery);`
一行代码搞定了策略模式，减少了重复代码块。

### 实现原理

实现原理也是相当的简单,看一遍就懂了

```
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
```

