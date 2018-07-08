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
package com.fortify.bugtracker.common.src.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.bugtracker.common.src.config.ISourceContextGeneratorConfiguration;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.IJSONMapFilterListener;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.fortify.util.rest.query.AbstractRestConnectionQueryBuilder;
import com.fortify.util.rest.query.IRestConnectionQuery;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This abstract {@link IContextGenerator} implementation generates {@link Context} instances
 * by querying the source system based on a configured optional filter expression. For every 
 * {@link JSONMap} returned by the source system query, a new {@link Context} is generated based 
 * on the initial context. Context properties are added to this new {@link Context} based on the 
 * configured expression to context properties mapping. 
 *  
 * @author Ruud Senden
 *
 * @param <C>
 */
public abstract class AbstractSourceContextGenerator<C extends ISourceContextGeneratorConfiguration> implements IContextGenerator {
	private C config = getDefaultConfig();
	
	/**
	 * Method to be implemented by concrete implementations to return the default configuration.
	 * This should never return null to avoid later {@link NullPointerException}s when accessing
	 * the configuration.
	 * @return
	 */
	protected abstract C getDefaultConfig();
	
	/**
	 * Method to be implemented by concrete implementations to create the base source system query builder.
	 * This query builder must include filtering criteria based on the given filterExpression, if any, and
	 * can optionally add additional implementation-specific filtering criteria.
	 * 
	 * @param context
	 * @param filterExpression
	 * @return
	 */
	protected abstract AbstractRestConnectionQueryBuilder<?, ?> createBaseQueryBuilder(Context initialContext);

	/**
	 * Method to be implemented by concrete implementations to add context properties to
	 * generated {@link Context} instances based on the source object for which this context
	 * is being generated. Implementations will usually add the source object itself for later
	 * reference, as well as any other required or useful properties that can be obtained from
	 * the source object or based on implementation-specific mappings
	 * 
	 * @param newContext is the {@link Context} currently being generated
	 * @param sourceObject for which newContext is being generated
	 * @return Updated {@link Context}
	 */
	protected abstract void updateContextForSourceObject(Context newContext, JSONMap sourceObject);
	
	/**
	 * Method to be implemented by concrete implementations to indicate whether configured
	 * filters should be ignored or not. This method should return true if the source objects
	 * to use have been defined on the command line, or false if this implementation should
	 * automatically load source objects based on defined filters.
	 * 
	 * @param initialContext is the initial context
	 * @return
	 */
	protected abstract boolean ignoreConfiguredFilters(Context initialContext);
	
	/**
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because configured filter expression matches 
	 * or does not match source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForFilterExpression(Context initialContext);
	
	/**
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because one of the configured name patterns matches 
	 * or does not match source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForNamePatterns(Context initialContext);
	
	/**
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because required attributes have or have not
	 * been set for the source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForAttributes(Context initialContext);
	
	// TODO Add JavaDoc
	protected abstract String getAttributeValue(JSONMap sourceObject, String attributeName);
	protected abstract String getName(JSONMap sourceObject);
	
	
	/**
	 * This method uses the base query created by {@link #createBaseQueryBuilder(Context, SimpleExpression)},
	 * amended with configured on-demand data and filters, to retrieve a list of source objects.
	 * For each source object returned by the query, a new context is generated by
	 * {@link #generateContextForSourceObject(Context, JSONMap)}. 
	 */
	@Override
	public Collection<Context> generateContexts(Context initialContext) {
		List<Context> result = new ArrayList<>();
		JSONList queryResult = createQuery(initialContext).getAll();
		if ( !CollectionUtils.isEmpty(queryResult) ) {
			for ( JSONMap json : queryResult.asValueType(JSONMap.class) ) {
				result.add(generateContextForSourceObject(initialContext, json));
			}
		}
		return result;
	}

	/**
	 * <p>Generate a new {@link Context} for the given source object. The new {@link Context}
	 * is initialized with the given initial {@link Context}, and then updated by calling
	 * the following methods:
	 * <ul>
	 *  <li>The implementation-specific {@link #updateContextForSourceObject(Context, JSONMap)} method</li>
	 *  <li>{@link #updateContextWithExpressionMappings(Context, JSONMap)}</li>
	 *  <li>{@link #updateContextWithNamePatternMappings(Context, JSONMap)}</li>
	 *  <li>{@link #updateContextWithAttributeMappings(Context, JSONMap)}</li>
	 * </ul>
	 * 
	 * @param initialContext
	 * @param sourceObject
	 * @return
	 */
	private Context generateContextForSourceObject(Context initialContext, JSONMap sourceObject) {
		Context newContext = new Context(initialContext);
		updateContextForSourceObject(newContext, sourceObject);
		updateContextWithExpressionMappings(newContext, sourceObject);
		updateContextWithNamePatternMappings(newContext, sourceObject);
		updateContextWithAttributeMappings(newContext, sourceObject);
		return newContext;
	}

