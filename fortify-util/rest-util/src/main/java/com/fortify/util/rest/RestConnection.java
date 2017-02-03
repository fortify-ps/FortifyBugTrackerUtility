package com.fortify.util.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;

/**
 * Utility for working with a REST API. Instances of this class can be configured
 * with a base URL, optional proxy configuration, and optional credentials for 
 * authenticating with the remote system. Subclasses can override various methods
 * to customize the behavior of the connection or to implement more advanced 
 * authentication mechanisms. 
 */
public class RestConnection implements IRestConnection {
	//private static final Log LOG = LogFactory.getLog(RestConnection.class);
	private static final Set<String> DEFAULT_HTTP_METHODS_TO_PRE_AUTHENTICATE = new HashSet<String>(Arrays.asList("POST","PUT","PATCH"));
	private final CredentialsProvider credentialsProvider = createCredentialsProvider();
	private final String baseUrl;
	private Client client;
	private ProxyConfiguration proxy;

	/**
	 * This constructor is used to specify the base URL for this connection.
	 *
	 * @param baseUrl
	 */
	public RestConnection(String baseUrl) 
	{
		this.baseUrl = validateAndNormalizeUrl(baseUrl);
	}
	
	/**
	 * This constructor is used to specify the base URL and credentials for the remote system.
	 *
	 * @param baseUrl
	 */
	public RestConnection(String baseUrl, Credentials credentials) {
		this(baseUrl);
		getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
	}
	
	
	
