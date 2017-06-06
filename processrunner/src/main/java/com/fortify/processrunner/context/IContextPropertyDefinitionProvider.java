package com.fortify.processrunner.context;

import java.util.Collection;

/**
 * Interface to be implemented by classes that want to add {@link ContextPropertyDefinition}
 * instances to {@link ContextPropertyDefinitions}.
 * 
 * @author Ruud Senden
 *
 */
public interface IContextPropertyDefinitionProvider {
	/**
	 * Add {@link ContextPropertyDefinition} instances to the provided
	 * {@link ContextPropertyDefinition} {@link Collection} that describe 
	 * the context properties supported/required by the current 
	 * {@link IContextPropertyDefinitionProvider} implementation. 
	 */
	public abstract void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context);
}
