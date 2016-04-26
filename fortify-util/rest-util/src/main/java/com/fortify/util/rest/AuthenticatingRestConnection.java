package com.fortify.util.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;

/**
 * Utility for working with a bug tracker REST API. Compared to 
 * {@link RestConnection}, this implementation supports authentication
 * using HTTP Basic Authentication. Subclasses may provide additional
 * authentication methods.
 */
@SuppressWarnings("deprecation")
public class AuthenticatingRestConnection extends RestConnection {
	private static final Set<String> DEFAULT_HTTP_METHODS_TO_PRE_AUTHENTICATE = 
		new HashSet<String>(Arrays.asList("POST","PUT","PATCH"));
	private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	
	public AuthenticatingRestConnection(String bugTrackerUrl) {
		super(bugTrackerUrl);
	}
	
	/**
	 * Since we depend on the default implementation of this method 
	 * in our superclass (returning an {@link ApacheHttpClient4}
	 * instance), we make this method final to prevent subclasses
	 * from returning a different type of client.
	 * @return {@link Client}
	 */
	@Override
	protected final Client createClient() {
		return super.createClient();
	}
	
	/**
	 * This constructor determines the various connection properties used
	 * to connect to the bug tracker. It doesn't actually connect to the
	 * bug tracker yet. 
	 *
	 * @param bugTrackerUrl The URL to use to connect to the bug tracker
	 * @param authStore The credentials used to authenticate with the bug tracker
	 */
	public AuthenticatingRestConnection(String bugTrackerUrl, String userName, String password) {
		this(bugTrackerUrl);
		getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
	}
	
	/**
	 * Get the client configuration. Apart from the default settings from
	 * {@link RestConnection#getApacheHttpClient4Config()}, this implementation will
	 * add the {@link CredentialsProvider} to the configuration, and
	 * specify whether preemptive basic authentication should be attempted
	 * based on the result of the {@link #doPreemptiveBasicAuthentication()}
	 * method.
	 * @return {@link ApacheHttpClient4Config} that adds a {@link CredentialsProvider}
	 *         and enables preemptive Basic Authentication if necessary.
	 */
	@Override
	protected ApacheHttpClient4Config getApacheHttpClient4Config() {
		ApacheHttpClient4Config cc =  super.getApacheHttpClient4Config();
		cc.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION, doPreemptiveBasicAuthentication());
		cc.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, getCredentialsProvider());
		cc.getProperties().put(ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS, getHttpParams());
		return cc;
	}

	/**
	 * Indicate whether preemptive basic authentication should be used.
	 * If this method returns true, the Basic Authentication header will
	 * be sent on all requests, preventing multiple round trips for
	 * performing authentication.
	 * @return Flag specifying whether preemptive Basic Authentication should be performed
	 */
	protected boolean doPreemptiveBasicAuthentication() {
		return true;
	}

	/**
	 * Get the {@link CredentialsProvider} used to execute requests.
	 * Clients can use this method to add credentials to the 
	 * {@link CredentialsProvider}.
	 * @return {@link CredentialsProvider} used to authenticate with the bug tracker
	 */
	protected final CredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}
	
	protected HttpParams getHttpParams() {
		HttpParams result = new BasicHttpParams();
		result.setParameter(AuthPNames.CREDENTIAL_CHARSET, "UTF-8");
		return result;
	}
	
	/**
	 * Subclasses can override this method to return the set of HTTP methods
	 * for which an authenticated connection should be initialized before
	 * executing the actual request. All returned HTTP methods should be
	 * in upper case. By default, this method returns POST, PUT and PATCH.
	 * @return {@link Set} of HTTP Methods that should be pre-authenticated
	 */
	protected Set<String> getHttpMethodsToPreAuthenticate() {
		return DEFAULT_HTTP_METHODS_TO_PRE_AUTHENTICATE;
	}
	
	/**
	 * Authenticating with the server may require several round trips,
	 * especially when using NTLM authentication. For HTTP methods that
	 * may contain a (possibly large) payload, we do not want this 
	 * payload to be included in the authentication requests for the
	 * following reasons:
	 * <ul>
	 *  <li>Re-sending a large payload multiple times can have a negative 
	 *      impact on performance
	 *  <li>If the client sends a non-repeatable entity, any round trip 
	 *      after the initial attempt (which may fail due to authentication
	 *      being required) will trigger an exception
	 * </ul>
	 * 
	 * As such, if the {@link #doInitializeAuthenticatedConnection()} method
	 * returns true, we call {@link #initializeAuthenticatedConnection()} to 
	 * pre-authenticate the connection for the HTTP methods returned by 
	 * {@link #getHttpMethodsToPreAuthenticate()}.
	 * 
	 * @param httpMethod for which to initialize the connection
	 */
	@Override
	protected void initializeConnection(String httpMethod) {
		if ( doInitializeAuthenticatedConnection() && getHttpMethodsToPreAuthenticate().contains(httpMethod.toUpperCase()) ) {
			initializeAuthenticatedConnection();
		}
	}
	
	/**
	 * Indicate whether the {@link #initializeAuthenticatedConnection()} method
	 * should be called for the HTTP methods returned by {@link #getHttpMethodsToPreAuthenticate()}.
	 * By default, this method returns false if preemptive basic authentication
	 * is enabled (assuming the connection will be automatically authenticated
	 * upon the actual request). If preemptive basic authentication is disabled,
	 * this method will return true.
	 * @return Flag indicating whether {@link #initializeAuthenticatedConnection()}
	 *         should be called if necessary
	 */
	protected boolean doInitializeAuthenticatedConnection() {
		return !doPreemptiveBasicAuthentication();
	}

	/**
	 * <p>Subclasses should override this method to set up an authenticated connection.
	 * Implementations would usually execute some cheap request (for example a GET or
	 * HEAD request on a resource that only returns a small payload), resulting
	 * in authentication to be performed if needed.</p>
	 * 
	 * <p>Note that the request to authenticate cannot use any of the HTTP methods
	 * returned by {@link #getHttpMethodsToPreAuthenticate()} as this would result 
	 * in an endless loop.</p>
	 * 
	 * <p>Also note that if a subclass does not override the {@link #doInitializeAuthenticatedConnection()}
	 * and/or {@link #doPreemptiveBasicAuthentication()} methods, this method will
	 * never be called (since preemptive basic authentication is enabled by 
	 * default, {@link #doInitializeAuthenticatedConnection()} will always return
	 * false). As such, we provide a default empty implementation instead of
	 * making this an abstract method.</p>
	 */
	protected void initializeAuthenticatedConnection() {}

}
