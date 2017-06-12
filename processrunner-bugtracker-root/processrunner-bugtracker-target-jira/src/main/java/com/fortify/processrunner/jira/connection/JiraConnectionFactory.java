package com.fortify.processrunner.jira.connection;

import org.apache.http.auth.UsernamePasswordCredentials;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.ProxyConfiguration;

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
		
		String baseUrl = ctx.getJiraBaseUrl();
		String userName = ctx.getJiraUserName();
		String password = ctx.getJiraPassword();
		ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Jira");
		return new JiraRestConnection(baseUrl, new UsernamePasswordCredentials(userName, password), proxy);
	}
	
	private interface IContextJiraConnection {
		public void setJiraConnection(JiraRestConnection connection);
		public JiraRestConnection getJiraConnection();
	}
}
