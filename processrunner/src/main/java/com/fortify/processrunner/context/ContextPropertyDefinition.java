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
 * This class describes a single context property that is
 * supported by a given {@link IProcessor} implementation.
 * It contains context property name, description, and
 * whether the property is required to be available in
 * the {@link Context}.
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
	 * Get the context property default value.
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
	
	public boolean isRequired(Context context) {
		return isRequired() && isNotIgnored(context);
	}
	
	public boolean isNotIgnored(Context context) {
		return ( ignoreIfPropertyNotSet==null || context.hasValue(ignoreIfPropertyNotSet) ) &&
				( ignoreIfPropertySet==null || !context.hasValue(ignoreIfPropertySet) );
	}

	private String readText() {
		return System.console()==null?readTextFromStdin():System.console().readLine(getDescription()+": ");
	}

	private String readPassword() {
		return System.console()==null?readTextFromStdin():new String(System.console().readPassword(getDescription()+": "));
	}
	
	private String readTextFromStdin() {
		System.out.print(String.format(getDescription()+ ": "));
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Error reading text from stdin", e);
		}
	}
	
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

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isReadFromConsole() {
		return readFromConsole;
	}
	
	public boolean isPassword() {
		return isPassword;
	}
	
	public String getIgnoreIfPropertySet() {
		return ignoreIfPropertySet;
	}
	
	public String getIgnoreIfPropertyNotSet() {
		return ignoreIfPropertyNotSet;
	}
	
	public ContextPropertyDefinition defaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public ContextPropertyDefinition readFromConsole(boolean readFromConsole) {
		this.readFromConsole = readFromConsole;
		return this;
	}

	public ContextPropertyDefinition isPassword(boolean isPassword) {
		this.isPassword = isPassword;
		return this;
	}

	public ContextPropertyDefinition ignoreIfPropertySet(String property) {
		this.ignoreIfPropertySet = property;
		return this;
	}

	public ContextPropertyDefinition ignoreIfPropertyNotSet(String property) {
		this.ignoreIfPropertyNotSet = property;
		return this;
	}
	
	@Override
	public String toString() {
		return "ContextPropertyDefinition [name=" + name + ", description=" + description + ", defaultValue="
				+ defaultValue + ", required=" + required + "]";
	}
}
