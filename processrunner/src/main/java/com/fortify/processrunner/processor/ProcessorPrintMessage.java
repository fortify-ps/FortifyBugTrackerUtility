package com.fortify.processrunner.processor;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation can print arbitrary messages
 * to standard out during the various processing phases. For each
 * processing phase, a corresponding (optional) message template can 
 * be configured.
 */
// TODO Make output stream configurable (default to System.out)
// TODO Add rootExpression configuration option, to allow easy 
//      access from the message templates to a specific root object.
public class ProcessorPrintMessage extends AbstractProcessor {
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
		TemplateExpression template = getMessageTemplatePreProcess();
		if ( template != null ) {
			System.out.print(SpringExpressionUtil.evaluateExpression(context, template, String.class));
		}
		return true;
	}
	
	@Override
	protected boolean process(Context context) {
		TemplateExpression template = getMessageTemplateProcess();
		if ( template != null ) {
			System.out.print(SpringExpressionUtil.evaluateExpression(context, template, String.class));
		}
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		TemplateExpression template = getMessageTemplatePostProcess();
		if ( template != null ) {
			System.out.print(SpringExpressionUtil.evaluateExpression(context, template, String.class));
		}
		return true;
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
