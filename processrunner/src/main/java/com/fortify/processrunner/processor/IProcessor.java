package com.fortify.processrunner.processor;

import java.util.List;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;

/**
 * <p>This interface defines processors that can perform arbitrary
 * tasks when the {@link #process(Phase, Context)} method is 
 * invoked.</p>
 * 
 * <p>The {@link #process(Phase, Context)} method can be 
 * invoked multiple times, once for each processing phase.
 * Available phases are pre-processing phase, processing phase,
 * and post-processing phase, as defined by the {@link Phase}
 * enumeration.</p>
 * 
 * <p>Usually classes should not implement this interface directly,
 * but instead extend from the {@link AbstractProcessor} class.</p>
 */
public interface IProcessor {
	/**
	 * Enumeration describing the available processing phases.
	 */
	public static enum Phase { PRE_PROCESS, PROCESS, POST_PROCESS }
	
	/**
	 * Process the given {@link Phase} with the given {@link Context}.
	 * @param phase
	 * @param context
	 * @return
	 */
	public boolean process(Phase phase, Context context);
	
	/**
	 * Get the {@link List} of {@link ContextProperty} instances
	 * that describe the context properties supported/required
	 * by the current {@link IProcessor} implementation. 
	 * @param context
	 * @return
	 */
	public List<ContextProperty> getContextProperties(Context context);
}
