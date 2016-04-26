package com.fortify.processrunner.processor;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;

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
	private String messageTemplatePreProcess;
	private String messageTemplateProcess;
	private String messageTemplatePostProcess;

	public ProcessorPrintMessage() {}
	
	public ProcessorPrintMessage(String messageTemplatePreProcess, String messageTemplateProcess, String messageTemplatePostProcess) {
		this.messageTemplatePreProcess = messageTemplatePreProcess;
		this.messageTemplateProcess = messageTemplateProcess;
		this.messageTemplatePostProcess = messageTemplatePostProcess;
	}
	
	@Override
	protected boolean preProcess(Context context) {
		String template = getMessageTemplatePreProcess();
		if ( template != null ) {
			System.out.print(SpringExpressionUtil.evaluateTemplateExpression(context, template, String.class));
		}
		return true;
	}
	
	@Override
	protected boolean process(Context context) {
		String template = getMessageTemplateProcess();
		if ( template != null ) {
			System.out.print(SpringExpressionUtil.evaluateTemplateExpression(context, template, String.class));
		}
		return true;
	}
	
	@Override
	protected boolean postProcess(Context context) {
		String template = getMessageTemplatePostProcess();
		if ( template != null ) {
			System.out.print(SpringExpressionUtil.evaluateTemplateExpression(context, template, String.class));
		}
		return true;
	}

	public String getMessageTemplatePreProcess() {
		return messageTemplatePreProcess;
	}

	public void setMessageTemplatePreProcess(String messageTemplatePreProcess) {
		this.messageTemplatePreProcess = messageTemplatePreProcess;
	}

	public String getMessageTemplateProcess() {
		return messageTemplateProcess;
	}

	public void setMessageTemplateProcess(String messageTemplateProcess) {
		this.messageTemplateProcess = messageTemplateProcess;
	}

	public String getMessageTemplatePostProcess() {
		return messageTemplatePostProcess;
	}

	public void setMessageTemplatePostProcess(String messageTemplatePostProcess) {
		this.messageTemplatePostProcess = messageTemplatePostProcess;
	}
	
	
	
	
}
