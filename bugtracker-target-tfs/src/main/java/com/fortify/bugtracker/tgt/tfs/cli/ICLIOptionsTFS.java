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
package com.fortify.bugtracker.tgt.tfs.cli;

import com.fortify.processrunner.cli.CLIOptionDefinition;

public interface ICLIOptionsTFS {

	String PRP_TFS_BASE_URL = "TFSBaseUrl";
	String PRP_TFS_USER_NAME = "TFSUserName";
	String PRP_TFS_PASSWORD = "TFSPassword";
	String PRP_TFS_COLLECTION = "TFSCollection";
	String PRP_TFS_PROJECT = "TFSProject";
	
	CLIOptionDefinition CLI_TFS_BASE_URL = new CLIOptionDefinition("TFS", ICLIOptionsTFS.PRP_TFS_BASE_URL, "TFS base URL", true);
	CLIOptionDefinition CLI_TFS_USER_NAME = new CLIOptionDefinition("TFS", ICLIOptionsTFS.PRP_TFS_USER_NAME, "TFS user name", true);
	CLIOptionDefinition CLI_TFS_PASSWORD = new CLIOptionDefinition("TFS", ICLIOptionsTFS.PRP_TFS_PASSWORD, "TFS password", true).isPassword(true);
	CLIOptionDefinition CLI_TFS_COLLECTION = new CLIOptionDefinition("TFS", ICLIOptionsTFS.PRP_TFS_COLLECTION, "TFS collection containing the project to submit vulnerabilities to", true);
	CLIOptionDefinition CLI_TFS_PROJECT = new CLIOptionDefinition("TFS", ICLIOptionsTFS.PRP_TFS_PROJECT, "TFS project to submit vulnerabilities to", true);
	
}
