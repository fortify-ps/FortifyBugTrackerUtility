package com.fortify.processrunner.ssc.appversion;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;

/**
 * Filter SSC application versions based on application version attributes.
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionFilterAttributeValue extends AbstractSSCApplicationVersionFilter {
	private Set<String> attributesWithAnyValue = null;
	private Set<String> attributesWithoutAnyValue = null;
	private Map<String, Collection<String>> attributesWithAllValues = null;
	
	public int getOrder() {
		return 2;
	}

	@Override
	public boolean isApplicationVersionMatching(Context context, String applicationVersionId, JSONObject applicationVersion) {
		Map<String, List<String>> avAttributes = SSCConnectionFactory.getConnection(context).getApplicationVersionAttributeValuesByName(applicationVersionId);
		boolean result = checkAttributesExist(avAttributes, attributesWithAnyValue);
		result = result && checkAttributesExist(avAttributes, attributesWithoutAnyValue);
		result = result && checkAttributesExist(avAttributes, attributesWithAllValues==null?null:attributesWithAllValues.keySet());
		if ( result ) {
			for ( Map.Entry<String, List<String>> avAttribute : avAttributes.entrySet() ) {
				result &= matchAttributesWithAnyValue(avAttribute)
						&& matchAttributesWithoutAnyValue(avAttribute)
						&& matchAttributesWithAllValues(avAttribute);
				if ( !result ) { break; }
			}
		}
		return result;
	}

	private boolean checkAttributesExist(Map<String, List<String>> avAttributes, Set<String> attributes) {
		if ( attributes != null ) {
			Set<String> attributesCopy = new HashSet<String>(attributes);
			attributesCopy.removeAll(avAttributes.keySet());
			if ( !attributesCopy.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	private boolean matchAttributesWithAnyValue(Entry<String, List<String>> avAttribute) {
		return attributesWithAnyValue!=null && attributesWithAnyValue.contains(avAttribute.getKey()) 
				? !avAttribute.getValue().isEmpty() 
				: true;
	}

	private boolean matchAttributesWithoutAnyValue(Entry<String, List<String>> avAttribute) {
		return attributesWithoutAnyValue!=null && attributesWithoutAnyValue.contains(avAttribute.getKey()) 
				? avAttribute.getValue().isEmpty() 
				: true;
	}

	private boolean matchAttributesWithAllValues(Entry<String, List<String>> avAttribute) {
		return attributesWithAllValues!=null && attributesWithAllValues.containsKey(avAttribute.getKey()) 
				? avAttribute.getValue().containsAll(attributesWithAllValues.get(avAttribute.getKey())) 
				: true;
	}

	public Set<String> getAttributesWithAnyValue() {
		return attributesWithAnyValue;
	}

	public void setAttributesWithAnyValue(Set<String> attributesWithAnyValue) {
		this.attributesWithAnyValue = attributesWithAnyValue;
	}

	public Set<String> getAttributesWithoutAnyValue() {
		return attributesWithoutAnyValue;
	}

	public void setAttributesWithoutAnyValue(Set<String> attributesWithoutAnyValue) {
		this.attributesWithoutAnyValue = attributesWithoutAnyValue;
	}

	public Map<String, Collection<String>> getAttributesWithAllValues() {
		return attributesWithAllValues;
	}

	public void setAttributesWithAllValues(Map<String, Collection<String>> attributesWithAllValues) {
		this.attributesWithAllValues = attributesWithAllValues;
	}
}
