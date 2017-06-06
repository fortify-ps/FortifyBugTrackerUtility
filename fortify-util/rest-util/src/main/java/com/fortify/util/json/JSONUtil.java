package com.fortify.util.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class provides various JSON-related utility methods. Note that each method must have
 * a unique name in order to be used in SpEL expressions. As such, this class must not define
 * any overloaded methods, unless those methods are not used in SpEL expressions.
 */
public final class JSONUtil {
	private JSONUtil() {}
	
	/**
	 * Convert the given JSONArray to a list. If the given JSONArray is null, this 
	 * method will return null.
	 * @param array
	 * @return A {@link List} containing all values from the given {@link JSONArray}
	 */
	public static final <R> List<R> objectArrayToList(JSONArray array, Class<R> listValueType) {
		List<R> result = null;
		if ( array != null ) {
			result = new ArrayList<R>();
			for(int i=0; i<array.length(); i++){
				CollectionUtils.addIgnoreNull(result, array.opt(i) );
			}
		}
		return result;
	}
	
	/**
	 * Convert the given JSONArray to a list. The entries in the list will be
	 * calculated based on evaluating the given listValueExpression against each
	 * JSONObject within the JSONArray. If the given JSONArray is null, this 
	 * method will return null.
	 * @param array
	 * @param listValueExpression
	 * @param listValueType
	 * @return A list containing containing the result of evaluating the given 
	 * 		listValueExpression on every object contained in the given {@link JSONArray}
	 */
	public static final <R> List<R> jsonObjectArrayToList(JSONArray array, String listValueExpression, Class<R> listValueType) {
		List<R> result = null;
		if ( array != null ) {
			result = new ArrayList<R>();
			for(int i=0; i<array.length(); i++){
				JSONObject value = array.optJSONObject(i);
				CollectionUtils.addIgnoreNull(result, SpringExpressionUtil.evaluateExpression(value, listValueExpression, listValueType) );
			}
		}
		return result;
	}
	
	/**
	 * Same as {@link #jsonObjectArrayToList(JSONArray, String, Class)}, but first filters the provided
	 * JSONArray using the provided matchExpression and matchValue. See {@link #filter(JSONArray, String, Object)}
	 * for more information.
	 * @param array
	 * @param listValueExpression
	 * @param listValueType
	 * @param matchExpression
	 * @param matchValue
	 * @return @return A list containing containing the result of evaluating the given 
	 * 		listValueExpression on every object contained in the given {@link JSONArray}
	 * 		for which the given matchExpression matches the given matchValue
	 */
	public static final <R> List<R> filteredJsonObjectArrayToList(JSONArray array, String listValueExpression, Class<R> listValueType, String matchExpression, Object matchValue) {
		return jsonObjectArrayToList(filter(array, matchExpression, matchValue), listValueExpression, listValueType);
	}
	
	/**
	 * Given a JSONArray, this method will return a new JSONArray containing all JSONObjects from the original array
	 * for which the given SpEL matchExpression matches the given matchValue.
	 * @param array The JSONArray to search
	 * @param matchExpression The SpEL expression used for matching 
	 * @param matchValue The value to match against the matchExpression result
	 * @return A copy of the given JSONArray, with all values values removed
	 * 		for which the given matchExpression doesn't match the given matchValue
	 */
	public static final JSONArray filter(JSONArray array, String matchExpression, Object matchValue) {
		JSONArray result = null;
		if ( array != null ) {
			result = new JSONArray();
			for(int i=0; i<array.length(); i++){
				JSONObject obj = array.optJSONObject(i);
				if ( isMatching(obj, matchExpression, matchValue) ) {
					result.put( obj );
				}
			}
		}
		return result;
	}
	
