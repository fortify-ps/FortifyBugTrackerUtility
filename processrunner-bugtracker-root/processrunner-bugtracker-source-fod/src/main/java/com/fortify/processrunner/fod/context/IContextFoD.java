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
package com.fortify.processrunner.fod.context;

import com.fortify.processrunner.context.Context;
import com.fortify.util.json.JSONMap;

/**
 * This interface can be used with the {@link Context#as(Class)} method to allow
 * access to FoD data like FoD connection properties, FoD REST connection, release 
 * id and top level filter parameter values.
 */
public interface IContextFoD {
	public static final String PRP_BASE_URL = "FoDBaseUrl";
	public static final String PRP_CLIENT_ID = "FoDClientId";
	public static final String PRP_CLIENT_SECRET = "FoDClientSecret";
	public static final String PRP_TENANT = "FoDTenant";
	public static final String PRP_USER_NAME = "FoDUserName";
	public static final String PRP_PASSWORD = "FoDPassword";
	public static final String PRP_RELEASE_ID = "FoDReleaseId";
	public static final String PRP_RELEASES = "FoDReleases";
	
	public void setFoDBaseUrl(String baseUrl);
	public String getFoDBaseUrl();
	
	public void setFoDClientId(String clientId);
	public String getFoDClientId();
	public void setFoDClientSecret(String clientSecret);
	public String getFoDClientSecret();
	
	public void setFoDTenant(String tenant);
	public String getFoDTenant();
	public void setFoDUserName(String userName);
	public String getFoDUserName();
	public void setFoDPassword(String password);
	public String getFoDPassword();
	
	public void setFoDReleaseId(String releaseId);
	public String getFoDReleaseId();
	
	public void setRelease(JSONMap release);
	public String getRelease();
	
	public void setFoDReleases(String releases);
	public String getFoDReleases();
}
