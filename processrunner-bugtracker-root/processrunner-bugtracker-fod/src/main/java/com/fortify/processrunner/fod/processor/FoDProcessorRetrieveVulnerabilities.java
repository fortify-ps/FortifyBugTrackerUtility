package com.fortify.processrunner.fod.processor;

import java.net.URI;
import java.net.URISyntaxException;
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
import com.sun.jersey.api.client.WebResource;

public class FoDProcessorRetrieveVulnerabilities extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDProcessorRetrieveVulnerabilities.class);
	private static final String KEY_START = "FoDProcessorRootVulnerabilityArray_start";
	private String uri = "/api/v2/Releases/${[FoDReleaseId]}/Vulnerabilities?start=${["+KEY_START+"]}";
	private String rootExpression = "data";
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
			WebResource resource = conn.getBaseResource()
				.uri(processUri(context, uri+filterParam));
			LOG.debug("Retrieving vulnerabilities from "+resource);
			JSONObject data = conn.executeRequest(HttpMethod.GET, resource, JSONObject.class);
			count = SpringExpressionUtil.evaluateExpression(data, "count", Integer.class);
			JSONArray vulnerabilitiesArray = SpringExpressionUtil.evaluateExpression(data, getRootExpression(), JSONArray.class);
			start += vulnerabilitiesArray.length();
			for ( int i = 0 ; i < vulnerabilitiesArray.length() ; i++ ) {
				contextFoD.setFoDCurrentVulnerability(vulnerabilitiesArray.optJSONObject(i));
				// We ignore the boolean result as we want to continue processing next vulnerabilities
				processor.process(Phase.PROCESS, context);
				contextFoD.setFoDCurrentVulnerability(null);
			}
			context.remove(KEY_START);
		}
		return processor.process(Phase.POST_PROCESS, context);
	}
	
	private URI processUri(Context context, String unprocessedUriString) {
		String uriString = SpringExpressionUtil.evaluateTemplateExpression(context, unprocessedUriString, String.class);
		try {
			return new URI(uriString);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Root URI "+uriString+" is not a valid URI");
		}
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(String rootExpression) {
		this.rootExpression = rootExpression;
	}

	public IProcessor getVulnerabilityProcessor() {
		return vulnerabilityProcessor;
	}

	public void setVulnerabilityProcessor(IProcessor vulnerabilityProcessor) {
		this.vulnerabilityProcessor = vulnerabilityProcessor;
	}
	
	
}