	/**
	 * For all JSONObjects in the given matchArray, the given matchArrayExpression will be evaluated to
	 * generate a set of match values. Then, for every JSONObject contained in the given sourceArray,
	 * the given sourceMatchExpression is evaluated. If the sourceMatchExpression evaluation result
	 * matches any of the match values, then the corresponding JSONObject from the sourceArray will
	 * be included in the filtered result.
	 * @param sourceArray The JSONArray to search
	 * @param sourceMatchExpression The SpEL expression used for matching elements from the sourceArray
	 * @param matchArray The JSONArray to match against
	 * @param matchArrayExpression The SpEL expression used to generate the set of match values from matchArray
	 * @param expressionType The object type as returned by both sourceMatchExpression and matchArrayExpression
	 */
	public static final <T> JSONArray filterByOtherArray(JSONArray sourceArray, String sourceMatchExpression, JSONArray matchArray, String matchArrayExpression, Class<T> expressionType) {
		List<T> matchValuesList = jsonObjectArrayToList(matchArray, matchArrayExpression, expressionType);
		Set<T> matchValues = matchValuesList==null ? new HashSet<T>() : new HashSet<T>(matchValuesList);
		JSONArray result = null;
		if ( sourceArray != null ) {
			result = new JSONArray();
			for(int i=0; i<sourceArray.length(); i++){
				JSONObject obj = sourceArray.optJSONObject(i);
				if ( isMatching(obj, sourceMatchExpression, matchValues, expressionType) ) {
					result.put( obj );
				}
			}
		}
		return result;
	}
	
	/**
	 * Given a JSONArray, this method will search the array for an object for which the given
	 * SpEL matchExpression matches the given matchValue. On this matched object, the given
	 * SpEL returnExpression is evaluated and the result is returned.
	 * @param array The JSONArray to search
	 * @param matchExpression The SpEL expression used for matching 
	 * @param matchValue The value to match against the matchExpression result
	 * @param returnExpression The SpEL expression used to calculate the return value
	 * @return The evaluation result for returnExpression on the {@link JSONObject} in the
	 * 		given {@link JSONArray} for which the given matchExpression matches the given
	 * 		matchValue.
	 */
	public static final <M,R> R mapValue(JSONArray array, String matchExpression, M matchValue, String returnExpression, Class<R> returnType) {
		JSONObject obj = findJSONObject(array, matchExpression, matchValue);
		return SpringExpressionUtil.evaluateExpression(obj, returnExpression, returnType);
	}
	
	/**
	 * Given a JSONArray, this method will search the array for an object for which the given
	 * SpEL matchExpression matches the given matchValue.
	 * @param array The JSONArray to search
	 * @param matchExpression The SpEL expression used for matching 
	 * @param matchValue The value to match against the matchExpression result
	 * @return The {@link JSONObject} from the given {@link JSONArray} for which
	 * 		the given matchExpression matches the given matchValue
	 */
	public static final JSONObject findJSONObject(JSONArray array, String matchExpression, Object matchValue) {
		if ( array == null || matchExpression == null ) { return null; }
		for(int i=0; i<array.length(); i++){
			JSONObject obj = array.optJSONObject(i);
			if ( isMatching(obj, matchExpression, matchValue) ) {
				return obj;
			}
		}
		return null;
	}
	
