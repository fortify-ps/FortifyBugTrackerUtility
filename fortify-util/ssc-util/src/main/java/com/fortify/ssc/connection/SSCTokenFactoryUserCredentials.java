/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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
package com.fortify.ssc.connection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.HttpMethod;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.ProxyConfiguration;

/**
 * This class is used to generate SSC tokens for accessing the
 * SSC REST API. Given a base URL, userName, password and
 * optional proxy configuration, it will call the SSC /oauth/token
 * API to request a REST token. The token will be automatically
 * refreshed as required.
 * 
 * @author Ruud Senden
 *
 */
public final class SSCTokenFactoryUserCredentials implements ISSCTokenFactory {
	static final Log LOG = LogFactory.getLog(SSCTokenFactoryUserCredentials.class);
	private final SSCBasicRestConnection conn;
	private final String userName;
	private final String password;
	private SSCTokenFactoryUserCredentials.TokenData tokenData = null;
	public SSCTokenFactoryUserCredentials(String baseUrl, String userName, String password, ProxyConfiguration proxyConfig) {
		conn = new SSCBasicRestConnection(baseUrl, proxyConfig);
		this.userName = userName;
		this.password = password;
	}
	
	public String getToken() {
		if ( tokenData == null || tokenData.isExpired() ) {
			String authHeaderValue = "Basic "+Base64.encodeBase64String((userName+":"+password).getBytes());
			tokenData = getTokenData(conn.executeRequest(HttpMethod.POST, conn.getBaseResource().path("/api/v1/auth/obtain_token").request().header("Authorization", authHeaderValue), null, JSONMap.class));
			LOG.info("[SSC] Obtained access token, expiring at "+tokenData.getTerminalDate().toString());
		}
		return tokenData.getToken();
	}
	
	private TokenData getTokenData(JSONMap json) {
		JSONMap data = (JSONMap)json.get("data");
		return new TokenData((String)data.get("token"), (String)data.get("terminalDate"));
	}

	private static final class TokenData {
		private static final SimpleDateFormat PARSER=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		private final String token;
        private final Date terminalDate;
        public TokenData(String token, String terminalDate) {
			this.token = token;
			try {
				this.terminalDate = PARSER.parse(terminalDate);
			} catch (ParseException e) {
				throw new RuntimeException("Error parsing SSC token terminal date", e);
			}
		}
		public String getToken() {
			return token;
		}
		public boolean isExpired() {
			return new Date().getTime() > getTerminalDate().getTime();
		}
		public Date getTerminalDate() {
			return terminalDate;
		}
	}
}
