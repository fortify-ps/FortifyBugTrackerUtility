package com.fortify.processrunner.fod.connection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.fortify.fod.connection.FoDConnectionRetrieverClientCredentials;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;

public class ContextAwareFoDConnectionRetrieverClientCredentials 
	extends FoDConnectionRetrieverClientCredentials 
	implements IContextAware, IContextPropertyProvider 
{
	public void setContext(Context context) {
		updateConnectionProperties(context);
	}
	
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		result.add(new ContextProperty(IContextFoDClientCredentials.PRP_BASE_URL, "FoD base URL", context, getBaseUrl(), true));
		result.add(new ContextProperty(IContextFoDClientCredentials.PRP_CLIENT_ID, "FoD client id", context, StringUtils.isNotBlank(getClientId())?getClientId():"Read from console", false));
		result.add(new ContextProperty(IContextFoDClientCredentials.PRP_CLIENT_SECRET, "FoD client secret", context, StringUtils.isNotBlank(getClientSecret())?"******":"Read from console", false));
		return result;
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
