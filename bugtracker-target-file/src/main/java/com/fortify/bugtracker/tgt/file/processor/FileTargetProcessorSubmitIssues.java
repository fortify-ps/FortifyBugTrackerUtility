/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.bugtracker.tgt.file.processor;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fortify.bugtracker.common.src.updater.IExistingIssueVulnerabilityUpdater;
import com.fortify.bugtracker.common.tgt.config.ITargetSubmitIssuesConfiguration;
import com.fortify.bugtracker.common.tgt.context.IContextBugTracker;
import com.fortify.bugtracker.common.tgt.processor.ITargetProcessorSubmitIssues;
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
@Component
public class FileTargetProcessorSubmitIssues extends AbstractProcessorBuildObjectMapsFromGroupedObjects implements ITargetProcessorSubmitIssues {
	private static final Log LOG = LogFactory.getLog(FileTargetProcessorSubmitIssues.class);
	
	public FileTargetProcessorSubmitIssues() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	public String getTargetName() {
		return "File";
	}
	
	public boolean setVulnerabilityUpdater(IExistingIssueVulnerabilityUpdater issueSubmittedListener) {
		// We ignore the issueSubmittedListener since we want to do a full export each time.
		// TODO should we make this configurable (full or partial export)?
		// We return false to indicate that we don't  support an issue submitted listener.
		return false;
	}
	
	
	@Override
	protected void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		context.as(IContextBugTracker.class).setTargetName(getTargetName());
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
	
	/**
	 * Autowire the configuration from the Spring configuration file.
	 * @param config
	 */
	@Autowired
	public void setConfiguration(ITargetSubmitIssuesConfiguration config) {
		super.setGroupTemplateExpression(config.getGroupTemplateExpressionForSubmit());
		super.setFields(config.getFieldsForSubmit());
	}
}
