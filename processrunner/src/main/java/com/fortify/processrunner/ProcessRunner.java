package com.fortify.processrunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.IProcessor.Phase;

/**
 * This class allows configuration of the initial context and the processor to
 * be run on that context. 
 */
public class ProcessRunner implements Runnable {
	private static final Log LOG = LogFactory.getLog(ProcessRunner.class);
	private Context context = new Context();
	private IProcessor processor = new CompositeProcessor();
	private String description;
	
	/**
	 * Run the configured processor using the configured context.
	 */
	public void run() {
		Context context = getContext();
		IProcessor processor = getProcessor();
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Running processor "+processor+" with context "+context);
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
		LOG.debug("Running phase "+phase);
		boolean result = processor.process(phase, context);
		LOG.debug("Phase "+phase+" result: "+result);
		return result;
	}
	
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}

	public IProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(IProcessor processor) {
		this.processor = processor;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
