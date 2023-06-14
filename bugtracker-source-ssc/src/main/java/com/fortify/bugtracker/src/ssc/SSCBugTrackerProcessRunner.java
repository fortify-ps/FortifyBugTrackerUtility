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
package com.fortify.bugtracker.src.ssc;

import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.AbstractBugTrackerProcessRunner;
import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.bugtracker.common.ssc.context.IContextSSCCommon;
import com.fortify.processrunner.context.Context;

/**
 * SSC-specific Spring configuration class; see {@link AbstractBugTrackerProcessRunner}
 * for more details.
 * 
 * @author Ruud Senden
 *
 */
@Component
public class SSCBugTrackerProcessRunner extends AbstractBugTrackerProcessRunner {
	@Override
	protected String getSourceName() {
		return "SSC";
	}
	
	@Override
	public void close(Context context) {
		SSCConnectionFactory.closeConnection(context);
	}
	
	@Override
    protected String getProcessingMessage(Context context) {
        IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
        return String.format("[SSC] Processing application version: %s", ctx.getSSCApplicationAndVersionName());
    }
}
