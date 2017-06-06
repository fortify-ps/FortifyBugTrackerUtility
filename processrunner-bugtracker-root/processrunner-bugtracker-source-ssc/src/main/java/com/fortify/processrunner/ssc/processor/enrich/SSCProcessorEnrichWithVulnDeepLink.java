package com.fortify.processrunner.ssc.processor.enrich;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class determines the SSC browser-viewable deep link for the current vulnerability,
 * and adds this link as the 'deepLink' property to the current vulnerability JSON object.
 * 
 * @author Ruud Senden
 */
public class SSCProcessorEnrichWithVulnDeepLink extends AbstractSSCProcessorEnrich {
	private TemplateExpression deepLinkUriExpression = SpringExpressionUtil.parseTemplateExpression("/html/ssc/index.jsp#!/version/${projectVersionId}/fix/${id}/");

	@Override
	protected boolean enrich(Context context, JSONObject currentVulnerability) throws JSONException {
		String baseUrl = SSCConnectionFactory.getConnection(context).getBaseUrl();
		String deepLink = baseUrl + SpringExpressionUtil.evaluateExpression(currentVulnerability, deepLinkUriExpression, String.class);
		currentVulnerability.put("deepLink", deepLink);
		return true;
	}
}
