package com.fortify.processrunner.archer.connection;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.ProxyConfiguration;
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
	public ArcherTokenFactoryRest(String baseUrl, ArcherAuthData authData, ProxyConfiguration proxyConfig) {
		conn = new ArcherBasicRestConnection(baseUrl, proxyConfig);
		this.authData = authData;
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