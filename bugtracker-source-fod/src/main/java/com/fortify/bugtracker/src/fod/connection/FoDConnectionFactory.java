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
package com.fortify.bugtracker.src.fod.connection;

import com.fortify.bugtracker.src.fod.cli.ICLIOptionsFoD;
import com.fortify.client.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.rest.CLIOptionAwareProxyConfiguration;
import com.fortify.util.rest.connection.ProxyConfig;

/**
 * This class allows for instantiating and caching {@link FoDAuthenticatingRestConnection}
 * instances based on {@link Context} properties.
 * 
 * @author Ruud Senden
 *
 */
public final class FoDConnectionFactory 
{
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cLIOptionDefinitions, Context context) {
		cLIOptionDefinitions.add(ICLIOptionsFoD.CLI_FOD_BASE_URL);
		cLIOptionDefinitions.add(ICLIOptionsFoD.CLI_FOD_TENANT);
		cLIOptionDefinitions.add(ICLIOptionsFoD.CLI_FOD_USER_NAME);
		cLIOptionDefinitions.add(ICLIOptionsFoD.CLI_FOD_PASSWORD);
		cLIOptionDefinitions.add(ICLIOptionsFoD.CLI_FOD_CLIENT_ID);
		cLIOptionDefinitions.add(ICLIOptionsFoD.CLI_FOD_CLIENT_SECRET);
		CLIOptionAwareProxyConfiguration.addCLIOptionDefinitions(cLIOptionDefinitions, context, "FoD");
	}
	
	public static final FoDAuthenticatingRestConnection getConnection(Context context) {
		IContextFoDConnection ctx = context.as(IContextFoDConnection.class);
		FoDAuthenticatingRestConnection result = ctx.getFoDConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setFoDConnection(result);
		}
		return result;
	}

	private static final FoDAuthenticatingRestConnection createConnection(Context context) {
		ProxyConfig proxy = CLIOptionAwareProxyConfiguration.getProxyConfiguration(context, "FoD");
		return FoDAuthenticatingRestConnection.builder()
			.proxy(proxy)
			.enableSerializationSingleJVM()
			.baseUrl(ICLIOptionsFoD.CLI_FOD_BASE_URL.getValue(context))
			.clientId(ICLIOptionsFoD.CLI_FOD_CLIENT_ID.getValue(context))
			.clientSecret(ICLIOptionsFoD.CLI_FOD_CLIENT_SECRET.getValue(context))
			.tenant(ICLIOptionsFoD.CLI_FOD_TENANT.getValue(context))
			.userName(ICLIOptionsFoD.CLI_FOD_USER_NAME.getValue(context))
			.password(ICLIOptionsFoD.CLI_FOD_PASSWORD.getValue(context))
			.build();
	}
	
	private interface IContextFoDConnection {
		public void setFoDConnection(FoDAuthenticatingRestConnection connection);
		public FoDAuthenticatingRestConnection getFoDConnection();
	}
}
