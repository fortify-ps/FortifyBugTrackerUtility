package com.fortify.fod.connection;

import javax.ws.rs.core.Form;

/**
 * <p>This {@link AbstractFoDConnectionRetriever} implementation
 * allows for configuring user credentials used to connect to FoD.</p> 
 */
public class FoDConnectionRetrieverUserCredentials extends AbstractFoDConnectionRetriever {
	private String tenant;
	private String userName;
	private String password;
	
	public FoDConnectionRetrieverUserCredentials() {
		setGrantType("password");
	}
	
	@Override
	public void addCredentials(Form form) {
		form.param("username", getUserNameWithTenant());
		form.param("password", getPassword());
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getUserName() {
		return userName;
	}
	
	public String getUserNameWithTenant() {
		return getTenant() + "\\" + getUserName();
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
