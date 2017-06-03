package com.fortify.processrunner.context.mapper;

public abstract class AbstractContextPropertyMapper implements IContextPropertyMapper {
	private String contextPropertyName;
	private boolean defaultValuesGenerator = true;
	
	public String getContextPropertyName() {
		return contextPropertyName;
	}
	public void setContextPropertyName(String contextPropertyName) {
		this.contextPropertyName = contextPropertyName;
	}
	public boolean isDefaultValuesGenerator() {
		return defaultValuesGenerator;
	}
	public void setDefaultValuesGenerator(boolean defaultValuesGenerator) {
		this.defaultValuesGenerator = defaultValuesGenerator;
	}
}
