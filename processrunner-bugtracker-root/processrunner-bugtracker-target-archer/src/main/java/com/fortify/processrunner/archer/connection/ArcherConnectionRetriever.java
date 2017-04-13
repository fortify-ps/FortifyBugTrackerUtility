package com.fortify.processrunner.archer.connection;

import com.fortify.util.rest.AbstractRestConnectionRetriever;
import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * <p>This {@link IRestConnectionRetriever} will create an 
 * authenticated Archer REST connection based on the configured 
 * properties like base URL, proxy configuration and authentication 
 * data.</p>  
 */
public abstract class ArcherConnectionRetriever extends AbstractRestConnectionRetriever<ArcherAuthenticatingRestConnection> implements IArcherConnectionRetriever {
	private String baseUrl;
	private String applicationName;
	private ArcherAuthData auth = new ArcherAuthData();
	
	protected final ArcherAuthenticatingRestConnection createConnection() {
		return new ArcherAuthenticatingRestConnection(getBaseUrl(), getAuth(), getApplicationName(), getProxy());
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @param baseUrl the baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * @return the auth
	 */
	public ArcherAuthData getAuth() {
		return auth;
	}

	/**
	 * @param auth the auth to set
	 */
	public void setAuth(ArcherAuthData auth) {
		this.auth = auth;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	
}