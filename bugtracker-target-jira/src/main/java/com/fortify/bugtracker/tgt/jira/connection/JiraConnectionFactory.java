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
package com.fortify.bugtracker.tgt.jira.connection;

import com.fortify.bugtracker.tgt.jira.cli.ICLIOptionsJira;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.rest.CLIOptionAwareProxyConfiguration;
import com.fortify.util.rest.connection.ProxyConfig;

public final class JiraConnectionFactory 
{
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		cliOptionDefinitions.add(ICLIOptionsJira.CLI_JIRA_BASE_URL);
		cliOptionDefinitions.add(ICLIOptionsJira.CLI_JIRA_USER_NAME);
		cliOptionDefinitions.add(ICLIOptionsJira.CLI_JIRA_PASSWORD);
		CLIOptionAwareProxyConfiguration.addCLIOptionDefinitions(cliOptionDefinitions, "Jira");
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
		ProxyConfig proxy = CLIOptionAwareProxyConfiguration.getProxyConfiguration(context, "Jira");
		return JiraRestConnection.builder()
			.proxy(proxy)
			.baseUrl(ICLIOptionsJira.CLI_JIRA_BASE_URL.getValue(context))
			.userName(ICLIOptionsJira.CLI_JIRA_USER_NAME.getValue(context))
			.password(ICLIOptionsJira.CLI_JIRA_PASSWORD.getValue(context))
			.build();
	}
	
	private interface IContextJiraConnection {
		public void setJiraConnection(JiraRestConnection connection);
		public JiraRestConnection getJiraConnection();
	}
}
