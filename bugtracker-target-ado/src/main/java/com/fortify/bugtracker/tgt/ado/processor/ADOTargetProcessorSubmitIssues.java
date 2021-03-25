/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.bugtracker.tgt.ado.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorSubmitIssues;
import com.fortify.bugtracker.tgt.ado.cli.ICLIOptionsADO;
import com.fortify.bugtracker.tgt.ado.config.ADOTargetConfiguration;
import com.fortify.bugtracker.tgt.ado.connection.ADOConnectionFactory;
import com.fortify.bugtracker.tgt.ado.connection.ADORestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;

/**
 * This {@link AbstractTargetProcessorSubmitIssues} implementation
 * submits issues to ADO.
 */
@Component
public class ADOTargetProcessorSubmitIssues extends AbstractTargetProcessorSubmitIssues {
	private String workItemType;
	
	@Override
	public void addTargetCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		ADOConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
		cliOptionDefinitions.add(ICLIOptionsADO.CLI_ADO_COLLECTION);
		cliOptionDefinitions.add(ICLIOptionsADO.CLI_ADO_PROJECT);
	}
	
	public String getTargetName() {
		return "ADO";
	}
	
	@Override
	protected TargetIssueLocator submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		ADORestConnection conn = ADOConnectionFactory.getConnection(context);
		issueData.put("System.Title", StringUtils.abbreviate((String)issueData.get("System.Title"), 254));
		return conn.submitIssue(
			ICLIOptionsADO.CLI_ADO_COLLECTION.getValue(context), 
			ICLIOptionsADO.CLI_ADO_PROJECT.getValue(context), getWorkItemType(), issueData);
	}
	
	@Override
	protected ITargetIssueFieldsRetriever getTargetIssueFieldsRetriever() {
		return new ITargetIssueFieldsRetriever() {
			public JSONMap getIssueFieldsFromTarget(Context context, TargetIssueLocator targetIssueLocator) {
				return ADOConnectionFactory.getConnection(context)
						.getWorkItemFields(ICLIOptionsADO.CLI_ADO_COLLECTION.getValue(context), targetIssueLocator);
			}
		};
	}

	public String getWorkItemType() {
		return workItemType;
	}

	public void setWorkItemType(String workItemType) {
		this.workItemType = workItemType;
	}
	
	@Autowired
	public void setConfiguration(ADOTargetConfiguration config) {
		setWorkItemType(config.getWorkItemType());
	}
	
}
