/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.bugtracker.common.ssc.cli;

import com.fortify.processrunner.cli.CLIOptionDefinition;

public interface ICLIOptionsSSC {

	String PRP_SSC_BASE_URL = "SSCBaseUrl";
	String PRP_SSC_AUTH_TOKEN = "SSCAuthToken";
	String PRP_SSC_USER_NAME = "SSCUserName";
	String PRP_SSC_PASSWORD = "SSCPassword";
	String PRP_SSC_APPLICATION_VERSION_ID = "SSCApplicationVersionId";
	String PRP_SSC_APPLICATION_VERSION_NAME_PATTERNS = "SSCApplicationVersionNamePatterns";
	String PRP_SSC_BUG_TRACKER_USER_NAME = "SSCBugTrackerUserName";
	String PRP_SSC_BUG_TRACKER_PASSWORD = "SSCBugTrackerPassword";
	
	public CLIOptionDefinition CLI_SSC_BASE_URL = new CLIOptionDefinition("SSC", PRP_SSC_BASE_URL, "SSC base URL", true);
	public CLIOptionDefinition CLI_SSC_USER_NAME = new CLIOptionDefinition("SSC", PRP_SSC_USER_NAME, "SSC user name", true).isAlternativeForOptions(PRP_SSC_AUTH_TOKEN);
	public CLIOptionDefinition CLI_SSC_PASSWORD = new CLIOptionDefinition("SSC", PRP_SSC_PASSWORD, "SSC password", true).isPassword(true).dependsOnOptions(PRP_SSC_USER_NAME);
	public CLIOptionDefinition CLI_SSC_AUTH_TOKEN = new CLIOptionDefinition("SSC", PRP_SSC_AUTH_TOKEN, "SSC auth token", true).isPassword(true).isAlternativeForOptions(PRP_SSC_USER_NAME);
	public CLIOptionDefinition CLI_SSC_APPLICATION_VERSION_NAME_PATTERNS = new CLIOptionDefinition("SSC", PRP_SSC_APPLICATION_VERSION_NAME_PATTERNS, "SSC application version names (<application name pattern>:<version name pattern>), separated by comma's", false).isAlternativeForOptions(PRP_SSC_APPLICATION_VERSION_ID);
	public CLIOptionDefinition CLI_SSC_APPLICATION_VERSION_ID = new CLIOptionDefinition("SSC", PRP_SSC_APPLICATION_VERSION_ID, "SSC application version id from which to retrieve vulnerabilities", true).isAlternativeForOptions(PRP_SSC_APPLICATION_VERSION_NAME_PATTERNS).defaultValueDescription("Automatically set while loading application versions");
	
}
