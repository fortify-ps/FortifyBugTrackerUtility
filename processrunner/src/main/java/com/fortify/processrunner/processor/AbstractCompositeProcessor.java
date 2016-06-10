package com.fortify.processrunner.processor;

import java.util.Collection;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextPropertyProvider;

/**
 * This abstract {@link IProcessor} implementation provides
 * support for invoking a list of individual {@link IProcessor}
 * instances. Concrete implementations of this class must 
 * implement the {@link #getProcessors()} method to return the 
 * list of individual {@link IProcessor} instances to be invoked
 * during processing.
 */
public abstract class AbstractCompositeProcessor extends AbstractProcessor {
	
	/**
	 * Add the {@link ContextProperty} instances by calling the 
	 * {@link IContextPropertyProvider#addContextProperties(Collection, Context)} 
	 * method on each individual {@link IProcessor} instance returned by the 
	 * {@link #getProcessors()} method. 
	 */
	@Override
	public final void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		IProcessor[] processors = getProcessors();
		if ( processors != null ) {
			for ( IProcessor processor : processors ) {
				if ( processor != null ) {
					processor.addContextProperties(contextProperties, context);
				}
			}
		}
		addCompositeContextProperties(contextProperties, context);
	}
	
	/**
	 * Concrete implementations can override this method to add additional
	 * {@link ContextProperty} instances.
	 * @param contextProperties
	 * @param context
	 */
	protected void addCompositeContextProperties(Collection<ContextProperty> contextProperties, Context context) {}

	/**
	 * Run the {@link Phase#PRE_PROCESS} phase on all
	 * {@link IProcessor} instances returned by {@link #getProcessors()}.
	 */
	@Override
	protected boolean preProcess(Context context) {
		return processForAll(Phase.PRE_PROCESS, context);
	}
	
	/**
	 * Run the {@link Phase#PROCESS} phase on all
	 * {@link IProcessor} instances returned by {@link #getProcessors()}.
	 */
	@Override
	protected boolean process(Context context) {
		return processForAll(Phase.PROCESS, context);
	}
	
	/**
	 * Run the {@link Phase#POST_PROCESS} phase on all
	 * {@link IProcessor} instances returned by {@link #getProcessors()}.
	 */
	@Override
	protected boolean postProcess(Context context) {
		return processForAll(Phase.POST_PROCESS, context);
	}
	
	/**
	 * Invoke the {@link IProcessor#process(Phase, Context)} method on 
	 * each of the {@link IProcessor} instances returned by the 
	 * {@link #getProcessors()} method for the given phase. If any of 
	 * the {@link IProcessor#process(Phase, Context)} invocations
	 * returns false, processing will stop and this method will return
	 * false. Otherwise, all {@link IProcessor} instances will be 
	 * invoked and this method will return true.
	 * @param phase
	 * @param context
	 * @return
	 */
	protected boolean processForAll(Phase phase, Context context) {
		for ( IProcessor processor : getProcessors() ) {
			if ( !processor.process(phase, context) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Concrete subclasses must implement this method to return
	 * the list of individual {@link IProcessor} implementations
	 * that make up this composite processor. 
	 * @return
	 */
	public abstract IProcessor[] getProcessors();

}