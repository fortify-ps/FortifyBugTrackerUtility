package com.fortify.processrunner.context.mapper;

import java.util.Map;

import com.fortify.processrunner.context.Context;

/**
 * This interface serves two purposes:
 * <ul>
 *  <li>Adding context property values based on another context property value</li>
 *  <li>Generating a list of default values for a context property</li>
 * </ul>
 * 
 * <p>These functionalities are often closely related and therefore contained in 
 * a single interface, If the user specifies a value for the context property 
 * supported by a specific implementation of this class, then the implementation 
 * should simply generate any context property values based on the user-supplied 
 * context property value. If the user did not specify a value for the given context 
 * property, the implementation should generate a list of default values, together 
 * with any generated context property values for each generated default value.</p>
 * 
 * <p>See the various implementation of this class to gain a better understanding.</p>
 * 
 * @author Ruud Senden
 *
 */
public interface IContextPropertyMapper {
	public String getContextPropertyName();
	public void addMappedContextProperties(Context context, Object contextPropertyValue);
	public boolean isDefaultValuesGenerator();
	public Map<Object, Context> getDefaultValuesWithExtraContextProperties(Context initialContext);
}
