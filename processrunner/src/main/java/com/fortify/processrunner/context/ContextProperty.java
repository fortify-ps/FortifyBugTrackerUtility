package com.fortify.processrunner.context;

public class ContextProperty {
	private final String name;
	private final String description;
	private final boolean required;
	
	public ContextProperty(String name, String description, boolean required) {
		super();
		this.name = name;
		this.description = description;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return required;
	}
}
