package com.fortify.processrunner.fod.processrunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fortify.processrunner.fod.processrunner.FoDBugTrackerProcessRunner.ProcessRunnerType;


@Configuration
public class FoDBugTrackerProcessRunnerConfiguration {
	@Bean
	public FoDBugTrackerProcessRunner submitVulnerabilities() {
		return new FoDBugTrackerProcessRunner(ProcessRunnerType.SUBMIT);
	}
	
	@Bean
	public FoDBugTrackerProcessRunner submitVulnerabilitiesAndUpdateIssueState() {
		return new FoDBugTrackerProcessRunner(ProcessRunnerType.SUBMIT_AND_UPDATE);
	}
	
	@Bean
	public FoDBugTrackerProcessRunner updateIssueState() {
		return new FoDBugTrackerProcessRunner(ProcessRunnerType.UPDATE);
	}
}
