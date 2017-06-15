package com.fortify.util.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

public interface IRestConnection {

	public abstract <T> T executeRequest(String httpMethod, Builder builder, Entity<?> entity, Class<T> returnType);
	public abstract <T> T executeRequest(String httpMethod, WebTarget webResource, Class<T> returnType);
	public abstract <T> T executeRequest(String httpMethod, WebTarget webResource, Entity<?> entity, Class<T> returnType);
	public abstract String getBaseUrl();
	public abstract WebTarget getBaseResource();
	public abstract WebTarget getResource(String url);
	public abstract Client getClient();
	public abstract void setProxy(ProxyConfiguration proxy);
}