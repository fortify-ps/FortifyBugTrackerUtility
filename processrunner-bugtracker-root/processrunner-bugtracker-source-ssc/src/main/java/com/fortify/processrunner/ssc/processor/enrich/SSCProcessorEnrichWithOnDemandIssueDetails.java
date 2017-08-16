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

import java.util.List;

import com.fortify.processrunner.common.processor.enrich.AbstractProcessorEnrichCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.util.ondemand.AbstractOnDemandRestPropertyLoader;
import com.fortify.processrunner.util.ondemand.IOnDemandPropertyLoader;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;

/**
 * This {@link AbstractProcessorEnrichCurrentVulnerability} implementation adds the
 * vulnerability details as an on-demand object (see {@link IOnDemandPropertyLoader})
 * to the current vulnerability.
 * 
 * @author Ruud Senden
 *
 */
public class SSCProcessorEnrichWithOnDemandIssueDetails extends AbstractProcessorEnrichCurrentVulnerability {

	@Override
	protected boolean enrich(Context context, JSONMap currentVulnerability) {
		currentVulnerability.put("details", new SSCOnDemandIssueDetailsLoader("/api/v1/issueDetails/"+currentVulnerability.get("id")));
		return true;
	}
	
	private static final class SSCOnDemandIssueDetailsLoader extends AbstractOnDemandRestPropertyLoader {
		private static final long serialVersionUID = 1L;
		public SSCOnDemandIssueDetailsLoader(String uri) {
			super(uri, "data");
		}
		protected SSCAuthenticatingRestConnection getConnection(Context context) {
			return SSCConnectionFactory.getConnection(context);
		}
		/**
		 * This method adds custom tag names based on the custom tag GUID's returned by SSC
		 */
		@Override
		protected JSONMap updateData(Context context, JSONMap data) {
			SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
			List<JSONMap> customTags = ContextSpringExpressionUtil.evaluateExpression(context, data, "customTagValues", JSONList.class).asValueType(JSONMap.class);
			for ( JSONMap customTag : customTags ) {
				customTag.put("customTagName", conn.getCustomTagName(customTag.get("customTagGuid", String.class)));
			}
			return super.updateData(context, data);
		}
	}
}
