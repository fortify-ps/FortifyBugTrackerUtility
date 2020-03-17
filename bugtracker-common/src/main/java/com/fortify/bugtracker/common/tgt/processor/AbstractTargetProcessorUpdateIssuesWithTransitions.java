/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.bugtracker.common.tgt.config.ITargetUpdateIssuesWithTransitionsConfiguration;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocatorAndFields;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextSpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

/**
 * This abstract class extends {@link AbstractTargetProcessorUpdateIssues} by
 * adding functionality for transitioning previously submitted issues to a new state. To do
 * so, the user can configure possible transitions for opening and closing issues, based on
 * current issue state. Concrete implementations will need to implement the 
 * {@link #transition(Context, TargetIssueLocator, String, String)} method to actually transition
 * the issue.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractTargetProcessorUpdateIssuesWithTransitions
		extends AbstractTargetProcessorUpdateIssues {
	private static final Log LOG = LogFactory.getLog(AbstractTargetProcessorUpdateIssuesWithTransitions.class);
	private LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsForOpeningIssue;
	private LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsForClosingIssue;
	
	/**
	 * Autowire the configuration from the Spring configuration file.
	 * @param config
	 */
	@Autowired
	public void setConfiguration(ITargetUpdateIssuesWithTransitionsConfiguration config) {
		setTransitionsForClosingIssue(config.getTransitionsForClosingIssue());
		setTransitionsForOpeningIssue(config.getTransitionsForOpeningIssue());
	}
	
	@Override
	protected boolean openIssueIfClosed(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields) {
		List<TransitionWithComment> transitions = getTransitions(context, targetIssueLocatorAndFields, getTransitionsForOpeningIssue());
		if ( transitions != null ) {
			return transition(context, targetIssueLocatorAndFields, transitions);
		}
		return false;
	}
	
	@Override
	protected boolean closeIssueIfOpen(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields) {
		List<TransitionWithComment> transitions = getTransitions(context, targetIssueLocatorAndFields, getTransitionsForClosingIssue());
		if ( transitions != null ) {
			return transition(context, targetIssueLocatorAndFields, transitions);
		}
		return false;
	}

	protected List<TransitionWithComment> getTransitions(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields, LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsMap) {
		if ( MapUtils.isNotEmpty(transitionsMap) && targetIssueLocatorAndFields.canRetrieveFields() ) {
			for ( Map.Entry<SimpleExpression, List<TransitionWithComment>> entry : transitionsMap.entrySet() ) {
				if ( ContextSpringExpressionUtil.evaluateExpression(context, targetIssueLocatorAndFields, entry.getKey(), Boolean.class) ) {
					return entry.getValue();
				}
			}
		}
		return null;
	}
	
	protected boolean transition(Context context, TargetIssueLocatorAndFields targetIssueLocatorAndFields, List<TransitionWithComment> transitions) {
		TargetIssueLocator targetIssueLocator = targetIssueLocatorAndFields.getLocator();
		if ( transitions==null ) {
			LOG.debug(String.format("[%s] No transitions found for issue %s", getTargetName(), targetIssueLocator.getDeepLink()));
			return false; 
		}
		for ( TransitionWithComment transition : transitions ) {
			if ( !transition(context, targetIssueLocator, transition.getName(), transition.getComment()) ) {
				LOG.warn(String.format("[%s] Transition %s for issue %s failed, please update issue status manually", getTargetName(), transition.getName(), targetIssueLocator.getDeepLink()));
				return false;
			}
		}
		return true; 
	}
	
	protected abstract boolean transition(Context context, TargetIssueLocator targetIssueLocator, String transitionName, String comment);

	/**
	 * @return the transitionsForOpeningIssue
	 */
	public LinkedHashMap<SimpleExpression, List<TransitionWithComment>> getTransitionsForOpeningIssue() {
		return transitionsForOpeningIssue;
	}

	/**
	 * @param transitionsForOpeningIssue the transitionsForOpeningIssue to set
	 */
	public void setTransitionsForOpeningIssue(
			LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsForOpeningIssue) {
		this.transitionsForOpeningIssue = transitionsForOpeningIssue;
	}

	/**
	 * @return the transitionsForClosingIssue
	 */
	public LinkedHashMap<SimpleExpression, List<TransitionWithComment>> getTransitionsForClosingIssue() {
		return transitionsForClosingIssue;
	}

	/**
	 * @param transitionsForClosingIssue the transitionsForClosingIssue to set
	 */
	public void setTransitionsForClosingIssue(
			LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsForClosingIssue) {
		this.transitionsForClosingIssue = transitionsForClosingIssue;
	}
	
	/**
	 * This class simply holds a transition and corresponding comment. 
	 * Note that this bean has an associated {@link TransitionWithCommentEditor}, 
	 * such that Spring can automatically convert String values into 
	 * TransitionWithComment instances. 
	 */
	public static final class TransitionWithComment {
		private String name;
		private String comment;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getComment() {
			return comment;
		}
		public void setComment(String comment) {
			this.comment = comment;
		}
	}
	
	/**
	 * PropertyEditor for {@link TransitionWithComment}. This property editor supports
	 * converting String values to {@link TransitionWithComment} instances. You can use
	 * one of the following two formats:
	 * <ul>
	 * <li>MyTransition</li>
	 * <li>MyTransition[MyCommentExpression]</li>
	 * </ul>
	 */
	public static final class TransitionWithCommentEditor extends PropertyEditorSupport {
		private static final Pattern pattern = Pattern.compile("^(.+?)(?:\\[(.+)\\])?$");
		@Override
		public void setAsText(String text) throws IllegalArgumentException {
			Matcher m = pattern.matcher(text);
			if ( !m.matches() ) {
				throw new IllegalArgumentException("'"+text+"' is not a valid Transition with Comment. Correct format: 'MyTransition' or 'MyTransition[MyComment]' (without single quotes)");
			}
			String name = m.group(1);
			String comment = StringUtils.isBlank(m.group(2))?null:m.group(2);
			TransitionWithComment twc = new TransitionWithComment();
			twc.setName(name);
			twc.setComment(comment);
			setValue(twc);
		}
	}
}
