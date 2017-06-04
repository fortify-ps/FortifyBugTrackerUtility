package com.fortify.processrunner.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation allows for writing data from the 
 * current {@link Context} instance to a file based on a list of configured 
 * {@link TemplateExpression} instances.
 */
public class ProcessorFileSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	private static final Log LOG = LogFactory.getLog(ProcessorFileSubmitIssueForVulnerabilities.class);
	private static final String DEFAULT_OUTPUT_FILE = "FoDVulnerabilities.csv";
	
	private String fieldSeparator = ",";
	private PrintWriter writer;
	private File file;
	private boolean writeHeader = true;
	
	
	/**
	 * Define the OutputFile context property.
	 */
	@Override
	public void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition("OutputFile", "File to write the issues to", context, DEFAULT_OUTPUT_FILE, false));
	}
	
	@Override
	public String getBugTrackerName() {
		return "File";
	}
	
	/**
	 * Get the output file name from the current {@link Context},
	 * open this file for writing, and add configured headers to this 
	 * file if the file has been newly created. 
	 */
	@Override
	protected boolean preProcessBeforeGrouping(Context context) {
		String fileName = (String)context.get("OutputFile");
		if ( StringUtils.isBlank(fileName) ) {
			fileName = DEFAULT_OUTPUT_FILE;
		}
		LOG.info(String.format("[%s] Writing vulnerabilities to file %s", getBugTrackerName(), fileName));
		try {
			file = new File(fileName);
			writer = new PrintWriter(new FileWriter(file, true));
			LOG.debug(String.format("[%s] Opened file %s", getBugTrackerName(), fileName));
		} catch ( IOException e ) {
			throw new RuntimeException("Error opening file appender", e);
		}
		return true;
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> fields) {
		Collection<String> keys = fields.keySet();
		writeHeader(keys);
		
		StringBuffer currentLine = new StringBuffer();
		for ( String key: keys ) {
			if ( currentLine.length()>0 ) { currentLine.append(getFieldSeparator()); }
			currentLine.append(fields.get(key));
		}
		
		writer.println(currentLine);
		writer.flush();
		
		return new SubmittedIssue(null, file.toURI().toASCIIString());
	}
	
	@Override
	protected boolean postProcessAfterProcessingGroups(Context context) {
		writer.close();
		LOG.debug(String.format("[%s] Closed file %s", getBugTrackerName(), context.get("OutputFile")));
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
