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
package com.fortify.bugtracker.tgt.octane.connection;

import com.fortify.bugtracker.tgt.octane.cli.ICLIOptionsOctane;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.rest.CLIOptionAwareProxyConfiguration;
import com.fortify.util.rest.connection.ProxyConfig;

public final class OctaneConnectionFactory 
{
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_BASE_URL);
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_USER_NAME);
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_PASSWORD);
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_CLIENT_ID);
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_CLIENT_SECRET);
		CLIOptionAwareProxyConfiguration.addCLIOptionDefinitions(cliOptionDefinitions, "Octane");
	}
	
	public static final OctaneAuthenticatingRestConnection getConnection(Context context) {
		IContextOctaneConnection ctx = context.as(IContextOctaneConnection.class);
		OctaneAuthenticatingRestConnection result = ctx.getOctaneConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setOctaneConnection(result);
		}
		return result;
	}

	private static final OctaneAuthenticatingRestConnection createConnection(Context context) {
		ProxyConfig proxy = CLIOptionAwareProxyConfiguration.getProxyConfiguration(context, "Octane");
		return OctaneAuthenticatingRestConnection.builder()
			.proxy(proxy)
			.baseUrl(ICLIOptionsOctane.CLI_OCTANE_BASE_URL.getValue(context))
			.clientId(ICLIOptionsOctane.CLI_OCTANE_CLIENT_ID.getValue(context))
			.clientSecret(ICLIOptionsOctane.CLI_OCTANE_CLIENT_SECRET.getValue(context))
			.userName(ICLIOptionsOctane.CLI_OCTANE_USER_NAME.getValue(context))
			.password(ICLIOptionsOctane.CLI_OCTANE_PASSWORD.getValue(context))
			.build();
	}
	
	private interface IContextOctaneConnection {
		public void setOctaneConnection(OctaneAuthenticatingRestConnection connection);
		public OctaneAuthenticatingRestConnection getOctaneConnection();
	}
}
