package com.fortify.processrunner.ssc.appversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.mapper.AbstractContextPropertyMapper;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.sun.jersey.api.client.WebResource;

/**
 * This class generates default values for the {@link IContextSSCCommon#PRP_SSC_APPLICATION_VERSION_ID}
 * context property by loading all SSC application versions and filtering them through any
 * configured {@link ISSCApplicationVersionFilter} instances. {@link ISSCApplicationVersionFilter} and
 * {@link ISSCApplicationVersionFilterFactory} will be automatically wired by Spring. 
 * 
 * @author Ruud Senden
 *
 */
public class SSCApplicationVersionIdGenerator extends AbstractContextPropertyMapper {
	private static final Log LOG = LogFactory.getLog(SSCApplicationVersionIdGenerator.class);
	private List<ISSCApplicationVersionFilter> filters;
	private List<ISSCApplicationVersionFilterFactory> filterFactories;

	public SSCApplicationVersionIdGenerator() {
		setContextPropertyName(IContextSSCCommon.PRP_SSC_APPLICATION_VERSION_ID);
	}
	
	public Map<Object, Context> getDefaultValuesWithExtraContextProperties(Context context) {
		Map<Object, Context> result = new HashMap<Object, Context>();
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		List<ISSCApplicationVersionFilter> filtersForContext = getFiltersForContext(context);
		int start=0;
		int count=50;
		while ( start < count ) {
			LOG.info("[SSC] Loading next set of application versions");
			WebResource resource = conn.getBaseResource().path("api/v1/projectVersions")
					.queryParam("start", ""+start).queryParam("limit", "");
			LOG.debug("[SSC] Retrieving application versions from "+resource);
			JSONObject data = conn.executeRequest(HttpMethod.GET, resource, JSONObject.class);
			count = data.optInt("count");
			JSONArray pvArray = data.optJSONArray("data");
			start += pvArray.length();
			for ( int i = 0 ; i < pvArray.length() ; i++ ) {
				JSONObject pv = pvArray.optJSONObject(i);
				String pvId = pv.optString("id");
				if ( isApplicationVersionIncluded(context, filtersForContext, pv) ) {
					Context extraContextProperties = new Context();
					addMappedContextProperties(context, pvId);
					result.put(pvId, extraContextProperties);
				}
			}
		}
		return result;
	}

	private List<ISSCApplicationVersionFilter> getFiltersForContext(Context context) {
		List<ISSCApplicationVersionFilter> filtersForContext = getFilters();
		List<ISSCApplicationVersionFilterFactory> filterFactories = getFilterFactories();
		if ( filtersForContext == null ) {
			filtersForContext = new ArrayList<ISSCApplicationVersionFilter>();
		}
		if ( filterFactories != null ) {
			for ( ISSCApplicationVersionFilterFactory filterFactory : filterFactories ) {
				Collection<ISSCApplicationVersionFilter> filtersFromFactory = filterFactory.getSSCApplicationVersionFilters(context);
				if ( filtersFromFactory!= null ) {
					filtersForContext.addAll(filtersFromFactory);
				}
			}
		}
		addExtraFilters(context, filtersForContext);
		filtersForContext.sort(new OrderComparator());
		return filtersForContext;
	}
	
	protected void addExtraFilters(Context context, List<ISSCApplicationVersionFilter> filters) {}
	
	private final boolean isApplicationVersionIncluded(Context context, List<ISSCApplicationVersionFilter> filtersForContext, JSONObject pv) {
		if ( pv == null ) { return false; }
		for ( ISSCApplicationVersionFilter filter : filtersForContext ) {
			if ( !filter.isApplicationVersionIncluded(context, pv) ) {
				return false;
			}
		}
		return true;
	}

	public void addMappedContextProperties(Context context, Object contextPropertyValue) {
		addMappedContextProperties(context, (String)contextPropertyValue);
	}
	
	protected void addMappedContextProperties(Context context, String pvId) {}

	public final List<ISSCApplicationVersionFilter> getFilters() {
		return filters;
	}
	
	@Autowired(required=false)
	public final void setFilters(List<ISSCApplicationVersionFilter> filters) {
		this.filters = filters;
	}

	public final List<ISSCApplicationVersionFilterFactory> getFilterFactories() {
		return filterFactories;
	}

	@Autowired(required=false)
	public final void setFilterFactories(List<ISSCApplicationVersionFilterFactory> filterFactories) {
		this.filterFactories = filterFactories;
	}
}
