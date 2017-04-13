package com.fortify.ssc.connection;

import com.fortify.util.rest.AbstractRestConnectionRetriever;
import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * <p>This abstract {@link IRestConnectionRetriever} will create 
 * an authenticated SSC REST connection based on the configured 
 * properties like base URL, proxy configuration and authentication 
 * data.</p>
 * 
 * <p>Subclasses will need to provide the actual authentication
 * data.</p>  
 */
public abstract class AbstractSSCConnectionRetriever extends AbstractRestConnectionRetriever<SSCAuthenticatingRestConnection> implements ISSCConnectionRetriever {
	private String baseUrl = "https://localhost:8080/ssc";
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}