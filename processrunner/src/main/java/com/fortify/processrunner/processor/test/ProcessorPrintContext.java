package com.fortify.processrunner.processor.test;

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
	@Override
	protected boolean preProcess(Context context) {
		System.out.println("Pre-processing context: "+context);
		return true;
	}
	
	@Override
	protected boolean process(Context context) {
		System.out.println("Processing context: "+context);
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		System.out.println("Post-processing context: "+context);
		return true;
	}
}
