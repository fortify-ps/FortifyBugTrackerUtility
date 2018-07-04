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
package com.fortify.bugtracker.common.tgt.issue;

import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;

/**
 * Instances of this class provide access to the target issue locator and issue
 * fields, based on the configured {@link Context}, {@link TargetIssueLocator}
 * and {@link ITargetIssueFieldsRetriever}. Note that issue fields retrieved 
 * from {@link ITargetIssueFieldsRetriever} are being cached. The {@link #resetFields()}
 * method can be called to refresh the cached fields.
 * 
 * @author Ruud Senden
 */
public final class TargetIssueLocatorAndFields {
	private final Context context;
	private final TargetIssueLocator targetIssueLocator;
	private final ITargetIssueFieldsRetriever targetIssueFieldsRetriever;
	private JSONMap fields = null; // Loaded on-demand
	
	/**
	 * Constructor for setting {@link Context}, {@link TargetIssueLocator} and {@link ITargetIssueFieldsRetriever}
	 * @param context
	 * @param targetIssueLocator
	 * @param targetIssueFieldsRetriever
	 */
	public TargetIssueLocatorAndFields(Context context, TargetIssueLocator targetIssueLocator, ITargetIssueFieldsRetriever targetIssueFieldsRetriever) {
		this.context = context;
		this.targetIssueLocator = targetIssueLocator;
		this.targetIssueFieldsRetriever = targetIssueFieldsRetriever;
	}
	
	public boolean canRetrieveFields() {
		return targetIssueFieldsRetriever!=null;
	}
	
	/**
	 * This method can be called to reset
	 */
	public void resetFields() {
		this.fields = null;
	}

	/**
	 * Get the {@link TargetIssueLocator}
	 * @return
	 */
	public final TargetIssueLocator getLocator() {
		return targetIssueLocator;
	}
	
	/**
	 * Get the issue details from the configured {@link ITargetIssueFieldsRetriever}
	 * @return
	 */
	public synchronized JSONMap getFields() {
		if ( fields == null ) {
			fields = targetIssueFieldsRetriever==null ? new JSONMap() : targetIssueFieldsRetriever.getIssueFieldsFromTarget(context, getLocator());
		}
		return fields;
	}
	
}
