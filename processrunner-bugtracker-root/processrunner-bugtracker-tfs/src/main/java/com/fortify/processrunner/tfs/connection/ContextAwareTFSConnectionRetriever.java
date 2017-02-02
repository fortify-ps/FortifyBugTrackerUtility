package com.fortify.processrunner.tfs.connection;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareTFSConnectionRetriever 
	extends TFSConnectionRetriever 
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
		contextProperties.add(new ContextProperty(IContextTFSConnectionProperties.PRP_BASE_URL, "TFS base URL", context, StringUtils.isNotBlank(getBaseUrl())?getBaseUrl():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextTFSConnectionProperties.PRP_USER_NAME, "TFS user name", context, StringUtils.isNotBlank(getUserName())?getUserName():"Read from console", false));
		contextProperties.add(new ContextProperty(IContextTFSConnectionProperties.PRP_PASSWORD, "TFS password", context, StringUtils.isNotBlank(getPassword())?"******":"Read from console", false));
		ProxyConfiguration proxy = getProxy();
		if ( proxy!=null && proxy instanceof IContextPropertyProvider ) {
			((IContextPropertyProvider)proxy).addContextProperties(contextProperties, context);
		}
	}
	
	protected void updateConnectionProperties(Context context) {
		IContextTFSConnectionProperties ctx = context.as(IContextTFSConnectionProperties.class);
		String baseUrl = ctx.getTFSBaseUrl();
		String userName = ctx.getTFSUserName();
		String password = ctx.getTFSPassword();
		
		if ( !StringUtils.isBlank(baseUrl) ) {
			setBaseUrl(baseUrl);
		}
		if ( !StringUtils.isBlank(userName) ) {
			setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			setPassword(password);
		}
		if ( getBaseUrl ()==null ) {
			setBaseUrl(System.console().readLine("TFS URL: "));
		}
		if ( getUserName()==null ) {
			setUserName(System.console().readLine("TFS User Name: "));
		}
		if ( getPassword()==null ) {
			setPassword(new String(System.console().readPassword("TFS Password: ")));
		}
	}
}
