package com.fortify.processrunner.fod.processor.composite;

import com.fortify.processrunner.fod.processor.FoDFilterOnBugSubmittedComment;
import com.fortify.processrunner.fod.processor.FoDFilterOnBugSubmittedField;
import com.fortify.processrunner.fod.processor.FoDProcessorAddBugLinkToVulnerabilities;
import com.fortify.processrunner.fod.processor.FoDProcessorAddBugSubmittedCommentToVulnerabilities;
import com.fortify.processrunner.fod.processor.FoDProcessorAddJSONData;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

public class FoDProcessorSubmitFilteredVulnerabilitiesToBugTracker extends FoDProcessorRetrieveFilteredVulnerabilities {
	private boolean useFoDCommentForSubmittedBugs = false;
	
	@Override
	protected CompositeProcessor createTopLevelFieldFilters() {
		CompositeProcessor result = super.createTopLevelFieldFilters();
		if ( !isUseFoDCommentForSubmittedBugs() ) {
			// If FoD comments are not used for submitted bugs, we add a top-level field filter on the bugSubmitted field.
			result.getProcessors().add(new FoDFilterOnBugSubmittedField());
		}
		return result;
	}
	
	@Override
	protected CompositeProcessor createSubLevelFieldFilters() {
		CompositeProcessor result = super.createSubLevelFieldFilters();
		if ( isUseFoDCommentForSubmittedBugs() ) {
			// If FoD comments are used for submitted bugs, we add the comment-based filter here
			result.getProcessors().add(new FoDFilterOnBugSubmittedComment());
		}
		return result;
	}
	
	@Override
	protected FoDProcessorAddJSONData createAddJSONDataProcessor() {
		FoDProcessorAddJSONData result = super.createAddJSONDataProcessor();
		if ( isUseFoDCommentForSubmittedBugs() ) {
			// Add summary field if we used FoD comments for submitted vulnerabilities, 
			// since the FoDFilterSubmittedToBugTracker requires access to this field.
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
		FoDProcessorAddBugSubmittedCommentToVulnerabilities result = new FoDProcessorAddBugSubmittedCommentToVulnerabilities();
		result.setIterableExpression(SpringExpressionUtil.parseSimpleExpression("CurrentGroup"));
		return result;
	}
	
	protected IProcessor createUpdateFoDBugLinkWithSubmittedBugProcessor() {
		FoDProcessorAddBugLinkToVulnerabilities result = new FoDProcessorAddBugLinkToVulnerabilities();
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
