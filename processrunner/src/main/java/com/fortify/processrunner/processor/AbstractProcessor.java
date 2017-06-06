package com.fortify.processrunner.processor;

import java.util.Collection;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;

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
 * 
 * @author Ruud Senden
 */
public abstract class AbstractProcessor implements IProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessor.class);
	
	/**
	 * Add {@link ContextPropertyDefinition} instances to the provided
	 * {@link ContextPropertyDefinition} {@link Collection} that describe 
	 * the context properties supported/required by the current 
	 * {@link IContextPropertyDefinitionProvider} implementation. By 
	 * default, this method does not add any context properties.
	 */
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}
	
	/**
	 * Process the given {@link Phase} with the given {@link Context}.
	 * This will log the current {@link Phase}, {@link IProcessor} and
	 * {@link Context} at trace level on entry and exit, and depending
	 * on the given {@link Phase} invoke either the 
	 * {@link #preProcess(Context)}, {@link #process(Context)}
	 * or {@link #postProcess(Context)} method (via the private
	 * {@link #_process(Phase, Context)} method).
	 */
	public final boolean process(Phase phase, Context context) {
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("[Process][ENTER] Phase: "+phase+"\nProcessor: "+this+"\nContext: "+context);
		}
		boolean result = _process(phase, context);
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("[Process][EXIT] Phase: "+phase+"\nProcessor: "+this+"\nContext: "+context+"\nResult: "+result);
		}
		return result;
	}

	/**
	 * Depending on the given {@link Phase}, this method will
	 * invoke either the {@link #preProcess(Context)}, 
	 * {@link #process(Context)} or {@link #postProcess(Context)} 
	 * method.
	 * @param phase
	 * @param context
	 * @return
	 */
	private boolean _process(Phase phase, Context context) {
		switch ( phase ) {
			case PRE_PROCESS: return preProcess(context);
			case PROCESS: return process(context);
			case POST_PROCESS: return postProcess(context);
		}
		throw new RuntimeException("Unsupported phase: "+phase);
	}

	/**
	 * Pre-process the given {@link Context}. By default this
	 * method does nothing and simply returns true.
	 * @param context
	 * @return
	 */
	protected boolean preProcess(Context context) {
		return true;
	}
	
	/**
	 * Process the given {@link Context}. By default this
	 * method does nothing and simply returns true.
	 * @param context
	 * @return
	 */
	protected boolean process(Context context) {
		return true;
	}
	
	/**
	 * Post-process the given {@link Context}. By default this
	 * method does nothing and simply returns true.
	 * @param context
	 * @return
	 */
	protected boolean postProcess(Context context) {
		return true;
	}
	
	/**
	 * This {@link #toString()} implementation uses
	 * {@link ReflectionToStringBuilder} to generate a string
	 * representation of this {@link IProcessor} implementation
	 * showing all instance field values for diagnostic
	 * information.
	 */
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
}
