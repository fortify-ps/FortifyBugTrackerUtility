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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class holds all configured {@link CLIOptionDefinition} instances
 * indexed by both {@link CLIOptionDefinition} name and group.
 * 
 * @author Ruud Senden
 *
 */
public class CLIOptionDefinitions {
	private LinkedHashMap<String, CLIOptionDefinition> optionsByName = new LinkedHashMap<>();

	public CLIOptionDefinitions() {}
	
	public CLIOptionDefinitions(CLIOptionDefinitions... others) {
		for ( CLIOptionDefinitions other : others ) {
			addAll(other);
		}
	}

	/**
	 * Add the given {@link CLIOptionDefinition} instances to the {@link Map} of {@link CLIOptionDefinition}s
	 * @param cliOptionDefinitions
	 * @return
	 */
	public CLIOptionDefinitions add(CLIOptionDefinition... cliOptionDefinitions) {
		for ( CLIOptionDefinition def : cliOptionDefinitions ) {
			addByName(def);
		}
		return this;
	}

	private void addByName(CLIOptionDefinition def) {
		if ( !optionsByName.containsKey(def.getName()) ) {
			optionsByName.put(def.getName(), def);
		}
	}
	
	public boolean containsCLIOptionDefinitionName(String name) {
		return getCLIOptionDefinitionNames().contains(name);
	}
	
	public Set<String> getCLIOptionDefinitionNames() {
		return optionsByName.keySet();
	}
	
	public Collection<CLIOptionDefinition> getCLIOptionDefinitions() {
		return optionsByName.values();
	}
	
	public CLIOptionDefinition getCLIOptionDefinitionByName(String name) {
		return optionsByName.get(name);
	}
	
	public Map<String, LinkedHashSet<CLIOptionDefinition>> getCLIOptionDefinitionsByGroup() {
		LinkedHashMap<String, LinkedHashSet<CLIOptionDefinition>> result = new LinkedHashMap<>();
		for ( CLIOptionDefinition def : getCLIOptionDefinitions() ) {
			result.computeIfAbsent(def.getGroup(), k -> new LinkedHashSet<CLIOptionDefinition>()).add(def);
		}
		return result;
	}

	public CLIOptionDefinitions addAll(CLIOptionDefinitions defs) {
		add(defs.getCLIOptionDefinitions().toArray(new CLIOptionDefinition[]{}));
		return this;
	}
	
	public CLIOptionDefinitions allowedSources(String... allowedSources) {
		for ( CLIOptionDefinition def : getCLIOptionDefinitions() ) {
			def.allowedSources(allowedSources);
		}
		return this;
	}
}
