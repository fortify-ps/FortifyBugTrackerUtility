package com.fortify.processrunner.context;

import java.util.Collection;

public interface IContextPropertyDefinitionProvider {
	/**
	 * Add {@link ContextPropertyDefinition} instances to the provided
	 * {@link ContextPropertyDefinition} {@link Collection} that describe 
	 * the context properties supported/required by the current 
	 * {@link IContextPropertyDefinitionProvider} implementation. 
	 */
	public abstract void addContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context);
}
