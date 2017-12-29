/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.processrunner.jira.connection;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.connection.ProxyConfig;

public final class JiraConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextJira.PRP_BASE_URL, "JIRA base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextJira.PRP_USER_NAME, "JIRA user name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextJira.PRP_PASSWORD, "JIRA password", true).readFromConsole(true).isPassword(true));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "Jira");
	}
	
	public static final JiraRestConnection getConnection(Context context) {
		IContextJiraConnection ctx = context.as(IContextJiraConnection.class);
		JiraRestConnection result = ctx.getJiraConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setJiraConnection(result);
		}
		return result;
	}

	private static final JiraRestConnection createConnection(Context context) {
		IContextJira ctx = context.as(IContextJira.class);
		
		ProxyConfig proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Jira");
		return JiraRestConnection.builder()
			.proxy(proxy)
			.baseUrl(ctx.getJiraBaseUrl())
			.userName(ctx.getJiraUserName())
			.password(ctx.getJiraPassword())
			.build();
	}
	
	private interface IContextJiraConnection {
		public void setJiraConnection(JiraRestConnection connection);
		public JiraRestConnection getJiraConnection();
	}
}
