package com.zz.gateway.dubbo.core.context;

import com.zz.gateway.dubbo.core.annotation.DubboApiWrapper;
import com.zz.gateway.client.core.annotation.ParamAttribute;
import com.zz.gateway.dubbo.core.annotation.ParamInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DubboApiContext {

    /**
     * path非正则时，PathMapping注解设置的path与该path所在的接口实现类的映射关系
     */
	public final static Map<String, DubboApiWrapper> MAP_DUBBO_API_WRAPPER = new HashMap<String, DubboApiWrapper>();
    /**
     * path为正则时，PathMapping注解设置的path与该path所在的接口实现类的映射关系
     */
	public final static Map<String, DubboApiWrapper> MAP_DUBBO_API_PATH_PATTERN_WRAPPER = new HashMap<String, DubboApiWrapper>();

	public static Map<String, Map<ParamAttribute.ParamFromType, List<ParamInfo>>> MAP_PARAM_INFO = new HashMap<String, Map<ParamAttribute.ParamFromType, List<ParamInfo>>>();
	public static Map<String, Class<?>> MAP_CLASSES = new HashMap<String, Class<?>>();

	public static String CHARSET = "UTF-8";
}
