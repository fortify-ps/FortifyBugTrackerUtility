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
package com.fortify.processrunner.common.bugtracker.issue;

import com.fortify.processrunner.context.Context;

public class SubmittedIssueAndIssueStateDetailsRetriever<IssueStateDetailsType> {
	private final Context context;
	private final SubmittedIssue submittedIssue;
	private final IssueStateDetailsRetriever<IssueStateDetailsType> issueStateDetailsRetriever;
	
	public SubmittedIssueAndIssueStateDetailsRetriever(Context context, SubmittedIssue submittedIssue, IssueStateDetailsRetriever<IssueStateDetailsType> issueStateDetailsRetriever) {
		super();
		this.context = context;
		this.submittedIssue = submittedIssue;
		this.issueStateDetailsRetriever = issueStateDetailsRetriever;
	}

	public Context getContext() {
		return context;
	}

	public SubmittedIssue getSubmittedIssue() {
		return submittedIssue;
	}

	public IssueStateDetailsRetriever<IssueStateDetailsType> getIssueStateDetailsRetriever() {
		return issueStateDetailsRetriever;
	}
	
	public IssueStateDetailsType getIssueState() {
		return getIssueStateDetailsRetriever().getIssueStateDetails(getContext(), getSubmittedIssue());
	}
	
}
