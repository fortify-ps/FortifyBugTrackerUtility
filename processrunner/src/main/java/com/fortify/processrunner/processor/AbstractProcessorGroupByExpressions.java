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
 * 
 * TODO Update JavaDoc as it is no longer valid
 */
public abstract class AbstractProcessorGroupByExpressions extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorGroupByExpressions.class);
	private SimpleExpression rootExpression;
	private TemplateExpression groupTemplateExpression;
	private MultiValueMap<String, Object> groups;
	private int totalCount = 0;
	
	/**
	 * Add context properties for grouping
	 */
	@Override
	public final void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		if ( getGroupTemplateExpression()!=null ) {
			contextProperties.add(new ContextProperty(IContextGrouping.PRP_ENABLE_GROUPING, "Enable grouping of vulnerabilities", context, "false", false));
		}
		addExtraContextProperties(contextProperties, context);
	}
	
	/**
	 * Subclasses can override this method to add extra context properties
	 * @param contextProperties
	 * @param context
	 */
	protected void addExtraContextProperties(Collection<ContextProperty> contextProperties, Context context) {}

	@Override
	protected final boolean preProcess(Context context) {
		groups = new LinkedMultiValueMap<String, Object>();
		totalCount = 0;
		return preProcessBeforeGrouping(context);
	}
	
	protected final boolean process(Context context) {
		if ( processBeforeGrouping(context)==false ) {
			return false;
		}
		
		totalCount++;
		SimpleExpression rootExpression = getRootExpression();
		TemplateExpression groupTemplateExpression = getGroupTemplateExpression();
		Object rootObject = SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
		
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("[Process] Current object: "+rootObject);
		}
		
		if ( !isGroupingEnabled(context) ) {
			// If grouping is not enabled, we directly process the 
			// group since we do not need to group the data first.
			return processGroup(context, Arrays.asList(new Object[]{rootObject}));
		} else {
			// If a group template expression is defined, we collect the group
			// data and invoke the process() method of the group processor
			// in our postProcess() method once all data has been grouped.
			String groupKey = SpringExpressionUtil.evaluateExpression(rootObject, groupTemplateExpression, String.class);
			groups.add(groupKey, rootObject);
			return true;
		}
	}

	private boolean isGroupingEnabled(Context context) {
		String enableGrouping = context.as(IContextGrouping.class).getEnableGrouping();
		return getGroupTemplateExpression() != null && "true".equals(enableGrouping);
	}

	protected final boolean postProcess(Context context) {
		boolean result = true;
		if ( isGroupingEnabled(context) ) {
			// If a group template expression is defined, we call the
			// process() method on the group processor for every
			// group that we have collected.
			logStatistics();
			
			for ( Map.Entry<String, List<Object>> group : groups.entrySet() ) {
				if ( !processGroup(context, group.getValue()) ) {
					result = false; break; // Stop processing remainder of groups
				};
			}
			
		}
		groups.clear();
		return postProcessAfterProcessingGroups(context) && result;
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
	
	protected abstract boolean processGroup(Context context, List<Object> currentGroup);
	
	protected void logStatistics() {
		logGroupsInfo();
		logMemoryUsage();
	}
	
	protected void logGroupsInfo() {
		LOG.info("[Process] Grouped "+totalCount+" items in "+(groups==null?0:groups.size())+" groups"); 
	}

	protected void logMemoryUsage() {
		if ( groups != null ) {
			try {
				LOG.info("[Process] Grouped data memory usage: "+MemoryUtil.deepMemoryUsageOf(groups)+" bytes");
			} catch ( IllegalStateException e ) {
				LOG.debug("[Process] Agent unavailable; memory information cannot be displayed.\n"
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
	
	private static interface IContextGrouping {
		public static final String PRP_ENABLE_GROUPING = "EnableGrouping";
		public void setEnableGrouping(String enableGrouping);
		public String getEnableGrouping();
	}
}
