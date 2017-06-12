package com.fortify.processrunner.util.rest;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.util.rest.ProxyConfiguration;

/**
 * This class allows for generating {@link ProxyConfiguration} instances
 * based on {@link Context} property values.
 * 
 * @author Ruud Senden
 *
 */
public final class ContextAwareProxyConfigurationFactory {
	private static final String PRP_SFX_URL = "ProxyUrl";
	private static final String PRP_SFX_USER_NAME = "ProxyUserName";
	private static final String PRP_SFX_PASSWORD = "ProxyPassword";
	
	private ContextAwareProxyConfigurationFactory() {}
	
	public static final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context, String name) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition(name+PRP_SFX_URL, name+" Proxy URL", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(name+PRP_SFX_USER_NAME, name+" Proxy User Name", false));
		contextPropertyDefinitions.add(new ContextPropertyDefinition(name+PRP_SFX_PASSWORD, name+" Proxy Password", false).readFromConsole(true).isPassword(true).ignoreIfPropertyNotSet(name+PRP_SFX_USER_NAME));
	}
	
	public static final ProxyConfiguration getProxyConfiguration(Context context, String name) {
		ProxyConfiguration proxy = null;
		String proxyUrl = (String)context.get(name+PRP_SFX_URL);
		if ( StringUtils.isNotBlank(proxyUrl) ) {
			proxy = new ProxyConfiguration();
			proxy.setUriString(proxyUrl);
			String userName = (String)context.get(name+PRP_SFX_USER_NAME);
			if ( StringUtils.isNotBlank(userName) ) {
				proxy.setUserName(userName);
				String password = (String)context.get(name+PRP_SFX_PASSWORD);
				if ( StringUtils.isNotBlank(password) ) {
					proxy.setPassword(password);
				}
			}
		}
		return proxy;
	}
}
