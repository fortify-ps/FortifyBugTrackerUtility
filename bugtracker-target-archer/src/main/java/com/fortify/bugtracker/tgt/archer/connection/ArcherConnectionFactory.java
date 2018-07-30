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
package com.fortify.bugtracker.tgt.archer.connection;

import com.fortify.bugtracker.tgt.archer.cli.ICLIOptionsArcher;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.rest.CLIOptionAwareProxyConfiguration;
import com.fortify.util.rest.connection.ProxyConfig;

public final class ArcherConnectionFactory 
{
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cLIOptionDefinitions, Context context) {
		cLIOptionDefinitions.add(ICLIOptionsArcher.CLI_ARCHER_BASE_URL);
		cLIOptionDefinitions.add(ICLIOptionsArcher.CLI_ARCHER_APPLICATION_NAME);
		cLIOptionDefinitions.add(ICLIOptionsArcher.CLI_ARCHER_INSTANCE_NAME);
		cLIOptionDefinitions.add(ICLIOptionsArcher.CLI_ARCHER_USER_NAME);
		cLIOptionDefinitions.add(ICLIOptionsArcher.CLI_ARCHER_USER_DOMAIN);
		cLIOptionDefinitions.add(ICLIOptionsArcher.CLI_ARCHER_PASSWORD);
		CLIOptionAwareProxyConfiguration.addCLIOptionDefinitions(cLIOptionDefinitions, context, "Archer");
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
		ProxyConfig proxy = CLIOptionAwareProxyConfiguration.getProxyConfiguration(context, "Archer");
		return ArcherAuthenticatingRestConnection.builder()
			.proxy(proxy)
			.baseUrl(ICLIOptionsArcher.CLI_ARCHER_BASE_URL.getValue(context))
			.applicationName(ICLIOptionsArcher.CLI_ARCHER_APPLICATION_NAME.getValue(context))
			.instanceName(ICLIOptionsArcher.CLI_ARCHER_INSTANCE_NAME.getValue(context))
			.userName(ICLIOptionsArcher.CLI_ARCHER_USER_NAME.getValue(context))
			.userDomain(ICLIOptionsArcher.CLI_ARCHER_USER_DOMAIN.getValue(context))
			.password(ICLIOptionsArcher.CLI_ARCHER_PASSWORD.getValue(context))
			.build();
	}
	
	private interface IContextArcherConnection {
		public void setArcherConnection(ArcherAuthenticatingRestConnection connection);
		public ArcherAuthenticatingRestConnection getArcherConnection();
	}
}