	/**
	 * Check whether at least one JSONObject within the given JSONArray contains the given
	 * attributeName. If such an object is found, true will be returned, otherwise false
	 * will be returned. If the given JSONArray is null or empty, this method will also return
	 * false. 
	 * @param array
	 * @param attributeName
	 * @return Boolean indicating whether the given {@link JSONArray} contains a
	 * 		JSONObject with the given attribute name.
	 */
	public static final boolean containsObjectWithAttribute(JSONArray array, String attributeName) {
		if ( array == null ) { return false; }
		for(int i=0; i<array.length(); i++){
			JSONObject obj = array.optJSONObject(i);
			if ( obj.has(attributeName) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method will evaluate the given matchExpression on the given JSONObject, then
	 * checks whether the evaluation result matches the given matchValue.
	 * @param obj
	 * @param matchExpression
	 * @param matchValue
	 * @return Boolean indicating whether the result of evaluating the given matchExpression
	 * 		against the given {@link JSONObject} matches the given matchValue.
	 */
	private static boolean isMatching(JSONObject obj, String matchExpression, Object matchValue) {
		if ( matchValue == null ) { return false; }
		Object expressionResult = SpringExpressionUtil.evaluateExpression(obj, matchExpression, matchValue.getClass());
		return expressionResult==matchValue || (matchValue!=null && matchValue.equals(expressionResult));
	}
	
	/**
	 * This method will evaluate the given matchExpression on the given JSONObject, then
	 * checks whether the evaluation result is contained in the given matchValues Set.
	 * @param obj
	 * @param matchExpression
	 * @param matchValue
	 * @return Boolean indicating whether the result of evaluating the given matchExpression
	 * 		against the given {@link JSONObject} matches any of the values contained in the
	 * 		given matchValues set.
	 */
	private static <T> boolean isMatching(JSONObject obj, String matchExpression, Set<T> matchValues, Class<T> valueType) {
		if ( matchValues == null ) { return false; }
		T expressionResult = SpringExpressionUtil.evaluateExpression(obj, matchExpression, valueType);
		return matchValues.contains(expressionResult);
	}
	
	/**
	 * Given a JSONArray, this method will return a map containing all JSONObjects from the array.
	 * For each object, the given keyExpression is used to calculate the map key.
	 * @param array
	 * @param keyExpression
	 * @param keyType
	 * @return A map containing the {@link JSONObject}s from the given {@link JSONArray}, 
	 * 		indexed by the result of evaluating the given keyExpression on each {@link JSONObject}
	 */
	public static final <K> LinkedHashMap<K, JSONObject> toMap(JSONArray array, String keyExpression, Class<K> keyType) {
		return toMap(array, keyExpression, keyType, "#this", JSONObject.class);
	}
	
	/**
	 * Given a list of JSONObject instances, this method will return a map containing all JSONObjects from the array.
	 * For each object, the given keyExpression is used to calculate the map key.
	 * @param list
	 * @param keyExpression
	 * @param keyType
	 * @return A map containing the {@link JSONObject}s from the given {@link List}, 
	 * 		indexed by the result of evaluating the given keyExpression on each {@link JSONObject}
	 */
	public static final <K> LinkedHashMap<K, JSONObject> toMap(List<JSONObject> list, String keyExpression, Class<K> keyType) {
		return toMap(list, keyExpression, keyType, "#this", JSONObject.class);
	}
	
	/**
	 * Given a JSONArray, this method will return a map with keys generated using the given 
	 * keyExpression, and corresponding values generated using the given valueExpression.
	 * @param array
	 * @param keyExpression
	 * @param keyType
	 * @param valueExpression
	 * @param valueType
	 * @return
	 */
	public static final <K, V> LinkedHashMap<K, V> toMap(JSONArray array, String keyExpression, Class<K> keyType, String valueExpression, Class<V> valueType) {
		LinkedHashMap<K, V> result = null;
		if ( array != null ) {
			result = new LinkedHashMap<K, V>();
			for(int i=0; i<array.length(); i++){
				JSONObject obj = array.optJSONObject(i);
				K key = SpringExpressionUtil.evaluateExpression(obj, keyExpression, keyType);
				V value = SpringExpressionUtil.evaluateExpression(obj, valueExpression, valueType);
				result.put(key, value);
			}
		}
		return result;
	}
	
	/**
	 * Given a List of JSONObject instances, this method will return a map with keys generated 
	 * using the given keyExpression, and corresponding values generated using the given valueExpression.
	 * @param array
	 * @param keyExpression
	 * @param keyType
	 * @param valueExpression
	 * @param valueType
	 * @return
	 */
	public static final <K, V> LinkedHashMap<K, V> toMap(List<JSONObject> list, String keyExpression, Class<K> keyType, String valueExpression, Class<V> valueType) {
		return toMap(new JSONArray(list), keyExpression, keyType, valueExpression, valueType);
	}
	
	/**
	 * Get a JSONArray from the given JSONObject by evaluating the given arrayExpression.
	 * @param object
	 * @param arrayExpression
	 * @return A {@link JSONArray} retrieved from the given {@link JSONObject} by
	 * 		evaluating the given arrayExpression 
	 */
	public static final JSONArray getJSONArray(JSONObject object, String arrayExpression) {
		return SpringExpressionUtil.evaluateExpression(object, arrayExpression, JSONArray.class);
	}
	
	/**
	 * Get a JSONObject from the given JSONObject by evaluating the given objectExpression.
	 * @param object
	 * @param objectExpression
	 * @return A {@link JSONObject} retrieved from the given {@link JSONObject} by
	 * 		evaluating the given arrayExpression 
	 */
	public static final JSONObject getJSONObject(JSONObject object, String objectExpression) {
		return SpringExpressionUtil.evaluateExpression(object, objectExpression, JSONObject.class);
	}

	/**
	 * Append the given arrayToAppend to the given array.
	 * @param array
	 * @param arrayToAppend
	 */
	public static final void appendJSONArray(JSONArray array, JSONArray arrayToAppend) {
		for ( int i = 0 ; i < arrayToAppend.length() ; i++ ) {
			array.put(arrayToAppend.opt(i));
		}
	}
}
