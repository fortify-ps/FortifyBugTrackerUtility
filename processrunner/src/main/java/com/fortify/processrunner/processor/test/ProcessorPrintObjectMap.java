package com.fortify.processrunner.processor.test;

import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMap.IContextObjectMap;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;

/**
 * This {@link IProcessor} implementation will print string
 * map information during the main processing phase. This can 
 * be used for debugging purposes.
 * 
 * TODO Extend from {@link ProcessorPrintMessage}
 */
public class ProcessorPrintObjectMap extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(ProcessorPrintObjectMap.class);
	@Override
	protected boolean process(Context context) {
		Map<String,Object> map = context.as(IContextObjectMap.class).getObjectMap();
		for ( Map.Entry<String, Object> entry : map.entrySet() ) {
			LOG.info(entry.getKey()+": "+WordUtils.wrap(""+entry.getValue(), 70, "\n\t", false));
		}
		return true;
	}
}
