package com.fortify.processrunner.archer.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.archer.connection.ArcherAuthenticatingRestConnection;
import com.fortify.processrunner.archer.context.IContextArcher;
import com.fortify.processrunner.common.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitJSONObjectFromGroupedObjects;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;

/**
 * This {@link AbstractProcessorSubmitJSONObjectFromGroupedObjects} implementation
 * submits issues to Archer.
 */
public class ProcessorArcherSubmitIssueFromGroupedObjects extends AbstractProcessorSubmitJSONObjectFromGroupedObjects {
	@Override
	public void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		// TODO
	}
	
	@Override
	protected String getBugTrackerName() {
		return "Archer";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, JSONObject jsonObject) {
		IContextArcher contextArcher = context.as(IContextArcher.class);
		ArcherAuthenticatingRestConnection conn = contextArcher.getArcherConnectionRetriever().getConnection();
		return conn.submitIssue(jsonObject);
	}
	
	@Override
	protected JSONObject getJSONObject(Context context, LinkedHashMap<String, Object> issueData) {
		// TODO Update Map with context properties
		return super.getJSONObject(context, issueData);
	}
	
	@Override
	protected void addField(JSONObject json, String key, Object value) {
		// TODO Add key prefix, ...
	}
	
}
