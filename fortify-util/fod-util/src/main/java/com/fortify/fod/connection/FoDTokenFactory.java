package com.fortify.fod.connection;

import java.util.Date;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private final MultivaluedMap<String, String> auth;
	private FoDTokenFactory.TokenData tokenData = null;
	public FoDTokenFactory(String baseUrl, MultivaluedMap<String, String> auth, ProxyConfiguration proxyConfig) {
		conn = new FoDBasicRestConnection(baseUrl, proxyConfig);
		this.auth = auth;
	}
	
	public String getToken() {
		if ( tokenData == null || tokenData.isExpired() ) {
			tokenData = conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/oauth/token").entity(auth, "application/x-www-form-urlencoded"), FoDTokenFactory.TokenData.class);
			LOG.info("Obtained FoD access token, expiring at "+new Date(tokenData.getExpiresAt()).toString());
		}
		return tokenData.getAccessToken();
	}
	
	@XmlRootElement
	private static final class TokenData {
		private String accessToken;
		private long expiresAt;
		public String getAccessToken() {
			return accessToken;
		}
		@XmlElement(name="access_token")
		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
		@XmlElement(name="expires_in")
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