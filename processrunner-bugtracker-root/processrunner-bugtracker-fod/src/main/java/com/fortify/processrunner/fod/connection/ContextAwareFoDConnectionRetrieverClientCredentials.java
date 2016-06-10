package com.fortify.processrunner.fod.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.fod.connection.FoDConnectionRetrieverClientCredentials;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareFoDConnectionRetrieverClientCredentials 
	extends FoDConnectionRetrieverClientCredentials 
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
		contextProperties.add(new ContextProperty(IContextFoDClientCredentials.PRP_BASE_URL, "FoD base URL", context, getBaseUrl(), true));
		contextProperties.add(new ContextProperty(IContextFoDClientCredentials.PRP_CLIENT_ID, "FoD client id", context, StringUtils.isNotBlank(getClientId())?getClientId():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextFoDClientCredentials.PRP_CLIENT_SECRET, "FoD client secret", context, StringUtils.isNotBlank(getClientSecret())?"******":"Read from console", false));
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextFoDClientCredentials ctx = context.as(IContextFoDClientCredentials.class);
		String baseUrl = ctx.getFoDBaseUrl();
		String clientId = ctx.getFoDClientId();
		String clientSecret = ctx.getFoDClientSecret();
		
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		if ( !StringUtils.isBlank(clientId) ) {
			setClientId(clientId);
		}
		if ( !StringUtils.isBlank(clientSecret) ) {
			setClientSecret(clientSecret);
		}
		
		if ( getClientId()==null ) {
			setClientId(System.console().readLine("FoD Client Id: "));
		}
		if ( getClientSecret()==null ) {
			setClientSecret(System.console().readLine("FoD Client Secret: "));
		}
	}
}
