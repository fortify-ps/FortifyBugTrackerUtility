package com.fortify.processrunner.context;

import java.beans.PropertyEditorSupport;

/**
 * This class allows for building {@link Context} instances from a String.
 * This is mostly used to configure a Context instance through a Spring
 * configuration file.
 * 
 * @author Ruud Senden
 *
 */
public final class ContextEditor extends PropertyEditorSupport {
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		Context context = new Context();
		String[] contextPropertiesWithValues = text.split(",");
		for ( String contextPropertyWithValue : contextPropertiesWithValues ) {
			int idx = contextPropertyWithValue.indexOf('=');
			String name = contextPropertyWithValue.substring(0, idx);
			String value = contextPropertyWithValue.substring(idx+1);
			context.put(name, value);
		}
		setValue(context);
	}
}