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
import java.util.LinkedHashSet;
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
import com.fortify.processrunner.cli.CLIOptionDefinition;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.processrunner.util.rest.IQueryBuilderUpdater;
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
public abstract class AbstractSourceContextGenerator<C extends ISourceContextGeneratorConfiguration, Q extends AbstractRestConnectionQueryBuilder<?, ?>> implements IContextGenerator {
	private List<IQueryBuilderUpdater<Q>> queryBuilderUpdaters;
	
	private C config = getDefaultConfig();
	
	/**
	 * Method to be implemented by concrete implementations to return the default configuration.
	 * This should never return null to avoid later {@link NullPointerException}s when accessing
	 * the configuration.
	 * @return
	 */
	protected abstract C getDefaultConfig();
	
	/**
	 * Method to be implemented by concrete implementations to return the CLI option name
	 * for selecting a single source object by id.
	 * 
	 * @return
	 */
	protected abstract String getCLIOptionNameForId();
	
	/**
	 * Method to be implemented by concrete implementations to return the CLI option name
	 * for selecting one or more source objects by a list of name patterns.
	 * 
	 * @return
	 */
	protected abstract String getCLIOptionNameForNamePatterns();
	
	/**
	 * Method to be implemented by concrete implementations to create the base source system query builder.
	 * This query builder should add any required on-demand data (for example attributes map), but should
	 * not add any filters, as that will be taken care of by other methods in this class.
	 * 
	 * @param context
	 * @return
	 */
	protected abstract Q createBaseQueryBuilder(Context initialContext);

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
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because configured filter expression matches 
	 * or does not match source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForConfiguredFilterExpression(Context initialContext);
	
	/**
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because one of the configured name patterns matches 
	 * or does not match source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForConfiguredNamePatterns(Context initialContext);
	
	/**
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because one of the name patterns provided as
	 * a context property matches or does not match source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForContextNamePatterns(Context initialContext);
	
	/**
	 * Method to be implemented by concrete implementations to log messages stating that a
	 * source object is included or excluded because required attributes have or have not
	 * been set for the source object.
	 * @param initialContext
	 * @return
	 */
	protected abstract IJSONMapFilterListener getFilterListenerForConfiguredAttributes(Context initialContext);
	
	// TODO Add JavaDoc
	protected abstract String getSourceObjectAttributeValue(JSONMap sourceObject, String attributeName);
	protected abstract String getSourceObjectName(JSONMap sourceObject);
	
	
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
	
	@Override
	public void updateProcessRunnerCLIOptionDefinitions(Collection<CLIOptionDefinition> defs) {
		for ( CLIOptionDefinition def : defs ) {
			String desc = getConfig().getMappingDescriptions().get(def.getName());
			if ( StringUtils.isNotBlank(desc) ) {
				def.defaultValueDescription(desc);
			}
			def.addAllowedSources(getConfig().getClass().getSimpleName()+" mappings");
		}
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
		if ( MapUtils.isNotEmpty(getConfig().getExpressionToCLIOptionsMap()) ) {
			for ( Map.Entry<SimpleExpression, Context> entry : getConfig().getExpressionToCLIOptionsMap().entrySet() ) {
				SimpleExpression exprString = entry.getKey();
				if ( ContextSpringExpressionUtil.evaluateExpression(newContext, sourceObject, exprString, Boolean.class) ) {
					mergeContexts(newContext, entry.getValue(), sourceObject);
				}
			}
		}
	}

	private void updateContextWithNamePatternMappings(Context newContext, JSONMap sourceObject) {
		if ( MapUtils.isNotEmpty(getConfig().getNamePatternToCLIOptionsMap()) ) {
			for (Map.Entry<Pattern, Context> entry : getConfig().getNamePatternToCLIOptionsMap().entrySet()) {
				Pattern pattern = entry.getKey();
				if ( pattern.matcher(getSourceObjectName(sourceObject)).matches() ) {
					mergeContexts(newContext, entry.getValue(), sourceObject);
				}
			}
		}
	}
	
