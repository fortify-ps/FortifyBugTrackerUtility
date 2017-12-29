/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
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
