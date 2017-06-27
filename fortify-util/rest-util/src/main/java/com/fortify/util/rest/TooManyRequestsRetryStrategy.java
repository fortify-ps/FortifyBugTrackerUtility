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
package com.fortify.util.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * This class implements an Apache HttpClient 4.x {@link ServiceUnavailableRetryStrategy}
 * that will retry a request if the server responds with an HTTP 429 (TOO_MANY_REQUESTS)
 * response. This can be enabled on {@link RestConnection} instances by overriding
 * {@link RestConnection#createApacheHttpClientBuilder()} as follows:
 * <code>
 *	protected HttpClientBuilder createApacheHttpClientBuilder() {
 *		return super.createApacheHttpClientBuilder().setServiceUnavailableRetryStrategy(new TooManyRequestsRetryStrategy(retryHeaderName));
 *	}
 * </code>
 * 
 */
public final class TooManyRequestsRetryStrategy implements ServiceUnavailableRetryStrategy {
	private String retryAfterHeaderName = "X-Retry-After";
	private ThreadLocal<Integer> interval = null;
	
	public TooManyRequestsRetryStrategy() {}
	public TooManyRequestsRetryStrategy(String retryAfterHeaderName) {
		this.retryAfterHeaderName = retryAfterHeaderName;
	}

	public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
		// TODO Temporary executionCount work-around for FoD issues; should check executionCount<2 
		if ( executionCount < 5 && response.getStatusLine().getStatusCode()==429 ) {
			interval = new ThreadLocal<Integer>();
			interval.set(Integer.parseInt(response.getFirstHeader(retryAfterHeaderName).getValue()));
			return true;
		}
		return false;
	}

	public long getRetryInterval() {
		long result = interval==null ? -1 : (interval.get()*1000);
		interval = null;
		// TODO Temporary work-around for FoD returning negative numbers
		if ( result < 0 ) {
			result = -result;
		}
		return result;
	}
}
