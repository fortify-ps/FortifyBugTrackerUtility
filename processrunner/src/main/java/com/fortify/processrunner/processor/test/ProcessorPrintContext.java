package com.fortify.processrunner.processor.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

/**
 * This {@link IProcessor} implementation will print context
 * information during each processing phase. This can be used
 * for debugging purposes.
 * 
 * TODO Extend from {@link ProcessorPrintMessage}
 */
public class ProcessorPrintContext extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(ProcessorPrintContext.class);
	@Override
	protected boolean preProcess(Context context) {
		LOG.info("Pre-processing context: "+context);
		return true;
	}
	
	@Override
	protected boolean process(Context context) {
		LOG.info("Processing context: "+context);
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		LOG.info("Post-processing context: "+context);
		return true;
	}
}
