package com.fortify.processrunner.octane.connection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneClientCredentials;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneUserCredentials;
import com.fortify.processrunner.octane.context.IContextOctane;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.ProxyConfiguration;

public final class OctaneConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_BASE_URL, "Octane base URL", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_USER_NAME, "Octane user name", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_PASSWORD, "Octane password", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_CLIENT_ID, "Octane client id", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextOctane.PRP_CLIENT_SECRET, "Octane client secret", context, "Read from console", false));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "Octane");
	}
	
	public static final OctaneAuthenticatingRestConnection getConnection(Context context) {
		IContextOctaneConnection ctx = context.as(IContextOctaneConnection.class);
		OctaneAuthenticatingRestConnection result = ctx.getOctaneConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setOctaneConnection(result);
		}
		return result;
	}

	private static final OctaneAuthenticatingRestConnection createConnection(Context context) {
		IContextOctane ctx = context.as(IContextOctane.class);
		
		OctaneAuthenticatingRestConnection result;
		OctaneUserCredentials userCreds = new OctaneUserCredentials();
		OctaneClientCredentials clientCreds = new OctaneClientCredentials();
		
		String baseUrl = ctx.getOctaneBaseUrl();
		userCreds.setUserName(ctx.getOctaneUserName());
		userCreds.setPassword(ctx.getOctanePassword());
		clientCreds.setClientId(ctx.getOctaneClientId());
		clientCreds.setClientSecret(ctx.getOctaneClientSecret());
		
		// Read base URL from console if not defined
		if ( StringUtils.isBlank(baseUrl) ) {
			baseUrl = System.console().readLine("Octane URL: ");
		}
		
		// Read userName from console if neither userName or clientId is defined
		if ( StringUtils.isBlank(userCreds.getUserName()) && StringUtils.isBlank(clientCreds.getClientId()) ) {
			userCreds.setUserName(System.console().readLine("Octane User Name (leave blank to use client credentials): "));
		}
		
		if ( StringUtils.isNotBlank(userCreds.getUserName()) ) {
			// If userName is defined or entered via console, read password and tenant from console if not defined
			if ( StringUtils.isBlank(userCreds.getPassword()) ) {
				userCreds.setPassword(new String(System.console().readPassword("Octane Password: ")));
			}
			ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Octane");
			result = new OctaneAuthenticatingRestConnection(baseUrl, userCreds, proxy);
		} else {
			// If userName is not defined and not entered via console, read clientId and clientSecret from console if not defined
			if ( StringUtils.isBlank(clientCreds.getClientId()) ) {
				clientCreds.setClientId(System.console().readLine("Octane Client Id: "));
			}
			if ( StringUtils.isBlank(clientCreds.getClientSecret()) ) {
				clientCreds.setClientSecret(new String(System.console().readPassword("Octane Client Secret: ")));
			}
			ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Octane");
			result = new OctaneAuthenticatingRestConnection(baseUrl, clientCreds, proxy);
		}
		return result;
	}
	
	private interface IContextOctaneConnection {
		public void setOctaneConnection(OctaneAuthenticatingRestConnection connection);
		public OctaneAuthenticatingRestConnection getOctaneConnection();
	}
}
