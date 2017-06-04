package com.fortify.processrunner.context;

import java.util.LinkedHashMap;

public class ContextPropertyDefinitions extends LinkedHashMap<String, ContextPropertyDefinition> {
	private static final long serialVersionUID = 1L;

	public ContextPropertyDefinition add(ContextPropertyDefinition contextPropertyDefinition) {
		put(contextPropertyDefinition.getName(), contextPropertyDefinition);
		return contextPropertyDefinition;
	}

}
