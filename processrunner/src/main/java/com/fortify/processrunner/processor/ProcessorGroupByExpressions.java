package com.fortify.processrunner.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.util.spring.SpringExpressionUtil;

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
	private String rootExpression;
	private String groupTemplateExpression;
	private IProcessor groupProcessor = new CompositeProcessor();
	
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		return groupProcessor.getContextProperties(context);
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
		String groupKey = SpringExpressionUtil.evaluateTemplateExpression(rootObject, getGroupTemplateExpression(), String.class);
		addGroupObject(contextGrouping, groupKey, rootObject);
		return true;
	}
	
	protected boolean postProcess(Context context) {
		IProcessor processor = getGroupProcessor();
		if ( !processor.process(Phase.PRE_PROCESS, context) ) {
			return false;
		}
		IContextGrouping contextGrouping = context.as(IContextGrouping.class);
		for ( Map.Entry<String, List<Object>> group : getGroups(contextGrouping).entrySet() ) {
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
	
	

	public String getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(String rootExpression) {
		this.rootExpression = rootExpression;
	}

	public String getGroupTemplateExpression() {
		return groupTemplateExpression;
	}

	public void setGroupTemplateExpression(String groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
	}

	public IProcessor getGroupProcessor() {
		return groupProcessor;
	}

	public void setGroupProcessor(IProcessor groupProcessor) {
		this.groupProcessor = groupProcessor;
	}



	public static interface IContextGrouping {
		public void setGroups(MultiValueMap<String, Object> groups);
		public MultiValueMap<String, Object> getGroups();
		
		public void setTotalCount(int count);
		public int getTotalCount();
		
		public void setCurrentGroup(Collection<Object> group);
		public Collection<Object> getCurrentGroup();
	}
}