	public String getBaseUrl() {
		return baseUrl;
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

	/**
	 * Validate and normalize the given URL. This will check whether the protocol
	 * is either HTTP or HTTPS, and it will add a trailing slash if necessary.
	 * @param baseUrl
	 * @return The validated and normalized URL
	 */
	private static final String validateAndNormalizeUrl(String baseUrl) {
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl+"/";
		}
		if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
			throw new RuntimeException("URL protocol should be either http or https");
		}
		return baseUrl;
	}
	
	/**
	 * Execute a request for the given method using the given web resource.
	 * @param httpMethod The HTTP method to be used, as specified by one of the constants
	 *                   in {@link HttpMethod}
	 * @param webResource The web resource used to execute the request. Usually this web resource 
	 * 					  is created using {@link #getBaseResource()}.path(...)...
	 * @param returnType The return type for the data returned by the request.
	 * @return The result of executing the HTTP request.
	 */
	public <T> T executeRequest(String httpMethod, WebResource webResource, Class<T> returnType) {
		return executeRequest(httpMethod, webResource.getRequestBuilder(), returnType);
	}
	
	/**
	 * Execute a request for the given method using the given builder.
	 * @param httpMethod The HTTP method to be used, as specified by one of the constants
	 *                   in {@link HttpMethod}
	 * @param builder	 The builder used to execute the request. Usually this builder is created
	 *                   using {@link #getBaseResource()}.path(...).entity(...).accept(...)...
	 * @param returnType The return type for the data returned by the request.
	 * @return The result of executing the HTTP request.
	 */
	public <T> T executeRequest(String httpMethod, Builder builder, Class<T> returnType) {
		ClientResponse response = null;
		try {
			initializeConnection(httpMethod);
			builder = updateBuilder(builder);
			response = builder.method(httpMethod, ClientResponse.class);
			return checkResponseAndGetOutput(httpMethod, builder, response, returnType);
		} catch ( ClientHandlerException e ) {
			throw new RuntimeException("Error connecting to bug tracker:\n"+e.getMessage(), e);
		} finally {
			if ( response != null ) { response.close(); }
		}
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
		return !doPreemptiveBasicAuthentication(); // TODO Add check whether credentials are available
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
	
	/**
	 * Check the response code. If successful, return the entity with the given return type,
	 * otherwise throw an exception.
	 * @param response
	 * @param returnType
	 * @return The entity from the given {@link ClientResponse} if available
	 */
	protected <T> T checkResponseAndGetOutput(String httpMethod, Builder builder, ClientResponse response, Class<T> returnType) {
		StatusType status = response.getStatusInfo();
		if ( status != null && status.getFamily() == Family.SUCCESSFUL ) {
			return getSuccessfulResponse(response, returnType, status);
		} else {
			throw getUnsuccesfulResponseException(response);
		}
	}

	/**
	 * Get the return value for a successful response.
	 * @param response
	 * @param returnType
	 * @param status
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getSuccessfulResponse(ClientResponse response, Class<T> returnType, StatusType status) {
		if ( returnType == null || status.getStatusCode() == Status.NO_CONTENT.getStatusCode() ) {
			return null;
		} else if ( returnType.isAssignableFrom(response.getClass()) ) {
			return (T)response;
		} else {
			return response.getEntity(returnType);
		}
	}
	
	protected RuntimeException getUnsuccesfulResponseException(ClientResponse response) {
		String reasonPhrase = getReasonPhrase(response);
		String msg = "Error accessing remote system "+getBaseUrl()+": "+reasonPhrase;
		String longMsg = msg+", response contents: \n"+response.getEntity(String.class);
		// By adding a new exception as the cause, we make sure that the response
		// contents will be logged whenever this RuntimeException is logged.
		RuntimeException re = new RuntimeException(msg, new Exception(longMsg));
		return re;
	}

	/** 
	 * Get the reason phrase from the response or status info.
	 * Jersey uses hard-coded reason phrases, so we try to read the 
	 * reason phrase(s) directly from the headers first.
	 * @param response to get the reason phrase from
	 * @return Reason phrase from the response
	 */
	private String getReasonPhrase(ClientResponse response) {
		List<String> reasonPhrases = response.getHeaders().get("Reason-Phrase");
		StatusType status = response.getStatusInfo();
		String reasonPhrase;
		if ( reasonPhrases!=null&&reasonPhrases.size()>0 ) {
			reasonPhrase = reasonPhrases.toString();
		} else if ( status != null ) {
			reasonPhrase = status.getReasonPhrase();
		} else {
			reasonPhrase = response.toString();
		}
		return reasonPhrase;
	}
	
	/**
	 * Get a {@link WebResource} object for the configured REST base URL.
	 * @return A {@link WebResource} instance for the configured REST base URL.
	 */
	public final WebResource getBaseResource() {
		return getResource(baseUrl);
	}
	
	/**
	 * Get a {@link WebResource} object for the given URL. Usually one should
	 * call {@link #getBaseResource()} instead, to use the configured REST base
	 * URL. The {@link #getResource(String)} method should usually only be used 
	 * for executing requests on full URL's returned by previous REST requests.
	 * 
	 * Subclasses can override this method to add additional information
	 * to the {@link WebResource}, for example request parameters that
	 * need to be sent on every request.
	 * 
	 * @param url for which to get a {@link WebResource} instance.
	 * @return A {@link WebResource} instance for the given URL.
	 */
	public WebResource getResource(String url) {
		return getClient().resource(url);
	}

	/**
	 * Get the cached client for executing requests. If the client
	 * has not been previously cached, this method will call 
	 * {@link #createClient()} to create a new client and then
	 * cache it.
	 * @return Cache {@link Client} instance if available, new {@link Client} instance otherwise
	 */
	public final Client getClient() {
		if ( client == null ) {
			client = createClient();
		}
		return client;
	}

	/**
	 * <p>Create a new client for executing requests. The default
	 * implementation creates a client based on Apache HttpClient
	 * 4.x by calling {@link #createApacheHttpClientBuilder()}.
	 * Some of the Jersey-specific settings like the CookieStore
	 * and whether to use preemptive basic authentication can
	 * be specified by overriding the corresponding methods.</p> 
	 * 
	 * <p>Subclasses can override this method to create a different 
	 * type of client. Note however that other classes may
	 * depend on the use of Apache HttpClient, for example 
	 * {@link AuthenticatingRestConnection}.</p>
	 * 
	 * <p>Subclasses that would would like to customize Apache
	 * HttpClient behavior should override {@link #createApacheHttpClientBuilder()}.</p>
	 * 
	 * @return New {@link Client} instance
	 */
	protected Client createClient() {
		HttpClient apacheClient = createApacheHttpClientBuilder().build();
		Client client = new Client(new ApacheHttpClient4Handler(apacheClient, createCookieStore(), doPreemptiveBasicAuthentication()));
		// TODO Allow subclasses to override this and other settings
		client.getProperties().put(ApacheHttpClient4Config.PROPERTY_ENABLE_BUFFERING, true);
		return client;
	}
	
	/**
	 * Subclasses can override this method to customize the Apache
	 * HttpClient configuration. Usually subclasses would call
	 * super.createApacheHttpClientBuilder() to re-use the default
	 * implementation and add additional configuration on top of that.
	 * @return
	 */
	protected HttpClientBuilder createApacheHttpClientBuilder() {
		// TODO Disable chunked encoding
		// TODO Set auth header charset
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultCredentialsProvider(getCredentialsProvider());
		// Add proxy host
		if ( proxy != null && proxy.getUri() != null ) {
			URI u = proxy.getUri();
			builder.setProxy(new HttpHost(u.getHost(), u.getPort(), u.getScheme()));
			
			// Add proxy credentials
			if ( StringUtils.isNotBlank(proxy.getUserName()) && StringUtils.isNotBlank( proxy.getPassword()) ) {
				getCredentialsProvider().setCredentials(new AuthScope(u.getHost(), u.getPort()),
			            new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword()));
			}
		}
		return builder;
	}
	
	/**
	 * Create the {@link CookieStore} to use between requests.
	 * This default implementation returns a {@link BasicCookieStore}
	 * instance. Subclasses can override this method to return
	 * an alternative {@link CookieStore} implementation, or
	 * null if cookies should not be maintained between requests.
	 * @return
	 */
	protected CookieStore createCookieStore() {
		return new BasicCookieStore();
	}
	
	/**
	 * Indicate whether preemptive basic authentication should be used.
	 * If this method returns true, the Basic Authentication header will
	 * be sent on all requests, preventing multiple round trips for
	 * performing authentication. This default implementation returns 
	 * 
	 * @return Flag specifying whether preemptive Basic Authentication should be performed
	 */
	protected boolean doPreemptiveBasicAuthentication() {
		return false;
	}
	
	/**
	 * Create the {@link CredentialsProvider} to use for requests.
	 * This default implementation returns a {@link BasicCredentialsProvider}
	 * instance.
	 * @return
	 */
	protected BasicCredentialsProvider createCredentialsProvider() {
		return new BasicCredentialsProvider();
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
	 * Update the given {@link Builder} before executing the request.
	 * By default this method simply returns the given builder. Subclasses
	 * can override this method to add information to the builder that
	 * is required for every request, for example to add headers that 
	 * need to be included in every request.
	 * @param builder to be updated
	 * @return updated builder
	 */
	protected Builder updateBuilder(Builder builder) {
		return builder;
	}
	
	/**
	 * URL-encode the given input String using UTF-8 encoding.
	 * @param input
	 * @return The URL-encoded input string
	 */
	public static final String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, CharEncoding.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to encode value "+input, e);
		}
	}

	/**
	 * Get the proxy configuration to use for the connection.
	 * @return The proxy configuration
	 */
	public ProxyConfiguration getProxy() {
		return proxy;
	}
	
	/**
	 * Set the proxy configuration to use for the connection.
	 * @param proxyConfiguration The proxy configuration to use for the connection.
	 */
	public void setProxy(ProxyConfiguration proxy) {
		this.proxy = proxy;
	}
	
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}

}
