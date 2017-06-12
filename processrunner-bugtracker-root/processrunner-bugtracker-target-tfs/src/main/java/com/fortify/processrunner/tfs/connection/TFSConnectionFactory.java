package com.fortify.processrunner.tfs.connection;

import org.apache.http.auth.UsernamePasswordCredentials;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.ProxyConfiguration;

public final class TFSConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextTFS.PRP_BASE_URL, "TFS base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextTFS.PRP_USER_NAME, "TFS user name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextTFS.PRP_PASSWORD, "TFS password", true).readFromConsole(true).isPassword(true));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "TFS");
	}
	
	public static final TFSRestConnection getConnection(Context context) {
		IContextTFSConnection ctx = context.as(IContextTFSConnection.class);
		TFSRestConnection result = ctx.getTFSConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setTFSConnection(result);
		}
		return result;
	}

	private static final TFSRestConnection createConnection(Context context) {
		IContextTFS ctx = context.as(IContextTFS.class);
		
		String baseUrl = ctx.getTFSBaseUrl();
		String userName = ctx.getTFSUserName();
		String password = ctx.getTFSPassword();
		ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "TFS");
		return new TFSRestConnection(baseUrl, new UsernamePasswordCredentials(userName, password), proxy);
	}
	
	private interface IContextTFSConnection {
		public void setTFSConnection(TFSRestConnection connection);
		public TFSRestConnection getTFSConnection();
	}
}
