package com.fortify.util.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public interface IRestConnection {

	public abstract <T> T executeRequest(String httpMethod, Builder builder, Class<T> returnType);
	public abstract <T> T executeRequest(String httpMethod, WebResource webResource, Class<T> returnType);
	public abstract String getBaseUrl();
	public abstract WebResource getBaseResource();
	public abstract WebResource getResource(String url);
	public abstract Client getClient();
}