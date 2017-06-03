package com.fortify.processrunner.ssc.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.ssc.connection.SSCConnectionRetrieverTokenCredentials;
import com.fortify.ssc.connection.SSCConnectionRetrieverUserCredentials;

public final class SSCConnectionFactory 
{
	public static final void addContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_BASE_URL, "SSC base URL", context,  "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_USER_NAME, "SSC user name", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_PASSWORD, "SSC password", context, "Read from console", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_AUTH_TOKEN, "SSC auth token", context, "Read from console", false));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "SSC");
	}
	
	public static final SSCAuthenticatingRestConnection getConnection(Context context) {
		IContextSSCConnection ctx = context.as(IContextSSCConnection.class);
		SSCAuthenticatingRestConnection result = ctx.getSSCConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setSSCConnection(result);
		}
		return result;
	}

	private static final SSCAuthenticatingRestConnection createConnection(Context context) {
		IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
		
		SSCAuthenticatingRestConnection result;
		SSCConnectionRetrieverUserCredentials userCreds = new SSCConnectionRetrieverUserCredentials();
		SSCConnectionRetrieverTokenCredentials tokenCreds = new SSCConnectionRetrieverTokenCredentials();
		
		String baseUrl = ctx.getSSCBaseUrl();
		userCreds.setUserName(ctx.getSSCUserName());
		userCreds.setPassword(ctx.getSSCPassword());
		tokenCreds.setAuthToken(ctx.getSSCAuthToken());
		
		// Read base URL from console if not defined
		if ( StringUtils.isBlank(baseUrl) ) {
			baseUrl = System.console().readLine("SSC URL: ");
		}
		
		// Read userName from console if neither userName or clientId is defined
		if ( StringUtils.isBlank(userCreds.getUserName()) && StringUtils.isBlank(tokenCreds.getAuthToken()) ) {
			userCreds.setUserName(System.console().readLine("SSC User Name (leave blank to use token-based authentication): "));
		}
		
		if ( StringUtils.isNotBlank(userCreds.getUserName()) ) {
			// If userName is defined or entered via console, read password and tenant from console if not defined
			if ( StringUtils.isBlank(userCreds.getPassword()) ) {
				userCreds.setPassword(new String(System.console().readPassword("SSC Password: ")));
			}
			userCreds.setBaseUrl(baseUrl);
			userCreds.setProxy(ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "SSC"));
			result = userCreds.getConnection();
		} else {
			// If userName is not defined and not entered via console, read authentication token from console if not defined
			if ( StringUtils.isBlank(tokenCreds.getAuthToken()) ) {
				tokenCreds.setAuthToken(new String(System.console().readPassword("SSC Authentication Token: ")));
			}
			tokenCreds.setBaseUrl(baseUrl);
			tokenCreds.setProxy(ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "SSC"));
			result = tokenCreds.getConnection();
		}
		
		return result;
	}
	
	private interface IContextSSCConnection {
		public void setSSCConnection(SSCAuthenticatingRestConnection connection);
		public SSCAuthenticatingRestConnection getSSCConnection();
	}
}
