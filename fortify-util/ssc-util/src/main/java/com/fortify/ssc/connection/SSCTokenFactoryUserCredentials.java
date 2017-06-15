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