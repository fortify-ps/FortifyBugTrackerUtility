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
package com.fortify.processrunner.tfs.connection;

import com.fortify.api.util.rest.connection.ProxyConfig;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;

public final class TFSConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextTFS.PRP_BASE_URL, "TFS base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextTFS.PRP_USER_NAME, "TFS user name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextTFS.PRP_PASSWORD, "TFS password", true).readFromConsole(true).isPassword(true));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "TFS");
	}
	
	public static final TFSRestConnection getConnection(Context context) {
		IContextTFSConnection ctx = context.as(IContextTFSConnection.class);
		TFSRestConnection result = ctx.getTFSConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setTFSConnection(result);
		}
		return result;
	}

	private static final TFSRestConnection createConnection(Context context) {
		IContextTFS ctx = context.as(IContextTFS.class);
		
		ProxyConfig proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "TFS");
		return TFSRestConnection.builder()
			.proxy(proxy)
			.baseUrl(ctx.getTFSBaseUrl())
			.userName(ctx.getTFSUserName())
			.password(ctx.getTFSPassword())
			.build();
	}
	
	private interface IContextTFSConnection {
		public void setTFSConnection(TFSRestConnection connection);
		public TFSRestConnection getTFSConnection();
	}
}
