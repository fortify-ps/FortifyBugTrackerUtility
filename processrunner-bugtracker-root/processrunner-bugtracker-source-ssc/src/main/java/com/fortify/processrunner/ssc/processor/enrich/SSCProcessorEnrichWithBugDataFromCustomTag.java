package com.fortify.processrunner.ssc.processor.enrich;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * Enrich the current vulnerability with the bug link from the configured {@link #customTagName}
 * custom tag.
 * 
 * @author Ruud Senden
 *
 */
public class SSCProcessorEnrichWithBugDataFromCustomTag extends AbstractSSCProcessorEnrich {
	private final String customTagName;
	public SSCProcessorEnrichWithBugDataFromCustomTag(String customTagName) {
		this.customTagName = customTagName;
	}
	
	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		if ( customTagName == null ) {
			currentVulnerability.put("bugLink", currentVulnerability.get("bugURL"));
		} else {
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			String customTagGuid = conn.getCustomTagGuid(customTagName);
			String bugLink = SpringExpressionUtil.evaluateExpression(currentVulnerability, "details.customTagValues", JSONList.class).mapValue("customTagGuid", customTagGuid, "textValue", String.class);
			currentVulnerability.put("bugLink", bugLink);
		}
		return true;
	}

}
