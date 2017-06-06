package com.fortify.processrunner.context;

import java.util.LinkedHashMap;

/**
 * This class holds all configured {@link ContextPropertyDefinition} instances
 * indexed by {@link ContextPropertyDefinition} name.
 * 
 * @author Ruud Senden
 *
 */
public class ContextPropertyDefinitions extends LinkedHashMap<String, ContextPropertyDefinition> {
	private static final long serialVersionUID = 1L;

	public ContextPropertyDefinition add(ContextPropertyDefinition contextPropertyDefinition) {
		put(contextPropertyDefinition.getName(), contextPropertyDefinition);
		return contextPropertyDefinition;
	}

}
