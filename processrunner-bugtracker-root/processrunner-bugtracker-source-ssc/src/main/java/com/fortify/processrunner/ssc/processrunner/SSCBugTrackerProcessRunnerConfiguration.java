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
