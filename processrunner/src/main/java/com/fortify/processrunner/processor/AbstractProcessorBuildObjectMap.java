package com.fortify.processrunner.processor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;

public abstract class AbstractProcessorBuildObjectMap extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorBuildObjectMap.class);
	
	@Override
	protected boolean process(Context context) {
		LinkedHashMap<String, Object> map = createMap(context);
		Collection<IMapUpdater> mapUpdaters = getMapUpdaters(context);
		for ( IMapUpdater mapUpdater : mapUpdaters ) {
			mapUpdater.updateMap(context, map);
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.trace("Build object map: "+map);
		}
		return processMap(context, map);
	}
	
	protected LinkedHashMap<String, Object> createMap(Context context) {
		return new LinkedHashMap<String, Object>();
	}
	
	protected abstract Collection<IMapUpdater> getMapUpdaters(Context context);
	protected abstract boolean processMap(Context context, LinkedHashMap<String, Object> map);
	
	protected static interface IMapUpdater {
		void updateMap(Context context, LinkedHashMap<String, Object> map);
	}
	
	protected static abstract class AbstractMapUpdaterWithRootObjectExpression implements IMapUpdater {
		private Expression rootExpression;
		public Expression getRootExpression() { return rootExpression; }
		public void setRootExpression(Expression rootExpression) { this.rootExpression = rootExpression; }
		public Object getRootObject(Context context) {
			return rootExpression==null ? context : 
				SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
		}
		public final void updateMap(Context context, LinkedHashMap<String, Object> map) {
			Object rootObject = getRootObject(context);
			if ( rootObject != null ) {
				updateMap(context, map, rootObject);
			}
		}
		protected abstract void updateMap(Context context, LinkedHashMap<String, Object> map, Object rootObject);
	}
	
	protected static abstract class AbstractMapUpdaterWithRootObjectExpressionAndExpressionMap extends AbstractMapUpdaterWithRootObjectExpression {
		private LinkedHashMap<String,? extends Expression> expressions;
		public LinkedHashMap<String,? extends Expression> getExpressions() { return expressions; }
		public void setExpressions(LinkedHashMap<String,? extends Expression> expressions) { this.expressions = expressions; }
		@Override
		protected final void updateMap(Context context, LinkedHashMap<String, Object> map, Object rootObject) {
			LinkedHashMap<String,? extends Expression> expressions = getExpressions();
			if ( expressions != null ) {
				for ( Map.Entry<String, ? extends Expression> entry : expressions.entrySet() ) {
					String key = entry.getKey();
					Expression expression = entry.getValue();
					Object value = getValue(context, map, key, rootObject, expression);
					map.put(key, value);
				}
			}
		}
		protected abstract Object getValue(Context context, LinkedHashMap<String, Object> map, String key, Object rootObject, Expression expression);
	}
	
	protected static class MapUpdaterPutValuesFromExpressionMap extends AbstractMapUpdaterWithRootObjectExpressionAndExpressionMap {
		public MapUpdaterPutValuesFromExpressionMap() {}
		public MapUpdaterPutValuesFromExpressionMap(Expression rootExpression, LinkedHashMap<String, ? extends Expression> expressions) {
			setRootExpression(rootExpression);
			setExpressions(expressions);
		}
		@Override
		protected Object getValue(Context context, LinkedHashMap<String, Object> map, String key, Object rootObject, Expression expression) {
			return SpringExpressionUtil.evaluateExpression(rootObject, expression, Object.class);
		}
	}
	
	protected static class MapUpdaterAppendValuesFromExpressionMap extends AbstractMapUpdaterWithRootObjectExpressionAndExpressionMap {
		public MapUpdaterAppendValuesFromExpressionMap() {}
		public MapUpdaterAppendValuesFromExpressionMap(Expression rootExpression, LinkedHashMap<String, ? extends Expression> expressions) {
			setRootExpression(rootExpression);
			setExpressions(expressions);
		}
		@Override
		protected Object getValue(Context context, LinkedHashMap<String, Object> map, String key, Object rootObject, Expression expression) {
			Object value = map.get(key);
			if ( rootObject instanceof Iterable ) {
				value = appendValues(context, value, (Iterable<?>)rootObject, expression);
			} else {
				value = appendValue(context, value, rootObject, expression);
			}
			return value;
		}

		protected Object appendValues(Context context, Object value, Iterable<?> rootObjects, Expression expression) {
			for ( Object rootObject : rootObjects ) {
				value = appendValue(context, value, rootObject, expression);
			}
			return value;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected Object appendValue(Context context, Object value, Object rootObject, Expression expression) {
			Object valueToAppend = SpringExpressionUtil.evaluateExpression(rootObject, expression, Object.class); 
			if ( value instanceof String ) {
				value = value+""+valueToAppend;
			} else if ( value instanceof Collection ) {
				((Collection)value).add(valueToAppend);
			}
			return value;
		}
	}
}
