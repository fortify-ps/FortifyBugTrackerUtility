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
package com.fortify.processrunner.processor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.api.util.spring.expression.TemplateExpression;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.map.MapBuilder;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterAppendValuesFromExpressionMap;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterPutValuesFromExpressionMap;

/**
 * <p>This abstract class allows for building an object map from grouped objects.
 * Objects are grouped by our parent {@link AbstractProcessorGroupByExpressions}
 * class. In our {@link #processGroup(Context, List)} method we build an object 
 * map for the current group, and then call {@link #processMap(Context, List, LinkedHashMap)}
 * to allow concrete implementation to further process the object map.</p>
 * 
 * <p>The {@link Map} will initially be built based on the first object in the group
 * and the configured key/expression pairs configured via {@link #setFields(LinkedHashMap)}.
 * Then, for every object in the group, values for fields configured through 
 * {@link #setAppendedFields(LinkedHashMap)} will be appended to existing map values.</p> 
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractProcessorBuildObjectMapFromGroupedObjects extends AbstractProcessorGroupByExpressions {
	private LinkedHashMap<String,TemplateExpression> fields;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	
	/**
	 * Build a {@link Map} for the current group, based on the configured {@link #fields} and {@link #appendedFields},
	 * and then call the {@link #processMap(Context, List, LinkedHashMap)} method to allow the {@link Map} to be
	 * further processed.
	 */
	@Override
	protected boolean processGroup(Context context, String groupName, List<Object> currentGroup) {
		StandardEvaluationContext sec = ContextSpringExpressionUtil.createStandardEvaluationContext(context);
		LinkedHashMap<String, Object> map = new MapBuilder()
				.addMapUpdater(new MapUpdaterPutValuesFromExpressionMap(sec, currentGroup.get(0), getFields()))
				.addMapUpdater(new MapUpdaterAppendValuesFromExpressionMap(sec, currentGroup, getAppendedFields()))
				.build(new LinkedHashMap<String, Object>());
		return processMap(context, currentGroup, map);
	}
	
	/**
	 * Method to be implemented by subclasses to process the generated {@link Map} for the group currently
	 * being processed.
	 * @param context
	 * @param currentGroup
	 * @param map
	 * @return
	 */
	protected abstract boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map);

	/**
	 * Get the configured field names to be added to the generated {@link Map}, together with {@link TemplateExpression}
	 * instances used to generate the corresponding {@link Map} values.
	 * @return
	 */
	public LinkedHashMap<String,TemplateExpression> getFields() {
		return fields;
	}

	/**
	 * Set the configured field names to be added to the generated {@link Map}, together with {@link TemplateExpression}
	 * instances used to generate the corresponding {@link Map} values.
	 * @return
	 */
	public void setFields(LinkedHashMap<String,TemplateExpression> fields) {
		this.fields = fields;
	}

	/**
	 * Get the configured field names to be added/updated in the generated {@link Map}, together with {@link TemplateExpression}
	 * instances used to generate the corresponding {@link Map} values to be appended to existing {@link Map} values.
	 * @return
	 */
	public LinkedHashMap<String,TemplateExpression> getAppendedFields() {
		return appendedFields;
	}

	/**
	 * Set the configured field names to be added/updated in the generated {@link Map}, together with {@link TemplateExpression}
	 * instances used to generate the corresponding {@link Map} values to be appended to existing {@link Map} values.
	 * @return
	 */
	public void setAppendedFields(LinkedHashMap<String,TemplateExpression> appendedFields) {
		this.appendedFields = appendedFields;
	}

}
