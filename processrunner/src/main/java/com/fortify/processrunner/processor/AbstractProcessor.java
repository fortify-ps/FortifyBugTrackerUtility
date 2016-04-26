package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;

/**
 * <p>This abstract {@link IProcessor} implementation allows subclasses
 * to implement only specific processing phases by overriding the 
 * {@link #preProcess(Context)}, {@link #process(Context)} and/or
 * {@link #postProcess(Context)} methods respectively. A default
 * implementation for each of these methods is provided that does
 * nothing.</p>
 *
 * <p>Advantages of extending this class instead of implementing the
 * {@link IProcessor} interface directly:</p>
 * <ul>
 *  <li>No need to manually determine the current processing phase</li>
 *  <li>Easy to provide functionality only for relevant processing phases</li>
 *  <li>Provides detailed logging</li>
 *  <li>Provides diagnostic {@link #toString()} method</li>
 * </ul>
 */
public abstract class AbstractProcessor implements IProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessor.class);
	private static final List<ContextProperty> EMPTY_CP_LIST = new ArrayList<ContextProperty>();
	
	public List<ContextProperty> getContextProperties(Context context) {
		return EMPTY_CP_LIST;
	}
	
	public final boolean process(Phase phase, Context context) {
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("[ENTER] Phase: "+phase+"\nProcessor: "+this+"\nContext: "+context);
		}
		boolean result = _process(phase, context);
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("[EXIT] Phase: "+phase+"\nProcessor: "+this+"\nContext: "+context+"\nResult: "+result);
		}
		return result;
	}

	private boolean _process(Phase phase, Context context) {
		switch ( phase ) {
			case PRE_PROCESS: return preProcess(context);
			case PROCESS: return process(context);
			case POST_PROCESS: return postProcess(context);
		}
		throw new RuntimeException("Unsupported phase: "+phase);
	}

	protected boolean preProcess(Context context) {
		return true;
	}
	
	protected boolean process(Context context) {
		return true;
	}
	
	protected boolean postProcess(Context context) {
		return true;
	}
	
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
}
