/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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

import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.ondemand.AbstractJSONMapOnDemandLoader;
import com.fortify.util.rest.json.preprocessor.enrich.AbstractJSONMapEnrich;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link AbstractJSONMapEnrich} implementation adds an on-demand revision property to the
 * current vulnerability based on the revision included in the issue details. This is a work-around
 * for a bug in some SSC versions, where the project version issues REST API doesn't return the
 * correct revision until metrics have been refreshed.
 * 
 * @author Ruud Senden
 *
 */
public class SSCJSONMapEnrichWithRevisionFromDetails extends AbstractJSONMapEnrich {
	public SSCJSONMapEnrichWithRevisionFromDetails() {}
	
	@Override
	protected void enrich(JSONMap json) {
		json.put("revision", new SSCJSONMapOnDemandLoaderRevisionFromDetails());
	}
	
	private static class SSCJSONMapOnDemandLoaderRevisionFromDetails extends AbstractJSONMapOnDemandLoader {
		private static final long serialVersionUID = 1L;
		
		public SSCJSONMapOnDemandLoaderRevisionFromDetails() {
			super(true);
		}

		@Override
		public Object getOnDemand(String propertyName, JSONMap parent) {
			return SpringExpressionUtil.evaluateExpression(parent, "details.revision", String.class);
		}
	}

}
