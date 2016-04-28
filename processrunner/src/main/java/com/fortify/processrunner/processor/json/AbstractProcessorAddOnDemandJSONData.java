package com.fortify.processrunner.processor.json;

import java.beans.PropertyEditorSupport;
import java.net.URI;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.expression.EvaluationContext;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.json.ondemand.IOnDemandJSONData;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.sun.jersey.api.client.WebResource;

/**
 * TODO JavaDoc
 */
// TODO Add support for loading on-demand objects immediately (for testing or early failure detection)?
// TODO Review this code for simplifications?
public abstract class AbstractProcessorAddOnDemandJSONData extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorAddOnDemandJSONData.class);
	private Map<String,RootExpressionAndUri> nameToRootExpressionAndUriMap;
	private Map<String,Map<String,SimpleExpression>> uriToNamesAndRootExpressionMap;
	
	@Override
	protected boolean process(Context context) {
		try {
			for ( Map.Entry<String, RootExpressionAndUri> entry : getNameToRootExpressionAndUriMap().entrySet() ) {
				IRestConnection conn = getRestconnection(context);
				RootExpressionAndUri rootExpressionAndUri = entry.getValue();
				
				WebResource resource = conn.getBaseResource().uri(
					SpringExpressionUtil.evaluateTemplateExpression(context, rootExpressionAndUri.uri, URI.class));
				OnDemandJSONObject od = new OnDemandJSONObject(conn, resource, getNameToRootExpressionMap(rootExpressionAndUri.uri));
				
				String name = entry.getKey();
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Adding on-demand JSON data for "+name+": "+od);
				}
				getRootObject(context).put(name, od);
			}
			return true;
		} catch (JSONException e) {
			throw new RuntimeException("Error adding on-demand JSON objects to root object");
		}
	}
	
	protected abstract JSONObject getRootObject(Context context);
	
	public Map<String, RootExpressionAndUri> getNameToRootExpressionAndUriMap() {
		if ( nameToRootExpressionAndUriMap == null ) { throw new NullPointerException("Property nameToRootExpressionAndUriMap not configured"); }
		return nameToRootExpressionAndUriMap;
	}

	public void setNameToRootExpressionAndUriMap(Map<String, RootExpressionAndUri> nameToRootExpressionAndUriMap) {
		this.nameToRootExpressionAndUriMap = nameToRootExpressionAndUriMap;
		this.uriToNamesAndRootExpressionMap = new HashMap<String,Map<String,SimpleExpression>>();
		for ( Map.Entry<String,RootExpressionAndUri> entry : nameToRootExpressionAndUriMap.entrySet() ) {
			getNameToRootExpressionMap(entry.getValue().uri)
				.put(entry.getKey(), entry.getValue().rootExpression);
		}
	}
	
	private Map<String,SimpleExpression> getNameToRootExpressionMap(String uri) {
		Map<String, SimpleExpression> nameToRootExpressionMap = uriToNamesAndRootExpressionMap.get(uri);
		if ( nameToRootExpressionMap == null ) {
			nameToRootExpressionMap = new HashMap<String,SimpleExpression>();
			uriToNamesAndRootExpressionMap.put(uri, nameToRootExpressionMap);
		}
		return nameToRootExpressionMap;
	}

	protected abstract IRestConnection getRestconnection(Context context);
	
	private static final class OnDemandJSONObject implements IOnDemandJSONData {
		private static final Log LOG = LogFactory.getLog(OnDemandJSONObject.class);
		private final IRestConnection connection;
		private final WebResource resource;
		private final Map<String,SimpleExpression> fieldToExpressionMap;
		
		public OnDemandJSONObject(IRestConnection connection, WebResource resource, Map<String,SimpleExpression> fieldToExpressionMap) {
			this.connection = connection;
			this.resource = resource;
			this.fieldToExpressionMap = fieldToExpressionMap;
		}
		
		// TODO Review/fix this code for cases where no URI or root expression have been
		//      configured for a given name, or if the REST response does not contain
		//      the configured root expression
		public Object replaceOnDemandJSONData(EvaluationContext evaluationContext, JSONObject target, String name) {
			try {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Getting on-demand JSON data from "+resource);
				}
				JSONObject json = connection.executeRequest(HttpMethod.GET, resource, JSONObject.class);
				for ( Map.Entry<String, SimpleExpression> entry : fieldToExpressionMap.entrySet() ) {
					String key = entry.getKey();
					Object value = SpringExpressionUtil.evaluateExpression(json, entry.getValue(), Object.class);
					target.put(key, value);
					if ( LOG.isTraceEnabled() ) {
						LOG.trace("Added on-demand JSON data for "+key+": "+value);
					}
				}
				return target.get(name);
			} catch ( JSONException e ) {
				throw new RuntimeException("Error replacing on-demand JSON objects with actual objects", e);
			}
		}
		
		@Override
		public String toString() {
			return new ReflectionToStringBuilder(this).toString();
		}
	}
	
	public static final class RootExpressionAndUri {
		public final SimpleExpression rootExpression;
		public final String uri;
		public RootExpressionAndUri(SimpleExpression rootExpression, String uri) {
			this.rootExpression = rootExpression;
			this.uri = uri;
		}
	}
	
	public static final class RootExpressionAndUriPropertyEditor extends PropertyEditorSupport {
		private static final MessageFormat FMT_ROOT_EXPRESSION_AND_URI = new MessageFormat("{0}|{1}");
		
		@Override
		public void setAsText(String paramString) throws IllegalArgumentException {
			setValue(parse(paramString));
		}
		
		public static final RootExpressionAndUri parse(String rootExpressionAndUriString) {
			try {
				Object[] array = FMT_ROOT_EXPRESSION_AND_URI.parse(rootExpressionAndUriString);
				return new RootExpressionAndUri(SpringExpressionUtil.parseSimpleExpression((String)array[0]), (String)array[1]);
			} catch (ParseException e) {
				throw new RuntimeException("Error parsing root expression and URI. Expected: <rootExpression>|<uri>, actual: "+rootExpressionAndUriString);
			}
		}
	}
}
