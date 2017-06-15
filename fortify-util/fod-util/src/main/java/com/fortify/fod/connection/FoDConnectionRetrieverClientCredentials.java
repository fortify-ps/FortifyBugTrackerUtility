package com.fortify.fod.connection;

import javax.ws.rs.core.Form;

/**
 * <p>This {@link AbstractFoDConnectionRetriever} implementation
 * allows for configuring client credentials used to connect to FoD.</p>
 */
public class FoDConnectionRetrieverClientCredentials extends AbstractFoDConnectionRetriever {
	private String clientId;
	private String clientSecret;
	
	public FoDConnectionRetrieverClientCredentials() {
		setGrantType("client_credentials");
	}
	
	@Override
	public void addCredentials(Form form) {
		form.param("client_id", getClientId());
		form.param("client_secret", getClientSecret());
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
