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

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;

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

	@SuppressWarnings("unchecked")
	@Override
	protected final boolean preProcess(Context context) {
		IContextGrouping ctx = context.as(IContextGrouping.class);
		DB db = DBMaker.tempFileDB()
				.closeOnJvmShutdown().fileDeleteAfterClose()
				.fileMmapEnableIfSupported()
				.make();
		Map<String, List<Object>> groups = db.hashMap("groups", Serializer.STRING, Serializer.JAVA).create();
		groups.clear(); // Make sure that we start with a clean cache 
		ctx.setGroupByExpressionsMapDB(db);
		ctx.setGroupByExpressionsGroupsMap(groups);
		return preProcessBeforeGrouping(context);
	}
	
	protected final boolean process(Context context) {
		if ( processBeforeGrouping(context)==false ) {
			return false;
		}
		
		//totalCount++;
		SimpleExpression rootExpression = getRootExpression();
		TemplateExpression groupTemplateExpression = getGroupTemplateExpression();
		Object rootObject = SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
		
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
			addGroupEntry(context, groupKey, rootObject);
			return true;
		}
	}

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
	
	protected abstract boolean processGroup(Context context, String groupName, List<Object> currentGroup);
	
	protected synchronized Map<String, List<Object>> getGroupsMap(Context context) {
		return context.as(IContextGrouping.class).getGroupByExpressionsGroupsMap();
	}
	
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
	
	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public TemplateExpression getGroupTemplateExpression() {
		return groupTemplateExpression;
	}

	public void setGroupTemplateExpression(TemplateExpression groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
	}
	
	public boolean isForceGrouping() {
		return false;
	}

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
