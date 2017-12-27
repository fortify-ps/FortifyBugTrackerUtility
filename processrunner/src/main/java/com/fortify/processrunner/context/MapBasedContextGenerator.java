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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * <p>This implementation of {@link IContextGenerator} allows for 
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
 * with the corresponding context properties.
 *  
 * @author Ruud Senden
 *
 */
public class MapBasedContextGenerator implements IContextGenerator {
	private Map<Object, Context> contexts = new HashMap<Object, Context>();
	private String contextPropertyName;
	
	public Collection<Context> generateContexts(Context initialContext) {
		Object contextPropertyValue = initialContext.get(contextPropertyName);
		if ( contextPropertyValue==null || (contextPropertyValue instanceof String && StringUtils.isBlank((String)contextPropertyValue)) ) {
			return mergeContextsWithInitialContext(initialContext, contexts.values());
		} else {
			return mergeContextsWithInitialContext(initialContext, Arrays.asList(contexts.get(contextPropertyValue)));
		}
	}

	private Collection<Context> mergeContextsWithInitialContext(Context initialContext, Collection<Context> contextsToMerge) {
		Collection<Context> result = new ArrayList<Context>(contexts.size());
		for ( Context contextToMerge : contextsToMerge ) {
			Context context = new Context(initialContext);
			context.putAll(contextToMerge);
			result.add(context);
		}
		return result;
	}

	public Map<Object, Context> getContexts() {
		return contexts;
	}

	@Required
	public void setContexts(Map<Object, Context> contexts) {
		this.contexts = contexts;
	}

	public String getContextPropertyName() {
		return contextPropertyName;
	}

	@Required
	public void setContextPropertyName(String contextPropertyName) {
		this.contextPropertyName = contextPropertyName;
	}
}
