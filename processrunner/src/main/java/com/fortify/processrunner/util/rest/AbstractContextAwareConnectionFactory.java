/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.processrunner.util.rest;

import com.fortify.api.util.rest.connection.AbstractRestConnectionConfig;
import com.fortify.api.util.rest.connection.AbstractRestConnectionRetriever;
import com.fortify.api.util.rest.connection.IRestConnection;
import com.fortify.api.util.rest.connection.IRestConnectionBuilder;
import com.fortify.processrunner.context.Context;

public abstract class AbstractContextAwareConnectionFactory<ConnType extends IRestConnection, BuilderType extends AbstractRestConnectionConfig<?> & IRestConnectionBuilder<ConnType>> {
	private final String contextPropertyPrefix;
	
	public AbstractContextAwareConnectionFactory(String contextPropertyPrefix) {
		this.contextPropertyPrefix = contextPropertyPrefix;
	}
	
	@SuppressWarnings("unchecked")
	public ConnType getConnection(Context context) {
		String key = contextPropertyPrefix+"Connection";
		ConnType result = (ConnType)context.get(key);
		if ( result == null ) {
			result = createConnection(context);
			context.put(key, result);
		}
		return result;
	}
	
	private ConnType createConnection(Context context) {
		AbstractRestConnectionRetriever<ConnType, BuilderType> retriever = createConnectionRetriever();
		retriever.getConfig().fromMap(context, contextPropertyPrefix, true);
		return retriever.getConnection();
	}
	
	public abstract AbstractRestConnectionRetriever<ConnType, BuilderType> createConnectionRetriever();

}
