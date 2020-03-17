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
package com.fortify.bugtracker.tgt.octane.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorSubmitIssues;
import com.fortify.bugtracker.tgt.octane.cli.ICLIOptionsOctane;
import com.fortify.bugtracker.tgt.octane.connection.OctaneAuthenticatingRestConnection;
import com.fortify.bugtracker.tgt.octane.connection.OctaneAuthenticatingRestConnection.OctaneSharedSpaceAndWorkspaceId;
import com.fortify.bugtracker.tgt.octane.connection.OctaneConnectionFactory;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;

/**
 * This {@link AbstractTargetProcessorSubmitIssues} implementation
 * submits issues to Octane.
 */
@Component
public class OctaneTargetProcessorSubmitIssues extends AbstractTargetProcessorSubmitIssues {
	@Override
	public void addTargetCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		OctaneConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_SHARED_SPACE_UID);
		cliOptionDefinitions.add(ICLIOptionsOctane.CLI_OCTANE_WORKSPACE_ID);
	}
	
	public String getTargetName() {
		return "Octane";
	}
	
	@Override
	protected TargetIssueLocator submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		OctaneAuthenticatingRestConnection conn = OctaneConnectionFactory.getConnection(context);
		issueData.put("name", StringUtils.abbreviate((String)issueData.get("name"), 254));
		return conn.submitIssue(
			new OctaneSharedSpaceAndWorkspaceId(ICLIOptionsOctane.CLI_OCTANE_SHARED_SPACE_UID.getValue(context), 
					ICLIOptionsOctane.CLI_OCTANE_WORKSPACE_ID.getValue(context)), issueData);
	}
	
	@Override
	protected ITargetIssueFieldsRetriever getTargetIssueFieldsRetriever() {
		return new ITargetIssueFieldsRetriever() {
			public JSONMap getIssueFieldsFromTarget(Context context, TargetIssueLocator targetIssueLocator) {
				return OctaneConnectionFactory.getConnection(context).getIssueDetails(targetIssueLocator);
			}
		};
	}
}
