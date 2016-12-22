package com.fortify.processrunner.fod.processor;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public class FoDProcessorAddVulnDeepLink extends AbstractProcessor {
	private TemplateExpression deepLinkUriExpression = SpringExpressionUtil.parseTemplateExpression("redirect/Issues/${vulnId}");

	@Override
	protected boolean process(Context context) {
		IContextFoD ctx = context.as(IContextFoD.class);
		JSONObject currentVulnerability = ctx.getFoDCurrentVulnerability();
		String baseUrl = ctx.getFoDConnectionRetriever().getBaseUrl();
		String deepLink = baseUrl + SpringExpressionUtil.evaluateExpression(currentVulnerability, deepLinkUriExpression, String.class);
		try {
			currentVulnerability.put("deepLink", deepLink);
		} catch (JSONException e) {
			throw new RuntimeException("Error adding deep link to vulnerability data", e);
		}
		return true;
	}
}
