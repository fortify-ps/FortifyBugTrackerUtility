package com.fortify.processrunner.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextSubmittedIssueData;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation allows for writing data from the 
 * current {@link Context} instance to a file based on a list of configured 
 * {@link TemplateExpression} instances.
 */
public class FileProcessorSubmitIssue extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FileProcessorSubmitIssue.class);
	private static final String DEFAULT_OUTPUT_FILE = "FoDVulnerabilities.csv";
	
	private String fieldSeparator = ",";
	private SimpleExpression rootExpression;
	private List<String> fieldHeaders;
	private List<TemplateExpression> fieldTemplateExpressions;
	private PrintWriter writer;
	
	/**
	 * Define the OutputFile context property.
	 */
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>(2);
		result.add(new ContextProperty("OutputFile", "File to write the issues to", context, DEFAULT_OUTPUT_FILE, false));
		return result;
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
			File f = new File(fileName);
			writer = new PrintWriter(new FileWriter(f, true));
			LOG.debug("Opened file "+fileName);
			appendHeaders(f);
		} catch ( IOException e ) {
			throw new RuntimeException("Error opening file appender", e);
		}
		return true;
	}
	
	/**
	 * Append the configured headers to the current output file
	 * during the pre-processing phase..
	 * @param f
	 * @param pw
	 */
	protected void appendHeaders(File f) {
		List<String> headers = getFieldHeaders();
		if ( (!f.exists() || f.length()==0) && headers!=null) {
			writer.println(StringUtils.join(headers, getFieldSeparator()));
			writer.flush();
		}
	}

	/** 
	 * <p>Process the current context by iterating over the configured 
	 * {@link TemplateExpression} instances, evaluating each configured
	 * {@link TemplateExpression} on the current root object, and writing
	 * the result to the configured file.</p>
	 * 
	 * <p>If root expression has been configured, it will be evaluated on
	 * the current {@link Context} instance and then used as the root object.
	 * If no root expression has been configured, the current {@link Context}
	 * instance will be used as the root object.</p> 
	 */
	@Override
	protected boolean process(Context context) {
		Object root = getRootExpression()==null?context:SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
		List<String> values = new ArrayList<String>();
		for ( TemplateExpression fieldExpressionTemplate : getFieldTemplateExpressions() ) {
			String value = SpringExpressionUtil.evaluateExpression(root, fieldExpressionTemplate, String.class);
			values.add(value);
		}
		
		String line = StringUtils.join(values, getFieldSeparator());
		LOG.info("Writing issue to "+(String)context.get("OutputFile")+": "+line);
		writer.println(line);
		writer.flush();
		
		IContextSubmittedIssueData ctx = context.as(IContextSubmittedIssueData.class);
		ctx.setSubmittedIssueBugTrackerName("File");
		ctx.setSubmittedIssueLocation((String)context.get("OutputFile"));
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		writer.close();
		LOG.debug("Close file "+context.get("OutputFile"));
		return true;
	}

	public String getFieldSeparator() {
		return fieldSeparator;
	}

	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}

	public List<String> getFieldHeaders() {
		return fieldHeaders;
	}

	public void setFieldHeaders(List<String> fieldHeaders) {
		this.fieldHeaders = fieldHeaders;
	}

	public List<TemplateExpression> getFieldTemplateExpressions() {
		return fieldTemplateExpressions;
	}

	public void setFieldTemplateExpressions(List<TemplateExpression> fieldTemplateExpressions) {
		this.fieldTemplateExpressions = fieldTemplateExpressions;
	}

	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}
}
