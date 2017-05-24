package com.fortify.processrunner.octane.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.IOctaneCredentials;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneClientCredentials;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneUserCredentials;
import com.fortify.util.rest.AbstractRestConnectionRetriever;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareOctaneConnectionRetrieverUserOrClientCredentials 
	extends AbstractRestConnectionRetriever<OctaneAuthenticatingRestConnection>
	implements IContextAware, IContextPropertyProvider, IOctaneConnectionRetriever 
{
	private String baseUrl;
	private final OctaneUserCredentials userCreds = new OctaneUserCredentials();
	private final OctaneClientCredentials clientCreds = new OctaneClientCredentials();
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public OctaneUserCredentials getUserCreds() {
		return userCreds;
	}

	public OctaneClientCredentials getClientCreds() {
		return clientCreds;
	}

	public void setContext(Context context) {
		updateConnectionProperties(context);
		setContextForProxy(context, getProxy());
	}
	
	private void setContextForProxy(Context context, ProxyConfiguration proxy) {
		if ( proxy!=null && proxy instanceof IContextAware ) {
			((IContextAware)proxy).setContext(context);
		}
	}
	
	
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(IContextOctaneConnectionProperties.PRP_BASE_URL, "Octane base URL", context,  StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextOctaneConnectionProperties.PRP_USER_NAME, "Octane user name", context, StringUtils.isNotBlank(getUserCreds().getUserName())?getUserCreds().getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextOctaneConnectionProperties.PRP_PASSWORD, "Octane password", context, StringUtils.isNotBlank(getUserCreds().getPassword())?"******":"Read from console", false));
		contextProperties.add(new ContextProperty(IContextOctaneConnectionProperties.PRP_CLIENT_ID, "Octane client id", context, StringUtils.isNotBlank(getClientCreds().getClientId())?getClientCreds().getClientId():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextOctaneConnectionProperties.PRP_CLIENT_SECRET, "Octane client secret", context, StringUtils.isNotBlank(getClientCreds().getClientSecret())?"******":"Read from console", false));
		
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextOctaneConnectionProperties ctx = context.as(IContextOctaneConnectionProperties.class);
		String baseUrl = ctx.getOctaneBaseUrl();
		String userName = ctx.getOctaneUserName();
		String password = ctx.getOctanePassword();
		String clientId = ctx.getOctaneClientId();
		String clientSecret = ctx.getOctaneClientSecret();
		
		// Set baseUrl from context
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		
		// Set userName and password from context
		if ( !StringUtils.isBlank(userName) ) {
			getUserCreds().setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			getUserCreds().setPassword(password);
		}
		
		// Set clientId and clientSecret from context
		if ( !StringUtils.isBlank(clientId) ) {
			getClientCreds().setClientId(clientId);
		}
		if ( !StringUtils.isBlank(clientSecret) ) {
			getClientCreds().setClientSecret(clientSecret);
		}
		
		// Read base URL from console if not defined
		if ( getBaseUrl() == null) {
			setBaseUrl (System.console().readLine("Octane URL: "));
		}
		
		// Read userName from console if neither userName or clientId is defined
		if ( getUserCreds().getUserName()==null && getClientCreds().getClientId()==null ) {
			getUserCreds().setUserName(System.console().readLine("Octane User Name (leave blank to use client credentials): "));
		}
		
		if ( StringUtils.isNotBlank(getUserCreds().getUserName()) ) {
			// If userName is defined or entered via console, read password from console if not defined
			if ( StringUtils.isBlank(getUserCreds().getPassword()) ) {
				getUserCreds().setPassword(new String(System.console().readPassword("Octane Password: ")));
			}
		} else {
			// If userName is not defined and not entered via console, read clientId and clientSecret from console if not defined
			if ( getClientCreds().getClientId()==null ) {
				getClientCreds().setClientId(new String(System.console().readPassword("Octane Client Id: ")));
			}
			if ( StringUtils.isBlank(getClientCreds().getClientSecret()) ) {
				getClientCreds().setClientSecret(new String(System.console().readPassword("Octane Client Secret: ")));
			}
		}
	}

	@Override
	protected OctaneAuthenticatingRestConnection createConnection() {
		IOctaneCredentials auth = StringUtils.isNotBlank(getUserCreds().getUserName())
				? getUserCreds() : getClientCreds();
		return new OctaneAuthenticatingRestConnection(getBaseUrl(), auth, getProxy());
	}
}
