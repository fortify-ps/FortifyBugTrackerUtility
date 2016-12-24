package com.fortify.processrunner.archer.connection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class holds authentication data for Archer.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ArcherAuthData {
	@XmlElement(name="InstanceName")
	private String instanceName = "v5.0";
	
	@XmlElement(name="UserName")
	private String userName;
	
	@XmlElement(name="UserDomain")
	private String userDomain;
	
	@XmlElement(name="Password")
	private String password;

	/**
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * @param instanceName the instanceName to set
	 */
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
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
	 * @return the userDomain
	 */
	public String getUserDomain() {
		return userDomain;
	}

	/**
	 * @param userDomain the userDomain to set
	 */
	public void setUserDomain(String userDomain) {
		this.userDomain = userDomain;
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
