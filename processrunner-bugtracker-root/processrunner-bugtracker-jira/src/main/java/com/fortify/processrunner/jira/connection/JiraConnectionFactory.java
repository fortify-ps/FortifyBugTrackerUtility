package com.fortify.processrunner.jira.connection;

import com.fortify.util.rest.AuthenticatingRestConnection;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.rest.IRestConnectionFactory;

public class JiraConnectionFactory implements IRestConnectionFactory {
	private String baseUrl;
	private String userName;
	private String password;
	
	public IRestConnection getConnection() {
		return new AuthenticatingRestConnection(getBaseUrl(), getUserName(), getPassword());
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
