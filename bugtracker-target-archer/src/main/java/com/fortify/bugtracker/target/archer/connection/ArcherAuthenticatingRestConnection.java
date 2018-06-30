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
package com.fortify.bugtracker.target.archer.connection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.bugtracker.common.target.issue.SubmittedIssue;
import com.fortify.util.rest.connection.AbstractRestConnectionConfig;
import com.fortify.util.rest.connection.IRestConnectionBuilder;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This class provides a token-authenticated REST connection
 * for Archer.
 */
public class ArcherAuthenticatingRestConnection extends ArcherBasicRestConnection {
	private static final Log LOG = LogFactory.getLog(ArcherAuthenticatingRestConnection.class);
	private final ArcherTokenFactoryRest tokenProviderRest;
	private final String applicationName;
	private long applicationId;
	private long levelId;
	private final Map<String,FieldContentAdder> fieldNamesToFieldContentAdderMap = new HashMap<String,FieldContentAdder>();
	
	public ArcherAuthenticatingRestConnection(ArcherRestConnectionConfig<?> config) {
		super(config);
		this.tokenProviderRest = new ArcherTokenFactoryRest(config);
		this.applicationName = config.getApplicationName();
		cacheFieldData();
	}
	
	private final void cacheFieldData() {
		JSONMap application = getApplication();
		this.applicationId = SpringExpressionUtil.evaluateExpression(application, "RequestedObject.Id", Long.class);
		List<JSONMap> fields = getFieldsForApplication();
		// TODO What if application defines multiple levels?
		this.levelId = SpringExpressionUtil.evaluateExpression(fields, "#this[0].LevelId", Long.class);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("[Archer] Available fields: "+fields);
		}
		for ( JSONMap field : fields ) {
			FieldContentAdder adder = new FieldContentAdder(this, field);
			fieldNamesToFieldContentAdderMap.put(SpringExpressionUtil.evaluateExpression(field, "Name", String.class), adder);
			fieldNamesToFieldContentAdderMap.put(SpringExpressionUtil.evaluateExpression(field, "Alias", String.class), adder);
		}
	}

	protected List<JSONMap> getFieldsForApplication() {
		JSONList fields = executeRequest(HttpMethod.GET, getBaseResource().path("api/core/system/fielddefinition/application").path(applicationId+""), JSONList.class);
		return fields.getValues("RequestedObject", JSONMap.class);
	}

	protected JSONMap getApplication() {
		LOG.info("[Archer] Loading information from application "+applicationName);
		String filter = "Name eq '"+applicationName+"'";
		JSONList apps = executeRequest(HttpMethod.GET, getBaseResource().path("api/core/system/application").queryParam("$filter", filter), JSONList.class);
		LOG.debug("[Archer] List of applications matching filter "+filter+": "+apps);
		JSONMap result = apps.find("RequestedObject.Name", applicationName, JSONMap.class);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("[Archer] Application data: "+result);
		}
		return result;
	}

	/**
	 * Update the {@link Builder} to add the Authorization header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.header("Authorization", "Archer session-id=\""+tokenProviderRest.getToken()+"\"");
	}
	
	/* (non-Javadoc)
	 * @see com.fortify.bugtracker.target.archer.connection.IArcherRestConnection#addValueToValuesList(com.fortify.util.rest.IRestConnection, java.lang.Long, java.lang.String)
	 */
	public Long addValueToValuesList(Long valueListId, String value) {
		LOG.info("[Archer] Adding value '"+value+"' to value list id "+valueListId);
		// Adding items to value lists is not supported via REST API, so we need to revert to SOAP API
		// TODO Simplify this method?
		// TODO Make this method more fail-safe (like checking for the correct response element)?
		Long result = null;
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
	        SOAPMessage message = messageFactory.createMessage();
	        SOAPPart soapPart = message.getSOAPPart();
	        SOAPEnvelope envelope = soapPart.getEnvelope();
	        SOAPBody body = envelope.getBody();
	        SOAPElement bodyElement = body.addChildElement(envelope.createName("CreateValuesListValue", "", "http://archer-tech.com/webservices/"));
	        bodyElement.addChildElement("sessionToken").addTextNode(tokenProviderRest.getToken());
	        bodyElement.addChildElement("valuesListId").addTextNode(valueListId+"");
	        bodyElement.addChildElement("valuesListValueName").addTextNode(value);
	        message.saveChanges();
	 
	        SOAPMessage response = executeRequest(HttpMethod.POST, 
	        		getBaseResource().path("/ws/field.asmx")
	        		.request()
	        		.header("SOAPAction", "\"http://archer-tech.com/webservices/CreateValuesListValue\"")
	        		.accept("text/xml"),
	        		Entity.entity(message, "text/xml"), SOAPMessage.class);
	        @SuppressWarnings("unchecked")
			Iterator<Object> it = response.getSOAPBody().getChildElements();
	        while (it.hasNext()){
	        	Object o = it.next();
	        	if ( o instanceof SOAPElement ) {	
	        		result = new Long(((SOAPElement)o).getTextContent());
	        	}
	          }
	        System.out.println(response);
		} catch (SOAPException e) {
			throw new RuntimeException("Error executing SOAP request", e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.fortify.bugtracker.target.archer.connection.IArcherRestConnection#submitIssue(java.util.LinkedHashMap)
	 */
	public SubmittedIssue submitIssue(LinkedHashMap<String, Object> issueData) {
		JSONMap data = new JSONMap();
		JSONMap fieldContents = new JSONMap();
		data.putPath("Content.LevelId", this.levelId);
		data.putPath("Content.FieldContents", fieldContents);
		for (Map.Entry<String, Object> entry : issueData.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			FieldContentAdder adder = fieldNamesToFieldContentAdderMap.get(key);
			if ( adder == null ) {
				LOG.error("[Archer] Field "+key+" not found in application, skipping");
			} else {
				adder.addFieldContent(fieldContents, key, value);
			}
		}
		JSONMap result = executeRequest(HttpMethod.POST, getBaseResource().path("api/core/content"), Entity.entity(data, "application/json"), JSONMap.class);
		String id = SpringExpressionUtil.evaluateExpression(result, "RequestedObject.Id", String.class);
		return new SubmittedIssue(id, getDeepLink(id));
	}
	
	private String getDeepLink(String id) {
		return getBaseUrl()+"/apps/ArcherApp/Home.aspx#record/"+applicationId+"/"+levelId+"/"+id;
	}

	public static interface IFieldContentValueRetriever {
		public Object getFieldContentValue(ArcherAuthenticatingRestConnection conn, JSONMap field, Object value);
	}
	
	private static final class FieldContentAdder {
		private final ArcherAuthenticatingRestConnection conn;
		private final JSONMap field;
		private final long fieldId;
		private final int fieldType;
		private final IFieldContentValueRetriever fieldContentValueRetriever; 
		public FieldContentAdder(ArcherAuthenticatingRestConnection conn, JSONMap field) {
			this.conn = conn;
			this.field = field;
			this.fieldId = SpringExpressionUtil.evaluateExpression(field, "Id", Long.class);
			this.fieldType = SpringExpressionUtil.evaluateExpression(field, "Type", Integer.class);
			this.fieldContentValueRetriever = FieldContentValueRetrieverFactory.getFieldContentValueRetriever(field);
		}
		
		public void addFieldContent(JSONMap fieldContents, String key, Object value) {
			if ( fieldContentValueRetriever == null ) {
				LOG.error("[Archer] Field type "+fieldType+" not supported by FortifyBugTrackerUtility, skipping field "+key);
			} else {
				fieldContents.put(fieldId+"", getFieldContent(value));
			}
		}

		private JSONMap getFieldContent(Object value) {
			JSONMap result = new JSONMap();
			result.put("Type", fieldType);
			result.put("Value", fieldContentValueRetriever.getFieldContentValue(conn, field, value));
			result.put("FieldId", fieldId);
			return result;
		}
		
		
	}
	
	private static final class FieldContentValueRetrieverFactory {
		private static final Map<Integer, IFieldContentValueRetriever> fieldTypeToContentValueRetrieverMap = getFieldTypeToContentValueRetrieverMap();
		
		public static final IFieldContentValueRetriever getFieldContentValueRetriever(JSONMap field) {
			int fieldType = SpringExpressionUtil.evaluateExpression(field, "Type", Integer.class);
			return fieldTypeToContentValueRetrieverMap.get(fieldType);
		}
		
		private static final Map<Integer, IFieldContentValueRetriever> getFieldTypeToContentValueRetrieverMap() {
			Map<Integer, IFieldContentValueRetriever> result = new HashMap<Integer, IFieldContentValueRetriever>();
			result.put(1, new FieldContentValueRetrieverText());
			result.put(4, new FieldContentValueRetrieverValuesList());
			
			// TODO See https://community.rsa.com/docs/DOC-65292 for all valid field types
			return result;
		}
		
		public static final class FieldContentValueRetrieverText implements IFieldContentValueRetriever {
			public Object getFieldContentValue(ArcherAuthenticatingRestConnection conn, JSONMap field, Object value) {
				return value==null?null:value.toString();
			}
		}
		
		public static final class FieldContentValueRetrieverValuesList implements IFieldContentValueRetriever {
			private static final Map<Long, JSONList> valueListsByIdMap = new HashMap<Long, JSONList>();
			public Object getFieldContentValue(ArcherAuthenticatingRestConnection conn, JSONMap field, Object value) {
				JSONMap result = new JSONMap();
				Long relatedValuesListId = SpringExpressionUtil.evaluateExpression(field, "RelatedValuesListId", Long.class);
				JSONList valuesListArray = getValueListArray(conn, relatedValuesListId);
				// TODO Map multiple values if value is array/list type
				// TODO Is it safe to use case-insensitive lookup?
				Long valueId = valuesListArray.mapValue("RequestedObject.Name.toLowerCase()", value.toString().toLowerCase(), "RequestedObject.Id", Long.class);
				if ( valueId == null ) {
					valueId = addValuesListValue(conn, relatedValuesListId, value.toString());
				} else {
					result.put("ValuesListIds", new Long[]{valueId});
				}
				return result;
			}
			
			

			private Long addValuesListValue(ArcherAuthenticatingRestConnection conn, Long valueListId, String value) {
				Long result = conn.addValueToValuesList(valueListId, value);
				// Remove cached entry such that on next request, the list with the newly added value is reloaded 
				valueListsByIdMap.remove(valueListId);
				return result;
			}



			private JSONList getValueListArray(ArcherAuthenticatingRestConnection conn, Long valueListId) {
				JSONList result = valueListsByIdMap.get(valueListId);
				if ( result == null && valueListId != null ) {
					result = conn.executeRequest(HttpMethod.GET, conn.getBaseResource().path("api/core/system/valueslistvalue/flat/valueslist/").path(valueListId+""), JSONList.class);
					valueListsByIdMap.put(valueListId, result);
				}
				return result;
			}
		}
	}
	
	/**
	 * This method returns an {@link ArcherRestConnectionBuilder} instance
	 * that allows for building {@link ArcherAuthenticatingRestConnection} instances.
	 * @return
	 */
	public static final ArcherRestConnectionBuilder builder() {
		return new ArcherRestConnectionBuilder();
	}
	
	/**
	 * This class provides a builder pattern for configuring an {@link ArcherAuthenticatingRestConnection} instance.
	 * It re-uses builder functionality from {@link AbstractRestConnectionConfig}, and adds a
	 * {@link #build()} method to build an {@link ArcherAuthenticatingRestConnection} instance.
	 * 
	 * @author Ruud Senden
	 */
	public static final class ArcherRestConnectionBuilder extends ArcherRestConnectionConfig<ArcherRestConnectionBuilder> implements IRestConnectionBuilder<ArcherAuthenticatingRestConnection> {
		@Override
		public ArcherAuthenticatingRestConnection build() {
			return new ArcherAuthenticatingRestConnection(this);
		}
	}
}