	private void updateContextWithExpressionMappings(Context newContext, JSONMap sourceObject) {
		if ( MapUtils.isNotEmpty(getConfig().getExpressionToContextMap()) ) {
			for ( Map.Entry<SimpleExpression, Context> entry : getConfig().getExpressionToContextMap().entrySet() ) {
				SimpleExpression exprString = entry.getKey();
				if ( ContextSpringExpressionUtil.evaluateExpression(newContext, sourceObject, exprString, Boolean.class) ) {
					mergeContexts(newContext, entry.getValue(), sourceObject);
				}
			}
		}
	}

	private void updateContextWithNamePatternMappings(Context newContext, JSONMap sourceObject) {
		if ( MapUtils.isNotEmpty(getConfig().getNamePatternToContextMap()) ) {
			for (Map.Entry<Pattern, Context> entry : getConfig().getNamePatternToContextMap().entrySet()) {
				Pattern pattern = entry.getKey();
				if ( pattern.matcher(getName(sourceObject)).matches() ) {
					mergeContexts(newContext, entry.getValue(), sourceObject);
				}
			}
		}
	}
	
	private void updateContextWithAttributeMappings(Context newContext, JSONMap sourceObject) {
		if ( MapUtils.isNotEmpty(getConfig().getAttributeMappings()) ) {
			for (Map.Entry<String, String> entry : getConfig().getAttributeMappings().entrySet() ) {
				String attributeName = entry.getKey();
				String attributeValue = getAttributeValue(sourceObject, attributeName);
				if ( StringUtils.isNotBlank(attributeValue) ) {
					mergeContexts(newContext, new Context().chainedPut(entry.getValue(), attributeValue), sourceObject);
				}
			}
		}
	}

	/**
	 * Merge the given contextWithValueExpressionsToMerge with the given targetContext. 
	 * Any properties that already exist in the given target context will not be overwritten. 
	 * Property values in contextWithValueExpressionsToMerge may contain Spring template 
	 * expressions; these expressions will be evaluated using the given source object before 
	 * merging the properties with the target context.
	 * 
	 * @param targetContext
	 * @param contextWithValueExpressionsToMerge
	 * @param sourceObject
	 */
	private void mergeContexts(Context targetContext, Context contextWithValueExpressionsToMerge, JSONMap sourceObject) {
		if ( contextWithValueExpressionsToMerge != null ) {
			for ( Entry<String, Object> entry : contextWithValueExpressionsToMerge.entrySet() ) {
				String ctxPropertyName = entry.getKey();
				if ( !targetContext.containsKey(ctxPropertyName) ) { // TODO Override or not? 
					Object ctxPropertyValue = SpringExpressionUtil.evaluateTemplateExpression(sourceObject, (String)entry.getValue(), Object.class);
					targetContext.put(ctxPropertyName, ctxPropertyValue);
				}
			}
		}
	}

	/**
	 * Create a new {@link IRestConnectionQuery} instance based on the {@link AbstractRestConnectionQueryBuilder}
	 * returned by the {@link #createBaseQueryBuilder(Context, SimpleExpression)} method. The connection
	 * builder will be amended with configured on demand data.
	 * 
	 * @param context
	 * @return
	 */
	private final IRestConnectionQuery createQuery(Context initialContext) {
		AbstractRestConnectionQueryBuilder<?,?> queryBuilder = createBaseQueryBuilder(initialContext);
		addOnDemandData(queryBuilder);
		if ( !ignoreConfiguredFilters(initialContext) ) {
			addJSONMapFilterForFilterExpression(initialContext, queryBuilder);
			addJSONMapFilterForNamePatterns(initialContext, queryBuilder);
			addJSONMapFilterForAttributes(initialContext, queryBuilder);
		}
		return queryBuilder.build();
	}

