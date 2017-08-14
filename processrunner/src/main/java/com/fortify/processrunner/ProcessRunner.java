/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.IProcessor.Phase;

/**
 * This class allows for running one or more independent {@link IProcessor}
 * implementations as configured through the {@link #setProcessors(IProcessor...)}
 * method.
 * 
 * @author Ruud Senden
 *
 */
public class ProcessRunner implements IContextPropertyDefinitionProvider {
	private static final Log LOG = LogFactory.getLog(ProcessRunner.class);
	private IProcessor[] processors = new IProcessor[]{};
	private String description;
	private boolean enabled = true;
	private boolean _default= false;

	/**
	 * Allow all configured {@link IProcessor} implementations to add
	 * their {@link ContextPropertyDefinitions}
	 */
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		for ( IProcessor processor : processors ) {
			processor.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		}
	}
	
	/**
	 * Run the configured processor(s) using the configured context.
	 * Each configured processor is run with its own individual context.
	 */
	public void run(Context context) {
		IProcessor[] processors = getProcessors();
		for ( IProcessor processor : processors ) {
			// Create a copy to let each processor have its own independent context
			Context processorContext = new Context(context);
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("[Process] Running processor "+processor+" with context "+context);
			}
			if ( process(Phase.PRE_PROCESS, processorContext, processor) ) {
				if ( process(Phase.PROCESS, processorContext, processor) ) {
					process(Phase.POST_PROCESS, processorContext, processor);
				}
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
	 * Get the {@link IProcessor} instances that will be run for this {@link ProcessRunner}
	 * @return
	 */
	public IProcessor[] getProcessors() {
		return processors;
	}

	/**
	 * Set the {@link IProcessor} instances that will be run for this {@link ProcessRunner}
	 * @param processors
	 */
	public void setProcessors(IProcessor... processors) {
		this.processors = processors;
	}
	
	/**
	 * Get the value of the 'enabled' flag which indicates whether this {@link ProcessRunner} is enabled
	 * @return true if this {@link ProcessRunner} is enabled, false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set the value of the 'enabled' flag which indicates whether this {@link ProcessRunner} is enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Get the value of the 'default' flag which indicates whether this {@link ProcessRunner} is the default {@link ProcessRunner}
	 * @return true if this is the default {@link ProcessRunner}, false otherwise
	 */
	public boolean isDefault() {
		return _default;
	}

	/**
	 * Set the value of the 'default' flag which indicates whether this {@link ProcessRunner} is the default {@link ProcessRunner}
	 */
	public void setDefault(boolean _default) {
		this._default = _default;
	}

	/**
	 * Get the description for this {@link ProcessRunner}, describing its functionality/behavior
	 * @return description for this {@link ProcessRunner}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description for this {@link ProcessRunner}, describing its functionality/behavior
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
