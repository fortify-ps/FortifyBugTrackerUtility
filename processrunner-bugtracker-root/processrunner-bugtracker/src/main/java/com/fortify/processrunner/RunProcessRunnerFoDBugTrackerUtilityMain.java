package com.fortify.processrunner;

import com.fortify.processrunner.RunProcessRunner;

public class RunProcessRunnerFoDBugTrackerUtilityMain extends RunProcessRunner {
	public static void main(String[] args) {
		new RunProcessRunnerFoDBugTrackerUtilityMain().runProcessRunner(args);
	}
	
	@Override
	protected String getDefaultConfigFileName() {
		return "FoDBugTrackerUtility.xml";
	}
	
	@Override
	protected String getDefaultLogFileName() {
		return "FoDBugTrackerUtility.log";
	}
}
