package com.hzl.gateway.route;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description
 *
 * @author hzl 2021/09/25 6:45 PM
 */
@Configuration
public class RouterInfos {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("register", r -> r.path("/rgst")
						.uri("https://blog.csdn.net"))
				.build();
	}
}
