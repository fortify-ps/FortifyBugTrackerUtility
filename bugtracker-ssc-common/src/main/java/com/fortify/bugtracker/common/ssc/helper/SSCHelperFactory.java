/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.bugtracker.common.ssc.helper;

import com.fortify.bugtracker.common.ssc.connection.SSCConnectionFactory;
import com.fortify.client.ssc.api.SSCAttributeDefinitionAPI;
import com.fortify.client.ssc.api.SSCAttributeDefinitionAPI.SSCAttributeDefinitionHelper;
import com.fortify.client.ssc.api.SSCCustomTagAPI;
import com.fortify.client.ssc.api.SSCCustomTagAPI.SSCCustomTagHelper;
import com.fortify.processrunner.context.Context;

/**
 * This class allows for instantiating and caching {@link SSCCustomTagHelper}
 * instances based on {@link Context} properties.
 * 
 * @author Ruud Senden
 *
 */
public final class SSCHelperFactory 
{
	public static final SSCCustomTagHelper getSSCCustomTagHelper(Context context) {
		IContextSSCHelper ctx = context.as(IContextSSCHelper.class);
		SSCCustomTagHelper result = ctx.getCustomTagHelper();
		if ( result == null ) {
			result = SSCConnectionFactory.getConnection(context).api(SSCCustomTagAPI.class).getCustomTagHelper();
			ctx.setCustomTagHelper(result);
		}
		return result;
	}

	public static final SSCAttributeDefinitionHelper getSSCAttributeDefinitionHelper(Context context) {
		IContextSSCHelper ctx = context.as(IContextSSCHelper.class);
		SSCAttributeDefinitionHelper result = ctx.getAttributeDefinitionHelper();
		if ( result == null ) {
			result = SSCConnectionFactory.getConnection(context).api(SSCAttributeDefinitionAPI.class).getAttributeDefinitionHelper();
			ctx.setAttributeDefinitionHelper(result);
		}
		return result;
	}
	
	private interface IContextSSCHelper {
		public void setCustomTagHelper(SSCCustomTagHelper customTagHelper);
		public SSCCustomTagHelper getCustomTagHelper();
		public void setAttributeDefinitionHelper(SSCAttributeDefinitionHelper attributeDefinitionHelper);
		public SSCAttributeDefinitionHelper getAttributeDefinitionHelper();
	}
}
