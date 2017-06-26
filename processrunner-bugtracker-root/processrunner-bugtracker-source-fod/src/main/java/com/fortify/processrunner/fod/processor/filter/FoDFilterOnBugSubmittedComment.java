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
package com.fortify.processrunner.fod.processor.filter;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This {@link IProcessor} implementation will perform filtering based on FoD 
 * comments that indicate that a vulnerability has been submitted to a bug 
 * tracker. If the excludeVulnerabilityWithMatchingComment flag is set to 
 * false (default), vulnerabilities will only be processed if they have a 
 * comment indicating that the vulnerability has been previously submitted to 
 * a bug tracker. If the flag is set to true, vulnerabilities will only be 
 * processed if they do not have have a comment indicating that the vulnerability 
 * has been previously submitted to a bug tracker.
 */
public class FoDFilterOnBugSubmittedComment extends FoDFilterOnComments {
	public FoDFilterOnBugSubmittedComment() {
		setFilterPatternTemplateExpression("--- Vulnerability submitted to ${BugTrackerName}.*");
	}
	
	public FoDFilterOnBugSubmittedComment(boolean excludeVulnerabilityWithMatchingComment) {
		this();
		setExcludeVulnerabilityWithMatchingComment(excludeVulnerabilityWithMatchingComment);
	}
}
