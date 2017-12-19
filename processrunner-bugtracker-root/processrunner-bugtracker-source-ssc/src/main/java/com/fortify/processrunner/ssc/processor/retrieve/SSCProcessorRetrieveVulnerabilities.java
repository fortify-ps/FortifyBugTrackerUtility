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
package com.fortify.processrunner.ssc.processor.retrieve;

import com.fortify.api.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.rest.query.IRestConnectionQuery;
import com.fortify.api.util.rest.query.PagingData;
import com.fortify.processrunner.common.processor.AbstractProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;

/**
 * TODO Add JavaDoc
 * @author Ruud Senden
 *
 */
public class SSCProcessorRetrieveVulnerabilities extends AbstractProcessorRetrieveVulnerabilities {
	public SSCProcessorRetrieveVulnerabilities(IRestConnectionQuery vulnerabilityQuery, IProcessor vulnerabilityProcessor, String purpose) {
		super(vulnerabilityQuery, vulnerabilityProcessor, purpose);
	}

	@Override
	protected String getLogMessageStart(Context context, String purpose) {
		IContextSSCCommon contextSSC = context.as(IContextSSCCommon.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		return "[SSC] Retrieving vulnerabilities"+(purpose==null?"":" for "+purpose)+" from application version id "+contextSSC.getSSCApplicationVersionId()+" at "+conn.getBaseUrl();
	}
	
	@Override
	protected String getLogMessageNextPage(Context context, PagingData pagingData) {
		return "[SSC] Loading next set of data";
	}
	
	@Override
	protected String getLogMessageProcessingVulnerability(Context context, JSONMap currentVulnerability) {
		return "[SSC] Processing vulnerability "+currentVulnerability.get("id");
	}
}

