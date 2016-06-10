package com.fortify.processrunner.processor;

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
 * This {@link IProcessor} implementation allows for collecting
 * and grouping data contained in the {@link Context} during the 
 * {@link Phase#PROCESS} phase. During the {@link Phase#POST_PROCESS}
 * phase, the configured group processor will then be invoked for
 * each individual group. The group processor can then access the
 * objects contained in the current group using the 
 * {@link IContextGrouping#getCurrentGroup()} method.
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
		return true;
	}
	
	protected boolean process(Context context) {
		IContextGrouping contextGrouping = context.as(IContextGrouping.class);
		Object rootObject = SpringExpressionUtil.evaluateExpression(context, getRootExpression(), Object.class);
		String groupKey = SpringExpressionUtil.evaluateExpression(rootObject, getGroupTemplateExpression(), String.class);
		addGroupObject(contextGrouping, groupKey, rootObject);
		return true;
	}
	
	protected boolean postProcess(Context context) {
		IContextGrouping contextGrouping = context.as(IContextGrouping.class);
		MultiValueMap<String, Object> groups = getGroups(contextGrouping);
		logStatistics(contextGrouping, groups);
		
		IProcessor processor = getGroupProcessor();
		if ( !processor.process(Phase.PRE_PROCESS, context) ) {
			return false;
		}
		for ( Map.Entry<String, List<Object>> group : groups.entrySet() ) {
			contextGrouping.setCurrentGroup(group.getValue());
			if ( !processor.process(Phase.PROCESS, context) ) {
				return false;
			};
		}
		return processor.process(Phase.POST_PROCESS, context);
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
		public static final String PRP_GROUP_TEMPLATE_EXPRESSION = "GroupTemplateExpression";
		public static final String PRP_GROUPS = "Groups";
		public static final String PRP_TOTAL_COUNT = "TotalCount";
		public static final String PRP_CURRENT_GROUP = "CurrentGroup";
		
		public void setGroupTemplateExpression(String groupTemplateExpression);
		public String getGroupTemplateExpression();
		
		public void setGroups(MultiValueMap<String, Object> groups);
		public MultiValueMap<String, Object> getGroups();
		
		public void setTotalCount(int count);
		public int getTotalCount();
		
		public void setCurrentGroup(Collection<Object> group);
		public Collection<Object> getCurrentGroup();
	}
}
