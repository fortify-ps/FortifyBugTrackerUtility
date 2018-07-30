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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.processor.IProcessor;

/**
 * This class describes a single command line option that is supported by a given {@link IProcessor} implementation.
 * It contains option name, description, and whether the property is required to be available in the current
 * {@link Context}. Optionally, this class also supports reading a context property value from the console
 * ({@link #readFromConsole} as either a normal string or password (hiding the input; {@link #isPassword(boolean)}.
 * The option definition can optionally be ignored (not required and not read from console) if an alternative 
 * option has been set ({@link #isAlternativeForOptions(String...)}), or if other options that the current property 
 * depends on have not been set ({@link #dependsOnOptions(String...)}).
 * 
 * @author Ruud Senden
 */
public class CLIOptionDefinition {
	private final String name;
	private final String description;
	private final boolean required;
	private String defaultValue = null;
	private boolean readFromConsole = false;
	private boolean isPassword = false;
	private String[] isAlternativeForOptions = null;
	private String[] dependsOnOptions = null;
	
	
	/**
	 * Constructor for setting the option name, description and required flag.
	 * @param name
	 * @param description
	 * @param required
	 */
	public CLIOptionDefinition(String name, String description, boolean required) {
		super();
		this.name = name;
		this.description = description;
		this.required = required;
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
	 * Get the context property default value, optionally reading the
	 * property value from console.
	 * @return
	 */
	public String getDefaultValue(Context context) {
		String result = defaultValue;
		if ( isReadFromConsole() && isNotIgnored(context) ) {
			if ( isPassword ) {
				result = readPassword();
			} else {
				result = readText();
			}
		}
		return result;
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
	 * Read text from console (if available) or from StdIn (to support running in Eclipse)
	 * @return
	 */
	private String readText() {
		return System.console()==null?readTextFromStdin():System.console().readLine(getDescription()+": ");
	}

	/**
	 * Read password from console (if available) or from StdIn (to support running in Eclipse).
	 * Note that the input will not be hidden when reading from StdIn.
	 * @return
	 */
	private String readPassword() {
		return System.console()==null?readTextFromStdin():new String(System.console().readPassword(getDescription()+": "));
	}
	
	/**
	 * Read text from StdIn
	 * @return
	 */
	private String readTextFromStdin() {
		System.out.print(String.format(getDescription()+ ": "));
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Error reading text from stdin", e);
		}
	}
	
	/**
	 * Get a description for the default value; either 'Read from console' if
	 * reading from console is enabled, otherwise the configured default value.
	 * @return
	 */
	public String getDefaultValueDescription() {
		return readFromConsole ? "Read from console" : defaultValue;
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

	/**
	 * Get the 'read from console' flag
	 * @return
	 */
	public boolean isReadFromConsole() {
		return readFromConsole;
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
	 * Set the 'read from console' flag
	 * @param readFromConsole
	 * @return
	 */
	public CLIOptionDefinition readFromConsole(boolean readFromConsole) {
		this.readFromConsole = readFromConsole;
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
	 * @param property
	 * @return
	 */
	public CLIOptionDefinition isAlternativeForOptions(String... properties) {
		this.isAlternativeForOptions = properties;
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
	
	public String getValue(Context context) {
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
}
