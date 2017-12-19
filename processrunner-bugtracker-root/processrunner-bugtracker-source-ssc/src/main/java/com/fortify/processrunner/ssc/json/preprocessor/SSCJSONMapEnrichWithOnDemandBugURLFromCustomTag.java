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
package com.fortify.processrunner.ssc.json.preprocessor;

import org.apache.commons.lang.StringUtils;

import com.fortify.api.util.rest.json.JSONList;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.rest.json.ondemand.AbstractJSONMapOnDemandLoader;
import com.fortify.api.util.rest.json.preprocessor.AbstractJSONMapEnrich;
import com.fortify.api.util.spring.SpringExpressionUtil;

/**
 * This {@link AbstractJSONMapEnrich} implementation adds an on-demand bugURL property to the
 * current vulnerability, which retrieves the bug URL from a configurable custom tag. We use 
 * an on-demand loader because the custom tag values are loaded on-demand as well.
 * 
 * @author Ruud Senden
 *
 */
public class SSCJSONMapEnrichWithOnDemandBugURLFromCustomTag extends AbstractJSONMapEnrich {
	private final String customTagName;
	public SSCJSONMapEnrichWithOnDemandBugURLFromCustomTag(String customTagName) {
		this.customTagName = customTagName;
	}
	
	@Override
	protected void enrich(JSONMap json) {
		if ( StringUtils.isNotBlank(customTagName) ) {
			json.put("bugURL", new SSCJSONMapOnDemandLoaderBugURLFromCustomTag(customTagName));
		}
	}
	
	private static class SSCJSONMapOnDemandLoaderBugURLFromCustomTag extends AbstractJSONMapOnDemandLoader {
		private static final long serialVersionUID = 1L;
		private final String customTagName;
		
		public SSCJSONMapOnDemandLoaderBugURLFromCustomTag(String customTagName) {
			super(true);
			this.customTagName = customTagName;
		}

		@Override
		public Object getOnDemand(String propertyName, JSONMap parent) {
			return SpringExpressionUtil.evaluateExpression(parent, "details.customTagValues", JSONList.class).mapValue("customTagName", customTagName, "textValue", String.class);
		}
	}

}
