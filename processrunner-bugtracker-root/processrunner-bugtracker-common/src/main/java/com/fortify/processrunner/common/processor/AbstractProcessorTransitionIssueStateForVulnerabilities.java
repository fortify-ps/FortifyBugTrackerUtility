package com.fortify.processrunner.common.processor;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;

public abstract class AbstractProcessorTransitionIssueStateForVulnerabilities<IssueStateType>
		extends AbstractProcessorUpdateIssueStateForVulnerabilities<IssueStateType> {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorTransitionIssueStateForVulnerabilities.class);
	private LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsForOpeningIssue;
	private LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsForClosingIssue;

	@Override
	protected boolean openIssue(Context context, SubmittedIssue submittedIssue, IssueStateType currentIssueState) {
		List<TransitionWithComment> transitions = getTransitions(context, submittedIssue, currentIssueState, getTransitionsForOpeningIssue());
		return transition(context, submittedIssue, currentIssueState, transitions);
	}
	
	@Override
	protected boolean closeIssue(Context context, SubmittedIssue submittedIssue, IssueStateType currentIssueState) {
		List<TransitionWithComment> transitions = getTransitions(context, submittedIssue, currentIssueState, getTransitionsForClosingIssue());
		return transition(context, submittedIssue, currentIssueState, transitions);
	}

	protected List<TransitionWithComment> getTransitions(Context context, SubmittedIssue submittedIssue, IssueStateType currentIssueState, LinkedHashMap<SimpleExpression, List<TransitionWithComment>> transitionsMap) {
		if ( transitionsMap != null ) {
			for ( Map.Entry<SimpleExpression, List<TransitionWithComment>> entry : transitionsMap.entrySet() ) {
				if ( SpringExpressionUtil.evaluateExpression(currentIssueState, entry.getKey(), Boolean.class) ) {
					return entry.getValue();
				}
			}
		}
		return null;
	}
	
	protected boolean transition(Context context, SubmittedIssue submittedIssue, IssueStateType currentIssueState, List<TransitionWithComment> transitions) {
		if ( transitions==null ) {
			LOG.debug(String.format("[%s] No transitions found for issue %s with issue state %s", getBugTrackerName(), submittedIssue.getDeepLink(), currentIssueState));
			return false; 
		}
		for ( TransitionWithComment transition : transitions ) {
			if ( !transition(context, submittedIssue, transition.getName(), transition.getComment()) ) {
				LOG.warn(String.format("[%s] Transition %s for issue %s failed, please update issue status manually", getBugTrackerName(), transition.getName(), submittedIssue.getDeepLink()));
				return false;
			}
		}
		return true; 
	}
	
	protected abstract boolean transition(Context context, SubmittedIssue submittedIssue, String transitionName, String comment);

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
