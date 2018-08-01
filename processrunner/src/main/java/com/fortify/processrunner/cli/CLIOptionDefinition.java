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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.fortify.processrunner.context.Context;

/**
 * This class describes a single command line option. It contains option name, description, and whether the option 
 * is required to be available in the current {@link Context}. The option definition can optionally be ignored 
 * (not required) if an alternative option has been set ({@link #isAlternativeForOptions(String...)}), or if other 
 * options that the current property depends on have not been set ({@link #dependsOnOptions(String...)}).
 * 
 * @author Ruud Senden
 */
public class CLIOptionDefinition {
	public static final String ALLOWED_SOURCE_CLI = "CLI option";
	private final String group;
	private final String name;
	private final String description;
	private final boolean required;
	private String defaultValue = null;
	private String defaultValueDescription = null;
	private boolean isPassword = false;
	private String[] isAlternativeForOptions = null;
	private String[] dependsOnOptions = null;
	private String[] allowedSources = {ALLOWED_SOURCE_CLI};
	private LinkedHashMap<String, String> allowedValues = null;
	private boolean isFlag = false;
	
	
	/**
	 * Constructor for setting the option name, description and required flag.
	 * @param name
	 * @param description
	 * @param required
	 */
	public CLIOptionDefinition(String group, String name, String description, boolean required) {
		this.group = group;
		this.name = name;
		this.description = description;
		this.required = required;
	}
	
	public String getGroup() {
		return group;
	}

	/**
	 * Get the context property name.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the context property description.
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * Indicate whether this context property is required and not ignored
	 * @param context
	 * @return
	 */
	public boolean isRequiredAndNotIgnored(Context context) {
		return isRequired() && isNotIgnored(context);
	}
	
	/**
	 * Indicate whether this context property is not ignored
	 * @param context
	 * @return
	 */
	public boolean isNotIgnored(Context context) {
		return ( dependsOnOptions==null || context.hasValueForAllKeys(dependsOnOptions) ) &&
				( isAlternativeForOptions==null || !context.hasValueForAnyKey(isAlternativeForOptions) );
	}

	/**
	 * Get the context property required flag.
	 * @return
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Get the configured default value
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String getDefaultValueDescription() {
		return StringUtils.defaultIfBlank(defaultValueDescription, getValueDescription(getDefaultValue()));
	}
	
	/**
	 * Get the 'is password' flag
	 * @return
	 */
	public boolean isPassword() {
		return isPassword;
	}
	
	/**
	 * Get the property names that, if any of them is set, will cause the current property definition to be ignored
	 * @return
	 */
	public String[] getIsAlternativeForOptions() {
		return isAlternativeForOptions;
	}
	
	/**
	 * Get the property name that, if not set, will cause the current property definition to be ignored
	 * @return
	 */
	public String[] getDependsOnOptions() {
		return dependsOnOptions;
	}
	
	public Map<String, String> getAllowedValues() {
		return allowedValues;
	}
	
	public boolean isFlag() {
		return isFlag;
	}
	
	public String[] getAllowedSources() {
		return allowedSources;
	}
	
	public boolean hideFromHelp() {
		return MapUtils.isNotEmpty(allowedValues) && allowedValues.size()==1 && allowedValues.containsKey(getDefaultValue());
	}
	
	/**
	 * Set the default value
	 * @param defaultValue
	 * @return
	 */
	public CLIOptionDefinition defaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	/**
	 * Set the default value description
	 * @param defaultValueDescription
	 * @return
	 */
	public CLIOptionDefinition defaultValueDescription(String defaultValueDescription) {
		this.defaultValueDescription = defaultValueDescription;
		return this;
	}

	/**
	 * Set the 'is password' flag
	 * @param isPassword
	 * @return
	 */
	public CLIOptionDefinition isPassword(boolean isPassword) {
		this.isPassword = isPassword;
		return this;
	}

	/**
	 * Set the option names that, if any of them is set, will cause the current option definition to be ignored
	 * @param options
	 * @return
	 */
	public CLIOptionDefinition isAlternativeForOptions(String... options) {
		this.isAlternativeForOptions = options;
		return this;
	}
	
	/**
	 * Set the allowed values for this option
	 * @param allowedValues
	 * @return
	 */
	public CLIOptionDefinition allowedValues(LinkedHashMap<String, String> allowedValues) {
		this.allowedValues = allowedValues;
		return this;
	}
	
	public CLIOptionDefinition allowedValue(String name, String description) {
		if ( this.allowedValues==null ) { this.allowedValues = new LinkedHashMap<>(); }
		this.allowedValues.put(name, description);
		return this;
	}

	/**
	 * Set the option names that, if any of them is not set, will cause the current option definition to be ignored
	 * @param property
	 * @return
	 */
	public CLIOptionDefinition dependsOnOptions(String... properties) {
		this.dependsOnOptions = properties;
		return this;
	}
	
	public CLIOptionDefinition isFlag(boolean isFlag) {
		this.isFlag = isFlag;
		return this;
	}
	
	public CLIOptionDefinition allowedSources(String... allowedSources) {
		this.allowedSources= allowedSources;
		return this;
	}
	
	public String getValue(Context context) {
		String result = getValueFromContext(context);
		if ( StringUtils.isBlank(result) ) {
			result = getDefaultValue();
			context.put(getName(), result);
		}
		if ( StringUtils.isBlank(result) && isRequiredAndNotIgnored(context) ) {
			// TODO Clean this up
			@SuppressWarnings("unchecked")
			List<String> optionNames = new ArrayList<String>(CollectionUtils.arrayToList(getIsAlternativeForOptions()));
			optionNames.add(getName());
			throw new IllegalArgumentException("Required CLI option "+StringUtils.join(optionNames, " or ")+" not defined");
		}
		if ( StringUtils.isNotBlank(result) && allowedValues!=null && !allowedValues.containsKey(result) ) {
			throw new IllegalArgumentException("CLI option value "+result+" not allowed for option "+getName());
		}
		return result;
	}

	public String getValueFromContext(Context context) {
		return context.get(getName(), String.class);
	}
	
	/**
	 * Get a human-readable presentation of this {@link CLIOptionDefinition}
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" [name=" + name + ", description=" + description + ", defaultValue="
				+ defaultValue + ", required=" + required + "]";
	}

	public String getCurrentValueDescription(Context context) {
		return getValueDescription(getValueFromContext(context));
	}
	
	private String getValueDescription(String value) {
		if ( StringUtils.isBlank(value) ) {
			return "<none>";
		} else if ( isPassword() ) { 
			return "<hidden>";
		} else {
			return value;
		}
	}
}
