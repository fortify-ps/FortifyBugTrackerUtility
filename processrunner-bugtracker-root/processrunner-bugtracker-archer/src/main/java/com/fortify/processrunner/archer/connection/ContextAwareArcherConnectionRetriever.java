package com.fortify.processrunner.archer.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareArcherConnectionRetriever 
	extends ArcherConnectionRetriever 
	implements IContextAware, IContextPropertyProvider 
{
	public void setContext(Context context) {
		updateConnectionProperties(context);
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextAware ) {
			((IContextAware)proxy).setContext(context);
		}
	}
	
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		ArcherAuthData auth = getAuth();
		contextProperties.add(new ContextProperty(IContextArcherConnectionProperties.PRP_BASE_URL, "Archer base URL", context, StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcherConnectionProperties.PRP_INSTANCE_NAME, "Archer instance name", context, StringUtils.isNotBlank(auth.getInstanceName())?auth.getInstanceName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcherConnectionProperties.PRP_USER_NAME, "Archer user name", context, StringUtils.isNotBlank(auth.getUserName())?auth.getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcherConnectionProperties.PRP_USER_DOMAIN, "Archer user domain", context, StringUtils.isNotBlank(auth.getUserDomain())?auth.getUserDomain():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextArcherConnectionProperties.PRP_PASSWORD, "Archer password", context, StringUtils.isNotBlank(auth.getPassword())?"******":"Read from console", false));
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextArcherConnectionProperties ctx = context.as(IContextArcherConnectionProperties.class);
		String baseUrl = ctx.getArcherBaseUrl();
		String instanceName = ctx.getArcherInstanceName();
		String userName = ctx.getArcherUserName();
		String userDomain = ctx.getArcherUserDomain();
		String password = ctx.getArcherPassword();
		
		ArcherAuthData auth = getAuth();
		
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		if ( !StringUtils.isBlank(instanceName) ) {
			auth.setInstanceName(instanceName);
		}
		if ( !StringUtils.isBlank(userName) ) {
			auth.setUserName(userName);
		}
		if ( !StringUtils.isBlank(userDomain) ) {
			auth.setUserDomain(userDomain);
		}
		if ( !StringUtils.isBlank(password) ) {
			auth.setPassword(password);
		}
		if ( getBaseUrl ()==null ) {
			setBaseUrl(System.console().readLine("Archer URL: "));
		}
		if ( auth.getInstanceName()==null ) {
			auth.setInstanceName(System.console().readLine("Archer Instance Name: "));
		}
		if ( auth.getUserName()==null ) {
			auth.setUserName(System.console().readLine("Archer User Name: "));
		}
		if ( auth.getUserDomain()==null ) {
			auth.setUserDomain(System.console().readLine("Archer User Domain: "));
		}
		if ( auth.getPassword()==null ) {
			auth.setPassword(new String(System.console().readPassword("Archer Password: ")));
		}
	}
}
