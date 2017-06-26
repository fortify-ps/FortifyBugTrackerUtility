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
package com.fortify.fod.connection;

import javax.ws.rs.client.Invocation.Builder;

import org.apache.http.client.ServiceUnavailableRetryStrategy;

import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.rest.RestConnection;
import com.fortify.util.rest.TooManyRequestsRetryStrategy;

/**
 * This class provides a basic, non-authenticating REST connection
 * for FoD. It's main characteristics compared to a standard 
 * {@link RestConnection} is that it will add an 
 * <code>Accept: application/json</code> header, and enable a 
 * 'service unavailable' strategy to retry requests that fail 
 * due to FoD rate limiting.
 */
public class FoDBasicRestConnection extends RestConnection {
	public FoDBasicRestConnection(String baseUrl, ProxyConfiguration proxy) {
		super(baseUrl);
		setProxy(proxy);
	}
	
	/**
	 * Update the {@link Builder} to add the Accept header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.accept("application/json");
	}
	
	@Override
	protected ServiceUnavailableRetryStrategy getServiceUnavailableRetryStrategy() {
		return new TooManyRequestsRetryStrategy("X-Rate-Limit-Reset");
	}
}
