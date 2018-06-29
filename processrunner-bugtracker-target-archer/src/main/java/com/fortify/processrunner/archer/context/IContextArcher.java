/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.processrunner.archer.context;

import com.fortify.processrunner.bugtracker.common.target.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to Archer properties.
 */
public interface IContextArcher extends IContextBugTracker {
	public static final String PRP_BASE_URL = "ArcherBaseUrl";
	public static final String PRP_INSTANCE_NAME = "ArcherInstanceName";
	public static final String PRP_USER_NAME = "ArcherUserName";
	public static final String PRP_USER_DOMAIN = "ArcherUserDomain";
	public static final String PRP_PASSWORD = "ArcherPassword";
	public static final String PRP_APPLICATION_NAME = "ArcherApplicationName";
	
	public void setArcherBaseUrl(String baseUrl);
	public String getArcherBaseUrl();
	public void setArcherApplicationName(String applicationName);
	public String getArcherApplicationName();
	public void setArcherInstanceName(String instanceName);
	public String getArcherInstanceName();
	public void setArcherUserName(String userName);
	public String getArcherUserName();
	public void setArcherUserDomain(String userDomain);
	public String getArcherUserDomain();
	public void setArcherPassword(String password);
	public String getArcherPassword();
}
