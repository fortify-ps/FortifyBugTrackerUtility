package com.fortify.processrunner.processor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;
import com.javamex.classmexer.MemoryUtil;

/**
 * <p>This {@link IProcessor} implementation allows for collecting
 * and grouping data stored in the {@link Context} during the 
 * {@link Phase#PROCESS} phase. The rootExpression property defines 
 * the expression used to retrieve each root object from the 
 * {@link Context}. The optional groupTemplateExpression is 
 * evaluated on each root object to identify the group that this
 * root object belongs to.</p>
 * 
 * <p>During the {@link Phase#POST_PROCESS}
 * phase, the configured group processor will be invoked for each 
 * individual group. The group processor can then access the root
 * objects contained in the current group using the 
 * {@link IContextGrouping#getCurrentGroup()} method.</p>
 * 
 * <p>If no grouping expression has been defined, the group processor
 * will be invoked immediately for every individual root object.
 * Just like grouped data, the group processor can then access this
 * root object using the {@link IContextGrouping#getCurrentGroup()} method.</p>
 */
public class ProcessorGroupByExpressions extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(ProcessorGroupByExpressions.class);
	private SimpleExpression rootExpression;
	private TemplateExpression groupTemplateExpression;
	private IProcessor groupProcessor = new CompositeProcessor();
	
	@Override
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		groupProcessor.addContextProperties(contextProperties, context);
	}
	
	@Override
	protected boolean preProcess(Context context) {
		// Initialize the count of the total number of entries that have been grouped
		context.as(IContextGrouping.class).setTotalCount(0);
		// Invoke the pre-process phase on the group processor
		return getGroupProcessor().process(Phase.PRE_PROCESS, context);
	}
	
	protected boolean process(Context context) {
		IContextGrouping contextGrouping = context.as(IContextGrouping.class);
		SimpleExpression rootExpression = getRootExpression();
		TemplateExpression groupTemplateExpression = getGroupTemplateExpression();
		Object rootObject = SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
		
		if ( groupTemplateExpression == null ) {
			// If no group template expression is defined, we directly invoke
			// the group processor since we do not need to group the data first.
			contextGrouping.setCurrentGroup(Arrays.asList(new Object[]{rootObject}));
			contextGrouping.setTotalCount(contextGrouping.getTotalCount()+1);
			return getGroupProcessor().process(Phase.PROCESS, context);
		} else {
			// If a group template expression is defined, we collect the group
			// data and invoke the process() method of the group processor
			// in our postProcess() method once all data has been grouped.
			String groupKey = SpringExpressionUtil.evaluateExpression(rootObject, groupTemplateExpression, String.class);
			addGroupObject(contextGrouping, groupKey, rootObject);
			return true;
		}
	}
	
	protected boolean postProcess(Context context) {
		IProcessor groupProcessor = getGroupProcessor();
		boolean result = true;
		if ( getGroupTemplateExpression() != null ) {
			// If a group template expression is defined, we call the
			// process() method on the group processor for every
			// group that we have collected.
			IContextGrouping contextGrouping = context.as(IContextGrouping.class);
			MultiValueMap<String, Object> groups = getGroups(contextGrouping);
			logStatistics(contextGrouping, groups);
			
			for ( Map.Entry<String, List<Object>> group : groups.entrySet() ) {
				contextGrouping.setCurrentGroup(group.getValue());
				if ( !groupProcessor.process(Phase.PROCESS, context) ) {
					result = false; break; // Stop processing remainder of groups
				};
			}
			
		}
		return groupProcessor.process(Phase.POST_PROCESS, context) && result;
	}

	protected void addGroupObject(IContextGrouping context, String groupKey, Object groupedObject) {
		MultiValueMap<String, Object> groups = getGroups(context);
		groups.add(groupKey, groupedObject);
		context.setTotalCount(context.getTotalCount()+1);
	}

	protected MultiValueMap<String, Object> getGroups(IContextGrouping context) {
		MultiValueMap<String, Object> groups = context.getGroups();
		if ( groups == null ) {
			groups = new LinkedMultiValueMap<String, Object>();
			context.setGroups(groups);
		}
		return groups;
	}
	
	protected void logStatistics(IContextGrouping context, MultiValueMap<String, Object> groups) {
		logGroupsInfo(context, groups);
		logMemoryUsage(groups);
	}
	
	protected void logGroupsInfo(IContextGrouping context, MultiValueMap<String, Object> groups) {
		LOG.info("Grouped "+context.getTotalCount()+" items in "+(groups==null?0:groups.size())+" groups"); 
	}

	protected void logMemoryUsage(MultiValueMap<String, Object> groups) {
		if ( groups != null ) {
			try {
				LOG.info("Grouped data memory usage: "+MemoryUtil.deepMemoryUsageOf(groups)+" bytes");
			} catch ( IllegalStateException e ) {
				LOG.debug("Agent unavailable; memory information cannot be displayed.\n"
						+"To enable memory information, add -javaagent:path/to/classmexer-0.03.jar to Java command line.\n"
						+"Classmexer can be downloaded from http://www.javamex.com/classmexer/classmexer-0_03.zip");
			}
		}
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

	public IProcessor getGroupProcessor() {
		return groupProcessor;
	}

	public void setGroupProcessor(IProcessor groupProcessor) {
		this.groupProcessor = groupProcessor;
	}

	public static interface IContextGrouping {
		public static final String PRP_GROUPS = "Groups";
		public static final String PRP_TOTAL_COUNT = "TotalCount";
		public static final String PRP_CURRENT_GROUP = "CurrentGroup";
		
		public void setGroups(MultiValueMap<String, Object> groups);
		public MultiValueMap<String, Object> getGroups();
		
		public void setTotalCount(int count);
		public int getTotalCount();
		
		public void setCurrentGroup(Collection<Object> group);
		public Collection<Object> getCurrentGroup();
	}
}
