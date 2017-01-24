package com.fortify.processrunner.fod.processor.composite;

import com.fortify.processrunner.fod.processor.enrich.FoDProcessorEnrichWithExtraFoDData;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnBugSubmittedComment;
import com.fortify.processrunner.fod.processor.filter.FoDFilterOnBugSubmittedField;
import com.fortify.processrunner.fod.processor.update.FoDProcessorUpdateVulnerabilitiesAddBugLink;
import com.fortify.processrunner.fod.processor.update.FoDProcessorUpdateVulnerabilitiesAddBugSubmittedComment;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class extends {@link FoDProcessorRetrieveFilteredVulnerabilities} with the following
 * functionality:
 * <ul>
 *  <li>Add filters to exclude vulnerabilities already submitted to the bug tracker</li>
 *  <li>After submitting a group of vulnerabilities to the bug tracker, add a corresponding
 *      comment or bugLink field to the FoD vulnerabilities</li>
 * </ul>
 * Apart from the configuration as documented for {@link FoDProcessorRetrieveFilteredVulnerabilities},
 * this class can be configured with an additional {@link #useFoDCommentForSubmittedBugs} flag.
 * If set to false (default), a link to the submitted bug will be stored in the FoD bugLink field.
 * If set to true, information about the submitted bug will be stored as an FoD comment.
 */
public class FoDProcessorSubmitFilteredVulnerabilitiesToBugTracker extends FoDProcessorRetrieveFilteredVulnerabilities {
	private boolean useFoDCommentForSubmittedBugs = false;
	
	@Override
	protected CompositeProcessor createTopLevelFieldFilters() {
		CompositeProcessor result = super.createTopLevelFieldFilters();
		if ( !isUseFoDCommentForSubmittedBugs() ) {
			// If FoD comments are not used for submitted bugs, we add a top-level field filter on bugSubmitted=false
			result.getProcessors().add(new FoDFilterOnBugSubmittedField("false"));
		}
		return result;
	}
	
	@Override
	protected CompositeProcessor createSubLevelFieldFilters() {
		CompositeProcessor result = super.createSubLevelFieldFilters();
		if ( isUseFoDCommentForSubmittedBugs() ) {
			// If FoD comments are used for submitted bugs, we add a comment-based filter to exclude vulnerabilities
			// that have already been submitted to the bug tracker
			result.getProcessors().add(new FoDFilterOnBugSubmittedComment(true));
		}
		return result;
	}
	
	@Override
	protected FoDProcessorEnrichWithExtraFoDData createAddJSONDataProcessor() {
		FoDProcessorEnrichWithExtraFoDData result = super.createAddJSONDataProcessor();
		if ( isUseFoDCommentForSubmittedBugs() ) {
			// Add summary field if we used FoD comments for submitted vulnerabilities, 
			// since the FoDFilterOnBugSubmittedComment filter requires access to this field.
			result.getFields().add("summary");
		}
		return result;
	}
	
	@Override
	protected IProcessor createGroupProcessor() {
		return new CompositeProcessor(getVulnerabilityProcessor(), createUpdateFoDWithSubmittedBugProcessor());
	}
	
	protected IProcessor createUpdateFoDWithSubmittedBugProcessor() {
		return isUseFoDCommentForSubmittedBugs() 
				? createUpdateFoDCommentWithSubmittedBugProcessor()
				: createUpdateFoDBugLinkWithSubmittedBugProcessor();  
	}
	
	protected IProcessor createUpdateFoDCommentWithSubmittedBugProcessor() {
		FoDProcessorUpdateVulnerabilitiesAddBugSubmittedComment result = new FoDProcessorUpdateVulnerabilitiesAddBugSubmittedComment();
		result.setIterableExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"));
		return result;
	}
	
	protected IProcessor createUpdateFoDBugLinkWithSubmittedBugProcessor() {
		FoDProcessorUpdateVulnerabilitiesAddBugLink result = new FoDProcessorUpdateVulnerabilitiesAddBugLink();
		result.setIterableExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"));
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
}
