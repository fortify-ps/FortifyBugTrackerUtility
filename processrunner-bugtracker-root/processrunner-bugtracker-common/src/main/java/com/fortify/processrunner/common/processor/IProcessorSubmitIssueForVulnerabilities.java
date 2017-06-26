/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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
package com.fortify.processrunner.common.processor;

import com.fortify.processrunner.common.issue.IIssueSubmittedListener;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This interface provides the methods necessary for submitting issues for
 * vulnerabilities.
 * 
 * @author Ruud Senden
 *
 */
public interface IProcessorSubmitIssueForVulnerabilities extends IProcessor {
	/**
	 * Get the bug tracker name for this implementation
	 * @return
	 */
	public String getBugTrackerName();
	
	/**
	 * Set the {@link IIssueSubmittedListener} to be called when the bug tracker
	 * implementation has submitted an issue. This method should return true if
	 * the bug tracker implementation supports calling the {@link IIssueSubmittedListener},
	 * or false if it doesn't support this. The latter is usually only the case
	 * if the bug tracker implementation itself updates the source system with the
	 * bug details.
	 * @param issueSubmittedListener
	 * @return true if {@link IIssueSubmittedListener} is supported, false otherwise
	 */
	public boolean setIssueSubmittedListener(IIssueSubmittedListener issueSubmittedListener);
	
	public boolean isIgnorePreviouslySubmittedIssues();
}
