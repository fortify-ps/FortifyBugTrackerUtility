package com.fortify.processrunner.fod.processor.composite;

import java.util.Arrays;
import java.util.List;

import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithBugDataFromComment;
import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithExtraFoDData;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnBugSubmittedComment;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnBugSubmittedField;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnHasCommentsField;
import com.fortify.processrunner.fod.processor.retrieve.FoDProcessorRetrieveVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.processor.ProcessorGroupByExpressions;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link IProcessor} implementation allows for updating tracker state
 * based on FoD vulnerability state. It will retrieve all FoD vulnerabilities
 * (both open and closed) that have been previously submitted to the bug tracker,
 * group them by external bug link/id, and determine whether all vulnerabilities
 * in each group can be considered 'closed' and thus the corresponding bug
 * can be closed as well. 
 */
public class FoDProcessorUpdateBugTrackerState extends AbstractCompositeProcessor {
	private boolean useFoDCommentForSubmittedBugs = false;
	
	private IProcessor vulnerabilityProcessor;
	
	@Override
	public List<IProcessor> getProcessors() {
		return Arrays.asList(createRootVulnerabilityArrayProcessor());
	}
	
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		FoDProcessorRetrieveVulnerabilities result = new FoDProcessorRetrieveVulnerabilities(
			isUseFoDCommentForSubmittedBugs() ? createCommentBasedProcessors() : createBugLinkBasedProcessors(),
			createGroupingProcessor()
		);
		result.setIncludeRemoved(true);
		return result;
	}
	
	/**
	 * Create the processors for managing bug state based on the FoD bugLink field.
	 */
	protected IProcessor createBugLinkBasedProcessors() {
		return new CompositeProcessor(
				// Add a top-level field filter on the bugSubmitted field to include only vulnerabilities 
				// that have already been submitted to the bug tracker
				new FoDFilterOnBugSubmittedField("true")
				// TODO Add processor to add bugId field to vulnerability?
		);
	}
	
	/**
	 * Create the processor for managing bug state based on FoD comments.
	 * @return
	 */
	protected IProcessor createCommentBasedProcessors() {
		return new CompositeProcessor(
			// Add top-level field filter to include only vulnerabilities with comments
			// (to avoid loading summary data if there are no comments anyway)
			new FoDFilterOnHasCommentsField("true"),
			// Add processor to add FoD summary data as required by FoDFilterOnBugSubmittedComment
			new FoDProcessorEnrichWithExtraFoDData("summary"),
			// Add comment-based filter to include only vulnerabilities that have already been submitted 
			// to the bug tracker
			new FoDFilterOnBugSubmittedComment(false),
			// Add processor to add bugLink (and bugId?) fields from comment to vulnerability
			new FoDProcessorEnrichWithBugDataFromComment()
		);
	}
	
	
	protected IProcessor createGroupingProcessor() {
		ProcessorGroupByExpressions result = new ProcessorGroupByExpressions();
		result.setRootExpression(SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability"));
		result.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugLink}"));
		result.setGroupProcessor(getVulnerabilityProcessor());
		return result;
	}
	

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

	public IProcessor getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	public void setVulnerabilityProcessor(IProcessor vulnerabilityProcessor) {
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}
}
