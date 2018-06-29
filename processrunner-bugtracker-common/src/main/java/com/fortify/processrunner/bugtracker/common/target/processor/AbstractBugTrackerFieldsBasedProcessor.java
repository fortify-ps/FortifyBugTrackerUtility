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
package com.fortify.processrunner.bugtracker.common.target.processor;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.bugtracker.common.target.config.BugTrackerFieldConfiguration;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This abstract class allows for configuring bug tracker fields and their contents to be processed.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractBugTrackerFieldsBasedProcessor extends AbstractProcessorBuildObjectMapFromGroupedObjects {
	
	/**
	 * Autowire the bug tracker field configuration from the Spring configuration file. Depending on
	 * the return value of {@link #includeOnlyFieldsToBeUpdated()}, we either configure all fields (for 
	 * initial bug submission), or only fields to be updated (for state management)
	 * 
	 * @param bugTrackerFieldConfiguration
	 */
	@Autowired
	public void setBugTrackerFieldConfiguration(BugTrackerFieldConfiguration bugTrackerFieldConfiguration) {
		if ( !includeOnlyFieldsToBeUpdated() ) {
			super.setFields(bugTrackerFieldConfiguration.getFields());
			super.setAppendedFields(bugTrackerFieldConfiguration.getAppendedFields());
		} else {
			String[] fieldsToUpdate = bugTrackerFieldConfiguration.getFieldsToUpdateDuringStateManagement();
			if ( fieldsToUpdate!=null && fieldsToUpdate.length>0 ) {
				setFields(getFilteredMap(bugTrackerFieldConfiguration.getFields(), fieldsToUpdate));
				setAppendedFields(getFilteredMap(bugTrackerFieldConfiguration.getAppendedFields(), fieldsToUpdate));
			}
		}
		
	}
	
	/**
	 * Utility method for filtering an input map based on the given array of keys.
	 * @param inputMap
	 * @param keys
	 * @return
	 */
	private LinkedHashMap<String, TemplateExpression> getFilteredMap(LinkedHashMap<String, TemplateExpression> inputMap, String[] keys) {
		LinkedHashMap<String, TemplateExpression> result = new LinkedHashMap<String, TemplateExpression>(keys.length);
		for ( String key : keys ) {
			result.put(key, inputMap.get(key));
		}
		return result;
	}
	
	/**
	 * Subclasses need to implement this method to identify whether all bug tracker fields
	 * should be configured on this instance, or only bug tracker fields to be updated.
	 * @return
	 */
	protected abstract boolean includeOnlyFieldsToBeUpdated();
}
