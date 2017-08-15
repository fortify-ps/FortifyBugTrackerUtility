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
package com.fortify.processrunner.fod.processor.retrieve;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * <p>This {@link IProcessor} implementation retrieves a list of
 * vulnerabilities from FoD for the release id specified as
 * a {@link Context} property.</p>
 * 
 * <p>For each individual vulnerability, the {@link IProcessor} 
 * implementation configured via 
 * {@link #setVulnerabilityProcessor(IProcessor)} will be called
 * to process the current vulnerability. The current vulnerability
 * can be accessed by the vulnerability processor using the
 * 'CurrentVulnerability' {@link Context} property.</p>
 */
public class FoDProcessorRetrieveVulnerabilities extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDProcessorRetrieveVulnerabilities.class);
	private static final SimpleExpression EXPR_COUNT = SpringExpressionUtil.parseSimpleExpression("totalCount");
	private SimpleExpression rootExpression = SpringExpressionUtil.parseSimpleExpression("items");
	private boolean includeRemoved;
	private IProcessor vulnerabilityProcessor;
	private String searchString;
	
	@Override
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		contextPropertyDefinitions.add(new ContextPropertyDefinition("FoDReleaseId","FoD release id from which to retrieve vulnerabilities",true));
		if ( vulnerabilityProcessor != null ) {
			vulnerabilityProcessor.addContextPropertyDefinitions(contextPropertyDefinitions, context);
		}
	}
	
	public FoDProcessorRetrieveVulnerabilities() {
		vulnerabilityProcessor = new CompositeProcessor();
	}
	
	public FoDProcessorRetrieveVulnerabilities(IProcessor... processors) {
		vulnerabilityProcessor = new CompositeProcessor(processors);
	}
	
	@Override
	public boolean process(Context context) {
		IProcessor processor = getVulnerabilityProcessor();
		processor.process(Phase.PRE_PROCESS, context);
		IContextFoD contextFoD = context.as(IContextFoD.class);
		IContextCurrentVulnerability contextCurrentVulnerability = context.as(IContextCurrentVulnerability.class);
		IRestConnection conn = FoDConnectionFactory.getConnection(context);
		LOG.info("[FoD] Retrieving vulnerabilities for release "+contextFoD.getFoDReleaseId()+" from "+conn.getBaseUrl());
		int start=0;
		int count=50;
		while ( start < count ) {
			LOG.info("[FoD] Loading next set of data");
			WebTarget resource = conn.getBaseResource()
					.path("/api/v3/Releases/{FoDReleaseId}/vulnerabilities")
					.queryParam("qm", "issues")
					.queryParam("limit", "50")
					.queryParam("offset", start)
					.resolveTemplate("FoDReleaseId", contextFoD.getFoDReleaseId());
			if ( StringUtils.isNotBlank(getSearchString()) ) {
				resource = resource.queryParam("filters", getSearchString());
			}
			if ( isIncludeRemoved() ) {
				resource = resource.queryParam("includeFixed", "true").queryParam("includeSuppressed", "true");
			}
			LOG.debug("[FoD] Retrieving vulnerabilities from "+resource);
			JSONMap data = conn.executeRequest(HttpMethod.GET, resource, JSONMap.class);
			count = SpringExpressionUtil.evaluateExpression(data, EXPR_COUNT, Integer.class);
			JSONList vulnerabilitiesArray = SpringExpressionUtil.evaluateExpression(data, getRootExpression(), JSONList.class);
			start += vulnerabilitiesArray.size();
			for ( JSONMap vuln : vulnerabilitiesArray.asValueType(JSONMap.class) ) {
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("[FoD] Processing vulnerability "+vuln.get("vulnId"));
				}
				contextCurrentVulnerability.setCurrentVulnerability(vuln);
				// We ignore the boolean result as we want to continue processing next vulnerabilities
				processor.process(Phase.PROCESS, context);
				contextCurrentVulnerability.setCurrentVulnerability(null);
			}
		}
		return processor.process(Phase.POST_PROCESS, context);
	}

	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public IProcessor getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	public void setVulnerabilityProcessor(IProcessor vulnerabilityProcessor) {
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}

	public boolean isIncludeRemoved() {
		return includeRemoved;
	}

	public void setIncludeRemoved(boolean includeRemoved) {
		this.includeRemoved = includeRemoved;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	
	
	
}
