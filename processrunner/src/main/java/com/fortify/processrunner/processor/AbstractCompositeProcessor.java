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
	
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		for ( IProcessor processor : getProcessors() ) {
			result.addAll( processor.getContextProperties(context) );
		}
		return result;
	}
	
	@Override
	protected boolean preProcess(Context context) {
		return processForAll(Phase.PRE_PROCESS, context);
	}
	
	@Override
	protected boolean process(Context context) {
		return processForAll(Phase.PROCESS, context);
	}
	
	@Override
	protected boolean postProcess(Context context) {
		return processForAll(Phase.POST_PROCESS, context);
	}
	
	public boolean processForAll(Phase phase, Context context) {
		for ( IProcessor processor : getProcessors() ) {
			if ( !processor.process(phase, context) ) {
				return false;
			}
		}
		return true;
	}

	public abstract IProcessor[] getProcessors();

}