package com.zz.gateway.dubbo.core.annotation;

import com.zz.gateway.dubbo.core.config.DubboReferenceConfigProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultResponseResult implements ResponseReactiveResult {

	public DefaultResponseResult(DubboReferenceConfigProperties dubboReferenceConfigProperties) {

	}

	@Override
	public Mono<Void> reactiveFluxResponse(ServerWebExchange exchange, ServerHttpResponse response,
			Flux<DataBuffer> strDataBuffer) {
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		response.setStatusCode(HttpStatus.OK);
		return response.writeWith(strDataBuffer);
	}

}
