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
package com.fortify.bugtracker.target.tfs.context;

import com.fortify.bugtracker.common.target.context.IContextBugTracker;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to TFS properties like collection, project and work item type.
 */
public interface IContextTFS extends IContextBugTracker {
	public static final String PRP_BASE_URL = "TFSBaseUrl";
	public static final String PRP_USER_NAME = "TFSUserName";
	public static final String PRP_PASSWORD = "TFSPassword";
	
	public void setTFSBaseUrl(String baseUrl);
	public String getTFSBaseUrl();
	public void setTFSUserName(String userName);
	public String getTFSUserName();
	public void setTFSPassword(String password);
	public String getTFSPassword();
	
	public void setTFSCollection(String collection);
	public String getTFSCollection();
	
	public void setTFSProject(String project);
	public String getTFSProject();
	
	public void setTFSWorkItemType(String workItemType);
	public String getTFSWorkItemType();
}
