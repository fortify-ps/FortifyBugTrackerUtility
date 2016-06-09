package com.fortify.processrunner;

import com.fortify.processrunner.RunProcessRunner;

public class RunProcessRunnerFoDToBugTrackerMain extends RunProcessRunner {
	public static void main(String[] args) {
		new RunProcessRunnerFoDToBugTrackerMain().runProcessRunner(args);
	}
	
	@Override
	protected String getDefaultConfigFileName() {
		return "FoDToBugTracker.xml";
	}
	
	@Override
	protected String getDefaultLogFileName() {
		return "FoDToBugTracker.log";
	}
}
