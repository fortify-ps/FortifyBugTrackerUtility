package com.fortify.processrunner.fod.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.fod.connection.FoDConnectionRetrieverClientCredentials;
import com.fortify.fod.connection.FoDConnectionRetrieverUserCredentials;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;

public final class FoDConnectionFactory 
{
	public static final void addContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_BASE_URL, "FoD base URL", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_TENANT, "FoD tenant", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_USER_NAME, "FoD user name", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_PASSWORD, "FoD password", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_CLIENT_ID, "FoD client id", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_CLIENT_SECRET, "FoD client secret", context, "Read from console", false));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "FoD");
	}
	
	public static final FoDAuthenticatingRestConnection getConnection(Context context) {
		IContextFoDConnection ctx = context.as(IContextFoDConnection.class);
		FoDAuthenticatingRestConnection result = ctx.getFoDConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setFoDConnection(result);
		}
		return result;
	}

	private static final FoDAuthenticatingRestConnection createConnection(Context context) {
		IContextFoD ctx = context.as(IContextFoD.class);
		
		FoDAuthenticatingRestConnection result;
		FoDConnectionRetrieverUserCredentials userCreds = new FoDConnectionRetrieverUserCredentials();
		FoDConnectionRetrieverClientCredentials clientCreds = new FoDConnectionRetrieverClientCredentials();
		
		String baseUrl = ctx.getFoDBaseUrl();
		userCreds.setTenant(ctx.getFoDTenant());
		userCreds.setUserName(ctx.getFoDUserName());
		userCreds.setPassword(ctx.getFoDPassword());
		clientCreds.setClientId(ctx.getFoDClientId());
		clientCreds.setClientSecret(ctx.getFoDClientSecret());
		
		// Read base URL from console if not defined
		if ( StringUtils.isBlank(baseUrl) ) {
			baseUrl = System.console().readLine("FoD URL: ");
		}
		
		// Read userName from console if neither userName or clientId is defined
		if ( StringUtils.isBlank(userCreds.getUserName()) && StringUtils.isBlank(clientCreds.getClientId()) ) {
			userCreds.setUserName(System.console().readLine("FoD User Name (leave blank to use client credentials): "));
		}
		
		if ( StringUtils.isNotBlank(userCreds.getUserName()) ) {
			// If userName is defined or entered via console, read password and tenant from console if not defined
			if ( StringUtils.isBlank(userCreds.getPassword()) ) {
				userCreds.setPassword(new String(System.console().readPassword("FoD Password: ")));
			}
			if ( StringUtils.isBlank(userCreds.getTenant())) {
				userCreds.setTenant(System.console().readLine("FoD Tenant: "));
			}
			userCreds.setBaseUrl(baseUrl);
			userCreds.setProxy(ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "FoD"));
			result = userCreds.getConnection();
		} else {
			// If userName is not defined and not entered via console, read clientId and clientSecret from console if not defined
			if ( StringUtils.isBlank(clientCreds.getClientId()) ) {
				clientCreds.setClientId(System.console().readLine("FoD Client Id: "));
			}
			if ( StringUtils.isBlank(clientCreds.getClientSecret()) ) {
				clientCreds.setClientSecret(new String(System.console().readPassword("FoD Client Secret: ")));
			}
			clientCreds.setBaseUrl(baseUrl);
			clientCreds.setProxy(ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "FoD"));
			result = clientCreds.getConnection();
		}
		
		return result;
	}
	
	private interface IContextFoDConnection {
		public void setFoDConnection(FoDAuthenticatingRestConnection connection);
		public FoDAuthenticatingRestConnection getFoDConnection();
	}
}
