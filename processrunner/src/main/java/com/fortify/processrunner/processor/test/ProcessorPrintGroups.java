package com.fortify.processrunner.processor.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions.IContextGrouping;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

/**
 * This {@link IProcessor} implementation will print group
 * information during the main processing phase. This can 
 * be used for debugging purposes.
 * 
 * TODO Extend from {@link ProcessorPrintMessage}
 */
public class ProcessorPrintGroups extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(ProcessorPrintGroups.class);
	@Override
	protected boolean process(Context context) {
		IContextGrouping contextGrouping = context.as(IContextGrouping.class);
		LOG.info("Processing group: "+contextGrouping.getCurrentGroup());
		return true;
	}
}
