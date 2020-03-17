/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.processrunner.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.fortify.processrunner.AbstractProcessRunner;
import com.fortify.processrunner.RunProcessRunnerFromSpringConfig;
import com.fortify.processrunner.context.IContextGenerator;

/**
 * <p>This class holds all configured {@link CLIOptionDefinition} instances. {@link CLIOptionDefinition}
 * instances are indexed by name, group and optionally source. Name and group indexing speaks for itself,
 * as name and group are {@link CLIOptionDefinition} properties. Source-based indexing allows for advanced
 * updates to {@link CLIOptionDefinition} instances based on the components that provided each 
 * {@link CLIOptionDefinition} instance. For example, this is used by 
 * {@link RunProcessRunnerFromSpringConfig#addCLIOptionDefinitions(CLIOptionDefinitions)} to invoke
 * {@link IContextGenerator#updateProcessRunnerCLIOptionDefinitions(Collection)} on {@link CLIOptionDefinition}
 * instances that are solely provided by {@link AbstractProcessRunner} instances, excluding {@link CLIOptionDefinition}
 * instances that were provided by the {@link IContextGenerator} instance. Other examples include
 * adding extra information about which actions various options are related to, as can be seen in
 * AbstractBugTrackerProcessRunner.</p>
 * 
 * <p>Note that any {@link CLIOptionDefinition} instances being added to this {@link CLIOptionDefinitions}
 * instances are being deep-copied before being stored; as such any updates to {@link CLIOptionDefinition} 
 * instances contained in this {@link CLIOptionDefinitions} instance will not affect the original
 * {@link CLIOptionDefinition} instance.</p>
 * 
 * @author Ruud Senden
 *
 */
public class CLIOptionDefinitions {
	private static final Collection<CLIOptionDefinition> EMPTY_COLLECTION = Collections.emptyList();
	private LinkedHashMap<String, CLIOptionDefinition> optionsByName = new LinkedHashMap<>();
	private LinkedHashMap<String, Collection<CLIOptionDefinition>> optionsByGroup = new LinkedHashMap<>();
	private LinkedHashMap<String, Collection<CLIOptionDefinition>> optionsBySource = new LinkedHashMap<>();
	
	/**
	 * Default constructor
	 */
	public CLIOptionDefinitions() {}
	
	/**
	 * Constructor for adding one or more {@link CLIOptionDefinitions}
	 * to the current {@link CLIOptionDefinitions}.
	 * @param defs
	 */
	public CLIOptionDefinitions(CLIOptionDefinitions... defs) {
		for ( CLIOptionDefinitions def : defs ) {
			addAll(def);
		}
	}

	/**
	 * Add the given {@link CLIOptionDefinition} instances to the optionsByName and optionsByGroup maps,
	 * if not already added before.
	 * @param cliOptionDefinitions
	 * @return
	 */
	public CLIOptionDefinitions add(CLIOptionDefinition... cliOptionDefinitions) {
		return add(null, cliOptionDefinitions);
	}
	
	/**
	 * Add the given {@link CLIOptionDefinition} instances to the optionsByName, optionsByGroup and optionsBySources 
	 * (if source is provided) maps. Existing instances in optionsByName and optionsByGroup maps will not be replaced.
	 * If a given {@link CLIOptionDefinition} does not yet exist, a deep copy will be made before adding it to the maps.
	 * The optionsBySource map will always be updated, to allow new groups to be defined. For a given {@link CLIOptionDefinition}
	 * name, all maps will always point to the exact same {@link CLIOptionDefinition} instance.
	 * @param source
	 * @param cliOptionDefinitions
	 * @return
	 */
	private CLIOptionDefinitions add(String source, CLIOptionDefinition... cliOptionDefinitions) {
		for ( CLIOptionDefinition def : cliOptionDefinitions ) {
			if ( !optionsByName.containsKey(def.getName()) ) {
				def = def.deepCopy();
				optionsByName.put(def.getName(), def);
				optionsByGroup.computeIfAbsent(def.getGroup(), k -> new LinkedHashSet<CLIOptionDefinition>()).add(def);
			}
			if ( source != null ) {
				// Add to optionsBySource if source is provided. We retrieve the CLIOptionDefinition from
				// the optionsByName map, to make sure that all maps are using the same CLIOptionDefinition
				// instances.
				optionsBySource.computeIfAbsent(source, k -> new LinkedHashSet<CLIOptionDefinition>()).add(optionsByName.get(def.getName()));
			}
		}
		return this;
	}
	
	/**
	 * Get all {@link CLIOptionDefinition} instances defined in this {@link CLIOptionDefinitions} instance
	 * @return
	 */
	public Collection<CLIOptionDefinition> getCLIOptionDefinitions() {
		return optionsByName.values();
	}
	
