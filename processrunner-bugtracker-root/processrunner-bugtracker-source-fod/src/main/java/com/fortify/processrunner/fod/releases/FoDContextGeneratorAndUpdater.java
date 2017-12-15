/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
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
package com.fortify.processrunner.fod.releases;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import com.fortify.api.fod.connection.FoDAuthenticatingRestConnection;
import com.fortify.api.util.rest.json.JSONMap;
import com.fortify.api.util.rest.json.processor.AbstractJSONMapProcessor;
import com.fortify.processrunner.context.AbstractContextGeneratorAndUpdater;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.processrunner.fod.connection.FoDConnectionFactory;
import com.fortify.processrunner.fod.context.IContextFoD;

@Component
public final class FoDContextGeneratorAndUpdater extends AbstractContextGeneratorAndUpdater implements IContextPropertyDefinitionProvider {
	private static final Log LOG = LogFactory.getLog(FoDContextGeneratorAndUpdater.class);
	private List<IFoDReleaseFilter> filters;
	private List<IFoDReleaseFilterFactory> filterFactories;
	private List<IFoDReleaseContextUpdater> updaters;
	private List<IFoDReleaseContextUpdaterFactory> updaterFactories;

	public FoDContextGeneratorAndUpdater() {
		setContextPropertyName(IContextFoD.PRP_RELEASE_ID);
		setUseForDefaultValueGeneration(true);
	}
	
