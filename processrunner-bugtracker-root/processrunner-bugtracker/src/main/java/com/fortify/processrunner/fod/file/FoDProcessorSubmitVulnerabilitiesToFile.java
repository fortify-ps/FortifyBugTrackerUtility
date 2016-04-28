package com.fortify.processrunner.fod.file;

import com.fortify.processrunner.file.FileProcessorSubmitIssue;
import com.fortify.processrunner.fod.processor.FoDProcessorAddCommentToVulnerabilitiesNonGrouped;
import com.fortify.processrunner.fod.processor.composite.FoDProcessorRetrieveFilteredVulnerabilities;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

public class FoDProcessorSubmitVulnerabilitiesToFile extends AbstractCompositeProcessor {
	private static final SimpleExpression EXPR_ROOT = SpringExpressionUtil.parseSimpleExpression("FoDCurrentVulnerability");
	private static final TemplateExpression EXPR_COMMENT = SpringExpressionUtil.parseTemplateExpression("--- Vulnerability submitted to ${SubmittedIssueBugTrackerName}");
	
	private final FoDProcessorRetrieveFilteredVulnerabilities fod = new FoDProcessorRetrieveFilteredVulnerabilities();
	private final FileProcessorSubmitIssue file = new FileProcessorSubmitIssue();
	
	public FoDProcessorSubmitVulnerabilitiesToFile() {
		fod.setVulnerabilityProcessor(new CompositeProcessor(file, createAddCommentToVulnerabilitiesProcessor()));
		file.setRootExpression(EXPR_ROOT);
	}
	
	protected IProcessor createAddCommentToVulnerabilitiesProcessor() {
		FoDProcessorAddCommentToVulnerabilitiesNonGrouped result = new FoDProcessorAddCommentToVulnerabilitiesNonGrouped();
		result.setCommentTemplate(EXPR_COMMENT);
		return result;
	}

	@Override
	public IProcessor[] getProcessors() {
		return new IProcessor[]{getFod()};
	}


	public FoDProcessorRetrieveFilteredVulnerabilities getFod() {
		return fod;
	}
	
	public FileProcessorSubmitIssue getFile() {
		return file;
	}
}
