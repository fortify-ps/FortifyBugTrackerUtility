package com.fortify.processrunner.file;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fortify.processrunner.common.context.IContextBugTracker;
import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.common.processor.IProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapsFromGroupedObjects;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This {@link IProcessor} implementation allows for writing data from the 
 * current {@link Context} instance to a file based on a list of configured 
 * {@link TemplateExpression} instances.
 * 
 * @author Ruud Senden
 */
public class ProcessorFileSubmitIssueForVulnerabilities extends AbstractProcessorBuildObjectMapsFromGroupedObjects implements IProcessorSubmitIssueForVulnerabilities {
	private static final Log LOG = LogFactory.getLog(ProcessorFileSubmitIssueForVulnerabilities.class);
	
	public ProcessorFileSubmitIssueForVulnerabilities() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	public String getBugTrackerName() {
		return "File";
	}
	
	public boolean setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener) {
		// We ignore the issueSubmittedListener since we want to do a full export each time.
		// TODO should we make this configurable (full or partial export)?
		// We return false to indicate that we don't  support an issue submitted listener.
		return false;
	}
	
	
	@Override
	protected void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		context.as(IContextBugTracker.class).setBugTrackerName(getBugTrackerName());
	}
	
	@Override
	protected boolean processMaps(Context context, String groupName, List<Object> currentGroup, List<LinkedHashMap<String, Object>> listOfMaps) {
		CsvSchema.Builder schemaBuilder = CsvSchema.builder();
	    for (String col : getFields().keySet()) {
            schemaBuilder.addColumn(col);
        }
	    CsvSchema schema = schemaBuilder.build().withHeader();
	    try {
			new CsvMapper().writer(schema).writeValue(new File(groupName), listOfMaps);
		} catch (Exception e) {
			throw new RuntimeException("Error writing data to file "+groupName, e);
		}
		LOG.info(String.format("[File] Submitted %d vulnerabilities to %s", currentGroup.size(), groupName));
		return true;
	}
	
	@Override
	public boolean isForceGrouping() {
		return true;
	}
	
	public boolean isIgnorePreviouslySubmittedIssues() {
		return false;
	}
}
