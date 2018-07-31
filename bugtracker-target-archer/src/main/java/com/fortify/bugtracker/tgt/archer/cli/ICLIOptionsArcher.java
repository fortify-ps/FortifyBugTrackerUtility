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
package com.fortify.bugtracker.tgt.archer.cli;

import com.fortify.processrunner.cli.CLIOptionDefinition;

public interface ICLIOptionsArcher {

	String PRP_ARCHER_BASE_URL = "ArcherBaseUrl";
	String PRP_ARCHER_INSTANCE_NAME = "ArcherInstanceName";
	String PRP_ARCHER_USER_NAME = "ArcherUserName";
	String PRP_ARCHER_USER_DOMAIN = "ArcherUserDomain";
	String PRP_ARCHER_PASSWORD = "ArcherPassword";
	String PRP_ARCHER_APPLICATION_NAME = "ArcherApplicationName";

	CLIOptionDefinition CLI_ARCHER_BASE_URL = new CLIOptionDefinition("Archer", ICLIOptionsArcher.PRP_ARCHER_BASE_URL, "Archer base URL", true);
	CLIOptionDefinition CLI_ARCHER_APPLICATION_NAME = new CLIOptionDefinition("Archer", ICLIOptionsArcher.PRP_ARCHER_APPLICATION_NAME, "Archer application name", true);
	CLIOptionDefinition CLI_ARCHER_INSTANCE_NAME = new CLIOptionDefinition("Archer", ICLIOptionsArcher.PRP_ARCHER_INSTANCE_NAME, "Archer instance name", true);
	CLIOptionDefinition CLI_ARCHER_USER_NAME = new CLIOptionDefinition("Archer", ICLIOptionsArcher.PRP_ARCHER_USER_NAME, "Archer user name", true);
	CLIOptionDefinition CLI_ARCHER_USER_DOMAIN = new CLIOptionDefinition("Archer", ICLIOptionsArcher.PRP_ARCHER_USER_DOMAIN, "Archer user domain", false);
	CLIOptionDefinition CLI_ARCHER_PASSWORD = new CLIOptionDefinition("Archer", ICLIOptionsArcher.PRP_ARCHER_PASSWORD, "Archer password", true).isPassword(true);
}
