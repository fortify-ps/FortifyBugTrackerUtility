package com.fortify.processrunner.jira.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareJiraConnectionRetriever 
	extends JiraConnectionRetriever 
	implements IContextAware, IContextPropertyProvider 
{
	public void setContext(Context context) {
		updateConnectionProperties(context);
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextAware ) {
			((IContextAware)proxy).setContext(context);
		}
	}
	
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(IContextJiraConnectionProperties.PRP_BASE_URL, "JIRA base URL", context, StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextJiraConnectionProperties.PRP_USER_NAME, "JIRA user name", context, StringUtils.isNotBlank(getUserName())?getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextJiraConnectionProperties.PRP_PASSWORD, "JIRA password", context, StringUtils.isNotBlank(getPassword())?"******":"Read from console", false));
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextJiraConnectionProperties ctx = context.as(IContextJiraConnectionProperties.class);
		String baseUrl = ctx.getJiraBaseUrl();
		String userName = ctx.getJiraUserName();
		String password = ctx.getJiraPassword();
		
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		if ( !StringUtils.isBlank(userName) ) {
			setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			setPassword(password);
		}
		if ( getBaseUrl ()==null ) {
			setBaseUrl(System.console().readLine("JIRA URL: "));
		}
		if ( getUserName()==null ) {
			setUserName(System.console().readLine("JIRA User Name: "));
		}
		if ( getPassword()==null ) {
			setPassword(new String(System.console().readPassword("JIRA Password: ")));
		}
	}
}
