package com.fortify.processrunner.archer.connection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.util.json.JSONObjectBuilder;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.rest.IRestConnection;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This class provides a token-authenticated REST connection
 * for Archer.
 */
public class ArcherAuthenticatingRestConnection extends ArcherBasicRestConnection {
	private final ArcherTokenFactory tokenProvider;
	private final String applicationName;
	private long applicationId;
	private long levelId;
	private final Map<String,FieldContentAdder> fieldNamesToFieldContentAdderMap = new HashMap<String,FieldContentAdder>();
	
	public ArcherAuthenticatingRestConnection(String baseUrl, ArcherAuthData authData, String applicationName, ProxyConfiguration proxyConfig) {
		super(baseUrl, proxyConfig);
		this.tokenProvider = new ArcherTokenFactory(baseUrl, authData, proxyConfig);
		this.applicationName = applicationName;
		cacheFieldData();
	}
	
	private final void cacheFieldData() {
		JSONObject application = getApplication();
		this.applicationId = SpringExpressionUtil.evaluateExpression(application, "RequestedObject.Id", Long.class);
		System.err.println("Application: "+application);
		JSONObject level = getLevelForApplication(application);
		this.levelId = SpringExpressionUtil.evaluateExpression(level, "RequestedObject.Id", Long.class);
		System.err.println("Level: "+level);
		List<JSONObject> fields = getFieldsForLevel(level);
		
		System.err.println("Fields: "+fields);
		for ( JSONObject field : fields ) {
			FieldContentAdder adder = new FieldContentAdder(this, field);
			fieldNamesToFieldContentAdderMap.put(SpringExpressionUtil.evaluateExpression(field, "Name", String.class), adder);
			fieldNamesToFieldContentAdderMap.put(SpringExpressionUtil.evaluateExpression(field, "Alias", String.class), adder);
		}
	}

	protected List<JSONObject> getFieldsForLevel(JSONObject level) {
		String levelId = SpringExpressionUtil.evaluateExpression(level, "RequestedObject.Id", String.class);
		JSONArray fields = executeRequest(HttpMethod.GET, getBaseResource().path("api/core/system/fielddefinition/level").path(levelId), JSONArray.class);
		List<JSONObject> fieldObjs = JSONUtil.jsonObjectArrayToList(fields, "RequestedObject", JSONObject.class);
		return fieldObjs;
	}

	protected JSONObject getLevelForApplication(JSONObject application) {
		String applicationId = SpringExpressionUtil.evaluateExpression(application, "RequestedObject.Id", String.class);
		JSONArray levels = executeRequest(HttpMethod.GET, getBaseResource().path("api/core/system/level/module").path(applicationId), JSONArray.class);
		// TODO Can an application have more than one level? If so, which one should we return?
		return JSONUtil.findJSONObject(levels, "RequestedObject.ModuleId", applicationId);
	}

	protected JSONObject getApplication() {
		JSONArray apps = executeRequest(HttpMethod.GET, getBaseResource().path("api/core/system/application").queryParam("$filter", "Name eq '"+applicationName+"'"), JSONArray.class);
		return JSONUtil.findJSONObject(apps, "RequestedObject.Name", applicationName);
	}

