/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
package com.fortify.bugtracker.tgt.jira.cli;

import com.fortify.processrunner.cli.CLIOptionDefinition;

public interface ICLIOptionsJira {

	String PRP_JIRA_BASE_URL = "JiraBaseUrl";
	String PRP_JIRA_USER_NAME = "JiraUserName";
	String PRP_JIRA_PASSWORD = "JiraPassword";
	String PRP_JIRA_PROJECT_KEY = "JiraProjectKey";
	
	CLIOptionDefinition CLI_JIRA_BASE_URL = new CLIOptionDefinition("Jira", ICLIOptionsJira.PRP_JIRA_BASE_URL, "JIRA base URL", true);
	CLIOptionDefinition CLI_JIRA_USER_NAME = new CLIOptionDefinition("Jira", ICLIOptionsJira.PRP_JIRA_USER_NAME, "JIRA user name", true);
	CLIOptionDefinition CLI_JIRA_PASSWORD = new CLIOptionDefinition("Jira", ICLIOptionsJira.PRP_JIRA_PASSWORD, "JIRA password", true).isPassword(true);
	CLIOptionDefinition CLI_JIRA_PROJECT_KEY = new CLIOptionDefinition("Jira", ICLIOptionsJira.PRP_JIRA_PROJECT_KEY, "JIRA project key identifying the JIRA project to submit vulnerabilities to", true);
}
