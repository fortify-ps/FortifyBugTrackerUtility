package com.fortify.processrunner.archer.connection;

import javax.ws.rs.HttpMethod;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			tokenData = conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/api/core/security/login").entity(authData, "application/json"), ArcherTokenFactory.TokenData.class);
			LOG.info("[Archer] Obtained Archer access token");
		}
		return tokenData.getSessionToken();
	}
	
	@XmlRootElement
	private static final class TokenData {
		private String sessionToken;
		public String getSessionToken() {
			return sessionToken;
		}
		@XmlElement(name="SessionToken")
		public void setSessionToken(String sessionToken) {
			this.sessionToken = sessionToken;
		}
	}
}