package com.fortify.processrunner.ssc.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.ssc.connection.ISSCConnectionRetriever;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.ssc.connection.SSCConnectionRetrieverTokenCredentials;
import com.fortify.ssc.connection.SSCConnectionRetrieverUserCredentials;
import com.fortify.util.rest.IRestConnectionRetriever;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareSSCConnectionRetrieverUserOrTokenCredentials 
	implements IRestConnectionRetriever<SSCAuthenticatingRestConnection>, IContextAware, IContextPropertyProvider, ISSCConnectionRetriever 
{
	private SSCConnectionRetrieverUserCredentials userCreds = new SSCConnectionRetrieverUserCredentials();
	private SSCConnectionRetrieverTokenCredentials tokenCreds = new SSCConnectionRetrieverTokenCredentials();
	
	public void setContext(Context context) {
		updateConnectionProperties(context);
		setContextForProxy(context, userCreds.getProxy());
		setContextForProxy(context, tokenCreds.getProxy());
	}
	
	private void setContextForProxy(Context context, ProxyConfiguration proxy) {
		if ( proxy!=null && proxy instanceof IContextAware ) {
			((IContextAware)proxy).setContext(context);
		}
	}
	
	public void setBaseUrl(String baseUrl) {
		userCreds.setBaseUrl(baseUrl);
		tokenCreds.setBaseUrl(baseUrl);
	}
	
	public String getBaseUrl() {
		// We assume that the base URL is always set via our setBaseURL() method 
		// (which updates both userCreds and clientCreds), so we ignore the base
		// URL set on clientCreds.
		return userCreds.getBaseUrl();
	}
	
	public void setProxy(ProxyConfiguration proxy) {
		userCreds.setProxy(proxy);
		tokenCreds.setProxy(proxy);
	}
	
	public ProxyConfiguration getProxy() {
		// We assume that the proxy is always set via our setProxy() method 
		// (which updates both userCreds and clientCreds), so we ignore the 
		// proxy set on clientCreds.
		return userCreds.getProxy();
	}
	
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(IContextSSCUserCredentials.PRP_SSC_BASE_URL, "SSC base URL", context,  StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextSSCUserCredentials.PRP_SSC_USER_NAME, "SSC user name", context, StringUtils.isNotBlank(userCreds.getUserName())?userCreds.getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextSSCUserCredentials.PRP_SSC_PASSWORD, "SSC password", context, StringUtils.isNotBlank(userCreds.getPassword())?"******":"Read from console", false));
		contextProperties.add(new ContextProperty(IContextSSCTokenCredentials.PRP_SSC_AUTH_TOKEN, "SSC auth token", context, StringUtils.isNotBlank(tokenCreds.getAuthToken())?"******":"Read from console", false));
		
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextSSCUserOrTokenCredentials ctx = context.as(IContextSSCUserOrTokenCredentials.class);
		String baseUrl = ctx.getSSCBaseUrl();
		String userName = ctx.getSSCUserName();
		String password = ctx.getSSCPassword();
		String authToken = ctx.getSSCAuthToken();
		
		// Set baseUrl from context for both userCreds and tokenCreds 
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		
		// Set userName and password from context for userCreds
		if ( !StringUtils.isBlank(userName) ) {
			userCreds.setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			userCreds.setPassword(password);
		}
		
		// Set authToken from context for tokenCreds
		if ( !StringUtils.isBlank(authToken) ) {
			tokenCreds.setAuthToken(authToken);
		}
		
		// Read base URL from console if not defined
		if ( getBaseUrl() == null) {
			setBaseUrl (System.console().readLine("SSC URL: "));
		}
		
		// Read userName from console if neither userName or authToken is defined
		if ( userCreds.getUserName()==null && tokenCreds.getAuthToken()==null ) {
			userCreds.setUserName(System.console().readLine("SSC User Name (leave blank to use auth token): "));
		}
		
		if ( StringUtils.isNotBlank(userCreds.getUserName()) ) {
			// If userName is defined or entered via console, read password from console if not defined
			if ( userCreds.getPassword()==null ) {
				userCreds.setPassword(new String(System.console().readPassword("SSC Password: ")));
			}
		} else {
			// If userName is not defined and not entered via console, read auth token from console if not defined
			if ( tokenCreds.getAuthToken()==null ) {
				tokenCreds.setAuthToken(new String(System.console().readPassword("SSC Auth Token: ")));
			}
		}
	}
	
	public SSCAuthenticatingRestConnection getConnection() {
		return StringUtils.isNotBlank(userCreds.getUserName()) 
				? userCreds.getConnection()
				: tokenCreds.getConnection();
	}
}
