/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
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
package com.fortify.util.spring;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.SimpleExpressionEditor;
import com.fortify.util.spring.expression.TemplateExpression;
import com.fortify.util.spring.expression.TemplateExpressionEditor;

/**
 * This class provides utility methods for working with Spring {@link ApplicationContext} instances,
 * like reading {@link ApplicationContext} data from XML configuration files and managing related
 * {@link PropertyEditor} instances.
 * 
 * @author Ruud Senden
 *
 */
public final class SpringContextUtil {
	private static final Log LOG = LogFactory.getLog(SpringContextUtil.class);
	private static final Map<Class<?>, Class<? extends PropertyEditor>> PROPERTY_EDITORS = getPropertyEditors();
	
	private SpringContextUtil() {}
	
	/**
	 * Automatically load all {@link PropertyEditorWithTargetClass} implementations
	 * (annotated with {@link Component}) from 
	 * com.fortify.util.spring.propertyeditor (sub-)packages. 
	 * 
	 * TODO Seems like this was mostly used for {@link TemplateExpressionEditor} and
	 *      {@link SimpleExpressionEditor}; not sure why we didn't just place these
	 *      classes in the same package as {@link TemplateExpression} and 
	 *      {@link SimpleExpression}, as we have done now. This needs to be carefully
	 *      tested for all integrations, but if this works, then the following can
	 *      be deleted from this class:
	 *      - getPropertyEditors() method
	 *      - PROPERTY_EDITORS constant
	 *      - CustomEditorConfigurer in getBaseContext() method
	 * @return
	 */
	private static final Map<Class<?>, Class<? extends PropertyEditor>> getPropertyEditors() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("com.fortify.util.spring.propertyeditor");
		try {
			Map<Class<?>, Class<? extends PropertyEditor>> result = new HashMap<Class<?>, Class<? extends PropertyEditor>>();
			Map<String, PropertyEditorWithTargetClass> beans = ctx.getBeansOfType(PropertyEditorWithTargetClass.class);
			for ( Map.Entry<String, PropertyEditorWithTargetClass> entry : beans.entrySet() ) {
				Class<?> targetClass = entry.getValue().getTargetClass();
				Class<? extends PropertyEditor> propertyEditorClass = entry.getValue().getClass();
				result.put(targetClass, propertyEditorClass);
			}
			LOG.debug("[Process] Loaded PropertyEditors for classes: "+result);
			return result;
		} finally {
			ctx.close();
		}
	}
	
	/**
	 * Get a basic Spring {@link GenericApplicationContext} with additional 
	 * custom editors registered. The additional custom editors are automatically
	 * loaded from com.fortify.util.spring.propertyeditor (sub-)packages
	 * if they have the {@link Component} annotation.
	 * @return Basic Spring ApplicationContext with custom editors registered.
	 */
	public static final GenericApplicationContext getBaseContext() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.setClassLoader(SpringContextUtil.class.getClassLoader());
		
		CustomEditorConfigurer cec = new CustomEditorConfigurer();
		cec.setCustomEditors(PROPERTY_EDITORS);
		context.addBeanFactoryPostProcessor(cec);
		return context;
	}
	
	/**
	 * Add XML-based configuration from the given resource names to the given context.
	 * See {@link DefaultResourceLoader} for a description on how to format the
	 * resource names.
	 * After all resources have been added to the context, the context may need to
	 * be refreshed in order to process all bean definitions.
	 * @param context
	 * @param resourceNames
	 */
	public static final void addConfigurationFromXmlResources(BeanDefinitionRegistry context, boolean errorOnMissingResource, String... resourceNames) {
		ResourceLoader resourceLoader = new DefaultResourceLoader(SpringContextUtil.class.getClassLoader());
		for ( String resourceName : resourceNames ) {
			Resource resource = resourceLoader.getResource(resourceName);
			addConfigurationFromXmlResource(context, resource, errorOnMissingResource);
		}
	}
	
	/**
	 * Add XML-based configuration from the given file names to the given context.
	 * After all files have been added to the context, the context may need to
	 * be refreshed in order to process all bean definitions.
	 * @param context
	 * @param fileNames
	 */
	public static final void addConfigurationFromXmlFiles(BeanDefinitionRegistry context, boolean errorOnMissingResource, String... fileNames) {
		for ( String fileName : fileNames ) {
			Resource resource = new FileSystemResource(fileName);
			addConfigurationFromXmlResource(context, resource, errorOnMissingResource);
		}
	}

	public static void addConfigurationFromXmlResource(BeanDefinitionRegistry context, Resource resource, boolean errorOnMissingResource) {
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		if ( !resource.exists() ) {
			if ( errorOnMissingResource ) {
				throw new RuntimeException("Resource "+resource.getFilename()+" does not exist");
			} else {
				LOG.info("[Process] Resource "+resource.getFilename()+" does not exist; no bean definitions will be added");
			}
		} else {
			LOG.debug("[Process] Loading bean definitions from resource "+resource.getFilename());
			reader.loadBeanDefinitions(resource);
		}
	}
	
	/**
	 * <p>Load a Spring {@link GenericApplicationContext} from the given resource names.
	 * See {@link DefaultResourceLoader} for a description on how to format the
	 * resource names.</p>
	 * 
	 * <p>This method will refresh the context after loading all resources, this
	 * means that no more resources can be added to the returned context.</p>
	 * 
	 * @param errorOnMissingResource Whether to throw an exception on missing resource names
	 * @param resourceNames Resource names to load
	 * @return
	 */
	public static final GenericApplicationContext loadApplicationContextFromResources(boolean errorOnMissingResource, String... resourceNames) {
		GenericApplicationContext result = getBaseContext();
		addConfigurationFromXmlResources(result, errorOnMissingResource, resourceNames);
		result.refresh();
		return result;
	}
	
	/**
	 * <p>Load a Spring {@link GenericApplicationContext} from the given file names.</p>
	 * 
	 * <p>This method will refresh the context after loading all resources, this
	 * means that no more resources can be added to the returned context.</p>
	 * 
	 * @param fileNames
	 * @return
	 */
	public static final GenericApplicationContext loadApplicationContextFromFiles(boolean errorOnMissingResource, String... fileNames) {
		GenericApplicationContext result = getBaseContext();
		addConfigurationFromXmlFiles(result, errorOnMissingResource, fileNames);
		result.refresh();
		return result;
	}
	
	public static interface PropertyEditorWithTargetClass extends PropertyEditor {
		public Class<?> getTargetClass();
	}
}