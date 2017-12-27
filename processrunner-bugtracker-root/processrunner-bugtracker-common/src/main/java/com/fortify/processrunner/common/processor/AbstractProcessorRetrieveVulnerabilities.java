/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.processrunner.common.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.rest.json.processor.IJSONMapProcessor;
import com.fortify.api.util.rest.query.IRestConnectionQuery;
import com.fortify.api.util.rest.query.PagingData;
import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.IProcessor;

public abstract class AbstractProcessorRetrieveVulnerabilities extends AbstractProcessor {

	private static final Log LOG = LogFactory.getLog(AbstractProcessorRetrieveVulnerabilities.class);
	protected final IProcessor vulnerabilityProcessor;
	protected final IRestConnectionQuery vulnerabilityQuery;
	protected final String purpose;

	public AbstractProcessorRetrieveVulnerabilities(IRestConnectionQuery vulnerabilityQuery, IProcessor vulnerabilityProcessor, String purpose) {
		this.vulnerabilityQuery = vulnerabilityQuery;
		this.vulnerabilityProcessor = vulnerabilityProcessor;
		this.purpose = purpose;
	}

	@Override
	public boolean process(final Context context) {
		vulnerabilityProcessor.process(Phase.PRE_PROCESS, context);
		final IContextCurrentVulnerability contextCurrentVulnerability = context.as(IContextCurrentVulnerability.class);
		LOG.info(getLogMessageStart(context, purpose));
		
		vulnerabilityQuery.processAll(new IJSONMapProcessor() {
			
			@Override
			public void process(JSONMap json) {
				contextCurrentVulnerability.setCurrentVulnerability(json);
				if ( LOG.isTraceEnabled() ) {
					LOG.trace(getLogMessageProcessingVulnerability(context, json));
				}
				// We ignore the boolean result as we want to continue processing next vulnerabilities
				vulnerabilityProcessor.process(Phase.PROCESS, context);
				contextCurrentVulnerability.setCurrentVulnerability(null);
			}
			
			

			@Override
			public void notifyNextPage(PagingData pagingData) {
				LOG.info(getLogMessageNextPage(context, pagingData));
			}
		});
		return vulnerabilityProcessor.process(Phase.POST_PROCESS, context);
	}

	protected abstract String getLogMessageStart(Context context, String purpose);
	
	protected abstract String getLogMessageProcessingVulnerability(Context context, JSONMap json);
	
	protected abstract String getLogMessageNextPage(Context context, PagingData pagingData);

}