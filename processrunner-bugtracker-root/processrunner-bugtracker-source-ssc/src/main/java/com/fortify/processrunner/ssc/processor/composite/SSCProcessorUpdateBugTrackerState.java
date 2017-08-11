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
package com.fortify.processrunner.ssc.processor.composite;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.common.bugtracker.issue.IssueState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithBugDataFromCustomTag;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithIssueDetails;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnBugURL;
import com.fortify.processrunner.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.ssc.vulnerability.SSCVulnerabilityUpdater;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link IProcessor} implementation allows for updating tracker state
 * based on SSC vulnerability state. It will retrieve all SSC vulnerabilities
 * (both open and closed) that have been previously submitted to the bug tracker,
 * group them by external bug link/id, and determine whether all vulnerabilities
 * in each group can be considered 'closed' and thus the corresponding bug
 * can be closed as well. 
 * 
 * @author Ruud Senden
 */
@Component
public class SSCProcessorUpdateBugTrackerState extends AbstractCompositeProcessor {
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState(); 
	private SSCVulnerabilityUpdater vulnerabilityUpdater;
	
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor;
	
	/**
	 * This method calls {@link SSCVulnerabilityUpdater#checkContext(Context)} to check the
	 * current application version configuration
	 */
	@Override
	protected boolean preProcess(Context context) {
		return (vulnerabilityUpdater==null || vulnerabilityUpdater.checkContext(context)) && super.preProcess(context);
	}
	
	@Override
	protected void addCompositeContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		SSCConnectionFactory.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	@Override
	public List<IProcessor> getProcessors() {
		return Arrays.asList(createRootVulnerabilityArrayProcessor());
	}
	
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		SSCProcessorRetrieveVulnerabilities result = new SSCProcessorRetrieveVulnerabilities(
			createFilters(),
			new SSCProcessorEnrichWithIssueDetails(),
			new SSCProcessorEnrichWithVulnDeepLink(),
			// TODO Move this to SSCVulnerabilityUpdater?
			new SSCProcessorEnrichWithBugDataFromCustomTag(getBugLinkCustomTagName()),
			getVulnState(),
			getUpdateIssueStateProcessor()
		);
		result.getIssueSearchOptions().setIncludeHidden(false);
		result.getIssueSearchOptions().setIncludeRemoved(true);
		result.getIssueSearchOptions().setIncludeSuppressed(true);
		return result;
	}

	private IProcessor createFilters() {
		IProcessor result;
		if ( getVulnerabilityUpdater()!=null ) {
			result = getVulnerabilityUpdater().createVulnerabilityAlreadySubmittedFilter();
		} else {
			result = new SSCFilterOnBugURL(true);
		}
		return result;
	}
	
	public SSCProcessorEnrichWithVulnState getVulnState() {
		return enrichWithVulnStateProcessor;
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities<?> getUpdateIssueStateProcessor() {
		return updateIssueStateProcessor;
	}
	
	public SSCVulnerabilityUpdater getVulnerabilityUpdater() {
		return vulnerabilityUpdater;
	}

	@Autowired(required=false)
	public void setVulnerabilityUpdater(SSCVulnerabilityUpdater vulnerabilityUpdater) {
		this.vulnerabilityUpdater = vulnerabilityUpdater;
	}
	
	private String getBugLinkCustomTagName() {
		return getVulnerabilityUpdater().getBugLinkCustomTagName();
	}

	@Autowired(required=false) // non-required to avoid Spring autowiring failures if bug tracker implementation doesn't include bug state management
	public void setUpdateIssueStateProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor) {
		updateIssueStateProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugURL}"));
		updateIssueStateProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(SSCProcessorEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		updateIssueStateProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugURL"));
		this.updateIssueStateProcessor = updateIssueStateProcessor;
	}
	
	@Autowired(required=false)
	public void setConfiguration(SSCBugTrackerProcessorConfiguration config) {
		getVulnState().setIsVulnerabilityOpenExpression(config.getIsVulnerabilityOpenExpression());
	}
}
