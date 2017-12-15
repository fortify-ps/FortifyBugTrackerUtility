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
package com.fortify.processrunner.fod.releases;

import org.springframework.core.Ordered;

import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.processrunner.context.Context;

/**
 * This interface allows for filtering FoD releases by implementing the
 * {@link #isApplicationVersionIncluded(Context, JSONMap)} method.
 * 
 * @author Ruud Senden
 *
 */
public interface IFoDReleaseFilter extends Ordered {
	public boolean isReleaseIncluded(Context context, JSONMap release);
	/**
	 * This method defines the filter order, to have less expensive filters
	 * be processed before more expensive filters. As a rule of thumb, this
	 * method should return the following number:
	 * numberOfCachedRESTCalls+10*numberOfNonCachedRESTCalls.
	 */
	int getOrder();
}
