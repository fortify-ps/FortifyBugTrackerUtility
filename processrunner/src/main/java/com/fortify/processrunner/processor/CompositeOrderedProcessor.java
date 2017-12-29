/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
import java.util.List;

import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;

/**
 * This {@link CompositeProcessor} implementation will dynamically re-order
 * the list of {@link IProcessor} instances based on the current value of
 * the {@link Ordered#getOrder()} method for each {@link IProcessor}, whenever
 * the {@link #getProcessors()} method is called.
 * 
 * Note that this could cause the different {@link IProcessor} instances to
 * be called in different orders for the different processing phases.
 * 
 * @author Ruud Senden
 *
 */
public class CompositeOrderedProcessor extends CompositeProcessor {

	/**
	 * Default constructor
	 */
	public CompositeOrderedProcessor() {
		super();
	}

	/**
	 * Constructor for setting a set of processors
	 */
	public CompositeOrderedProcessor(IProcessor... processors) {
		super(processors);
	}
	
	/**
	 * This method retrieves the currently configured {@link IProcessor} instances
	 * from our superclass, and returns them in a sorted order.
	 */
	@Override
	protected List<IProcessor> getProcessors() {
		List<IProcessor> result = new ArrayList<IProcessor>(super.getProcessors());
		result.sort(new OrderComparator());
		return result;
	}
	
}
