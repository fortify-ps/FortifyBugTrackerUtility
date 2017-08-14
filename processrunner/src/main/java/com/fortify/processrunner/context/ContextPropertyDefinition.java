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
package com.fortify.processrunner.context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fortify.processrunner.processor.IProcessor;

/**
 * This class describes a single context property that is supported by a given {@link IProcessor} implementation.
 * It contains context property name, description, and whether the property is required to be available in
 * the {@link Context}. Optionally, this class also supports reading a context property value from the console
 * ({@link #readFromConsole} as either a normal string or password (hiding the input; {@link #isPassword(boolean)}.
 * The context property definition can optionally be ignored (not required and not read from console) if another
 * context property has been set ({@link #ignoreIfPropertySet}) or has not been set ({@link #getIgnoreIfPropertyNotSet()}. 
 * 
 * @author Ruud Senden
 */
public class ContextPropertyDefinition {
	private final String name;
	private final String description;
	private final boolean required;
	private String defaultValue = null;
	private boolean readFromConsole = false;
	private boolean isPassword = false;
	private String ignoreIfPropertySet = null;
	private String ignoreIfPropertyNotSet = null;
	
	
	/**
	 * Constructor for setting the context property name, description and required flag.
	 * @param name
	 * @param description
	 * @param required
	 */
	public ContextPropertyDefinition(String name, String description, boolean required) {
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
		return ( ignoreIfPropertyNotSet==null || context.hasValue(ignoreIfPropertyNotSet) ) &&
				( ignoreIfPropertySet==null || !context.hasValue(ignoreIfPropertySet) );
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
	 * Get the property name that, if set, will cause the current property definition to be ignored
	 * @return
	 */
	public String getIgnoreIfPropertySet() {
		return ignoreIfPropertySet;
	}
	
	/**
	 * Get the property name that, if not set, will cause the current property definition to be ignored
	 * @return
	 */
	public String getIgnoreIfPropertyNotSet() {
		return ignoreIfPropertyNotSet;
	}
	
	/**
	 * Set the default value
	 * @param defaultValue
	 * @return
	 */
	public ContextPropertyDefinition defaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	/**
	 * Set the 'read from console' flag
	 * @param readFromConsole
	 * @return
	 */
	public ContextPropertyDefinition readFromConsole(boolean readFromConsole) {
		this.readFromConsole = readFromConsole;
		return this;
	}

	/**
	 * Set the 'is password' flag
	 * @param isPassword
	 * @return
	 */
	public ContextPropertyDefinition isPassword(boolean isPassword) {
		this.isPassword = isPassword;
		return this;
	}

	/**
	 * Set the property name that, if set, will cause the current property definition to be ignored
	 * @param property
	 * @return
	 */
	public ContextPropertyDefinition ignoreIfPropertySet(String property) {
		this.ignoreIfPropertySet = property;
		return this;
	}

	/**
	 * Set the property name that, if not set, will cause the current property definition to be ignored
	 * @param property
	 * @return
	 */
	public ContextPropertyDefinition ignoreIfPropertyNotSet(String property) {
		this.ignoreIfPropertyNotSet = property;
		return this;
	}
	
	/**
	 * Get a human-readable presentation of this {@link ContextPropertyDefinition}
	 */
	@Override
	public String toString() {
		return "ContextPropertyDefinition [name=" + name + ", description=" + description + ", defaultValue="
				+ defaultValue + ", required=" + required + "]";
	}
}
