package com.fortify.ssc.connection;

/**
 * <p>This {@link AbstractSSCConnectionRetriever} implementation
 * allows for configuring user name and password used to connect to SSC.</p> 
 * 
 * @author Ruud Senden
 *
 */
public class SSCConnectionRetrieverUserCredentials extends AbstractSSCConnectionRetriever {
	private String userName;
	private String password;
	
	protected final SSCAuthenticatingRestConnection createConnection() {
		return new SSCAuthenticatingRestConnection(getBaseUrl(), getUserName(), getPassword(), getProxy());
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
