/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.bugtracker.common.tgt.processor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.bugtracker.common.processor.IProcessorWithTargetName;
import com.fortify.bugtracker.common.src.updater.IExistingIssueVulnerabilityUpdater;
import com.fortify.bugtracker.common.tgt.config.ITargetUpdateIssuesConfiguration;
import com.fortify.bugtracker.common.tgt.context.IContextBugTracker;
import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsUpdater;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorAndFields;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * <p>This abstract {@link IProcessor} implementation can update issue state for previously submitted issues,
 * based on possibly updated vulnerability data. Based on our superclass {@link AbstractTargetProcessorGenerateIssueFields}, 
 * we first group all previously submitted vulnerabilities by the corresponding external system issue link, and then 
 * update the bug tracker issue based on current vulnerability state.</p>
 * 
 * Concrete implementations can override the following methods to add support for the corresponding
 * functionality:
 * <ul>
 *  <li>{@link #updateIssueFields(Context, TargetIssueLocator, LinkedHashMap)}: Update existing issue fields with updated values</li>
 *  <li>{@link #openIssue(Context, TargetIssueLocator, Object)}: (Re-)open an issue if it is currently closed but corresponding 
 *      vulnerabilities are still open</li>
 *  <li>{@link #closeIssue(Context, TargetIssueLocator, Object)}: Close an issue if it is currently open but corresponding 
 *      vulnerabilities have been closed/fixed</li>
 * </ul>
 * Note that if a bug tracker uses transitioning schemes for managing issue state, it would be better to subclass
 * {@link AbstractTargetProcessorUpdateIssuesWithTransitions} instead, as it allows for configurable state
 * transitions to open and close issues.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractTargetProcessorUpdateIssues extends AbstractTargetProcessor implements IProcessorWithTargetName, ITargetProcessorUpdateIssues {
	private static final Log LOG = LogFactory.getLog(AbstractTargetProcessorUpdateIssues.class);
	private IExistingIssueVulnerabilityUpdater vulnerabilityUpdater;
	private SimpleExpression isVulnStateOpenExpression;
	private SimpleExpression vulnBugIdExpression;
	private SimpleExpression vulnBugLinkExpression;
	
	/**
	 * This constructor sets the root expression on our parent to 'CurrentVulnerability'
	 */
	public AbstractTargetProcessorUpdateIssues() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	/**
	 * Add the bug tracker name to the current context, and call 
	 * {@link #addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions, Context)}
	 * to allow subclasses to add additional context property definitions
	 */
	@Override
	public final void addExtraContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		// TODO Decide on whether we want the user to be able to override the bug tracker name via the context
		// contextPropertyDefinitions.add(new ContextProperty(IContextBugTracker.PRP_BUG_TRACKER_NAME, "Bug tracker name", context, getBugTrackerName(), false));
		context.as(IContextBugTracker.class).setBugTrackerName(getTargetName());
		addBugTrackerContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	/**
	 * Subclasses can override this method to add additional bug tracker related {@link ContextPropertyDefinitions}
	 * @param contextPropertyDefinitions
	 * @param context
	 */
	protected void addBugTrackerContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {}
	
	/**
	 * Autowire the configuration from the Spring configuration file.
	 * @param config
	 */
	@Autowired
	public void setConfiguration(ITargetUpdateIssuesConfiguration config) {
		super.setFields(config.getFieldsForUpdate());
		super.setAppendedFields(config.getAppendedFieldsForUpdate());
	}
	
	/**
	 * Process the current group of vulnerabilities (grouped by bug tracker deep link) to update the corresponding
	 * previously submitted issue. This includes updating issue fields, re-opening issues if they have been closed
	 * but there are open vulnerabilities, and closing issues if they are open but no open vulnerabilities are remaining.
	 */
	@Override
	protected boolean processMap(Context context, List<Object> vulnerabilities, LinkedHashMap<String, Object> issueData) {
		TargetIssueLocator targetIssueLocator = getTargetIssueLocator(vulnerabilities.get(0));
		TargetIssueLocatorAndFields targetIssueLocatorAndFields = getTargetIssueLocatorAndFields(context, targetIssueLocator);
		boolean targetIssueUpdated = false;
		targetIssueUpdated |= updateIssueFieldsIfNecessary(context, targetIssueLocatorAndFields, issueData);
		
		if ( hasOpenVulnerabilities(vulnerabilities) ) {
			targetIssueUpdated |= openIssueIfNecessary(context, targetIssueLocatorAndFields);
		} else {
			targetIssueUpdated |= closeIssueIfNecessary(context, targetIssueLocatorAndFields);
		}
		
		if ( !targetIssueUpdated ) {
			LOG.info(String.format("[%s] No updates needed for issue %s", getTargetName(), targetIssueLocator.getDeepLink()));
		}
		
		if ( vulnerabilityUpdater!=null ) {
			vulnerabilityUpdater.updateVulnerabilityStateForExistingIssue(context, getTargetName(), targetIssueLocatorAndFields, vulnerabilities);
		}
		
		return true;
	}

	private boolean updateIssueFieldsIfNecessary(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields, LinkedHashMap<String, Object> issueFields) {
		ITargetIssueFieldsUpdater updater = getTargetIssueFieldsUpdater();
		if ( updater!=null && MapUtils.isNotEmpty(issueFields) ) {
			if ( targetIssueLocatorAndFields.canRetrieveFields() ) {
				issueFields = removeUnmodifiedFields(targetIssueLocatorAndFields, issueFields);
			}
			if ( MapUtils.isNotEmpty(issueFields) && updater.updateIssueFields(context, targetIssueLocatorAndFields, new LinkedHashMap<String, Object>(issueFields)) ) {
				LOG.info(String.format("[%s] Updated field(s) %s for issue %s", getTargetName(), issueFields.keySet().toString(), targetIssueLocatorAndFields.getLocator().getDeepLink()));
				targetIssueLocatorAndFields.resetFields();
				return true;
			}
		}
		return false;
	}
	
	private LinkedHashMap<String, Object> removeUnmodifiedFields(TargetIssueLocatorAndFields targetIssueLocatorAndFields, LinkedHashMap<String, Object> issueFieldsForUpdate) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		JSONMap currentIssueFields = targetIssueLocatorAndFields.getFields();
		for ( Map.Entry<String, Object> entry : issueFieldsForUpdate.entrySet() ) {
			Object currentValue = currentIssueFields.get(entry.getKey());
			if ( currentValue==null || areFieldValuesDifferent(currentValue, entry.getValue()) ) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Compare the current field value from the target against the new value generated
	 * based on configuration. If both values are instances of String, this default
	 * implementation ignores any whitespace and (HTML) tags, and ignores any 
	 * surrounding text/elements in the current field value from the target.
	 * @param valueFromTarget
	 * @param valueFromConfig
	 * @return true if field values are different, false if they are the same
	 */
	protected boolean areFieldValuesDifferent(Object valueFromTarget, Object valueFromConfig) {
		if ( valueFromTarget.equals(valueFromConfig) ) { 
			return false; 
		} else if ( valueFromTarget instanceof String && valueFromConfig instanceof String ) {
			String valueFromTargetString = (String)valueFromTarget;
			String valueFromConfigString = (String)valueFromConfig;
			valueFromTargetString = StringUtils.deleteWhitespace(valueFromTargetString).replaceAll("\\<.*?\\>", "");
			valueFromConfigString = StringUtils.deleteWhitespace(valueFromConfigString).replaceAll("\\<.*?\\>", "");
			
			return !valueFromTargetString.contains(valueFromConfigString);
		}
		return true;
	}

	private boolean openIssueIfNecessary(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields) {
		if ( openIssueIfClosed(context, targetIssueLocatorAndFields) ) {
			LOG.info(String.format("[%s] Re-opened issue %s", getTargetName(), targetIssueLocatorAndFields.getLocator().getDeepLink()));
			targetIssueLocatorAndFields.resetFields();
			return true;
		}
		return false;
	}
	
	private boolean closeIssueIfNecessary(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields) {
		if ( closeIssueIfOpen(context, targetIssueLocatorAndFields) ) {
			LOG.info(String.format("[%s] Closed issue %s", getTargetName(), targetIssueLocatorAndFields.getLocator().getDeepLink()));
			targetIssueLocatorAndFields.resetFields();
			return true;
		}
		return false;
	}

	/**
	 * Get information about the previously submitted issue from the current vulnerability.
	 * @param vulnerability
	 * @return
	 */
	protected TargetIssueLocator getTargetIssueLocator(Object vulnerability) {
		TargetIssueLocator result = new TargetIssueLocator(
			SpringExpressionUtil.evaluateExpression(vulnerability, vulnBugIdExpression, String.class),
			SpringExpressionUtil.evaluateExpression(vulnerability, vulnBugLinkExpression, String.class)
		);
		return result;
	}
	
	/**
	 * Subclasses can override this method to add support for updating bug tracker issue fields
	 * @return {@link ITargetIssueFieldsUpdater} instance, or null if updating issue fields is not supported  
	 */
	protected ITargetIssueFieldsUpdater getTargetIssueFieldsUpdater() {
		return null;
	}
	
	/**
	 * Subclasses can override this method to add support for re-opening closed issues
	 * @param context The current context
	 * @param targetIssueLocatorAndFields provides access to target issue locator and fields
	 * @return true if target issue state has been changed, false otherwise
	 */
	protected boolean openIssueIfClosed(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields) {
		return false;
	}

	/**
	 * Subclasses can override this method to add support for closing issues
	 * @param context The current context
	 * @param targetIssueLocatorAndFields provides access to target issue locator and fields
	 * @return true if target issue state has been changed, false otherwise
	 */
	protected boolean closeIssueIfOpen(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields) {
		return false;
	}

	public abstract String getTargetName();
	
	private boolean hasOpenVulnerabilities(List<Object> currentGroup) {
		for ( Object o : currentGroup ) {
			if ( SpringExpressionUtil.evaluateExpression(o, getIsVulnStateOpenExpression(), Boolean.class) ) {
				return true;
			}
		}
		return false;
	}

	public SimpleExpression getIsVulnStateOpenExpression() {
		return isVulnStateOpenExpression;
	}

	/* (non-Javadoc)
	 * @see com.fortify.bugtracker.common.tgt.processor.ITargetProcessorUpdateIssues#setIsVulnStateOpenExpression(com.fortify.util.spring.expression.SimpleExpression)
	 */
	@Override
	public void setIsVulnStateOpenExpression(SimpleExpression isVulnStateOpenExpression) {
		this.isVulnStateOpenExpression = isVulnStateOpenExpression;
	}

	/**
	 * @return the bugIdExpression
	 */
	public SimpleExpression getVulnBugIdExpression() {
		return vulnBugIdExpression;
	}

	/* (non-Javadoc)
	 * @see com.fortify.bugtracker.common.tgt.processor.ITargetProcessorUpdateIssues#setVulnBugIdExpression(com.fortify.util.spring.expression.SimpleExpression)
	 */
	@Override
	public void setVulnBugIdExpression(SimpleExpression vulnBugIdExpression) {
		this.vulnBugIdExpression = vulnBugIdExpression;
	}

	/**
	 * @return the bugLinkExpression
	 */
	public SimpleExpression getVulnBugLinkExpression() {
		return vulnBugLinkExpression;
	}

	/* (non-Javadoc)
	 * @see com.fortify.bugtracker.common.tgt.processor.ITargetProcessorUpdateIssues#setVulnBugLinkExpression(com.fortify.util.spring.expression.SimpleExpression)
	 */
	@Override
	public void setVulnBugLinkExpression(SimpleExpression vulnBugLinkExpression) {
		this.vulnBugLinkExpression = vulnBugLinkExpression;
	}
	
	@Override
	public boolean isForceGrouping() {
		return true;
	}
	
	@Autowired(required=false)
	public void setExistingIssueVulnerabilityUpdater(IExistingIssueVulnerabilityUpdater vulnerabilityUpdater) {
		this.vulnerabilityUpdater = vulnerabilityUpdater;
	}
}
