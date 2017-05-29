package com.fortify.processrunner.jira.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.jira.context.IContextJira;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.ProxyConfiguration;

public final class JiraConnectionFactory 
{
	public static final void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(IContextJira.PRP_BASE_URL, "JIRA base URL", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextJira.PRP_USER_NAME, "JIRA user name", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextJira.PRP_PASSWORD, "JIRA password", context, "Read from console", false));
		ContextAwareProxyConfigurationFactory.addContextProperties(contextProperties, context, "Jira");
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
		
		// Read base URL from console if not defined
		if ( StringUtils.isBlank(baseUrl) ) {
			baseUrl = System.console().readLine("Jira URL: ");
		}
		
		// Read user name from console if not defined
		if ( StringUtils.isBlank(userName) ) {
			userName = System.console().readLine("Jira User Name: ");
		}
		
		// Read password from console if not defined
		if ( StringUtils.isBlank(password) ) {
			password = new String(System.console().readPassword("Jira Password: "));
		}
		
		ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Jira");
		return new JiraRestConnection(baseUrl, new UsernamePasswordCredentials(userName, password), proxy);
	}
	
	private interface IContextJiraConnection {
		public void setJiraConnection(JiraRestConnection connection);
		public JiraRestConnection getJiraConnection();
	}
}
