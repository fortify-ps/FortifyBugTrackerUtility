package com.fortify.fod.connection;

import com.fortify.util.rest.AbstractRestConnectionRetriever;
import com.fortify.util.rest.IRestConnectionRetriever;
import com.sun.jersey.api.representation.Form;

/**
 * <p>This abstract {@link IRestConnectionRetriever} will create 
 * an authenticated FoD REST connection based on the configured 
 * properties like base URL, proxy configuration and authentication 
 * data.</p>
 * 
 * <p>Subclasses will need to provide the actual authentication
 * data.</p>  
 */
public abstract class AbstractFoDConnectionRetriever extends AbstractRestConnectionRetriever<FoDAuthenticatingRestConnection> implements IFoDConnectionRetriever {
	private String baseUrl = "https://hpfod.com/";
	private String scope = "https://hpfod.com/tenant";
	private String grantType;
	
	protected final FoDAuthenticatingRestConnection createConnection() {
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
}