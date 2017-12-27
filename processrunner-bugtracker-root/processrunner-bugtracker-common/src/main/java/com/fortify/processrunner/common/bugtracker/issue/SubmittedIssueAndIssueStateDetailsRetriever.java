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
package com.fortify.processrunner.common.bugtracker.issue;

import com.fortify.processrunner.context.Context;

/**
 * Instances of this class hold information about the current {@link Context}
 * and {@link SubmittedIssue}, together with an {@link IIssueStateDetailsRetriever}
 * implementation that allows for retrieving issue state details for the submitted
 * issue.
 * 
 * @author Ruud Senden
 *
 * @param <IssueStateDetailsType>
 */
public class SubmittedIssueAndIssueStateDetailsRetriever<IssueStateDetailsType> {
	private final Context context;
	private final SubmittedIssue submittedIssue;
	private final IIssueStateDetailsRetriever<IssueStateDetailsType> issueStateDetailsRetriever;
	
	/**
	 * Constructor for setting {@link Context}, {@link SubmittedIssue} and {@link IIssueStateDetailsRetriever}
	 * @param context
	 * @param submittedIssue
	 * @param issueStateDetailsRetriever
	 */
	public SubmittedIssueAndIssueStateDetailsRetriever(Context context, SubmittedIssue submittedIssue, IIssueStateDetailsRetriever<IssueStateDetailsType> issueStateDetailsRetriever) {
		super();
		this.context = context;
		this.submittedIssue = submittedIssue;
		this.issueStateDetailsRetriever = issueStateDetailsRetriever;
	}

	/**
	 * Get the current {@link Context}
	 * @return
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Get the {@link SubmittedIssue}
	 * @return
	 */
	public SubmittedIssue getSubmittedIssue() {
		return submittedIssue;
	}

	/**
	 * Get the {@link IIssueStateDetailsRetriever}
	 * @return
	 */
	public IIssueStateDetailsRetriever<IssueStateDetailsType> getIssueStateDetailsRetriever() {
		return issueStateDetailsRetriever;
	}
	
	/**
	 * Get the issue state details from the configured {@link IIssueStateDetailsRetriever}
	 * @return
	 */
	public IssueStateDetailsType getIssueStateDetails() {
		return getIssueStateDetailsRetriever().getIssueStateDetails(getContext(), getSubmittedIssue());
	}
	
}
