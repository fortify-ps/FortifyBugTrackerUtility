/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.fod.processor.enrich;

import java.util.HashMap;
import java.util.Map;

import com.fortify.api.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.spring.SpringExpressionUtil;
import com.fortify.api.util.spring.expression.TemplateExpression;
import com.fortify.processrunner.common.processor.enrich.AbstractProcessorEnrichCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.util.ondemand.AbstractOnDemandRestPropertyLoader;

/**
 * This {@link AbstractProcessorEnrichCurrentVulnerability} implementation adds the following 
 * on-demand properties to the current vulnerability, allowing this extra vulnerability data
 * to be automatically loaded from FoD whenever these properties are being accessed:
 * <ul>
 *  <li>summary</li>
 *  <li>details</li>
 *  <li>recommendations</li>
 *  <li>screenshots</li>
 *  <li>history</li>
 *  <li>request-response</li>
 *  <li>headers</li>
 *  <li>parameters</li>
 *  <li>traces</li>
 * </ul>
 * 
 * @author Ruud Senden
 */
public class FoDProcessorEnrichWithOnDemandIssueDetails extends AbstractProcessorEnrichCurrentVulnerability {
	private static final Map<String, TemplateExpression> FIELD_TO_URI_EXPRESSION_MAP = getUriExpressionMap();

	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		for ( Map.Entry<String, TemplateExpression> entry : FIELD_TO_URI_EXPRESSION_MAP.entrySet() ) {
			if ( !currentVulnerability.containsKey(entry.getKey()) ) {
				String uri = SpringExpressionUtil.evaluateExpression(context, entry.getValue(), String.class);
				currentVulnerability.put(entry.getKey(), new FoDOnDemandPropertyLoader(uri));
			}
		}
		return true;
	}

	private static final Map<String, TemplateExpression> getUriExpressionMap() {
		Map<String, TemplateExpression> result = new HashMap<String, TemplateExpression>();
		add(result, "summary");
		add(result, "details");
		add(result, "recommendations");
		add(result, "screenshots");
		add(result, "history");
		add(result, "request-response");
		add(result, "headers");
		add(result, "parameters");
		add(result, "traces");
		return result;
	}
	
	private static final void add(Map<String, TemplateExpression> map, String name) {
		map.put(name, SpringExpressionUtil.parseTemplateExpression("/api/v3/releases/${[FoDReleaseId]}/vulnerabilities/${[CurrentVulnerability].vulnId}/"+name));
	}
	
	private static final class FoDOnDemandPropertyLoader extends AbstractOnDemandRestPropertyLoader {
		private static final long serialVersionUID = 1L;
		public FoDOnDemandPropertyLoader(String uri) {
			super(uri);
		}
		protected FoDAuthenticatingRestConnection getConnection(Context context) {
			return FoDConnectionFactory.getConnection(context);
		}
	}
}
