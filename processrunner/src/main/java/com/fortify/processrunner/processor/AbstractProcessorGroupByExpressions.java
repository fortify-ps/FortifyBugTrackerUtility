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
package com.fortify.processrunner.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import com.fortify.api.util.spring.expression.SimpleExpression;
import com.fortify.api.util.spring.expression.TemplateExpression;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;

/**
 * <p>This {@link IProcessor} implementation allows for collecting
 * and grouping data stored in the {@link Context} during the 
 * {@link Phase#PROCESS} phase. The rootExpression property defines 
 * the expression used to retrieve each root object from the 
 * {@link Context}. The optional groupTemplateExpression is 
 * evaluated on each root object to identify the group that this
 * root object belongs to.</p>
 * 
 * <p>During the {@link Phase#POST_PROCESS} phase, the 
 * {@link #processGroup(Context, List)} method will be called
 * to allow actual implementations to process each group.</p>
 * 
 * <p>If no grouping expression has been defined, or if the DisableGrouping
 * context property has been set to true, the {@link #processGroup(Context, List)}
 * method will be invoked immediately for every individual root object.</p>
 * 
 * @author Ruud Senden
 */
public abstract class AbstractProcessorGroupByExpressions extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorGroupByExpressions.class);
	private SimpleExpression rootExpression;
	private TemplateExpression groupTemplateExpression;
	
	/**
	 * Add context properties for grouping
	 */
	@Override
	public final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		if ( getGroupTemplateExpression()!=null && !isForceGrouping() ) {
			contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextGrouping.PRP_DISABLE_GROUPING, "Disable grouping of vulnerabilities", false));
		}
		addExtraContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	/**
	 * Subclasses can override this method to add extra context properties
	 * @param contextPropertyDefinitions
	 * @param context
	 */
	protected void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}

	/**
	 * If grouping is enabled, initialize the temporary cache that will hold grouped objects.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected final boolean preProcess(Context context) {
		if ( isGroupingEnabled(context) ) {
			IContextGrouping ctx = context.as(IContextGrouping.class);
			DB db = DBMaker.tempFileDB()
					.closeOnJvmShutdown().fileDeleteAfterClose()
					.fileMmapEnableIfSupported()
					.make();
			Map<String, List<Object>> groups = db.hashMap("groups", Serializer.STRING, Serializer.JAVA).create();
			groups.clear(); // Make sure that we start with a clean cache 
			ctx.setGroupByExpressionsMapDB(db);
			ctx.setGroupByExpressionsGroupsMap(groups);
		}
		return preProcessBeforeGrouping(context);
	}
	
	/**
	 * <p>This method retrieves the current root object by evaluating the expression configured through 
	 * {@link #setRootExpression(SimpleExpression)} on the given {@link Context}.</p>
	 * 
	 * <p>If grouping is enabled, the group key will then be determined by evaluating the expression 
	 * configured through {@link #setGroupTemplateExpression(TemplateExpression)} on the root object, 
	 * and the root object will be stored in the temporary cache based on the group key. The grouped 
	 * objects will be further processed by the {@link #postProcess(Context)} method once all objects
	 * have been grouped.</p>
	 * 
	 * 
	 * <p>If grouping is not enabled, the {@link #processGroup(Context, String, List)} method will be 
	 * called directly in order to immediately process the single root object.</p>
	 */
	protected final boolean process(Context context) {
		if ( processBeforeGrouping(context)==false ) {
			return false;
		}
		
		//totalCount++;
		SimpleExpression rootExpression = getRootExpression();
		TemplateExpression groupTemplateExpression = getGroupTemplateExpression();
		Object rootObject = ContextSpringExpressionUtil.evaluateExpression(context, context, rootExpression, Object.class);
		
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("[Process] Current object: "+rootObject);
		}
		
		if ( !isGroupingEnabled(context) ) {
			// If grouping is not enabled, we directly process the 
			// group since we do not need to group the data first.
			return processGroup(context, null, Arrays.asList(new Object[]{rootObject}));
		} else {
			// If a group template expression is defined, we collect the group
			// data and invoke the process() method of the group processor
			// in our postProcess() method once all data has been grouped.
			String groupKey = ContextSpringExpressionUtil.evaluateExpression(context, rootObject, groupTemplateExpression, String.class);
			if ( groupKey != null ) {
				addGroupEntry(context, groupKey, rootObject);
			} else {
				LOG.warn("Group key is null for "+rootObject);
			}
			return true;
		}
	}

	/**
	 * If grouping is enabled, this method will get each group of objects from
	 * the group cache, and call the {@link #processGroup(Context, String, List)}
	 * method for each group of objects. If grouping is not enabled, the 
	 * {@link #processGroup(Context, String, List)} method will already have
	 * been called by the {@link #process(Context)} method and thus will not be
	 * called again. Once finished, the temporary cache will be cleaned up.
	 */
	protected final boolean postProcess(Context context) {
		boolean result = true;
		Map<String, List<Object>> groupsMap = getGroupsMap(context);
		if ( isGroupingEnabled(context) ) {
			// If grouping is enabled, we call the process() method on 
			// the group processor for every group that we have collected.
			
			for ( Map.Entry<String, List<Object>> group : groupsMap.entrySet() ) {
				if ( !processGroup(context, group.getKey(), group.getValue()) ) {
					result = false; break; // Stop processing remainder of groups
				};
			}
			
		}
		removeGroupsMap(context);
		return postProcessAfterProcessingGroups(context) && result;
	}
	
	/**
	 * Indicate whether grouping is enabled. If no group template expression is configured
	 * through {@link #setGroupTemplateExpression(TemplateExpression)}, grouping will be disabled.
	 * Otherwise, if the {@link #isForceGrouping()} method returns true, grouping will be enabled
	 * without allowing a user to disable grouping through the 'DisableGrouping' context property.
	 * If grouping is not forced, then grouping will be enabled unless the 'DisableGrouping' context
	 * property is set to 'true'.
	 * @param context
	 * @return
	 */
	private boolean isGroupingEnabled(Context context) {
		String disableGrouping = context.as(IContextGrouping.class).getDisableGrouping();
		return getGroupTemplateExpression() != null && (isForceGrouping() || !"true".equals(disableGrouping));
	}
	
	/**
	 * Subclasses can override this method to perform additional
	 * processing in the pre-processing phase
	 * @param context
	 * @return
	 */
	protected boolean preProcessBeforeGrouping(Context context) {
		return true;
	}
	
	/**
	 * Subclasses can override this method to perform additional
	 * processing in the processing phase
	 * @param context
	 * @return
	 */
	protected boolean processBeforeGrouping(Context context) {
		return true;
	}
	
	/**
	 * Subclasses can override this method to perform additional
	 * processing in the post-processing phase
	 * @param context
	 * @return
	 */
	protected boolean postProcessAfterProcessingGroups(Context context) {
		return true;
	}
	
	/**
	 * Subclasses must implement this method to actually process an individual
	 * group. Note that groupName may be null if grouping is disabled; in that 
	 * case currentGroup will contain only a single object.
	 * @param context
	 * @param groupName
	 * @param currentGroup
	 * @return
	 */
	protected abstract boolean processGroup(Context context, String groupName, List<Object> currentGroup);
	
	/**
	 * Get the groups map from the {@link Context}
	 * @param context
	 * @return
	 */
	protected synchronized Map<String, List<Object>> getGroupsMap(Context context) {
		return context.as(IContextGrouping.class).getGroupByExpressionsGroupsMap();
	}
	
	/**
	 * Remove the groups map from the {@link Context}, and close the
	 * temporary cache.
	 * @param context
	 */
	protected void removeGroupsMap(Context context) {
		context.as(IContextGrouping.class).getGroupByExpressionsGroupsMap().clear();
		context.as(IContextGrouping.class).getGroupByExpressionsMapDB().close();
	}
	
	/**
	 * Add a new group entry. This will dynamically add the group entry
	 * to the groups map if it does not yet exist. Depending on the
	 * disk-backed map implementation, updated values may not be stored,
	 * so we explicitly create a new value list and overwrite the existing
	 * map entry.
	 * @param context
	 * @param key
	 * @param value
	 */
	protected synchronized void addGroupEntry(Context context, String key, Object value) {
		Map<String, List<Object>> map = getGroupsMap(context);
		List<Object> cachedList = map.getOrDefault(key, new ArrayList<Object>());
		List<Object> newList = new ArrayList<Object>(cachedList);
		newList.add(value);
		map.put(key, newList);
	}
	
	/**
	 * Get the configured expression for determining the root object from the {@link Context}
	 * @return
	 */
	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	/**
	 * Set the expression for determining the root object from the {@link Context}
	 * @param rootExpression
	 */
	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	/**
	 * Get the configured expression for determining the group key from the root object
	 * @return
	 */
	public TemplateExpression getGroupTemplateExpression() {
		return groupTemplateExpression;
	}

	/**
	 * Set the configured expression for determining the group key from the root object
	 * @param groupTemplateExpression
	 */
	public void setGroupTemplateExpression(TemplateExpression groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
	}
	
	/**
	 * Subclasses can override this method to force grouping, disallowing users from
	 * disabling grouping based on the 'DisableGrouping' context property.
	 * @return
	 */
	public boolean isForceGrouping() {
		return false;
	}

	/**
	 * Interface for type-safe access to grouping-related data in the {@link Context}
	 */
	private static interface IContextGrouping {
		public static final String PRP_DISABLE_GROUPING = "DisableGrouping";
		public void setDisableGrouping(String disableGrouping);
		public String getDisableGrouping();
		public void setGroupByExpressionsMapDB(DB db);
		public DB getGroupByExpressionsMapDB();
		public void setGroupByExpressionsGroupsMap(Map<String, List<Object>> groups);
		public Map<String, List<Object>> getGroupByExpressionsGroupsMap();
	}
}