	public void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		// Since we generate the FoDReleaseId property either automatically or from
		// releases specified through the FoDReleases property, we remove
		// the option for setting a single release id.
		contextPropertyDefinitions.remove(IContextFoD.PRP_RELEASE_ID);
		contextPropertyDefinitions.add(new ContextPropertyDefinition(IContextFoD.PRP_RELEASES, "FoD release names (<application>:<release>) or id's, separated by comma's", false));
	}
	
	public Map<Object, Context> getDefaultValuesWithMappedContextProperties(Context context) {
		IContextFoD ctx = context.as(IContextFoD.class);
		String releases = ctx.getFoDReleases();
		if ( releases != null ) {
			return getDefaultValuesWithMappedContextPropertiesForReleases(context, releases.split(","));
		} else {
			return getDefaultValuesWithMappedContextPropertiesFromFilters(context, getFiltersForContext(context));
		}
	}
	
	private Map<Object, Context> getDefaultValuesWithMappedContextPropertiesForReleases(Context context, String[] releaseNamesOrIds) {
		Map<Object, Context> result = new HashMap<Object, Context>();
		FoDAuthenticatingRestConnection conn = FoDConnectionFactory.getConnection(context);
		for ( String releaseNameOrId : releaseNamesOrIds ) {
			JSONMap release;
			String[] releaseElements = releaseNameOrId.split(":");
			if ( releaseElements.length == 1 ) {
				release = conn.getRelease(releaseElements[0]);
			} else if ( releaseElements.length == 2 ) {
				release = conn.getRelease(releaseElements[0], releaseElements[1]);
			} else {
				throw new IllegalArgumentException("Applications or releases containing a ':' can only be specified by id");
			}
			if ( release == null ) {
				LOG.warn("[FoD] Release "+releaseNameOrId+" not found");
			} else {
				putDefaultValuesWithMappedContextProperties(result, context, release);
			}
		}
		return result;
	}

	private Map<Object, Context> getDefaultValuesWithMappedContextPropertiesFromFilters(final Context context, final List<IFoDReleaseFilter> filtersForContext) {
		final Map<Object, Context> result = new HashMap<Object, Context>();
		LOG.info("[FoD] Loading releases");
		FoDConnectionFactory.getConnection(context).processReleases(new AbstractJSONMapProcessor() {	
			public void process(JSONMap applicationVersion) {
				if ( isReleaseIncluded(context, filtersForContext, applicationVersion) ) {
					putDefaultValuesWithMappedContextProperties(result, context, applicationVersion);
				}
			}
		});
		return result;
	}
	
	private void putDefaultValuesWithMappedContextProperties(Map<Object, Context> defaultValuesWithMappedContextProperties, Context initialContext,	JSONMap release) {
		Context extraContextProperties = new Context(initialContext);
		addMappedContextProperties(extraContextProperties, release);
		defaultValuesWithMappedContextProperties.put(release.get("releaseId", String.class), extraContextProperties);
	}
	
	@Override
	protected void addMappedContextProperties(Context context, Object contextPropertyValue) {
		JSONMap release = FoDConnectionFactory.getConnection(context).getRelease((String)contextPropertyValue);
		addMappedContextProperties(context, release);
		
	}

	private void addMappedContextProperties(Context context, JSONMap release) {
		for ( IFoDReleaseContextUpdater updater : getContextUpdatersForContext(context) ) {
			updater.updateContext(context, release);
		}
		context.as(IContextFoD.class).setRelease(release);
	}

	private List<IFoDReleaseFilter> getFiltersForContext(Context context) {
		List<IFoDReleaseFilter> filtersForContext = getFilters();
		List<IFoDReleaseFilterFactory> filterFactories = getFilterFactories();
		if ( filtersForContext == null ) {
			filtersForContext = new ArrayList<IFoDReleaseFilter>();
		}
		if ( filterFactories != null ) {
			for ( IFoDReleaseFilterFactory filterFactory : filterFactories ) {
				Collection<IFoDReleaseFilter> filtersFromFactory = filterFactory.getFoDReleaseFilters(context);
				if ( filtersFromFactory!= null ) {
					filtersForContext.addAll(filtersFromFactory);
				}
			}
		}
		filtersForContext.sort(new OrderComparator());
		return filtersForContext;
	}
	
	private List<IFoDReleaseContextUpdater> getContextUpdatersForContext(Context context) {
		List<IFoDReleaseContextUpdater> updatersForContext = getUpdaters();
		List<IFoDReleaseContextUpdaterFactory> updatersForContextFactories = getUpdaterFactories();
		if ( updatersForContext == null ) {
			updatersForContext = new ArrayList<IFoDReleaseContextUpdater>();
		}
		if ( updatersForContextFactories != null ) {
			for ( IFoDReleaseContextUpdaterFactory factory : updatersForContextFactories ) {
				Collection<IFoDReleaseContextUpdater> updatersFromFactory = factory.getFoDReleaseContextUpdaters(context);
				if ( updatersFromFactory!= null ) {
					updatersForContext.addAll(updatersFromFactory);
				}
			}
		}
		return updatersForContext;
	}
	
	private final boolean isReleaseIncluded(Context context, List<IFoDReleaseFilter> filtersForContext, JSONMap pv) {
		if ( pv == null ) { return false; }
		for ( IFoDReleaseFilter filter : filtersForContext ) {
			if ( !filter.isReleaseIncluded(context, pv) ) {
				return false;
			}
		}
		return true;
	}


	public final List<IFoDReleaseFilter> getFilters() {
		return filters;
	}
	
	@Autowired(required=false)
	public final void setFilters(List<IFoDReleaseFilter> filters) {
		this.filters = filters;
	}

	public final List<IFoDReleaseFilterFactory> getFilterFactories() {
		return filterFactories;
	}

	@Autowired(required=false)
	public final void setFilterFactories(List<IFoDReleaseFilterFactory> filterFactories) {
		this.filterFactories = filterFactories;
	}

	public List<IFoDReleaseContextUpdater> getUpdaters() {
		return updaters;
	}

	@Autowired(required=false)
	public void setUpdaters(List<IFoDReleaseContextUpdater> updaters) {
		this.updaters = updaters;
	}

	public List<IFoDReleaseContextUpdaterFactory> getUpdaterFactories() {
		return updaterFactories;
	}

	@Autowired(required=false)
	public void setUpdaterFactories(List<IFoDReleaseContextUpdaterFactory> updaterFactories) {
		this.updaterFactories = updaterFactories;
	}
}
