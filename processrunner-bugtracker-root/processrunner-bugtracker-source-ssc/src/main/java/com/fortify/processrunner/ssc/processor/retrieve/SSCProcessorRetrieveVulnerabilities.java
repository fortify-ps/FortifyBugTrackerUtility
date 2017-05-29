package com.fortify.processrunner.ssc.processor.retrieve;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.context.IContextCurrentVulnerability;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSC;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.sun.jersey.api.client.WebResource;

/**
 * <p>This {@link IProcessor} implementation retrieves a list of
 * vulnerabilities from SSC for the application version id specified 
 * as a {@link Context} property.</p>
 * 
 * <p>For each individual vulnerability, the {@link IProcessor} 
 * implementation configured via 
 * {@link #setVulnerabilityProcessor(IProcessor)} will be called
 * to process the current vulnerability. The current vulnerability
 * can be accessed by the vulnerability processor using the
 * 'CurrentVulnerability' {@link Context} property.</p>
 * 
 * <p>If the 'SSCTopLevelFilterParamValue' {@link Context} property 
 * has been set (usually by adding filters via 
 * {@link SSCFilterOnTopLevelFields}), this filter parameter value 
 * will be passed on to SSC to allow SSC to filter the vulnerabilities 
 * being returned.</p>
 */
public class SSCProcessorRetrieveVulnerabilities extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(SSCProcessorRetrieveVulnerabilities.class);
	private static final String KEY_START = "SSCProcessorRootVulnerabilityArray_start";
	private static final SimpleExpression EXPR_COUNT = SpringExpressionUtil.parseSimpleExpression("count");
	private String uriTemplateExpression = "api/v1/projectVersions/${SSCApplicationVersionId}/issues?qm=issues&limit=50&offset=${"+KEY_START+"}";
	private SimpleExpression rootExpression = SpringExpressionUtil.parseSimpleExpression("data");
	private boolean includeRemoved;
	private IProcessor vulnerabilityProcessor;
	
	@Override
	public void addContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty("SSCApplicationVersionId","SSC application version id from which to retrieve vulnerabilities",context,null,true));
		if ( vulnerabilityProcessor != null ) {
			vulnerabilityProcessor.addContextProperties(contextProperties, context);
		}
	}
	
	public SSCProcessorRetrieveVulnerabilities() {
		vulnerabilityProcessor = new CompositeProcessor();
	}
	
	public SSCProcessorRetrieveVulnerabilities(IProcessor... processors) {
		vulnerabilityProcessor = new CompositeProcessor(processors);
	}
	
	@Override
	public boolean process(Context context) {
		IProcessor processor = getVulnerabilityProcessor();
		processor.process(Phase.PRE_PROCESS, context);
		IContextSSC contextSSC = context.as(IContextSSC.class);
		IContextCurrentVulnerability contextCurrentVulnerability = context.as(IContextCurrentVulnerability.class);
		IRestConnection conn = SSCConnectionFactory.getConnection(context);
		String filterParamValue = contextSSC.getSSCTopLevelFilterParamValue();
		String filterParam = StringUtils.isBlank(filterParamValue)?"":"&q="+filterParamValue;
		LOG.info("[SSC] Retrieving vulnerabilities for application version id "+contextSSC.getSSCApplicationVersionId()+" from "+conn.getBaseUrl());
		int start=0;
		int count=50;
		while ( start < count ) {
			LOG.info("[SSC] Loading next set of data");
			context.put(KEY_START, start);
			URI uri = SpringExpressionUtil.evaluateTemplateExpression(context, getUriTemplateExpression()+filterParam, URI.class);
			WebResource resource = conn.getBaseResource().uri(uri);
			if ( isIncludeRemoved() ) {
				// TODO Implement this for SSC; probably have to do a PUT request on issueSearchOptions first
				resource = resource.queryParam("includeFixed", "true").queryParam("includeSuppressed", "true");
			}
			LOG.debug("[SSC] Retrieving vulnerabilities from "+resource);
			JSONObject data = conn.executeRequest(HttpMethod.GET, resource, JSONObject.class);
			count = SpringExpressionUtil.evaluateExpression(data, EXPR_COUNT, Integer.class);
			JSONArray vulnerabilitiesArray = SpringExpressionUtil.evaluateExpression(data, getRootExpression(), JSONArray.class);
			start += vulnerabilitiesArray.length();
			for ( int i = 0 ; i < vulnerabilitiesArray.length() ; i++ ) {
				JSONObject vuln = vulnerabilitiesArray.optJSONObject(i);
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("[SSC] Processing vulnerability "+vuln.optString("vulnId"));
				}
				contextCurrentVulnerability.setCurrentVulnerability(vuln);
				// We ignore the boolean result as we want to continue processing next vulnerabilities
				processor.process(Phase.PROCESS, context);
				contextCurrentVulnerability.setCurrentVulnerability(null);
			}
			context.remove(KEY_START);
		}
		return processor.process(Phase.POST_PROCESS, context);
	}
	
	public String getUriTemplateExpression() {
		return uriTemplateExpression;
	}

	public void setUriTemplateExpression(String uriTemplateExpression) {
		this.uriTemplateExpression = uriTemplateExpression;
	}

	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public IProcessor getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	public void setVulnerabilityProcessor(IProcessor vulnerabilityProcessor) {
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}

	public boolean isIncludeRemoved() {
		return includeRemoved;
	}

	public void setIncludeRemoved(boolean includeRemoved) {
		this.includeRemoved = includeRemoved;
	}
	
	
}
