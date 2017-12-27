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
package com.fortify.processrunner.context;

import java.beans.PropertyEditorSupport;

/**
 * This class allows for building {@link Context} instances from a String.
 * This is mostly used to configure a Context instance through a Spring
 * configuration file.
 * 
 * @author Ruud Senden
 *
 */
public final class ContextEditor extends PropertyEditorSupport {
	
	/**
	 * Create a new {@link Context} instance, parsed from the given String.
	 * String format should be 'key1=value1,key2=values2,...'
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		Context context = new Context();
		String[] contextPropertiesWithValues = text.split(",");
		for ( String contextPropertyWithValue : contextPropertiesWithValues ) {
			int idx = contextPropertyWithValue.indexOf('=');
			String name = contextPropertyWithValue.substring(0, idx);
			String value = contextPropertyWithValue.substring(idx+1);
			context.put(name, value);
		}
		setValue(context);
	}
}
