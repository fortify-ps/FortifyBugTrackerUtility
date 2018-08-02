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
package com.fortify.processrunner.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This class holds all configured {@link CLIOptionDefinition} instances
 * indexed by both {@link CLIOptionDefinition} name and group.
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
	 * Add the given {@link CLIOptionDefinition} instances to the {@link Map} of {@link CLIOptionDefinition}s
	 * @param cliOptionDefinitions
	 * @return
	 */
	public CLIOptionDefinitions add(CLIOptionDefinition... cliOptionDefinitions) {
		return add(null, cliOptionDefinitions);
	}
	
	private CLIOptionDefinitions add(String source, CLIOptionDefinition... cliOptionDefinitions) {
		for ( CLIOptionDefinition def : cliOptionDefinitions ) {
			if ( !optionsByName.containsKey(def.getName()) ) {
				def = def.deepCopy();
				optionsByName.put(def.getName(), def);
				optionsByGroup.computeIfAbsent(def.getGroup(), k -> new LinkedHashSet<CLIOptionDefinition>()).add(def);
			}
			if ( source != null ) {
				optionsBySource.computeIfAbsent(source, k -> new LinkedHashSet<CLIOptionDefinition>()).add(optionsByName.get(def.getName()));
			}
		}
		return this;
	}
	
	public Collection<CLIOptionDefinition> getCLIOptionDefinitions() {
		return optionsByName.values();
	}
	
	public Collection<String> getCLIOptionDefinitionNames() {
		return optionsByName.keySet();
	}
	
	public CLIOptionDefinition getCLIOptionDefinitionByName(String name) {
		return optionsByName.get(name);
	}
	
	public boolean containsCLIOptionDefinitionName(String name) {
		return optionsByName.containsKey(name);
	}
	
	public Collection<String> getCLIOptionDefinitionGroups() {
		return optionsByGroup.keySet();
	}
	
	public Collection<CLIOptionDefinition> getCLIOptionDefinitionsByGroup(String group) {
		return optionsByGroup.getOrDefault(group, EMPTY_COLLECTION);
	}
	
	public boolean containsCLIOptionDefinitionGroup(String group) {
		return optionsByGroup.containsKey(group);
	}
	
	public Collection<String> getCLIOptionDefinitionSources() {
		return optionsBySource.keySet();
	}
	
	public Collection<CLIOptionDefinition> getCLIOptionDefinitionsBySource(String source) {
		return optionsBySource.getOrDefault(source, EMPTY_COLLECTION);
	}
	
	public Collection<CLIOptionDefinition> getCLIOptionDefinitionsExludingSource(String sourceToExclude) {
		ArrayList<CLIOptionDefinition> result = new ArrayList<>(getCLIOptionDefinitions());
		Collection<CLIOptionDefinition> excludeDefs = getCLIOptionDefinitionsBySource(sourceToExclude);
		result.removeIf(o -> excludeDefs.contains(o));
		return result;
	}
	
	public boolean containsCLIOptionDefinitionSource(String source) {
		return optionsBySource.containsKey(source);
	}

	public CLIOptionDefinitions addAll(CLIOptionDefinitions defs) {
		if ( defs != null ) {
			add(defs.getCLIOptionDefinitions().toArray(new CLIOptionDefinition[]{}));
		}
		return this;
	}
	
	public CLIOptionDefinitions addAll(ICLIOptionDefinitionProvider provider) {
		if ( provider != null ) { provider.addCLIOptionDefinitions(this); }
		return this;
	}
	
	public CLIOptionDefinitions addAll(String source, CLIOptionDefinitions defs) {
		if ( defs != null ) {
			add(source, defs.getCLIOptionDefinitions().toArray(new CLIOptionDefinition[]{}));
		}
		return this;
	}
	
	public CLIOptionDefinitions addAll(String source, ICLIOptionDefinitionProvider provider) {
		addAll(source, new CLIOptionDefinitions().addAll(provider));
		return this;
	}
}
