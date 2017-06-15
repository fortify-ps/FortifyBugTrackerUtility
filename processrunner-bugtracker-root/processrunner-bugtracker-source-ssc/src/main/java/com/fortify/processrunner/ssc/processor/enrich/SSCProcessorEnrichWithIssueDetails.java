package com.fortify.processrunner.ssc.processor.enrich;

import javax.ws.rs.HttpMethod;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.IRestConnection;

/**
 * This class allows for loading additional issue details from SSC and adding them to the 
 * current SSC vulnerability JSON object.
 * 
 * @author Ruud Senden
 */
public class SSCProcessorEnrichWithIssueDetails extends AbstractSSCProcessorEnrich {

	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		IRestConnection conn = SSCConnectionFactory.getConnection(context);
		JSONMap details = conn.executeRequest(HttpMethod.GET,  
				conn.getBaseResource().path("/api/v1/issueDetails").path(""+currentVulnerability.get("id")), JSONMap.class);
		currentVulnerability.put("details", details.get("data"));
		return true;
	}
}
