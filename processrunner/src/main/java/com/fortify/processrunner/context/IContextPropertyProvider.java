package com.fortify.processrunner.context;

import java.util.List;

public interface IContextPropertyProvider {
	/**
	 * Get the {@link List} of {@link ContextProperty} instances
	 * that describe the context properties supported/required
	 * by the current {@link IContextPropertyProvider} implementation. 
	 * @param context
	 * @return
	 */
	public abstract List<ContextProperty> getContextProperties(Context context);
}
