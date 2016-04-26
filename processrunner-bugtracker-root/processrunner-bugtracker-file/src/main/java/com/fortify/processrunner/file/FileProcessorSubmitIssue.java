package com.fortify.processrunner.file;

import java.io.BufferedWriter;
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
import com.fortify.util.spring.SpringExpressionUtil;

public class FileProcessorSubmitIssue extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FileProcessorSubmitIssue.class);
	
	private String fieldSeparator = ",";
	private String rootExpression;
	private List<String> fieldHeaders;
	private List<String> fieldExpressionTemplates;
	private PrintWriter pw;
	
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>(2);
		result.add(new ContextProperty("OutputFile", "File to write the issues to", !context.containsKey("OutputFile")));
		return result;
	}
	
	@Override
	protected boolean preProcess(Context context) {
		System.out.println("Writing issues to file "+context.get("OutputFile"));
		try {
			File f = new File((String)context.get("OutputFile"));
			pw = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
			appendHeaders(f, pw);
		} catch ( IOException e ) {
			throw new RuntimeException("Error opening file appender", e);
		}
		return true;
	}
	
	protected void appendHeaders(File f, PrintWriter pw) {
		List<String> headers = getFieldHeaders();
		if ( (!f.exists() || f.length()==0) && headers!=null) {
			pw.println(StringUtils.join(headers, getFieldSeparator()));
		}
	}

	@Override
	protected boolean process(Context context) {
		Object root = getRootExpression()==null?context:SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
		List<String> values = new ArrayList<String>();
		for ( String fieldExpressionTemplate : getFieldExpressionTemplates() ) {
			String value = SpringExpressionUtil.evaluateTemplateExpression(root, fieldExpressionTemplate, String.class);
			values.add(value);
		}
		
		String line = StringUtils.join(values, getFieldSeparator());
		LOG.trace("Writing issue to "+(String)context.get("OutputFile")+": "+line);
		pw.println(line);
		
		IContextSubmittedIssueData ctx = context.as(IContextSubmittedIssueData.class);
		ctx.setSubmittedIssueBugTrackerName((String)context.get("OutputFile"));
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		pw.close();
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

	public List<String> getFieldExpressionTemplates() {
		return fieldExpressionTemplates;
	}

	public void setFieldExpressionTemplates(List<String> fieldExpressionTemplates) {
		this.fieldExpressionTemplates = fieldExpressionTemplates;
	}

	public String getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(String rootExpression) {
		this.rootExpression = rootExpression;
	}
	
	

	
}
