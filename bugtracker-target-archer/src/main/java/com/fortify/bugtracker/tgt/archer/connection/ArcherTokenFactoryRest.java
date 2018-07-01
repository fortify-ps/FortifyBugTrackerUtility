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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class is used to generate Archer tokens for accessing the
 * Archer REST API. Given a base URL, authentication data and
 * optional proxy configuration, it will call the Archer
 * /api/core/security/login API to request a REST token.
 */
public final class ArcherTokenFactoryRest {
	static final Log LOG = LogFactory.getLog(ArcherTokenFactoryRest.class);
	private final ArcherBasicRestConnection conn;
	private final ArcherAuthData authData;
	private TokenData tokenData = null;
	public ArcherTokenFactoryRest(ArcherRestConnectionConfig<?> config) {
		conn = new ArcherBasicRestConnection(config);
		this.authData = new ArcherAuthData(config);
	}
	
	public String getToken() {
		if ( tokenData == null ) {
			JSONMap json = conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/api/core/security/login"), Entity.entity(authData, "application/json"), JSONMap.class);
			tokenData = getTokenData(json);
			LOG.info("[Archer] Obtained Archer access token");
		}
		return tokenData.getSessionToken();
	}
	
	private TokenData getTokenData(JSONMap json) {
		return new TokenData(SpringExpressionUtil.evaluateExpression(json, "RequestedObject.SessionToken", String.class));
	}

	private static final class TokenData {
		private final String sessionToken;
		public TokenData(String sessionToken) {
			this.sessionToken = sessionToken;
		}
		public String getSessionToken() {
			return sessionToken;
		}
	}
}
