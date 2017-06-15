package com.fortify.util.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.convert.support.DefaultConversionService;

import com.fortify.util.spring.SpringExpressionUtil;

public class JSONList extends ArrayList<Object> {
	private static final long serialVersionUID = 1L;

	public JSONList() {
		super();
	}

	public JSONList(Collection<? extends Object> c) {
		super(c);
	}

	public JSONList(int initialCapacity) {
		super(initialCapacity);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> asValueType(Class<T> type) {
		return (List<T>)this;
	}
	
	public final <R> List<R> getValues(String listValueExpression, Class<R> listValueType) {
		List<R> result = new ArrayList<R>();
		for( Object value : this ){
			CollectionUtils.addIgnoreNull(result, SpringExpressionUtil.evaluateExpression(value, listValueExpression, listValueType) );
		}
		return result;
	}
	
	public final JSONList filter(String matchExpression, Object matchValue) {
		JSONList result = new JSONList();
		for ( Object value : this ) {
			if ( isMatching(value, matchExpression, matchValue) ) {
				result.add( value );
			}
		}
		return result;
	}
	
	public final <M,R> R mapValue(String matchExpression, M matchValue, String returnExpression, Class<R> returnType) {
		Object value = find(matchExpression, matchValue, Object.class);
		return SpringExpressionUtil.evaluateExpression(value, returnExpression, returnType);
	}
	
	public final <R> R find(String matchExpression, Object matchValue, Class<R> type) {
		if ( matchExpression == null ) { return null; }
		for ( Object value : this ) {
			if ( isMatching(value, matchExpression, matchValue) ) {
				return new DefaultConversionService().convert(value, type);
			}
		}
		return null;
	}
	
	public final <K, V> LinkedHashMap<K, V> toMap(String keyExpression, Class<K> keyType, String valueExpression, Class<V> valueType) {
		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
		for ( Object obj : this ) {
			K key = SpringExpressionUtil.evaluateExpression(obj, keyExpression, keyType);
			V value = SpringExpressionUtil.evaluateExpression(obj, valueExpression, valueType);
			result.put(key, value);
		}
		return result;
	}
	
	public final <K, V> LinkedHashMap<K, V> toMap(String keyExpression, Class<K> keyType, Class<V> valueType) {
		return toMap(keyExpression, keyType, "#this", valueType);
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
	private static boolean isMatching(Object obj, String matchExpression, Object matchValue) {
		if ( matchValue == null ) { return false; }
		Object expressionResult = SpringExpressionUtil.evaluateExpression(obj, matchExpression, matchValue.getClass());
		return expressionResult==matchValue || (matchValue!=null && matchValue.equals(expressionResult));
	}
}
