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
package com.fortify.bugtracker.source.fod.processor.retrieve;

import com.fortify.bugtracker.common.source.processor.AbstractProcessorRetrieveVulnerabilities;
import com.fortify.bugtracker.source.fod.connection.FoDConnectionFactory;
import com.fortify.bugtracker.source.fod.context.IContextFoD;
import com.fortify.client.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.query.IRestConnectionQuery;
import com.fortify.util.rest.query.PagingData;

/**
 * TODO Update JavaDoc
 */
public class FoDProcessorRetrieveVulnerabilities extends AbstractProcessorRetrieveVulnerabilities {
	public FoDProcessorRetrieveVulnerabilities(IRestConnectionQuery vulnerabilityQuery, IProcessor vulnerabilityProcessor, String purpose) {
		super(vulnerabilityQuery, vulnerabilityProcessor, purpose);
	}

	@Override
	protected String getLogMessageStart(Context context, String purpose) {
		IContextFoD contextFoD = context.as(IContextFoD.class);
		FoDAuthenticatingRestConnection conn = FoDConnectionFactory.getConnection(context);
		return "[FoD] Retrieving vulnerabilities"+(purpose==null?"":" for "+purpose)+" from release "+contextFoD.getFoDReleaseId()+" at "+conn.getBaseUrl();
	}
	
	@Override
	protected String getLogMessageNextPage(Context context, PagingData pagingData) {
		return "[FoD] Loading next set of data";
	}
	
	@Override
	protected String getLogMessageProcessingVulnerability(Context context, JSONMap currentVulnerability) {
		return "[FoD] Processing vulnerability "+currentVulnerability.get("vulnId");
	}
}
