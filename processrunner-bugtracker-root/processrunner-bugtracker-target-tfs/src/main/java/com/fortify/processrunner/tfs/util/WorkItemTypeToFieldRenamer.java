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
package com.fortify.processrunner.tfs.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkItemTypeToFieldRenamer {
	private Map<String,Map<String,String>> workItemTypeToFieldRenameMap;
	
	public final void renameFields(String workItemType, LinkedHashMap<String, Object> map) {
		if ( workItemTypeToFieldRenameMap != null ) {
			Map<String, String> renameMap = workItemTypeToFieldRenameMap.get(workItemType);
			if ( renameMap != null ) {
				for ( Map.Entry<String, String> renameEntry : renameMap.entrySet() ) {
					Object value = map.remove(renameEntry.getKey());
					if ( value != null ) {
						map.put(renameEntry.getValue(), value);
					}
				}
			}
		}

	}

	public Map<String,Map<String,String>> getWorkItemTypeToFieldRenameMap() {
		return workItemTypeToFieldRenameMap;
	}

	public void setWorkItemTypeToFieldRenameMap(Map<String,Map<String,String>> workItemTypeToFieldRenameMap) {
		this.workItemTypeToFieldRenameMap = workItemTypeToFieldRenameMap;
	}

	
	
	
}
