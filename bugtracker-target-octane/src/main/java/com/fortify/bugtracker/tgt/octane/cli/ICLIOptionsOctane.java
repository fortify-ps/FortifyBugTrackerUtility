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
package com.fortify.bugtracker.tgt.octane.cli;

import com.fortify.processrunner.cli.CLIOptionDefinition;

public interface ICLIOptionsOctane {

	String PRP_OCTANE_BASE_URL = "OctaneBaseUrl";
	String PRP_OCTANE_USER_NAME = "OctaneUserName";
	String PRP_OCTANE_PASSWORD = "OctanePassword";
	String PRP_OCTANE_CLIENT_ID = "OctaneClientId";
	String PRP_OCTANE_CLIENT_SECRET = "OctaneClientSecret";
	String PRP_OCTANE_WORKSPACE_ID = "OctaneWorkspaceId";
	String PRP_OCTANE_SHARED_SPACE_UID = "OctaneSharedSpaceUid";

	CLIOptionDefinition CLI_OCTANE_BASE_URL = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_BASE_URL, "Octane base URL", true);
	CLIOptionDefinition CLI_OCTANE_USER_NAME = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_USER_NAME, "Octane user name", true).isAlternativeForOptions(ICLIOptionsOctane.PRP_OCTANE_CLIENT_ID);
	CLIOptionDefinition CLI_OCTANE_PASSWORD = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_PASSWORD, "Octane password", true).isPassword(true).dependsOnOptions(ICLIOptionsOctane.PRP_OCTANE_USER_NAME);
	CLIOptionDefinition CLI_OCTANE_CLIENT_ID = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_CLIENT_ID, "Octane client id", true).isAlternativeForOptions(ICLIOptionsOctane.PRP_OCTANE_USER_NAME);
	CLIOptionDefinition CLI_OCTANE_CLIENT_SECRET = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_CLIENT_SECRET, "Octane client secret", true).isPassword(true).dependsOnOptions(ICLIOptionsOctane.PRP_OCTANE_CLIENT_ID);
	CLIOptionDefinition CLI_OCTANE_SHARED_SPACE_UID = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_SHARED_SPACE_UID, "Octane Shared Space UID", true);
	CLIOptionDefinition CLI_OCTANE_WORKSPACE_ID = new CLIOptionDefinition("Octane", ICLIOptionsOctane.PRP_OCTANE_WORKSPACE_ID, "Octane Workspace ID", true);
	
}
