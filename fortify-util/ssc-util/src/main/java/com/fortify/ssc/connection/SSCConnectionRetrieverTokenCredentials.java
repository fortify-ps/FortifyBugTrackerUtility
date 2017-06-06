package com.fortify.ssc.connection;

/**
 * <p>This {@link AbstractSSCConnectionRetriever} implementation
 * allows for configuring token credentials used to connect to SSC.</p>
 * 
 * @author Ruud Senden
 *
 */
public class SSCConnectionRetrieverTokenCredentials extends AbstractSSCConnectionRetriever {
	private String authToken;
	
	protected final SSCAuthenticatingRestConnection createConnection() {
		return new SSCAuthenticatingRestConnection(getBaseUrl(), getAuthToken(), getProxy());
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	
	
}
