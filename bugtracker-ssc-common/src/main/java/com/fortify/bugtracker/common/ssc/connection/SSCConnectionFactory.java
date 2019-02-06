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
package com.fortify.bugtracker.common.ssc.connection;

import com.fortify.bugtracker.common.ssc.cli.ICLIOptionsSSC;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.rest.CLIOptionAwareProxyConfiguration;
import com.fortify.util.rest.connection.ProxyConfig;

/**
 * This class allows for instantiating and caching {@link SSCAuthenticatingRestConnection}
 * instances based on {@link Context} properties.
 * 
 * @author Ruud Senden
 *
 */
public final class SSCConnectionFactory 
{
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_BASE_URL);
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_USER_NAME);
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_PASSWORD);
		cliOptionDefinitions.add(ICLIOptionsSSC.CLI_SSC_AUTH_TOKEN);
		CLIOptionAwareProxyConfiguration.addCLIOptionDefinitions(cliOptionDefinitions, "SSC");
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
		ProxyConfig proxy = CLIOptionAwareProxyConfiguration.getProxyConfiguration(context, "SSC");
		return SSCAuthenticatingRestConnection.builder()
			.proxy(proxy)
			.baseUrl(ICLIOptionsSSC.CLI_SSC_BASE_URL.getValue(context))
			.authToken(ICLIOptionsSSC.CLI_SSC_AUTH_TOKEN.getValue(context))
			.userName(ICLIOptionsSSC.CLI_SSC_USER_NAME.getValue(context))
			.password(ICLIOptionsSSC.CLI_SSC_PASSWORD.getValue(context))
			.build();
	}
	
	private interface IContextSSCConnection {
		public void setSSCConnection(SSCAuthenticatingRestConnection connection);
		public SSCAuthenticatingRestConnection getSSCConnection();
	}
}
