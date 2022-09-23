package com.zz.scorder;

import com.alibaba.cloud.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import com.alibaba.cloud.sentinel.feign.SentinelFeign;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.zz.api.common.config.SentinelMybatisConfig;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.Request;
import feign.Target;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by Francis.zz on 2018/2/27.
 * 开启Eureka服务消费者，向Eureka服务注册中心注册服务
 * EnableFeignClients的EnableFeignClients指定需要扫描的feign client包
 */
@SpringBootApplication(scanBasePackages = {"com.zz.scservice.fallback", "com.zz.scorder"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zz.scservice", "com.zz.scorder.feignclient"})
//@EnableCircuitBreaker
@ImportAutoConfiguration({SentinelMybatisConfig.class})
@EnableCaching
public class ScOrderApplication {
    /**
     * OpenFeign 执行流程分析参考：https://www.cnblogs.com/chiangchou/p/api.html
     * @EnableFeignClients 注解开启FeignClient扫描，导入{@link org.springframework.cloud.openfeign.FeignClientsRegistrar}
     * 来扫描所有指定包下所有带有<code>FeignClient</code>注解的接口,
     * 通过{@link org.springframework.cloud.openfeign.FeignClientsRegistrar#registerFeignClients} 提取接口的注解信息来注册一个 FeignClientSpecification 的Bean，
     * 注意这里的 getClientName 方法，用来生成beanName名称。
     *
     * 并解析FeignClient注解的相关参数，用来生成Target(Feign代理客户端)
     *
     * Feign客户端默认由{@link org.springframework.cloud.openfeign.FeignClientsConfiguration.DefaultFeignBuilderConfiguration#feignBuilder}构建
     * sentinel feign由{@link com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration}构建，覆盖默认构建
     * 超时时间、request client等{@link Feign.Builder#build()}设置.
     * {@link org.springframework.cloud.openfeign.FeignClientFactoryBean#getTarget}里面有创建Client
     * apache http client创建{@link org.springframework.cloud.openfeign.FeignAutoConfiguration.HttpClientFeignConfiguration#feignClient}，
     * 注入{@link feign.Client}
     *   > {@link feign.httpclient.ApacheHttpClient#execute}
     *     > {@link org.apache.http.impl.client.CloseableHttpClient#execute}
     *       具体使用哪种httpclient由 {@link org.springframework.cloud.openfeign.FeignAutoConfiguration}的配置决定，比如 HttpClientFeignConfiguration
     *       但是httpclient是由{@link org.springframework.cloud.openfeign.clientconfig.HttpClientFeignConfiguration#createClient} 创建出来的
     *
     * <p>feignClient调用http client（或者ok http）完成请求</p>
     * {@link feign.httpclient.ApacheHttpClient}装饰了{@link HttpClient}的实现类，
     * 查看{@link org.springframework.cloud.openfeign.loadbalancer.HttpClientFeignLoadBalancerConfiguration#feignClient}
     * 
     * <h1>openfeign创建feignClient和解析springmvc参数</h1>
     * <p>使用了JDK的动态代理，为feign接口创建动态代理。{@link java.lang.reflect.InvocationHandler}和{@link java.lang.reflect.Proxy}</p>
     *
     * 使用{@link feign.Target.HardCodedTarget} 作为feign接口的目标代理类，
     *
     * > {@link org.springframework.cloud.openfeign.FeignClientsRegistrar#registerFeignClients(AnnotationMetadata, BeanDefinitionRegistry)}
     *   > {@link FeignClientFactoryBean#getObject()} 创建出接口的代理对象，注册为bean,这里会调用{@link FeignClientFactoryBean#feign}方法设置参数
     *     > getTarget 这里判断如果url没有值，只有服务名就会调用负载均衡相关的方法
     *         > DefaultTargeter.target
     *             > {@link Feign.Builder#target} 这里使用{@link feign.Target.HardCodedTarget}作为target参数
     *                 > {@link feign.ReflectiveFeign#newInstance}
     *                     > {@link feign.ReflectiveFeign.ParseHandlersByName#apply} 解析方法或参数的mvc注解
     *                         > {@link feign.Contract.BaseContract#parseAndValidateMetadata}，解析元数据metadata并缓存
     *                             > {@link org.springframework.cloud.openfeign.support.SpringMvcContract#processAnnotationOnMethod} 解析方法上的http请求方法注解
     *                             > {@link org.springframework.cloud.openfeign.support.SpringMvcContract#processAnnotationsOnParameter} 解析方法的参数上的注解，即参数传递方法
     *                     > factory.create 通过工厂模式创建出用来代理feign接口的代理类。例如{@link feign.ReflectiveFeign.FeignInvocationHandler}
     *                       {@link feign.ReflectiveFeign}的构造方法中设置InvocationHandlerFactory工厂对象
     *                          在接入sentinel后，这里的工厂类是{@link }
     *                     > Proxy.newProxyInstance 使用JDK的动态代理创建出代理类。具体执行时就是传入的代理对象参数，FeignInvocationHandler#invoke
     *
     * {@link org.springframework.cloud.openfeign.support.SpringMvcContract#getDefaultAnnotatedArgumentsProcessors} 默认支持参数解析的几种注解处理器
     *
     * <h1>timeout超时时间等参数的设置</h1>
     *  httpclient由{@link org.springframework.cloud.openfeign.clientconfig.HttpClientFeignConfiguration#createClient} 创建，
     *  这里暂时会用 `feign.httpclient.connection-timeout` 作为连接超时时间
     *
     * 创建feignClient时会使用{@link org.springframework.cloud.openfeign.FeignClientProperties#config}的配置`feign.client.config.default.read-timeout`，
     * 如果没有配置该属性就会使用默认的{@link feign.Request.Options}，分别是10s和60s。
     *   查看{@link FeignClientFactoryBean#configureFeign}。
     * {@link feign.httpclient.ApacheHttpClient}装饰了{@link HttpClient}，在调用httpclient前会封装一些参数，超时时间就是在这里赋值的。
     *   查看{@link feign.httpclient.ApacheHttpClient#toHttpUriRequest}。
     * 因此优先使用`feign.client.config.default.read-timeout`配置的超时时间，如果没有配置，就是默认的10s和60s。
     * 而`feign.httpclient.connection-timeout`的配置则无效，会被覆盖。
     *
     *
     * <h1>负载均衡<h1/>
     * 装配类{@link org.springframework.cloud.openfeign.loadbalancer.HttpClientFeignLoadBalancerConfiguration}
     * 需要引入 LoadBalancer 依赖才会启用
     * {@link org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient#execute(Request, Request.Options)}
     * 解析服务名获取其实例具体的ip端口信息
     * spring cloud2020默认不再集成ribbon，改用 LoadBalancer。并且需要手动引入LoadBalancer 依赖才能从注册中心解析服务名实现负载均衡
     *
     * <h1>sentinel-feign代理请求服务的步骤</h1>
     * 1. sentinel{@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler#invoke}进行限流、降级等拦截处理
     * 2. {@link feign.SynchronousMethodHandler#invoke}包裹重试逻辑，其中buildTemplateFromArgs.create会解析动态url(@PathVariable)，这是在sentinel降级拦截之后执行的。
     * 3. {@link feign.SynchronousMethodHandler#targetRequest} 是构建请求参数，target为Feign代理，执行之前会先执行{@link feign.RequestInterceptor}所有注册的请求拦截器，
     * 可以通过实现RequestInterceptor接口自定义一些操作，比如向Header添加参数，修改请求服务ID（多租户定制服务实现）等。
     * 4. {@link feign.SynchronousMethodHandler#executeAndDecode} 执行请求操作
     *
     * <h1>sentinel创建代理对象的过程</h1>
     * sentinel feign由{@link com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration}构建，覆盖默认构建的Feign.Builder
     * 需要配置feign.sentinel.enabled=true来启用sentinel的代理
     *
     * {@link FeignClientFactoryBean#getObject()} 创建出接口的代理对象
     * 这时{@link org.springframework.cloud.openfeign.FeignClientFactoryBean#getTarget}的
     * `Feign.Builder builder = feign(context)`{@link org.springframework.cloud.openfeign.FeignClientFactoryBean#feign}
     * 从上下文容器中获取到的就是{@link SentinelFeign.Builder#build()}。
     * 这里还会为Builder对象设置一些参数，比如 capabilities 属性，
     * {@link feign.Capability} 实现类{@link org.springframework.cloud.openfeign.CachingCapability}，在FeignAutoConfiguration中注册
     *   > {@link FeignClientFactoryBean#loadBalance}
     *     > getOptional(context, Client.class) 从上下文容器中获取注册的{@link feign.Client}接口的实现类，
     *       比如 {@link org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient}
     *       装饰{@link feign.httpclient.ApacheHttpClient}在HttpClientFeignLoadBalancerConfiguration中完成。
     *       设置feign builder的client属性
     *     > get(context, Targeter.class) 从上下文容器中获取注册的{@link org.springframework.cloud.openfeign.Targeter}接口的实现类，
     *       比如{@link org.springframework.cloud.openfeign.DefaultTargeter}
     *     > targeter.target 执行取到的Targeter实现类对象的tartget方法，{@link org.springframework.cloud.openfeign.DefaultTargeter#target}
     *       {@link Feign.Builder#target(Target)} 这里的target参数为{@link feign.Target.HardCodedTarget}对象
     *       > {@link Feign.Builder#build()} 这里对象的一些属性已是sentinel自定义的
     *         这里很多属性调用了{@link feign.Capability#enrich(Object, List)}方法，意思就是遍历capabilities列表，如果对象中的enrich方法返回值与第一个参数的属性类型相同，就会调用enrich方法封装属性。
     *         比如{@link org.springframework.cloud.openfeign.CachingCapability#enrich(InvocationHandlerFactory)}
     *         就会将前面sentinel设置的invocationHandlerFactory属性使用{@link org.springframework.cloud.openfeign.FeignCachingInvocationHandlerFactory}类装饰。
     *         build返回{@link feign.ReflectiveFeign}对象，该对象的InvocationHandlerFactory工厂类为FeignCachingInvocationHandlerFactory
     *         > {@link feign.ReflectiveFeign#newInstance(Target)}，这里会解析mvc的注解，查看“openfeign创建feignClient和解析springmvc参数”
     *           调用工厂方法{@link org.springframework.cloud.openfeign.FeignCachingInvocationHandlerFactory#create(Target, Map)} 创建出InvocationHandler实现类，
     *           先调用被装饰的sentinel的工厂方法，{@link SentinelFeign.Builder#build()}里面的内部类，
     *           这里会返回{@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler} ，feign接口代理最终会执行到SentinelInvocationHandler#invoke方法。
     *
     *           最后创建jdk动态代理类，并返回该对象。feign接口的方法实现执行时就会调用上面创建出来的InvocationHandler类的invoke方法。
     *
     * <h2>sentinel的Feign.Builder#build方法</h2>
     * {@link SentinelFeign.Builder#build()}
     * 最终还是返回了父类对象{@link Feign.Builder}，
     * contract属性设置为{@link com.alibaba.cloud.sentinel.feign.SentinelContractHolder}，在{@link feign.ReflectiveFeign.ParseHandlersByName#apply}调用是就会使用到
     * invocationHandlerFactory 属性设置为自定义的内部类
     *
     * <h1>sentinel熔断降级说明</h1>
     *
     * 通过{@link com.alibaba.csp.sentinel.spi.SpiLoader}加载指定类
     *   > 加载{@link com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder}
     *     > 加载com.alibaba.csp.sentinel.slotchain.ProcessorSlot文件中的类，放入{@link com.alibaba.csp.sentinel.slotchain.ProcessorSlot}的调用链中
     *
     * `SentinelInvocationHandler`代理请求方法
     * 1. 如果实现了fallback，那么只要请求服务报错就会执行fallback的降级方法(不管有没有触发降级配置)
     * @see {@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler#invoke}
     *   > SphU.entry 就是调用链条的entry方法，就是spi配置的ProcessorSlot接口实现类的entry方法，一般是预检查，
     *       还有是否熔断的检查，比如 `AbstractCircuitBreaker`熔断后就校验熔断时长的时间戳，如果熔断了就会直接抛出DegradeException
     *       > {@link com.alibaba.csp.sentinel.CtSph#entryWithPriority(ResourceWrapper, int, boolean, Object...)}
     *         捕获到BlockException异常就会调用链条的exit方法
     *         > 调用链条中ProcessorSlot实现类的entry方法，比如{@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot#entry}
     *   > 调用原始的被代理的方法，controller类的方法
     *   > 捕获到异常时判断有没有实现fallback，有就返回fallback的响应信息
     *   > finally方法会调用链条的exit
     *     > {@link com.alibaba.csp.sentinel.CtEntry#exitForContext(Context, int, Object...)}
     *       > 调用链条中ProcessorSlot实现类的exit方法，比如{@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot#exit(Context, ResourceWrapper, int, Object...)}
     *         记录总数和异常数，判断是否达到熔断阈值等操作
     *
     *
     * 2. RT配置：同1s内的请求数大于5({@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule#rtSlowRequestAmount})
     *    且平均响应时间大于阀值则在接下来的时间窗口触发熔断降级。
     * 其余规则配置具体参考：
     * https://mrbird.cc/Sentinel%E6%8E%A7%E5%88%B6%E5%8F%B0%E8%AF%A6%E8%A7%A3.html
     * https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7
     * <h2>DegradeSlot</h2>
     * 降级插槽{@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot}
     * {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot} 初始化统计节点
     *
     * <h1>限流(FlowRule)异常拦截</h1>
     * @see {@link com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration#sentinelWebMvcConfig} 注入BlockExceptionHandler，以及web处理相关参数
     * @see {@link com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration#sentinelWebInterceptor}注入异常处理拦截器，拦截并处理BlockException异常。
     * 这里也会获取resourceName对应的所有插槽，包括降级插槽。{@link com.alibaba.csp.sentinel.CtSph#lookProcessChain}.
     * 也就是说如果接口触发降级也会在这里的preHandler中抛出DegradeException.
     *
     * 可以通过spring.cloud.setinel.blockPage 设置异常响应重定向的页面
     * 否则默认使用{@link com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler}类处理
     *
     * <h1>sentinel控制台交互</h1>
     * <h2>sentinel客户端向sentinel-console上报心跳</h2>
     * 1. 通过{@link com.alibaba.csp.sentinel.Env}和{@link com.alibaba.csp.sentinel.cluster.ClusterStateManager} 调用 {@link InitExecutor#doInit}
     *    来初始化通过SPI配置的所有{@link com.alibaba.csp.sentinel.init.InitFunc}接口的实现类。
     * Env在sentinel过滤器中才会被调用，因此上报心跳是延迟的，等网关成功路由才会上报，所以这里第一次记录的metric信息就无法被console读取到，这就是为什么console统计的
     *    metric信息总是会少metric文件第1s的数据（服务启动后第一次成功路由后上报心跳的那一次metric信息）。
     * 可以自定义在服务启动时就调用{@link InitExecutor#doInit}，立马注册服务到sentinel-console。
     *
     * 2. sentinel-transport客户端服务上报信息是初始化的实现类 {@link com.alibaba.csp.sentinel.transport.init.HeartbeatSenderInitFunc}
     * 3. 最终调用通过SPI配置的接口{@link com.alibaba.csp.sentinel.transport.HeartbeatSender}
     *    的实现类{@link com.alibaba.csp.sentinel.transport.heartbeat.SimpleHttpHeartbeatSender}。请求地址是“/registry/machine”。
     * 不设置“csp.sentinel.dashboard.serve”参数console自身就不会注册，但是SimpleHttpHeartbeatSender的定时任务还是会继续运行。
     *
     * <h2>sentinel客户端开放接口</h2>
     * ClusterStateManager初始化通过SPI配置的所有{@link com.alibaba.csp.sentinel.init.InitFunc}接口的实现类
     * sentinel-transport客户端开启sentinel命令的接口 {@link com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc}
     * 最终调用通过SPI配置的接口{@link com.alibaba.csp.sentinel.transport.CommandCenter}
     * 的实现类 {@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}
     *
     * spring.cloud.sentinel.transport.port 配置的端口会在{@link com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter}
     * 中用来开启socket监听服务（如果端口被占用，则会自动使用别的端口）。
     * 然后使用{@link com.alibaba.csp.sentinel.transport.command.http.HttpEventTask} 类做实际的处理操作。
     * {@link com.alibaba.csp.sentinel.command.annotation.CommandMapping}注解用来注册接口实际处理逻辑。
     * {@link com.alibaba.csp.sentinel.command.CommandHandlerProvider} 注册Handler
     * 比如sentinel console实时监控界面获取metric信息的请求<code>metric</code>
     * 就是sentinel客户端的{@link com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler}类处理的。
     * 实际metric数据来源就是从sentinel的metric日志文件中读取的。
     * <p>如果需要新增CommandHandler实现，则需要在SPI文件com.alibaba.csp.sentinel.command.CommandHandler中注册CommandHandler实现类才会生效。</p>
     *
     * <h1>nacos动态规则</h1>
     * {@link com.alibaba.cloud.sentinel.SentinelProperties}注入 datasource 属性(即spring.cloud.sentinel.datasource属性配置)
     *   时自动初始化{@link com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties}或别的属性类，
     *   然后通过{@link NacosDataSourceFactoryBean#getObject()} 创建 {@link com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource} nacos数据源属性
     * 创建datasource的操作在{@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler#registerBean}
     *
     * 通过{@link com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties#postRegister(AbstractDataSource)}
     *   注册配置数据监听器，监听nacos等配置中心的数据改变。
     * 比如降级的配置就存放在{@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager#circuitBreakers}静态属性中。
     *   > {@link com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager.RulePropertyListener#buildCircuitBreakers} 通过配置的参数创建`CircuitBreaker`并缓存，
     *     熔断判断是根据资源名取出缓存。
     *
     * <h1>规则配置说明</h1>
     * 资源名：
     * {@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler#invoke(Object, Method, Object[])}
     * <code>String resourceName = methodMetadata.template().method().toUpperCase()
     *                      + ":" + hardCodedTarget.url() + methodMetadata.template().path();</code>
     *
     * 例如有如下的接口树：
     * <code>
     *     /createOrder
     *     ├── POST:http://sc-service1/320200/createOrder
     *     ├── mybatis:com.zz.scorder.dao.ConfigMapper.selectByCardCode
     * </code>
     * “/createOrder” 接口为当前服务对外接口
     * “POST:http://sc-service1/320200/createOrder” 为当前服务的createOrder接口需要调用的上游接口
     * 对“/createOrder”接口可以配置限流、降级，降级也会在prehandler中体现，如果降级的话就不会再执行Controller中的代码。所有如果需要定制客户端响应也需要在`BlockExceptionHandler`中处理
     * 而对于“POST:http://sc-service1/320200/createOrder”来说就只能配置降级了，因为限流是在Controller之前拦截的，这里的降级执行的就是 `InvocationHandler`。
     *
     * <h1>nacos、sentinel相关日志输出目录配置</h1>
     * 1. sentinel客户端metric记录日志(MetricWriter类)、transport接口上报日志(CommandCenterLog)、sentinel-record日志(RecordLog)：优先通过`csp.sentinel.log.dir`，
     *    如果没有则查找`spring.cloud.sentinel.log.dir` 属性配置。
     *    可以通过sentinel-core模块的{@link com.alibaba.csp.sentinel.log.jul.BaseJulLogger}设置等级或关闭，metric日志是直接写入文件的不会关闭。
     * 2. nacos-client日志通过重写`nacos-logback.xml`配置文件或者使用java启动命令行参数 `-Dnacos.logging.config=` 配置.
     *    {@link com.alibaba.nacos.client.logging.AbstractNacosLogging}
     * 3. nacos-SNAPSHOT日志通过命令行参数 `-DJM.SNAPSHOT.PATH=` 配置. {@link com.alibaba.nacos.client.config.impl.LocalConfigInfoProcessor}
     *
     * <h1>nacos-discovery服务注册</h1>
     * @see {@link com.alibaba.cloud.nacos.NacosDiscoveryProperties} 服务注册属性配置，比如可以配置客户端 IP(多网卡的服务器配置指定网卡)
     * @see {@link com.alibaba.cloud.nacos.registry.NacosRegistration}
     * @see {@link com.alibaba.cloud.nacos.registry.NacosServiceRegistry#register}
     * @see {@link com.alibaba.nacos.client.naming.NacosNamingService#registerInstance} 发送register请求，注册服务
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(ScOrderApplication.class, args);
    }
}
