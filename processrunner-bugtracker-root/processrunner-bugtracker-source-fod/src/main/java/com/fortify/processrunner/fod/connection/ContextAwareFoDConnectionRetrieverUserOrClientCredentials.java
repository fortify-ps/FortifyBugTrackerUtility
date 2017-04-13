package com.fortify.processrunner.fod.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.fod.connection.FoDConnectionRetrieverClientCredentials;
import com.fortify.fod.connection.FoDConnectionRetrieverUserCredentials;
import com.fortify.fod.connection.IFoDConnectionRetriever;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.util.rest.IRestConnectionRetriever;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareFoDConnectionRetrieverUserOrClientCredentials 
	implements IRestConnectionRetriever<FoDAuthenticatingRestConnection>, IContextAware, IContextPropertyProvider, IFoDConnectionRetriever 
{
	private FoDConnectionRetrieverUserCredentials userCreds = new FoDConnectionRetrieverUserCredentials();
	private FoDConnectionRetrieverClientCredentials clientCreds = new FoDConnectionRetrieverClientCredentials();
	
	public void setContext(Context context) {
		updateConnectionProperties(context);
		setContextForProxy(context, userCreds.getProxy());
		setContextForProxy(context, clientCreds.getProxy());
	}
	
	private void setContextForProxy(Context context, ProxyConfiguration proxy) {
		if ( proxy!=null && proxy instanceof IContextAware ) {
			((IContextAware)proxy).setContext(context);
		}
	}
	
	public void setBaseUrl(String baseUrl) {
		userCreds.setBaseUrl(baseUrl);
		clientCreds.setBaseUrl(baseUrl);
	}
	
	public String getBaseUrl() {
		// We assume that the base URL is always set via our setBaseURL() method 
		// (which updates both userCreds and clientCreds), so we ignore the base
		// URL set on clientCreds.
		return userCreds.getBaseUrl();
	}

	public void setScope(String scope) {
		userCreds.setScope(scope);
		clientCreds.setScope(scope);
	}
	
	public void setProxy(ProxyConfiguration proxy) {
		userCreds.setProxy(proxy);
		clientCreds.setProxy(proxy);
	}
	
	public ProxyConfiguration getProxy() {
		// We assume that the proxy is always set via our setProxy() method 
		// (which updates both userCreds and clientCreds), so we ignore the 
		// proxy set on clientCreds.
		return userCreds.getProxy();
	}
	
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(IContextFoDUserCredentials.PRP_BASE_URL, "FoD base URL", context,  StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextFoDUserCredentials.PRP_TENANT, "FoD tenant", context, StringUtils.isNotBlank(userCreds.getTenant())?userCreds.getTenant():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextFoDUserCredentials.PRP_USER_NAME, "FoD user name", context, StringUtils.isNotBlank(userCreds.getUserName())?userCreds.getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextFoDUserCredentials.PRP_PASSWORD, "FoD password", context, StringUtils.isNotBlank(userCreds.getPassword())?"******":"Read from console", false));
		contextProperties.add(new ContextProperty(IContextFoDClientCredentials.PRP_CLIENT_ID, "FoD client id", context, StringUtils.isNotBlank(clientCreds.getClientId())?clientCreds.getClientId():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextFoDClientCredentials.PRP_CLIENT_SECRET, "FoD client secret", context, StringUtils.isNotBlank(clientCreds.getClientSecret())?"******":"Read from console", false));
		
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextFoDUserOrClientCredentials ctx = context.as(IContextFoDUserOrClientCredentials.class);
		String baseUrl = ctx.getFoDBaseUrl();
		String tenant = ctx.getFoDTenant();
		String userName = ctx.getFoDUserName();
		String password = ctx.getFoDPassword();
		String clientId = ctx.getFoDClientId();
		String clientSecret = ctx.getFoDClientSecret();
		
		// Set baseUrl from context for both userCreds and clientCreds 
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		
		// Set userName, password and tentant from context for userCreds
		if ( !StringUtils.isBlank(userName) ) {
			userCreds.setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			userCreds.setPassword(password);
		}
		if ( !StringUtils.isBlank(tenant) ) {
			userCreds.setTenant(tenant);
		}
		
		// Set clientId and clientSecret from context for clientCreds
		if ( !StringUtils.isBlank(clientId) ) {
			clientCreds.setClientId(clientId);
		}
		if ( !StringUtils.isBlank(clientSecret) ) {
			clientCreds.setClientSecret(clientSecret);
		}
		
		// Read base URL from console if not defined
		if ( getBaseUrl() == null) {
			setBaseUrl (System.console().readLine("FoD URL: "));
		}
		
		// Read userName from console if neither userName or clientId is defined
		if ( userCreds.getUserName()==null && clientCreds.getClientId()==null ) {
			userCreds.setUserName(System.console().readLine("FoD User Name (leave blank to use client credentials): "));
		}
		
		if ( StringUtils.isNotBlank(userCreds.getUserName()) ) {
			// If userName is defined or entered via console, read password and tenant from console if not defined
			if ( userCreds.getPassword()==null ) {
				userCreds.setPassword(new String(System.console().readPassword("FoD Password: ")));
			}
			if ( userCreds.getTenant()==null ) {
				userCreds.setTenant(System.console().readLine("FoD Tenant: "));
			}
		} else {
			// If userName is not defined and not entered via console, read clientId and clientSecret from console if not defined
			if ( clientCreds.getClientId()==null ) {
				clientCreds.setClientId(new String(System.console().readPassword("FoD Client Id: ")));
			}
			if ( clientCreds.getClientSecret()==null ) {
				clientCreds.setClientSecret(new String(System.console().readPassword("FoD Client Secret: ")));
			}
		}
	}
	
	public FoDAuthenticatingRestConnection getConnection() {
		return StringUtils.isNotBlank(userCreds.getUserName()) 
				? userCreds.getConnection()
				: clientCreds.getConnection();
	}
}
