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
package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.map.MapBuilder;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterPutValuesFromExpressionMap;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This abstract class allows for building a object maps from grouped objects.
 * Objects are grouped by our parent {@link AbstractProcessorGroupByExpressions}
 * class. In our {@link #processGroup(Context, String, List)} method we build an
 * object map for each entry in the current group, and then call 
 * {@link #processMap(Context, String, List, LinkedHashMap)} to allow concrete implementations 
 * to further process the object maps.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractProcessorBuildObjectMapsFromGroupedObjects extends AbstractProcessorGroupByExpressions {
	private LinkedHashMap<String,TemplateExpression> fields;
	
	/**
	 * Generate a list of {@link Map} instance for every object in the current group,
	 * and then call {@link #processMaps(Context, String, List, List)} to further
	 * process the list of generated {@link Map} instances.
	 */
	@Override
	protected boolean processGroup(Context context, String groupName, List<Object> currentGroup) {
		StandardEvaluationContext sec = ContextSpringExpressionUtil.createStandardEvaluationContext(context);
		List<LinkedHashMap<String, Object>> listOfMaps = new ArrayList<LinkedHashMap<String,Object>>(currentGroup.size());
		for ( Object entry : currentGroup ) {
			listOfMaps.add( new MapBuilder()
					.addMapUpdater(new MapUpdaterPutValuesFromExpressionMap(sec, entry, getFields()))
					.build(new LinkedHashMap<String, Object>()) );
		}
		return processMaps(context, groupName, currentGroup, listOfMaps);
	}
	
	/**
	 * Method to be implemented by subclasses to process the generated {@link Map} instances for the group currently
	 * being processed.
	 * @param context
	 * @param groupName
	 * @param currentGroup
	 * @param listOfMaps
	 * @return
	 */
	protected abstract boolean processMaps(Context context, String groupName, List<Object> currentGroup, List<LinkedHashMap<String, Object>> listOfMaps);

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

}
