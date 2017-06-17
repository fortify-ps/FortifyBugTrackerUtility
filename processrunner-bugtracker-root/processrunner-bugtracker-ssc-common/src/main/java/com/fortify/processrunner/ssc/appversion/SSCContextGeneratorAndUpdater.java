package com.fortify.processrunner.ssc.appversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.context.AbstractContextGeneratorAndUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.context.IContextSSCCommon;
import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONList;
import com.fortify.util.json.JSONMap;

@Component
public final class SSCContextGeneratorAndUpdater extends AbstractContextGeneratorAndUpdater implements IContextPropertyDefinitionProvider {
	private static final Log LOG = LogFactory.getLog(SSCContextGeneratorAndUpdater.class);
	private List<ISSCApplicationVersionFilter> filters;
	private List<ISSCApplicationVersionFilterFactory> filterFactories;
	private List<ISSCApplicationVersionContextUpdater> updaters;
	private List<ISSCApplicationVersionContextUpdaterFactory> updaterFactories;

	public SSCContextGeneratorAndUpdater() {
		setContextPropertyName(IContextSSCCommon.PRP_SSC_APPLICATION_VERSION_ID);
		setUseForDefaultValueGeneration(true);
	}
	
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		// Since we generate the SSCApplicationVersionId property either automatically or from
		// application versions specified through the SSCApplicationVersions property, we remove
		// the option for setting a single application version.
		contextPropertyDefinitions.remove(IContextSSCCommon.PRP_SSC_APPLICATION_VERSION_ID);
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextSSCCommon.PRP_SSC_APPLICATION_VERSIONS, "SSC application version names (<project>:<version>) or id's, separated by comma's", false));
	}
	
	public Map<Object, Context> getDefaultValuesWithMappedContextProperties(Context context) {
		IContextSSCCommon ctx = context.as(IContextSSCCommon.class);
		String appVersions = ctx.getSSCApplicationVersions();
		if ( appVersions != null ) {
			return getDefaultValuesWithMappedContextPropertiesForAppVersions(context, appVersions.split(","));
		} else {
			return getDefaultValuesWithMappedContextPropertiesFromFilters(context, getFiltersForContext(context));
		}
	}
	
	private Map<Object, Context> getDefaultValuesWithMappedContextPropertiesForAppVersions(Context context, String[] appVersions) {
		Map<Object, Context> result = new HashMap<Object, Context>();
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		for ( String appVersion : appVersions ) {
			JSONMap applicationVersion;
			String[] appVersionElements = appVersion.split(":");
			if ( appVersionElements.length == 1 ) {
				applicationVersion = conn.getApplicationVersion(appVersionElements[0]);
			} else if ( appVersionElements.length == 2 ) {
				applicationVersion = conn.getApplicationVersion(appVersionElements[0], appVersionElements[1]);
			} else {
				throw new IllegalArgumentException("Applications or versions containing a ':' can only be specified by id");
			}
			if ( applicationVersion == null ) {
				LOG.warn("[SSC] Application version "+appVersion+" not found");
			} else {
				putDefaultValuesWithMappedContextProperties(result, context, applicationVersion);
			}
		}
		return result;
	}

	private Map<Object, Context> getDefaultValuesWithMappedContextPropertiesFromFilters(Context context, List<ISSCApplicationVersionFilter> filtersForContext) {
		Map<Object, Context> result = new HashMap<Object, Context>();
		SSCAuthenticatingRestConnection conn = SSCConnectionFactory.getConnection(context);
		int start=0;
		int count=50;
		while ( start < count ) {
			LOG.info("[SSC] Loading next set of application versions");
			WebTarget resource = conn.getBaseResource().path("api/v1/projectVersions")
					.queryParam("start", ""+start).queryParam("limit", "");
			LOG.debug("[SSC] Retrieving application versions from "+resource);
			JSONMap data = conn.executeRequest(HttpMethod.GET, resource, JSONMap.class);
			count = data.get("count", Integer.class);
			JSONList pvArray = data.get("data", JSONList.class);
			start += pvArray.size();
			for ( JSONMap pv : pvArray.asValueType(JSONMap.class) ) {
				if ( isApplicationVersionIncluded(context, filtersForContext, pv) ) {
					putDefaultValuesWithMappedContextProperties(result, context, pv);
				}
			}
		}
		return result;
	}
	
	private void putDefaultValuesWithMappedContextProperties(Map<Object, Context> defaultValuesWithMappedContextProperties, Context initialContext,	JSONMap applicationVersion) {
		Context extraContextProperties = new Context();
		addMappedContextProperties(initialContext, applicationVersion);
		defaultValuesWithMappedContextProperties.put(applicationVersion.get("id", String.class), extraContextProperties);
	}
	
	@Override
	protected void addMappedContextProperties(Context context, Object contextPropertyValue) {
		JSONMap applicationVersion = SSCConnectionFactory.getConnection(context).getApplicationVersion((String)contextPropertyValue);
		addMappedContextProperties(context, applicationVersion);
		
	}

	private void addMappedContextProperties(Context context, JSONMap applicationVersion) {
		for ( ISSCApplicationVersionContextUpdater updater : getContextUpdatersForContext(context) ) {
			updater.updateContext(context, applicationVersion);
		}
		context.as(IContextSSCCommon.class).setAppVersion(applicationVersion);
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
		filtersForContext.sort(new OrderComparator());
		return filtersForContext;
	}
	
	private List<ISSCApplicationVersionContextUpdater> getContextUpdatersForContext(Context context) {
		List<ISSCApplicationVersionContextUpdater> updatersForContext = getUpdaters();
		List<ISSCApplicationVersionContextUpdaterFactory> updatersForContextFactories = getUpdaterFactories();
		if ( updatersForContext == null ) {
			updatersForContext = new ArrayList<ISSCApplicationVersionContextUpdater>();
		}
		if ( updatersForContextFactories != null ) {
			for ( ISSCApplicationVersionContextUpdaterFactory factory : updatersForContextFactories ) {
				Collection<ISSCApplicationVersionContextUpdater> updatersFromFactory = factory.getSSCApplicationVersionContextUpdaters(context);
				if ( updatersFromFactory!= null ) {
					updatersForContext.addAll(updatersFromFactory);
				}
			}
		}
		return updatersForContext;
	}
	
	private final boolean isApplicationVersionIncluded(Context context, List<ISSCApplicationVersionFilter> filtersForContext, JSONMap pv) {
		if ( pv == null ) { return false; }
		for ( ISSCApplicationVersionFilter filter : filtersForContext ) {
			if ( !filter.isApplicationVersionIncluded(context, pv) ) {
				return false;
			}
		}
		return true;
	}


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

	public List<ISSCApplicationVersionContextUpdater> getUpdaters() {
		return updaters;
	}

	@Autowired(required=false)
	public void setUpdaters(List<ISSCApplicationVersionContextUpdater> updaters) {
		this.updaters = updaters;
	}

	public List<ISSCApplicationVersionContextUpdaterFactory> getUpdaterFactories() {
		return updaterFactories;
	}

	@Autowired(required=false)
	public void setUpdaterFactories(List<ISSCApplicationVersionContextUpdaterFactory> updaterFactories) {
		this.updaterFactories = updaterFactories;
	}
}
