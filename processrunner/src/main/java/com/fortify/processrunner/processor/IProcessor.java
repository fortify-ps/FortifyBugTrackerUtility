package com.fortify.processrunner.processor;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;

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
 * 
 * @author Ruud Senden
 */
public interface IProcessor extends IContextPropertyDefinitionProvider {
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
}
