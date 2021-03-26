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
package com.fortify.bugtracker.tgt.ado.cli;

import com.fortify.processrunner.cli.CLIOptionDefinition;

public interface ICLIOptionsADO {

	String PRP_ADO_BASE_URL = "ADOBaseUrl";
	String PRP_ADO_USER_NAME = "ADOUserName";
	String PRP_ADO_PASSWORD = "ADOPassword";
	String PRP_ADO_PROJECT = "ADOProject";
	
	CLIOptionDefinition CLI_ADO_BASE_URL = new CLIOptionDefinition("ADO", ICLIOptionsADO.PRP_ADO_BASE_URL, "Azure DevOps base URL", true);
	CLIOptionDefinition CLI_ADO_USER_NAME = new CLIOptionDefinition("ADO", ICLIOptionsADO.PRP_ADO_USER_NAME, "Azure DevOps user name", true);
	CLIOptionDefinition CLI_ADO_PASSWORD = new CLIOptionDefinition("ADO", ICLIOptionsADO.PRP_ADO_PASSWORD, "Azure DevOps password", true).isPassword(true);
	CLIOptionDefinition CLI_ADO_PROJECT = new CLIOptionDefinition("ADO", ICLIOptionsADO.PRP_ADO_PROJECT, "Azure DevOps project to submit vulnerabilities to", true);
	
}
