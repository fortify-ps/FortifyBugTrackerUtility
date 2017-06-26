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
package com.fortify.processrunner.ssc.processrunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fortify.processrunner.ProcessRunner;
import com.fortify.processrunner.ssc.processrunner.SSCBugTrackerProcessRunner.ProcessRunnerType;

/**
 * This class generated the {@link ProcessRunner} Spring beans
 * for submitting or updating external issues based on SSC vulnerabilities,
 * or both.
 * 
 * @author Ruud Senden
 *
 */
@Configuration
public class SSCBugTrackerProcessRunnerConfiguration {
	@Bean
	public SSCBugTrackerProcessRunner submitVulnerabilities() {
		return new SSCBugTrackerProcessRunner(ProcessRunnerType.SUBMIT);
	}
	
	@Bean
	public SSCBugTrackerProcessRunner submitVulnerabilitiesAndUpdateIssueState() {
		return new SSCBugTrackerProcessRunner(ProcessRunnerType.SUBMIT_AND_UPDATE);
	}
	
	@Bean
	public SSCBugTrackerProcessRunner updateIssueState() {
		return new SSCBugTrackerProcessRunner(ProcessRunnerType.UPDATE);
	}
}
