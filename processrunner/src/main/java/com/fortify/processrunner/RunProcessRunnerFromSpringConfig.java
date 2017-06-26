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
package com.fortify.processrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.processrunner.context.IContextPropertyDefinitionProvider;
import com.fortify.processrunner.context.IContextUpdater;
import com.fortify.util.spring.SpringContextUtil;

/**
 * This class allows for running {@link ProcessRunner} instances based on Spring configuration.
 * 
 * @author Ruud Senden
 */
public class RunProcessRunnerFromSpringConfig {
	private static final Log LOG = LogFactory.getLog(RunProcessRunnerFromSpringConfig.class);
	private static final String DEFAULT_BEAN_NAME = "defaultProcessRunner";
	private final GenericApplicationContext appContext;
	
	/**
	 * Constructor for setting the Spring configuration file name
	 * @param springConfigFileName
	 */
	public RunProcessRunnerFromSpringConfig(String springConfigFileName) {
		checkConfigFile(springConfigFileName);
		LOG.info("[Process] Using Spring configuration file "+springConfigFileName);
		this.appContext = SpringContextUtil.loadApplicationContextFromFiles(true, springConfigFileName);
	}
	
	/**
	 * Run the {@link ProcessRunner} instance identified by the given 
	 * processRunnerName, using the given externalContext. If processRunnerName
	 * is null, we will try to identify a default {@link ProcessRunner} instance.
	 * The {@link ProcessRunner} will be run once for every {@link Context} 
	 * returned by our {@link #getContexts(ProcessRunner, Context)} method.
	 * @param externalContext
	 * @param processRunnerName
	 */
	public void run(Context externalContext, String processRunnerName) {
		processRunnerName = getProcessRunnerNameOrDefault(processRunnerName);
		LOG.info("[Process] Using process runner "+processRunnerName);
		ProcessRunner runner = getProcessRunner(processRunnerName);
		addDefaultValues(runner, externalContext);
		Collection<Context> contexts = getContexts(externalContext);
		for ( Context context : contexts ) {
			try {
				checkContext(runner, context);
				runner.run(context);
			} catch (Throwable t) {
				LOG.error("[Process] Error during process run for "+processRunnerName+": "+t.getLocalizedMessage(), t);
			}
		}
		LOG.info("[Process] Processing complete for " + processRunnerName);
	}

	/**
	 * This method indicates whether we can identify a default {@link ProcessRunner} instance
	 * @return
	 */
	public boolean hasDefaultProcessRunner() {
		return getDefaultProcessRunnerName() != null;
	}
	
	/**
	 * Get the list of enabled {@link ProcessRunner} names
	 * @return
	 */
	public Collection<String> getEnabledProcessRunnerNames() {
		return getEnabledProcessRunners().keySet();
	}
	
	/**
	 * Get the {@link ProcessRunner} for the given name
	 * @param processRunnerName
	 * @return
	 */
	private ProcessRunner getProcessRunner(String processRunnerName) {
		return appContext.getBean(processRunnerName, ProcessRunner.class);
	}
	
	/**
	 * Get the {@link ContextPropertyDefinitions} for the given {@link ProcessRunner} name
	 * @param processRunnerName
	 * @return
	 */
	public ContextPropertyDefinitions getContextPropertyDefinitions(String processRunnerName) {
		return getContextPropertyDefinitions(getProcessRunner(getProcessRunnerNameOrDefault(processRunnerName)), new Context());
	}
	
	/**
	 * Get the {@link ContextPropertyDefinitions} for the given {@link ProcessRunner} instance
	 * @param processRunnerName
	 * @return
	 */
	private final ContextPropertyDefinitions getContextPropertyDefinitions(ProcessRunner runner, Context context) {
		ContextPropertyDefinitions result = new ContextPropertyDefinitions();
		addContextPropertyDefinitionsFromProcessRunner(runner, result, context);
		addContextPropertyDefinitionsFromContext(result, context);
		addContextPropertyDefinitionsFromContextGenerator(result, context);
		return result;
	}

