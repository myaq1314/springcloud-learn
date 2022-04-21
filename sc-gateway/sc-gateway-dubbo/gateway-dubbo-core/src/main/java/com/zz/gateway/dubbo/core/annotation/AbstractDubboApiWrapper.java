package com.zz.gateway.dubbo.core.annotation;

import com.zz.gateway.client.core.annotation.ParamAttribute;
import com.zz.gateway.dubbo.core.context.DubboApiContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public abstract class AbstractDubboApiWrapper extends AbstractBaseApiWrapper implements DubboApiWrapper {

	@Override
	public CompletableFuture<Object> handler(String pathPattern, ServerWebExchange exchange, Object body) {
		throw new UnsupportedOperationException();
	}

	private String decodeUrlEncode(String value) {
		if (!StringUtils.isEmpty(value)) {
			try {
				value = java.net.URLDecoder.decode(value, DubboApiContext.CHARSET);
			} catch (UnsupportedEncodingException e) {
				log.error("decode fail", e);
			}
		}
		return value;
	}

	protected void handlerConvertParams(String pathPattern, ServerWebExchange exchange, Object[] params, Object body)
			throws InterruptedException, ExecutionException, IllegalAccessException, InvocationTargetException,
			InstantiationException {
		ServerHttpRequest serverHttpRequest = exchange.getRequest();
		final Map<ParamAttribute.ParamFromType, List<ParamInfo>> mapGroupByParamType = DubboApiContext.MAP_PARAM_INFO.get(pathPattern);
		final Map<String, String> mapPathParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// cookie
		List<ParamInfo> listParams = mapGroupByParamType.get(ParamAttribute.ParamFromType.FROM_COOKIE);
		if (!CollectionUtils.isEmpty(listParams)) {
			serverHttpRequest.getCookies().forEach((key, values) -> {
				if (values != null && !values.isEmpty()) {
					mapPathParams.put(key, decodeUrlEncode(values.get(0).getValue()));
				}
			});
			convertParam(listParams, mapPathParams, params);
		}

		// body
		listParams = mapGroupByParamType.get(ParamAttribute.ParamFromType.FROM_BODY);
		if (!CollectionUtils.isEmpty(listParams)) {
			if (listParams.size() > 1) {
				throw new IllegalArgumentException("body Parameter verification exception");
			}
			convertBodyToParam(listParams.get(0), body, params);
		}
		// header
		listParams = mapGroupByParamType.get(ParamAttribute.ParamFromType.FROM_HEADER);
		if (!CollectionUtils.isEmpty(listParams)) {
			serverHttpRequest.getHeaders().toSingleValueMap().forEach((key, value) -> {
				mapPathParams.put(key, decodeUrlEncode(value));
			});
			convertParam(listParams, mapPathParams, params);
		}
		// path
		listParams = mapGroupByParamType.get(ParamAttribute.ParamFromType.FROM_PATH);
		if (!CollectionUtils.isEmpty(listParams)) {
			pathMatcher.extractUriTemplateVariables(pathPattern, serverHttpRequest.getPath().value())
					.forEach((key, value) -> {
						mapPathParams.put(key, decodeUrlEncode(value));
					});
			convertParam(listParams, mapPathParams, params);
		}

		// queryParams
		listParams = mapGroupByParamType.get(ParamAttribute.ParamFromType.FROM_QUERYPARAMS);
		if (!CollectionUtils.isEmpty(listParams)) {
			serverHttpRequest.getQueryParams().toSingleValueMap().forEach((key, value) -> {
				mapPathParams.put(key, decodeUrlEncode(value));
			});
			convertParam(listParams, mapPathParams, params);
		}
		// from attribute
		listParams = mapGroupByParamType.get(ParamAttribute.ParamFromType.FROM_ATTRIBUTE);
		if (!CollectionUtils.isEmpty(listParams)) {
			listParams.forEach(o -> {
				convertAttriToParam(o, exchange.getAttribute(o.getParamName()), params);
			});
		}
	}

}
