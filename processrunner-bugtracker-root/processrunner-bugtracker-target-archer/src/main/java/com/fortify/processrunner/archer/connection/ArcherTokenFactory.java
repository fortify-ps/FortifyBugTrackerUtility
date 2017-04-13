package com.fortify.processrunner.archer.connection;

import javax.ws.rs.HttpMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.rest.ProxyConfiguration;

/**
 * This class is used to generate Archer tokens for accessing the
 * Archer REST API. Given a base URL, authentication data and
 * optional proxy configuration, it will call the Archer
 * /api/core/security/login API to request a REST token.
 */
public final class ArcherTokenFactory {
	static final Log LOG = LogFactory.getLog(ArcherTokenFactory.class);
	private final ArcherBasicRestConnection conn;
	private final ArcherAuthData authData;
	private ArcherTokenFactory.TokenData tokenData = null;
	public ArcherTokenFactory(String baseUrl, ArcherAuthData authData, ProxyConfiguration proxyConfig) {
		conn = new ArcherBasicRestConnection(baseUrl, proxyConfig);
		this.authData = authData;
	}
	
	public String getToken() {
		if ( tokenData == null ) {
			JSONObject json = conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/api/core/security/login").entity(authData, "application/json"), JSONObject.class);
			tokenData = getTokenData(json);
			LOG.info("[Archer] Obtained Archer access token");
		}
		return tokenData.getSessionToken();
	}
	
	private TokenData getTokenData(JSONObject json) {
		return new TokenData(json.optJSONObject("RequestedObject").optString("SessionToken"));
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