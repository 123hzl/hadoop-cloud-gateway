package com.hzl.gateway.nacos;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.hzl.gateway.predicate.RegisterPredicate;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * description
 * 获取nacos注册中心服务信息
 * 后面添加url接口，供其他服务发布完成后，发送通知给路由更新路由信息
 * @author hzl 2021/09/25 6:55 PM
 */
@Slf4j
@Component
public class NacosDynamicRouteService implements ApplicationEventPublisherAware {
//	private String dataId = "gateway-router";
//	private String group = "DEFAULT_GROUP";
//	@Value("${spring.cloud.nacos.discovery.server-addr}")
//	private String serverAddr;

	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;

	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	@Qualifier("nacosDiscoveryClient")
	private DiscoveryClient discoveryClient;

	private static final List<String> ROUTE_LIST = new ArrayList<>();

	@PostConstruct
	public void dynamicRouteByNacosListener() {


		List<String> serviceIds=discoveryClient.getServices();
		//注册中心的注册信息不为空
		if(!CollectionUtils.isEmpty(serviceIds)){
			//清空历史路由信息
			ROUTE_LIST.addAll(serviceIds);
			//clearRoute();

			serviceIds.forEach(serviceId->{
				log.info("服务名"+serviceId);
				//根据nacos服务id获取具体的服务信息
				List<ServiceInstance> serviceInstances=discoveryClient.getInstances(serviceId);
				//服务具体信息不为空，正常gateway启动的时候服务具体信息还没有注册到nacos
				if(!CollectionUtils.isEmpty(serviceInstances)){
					//手动封装路由对象，包括过滤器，断言，路径

					serviceInstances.forEach(serviceInstance -> {
						log.info("获取注册信息port"+serviceInstance.getPort());
						log.info("获取注册信息host"+serviceInstance.getHost());
						log.info("获取注册信息uri"+serviceInstance.getUri());
						log.info("获取注册信息serviceId"+serviceInstance.getServiceId());
						log.info("获取注册信息getInstanceId"+serviceInstance.getInstanceId());

						addRoute(convertToRouteDefinition(serviceInstance));
					});

				}
			});
			//发布
			publish();

		}

	}

	private void clearRoute() {
		for (String id : ROUTE_LIST) {
			this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
		}
		ROUTE_LIST.clear();
	}

	private void addRoute(RouteDefinition definition) {
		try {
			routeDefinitionWriter.save(Mono.just(definition)).subscribe();
			ROUTE_LIST.add(definition.getId());
		} catch (Exception e) {
			log.error("addRoute error" + e);
		}
	}

	private void publish() {
		this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this.routeDefinitionWriter));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}


	//转换路由对象
	private RouteDefinition convertToRouteDefinition(ServiceInstance serviceInstance){
		RouteDefinition routeDefinition=new RouteDefinition();
		routeDefinition.setId(serviceInstance.getServiceId());
		//设置断言
		routeDefinition.setPredicates(RegisterPredicate.getList());
		//设置路径
		routeDefinition.setUri(serviceInstance.getUri());

		//设置拦截器，暂时没弄todo
		//routeDefinition.setFilters(null);
		return routeDefinition;
	}
}