	/**
	 * Get the given processRunnerName, or try to determine a default {@link ProcessRunner}
	 * name if the given name is null
	 * @param processRunnerName
	 * @return
	 */
	private String getProcessRunnerNameOrDefault(String processRunnerName) {
		if ( processRunnerName==null ) {
			processRunnerName = getDefaultProcessRunnerName();
		}
		if ( processRunnerName==null ) {
			throw new IllegalArgumentException("No process runner names specified, and no default process runners available");
		}
		return processRunnerName;
	}
	
	/**
	 * <p>Get the {@link Context} instances to use to run the given {@link ProcessRunner} instance.
	 * This method will combine the provided external context and a configured context (if available),
	 * and use this combined context to generate {@link Context} instances.</p>
	 * 
	 * <p>If an enabled {@link IContextGenerator} has been configured, it will be invoked to generate
	 * the {@link Context} instances. If not, this method will simply return the single (combined)
	 * {@link Context} instance.</p>
	 * @param externalContext
	 * @return
	 */
	protected Collection<Context> getContexts(Context externalContext) {
		Context context = mergeContexts(getConfigContext(), externalContext);
		IContextGenerator contextGenerator = getEnabledContextGenerator();
		
		return contextGenerator == null
			? Arrays.asList(updateContext(context))
			: contextGenerator.generateContexts(context);
	}

	/**
	 * Update {@link Context} using the configured {@link IContextUpdater} instances
	 * @param context
	 * @return
	 */
	private Context updateContext(Context context) {
		Collection<IContextUpdater> contextUpdaters = getContextUpdaters();
		for ( IContextUpdater contextUpdater : contextUpdaters ) {
			contextUpdater.updateContext(context);
		}
		return context;
	}

	/**
	 * Get a {@link Context} combining all {@link Context} instances
	 * defined via the Spring configuration file. This method never 
	 * returns null, even if no {@link Context} instances have been
	 * defined in the Spring configuration file.
	 * @return
	 */
	private Context getConfigContext() {
		return mergeContexts(appContext.getBeansOfType(Context.class).values().toArray(new Context[]{}));
	}

	/**
	 * Merge the given contexts into a single context. If multiple contexts
	 * contain the same key, the last one wins.
	 * @param contexts
	 * @return
	 */
	private Context mergeContexts(Context... contexts) {
		Context result = new Context();
		if ( contexts != null ) {
			for ( Context context : contexts ) {
				result.putAll(context);
			}
		}
		return result;
	}
	
	/**
	 * Check the given {@link Context} for any missing context property values, based on
	 * the available {@link ContextPropertyDefinitions}
	 * @param runner
	 * @param context
	 */
	protected final void checkContext(ProcessRunner runner, Context context) {
		ContextPropertyDefinitions contextPropertyDefinitions = getContextPropertyDefinitions(runner, context);
		for ( ContextPropertyDefinition contextPropertyDefinition : contextPropertyDefinitions.values() ) {
			String name = contextPropertyDefinition.getName();
			if ( contextPropertyDefinition.isRequired(context) && !context.hasValue(name) ) {
				throw new IllegalStateException("ERROR: Required option -"+name+" not set");
			}
		}
	}
	
	protected final void addDefaultValues(ProcessRunner runner, Context context) {
		ContextPropertyDefinitions contextPropertyDefinitions = getContextPropertyDefinitions(runner, context);
		for ( ContextPropertyDefinition contextPropertyDefinition : contextPropertyDefinitions.values() ) {
			String name = contextPropertyDefinition.getName();
			if ( !context.hasValue(name) ) {
				context.put(name, contextPropertyDefinition.getDefaultValue(context));
			}
		}
	}

