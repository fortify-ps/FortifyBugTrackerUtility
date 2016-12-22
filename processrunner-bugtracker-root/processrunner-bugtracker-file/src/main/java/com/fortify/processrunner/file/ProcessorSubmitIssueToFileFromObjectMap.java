package com.fortify.processrunner.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextSubmittedIssueData;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMap.IContextObjectMap;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation allows for writing data from the 
 * current {@link Context} instance to a file based on a list of configured 
 * {@link TemplateExpression} instances.
 */
public class ProcessorSubmitIssueToFileFromObjectMap extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(ProcessorSubmitIssueToFileFromObjectMap.class);
	private static final String DEFAULT_OUTPUT_FILE = "FoDVulnerabilities.csv";
	
	private String fieldSeparator = ",";
	private PrintWriter writer;
	private File file;
	private boolean writeHeader = true;
	
	
	/**
	 * Define the OutputFile context property.
	 */
	@Override
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty("OutputFile", "File to write the issues to", context, DEFAULT_OUTPUT_FILE, false));
	}
	
	/**
	 * Get the output file name from the current {@link Context},
	 * open this file for writing, and add configured headers to this 
	 * file if the file has been newly created. 
	 */
	@Override
	protected boolean preProcess(Context context) {
		String fileName = (String)context.get("OutputFile");
		if ( StringUtils.isBlank(fileName) ) {
			fileName = DEFAULT_OUTPUT_FILE;
		}
		LOG.info("Writing vulnerabilities to file "+fileName);
		try {
			file = new File(fileName);
			writer = new PrintWriter(new FileWriter(file, true));
			LOG.debug("Opened file "+fileName);
		} catch ( IOException e ) {
			throw new RuntimeException("Error opening file appender", e);
		}
		return true;
	}

	@Override
	protected boolean process(Context context) {
		Map<String, Object> fields = context.as(IContextObjectMap.class).getObjectMap();
		Collection<String> keys = fields.keySet();
		writeHeader(keys);
		
		StringBuffer currentLine = new StringBuffer();
		for ( String key: keys ) {
			if ( currentLine.length()>0 ) { currentLine.append(getFieldSeparator()); }
			currentLine.append(fields.get(key));
		}
		
		LOG.info("Writing issue to "+(String)context.get("OutputFile")+": "+currentLine);
		writer.println(currentLine);
		writer.flush();
		
		IContextSubmittedIssueData ctx = context.as(IContextSubmittedIssueData.class);
		ctx.setSubmittedIssueBugTrackerName("file");
		ctx.setSubmittedIssueLocation(file.toURI().toASCIIString());
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		writer.close();
		LOG.debug("Close file "+context.get("OutputFile"));
		return true;
	}
	
	/**
	 * Append the headers to the current output file
	 * @param context
	 */
	protected void writeHeader(Collection<String> headers) {
		if ( writeHeader && (!file.exists() || file.length()==0) && headers!=null) {
			writer.println(StringUtils.join(headers, getFieldSeparator()));
			writer.flush();
		}
		writeHeader = false;
	}

	public String getFieldSeparator() {
		return fieldSeparator;
	}

	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}
}
