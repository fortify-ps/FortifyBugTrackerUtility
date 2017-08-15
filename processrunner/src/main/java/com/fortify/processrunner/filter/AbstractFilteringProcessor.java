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
package com.fortify.processrunner.filter;

import org.springframework.core.Ordered;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.CompositeOrderedProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.util.ondemand.IOnDemandPropertyLoader;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * Abstract filtering {@link IProcessor} implementation. Concrete implementations need to implement the
 * {@link #isMatching(Context, Object)} method to perform the actual matching against the root object 
 * retrieved from the current {@link Context}. This class provides functionality for retrieving the
 * root object from the context and either including or excluding matched objects. In addition, it
 * implements the {@link Ordered} interface to allow for dynamic ordering of filters based on
 * execution time, allowing faster filters to be evaluated before slower filters. This can for
 * example be useful if filters access on-demand data (see {@link IOnDemandPropertyLoader}). For 
 * optimal ordering, all filter implementations should be managed by a single {@link CompositeOrderedProcessor}.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractFilteringProcessor extends AbstractProcessor implements Ordered {
	private SimpleExpression rootExpression;
	private boolean excludeMatchedObjects;
	private int order;
	
	/**
	 * Default constructor
	 */
	public AbstractFilteringProcessor() {}
	
	/**
	 * Constructor for setting a root expression and 'excludeMatchedObjects' flag
	 * @param rootExpression Expression to retrieve the root object from the {@link Context}
	 * @param excludeMatchedObjects if set to true, matched objects will be excluded. If set to false, matched objects will be included.
	 */
	public AbstractFilteringProcessor(SimpleExpression rootExpression, boolean excludeMatchedObjects) {
		this.rootExpression = rootExpression;
		this.excludeMatchedObjects = excludeMatchedObjects;
	}

	/**
	 * This method retrieves the root object from the given {@link Context}, calls the abstract {@link #isMatching(Context, Object)}
	 * method to determine whether the root object matches, and based on the result and the 'excludeMatchedObjects' flag,
	 * will decide whether we should continue processing the current {@link Context} or not. In addition this method
	 * calculates the execution time to allow duration-based ordering of filters.
	 */
	@Override
	protected final boolean process(Context context) {
		long startTime = System.currentTimeMillis();
		try {
			Object rootObject = getRootExpression()==null?context:ContextSpringExpressionUtil.evaluateExpression(context, context, getRootExpression(), Object.class);
			return isExcludeMatchedObjects() != isMatching(context, rootObject);
		} finally {
			int duration = (int)(System.currentTimeMillis()-startTime);
			this.order = (this.order + duration)/2;
		}
	}

	/**
	 * Concrete implementations must implement this method to perform the actual matching
	 * of the given root object.
	 * @param context
	 * @param rootObject
	 * @return
	 */
	protected abstract boolean isMatching(Context context, Object rootObject);

	/**
	 * Get the configured root expression. May be null, in which case the current {@link Context} will be used as the root object
	 * @return
	 */
	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	/**
	 * Configure the optional root expression. If not configured, the current {@link Context} will be used as the root object
	 * @param rootExpression
	 */
	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	/**
	 * Get the value of the 'exclude matched objects' flag
	 * @return true if matched objects will be excluded, false if matched objects will be included.
	 */
	public boolean isExcludeMatchedObjects() {
		return excludeMatchedObjects;
	}

	/**
	 * Set the value of the 'exclude matched objects' flag
	 * @param excludeMatchedObjects true if matched objects should be excluded, false if matched objects should be included
	 */
	public void setExcludeMatchedObjects(boolean excludeMatchedObjects) {
		this.excludeMatchedObjects = excludeMatchedObjects;
	}
	
	/**
	 * This method returns the average execution time to allow duration-based ordering of filters
	 */
	public int getOrder() {
		return order;
	}

}