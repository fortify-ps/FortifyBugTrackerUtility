/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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

import java.util.Collection;
import java.util.List;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;

/**
 * This abstract {@link IProcessor} implementation provides
 * support for invoking a list of individual {@link IProcessor}
 * instances. Concrete implementations of this class must 
 * implement the {@link #getProcessors()} method to return the 
 * list of individual {@link IProcessor} instances to be invoked
 * during processing. The list of {@link IProcessor} instances
 * may contain null values that will be ignored.
 * 
 * @author Ruud Senden
 */
public abstract class AbstractCompositeProcessor extends AbstractProcessor {
	
	/**
	 * Add the {@link ContextPropertyDefinition} instances by calling the 
	 * {@link IContextPropertyDefinitionProvider#addContextPropertyDefinitions(Collection, Context)} 
	 * method on each individual {@link IProcessor} instance returned by the 
	 * {@link #getProcessors()} method. 
	 */
	@Override
	public final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		for ( IProcessor processor : getProcessors() ) {
			if ( processor != null ) {
				processor.addContextPropertyDefinitions(contextPropertyDefinitions, context);
			}
		}
		addCompositeContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	/**
	 * Concrete implementations can override this method to add additional
	 * {@link ContextPropertyDefinition} instances.
	 * @param contextPropertyDefinitions
	 * @param context
	 */
	protected void addCompositeContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}

	/**
	 * Run the {@link Phase#PRE_PROCESS} phase on all
	 * {@link IProcessor} instances returned by {@link #getProcessors()}.
	 */
	@Override
	protected boolean preProcess(Context context) {
		return processForAll(Phase.PRE_PROCESS, context);
	}
	
	/**
	 * Run the {@link Phase#PROCESS} phase on all
	 * {@link IProcessor} instances returned by {@link #getProcessors()}.
	 */
	@Override
	protected boolean process(Context context) {
		return processForAll(Phase.PROCESS, context);
	}
	
	/**
	 * Run the {@link Phase#POST_PROCESS} phase on all
	 * {@link IProcessor} instances returned by {@link #getProcessors()}.
	 */
	@Override
	protected boolean postProcess(Context context) {
		return processForAll(Phase.POST_PROCESS, context);
	}
	
	/**
	 * Invoke the {@link IProcessor#process(Phase, Context)} method on 
	 * each of the {@link IProcessor} instances returned by the 
	 * {@link #getProcessors()} method for the given phase. If any of 
	 * the {@link IProcessor#process(Phase, Context)} invocations
	 * returns false, processing will stop and this method will return
	 * false. Otherwise, all {@link IProcessor} instances will be 
	 * invoked and this method will return true.
	 * @param phase
	 * @param context
	 * @return
	 */
	protected boolean processForAll(Phase phase, Context context) {
		for ( IProcessor processor : getProcessors() ) {
			if ( processor != null ) {
				if ( !processor.process(phase, context) ) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Concrete subclasses must implement this method to return
	 * the list of individual {@link IProcessor} implementations
	 * that make up this composite processor. 
	 * @return
	 */
	public abstract List<IProcessor> getProcessors();

}
