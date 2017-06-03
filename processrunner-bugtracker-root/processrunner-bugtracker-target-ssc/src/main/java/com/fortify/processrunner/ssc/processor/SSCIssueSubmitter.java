package com.fortify.processrunner.ssc.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.processor.AbstractProcessorBuildObjectMapFromGroupedObjects;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCTarget;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;

public class SSCIssueSubmitter extends AbstractProcessorBuildObjectMapFromGroupedObjects {
	private static final Log LOG = LogFactory.getLog(SSCIssueSubmitter.class);
	private String shortName;
	private String userName;
	private String password;
	
	public SSCIssueSubmitter() {
		setRootExpression(SpringExpressionUtil.parseSimpleExpression("CurrentVulnerability"));
	}
	
	@Override
	protected void addExtraContextPropertyDefinitions(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		String name = getShortName();
		if ( name!=null ) {
			contextPropertyDefinitions.add(new ContextPropertyDefinition(name+"UserName", name+" user name", context, "Read from console", false));
			contextPropertyDefinitions.add(new ContextPropertyDefinition(name+"Password", name+" password", context, "Read from console", false));
		}
	}
	
	@Override
	protected boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map) {
		IContextSSCTarget ctx = context.as(IContextSSCTarget.class);
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		String applicationVersionId = ctx.getSSCApplicationVersionId();
		if ( conn.isBugTrackerAuthenticationRequired(applicationVersionId) ) {
			conn.authenticateForBugFiling(applicationVersionId, getUserName(context), getPassword(context));
		}
		List<String> issueInstanceIds = new ArrayList<String>();
		for ( Object issue : currentGroup ) {
			issueInstanceIds.add(SpringExpressionUtil.evaluateExpression(issue, "issueInstanceId", String.class));
		}
		JSONObject result = conn.fileBug(ctx.getSSCApplicationVersionId(), map, issueInstanceIds);
		String bugLink = SpringExpressionUtil.evaluateExpression(result, "data?.values?.externalBugDeepLink", String.class);
		LOG.info(String.format("[SSC] Submitted %d vulnerabilities via SSC to %s", currentGroup.size(), bugLink));
		return true;
	}
	
	private String getUserName(Context context) {
		String result = null;
		String name = getShortName();
		if ( name!=null ) {
			result = (String)context.get(name+"UserName"); 
		}
		if ( result == null ) {
			result = getUserName();
		}
		if ( result == null ) {
			result = System.console().readLine(getShortName()+" User Name: ");
			setUserName(result);
		}
		return result;
	}
	
	private String getPassword(Context context) {
		String result = null;
		String name = getShortName();
		if ( name!=null ) {
			result = (String)context.get(name+"UserName"); 
		}
		if ( result == null ) {
			result = getPassword();
		}
		if ( result == null ) {
			result = new String(System.console().readPassword(getShortName()+" Password: "));
			setPassword(result);
		}
		return result;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
