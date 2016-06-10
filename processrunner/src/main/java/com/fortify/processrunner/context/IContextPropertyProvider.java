package com.fortify.processrunner.context;

import java.util.Collection;

public interface IContextPropertyProvider {
	/**
	 * Add {@link ContextProperty} instances to the provided
	 * {@link ContextProperty} {@link Collection} that describe 
	 * the context properties supported/required by the current 
	 * {@link IContextPropertyProvider} implementation. 
	 */
	public abstract void addContextProperties(Collection<ContextProperty> contextProperties, Context context);
}
