/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
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
package com.fortify.bugtracker.common.ssc.json.preprocessor.enrich;

import org.apache.commons.lang.StringUtils;

import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoader;
import com.fortify.util.rest.json.preprocessor.enrich.AbstractJSONMapEnrich;
import com.fortify.util.spring.expression.helper.DefaultExpressionHelper;

/**
 * This {@link AbstractJSONMapEnrich} implementation adds an on-demand bugURL property to the
 * current vulnerability, which retrieves the bug URL from a configurable custom tag. We use 
 * an on-demand loader because the custom tag values are loaded on-demand as well.
 * 
 * @author Ruud Senden
 *
 */
public class SSCJSONMapEnrichWithOnDemandBugURLFromCustomTag extends AbstractJSONMapEnrich {
	private final String customTagGuid;
	public SSCJSONMapEnrichWithOnDemandBugURLFromCustomTag(String customTagGuid) {
		this.customTagGuid = customTagGuid;
	}
	
	@Override
	protected void enrich(JSONMap json) {
		if ( StringUtils.isNotBlank(customTagGuid) ) {
			json.put("bugURL", new SSCJSONMapOnDemandLoaderBugURLFromCustomTag(customTagGuid));
		}
	}
	
	private static class SSCJSONMapOnDemandLoaderBugURLFromCustomTag extends AbstractJSONMapOnDemandLoader {
		private static final long serialVersionUID = 1L;
		private final String customTagGuid;
		
		public SSCJSONMapOnDemandLoaderBugURLFromCustomTag(String customTagGuid) {
			super(true);
			this.customTagGuid = customTagGuid;
		}

		@Override
		public Object getOnDemand(String propertyName, JSONMap parent) {
			return DefaultExpressionHelper.get().evaluateSimpleExpression(parent, "details.customTagValues", JSONList.class).mapValue("customTagGuid", customTagGuid, "textValue", String.class);
		}
	}

}
