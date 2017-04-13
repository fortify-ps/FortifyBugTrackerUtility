package com.fortify.processrunner.ssc.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.ssc.connection.SSCConnectionRetrieverUserCredentials;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareSSCConnectionRetrieverUserCredentials 
	extends SSCConnectionRetrieverUserCredentials 
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
		contextProperties.add(new ContextProperty(IContextSSCUserCredentials.PRP_SSC_BASE_URL, "SSC base URL", context,  StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextSSCUserCredentials.PRP_SSC_USER_NAME, "SSC user name", context, StringUtils.isNotBlank(getUserName())?getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextSSCUserCredentials.PRP_SSC_PASSWORD, "SSC password", context, StringUtils.isNotBlank(getPassword())?"******":"Read from console", false));
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextSSCUserCredentials ctx = context.as(IContextSSCUserCredentials.class);
		String baseUrl = ctx.getSSCBaseUrl();
		String userName = ctx.getSSCUserName();
		String password = ctx.getSSCPassword();
		
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		if ( !StringUtils.isBlank(userName) ) {
			setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			setPassword(password);
		}		
		if ( getBaseUrl() == null) {
			setBaseUrl (System.console().readLine("SSC URL: "));
		}		
		if ( getUserName()==null ) {
			setUserName(System.console().readLine("SSC User Name: "));
		}
		if ( getPassword()==null ) {
			setPassword(new String(System.console().readPassword("SSC Password: ")));
		}
	}
}
