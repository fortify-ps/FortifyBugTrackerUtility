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
package com.fortify.processrunner.ssc.appversion;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.util.json.JSONMap;

/**
 * Filter SSC application versions based on application version attributes,
 * and map application version attribute values to context properties.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionAttributeFilterAndMapper implements ISSCApplicationVersionFilter, ISSCApplicationVersionContextUpdater {
	private Map<String, String> optionalAttributeMappings = null;
	private Map<String, String> requiredAttributeMappings = null;
	
	public int getOrder() {
		return 2;
	}
	
	public boolean isApplicationVersionIncluded(Context context, JSONMap applicationVersion) {
		Map<String, List<String>> avAttributes = getApplicationVersionAttributeValuesByName(context, applicationVersion);
		return checkAttributesHaveValues(avAttributes, requiredAttributeMappings.keySet());
	}
	
	public void updateContext(Context context, JSONMap applicationVersion) {
		Map<String, List<String>> avAttributes = getApplicationVersionAttributeValuesByName(context, applicationVersion);
		addMappedAttributes(context, avAttributes, optionalAttributeMappings, false);
		addMappedAttributes(context, avAttributes, requiredAttributeMappings, true);
	}
	
	public Map<String, String> getOptionalAttributeMappings() {
		return optionalAttributeMappings;
	}

	public void setOptionalAttributeMappings(Map<String, String> optionalAttributeMappings) {
		this.optionalAttributeMappings = optionalAttributeMappings;
	}

	public Map<String, String> getRequiredAttributeMappings() {
		return requiredAttributeMappings;
	}

	public void setRequiredAttributeMappings(Map<String, String> requiredAttributeMappings) {
		this.requiredAttributeMappings = requiredAttributeMappings;
	}
	
	private void addMappedAttributes(Context context, Map<String, List<String>> avAttributes, Map<String, String> mappings, boolean required) {
		if ( mappings != null ) {
			for ( Map.Entry<String, String> entry : mappings.entrySet() ) {
				String attrName = entry.getKey();
				String ctxKey = entry.getValue();
				List<String> values = avAttributes.get(attrName);
				if ( values!=null && values.size()==1 ) {
					context.put(ctxKey, values.get(0));
				} else if ( values!=null && values.size() > 1 ) {
					throw new IllegalStateException("Cannot map application version attribute "+attrName+" containing multiple values to context property "+ctxKey);
				} else if ( required && !context.containsKey(ctxKey) ) {
					throw new IllegalStateException("Required application version attribute "+attrName+" has no value");
				}
			}
		}
	}

	private Map<String, List<String>> getApplicationVersionAttributeValuesByName(Context context, JSONMap applicationVersion) {
		return SSCConnectionFactory.getConnection(context).getApplicationVersionAttributeValuesByName(applicationVersion.get("id",String.class));
	}

	private boolean checkAttributesHaveValues(Map<String, List<String>> avAttributes, Set<String> attributes) {
		if ( attributes != null ) {
			Set<String> attributesCopy = new HashSet<String>(attributes);
			attributesCopy.removeAll(avAttributes.keySet());
			if ( !attributesCopy.isEmpty() ) {
				return false;
			}
		}
		return true;
	}
}