	private void updateContextWithAttributeMappings(Context newContext, JSONMap sourceObject) {
		if ( MapUtils.isNotEmpty(getConfig().getAttributeToCLIOptionMap()) ) {
			for (Map.Entry<String, String> entry : getConfig().getAttributeToCLIOptionMap().entrySet() ) {
				String attributeName = entry.getKey();
				String attributeValue = getSourceObjectAttributeValue(sourceObject, attributeName);
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
				if ( !targetContext.containsKey(ctxPropertyName) ) {
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
		Q queryBuilder = createBaseQueryBuilder(initialContext);
		addOnDemandData(queryBuilder);
		if ( initialContext.containsKey(getCLIOptionNameForId()) ) {
			updateQueryBuilderWithId(initialContext, queryBuilder);
		} else if ( initialContext.containsKey(getCLIOptionNameForNamePatterns()) ) {
			updateQueryBuilderWithContextNamePatterns(initialContext, queryBuilder);
		} else {
			updateQueryBuilderWithConfiguredFilterExpression(initialContext, queryBuilder);
			updateQueryBuilderWithConfiguredNamePatterns(initialContext, queryBuilder);
			updateQueryBuilderWithConfiguredAttributes(initialContext, queryBuilder);
		}
		updateQueryBuilderWithQueryBuilderUpdaters(initialContext, queryBuilder);
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
	
	// TODO Add default implementation?
	protected abstract void updateQueryBuilderWithId(Context initialContext, Q queryBuilder);

	private void updateQueryBuilderWithContextNamePatterns(Context initialContext,	Q queryBuilder) {
		String namePatternsString = (String)initialContext.get(getCLIOptionNameForNamePatterns());
		Set<Pattern> namePatterns = parseNamePatternStrings(namePatternsString);
		JSONMapFilterNamePatterns filter = new JSONMapFilterNamePatterns(MatchMode.INCLUDE, namePatterns);
		addNonNullFilterListener(filter, getFilterListenerForContextNamePatterns(initialContext));
		queryBuilder.preProcessor(filter);
	}
	
	private Set<Pattern> parseNamePatternStrings(String namePatternsString) {
		Set<Pattern> result = new LinkedHashSet<>();
		for ( String patternString : namePatternsString.split(",") ) {
			result.add(Pattern.compile(patternString));
		}
		return result;
	}

	private void updateQueryBuilderWithConfiguredFilterExpression(Context initialContext, Q queryBuilder) {
		SimpleExpression filterExpression = getConfig().getFilterExpression();
		if ( filterExpression != null ) {
			JSONMapFilterSpEL filter = new JSONMapFilterSpEL(MatchMode.INCLUDE, filterExpression);
			addNonNullFilterListener(filter, getFilterListenerForConfiguredFilterExpression(initialContext));
			queryBuilder.preProcessor(filter);
		}
	}

	private void updateQueryBuilderWithConfiguredNamePatterns(Context initialContext, Q queryBuilder) {
		LinkedHashMap<Pattern, Context> namePatternToContextMap = getConfig().getNamePatternToCLIOptionsMap();
		if ( MapUtils.isNotEmpty(namePatternToContextMap) ) {
			JSONMapFilterNamePatterns filter = new JSONMapFilterNamePatterns(MatchMode.INCLUDE, namePatternToContextMap.keySet());
			addNonNullFilterListener(filter, getFilterListenerForConfiguredNamePatterns(initialContext));
			queryBuilder.preProcessor(filter);
		}
	}

	private void updateQueryBuilderWithConfiguredAttributes(Context initialContext, Q queryBuilder) {
		Map<String, String> attributeMappings = getConfig().getAttributeToCLIOptionMap();
		if ( MapUtils.isNotEmpty(attributeMappings) ) {
			Map<String, String> copyOfAttributeMappings = new HashMap<>(attributeMappings);
			// Remove any mappings for which a context attribute already exists in the given initialContext
			copyOfAttributeMappings.values().removeAll(initialContext.keySet());
			JSONMapFilterRequiredAttributes filter = new JSONMapFilterRequiredAttributes(MatchMode.INCLUDE, copyOfAttributeMappings.keySet());
			addNonNullFilterListener(filter, getFilterListenerForConfiguredAttributes(initialContext));
			queryBuilder.preProcessor(filter);
		}
	}
	
	private void updateQueryBuilderWithQueryBuilderUpdaters(Context initialContext, Q queryBuilder) {
		if ( getQueryBuilderUpdaters()!=null ) {
			for ( IQueryBuilderUpdater<Q> updater : getQueryBuilderUpdaters() ) {
				updater.updateQueryBuilder(initialContext, queryBuilder);
			}
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
	
	public List<IQueryBuilderUpdater<Q>> getQueryBuilderUpdaters() {
		return queryBuilderUpdaters;
	}

	@Autowired(required=false)
	public void setQueryBuilderUpdaters(List<IQueryBuilderUpdater<Q>> queryBuilderUpdaters) {
		this.queryBuilderUpdaters = queryBuilderUpdaters;
	}

	private class JSONMapFilterNamePatterns extends AbstractJSONMapFilter {
		private final Set<Pattern> namePatterns;
		
		public JSONMapFilterNamePatterns(MatchMode matchMode, Set<Pattern> namePatterns) {
			super(matchMode);
			this.namePatterns = namePatterns;
		}

		@Override
		protected boolean isMatching(JSONMap json) {
			if ( CollectionUtils.isNotEmpty(namePatterns) ) {
				for ( Pattern pattern : namePatterns ) {
					if ( pattern.matcher(getSourceObjectName(json)).matches() ) {
						return true;
					}
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
			if ( CollectionUtils.isNotEmpty(requiredAttributeNames) ) {
				for ( String requiredAttributeName : requiredAttributeNames ) {
					if ( StringUtils.isBlank(getSourceObjectAttributeValue(json, requiredAttributeName)) ) {
						return false;
					}
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
