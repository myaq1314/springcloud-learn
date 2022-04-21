package com.zz.gateway.dubbo.core.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class JacksonSerialization implements Serialization {

	private final ObjectMapper mapper;

	public JacksonSerialization() {
		mapper = new ObjectMapper();
		initMapper(mapper);
	}

	private void initMapper(ObjectMapper mapperTemp) {
		mapperTemp.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// 允许属性名称没有引号
		mapperTemp.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		// 允许单引号
		mapperTemp.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		// 设置输入时忽略在json字符串中存在但在java对象实际没有的属性
		mapperTemp.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// 设置输出时包含属性的风格
		mapperTemp.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		// 忽略大小写
		mapperTemp.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}

	@Override
	public String serialize(Object value) {
		if (value == null) {
			return null;
		}
		try {
            return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.error(" toJsonString error", e);
		}
		return null;
	}

	@Override
	public byte[] serializeByte(Object value) {
		if (value == null) {
			return null;
		}
		try {
            return mapper.writeValueAsBytes(value);
		} catch (JsonProcessingException e) {
			log.error(" toJsonByte error", e);
		}
		return null;
	}

	@Override
	public <T> T deserialize(String input, Class<T> clazz) {
		if(StringUtils.isEmpty(input)) {
			return null;
		}
		T t = null;
		try {
            t = mapper.readValue(input, clazz);
		} catch (Exception e) {
			log.error(" parse json to class [{}] ", clazz.getSimpleName());
		}
		return t;
	}

	@Override
	public <T> T convertValue(Object obj, Class<T> clazz) {
		if(null==obj) {
			return null;
		}
		T t = null;
		try {
            t = mapper.convertValue(obj, clazz);
		} catch (Exception e) {
			log.error(" parse json to class [{}] error", clazz.getSimpleName());
		}
		return t;
	}
}
