package com.fortify.processrunner.processor;

/**
 * This {@link IProcessor} implementation allows a list of 
 * individual {@link IProcessor} instances to be configured
 * using the constructor or {@link #setProcessors(IProcessor...)}
 * method. Based on functionality provided by 
 * {@link AbstractCompositeProcessor}, each of the configured
 * processors will be invoked. 
 */
public class CompositeProcessor extends AbstractCompositeProcessor {
	private IProcessor[] processors;
	
	public CompositeProcessor() {}
	
	public CompositeProcessor(IProcessor... processors) {
		this.processors = processors;
	}
	
	@Override
	public IProcessor[] getProcessors() {
		return processors;
	}
	
	public void setProcessors(IProcessor... processors) {
		this.processors = processors;
	}
}
