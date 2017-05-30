package com.fortify.processrunner.ssc.processor.enrich;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.spring.SpringExpressionUtil;

public class SSCProcessorEnrichWithBugDataFromCustomTag extends AbstractSSCProcessorEnrich {
	private final String customTagName;
	public SSCProcessorEnrichWithBugDataFromCustomTag(String customTagName) {
		this.customTagName = customTagName;
	}
	
	@Override
	protected boolean enrich(Context context, JSONObject currentVulnerability) throws JSONException {
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String customTagGuid = conn.getCustomTagGuid(customTagName);
		String bugLink = JSONUtil.mapValue(SpringExpressionUtil.evaluateExpression(currentVulnerability, "details.customTagValues", JSONArray.class), "customTagGuid", customTagGuid, "textValue", String.class);
		currentVulnerability.put("bugLink", bugLink);
		return true;
	}

}
