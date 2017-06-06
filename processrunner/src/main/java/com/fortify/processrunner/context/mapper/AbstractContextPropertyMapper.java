package com.fortify.processrunner.context.mapper;

/**
 * This abstract implementation of {@link IContextPropertyMapper}
 * allows for configuring the main context property name, and whether
 * this implementation should be used as a default values generator.
 * 
 * @author Ruud Senden
 *
 */
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
