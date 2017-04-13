package com.fortify.ssc.connection;

import org.apache.commons.codec.binary.Base64;

/**
 * This class is used to properly encode an existing SSC authentication token 
 * for accessing the SSC REST API.
 */
public final class SSCTokenFactoryTokenCredentials implements ISSCTokenFactory {
	private final String token;
	
	public SSCTokenFactoryTokenCredentials(String token) {
		this.token = Base64.encodeBase64String(token.getBytes());
	}
	
	/* (non-Javadoc)
	 * @see com.fortify.ssc.connection.ISSCTokenFactory#getToken()
	 */
	public String getToken() {
		return token;
	}
}