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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class allows for loading additional vulnerability details from FoD and adding them to the 
 * current FoD vulnerability JSON object. The following additional vulnerability details are
 * supported by this class:
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
 * The extra data from the list above to be added to the current vulnerability can be specified either 
 * via the constructor or the {@link #setFields(Set)} methods. The data will be added as a JSON object
 * to the current vulnerability under the corresponding property name.
 */
public class FoDProcessorEnrichWithExtraFoDData extends AbstractFoDProcessorEnrich {
	private static final Map<String, TemplateExpression> FIELD_TO_URI_EXPRESSION_MAP = getUriExpressionMap();
	private Set<String> fields = new HashSet<String>();
	
	public FoDProcessorEnrichWithExtraFoDData() {}
	
	public FoDProcessorEnrichWithExtraFoDData(String... fields) {
		getFields().addAll(Arrays.asList(fields));
	}

	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		for ( String field : fields ) {
			currentVulnerability.put(field, getJSONMap(context, field));
		}
		return true;
	}

	private JSONMap getJSONMap(Context context, String field) {
		IRestConnection conn = FoDConnectionFactory.getConnection(context);
		TemplateExpression uriExpr = FIELD_TO_URI_EXPRESSION_MAP.get(field); 
		if ( uriExpr == null ) {
			throw new RuntimeException("Unknown FoD field "+field);
		}
		String uri = SpringExpressionUtil.evaluateExpression(context, uriExpr, String.class);
		JSONMap data = conn.executeRequest(HttpMethod.GET, 
				conn.getBaseResource().path(uri), JSONMap.class);
		return data;
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

	/**
	 * @return the fields
	 */
	public Set<String> getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(Set<String> fields) {
		this.fields = fields;
	}
}
