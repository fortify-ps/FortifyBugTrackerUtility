package com.fortify.util.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 * Utility for working with a REST API. Note that this class doesn't
 * do any authentication; this will need to be implemented in subclasses.
 */
public class RestConnection implements IRestConnection {
	private final String baseUrl;
	private Client client;
	private ProxyConfiguration proxy;

	/**
	 * This constructor determines the various connection properties used
	 * to connect to the bug tracker. It doesn't actually connect to the
	 * bug tracker yet. 
	 *
	 * @param bugTrackerUrl
	 */
	public RestConnection(String baseUrl) 
	{
		this.baseUrl = validateAndNormalizeUrl(baseUrl);
	}
	
	public String getBaseUrl() {
		return baseUrl;
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
	 * Execute a request for the given method using the given builder.
	 * @param httpMethod The HTTP method to be used, as specified by one of the constants
	 *                   in {@link HttpMethod}
	 * @param builder	 The builder used to execute the request. Usually this builder is created
	 *                   using {@link #getBaseResource()}.path(...).entity(...).accept(...)...
	 * @param returnType The return type for the data returned by the request.
	 * @return The result of executing the HTTP request.
	 */
	public <T> T executeRequest(String httpMethod, Builder builder, Class<T> returnType) {
		try {
			initializeConnection(httpMethod);
			builder = updateBuilder(builder);
			ClientResponse response = builder.method(httpMethod, ClientResponse.class);
			return checkResponseAndGetOutput(response, returnType);
		} catch ( ClientHandlerException e ) {
			throw new RuntimeException("Error connecting to resource:\n"+e.getMessage(), e);
		}
	}

	/**
	 * Subclasses can override this method to initialize the connection if necessary,
	 * before sending the actual request. This can for example be used to perform 
	 * authentication on a cheap resource before sending a (possibly large) payload. 
	 * The default implementation does nothing.
	 * @param httpMethod
	 */
	protected void initializeConnection(String httpMethod) {
		// Default implementation does nothing
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
	 * Check the response code. If successful, return the entity with the given return type,
	 * otherwise throw an exception.
	 * @param response
	 * @param returnType
	 * @return The entity from the given {@link ClientResponse} if available
	 */
	@SuppressWarnings("unchecked")
	protected <T> T checkResponseAndGetOutput(ClientResponse response, Class<T> returnType) {
		StatusType status = response.getStatusInfo();
		if ( status != null && status.getFamily() == Family.SUCCESSFUL ) {
			if ( returnType != null ) {
				if ( returnType.isAssignableFrom(response.getClass()) ) {
					return (T)response;
				} else if ( status.getStatusCode() != Status.NO_CONTENT.getStatusCode() ) {
					return response.getEntity(returnType);
				}
			}
			return null;
		} else {
			Integer reasonCode = status.getStatusCode();
			String reasonPhrase = getReasonPhrase(response);
			String className = this.getClass().toString();
			if (reasonCode.equals(401) || reasonCode.equals(400)) //FOD gives a 400 error for bad user credentials and a 401 for bad token/secret. Interim fix is to assume our auth request is well-formed and is simply bad creds.
			{
				if (className.contains("fod"))
				{
					throw new RuntimeException("Authorization failure for FoD credentials.");
				}
				if (className.contains("jira"))
				{
					throw new RuntimeException("Authorization failure for JIRA credentials.");
				}
				throw new RuntimeException("Authorization failure for integration connection." +response.getEntity(String.class));
			} else
			{
				throw new RuntimeException(reasonPhrase);
			}
		}
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
	 * Create a new client for executing requests. The default
	 * implementation calls {@link #createApacheHttpClient4()}
	 * to create an {@link ApacheHttpClient4} instance. 
	 * 
	 * Subclasses can override this method to create a different 
	 * type of client. Note however that other classes may
	 * depend on {@link ApacheHttpClient4}, for example 
	 * {@link AuthenticatingRestConnection}.
	 * 
	 * Subclasses that would would like to customize
	 * the created {@link ApacheHttpClient4} should override
	 * {@link #createApacheHttpClient4()} instead.
	 * @return New {@link Client} instance
	 */
	protected Client createClient() {
		return createApacheHttpClient4();
	}
	
	/**
	 * Create a new {@link ApacheHttpClient4} instance based on the 
	 * {@link ClientConfig} returned by {@link #getApacheHttpClient4Config()}.
	 * Subclasses may override this method to perform additional
	 * configuration on the client or use some different way
	 * for instantiating the client.
	 * @return New {@link ApacheHttpClient4} instance
	 */
	protected ApacheHttpClient4 createApacheHttpClient4() {
		return ApacheHttpClient4.create(getApacheHttpClient4Config());
	}
	
	/**
	 * Return the {@link ApacheHttpClient4Config} used for creating the 
	 * {@link ApacheHttpClient4}. By default this returns a standard
	 * instance of {@link DefaultApacheHttpClient4Config} with optional
	 * proxy support. Subclasses can override this method to update the 
	 * configuration.
	 * @return {@link ApacheHttpClient4Config} used to configure {@link ApacheHttpClient4}
	 */
	protected ApacheHttpClient4Config getApacheHttpClient4Config() {
		DefaultApacheHttpClient4Config cc = new DefaultApacheHttpClient4Config();
		if ( proxy != null && StringUtils.isNotBlank(proxy.getUri()) ) {
			cc.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_URI, proxy.getUri());
			if ( StringUtils.isNotBlank(proxy.getUserName()) && StringUtils.isNotBlank( proxy.getPassword()) ) {
				cc.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME, proxy.getUserName());
				cc.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD, proxy.getPassword());
			}
		}
		return cc;
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
