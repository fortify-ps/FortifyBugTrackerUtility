package com.fortify.util.rest;

/**
 * This class defines a proxy configuration.
 */
public class ProxyConfiguration {
	private String uri;
	private String userName;
	private String password;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
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
