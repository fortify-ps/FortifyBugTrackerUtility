package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.List;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;

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
	 * Get the list of supported context properties by combining the 
	 * context properties returned by the individual {@link IProcessor}
	 * instances returned by the {@link #getProcessors()} method. 
	 */
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		for ( IProcessor processor : getProcessors() ) {
			result.addAll( processor.getContextProperties(context) );
		}
		return result;
	}
	
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