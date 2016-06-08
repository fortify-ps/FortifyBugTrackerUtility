package com.fortify.util.rest;

public abstract class AbstractRestConnectionRetriever implements IRestConnectionRetriever {
	private IRestConnection connection;
	private ProxyConfiguration proxy;
	
	public final IRestConnection getConnection() {
		if ( connection == null ) {
			connection = createConnection();
			connection.setProxy(getProxy());
		}
		return connection;	}

	protected abstract IRestConnection createConnection();

	public ProxyConfiguration getProxy() {
		return proxy;
	}

	public void setProxy(ProxyConfiguration proxy) {
		this.proxy = proxy;
	}
}
