package com.fortify.processrunner.context.mapper;

import java.util.Collection;

import com.fortify.processrunner.context.Context;

// TODO Decide on the best name for this class; IContextPropertyMapper, IContextPropertyGenerator, IMappedContextPropertyGenerator, ...?
public interface IContextPropertyMapper {
	public String getContextPropertyName();
	public void addMappedContextProperties(Context context, Object contextPropertyValue);
	public boolean isDefaultValuesGenerator();
	public Collection<Object> getDefaultValues();
}
