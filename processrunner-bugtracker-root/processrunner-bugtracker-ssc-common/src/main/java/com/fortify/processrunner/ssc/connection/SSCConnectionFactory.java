package com.fortify.processrunner.ssc.connection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.ssc.connection.SSCConnectionRetrieverTokenCredentials;
import com.fortify.ssc.connection.SSCConnectionRetrieverUserCredentials;
import com.fortify.util.rest.ProxyConfiguration;

/**
 * This utility class creates and caches an {@link SSCAuthenticatingRestConnection} instance
 * based on {@link Context} properties. The created connection will be cached in the {@link Context}.
 * 
 * @author Ruud Senden
 *
 */
public final class SSCConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_BASE_URL, "SSC base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_USER_NAME, "SSC user name (leave blank to use auth token)", true).readFromConsole(true).ignoreIfPropertySet(IContextSSCCommon.PRP_SSC_AUTH_TOKEN));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_PASSWORD, "SSC password", true).readFromConsole(true).isPassword(true).ignoreIfPropertyNotSet(IContextSSCCommon.PRP_SSC_USER_NAME));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_AUTH_TOKEN, "SSC auth token (leave blank to use username/password)", true).readFromConsole(true).isPassword(true).ignoreIfPropertySet(IContextSSCCommon.PRP_SSC_USER_NAME));
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
		
		String baseUrl = ctx.getSSCBaseUrl();
		ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "SSC");
		if ( StringUtils.isNotBlank(ctx.getSSCAuthToken()) ) {
			SSCConnectionRetrieverTokenCredentials tokenCreds = new SSCConnectionRetrieverTokenCredentials();
			tokenCreds.setBaseUrl(baseUrl);
			tokenCreds.setProxy(proxy);
			tokenCreds.setAuthToken(ctx.getSSCAuthToken());
			return tokenCreds.getConnection();
		} else if ( StringUtils.isNotBlank(ctx.getSSCUserName() )) {
			SSCConnectionRetrieverUserCredentials userCreds = new SSCConnectionRetrieverUserCredentials();
			userCreds.setBaseUrl(baseUrl);
			userCreds.setProxy(proxy);
			userCreds.setUserName(ctx.getSSCUserName());
			userCreds.setPassword(ctx.getSSCPassword());
			return userCreds.getConnection();
		} else {
			throw new IllegalStateException("Either SSC user name and password, or authentication token must be specified");
		}
	}
	
	private interface IContextSSCConnection {
		public void setSSCConnection(SSCAuthenticatingRestConnection connection);
		public SSCAuthenticatingRestConnection getSSCConnection();
	}
}
