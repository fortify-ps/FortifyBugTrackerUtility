package com.fortify.processrunner.context;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This class describes a single context property that is
 * supported by a given {@link IProcessor} implementation.
 * It contains context property name, description, and
 * whether the property is required to be available in
 * the {@link Context}.
 */
public class ContextPropertyDefinition {
	private final String name;
	private final String description;
	private final String defaultValue;
	private final boolean required;
	
	/**
	 * Constructor for setting the context property name, description and required flag.
	 * @param name
	 * @param description
	 * @param required
	 */
	public ContextPropertyDefinition(String name, String description, Context defaultContext, String defaultValue, boolean required) {
		super();
		this.name = name;
		this.description = description;
		this.defaultValue = (String)defaultContext.getOrDefault(name, defaultValue);
		this.required = required;
	}

	/**
	 * Get the context property name.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the context property description.
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get the context property default value (hardcoded or from current context).
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Get the context property required flag.
	 * @return
	 */
	public boolean isRequired() {
		return required;
	}

	@Override
	public String toString() {
		return "ContextPropertyDefinition [name=" + name + ", description=" + description + ", defaultValue="
				+ defaultValue + ", required=" + required + "]";
	}
	
	
}
