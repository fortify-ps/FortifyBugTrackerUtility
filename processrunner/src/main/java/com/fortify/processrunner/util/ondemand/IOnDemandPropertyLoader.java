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
package com.fortify.processrunner.util.ondemand;

import java.io.Serializable;
import java.util.Map;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.propertyaccessor.ondemand.OnDemandMapPropertyAccessor;

/**
 * <p>This interface allows for loading properties on demand. When used in conjunction with
 * {@link OnDemandMapPropertyAccessor}, any {@link Map} values that implement this interface
 * will be automatically replaced with the value returned by the {@link #getValue(Context, Map)}
 * method.</p>
 * 
 * <p>Note that instances must be {@link Serializable}, since the on-demand property loaders
 * may need to be stored in persistent caches.</p>
 *  
 * @author Ruud Senden
 *
 * @param <T>
 */
public interface IOnDemandPropertyLoader<T> extends Serializable {
	/**
	 * Get the property value, optionally based on the given {@link Context} and target {@link Map}
	 * @param ctx
	 * @param targetMap
	 * @return
	 */
	public T getValue(Context ctx, Map<?,?> targetMap);
}
