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
package com.fortify.processrunner.context;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This implementation of {@link AbstractContextGeneratorAndUpdater} allows for 
 * configuring a static Map of values for a configured context property name with their
 * corresponding dependent context properties.</p>
 * 
 * For example:
 * <ul>
 *  <li>Configured context property name: SomeIdProperty
 *  <li>Map contents:
 *    <ul>
 *      <li>key=1, value="DependentProperty1=SomeValue1,DependentProperty2=SomeValue2"</li>
 *      <li>key=1, value="DependentProperty1=SomeOtherValue1,DependentProperty2=SomeOtherValue2"</li>
 *    </ul>
 *  </li>
 * </ul>
 * If the user specified value 1 for SomeIdProperty, the corresponding values for DependentProperty1
 * and DependentProperty2 will be added to the {@link Context}. If the user didn't generate a value
 * for SomeIdProperty, this class will provide default values 1 and 2 for SomeIdProperty, together
 * with the corresponding context properties, assuming the {@link #useForDefaultValueGeneration}
 * property is set to true.
 *  
 * @author Ruud Senden
 *
 */
public class MapBasedContextGeneratorAndUpdater extends AbstractContextGeneratorAndUpdater {
	private Map<Object, Context> contexts = new HashMap<Object, Context>();

	protected void addMappedContextProperties(Context context, Object contextPropertyValue) {
		Map<String, Object> mappedContextProperties = getContexts().get(contextPropertyValue);
		if ( mappedContextProperties!=null ) {
			context.putAll(mappedContextProperties);
		}
	}
	
	@Override
	protected Map<Object, Context> getDefaultValuesWithMappedContextProperties(Context initialContext) {
		return getContexts();
	}

	public Map<Object, Context> getContexts() {
		return contexts;
	}

	public void setContexts(Map<Object, Context> contexts) {
		this.contexts = contexts;
	}
}
