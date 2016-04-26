package com.fortify.processrunner.processor.test;

import java.util.Map;

import org.apache.commons.lang.WordUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorPrintMessage;
import com.fortify.processrunner.processor.ProcessorBuildStringMap.IContextStringMap;

/**
 * This {@link IProcessor} implementation will print string
 * map information during the main processing phase. This can 
 * be used for debugging purposes.
 * 
 * TODO Extend from {@link ProcessorPrintMessage}
 */
public class ProcessorPrintStringMap extends AbstractProcessor {
	@Override
	protected boolean process(Context context) {
		Map<String,String> map = context.as(IContextStringMap.class).getStringMap();
		for ( Map.Entry<String, String> entry : map.entrySet() ) {
			System.out.println(entry.getKey()+": "+WordUtils.wrap(entry.getValue(), 70, "\n\t", false));
		}
		return true;
	}
}
