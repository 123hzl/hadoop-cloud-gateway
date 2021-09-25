package com.hzl.gateway.nacos;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * description
 * 获取nacos注册中心服务信息
 *
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
		if(!CollectionUtils.isEmpty(serviceIds)){
			serviceIds.forEach(serviceId->{
				log.info("服务名"+serviceId);

				List<ServiceInstance> serviceInstances=discoveryClient.getInstances(serviceId);
				if(!CollectionUtils.isEmpty(serviceInstances)){
					serviceInstances.forEach(serviceInstance -> {
						log.info("获取注册信息port"+serviceInstance.getPort());
						log.info("获取注册信息host"+serviceInstance.getHost());
						log.info("获取注册信息uri"+serviceInstance.getUri());
						log.info("获取注册信息serviceId"+serviceInstance.getServiceId());


					});

				}
			});

		}

		//clearRoute();
		//addRoute(routeDefinition);
		//publish();

//		try {
//			ConfigService configService = NacosFactory.createConfigService(serverAddr);
//			configService.getConfig(dataId, group, 5000);
//			configService.addListener(dataId, group, new Listener() {
//				@Override
//				public void receiveConfigInfo(String configInfo) {
//					clearRoute();
//					try {
//						if (StringUtil.isNullOrEmpty(configInfo)) {//配置被删除
//							return;
//						}
//						List<RouteDefinition> gatewayRouteDefinitions = JSONObject.parseArray(configInfo, RouteDefinition.class);
//						for (RouteDefinition routeDefinition : gatewayRouteDefinitions) {
//							addRoute(routeDefinition);
//						}
//						publish();
//					} catch (Exception e) {
//						log.error("receiveConfigInfo error" + e);
//					}
//				}
//
//				@Override
//				public Executor getExecutor() {
//					return null;
//				}
//			});
//		} catch (NacosException e) {
//			log.error("dynamicRouteByNacosListener error" + e);
//		}
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
}
