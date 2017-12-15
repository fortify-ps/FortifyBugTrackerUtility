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
package com.fortify.processrunner.ssc.processor.enrich;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.api.util.rest.json.JSONList;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.processrunner.common.processor.enrich.AbstractProcessorEnrichCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.ondemand.IOnDemandPropertyLoader;

/**
 * This {@link AbstractProcessorEnrichCurrentVulnerability} implementation adds an on-demand 
 * (see {@link IOnDemandPropertyLoader}) bugURL property to the current vulnerability that 
 * retrieves the bug URL from a configurable custom tag. We use {@link IOnDemandPropertyLoader} 
 * because the custom tag values are loaded on-demand as well as part of the vulnerability 
 * details (see {@link SSCProcessorEnrichWithOnDemandIssueDetails}.
 * 
 * @author Ruud Senden
 *
 */
public class SSCProcessorEnrichWithOnDemandBugURLFromCustomTag extends AbstractProcessorEnrichCurrentVulnerability {
	private final String customTagName;
	public SSCProcessorEnrichWithOnDemandBugURLFromCustomTag(String customTagName) {
		this.customTagName = customTagName;
	}
	
	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		if ( StringUtils.isNotBlank(customTagName) ) {
			currentVulnerability.put("bugURL", new IOnDemandPropertyLoader<String>() {
				private static final long serialVersionUID = 1L;
				public String getValue(Context ctx, Map<?, ?> targetMap) {
					return ContextSpringExpressionUtil.evaluateExpression(ctx, targetMap, "details.customTagValues", JSONList.class).mapValue("customTagName", customTagName, "textValue", String.class);
				}
			});
		}
		return true;
	}

}
