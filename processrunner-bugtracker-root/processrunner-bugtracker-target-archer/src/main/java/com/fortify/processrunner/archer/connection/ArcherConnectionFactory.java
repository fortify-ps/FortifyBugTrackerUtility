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
package com.fortify.processrunner.archer.connection;

import com.fortify.api.util.rest.connection.ProxyConfig;
import com.fortify.processrunner.archer.context.IContextArcher;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;

public final class ArcherConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_BASE_URL, "Archer base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_APPLICATION_NAME, "Archer application name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_INSTANCE_NAME, "Archer instance name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_USER_NAME, "Archer user name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_USER_DOMAIN, "Archer user domain, use 'undefined' if not defined", false).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_PASSWORD, "Archer password", true).readFromConsole(true).isPassword(true));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "Archer");
	}
	
	public static final ArcherAuthenticatingRestConnection getConnection(Context context) {
		IContextArcherConnection ctx = context.as(IContextArcherConnection.class);
		ArcherAuthenticatingRestConnection result = ctx.getArcherConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setArcherConnection(result);
		}
		return result;
	}

	private static final ArcherAuthenticatingRestConnection createConnection(Context context) {
		IContextArcher ctx = context.as(IContextArcher.class);
		
		ArcherAuthData auth = new ArcherAuthData();
		
		String baseUrl = ctx.getArcherBaseUrl();
		String applicationName = ctx.getArcherApplicationName();
		auth.setInstanceName(ctx.getArcherInstanceName());
		auth.setUserName(ctx.getArcherUserName());
		auth.setUserDomain("undefined".equals(ctx.getArcherUserDomain())?null:ctx.getArcherUserDomain());
		auth.setPassword(ctx.getArcherPassword());
		
		
		ProxyConfig proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Archer");
		return new ArcherAuthenticatingRestConnection(baseUrl, auth, applicationName, proxy);
	}
	
	private interface IContextArcherConnection {
		public void setArcherConnection(ArcherAuthenticatingRestConnection connection);
		public ArcherAuthenticatingRestConnection getArcherConnection();
	}
}
