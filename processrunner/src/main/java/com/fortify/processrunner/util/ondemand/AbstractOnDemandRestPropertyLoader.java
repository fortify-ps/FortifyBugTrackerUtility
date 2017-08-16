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

import java.util.Map;

import javax.ws.rs.HttpMethod;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.IRestConnection;

/**
 * This abstract {@link IOnDemandPropertyLoader} implementation allows for on-demand loading of data from
 * a single REST resource, as configured through the constructor. Optionally a data expression can be
 * configured to be evaluated on the result of the REST call before returning the result.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractOnDemandRestPropertyLoader implements IOnDemandPropertyLoader<JSONMap> {
	private static final long serialVersionUID = 1L;
	private final String uri;
	private final String dataExpression;
	
	/**
	 * Constructor for setting the REST uri to be invoked
	 * @param uri
	 */
	public AbstractOnDemandRestPropertyLoader(String uri) {
		this(uri, null);
	}
	
	/**
	 * Constructor for setting the REST uri to be invoked, as well as an expression
	 * to be evaluated on the result of the REST call before returning the result.
	 * @param uri
	 * @param dataExpression
	 */
	public AbstractOnDemandRestPropertyLoader(String uri, String dataExpression) {
		this.uri = uri;
		this.dataExpression = dataExpression;
	}
	
	/**
	 * Get the value for this on-demand property loader by invoking the configured
	 * REST uri, and optionally evaluating the configured data expression on the result.
	 */
	public JSONMap getValue(Context context, Map<?, ?> targetMap) {
		IRestConnection conn = getConnection(context);
		JSONMap jsonMap = conn.executeRequest(HttpMethod.GET, conn.getBaseResource().path(uri), JSONMap.class);
		JSONMap result = dataExpression==null ? jsonMap : ContextSpringExpressionUtil.evaluateExpression(context, jsonMap, dataExpression, JSONMap.class);
		return updateData(context, result);
	}
	
	/**
	 * Subclasses can override this method to further update the data returned by the REST call
	 * @param data
	 * @return
	 */
	protected JSONMap updateData(Context context, JSONMap data) {
		return data;
	}

	/**
	 * Subclasses must implement this method to return an {@link IRestConnection} instance
	 * to be used for invoking the REST calls.
	 * @param context
	 * @return
	 */
	protected abstract IRestConnection getConnection(Context context);
}
