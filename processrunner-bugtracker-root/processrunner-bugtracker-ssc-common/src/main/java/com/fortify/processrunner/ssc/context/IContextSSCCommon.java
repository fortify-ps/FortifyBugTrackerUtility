/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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
package com.fortify.processrunner.ssc.context;

import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.processrunner.context.Context;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to SSC data like connection properties and application version names/id's.
 * 
 * @author Ruud Senden
 */
public interface IContextSSCCommon {
	public static final String PRP_SSC_BASE_URL = "SSCBaseUrl";
	public static final String PRP_SSC_AUTH_TOKEN = "SSCAuthToken";
	public static final String PRP_SSC_USER_NAME = "SSCUserName";
	public static final String PRP_SSC_PASSWORD = "SSCPassword";
	public static final String PRP_SSC_APPLICATION_VERSION_ID = "SSCApplicationVersionId";
	public static final String PRP_SSC_APPLICATION_VERSIONS = "SSCApplicationVersions";
	
	public void setSSCBaseUrl(String baseUrl);
	public String getSSCBaseUrl();
	
	public void setSSCAuthToken(String authToken);
	public String getSSCAuthToken();
	
	public void setSSCUserName(String userName);
	public String getSSCUserName();
	public void setSSCPassword(String password);
	public String getSSCPassword();
	
	public void setSSCApplicationVersionId(String applicationVersionId);
	public String getSSCApplicationVersionId();
	
	public void setSSCApplicationVersions(String applicationVersionIdsOrNames);
	public String getSSCApplicationVersions();
	
	public void setApplicationVersion(JSONMap applicationVersion);
	public JSONMap getApplicationVersion();
}
