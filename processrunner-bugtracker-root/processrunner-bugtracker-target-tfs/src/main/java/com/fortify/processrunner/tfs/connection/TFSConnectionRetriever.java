package com.fortify.processrunner.tfs.connection;

import org.apache.http.auth.UsernamePasswordCredentials;

import com.fortify.util.rest.AbstractRestConnectionRetriever;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.rest.IRestConnectionRetriever;

/**
 * This {@link IRestConnectionRetriever} implementation is used to 
 * retrieve {@link IRestConnection} instances for TFS based on
 * configured connection properties like TFS base URL and credentials. 
 */
public class TFSConnectionRetriever extends AbstractRestConnectionRetriever<TFSRestConnection> implements ITFSConnectionRetriever {
	private String baseUrl;
	private String userName;
	private String password;
	
	public final TFSRestConnection createConnection() {
		return new TFSRestConnection(getBaseUrl(), new UsernamePasswordCredentials(getUserName(), getPassword()));
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
