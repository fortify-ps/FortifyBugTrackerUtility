/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
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

import com.fortify.processrunner.cli.CLIOptionDefinition;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.connection.ProxyConfig;

/**
 * This class allows for generating {@link ProxyConfig} instances
 * based on {@link Context} property values.
 * 
 * @author Ruud Senden
 *
 */
public final class CLIOptionAwareProxyConfiguration {
	private static final String PRP_SFX_URL = "ProxyUrl";
	private static final String PRP_SFX_USER_NAME = "ProxyUserName";
	private static final String PRP_SFX_PASSWORD = "ProxyPassword";
	
	private CLIOptionAwareProxyConfiguration() {}
	
	/**
	 * Add {@link CLIOptionDefinitions} that describe various proxy-related settings.
	 * @param cliOptionDefinitions
	 * @param name
	 */
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions, String name) {
		cliOptionDefinitions.add(new CLIOptionDefinition(name, name+PRP_SFX_URL, name+" Proxy URL", false));
		cliOptionDefinitions.add(new CLIOptionDefinition(name, name+PRP_SFX_USER_NAME, name+" Proxy User Name", false));
		cliOptionDefinitions.add(new CLIOptionDefinition(name, name+PRP_SFX_PASSWORD, name+" Proxy Password", false).isPassword(true).dependsOnOptions(name+PRP_SFX_USER_NAME));
	}
	
	/**
	 * Create a new {@link ProxyConfig} instance based on {@link Context} properties.
	 * This method will return null if no proxy URL has been set.
	 * @param context
	 * @param name
	 * @return
	 */
	public static final ProxyConfig getProxyConfiguration(Context context, String name) {
		ProxyConfig proxy = null;
		String proxyUrl = (String)context.get(name+PRP_SFX_URL);
		if ( StringUtils.isNotBlank(proxyUrl) ) {
			proxy = new ProxyConfig();
			proxy.setUrl(proxyUrl);
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
