package com.fortify.processrunner.fod.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.processrunner.processor.CompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.sun.jersey.api.client.WebResource;

public class FoDProcessorRetrieveVulnerabilities extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDProcessorRetrieveVulnerabilities.class);
	private static final String KEY_START = "FoDProcessorRootVulnerabilityArray_start";
	private static final SimpleExpression EXPR_COUNT = SpringExpressionUtil.parseSimpleExpression("count");
	private String uriTemplateExpression = "/api/v2/Releases/${[FoDReleaseId]}/Vulnerabilities?limit=50&start=${["+KEY_START+"]}";
	private SimpleExpression rootExpression = SpringExpressionUtil.parseSimpleExpression("data");
	private IProcessor vulnerabilityProcessor;
	
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		result.add(new ContextProperty("FoDReleaseId","FoD release id from which to retrieve vulnerabilities",!context.containsKey("FoDReleaseId")));
		result.addAll(vulnerabilityProcessor.getContextProperties(context));
		return result;
	}
	
	public FoDProcessorRetrieveVulnerabilities() {
		vulnerabilityProcessor = new CompositeProcessor();
	}
	
	public FoDProcessorRetrieveVulnerabilities(IProcessor... processors) {
		vulnerabilityProcessor = new CompositeProcessor(processors);
	}
	
	@Override
	public boolean process(Context context) {
		LOG.debug("Retrieving and processing vulnerabilities");
		IProcessor processor = getVulnerabilityProcessor();
		processor.process(Phase.PRE_PROCESS, context);
		IContextFoD contextFoD = context.as(IContextFoD.class);
		IRestConnection conn = contextFoD.getFoDConnection();
		String filterParamValue = contextFoD.getFoDTopLevelFilterParamValue();
		String filterParam = StringUtils.isBlank(filterParamValue)?"":"&q="+filterParamValue;
		int start=0;
		int count=50;
		while ( start < count ) {
			context.put(KEY_START, start);
			URI uri = SpringExpressionUtil.evaluateTemplateExpression(context, getUriTemplateExpression()+filterParam, URI.class);
			WebResource resource = conn.getBaseResource().uri(uri);
			LOG.debug("Retrieving vulnerabilities from "+resource);
			JSONObject data = conn.executeRequest(HttpMethod.GET, resource, JSONObject.class);
			count = SpringExpressionUtil.evaluateExpression(data, EXPR_COUNT, Integer.class);
			JSONArray vulnerabilitiesArray = SpringExpressionUtil.evaluateExpression(data, getRootExpression(), JSONArray.class);
			start += vulnerabilitiesArray.length();
			for ( int i = 0 ; i < vulnerabilitiesArray.length() ; i++ ) {
				JSONObject vuln = vulnerabilitiesArray.optJSONObject(i);
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Processing vulnerability "+vuln.optString("vulnId"));
				}
				contextFoD.setFoDCurrentVulnerability(vuln);
				// We ignore the boolean result as we want to continue processing next vulnerabilities
				processor.process(Phase.PROCESS, context);
				contextFoD.setFoDCurrentVulnerability(null);
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
	
	
}
