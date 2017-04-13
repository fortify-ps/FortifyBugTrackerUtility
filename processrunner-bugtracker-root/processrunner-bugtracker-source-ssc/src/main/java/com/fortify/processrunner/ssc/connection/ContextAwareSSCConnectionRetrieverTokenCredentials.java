package com.fortify.processrunner.ssc.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.ssc.connection.SSCConnectionRetrieverTokenCredentials;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareSSCConnectionRetrieverTokenCredentials 
	extends SSCConnectionRetrieverTokenCredentials
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
		contextProperties.add(new ContextProperty(IContextSSCTokenCredentials.PRP_SSC_BASE_URL, "SSC base URL", context,  StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextSSCTokenCredentials.PRP_SSC_AUTH_TOKEN, "SSC auth token", context, StringUtils.isNotBlank(getAuthToken())?getAuthToken():"Read from console", false));
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextSSCTokenCredentials ctx = context.as(IContextSSCTokenCredentials.class);
		String baseUrl = ctx.getSSCBaseUrl();
		String authToken = ctx.getSSCAuthToken();
		
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		if ( !StringUtils.isBlank(authToken) ) {
			setAuthToken(authToken);
		}
		if ( getBaseUrl() == null) {
			setBaseUrl (System.console().readLine("SSC URL: "));
		}
		
		if ( getAuthToken()==null ) {
			setAuthToken(System.console().readLine("SSC Auth Token: "));
		}
	}
}
