package com.fortify.fod.connection;

import com.fortify.util.rest.IRestConnection;
import com.fortify.util.rest.IRestConnectionFactory;
import com.fortify.util.rest.ProxyConfiguration;
import com.sun.jersey.api.representation.Form;

/**
 * <p>This abstract factory class will create an authenticated
 * FoD REST connection based on the configured properties like
 * base URL, proxy configuration and authentication data.</p>
 * 
 * <p>Subclasses will need to provide the actual authentication
 * data.</p>  
 */
public abstract class AbstractFoDConnectionFactory implements IRestConnectionFactory {
	private String baseUrl = "https://hpfod.com/";
	private String scope = "https://hpfod.com/tenant";
	private String grantType;
	private ProxyConfiguration proxy;
	
	public final IRestConnection getConnection() {
		Form form = new Form();
		form.putSingle("scope",getScope());
		form.putSingle("grant_type", getGrantType());
		addCredentials(form);
		return new FoDAuthenticatingRestConnection(getBaseUrl(), form, getProxy());
	}

	protected abstract void addCredentials(Form form);
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public ProxyConfiguration getProxy() {
		return proxy;
	}

	public void setProxy(ProxyConfiguration proxy) {
		this.proxy = proxy;
	}
	
	

}