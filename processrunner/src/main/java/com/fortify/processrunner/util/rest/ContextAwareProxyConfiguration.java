package com.fortify.processrunner.util.rest;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.context.IContextAware;
import com.fortify.processrunner.context.IContextPropertyProvider;
import com.fortify.util.rest.ProxyConfiguration;

public class ContextAwareProxyConfiguration extends ProxyConfiguration implements IContextAware, IContextPropertyProvider {
	private static final String PRP_SFX_URL = "ProxyUrl";
	private static final String PRP_SFX_USER_NAME = "ProxyUserName";
	private static final String PRP_SFX_PASSWORD = "ProxyPassword";
	private String name = "";
	
	public void setContext(Context context) {
		updateProxyProperties(context);
	}
	
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty(getName()+PRP_SFX_URL, getName()+" Proxy URL", context, getUri(), false));
		contextProperties.add(new ContextProperty(getName()+PRP_SFX_USER_NAME, getName()+" Proxy User Name", context, getUserName(), false));
		contextProperties.add(new ContextProperty(getName()+PRP_SFX_PASSWORD, getName()+" Proxy Password", context, StringUtils.isNotBlank(getPassword())?"******":"Read from console if proxy user name is set", false));
		
	}
	
	protected void updateProxyProperties(Context context) {
		setUri((String)context.getOrDefault(getName()+PRP_SFX_URL, getUri()));
		setUserName((String)context.getOrDefault(getName()+PRP_SFX_USER_NAME, getUserName()));
		setPassword((String)context.getOrDefault(getName()+PRP_SFX_PASSWORD, getPassword()));
		
		if ( StringUtils.isNotBlank(getUserName()) && StringUtils.isBlank(getPassword()) ) {
			setPassword(new String(System.console().readPassword(getName()+" Proxy Password: ")));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
