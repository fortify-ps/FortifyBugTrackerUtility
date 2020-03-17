/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
package com.fortify.processrunner.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

public final class HelpPrinter {
	private int width = 80;
	private final StringBuffer sb = new StringBuffer();
	
	public HelpPrinter() {
		try {
			this.width = org.jline.terminal.TerminalBuilder.terminal().getWidth();
		} catch (IOException e) {}
		if ( this.width < 10 ) {
			this.width = 80;
		}
	}

	public HelpPrinter append(int indent, String str) {
		int leadingSpaces = str.indexOf(str.trim());
		indent += leadingSpaces;
		str = str.substring(leadingSpaces);
		String padding = StringUtils.leftPad("",indent);
		sb.append(padding+WordUtils.wrap(str, width-indent, "\n"+padding, false)).append("\n");
		return this;
	}
	
	public HelpPrinter appendEmptyLn() {
		sb.append("\n");
		return this;
	}
	
	public KeyValueGroupBuilder keyValueGroupBuilder() {
		return new KeyValueGroupBuilder(this);
	}
	
	public void printHelp() {
		System.out.println(sb.toString());
	}
	
	public final class KeyValueGroupBuilder {
		private final LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
		private final HelpPrinter hp;
		private KeyValueGroupBuilder(HelpPrinter hp) {
			this.hp = hp;
		}
		
		public KeyValueGroupBuilder append(Map<String, String[]> keysAndValues) {
			map.putAll(keysAndValues);
			return this;
		}
		
		public KeyValueGroupBuilder append(String key, String... values) {
			if ( values != null ) {
				map.put(key, values);
			}
			return this;
		}
		
		public KeyValueGroupBuilder append(String key, Map<String, String> values) {
			if ( MapUtils.isNotEmpty(values) ) {
				List<String> valuesList = new ArrayList<>();
				for ( Map.Entry<String, String> entry : values.entrySet() ) {
					valuesList.add(entry.getKey());
					valuesList.add("  "+entry.getValue());
					append(key, valuesList.toArray(new String[] {}));
				}
			}
			return this;
		}
		
		public HelpPrinter build(int indent) {
			if ( MapUtils.isNotEmpty(map) ) {
				int maxKeyLength = map.keySet().stream().max(Comparator.comparingInt(String::length)).get().length();
				for ( Map.Entry<String, String[]> entry : map.entrySet() ) { 
					int newIndent = indent;
					String key = entry.getKey();
					for ( String value : entry.getValue() ) {
						if ( newIndent==indent ) {
							hp.append(indent, StringUtils.rightPad(key+": ", maxKeyLength+2)+value);
							newIndent = indent+maxKeyLength+2;
						} else {
							hp.append(newIndent, value);
						}
					}
				}
			}
			return hp;
		}
	}
}