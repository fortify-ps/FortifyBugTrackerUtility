package com.fortify.fod.connection;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.util.rest.ProxyConfiguration;

/**
 * This class is used to generate FoD tokens for accessing the
 * FoD REST API. Given a base URL, authentication form data and
 * optional proxy configuration, it will call the FoD /oauth/token
 * API to request a REST token. The token will be automatically
 * refreshed as required.
 */
public final class FoDTokenFactory {
	static final Log LOG = LogFactory.getLog(FoDTokenFactory.class);
	private final FoDBasicRestConnection conn;
	private final Form auth;
	private FoDTokenFactory.TokenData tokenData = null;
	public FoDTokenFactory(String baseUrl, Form auth, ProxyConfiguration proxyConfig) {
		conn = new FoDBasicRestConnection(baseUrl, proxyConfig);
		this.auth = auth;
	}
	
	public String getToken() {
		if ( tokenData == null || tokenData.isExpired() ) {
			//Map test = conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/oauth/token"), Entity.entity(auth, "application/x-www-form-urlencoded"), Map.class);
			tokenData = conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/oauth/token"), Entity.entity(auth, "application/x-www-form-urlencoded"), FoDTokenFactory.TokenData.class);
			LOG.info("[FoD] Obtained access token, expiring at "+new Date(tokenData.getExpiresAt()).toString());
		}
		return tokenData.getAccessToken();
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static final class TokenData {
		private String accessToken;
		private long expiresAt;
		public String getAccessToken() {
			return accessToken;
		}
		@JsonProperty("access_token")
		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
		@JsonProperty("expires_in")
		public void setExpiresIn(long expiresIn) {
			this.expiresAt = new Date().getTime()+((expiresIn-5)*1000);
		}
		public long getExpiresAt() {
			return expiresAt;
		}
		public boolean isExpired() {
			return new Date().getTime() > expiresAt;
		}
	}
}