	/**
	 * Get all {@link CLIOptionDefinition} names instances defined in this {@link CLIOptionDefinitions} instance
	 * @return
	 */
	public Collection<String> getCLIOptionDefinitionNames() {
		return optionsByName.keySet();
	}
	
	/**
	 * Get the {@link CLIOptionDefinition} instance associated with the given name
	 * @param name
	 * @return
	 */
	public CLIOptionDefinition getCLIOptionDefinitionByName(String name) {
		return optionsByName.get(name);
	}
	
	/**
	 * Check whether this {@link CLIOptionDefinitions} instance contains a {@link CLIOptionDefinition}
	 * with the given name
	 * @param name
	 * @return
	 */
	public boolean containsCLIOptionDefinitionName(String name) {
		return optionsByName.containsKey(name);
	}
	
	/**
	 * Get all {@link CLIOptionDefinition} groups instances defined in this {@link CLIOptionDefinitions} instance
	 * @return
	 */
	public Collection<String> getCLIOptionDefinitionGroups() {
		return optionsByGroup.keySet();
	}
	
	/**
	 * Get the {@link CLIOptionDefinition} instances associated with the given group
	 * @param group
	 * @return
	 */
	public Collection<CLIOptionDefinition> getCLIOptionDefinitionsByGroup(String group) {
		return optionsByGroup.getOrDefault(group, EMPTY_COLLECTION);
	}
	
	/**
	 * Check whether this {@link CLIOptionDefinitions} instance contains a {@link CLIOptionDefinition}
	 * group with the given name
	 * @param group
	 * @return
	 */
	public boolean containsCLIOptionDefinitionGroup(String group) {
		return optionsByGroup.containsKey(group);
	}
	
	/**
	 * Get all sources defined in this {@link CLIOptionDefinitions} instance
	 * @return
	 */
	public Collection<String> getCLIOptionDefinitionSources() {
		return optionsBySource.keySet();
	}
	
	/**
	 * Get the {@link CLIOptionDefinition} instances associated with the given source
	 * @param source
	 * @return
	 */
	public Collection<CLIOptionDefinition> getCLIOptionDefinitionsBySource(String source) {
		return optionsBySource.getOrDefault(source, EMPTY_COLLECTION);
	}
	
	/**
	 * Get the {@link CLIOptionDefinition} instances that are not included in the given sourceToExclude.
	 * @param sourceToExclude
	 * @return
	 */
	public Collection<CLIOptionDefinition> getCLIOptionDefinitionsExludingSource(String sourceToExclude) {
		ArrayList<CLIOptionDefinition> result = new ArrayList<>(getCLIOptionDefinitions());
		Collection<CLIOptionDefinition> excludeDefs = getCLIOptionDefinitionsBySource(sourceToExclude);
		result.removeIf(o -> excludeDefs.contains(o));
		return result;
	}
	
	/**
	 * Check whether this {@link CLIOptionDefinitions} instance contains a source with the given name
	 * @param source
	 * @return
	 */
	public boolean containsCLIOptionDefinitionSource(String source) {
		return optionsBySource.containsKey(source);
	}

	/**
	 * Add all {@link CLIOptionDefinition} instances from the given {@link CLIOptionDefinitions} instance
	 * @param defs
	 * @return
	 */
	public CLIOptionDefinitions addAll(CLIOptionDefinitions defs) {
		if ( defs != null ) {
			add(defs.getCLIOptionDefinitions().toArray(new CLIOptionDefinition[]{}));
		}
		return this;
	}
	
	/**
	 * Add all {@link CLIOptionDefinition} instances provided by the given {@link ICLIOptionDefinitionProvider}
	 * @param provider
	 * @return
	 */
	public CLIOptionDefinitions addAll(ICLIOptionDefinitionProvider provider) {
		if ( provider != null ) { provider.addCLIOptionDefinitions(this); }
		return this;
	}
	
	/**
	 * Same as {@link #addAll(CLIOptionDefinitions)}, but associates the {@link CLIOptionDefinition}
	 * instances to the given source.
	 * @param source
	 * @param defs
	 * @return
	 */
	public CLIOptionDefinitions addAll(String source, CLIOptionDefinitions defs) {
		if ( defs != null ) {
			add(source, defs.getCLIOptionDefinitions().toArray(new CLIOptionDefinition[]{}));
		}
		return this;
	}
	
	/**
	 * Same as {@link #addAll(ICLIOptionDefinitionProvider)}, but associates the {@link CLIOptionDefinition}
	 * instances to the given source.
	 * @param source
	 * @param provider
	 * @return
	 */
	public CLIOptionDefinitions addAll(String source, ICLIOptionDefinitionProvider provider) {
		addAll(source, new CLIOptionDefinitions().addAll(provider));
		return this;
	}
}