	/**
	 * Update the {@link Builder} to add the Authorization header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.header("Authorization", "Archer session-id=\""+tokenProvider.getToken()+"\"");
	}

	public SubmittedIssue submitIssue(LinkedHashMap<String, Object> issueData) {
		JSONObject data = new JSONObject();
		JSONObject fieldContents = new JSONObject();
		final JSONObjectBuilder builder = new JSONObjectBuilder();
		builder.updateJSONObjectWithPropertyPath(data, "Content.LevelId", this.levelId);
		builder.updateJSONObjectWithPropertyPath(data, "Content.FieldContents", fieldContents);
		for (Map.Entry<String, Object> entry : issueData.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			FieldContentAdder adder = fieldNamesToFieldContentAdderMap.get(key);
			adder.addFieldContent(fieldContents, value);
		}
		try {
			System.out.println(data.toString(2));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject result = executeRequest(HttpMethod.POST, getBaseResource().path("api/core/content").entity(data, "application/json"), JSONObject.class);
		String id = SpringExpressionUtil.evaluateExpression(result, "RequestedObject.Id", String.class);
		return new SubmittedIssue(id, getDeepLink(id));
	}
	
	private String getDeepLink(String id) {
		return getBaseUrl()+"/apps/ArcherApp/Home.aspx#record/"+applicationId+"/"+levelId+"/"+id;
	}

	public static interface IFieldContentValueRetriever {
		public Object getFieldContentValue(IRestConnection conn, JSONObject field, Object value);
	}
	
	private static final class FieldContentAdder {
		private final IRestConnection conn;
		private final JSONObject field;
		private final long fieldId;
		private final int fieldType;
		private final IFieldContentValueRetriever fieldContentValueRetriever; 
		public FieldContentAdder(IRestConnection conn, JSONObject field) {
			this.conn = conn;
			this.field = field;
			this.fieldId = SpringExpressionUtil.evaluateExpression(field, "Id", Long.class);
			this.fieldType = SpringExpressionUtil.evaluateExpression(field, "Type", Integer.class);
			this.fieldContentValueRetriever = FieldContentValueRetrieverFactory.getFieldContentValueRetriever(field);
		}
		
		public void addFieldContent(JSONObject fieldContents, Object value) {
			try {
				fieldContents.put(fieldId+"", getFieldContent(value));
			} catch (JSONException e) {
				throw new RuntimeException("Error creating FieldContents object", e);
			}
		}

		private JSONObject getFieldContent(Object value) throws JSONException {
			JSONObject result = new JSONObject();
			result.put("Type", fieldType);
			result.put("Value", fieldContentValueRetriever.getFieldContentValue(conn, field, value));
			result.put("FieldId", fieldId);
			return result;
		}
		
		
	}
	
	private static final class FieldContentValueRetrieverFactory {
		private static final Map<Integer, IFieldContentValueRetriever> fieldTypeToContentValueRetrieverMap = getFieldTypeToContentValueRetrieverMap();
		
		public static final IFieldContentValueRetriever getFieldContentValueRetriever(JSONObject field) {
			int fieldType = SpringExpressionUtil.evaluateExpression(field, "Type", Integer.class);
			return fieldTypeToContentValueRetrieverMap.get(fieldType);
		}
		
		private static final Map<Integer, IFieldContentValueRetriever> getFieldTypeToContentValueRetrieverMap() {
			Map<Integer, IFieldContentValueRetriever> result = new HashMap<Integer, IFieldContentValueRetriever>();
			result.put(1, new FieldContentValueRetrieverText());
			result.put(4, new FieldContentValueRetrieverValuesList());
			return result;
		}
		
		public static final class FieldContentValueRetrieverText implements IFieldContentValueRetriever {
			public Object getFieldContentValue(IRestConnection conn, JSONObject field, Object value) {
				return value==null?null:value.toString();
			}
		}
		
		public static final class FieldContentValueRetrieverValuesList implements IFieldContentValueRetriever {
			private static final Map<String, JSONArray> valueListsByIdMap = new HashMap<String, JSONArray>();
			public Object getFieldContentValue(IRestConnection conn, JSONObject field, Object value) {
				JSONObject result = new JSONObject();
				String relatedValuesListId = SpringExpressionUtil.evaluateExpression(field, "RelatedValuesListId", String.class);
				JSONArray valuesListArray = getValueListArray(conn, relatedValuesListId);
				// TODO Map multiple values if value is array/list type
				// TODO Is it safe to use case-insensitive lookup?
				Long valueId = JSONUtil.mapValue(valuesListArray, "RequestedObject.Name.toLowerCase()", value.toString().toLowerCase(), "RequestedObject.Id", Long.class);
				if ( valueId == null ) {
					new JSONObjectBuilder().updateJSONObjectWithPropertyPath(result, "OtherText", value);
				} else {
					new JSONObjectBuilder().updateJSONObjectWithPropertyPath(result, "ValuesListIds", new Long[]{valueId});
				}
				return result;
			}
			
			private JSONArray getValueListArray(IRestConnection conn, String valueListId) {
				JSONArray result = valueListsByIdMap.get(valueListId);
				if ( result == null && valueListId != null ) {
					result = conn.executeRequest(HttpMethod.GET, conn.getBaseResource().path("api/core/system/valueslistvalue/flat/valueslist/").path(valueListId), JSONArray.class);
					valueListsByIdMap.put(valueListId, result);
					System.err.println("Values ["+valueListId+"]: "+result);
				}
				return result;
			}
		}
	}
}