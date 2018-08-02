/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.bugtracker.common.tgt.processor;

import com.fortify.bugtracker.common.processor.IProcessorWithTargetName;
import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorAndFields;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;

public abstract class AbstractTargetProcessor extends AbstractProcessorBuildObjectMapFromGroupedObjects implements IProcessorWithTargetName {
	/**
	 * This method just calls 
	 * {@link #addTargetCLIOptionDefinitions(CLIOptionDefinitions)}
	 * to allow subclasses to add additional context property definitions
	 */
	@Override
	public final void addExtraCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		addTargetCLIOptionDefinitions(cliOptionDefinitions);
	}
	
	@Override
	public abstract String getTargetName();

	/**
	 * Subclasses can override this method to add additional bug tracker related {@link CLIOptionDefinitions}
	 * @param cliOptionDefinitions
	 */
	protected void addTargetCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {}
	
	
	
	protected TargetIssueLocatorAndFields getTargetIssueLocatorAndFields(Context context, TargetIssueLocator targetIssueLocator) {
		return new TargetIssueLocatorAndFields(context, targetIssueLocator, getTargetIssueFieldsRetriever());
	}

	/**
	 * Subclasses may override this method to return an {@link ITargetIssueFieldsRetriever} instance that
	 * can be used to retrieve target issue fields.
	 * @return
	 */
	protected ITargetIssueFieldsRetriever getTargetIssueFieldsRetriever() { 
		return null;
	}

}