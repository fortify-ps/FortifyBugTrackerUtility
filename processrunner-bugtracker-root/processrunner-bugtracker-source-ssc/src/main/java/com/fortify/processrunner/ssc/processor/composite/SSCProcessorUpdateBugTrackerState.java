package com.fortify.processrunner.ssc.processor.composite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;

/**
 * This {@link IProcessor} implementation allows for updating tracker state
 * based on SSC vulnerability state. It will retrieve all SSC vulnerabilities
 * (both open and closed) that have been previously submitted to the bug tracker,
 * group them by external bug link/id, and determine whether all vulnerabilities
 * in each group can be considered 'closed' and thus the corresponding bug
 * can be closed as well. 
 * 
 * TODO Update this for SSC
 */
public class SSCProcessorUpdateBugTrackerState extends AbstractCompositeProcessor {
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState(); 
	private Set<String> extraFields = new HashSet<String>();
	private boolean useFoDCommentForSubmittedBugs = false;
	
	private AbstractProcessorUpdateIssueStateForVulnerabilities updateIssueStateProcessor;
	
	@Override
	public List<IProcessor> getProcessors() {
		//return Arrays.asList(createRootVulnerabilityArrayProcessor());
		return null; // TODO
	}
	
	/*
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		SSCProcessorRetrieveVulnerabilities result = new SSCProcessorRetrieveVulnerabilities(
			isUseFoDCommentForSubmittedBugs() ? createCommentBasedProcessors() : createBugLinkBasedProcessors(),
			createAddVulnDeepLinkProcessor(),
			createAddJSONDataProcessor(),
			getVulnState(),
			getUpdateIssueStateProcessor()
		);
		result.setIncludeRemoved(true);
		return result;
	}
	*/

	/**
	 * Create the processors for managing bug state based on the FoD bugLink field.
	 */
	/*
	protected IProcessor createBugLinkBasedProcessors() {
		return new CompositeProcessor(
				// Add a top-level field filter on the bugSubmitted field to include only vulnerabilities 
				// that have already been submitted to the bug tracker
				new SSCFilterOnBugSubmittedField("true")
				// TODO Add processor to add bugId field to vulnerability?
		);
	}
	*/
	/**
	 * Create the processor for managing bug state based on FoD comments.
	 * @return
	 */
	/*
	protected IProcessor createCommentBasedProcessors() {
		return new CompositeProcessor(
			// Add top-level field filter to include only vulnerabilities with comments
			// (to avoid loading summary data if there are no comments anyway)
			new SSCFilterOnHasCommentsField("true"),
			// Add processor to add FoD summary data as required by FoDFilterOnBugSubmittedComment
			new SSCProcessorEnrichWithIssueDetails("summary"),
			// Add comment-based filter to include only vulnerabilities that have already been submitted 
			// to the bug tracker
			new FoDFilterOnBugSubmittedComment(false),
			// Add processor to add bugLink (and bugId?) fields from comment to vulnerability
			new FoDProcessorEnrichWithBugDataFromComment()
		);
	}
	
	protected SSCProcessorEnrichWithIssueDetails createAddJSONDataProcessor() {
		SSCProcessorEnrichWithIssueDetails result = new SSCProcessorEnrichWithIssueDetails();
		result.setFields(getExtraFields());
		return result;
	}
	
	protected IProcessor createAddVulnDeepLinkProcessor() {
		return new SSCProcessorEnrichWithVulnDeepLink();
	}
	*/
	/**
	 * @return the useFoDCommentForSubmittedBugs
	 */
	public boolean isUseFoDCommentForSubmittedBugs() {
		return useFoDCommentForSubmittedBugs;
	}

	/**
	 * @param useFoDCommentForSubmittedBugs the useFoDCommentForSubmittedBugs to set
	 */
	public void setUseFoDCommentForSubmittedBugs(boolean useFoDCommentForSubmittedBugs) {
		this.useFoDCommentForSubmittedBugs = useFoDCommentForSubmittedBugs;
	}
	
	/**
	 * @return the extraFields
	 */
	public Set<String> getExtraFields() {
		return extraFields;
	}

	/**
	 * @param extraFields the extraFields to set
	 */
	public void setExtraFields(Set<String> extraFields) {
		this.extraFields = extraFields;
	}
	
	public SSCProcessorEnrichWithVulnState getVulnState() {
		return enrichWithVulnStateProcessor;
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities getUpdateIssueStateProcessor() {
		return updateIssueStateProcessor;
	}

	/*
	public void setUpdateIssueStateProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities updateIssueStateProcessor) {
		updateIssueStateProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugLink}"));
		updateIssueStateProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(SSCProcessorEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		updateIssueStateProcessor.setVulnBugIdExpression(SpringExpressionUtil.parseSimpleExpression("bugId"));
		updateIssueStateProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugLink"));
		this.updateIssueStateProcessor = updateIssueStateProcessor;
	}
	*/
}