	protected final void addContextPropertyDefinitionsFromProcessRunner(ProcessRunner runner, ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		runner.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	protected final void addContextPropertyDefinitionsFromContext(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		context.addContextPropertyDefinitions(contextPropertyDefinitions);
	}
	
	protected final void addContextPropertyDefinitionsFromContextGenerator(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		IContextGenerator generator = getEnabledContextGenerator();
		if ( generator != null && generator instanceof IContextPropertyDefinitionProvider ) {
			((IContextPropertyDefinitionProvider)generator).addContextPropertyDefinitions(contextPropertyDefinitions, context);
		}
		
	}
	
	/**
	 * Check whether the given configuration file exists and is readable. 
	 * @param configFile
	 */
	protected final void checkConfigFile(String configFile) {
		Resource resource = new FileSystemResource(configFile);
		if ( !resource.exists() ) {
			throw new IllegalArgumentException("ERROR: Configuration file "+configFile+" does not exist");
		}
		if ( !resource.isReadable() ) {
			throw new IllegalArgumentException("ERROR: Configuration file "+configFile+" is not readable");
		}
	}

	/**
	 * Get all enabled {@link ProcessRunner} instances that are available through Spring configuration
	 * @return
	 */
	private Map<String, ProcessRunner> getEnabledProcessRunners() {
		Map<String, ProcessRunner> processRunnersMap = appContext.getBeansOfType(ProcessRunner.class);
		processRunnersMap.values().removeIf(new Predicate<ProcessRunner>() {
			public boolean test(ProcessRunner processRunner) {
				return !processRunner.isEnabled();
			}
		});
		return processRunnersMap;
	}
	
	/**
	 * Determine the default {@link ProcessRunner} name
	 * @return
	 */
	private String getDefaultProcessRunnerName() {
		Map<String, ProcessRunner> processRunnersMap = getEnabledProcessRunners();
		String result = null;
		if ( processRunnersMap.containsKey(DEFAULT_BEAN_NAME) ) {
			result = DEFAULT_BEAN_NAME;
		} else if (processRunnersMap.size()==1) {
			result = processRunnersMap.keySet().iterator().next();
		} else {
			Map<String, ProcessRunner> defaultRunners = new HashMap<String, ProcessRunner>(processRunnersMap);
			defaultRunners.values().removeIf(new Predicate<ProcessRunner>() {
				public boolean test(ProcessRunner processRunner) {
					return !processRunner.isDefault();
				}
			});
			if ( defaultRunners.size()==1 ) {
				return defaultRunners.keySet().iterator().next();
			} else if ( defaultRunners.size()>1 ) {
				throw new IllegalStateException("More than 1 process runner found");
			}
			result = defaultRunners.keySet().iterator().next();
		}
		return result;
	}
	
	/**
	 * Get the single configured instance of {@link IContextGenerator} that
	 * is enabled for generating {@link Context} instances
	 * @return
	 */
	protected IContextGenerator getEnabledContextGenerator() {
		IContextGenerator result = null;
		List<IContextGenerator> contextGenerators = new ArrayList<IContextGenerator>(getContextGenerators());
		contextGenerators.removeIf(new Predicate<IContextGenerator>() {
			public boolean test(IContextGenerator contextPropertyMapper) {
				return !contextPropertyMapper.isContextGeneratorEnabled();
			}
		});
		if ( contextGenerators.size()==1 ) {
			result = contextGenerators.get(0);
		} else if ( contextGenerators.size()>1 ) {
			throw new IllegalStateException("More than 1 enabled context generator found");
		}
		return result;
	}
	
	/**
	 * Get all configured {@link IContextGenerator} instances
	 * @return
	 */
	protected Collection<IContextGenerator> getContextGenerators() {
		return appContext.getBeansOfType(IContextGenerator.class).values();
	}
	
	/**
	 * Get all configured {@link IContextUpdater} instances
	 * @return
	 */
	protected Collection<IContextUpdater> getContextUpdaters() {
		return appContext.getBeansOfType(IContextUpdater.class).values();
	}
}
