/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
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
