package com.fortify.processrunner.ssc.processor.enrich;

import javax.ws.rs.HttpMethod;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.util.rest.IRestConnection;

/**
 * This class allows for loading additional issue details from SSC and adding them to the 
 * current SSC vulnerability JSON object.
 * 
 * @author Ruud Senden
 */
public class SSCProcessorEnrichWithIssueDetails extends AbstractSSCProcessorEnrich {

	@Override
	protected boolean enrich(Context context, JSONObject currentVulnerability) throws JSONException {
		IRestConnection conn = SSCConnectionFactory.getConnection(context);
		JSONObject details = conn.executeRequest(HttpMethod.GET,  
				conn.getBaseResource().path("/api/v1/issueDetails").path(currentVulnerability.getString("id")), JSONObject.class);
		currentVulnerability.put("details", details.getJSONObject("data"));
		return true;
	}
}
