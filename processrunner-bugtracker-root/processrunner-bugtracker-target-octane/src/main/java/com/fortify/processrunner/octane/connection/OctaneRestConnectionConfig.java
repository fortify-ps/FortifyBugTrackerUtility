/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.octane.connection;

import org.apache.commons.lang.StringUtils;

import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.IOctaneCredentials;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneClientCredentials;
import com.fortify.processrunner.octane.connection.OctaneAuthenticatingRestConnection.OctaneUserCredentials;
import com.fortify.util.rest.connection.AbstractRestConnectionWithUsernamePasswordConfig;

public class OctaneRestConnectionConfig<T extends OctaneRestConnectionConfig<T>> extends AbstractRestConnectionWithUsernamePasswordConfig<T> {
	private String clientId;
	private String clientSecret;
	
	public T clientId(String clientId) {
		setClientId(clientId); return getThis();
	}
	
	public T clientSecret(String clientSecret) {
		setClientSecret(clientSecret); return getThis();
	}
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	public IOctaneCredentials getCredentials() {
		if ( StringUtils.isNotBlank(getUserName()) ) {
			return new OctaneUserCredentials(getUserName(), getPassword());
		} else if (StringUtils.isNotBlank(getClientId()) ) {
			return new OctaneClientCredentials(getClientId(), getClientSecret());
		} else {
			throw new IllegalStateException("Either Octane user name and password, or client id and secret must be specified");
		}
	}
	
	
}
