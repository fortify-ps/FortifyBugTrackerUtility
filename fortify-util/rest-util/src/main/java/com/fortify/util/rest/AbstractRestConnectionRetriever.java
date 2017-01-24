package com.fortify.util.rest;

public abstract class AbstractRestConnectionRetriever<C extends IRestConnection> implements IRestConnectionRetriever<C> {
	private C connection;
	private ProxyConfiguration proxy;
	
	public final C getConnection() {
		if ( connection == null ) {
			connection = createConnection();
			connection.setProxy(getProxy());
		}
		return connection;	}

	protected abstract C createConnection();

	public ProxyConfiguration getProxy() {
		return proxy;
	}

	public void setProxy(ProxyConfiguration proxy) {
		this.proxy = proxy;
	}
}
