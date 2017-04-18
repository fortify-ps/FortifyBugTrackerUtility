package com.fortify.processrunner.archer.connection;

import java.util.LinkedHashMap;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.rest.ProxyConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class ArcherConnection implements IArcherConnection {

	public <T> T executeRequest(String httpMethod, Builder builder, Class<T> returnType) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T executeRequest(String httpMethod, WebResource webResource, Class<T> returnType) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBaseUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public WebResource getBaseResource() {
		// TODO Auto-generated method stub
		return null;
	}

	public WebResource getResource(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public Client getClient() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProxy(ProxyConfiguration proxy) {
		// TODO Auto-generated method stub

	}

	public Long addValueToValuesList(Long valueListId, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	public SubmittedIssue submitIssue(LinkedHashMap<String, Object> issueData) {
		// TODO Auto-generated method stub
		return null;
	}

}
