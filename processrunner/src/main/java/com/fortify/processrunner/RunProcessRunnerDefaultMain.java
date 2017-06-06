package com.fortify.processrunner;

/**
 * This class provides the main method for running a {@link ProcessRunner}-based
 * utility. It simply creates and invokes a new instance of {@link RunProcessRunnerFromCLI}.
 * 
 * @author Ruud Senden
 *
 */
public class RunProcessRunnerDefaultMain extends RunProcessRunnerFromCLI {
	public static final void main(String[] args) {
		new RunProcessRunnerDefaultMain().runProcessRunner(args);
	}
}
