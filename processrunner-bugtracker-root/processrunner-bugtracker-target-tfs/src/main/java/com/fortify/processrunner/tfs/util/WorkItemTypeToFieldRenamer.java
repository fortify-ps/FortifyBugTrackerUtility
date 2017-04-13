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
