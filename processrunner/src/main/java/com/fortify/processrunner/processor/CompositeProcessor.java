/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This {@link IProcessor} implementation allows a list of 
 * individual {@link IProcessor} instances to be configured
 * using the constructor or {@link #addProcessors(IProcessor...)}
 * method. Based on functionality provided by 
 * {@link AbstractCompositeProcessor}, each of the configured
 * processors will be invoked. 
 * 
 * @author Ruud Senden
 */
public class CompositeProcessor extends AbstractCompositeProcessor {
	private final List<IProcessor> processors = new ArrayList<IProcessor>();
	
	/**
	 * Default constructor, allowing manual configuration
	 * of the list of {@link IProcessor} instances to be 
	 * configured via the {@link #addProcessors(IProcessor...)}
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
		addProcessors(processors);
	}
	
	/**
	 * Get the list of configured {@link IProcessor} instances
	 * that make up this composite processor.
	 */
	@Override
	protected List<IProcessor> getProcessors() {
		return processors;
	}
	
	/**
	 * Configure the list of {@link IProcessor} instances
	 * that make up this composite processor.
	 * @param processors
	 */
	public final void addProcessors(IProcessor... processors) {
		this.processors.addAll(Arrays.asList(processors));
	}
}
