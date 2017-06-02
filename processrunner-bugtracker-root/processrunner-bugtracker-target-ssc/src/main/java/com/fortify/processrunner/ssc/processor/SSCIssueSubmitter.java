package com.fortify.processrunner.ssc.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCTarget;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;

public class SSCIssueSubmitter extends AbstractProcessorBuildObjectMapFromGroupedObjects {
	public SSCIssueSubmitter() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		IContextSSCTarget ctx = context.as(IContextSSCTarget.class);
		List<String> issueInstanceIds = new ArrayList<String>();
		for ( Object issue : currentGroup ) {
			issueInstanceIds.add(SpringExpressionUtil.evaluateExpression(issue, "issueInstanceId", String.class));
		}
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		conn.fileBug(ctx.getSSCApplicationVersionId(), map, issueInstanceIds, ctx.getSSCBugTrackerUserName(), ctx.getSSCBugTrackerPassword());
		// TODO Output status information
		return true;
	}
}
