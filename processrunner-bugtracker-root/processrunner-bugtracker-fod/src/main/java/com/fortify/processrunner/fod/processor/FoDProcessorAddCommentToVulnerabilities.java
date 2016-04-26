package com.fortify.processrunner.fod.processor;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.sun.jersey.api.client.WebResource;

public class FoDProcessorAddCommentToVulnerabilities extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessor.class);
	private String uri = "/api/v2/Releases/${[FoDReleaseId]}/Vulnerabilities/BulkEdit";
	private String rootExpression;
	private String iterableExpression;
	private String vulnIdExpression;
	private String commentTemplate;
	
	@Override
	protected boolean process(Context context) {
		JSONArray vulnIds = getVulnIds(context);
		String comment = SpringExpressionUtil.evaluateTemplateExpression(context, getCommentTemplate(), String.class);
		bulkEditComments(context, vulnIds, comment);
		return true;
	}
	
	private URI processUri(Context context, String unprocessedUriString) {
		String uriString = SpringExpressionUtil.evaluateTemplateExpression(context, unprocessedUriString, String.class);
		try {
			return new URI(uriString);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Root URI "+uriString+" is not a valid URI");
		}
	}

	private void bulkEditComments(Context context, JSONArray vulnIds, String comment) {
		if ( LOG.isDebugEnabled() ) {
			try {
				LOG.debug("Adding comment '"+comment+"' to vulnerability id's "+vulnIds.join(", "));
			} catch (JSONException e) {
				LOG.error("Error joining JSONArray", e);
			}
		}
		IContextFoD contextFod = context.as(IContextFoD.class);
		IRestConnection conn = contextFod.getFoDConnection();
		
		WebResource resource = conn.getBaseResource().uri(processUri(context, uri));
		LOG.debug("Add bulk comments using "+resource);
		JSONObject data = getBulkEditCommentsJSONObject(vulnIds, comment);
		conn.executeRequest(HttpMethod.POST, resource.entity(data,MediaType.APPLICATION_JSON), JSONObject.class);
	}

	private JSONObject getBulkEditCommentsJSONObject(JSONArray vulnIds, String comment) {
		try {
			JSONObject editData = new JSONObject();
			editData.put("comment", comment);
			JSONObject data = new JSONObject();
			data.put("editData", editData);
			data.put("vulnerabilityIds", vulnIds);
			return data;
		} catch ( JSONException e ) {
			throw new RuntimeException("Cannot create FoD bulk edit request", e);
		}
	}

	private JSONArray getVulnIds(Context context) {
		JSONArray vulnIds = new JSONArray();
		Object root = rootExpression==null?context:SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
		if ( getIterableExpression() == null ) {
			vulnIds.put(SpringExpressionUtil.evaluateExpression(root, getVulnIdExpression(), String.class));
		} else {
			Iterable<?> iterable = SpringExpressionUtil.evaluateExpression(root, getIterableExpression(), Iterable.class);
			for ( Object o : iterable ) {
				vulnIds.put(SpringExpressionUtil.evaluateExpression(o, getVulnIdExpression(), String.class));
			}
		}
		return vulnIds;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	

	public String getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(String rootExpression) {
		this.rootExpression = rootExpression;
	}

	public String getIterableExpression() {
		return iterableExpression;
	}

	public void setIterableExpression(String iterableExpression) {
		this.iterableExpression = iterableExpression;
	}

	public String getVulnIdExpression() {
		return vulnIdExpression;
	}

	public void setVulnIdExpression(String vulnIdExpression) {
		this.vulnIdExpression = vulnIdExpression;
	}

	public String getCommentTemplate() {
		return commentTemplate;
	}

	public void setCommentTemplate(String commentTemplate) {
		this.commentTemplate = commentTemplate;
	}
	
}
