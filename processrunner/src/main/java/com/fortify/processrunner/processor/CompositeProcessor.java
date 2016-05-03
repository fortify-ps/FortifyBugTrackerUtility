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
	
	/**
	 * Default constructor, allowing manual configuration
	 * of the list of {@link IProcessor} instances to be 
	 * configured via the {@link #setProcessors(IProcessor...)}
	 * method. 
	 */
	public CompositeProcessor() {}
	
	/**
	 * This constructor allows configuring the list
	 * of {@link IProcessor} instances that make up this
	 * composite processor.
	 * @param processors
	 */
	public CompositeProcessor(IProcessor... processors) {
		this.processors = processors;
	}
	
	/**
	 * Get the list of configured {@link IProcessor} instances
	 * that make up this composite processor.
	 */
	@Override
	public IProcessor[] getProcessors() {
		return processors;
	}
	
	/**
	 * Configure the list of {@link IProcessor} instances
	 * that make up this composite processor.
	 * @param processors
	 */
	public void setProcessors(IProcessor... processors) {
		this.processors = processors;
	}
}
