package com.fortify.processrunner.archer.connection;

import com.fortify.processrunner.archer.context.IContextArcher;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.ProxyConfiguration;

public final class ArcherConnectionFactory 
{
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_BASE_URL, "Archer base URL", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_APPLICATION_NAME, "Archer application name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_INSTANCE_NAME, "Archer instance name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_USER_NAME, "Archer user name", true).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_USER_DOMAIN, "Archer user domain, use 'undefined' if not defined", false).readFromConsole(true));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextArcher.PRP_PASSWORD, "Archer password", true).readFromConsole(true).isPassword(true));
		ContextAwareProxyConfigurationFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context, "Archer");
	}
	
	public static final ArcherAuthenticatingRestConnection getConnection(Context context) {
		IContextArcherConnection ctx = context.as(IContextArcherConnection.class);
		ArcherAuthenticatingRestConnection result = ctx.getArcherConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setArcherConnection(result);
		}
		return result;
	}

	private static final ArcherAuthenticatingRestConnection createConnection(Context context) {
		IContextArcher ctx = context.as(IContextArcher.class);
		
		ArcherAuthData auth = new ArcherAuthData();
		
		String baseUrl = ctx.getArcherBaseUrl();
		String applicationName = ctx.getArcherApplicationName();
		auth.setInstanceName(ctx.getArcherInstanceName());
		auth.setUserName(ctx.getArcherUserName());
		auth.setUserDomain("undefined".equals(ctx.getArcherUserDomain())?null:ctx.getArcherUserDomain());
		auth.setPassword(ctx.getArcherPassword());
		
		
		ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Archer");
		return new ArcherAuthenticatingRestConnection(baseUrl, auth, applicationName, proxy);
	}
	
	private interface IContextArcherConnection {
		public void setArcherConnection(ArcherAuthenticatingRestConnection connection);
		public ArcherAuthenticatingRestConnection getArcherConnection();
	}
}
