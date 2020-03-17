/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.processrunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.cli.ICLIOptionDefinitionProvider;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.IProcessor.Phase;

/**
 * This class allows for running one or more independent {@link IProcessor}
 * implementations as returned by the {@link #getProcessor(Context)} method.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractProcessRunner implements ICLIOptionDefinitionProvider {
	private static final Log LOG = LogFactory.getLog(AbstractProcessRunner.class);

	/**
	 * Run the configured processor(s) using the configured context.
	 * Each configured processor is run with its own individual context.
	 */
	public void run(Context context) {
		IProcessor processor = getProcessor(context);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("[Process] Running processor "+processor+" with context "+context);
		}
		if ( process(Phase.PRE_PROCESS, context, processor) ) {
			if ( process(Phase.PROCESS, context, processor) ) {
				process(Phase.POST_PROCESS, context, processor);
			}
		}
	}

	/**
	 * Run the given phase with the given context on the given processor.
	 * @param phase
	 * @param context
	 * @param processor
	 * @return
	 */
	private static final boolean process(Phase phase, Context context, IProcessor processor) {
		LOG.debug("[Process] Running phase "+phase);
		boolean result = processor.process(phase, context);
		LOG.debug("[Process] Phase "+phase+" result: "+result);
		return result;
	}

	/**
	 * Get the {@link IProcessor} instance that will be run for this {@link AbstractProcessRunner}
	 * @return
	 */
	public abstract IProcessor getProcessor(Context context);
}
