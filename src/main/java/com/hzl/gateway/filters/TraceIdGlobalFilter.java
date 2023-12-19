package com.hzl.gateway.filters;

import com.hzl.gateway.constants.TraceIdConstant;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * description
 * 生产traceId的全局路由
 * @author hzl 2023/01/29 2:55 PM
 */
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// ID生成
		String traceId = UUID.randomUUID().toString();
		MDC.put(TraceIdConstant.LOG_TRACE_ID, traceId);
		ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate()
				.headers(h -> h.add(TraceIdConstant.TRACE_ID, traceId))
				.build();
		ServerWebExchange build = exchange.mutate().request(serverHttpRequest).build();
		return chain.filter(build);
	}

	/**
	 * <p>
	 * 返回的值越小优先级越高
	 * </p>
	 * 
	 * @author hzl 2023/01/29 2:56 PM
	 */
	@Override
	public int getOrder() {
		return 0;
	}
}
