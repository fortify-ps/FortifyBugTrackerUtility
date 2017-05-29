package com.fortify.processrunner.archer.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.archer.context.IContextArcher;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.util.rest.ContextAwareProxyConfigurationFactory;
import com.fortify.util.rest.ProxyConfiguration;

public final class ArcherConnectionFactory 
{
	public static final void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(IContextArcher.PRP_BASE_URL, "Archer base URL", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcher.PRP_APPLICATION_NAME, "Archer application name", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcher.PRP_INSTANCE_NAME, "Archer instance name", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcher.PRP_USER_NAME, "Archer user name", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcher.PRP_USER_DOMAIN, "Archer user domain, use 'undefined' if not defined", context, "Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcher.PRP_PASSWORD, "Archer password", context, "Read from console", false));
		ContextAwareProxyConfigurationFactory.addContextProperties(contextProperties, context, "Archer");
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
		auth.setUserDomain(ctx.getArcherUserDomain());
		auth.setPassword(ctx.getArcherPassword());
		
		// Read base URL from console if not defined
		if ( StringUtils.isBlank(baseUrl) ) {
			baseUrl = System.console().readLine("Archer URL: ");
		}
		
		if ( StringUtils.isBlank(applicationName) ) {
			applicationName = System.console().readLine("Archer Application Name: ");
		}
		if ( StringUtils.isBlank(auth.getInstanceName()) ) {
			auth.setInstanceName(System.console().readLine("Archer Instance Name: "));
		}
		if ( StringUtils.isBlank(auth.getUserName()) ) {
			auth.setUserName(System.console().readLine("Archer User Name: "));
		}
		if ( StringUtils.isBlank(auth.getUserDomain()) ) {
			auth.setUserDomain(System.console().readLine("Archer User Domain: "));
		}
		if ( StringUtils.isBlank(auth.getUserDomain()) || "undefined".equalsIgnoreCase(auth.getUserDomain()) ) {
			auth.setUserDomain(null);
		}
		if ( auth.getPassword()==null ) {
			auth.setPassword(new String(System.console().readPassword("Archer Password: ")));
		}
		
		ProxyConfiguration proxy = ContextAwareProxyConfigurationFactory.getProxyConfiguration(context, "Archer");
		return new ArcherAuthenticatingRestConnection(baseUrl, auth, applicationName, proxy);
	}
	
	private interface IContextArcherConnection {
		public void setArcherConnection(ArcherAuthenticatingRestConnection connection);
		public ArcherAuthenticatingRestConnection getArcherConnection();
	}
}
