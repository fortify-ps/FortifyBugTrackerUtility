package com.fortify.processrunner.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation can print arbitrary messages
 * to standard out during the various processing phases. For each
 * processing phase, a corresponding (optional) message template can 
 * be configured.
 */
// TODO (Low) Add rootExpression configuration option, to allow easy 
//      access from the message templates to a specific root object.
public class ProcessorPrintMessage extends AbstractProcessor {
	protected static final Log LOG = LogFactory.getLog(ProcessorPrintMessage.class);
	private TemplateExpression messageTemplatePreProcess;
	private TemplateExpression messageTemplateProcess;
	private TemplateExpression messageTemplatePostProcess;

	public ProcessorPrintMessage() {}
	
	public ProcessorPrintMessage(String messageTemplatePreProcess, String messageTemplateProcess, String messageTemplatePostProcess) {
		this.messageTemplatePreProcess = messageTemplatePreProcess==null?null:SpringExpressionUtil.parseTemplateExpression(messageTemplatePreProcess);
		this.messageTemplateProcess = messageTemplateProcess==null?null:SpringExpressionUtil.parseTemplateExpression(messageTemplateProcess);
		this.messageTemplatePostProcess = messageTemplatePostProcess==null?null:SpringExpressionUtil.parseTemplateExpression(messageTemplatePostProcess);
	}
	
	@Override
	protected boolean preProcess(Context context) {
		return process(context, getMessageTemplatePreProcess());
	}
	
	@Override
	protected boolean process(Context context) {
		return process(context, getMessageTemplateProcess());
	}
	
	@Override
	protected boolean postProcess(Context context) {
		return process(context, getMessageTemplatePostProcess());
	}
	
	protected boolean process(Context context, TemplateExpression template) {
		if ( template != null ) {
			printAndLog(SpringExpressionUtil.evaluateExpression(context, template, String.class));
		}
		return true;
	}
	
	protected void printAndLog(String message) {
		LOG.info(message);
	}

	public TemplateExpression getMessageTemplatePreProcess() {
		return messageTemplatePreProcess;
	}

	public void setMessageTemplatePreProcess(TemplateExpression messageTemplatePreProcess) {
		this.messageTemplatePreProcess = messageTemplatePreProcess;
	}

	public TemplateExpression getMessageTemplateProcess() {
		return messageTemplateProcess;
	}

	public void setMessageTemplateProcess(TemplateExpression messageTemplateProcess) {
		this.messageTemplateProcess = messageTemplateProcess;
	}

	public TemplateExpression getMessageTemplatePostProcess() {
		return messageTemplatePostProcess;
	}

	public void setMessageTemplatePostProcess(TemplateExpression messageTemplatePostProcess) {
		this.messageTemplatePostProcess = messageTemplatePostProcess;
	}
	
	
	
	
}
