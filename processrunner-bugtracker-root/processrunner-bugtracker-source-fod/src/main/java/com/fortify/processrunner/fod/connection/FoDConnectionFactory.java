package com.fortify.processrunner.fod.connection;

import org.apache.commons.lang.StringUtils;

import com.fortify.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.fod.connection.FoDConnectionRetrieverClientCredentials;
import com.fortify.fod.connection.FoDConnectionRetrieverUserCredentials;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;

public final class FoDConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_BASE_URL, "FoD base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_TENANT, "FoD tenant", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_USER_NAME, "FoD user name (leave blank to use client credentials)", true).readFromConsole(true).ignoreIfPropertySet(IContextFoD.PRP_CLIENT_ID));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_PASSWORD, "FoD password", true).readFromConsole(true).isPassword(true).ignoreIfPropertyNotSet(IContextFoD.PRP_USER_NAME));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_CLIENT_ID, "FoD client id (leave blank to use user credentials)", true).readFromConsole(true).ignoreIfPropertySet(IContextFoD.PRP_USER_NAME));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_CLIENT_SECRET, "FoD client secret", true).readFromConsole(true).isPassword(true).ignoreIfPropertyNotSet(IContextFoD.PRP_CLIENT_ID));
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
		
		if ( StringUtils.isNotBlank(ctx.getFoDUserName()) ) {
			FoDConnectionRetrieverUserCredentials userCreds = new FoDConnectionRetrieverUserCredentials();
			userCreds.setBaseUrl(ctx.getFoDBaseUrl());
			userCreds.setProxy(ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "FoD"));
			userCreds.setTenant(ctx.getFoDTenant());
			userCreds.setUserName(ctx.getFoDUserName());
			userCreds.setPassword(ctx.getFoDPassword());
			return userCreds.getConnection();
		} else if ( StringUtils.isNotBlank(ctx.getFoDClientId()) ) {
			FoDConnectionRetrieverClientCredentials clientCreds = new FoDConnectionRetrieverClientCredentials();
			clientCreds.setBaseUrl(ctx.getFoDBaseUrl());
			clientCreds.setProxy(ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "FoD"));
			clientCreds.setClientId(ctx.getFoDClientId());
			clientCreds.setClientSecret(ctx.getFoDClientSecret());
			return clientCreds.getConnection();
		} else {
			throw new IllegalStateException("Either FoD username and password, or client id and client secret must be specified");
		}
	}
	
	private interface IContextFoDConnection {
		public void setFoDConnection(FoDAuthenticatingRestConnection connection);
		public FoDAuthenticatingRestConnection getFoDConnection();
	}
}
