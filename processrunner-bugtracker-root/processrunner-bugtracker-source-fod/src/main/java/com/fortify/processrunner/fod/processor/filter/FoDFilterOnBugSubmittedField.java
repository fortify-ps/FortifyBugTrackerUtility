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
 * This {@link IProcessor} implementation will perform filtering based on the
 * FoD bugSubmitted field. Only if the bugSubmitted field matches the configured
 * filterValue ("true" or "false"), the vulnerability will be processed.
 */
public class FoDFilterOnBugSubmittedField extends FoDFilterOnTopLevelField {
	private static final String FIELD_NAME = "bugSubmitted"; 
	public FoDFilterOnBugSubmittedField() {
		super(FIELD_NAME);
	}
	
	public FoDFilterOnBugSubmittedField(String filterValue) {
		super(FIELD_NAME, filterValue);
	}
}
