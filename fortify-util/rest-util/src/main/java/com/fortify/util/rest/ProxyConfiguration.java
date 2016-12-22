package com.fortify.util.rest;

import java.net.URI;

/**
 * This class defines a proxy configuration.
 */
public class ProxyConfiguration {
	private URI uri;
	private String userName;
	private String password;
	
	public URI getUri() {
		return uri;
	}
	public void setUri(URI uri) {
		this.uri = uri;
	}
	public String getUriString() {
		return uri==null?null:uri.toString();
	}
	public void setUriString(String uriString) {
		this.uri = uriString==null?null:URI.create(uriString);
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
