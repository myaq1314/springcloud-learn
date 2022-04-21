package com.zz.gateway.dubbo.core.annotation;

import com.zz.gateway.client.core.annotation.ParamAttribute;

public class ParamInfo {
    public ParamInfo() {
    }

    public ParamInfo(int index, String paramName, ParamAttribute.ParamFromType paramFromType, String paramType, ParamAttribute.ParamFormat paramFormat, boolean simpleType, boolean childAllSimpleType, boolean required) {
		this.index=index;
		this.paramName=paramName;
		this.paramFromType=paramFromType;
		this.paramType=paramType;
		this.simpleType=simpleType;
		this.childAllSimpleType=simpleType;
		this.required=required;
		this.paramFormat=paramFormat;
	}

	private int index;
	
	private String paramName;
	
	private ParamAttribute.ParamFromType paramFromType;
	
	private String paramType;
	
	private boolean simpleType;
	
	private boolean childAllSimpleType;
	
	private boolean required;
	
	private ParamAttribute.ParamFormat paramFormat;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public ParamAttribute.ParamFromType getParamFromType() {
		return paramFromType;
	}

	public void setParamFromType(ParamAttribute.ParamFromType paramFromType) {
		this.paramFromType = paramFromType;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public boolean isSimpleType() {
		return simpleType;
	}

	public void setSimpleType(boolean simpleType) {
		this.simpleType = simpleType;
	}

	public boolean isChildAllSimpleType() {
		return childAllSimpleType;
	}

	public void setChildAllSimpleType(boolean childAllSimpleType) {
		this.childAllSimpleType = childAllSimpleType;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public ParamAttribute.ParamFormat getParamFormat() {
		return paramFormat;
	}

	public void setParamFormat(ParamAttribute.ParamFormat paramFormat) {
		this.paramFormat = paramFormat;
	}
}
