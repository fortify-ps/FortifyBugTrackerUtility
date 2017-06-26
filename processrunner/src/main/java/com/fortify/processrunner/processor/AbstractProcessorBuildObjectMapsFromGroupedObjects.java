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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.util.map.MapBuilder;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterPutValuesFromExpressionMap;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This abstract class allows for building a object maps from grouped objects.
 * Objects are grouped by our parent {@link AbstractProcessorGroupByExpressions}
 * class. In our {@link #processGroup(Context, String, List)} method we build object 
 * maps for each entry in the current group, and then call 
 * {@link #processMap(Context, String, List, LinkedHashMap)} to allow concrete implementations 
 * to further process the object maps.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractProcessorBuildObjectMapsFromGroupedObjects extends AbstractProcessorGroupByExpressions {
	private LinkedHashMap<String,TemplateExpression> fields;
	
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
	
	protected abstract boolean processMaps(Context context, String groupName, List<Object> currentGroup, List<LinkedHashMap<String, Object>> listOfMaps);

	public LinkedHashMap<String,TemplateExpression> getFields() {
		return fields;
	}

	public void setFields(LinkedHashMap<String,TemplateExpression> fields) {
		this.fields = fields;
	}

}