	/**
	 * Add on-demand data to the given {@link AbstractRestConnectionQueryBuilder}
	 * based on {@link ISourceContextGeneratorConfiguration#getExtraData()}.
	 * 
	 * @param queryBuilder
	 */
	private final void addOnDemandData(AbstractRestConnectionQueryBuilder<?,?> queryBuilder) {
		// TODO Remove code duplication with SourceVulnerabilityProcessorHelper
		Map<String, String> extraData = getConfig().getExtraData();
		if ( extraData != null ) {
			for ( Map.Entry<String, String> entry : extraData.entrySet() ) {
				String propertyName = entry.getKey();
				String uriString = StringUtils.substringBeforeLast(entry.getValue(), ";");
				// TODO Parse properly as properties, to allow additional properties if ever necessary
				boolean useCache = "useCache=true".equals(StringUtils.substringAfterLast(entry.getValue(), ";"));
				queryBuilder.onDemand(propertyName, uriString, useCache?propertyName:null);
			}
		}
	}
	
	private void addJSONMapFilterForFilterExpression(Context initialContext, AbstractRestConnectionQueryBuilder<?, ?> queryBuilder) {
		SimpleExpression filterExpression = getConfig().getFilterExpression();
		if ( filterExpression != null ) {
			JSONMapFilterSpEL filter = new JSONMapFilterSpEL(MatchMode.INCLUDE, filterExpression);
			addNonNullFilterListener(filter, getFilterListenerForFilterExpression(initialContext));
			queryBuilder.preProcessor(filter);
		}
	}

	private void addJSONMapFilterForNamePatterns(Context initialContext, AbstractRestConnectionQueryBuilder<?, ?> queryBuilder) {
		LinkedHashMap<Pattern, Context> namePatternToContextMap = getConfig().getNamePatternToContextMap();
		if ( MapUtils.isNotEmpty(namePatternToContextMap) ) {
			JSONMapFilterNamePatterns filter = new JSONMapFilterNamePatterns(MatchMode.INCLUDE, namePatternToContextMap.keySet());
			addNonNullFilterListener(filter, getFilterListenerForNamePatterns(initialContext));
			queryBuilder.preProcessor(filter);
		}
	}

	private void addJSONMapFilterForAttributes(Context initialContext, AbstractRestConnectionQueryBuilder<?, ?> queryBuilder) {
		Map<String, String> attributeMappings = getConfig().getAttributeMappings();
		if ( MapUtils.isNotEmpty(attributeMappings) ) {
			Map<String, String> copyOfAttributeMappings = new HashMap<>(attributeMappings);
			// Remove any mappings for which a context attribute already exists in the given initialContext
			copyOfAttributeMappings.values().removeAll(initialContext.keySet());
			JSONMapFilterRequiredAttributes filter = new JSONMapFilterRequiredAttributes(MatchMode.INCLUDE, copyOfAttributeMappings.keySet());
			addNonNullFilterListener(filter, getFilterListenerForAttributes(initialContext));
			queryBuilder.preProcessor(filter);
		}
		
	}
	
	private void addNonNullFilterListener(AbstractJSONMapFilter filter, IJSONMapFilterListener listener) {
		if ( listener != null ) {
			filter.addFilterListeners(listener);
		}
	}

	public C getConfig() {
		return this.config;
	}
	
	@Autowired(required=false)
	public void setConfig(C config) {
		this.config = config;
	}
	
	private class JSONMapFilterNamePatterns extends AbstractJSONMapFilter {
		private final Set<Pattern> namePatterns;
		
		public JSONMapFilterNamePatterns(MatchMode matchMode, Set<Pattern> namePatterns) {
			super(matchMode);
			this.namePatterns = namePatterns;
		}

		@Override
		protected boolean isMatching(JSONMap json) {
			for ( Pattern pattern : namePatterns ) {
				if ( pattern.matcher(getName(json)).matches() ) {
					return true;
				}
			}
			return false;
		}
		
		// May be called from SpEL for logging
		@SuppressWarnings("unused")
		public Set<Pattern> getNamePatterns() {
			return namePatterns;
		}
	}
	
	private class JSONMapFilterRequiredAttributes extends AbstractJSONMapFilter {
		private final Set<String> requiredAttributeNames;
		
		public JSONMapFilterRequiredAttributes(MatchMode matchMode, Set<String> requiredAttributeNames) {
			super(matchMode);
			this.requiredAttributeNames = requiredAttributeNames;
		}

		@Override
		protected boolean isMatching(JSONMap json) {
			for ( String requiredAttributeName : requiredAttributeNames ) {
				if ( StringUtils.isBlank(getAttributeValue(json, requiredAttributeName)) ) {
					return false;
				}
			}
			return true;
		}
		
		// May be called from SpEL for logging
		@SuppressWarnings("unused")
		public Set<String> getRequiredAttributeNames() {
			return requiredAttributeNames;
		}
	}
}
