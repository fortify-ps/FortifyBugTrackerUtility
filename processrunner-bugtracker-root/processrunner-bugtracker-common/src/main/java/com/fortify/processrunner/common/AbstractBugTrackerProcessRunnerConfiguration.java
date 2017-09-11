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
package com.fortify.processrunner.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fortify.processrunner.common.BugTrackerProcessRunner.ProcessRunnerType;

/**
 * This Spring {@link Configuration} class instantiates {@link BugTrackerProcessRunner}
 * instances for the three available process runner types (submit, update and submit&update).
 * This is a Spring {@link Configuration} class but is not annotated as such;
 * concrete subclasses will need to add this annotation. This is to allow Spring 
 * configuration files to use component-scan on a specific source system implementation
 * (i.e. SSC or FoD). We cannot use component-scan on the root com.fortify.processrunner
 * package, since that would automatically load components from other source system
 * implementations as well.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractBugTrackerProcessRunnerConfiguration {
	@Bean
	public BugTrackerProcessRunner submitVulnerabilities() {
		return new BugTrackerProcessRunner(getSourceSystemName(), ProcessRunnerType.SUBMIT);
	}
	
	@Bean
	public BugTrackerProcessRunner submitVulnerabilitiesAndUpdateIssueState() {
		return new BugTrackerProcessRunner(getSourceSystemName(), ProcessRunnerType.SUBMIT_AND_UPDATE);
	}
	
	@Bean
	public BugTrackerProcessRunner updateIssueState() {
		return new BugTrackerProcessRunner(getSourceSystemName(), ProcessRunnerType.UPDATE);
	}
	
	protected abstract String getSourceSystemName();
}
