package com.fortify.processrunner;

import com.fortify.processrunner.RunProcessRunnerFromCLI;

/**
 * Main class for running FoDBugTrackerUtility. This simply extends 
 * {@link RunProcessRunnerFromCLI}, but provides other default
 * values for config file name and log file name.
 * 
 * @author Ruud Senden
 *
 */
public class RunProcessRunnerFoDBugTrackerUtilityMain extends RunProcessRunnerFromCLI {
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
