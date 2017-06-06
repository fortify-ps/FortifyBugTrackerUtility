package com.fortify.processrunner;

import java.util.ArrayList;
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
import com.fortify.processrunner.context.mapper.IContextPropertyMapper;
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
		List<Context> contexts = getContexts(runner, externalContext);
		for ( Context context : contexts ) {
			try {
				runner.run(context);
			} catch (Throwable t) {
				LOG.error("[Process] Error during process run for "+processRunnerName+": "+t.getLocalizedMessage());
				LOG.debug("Details", t);
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
		return getContextPropertyDefinitions(new Context(), getProcessRunner(getProcessRunnerNameOrDefault(processRunnerName)));
	}
	
	/**
	 * Get the {@link ContextPropertyDefinitions} for the given {@link ProcessRunner} instance
	 * @param processRunnerName
	 * @return
	 */
	private final ContextPropertyDefinitions getContextPropertyDefinitions(Context context, ProcessRunner runner) {
		ContextPropertyDefinitions result = new ContextPropertyDefinitions();
		addContextPropertyDefinitionsFromProcessRunner(runner, result, context);
		addContextPropertyDefinitionsFromContext(result, context);
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
	 * Get the {@link Context} instances to use to run the given {@link ProcessRunner} instance.
	 * This method will generate {@link Context} instances based on the following procedure:
	 * <ul>
	 *   <li>Merge the provided externalContext with static Context instances provided in the Spring configuration</li>
	 *   <li>Generate a new context for every default value provided by an {@link IContextPropertyMapper} (if available)</li>
	 *   <li>Add any mapped context properties based on other {@link IContextPropertyMapper} instances</li>
	 *   <li>Check the {@link Context} for any missing context property values, based on {@link ContextPropertyDefinitions}</li> 
	 * </ul>
	 * @param runner
	 * @param externalContext
	 * @return
	 */
	protected List<Context> getContexts(ProcessRunner runner, Context externalContext) {
		List<Context> result = new ArrayList<Context>();
		Context context = mergeContexts(getConfigContext(), externalContext);
		IContextPropertyMapper defaultValuesGenerator = getDefaultValuesGenerator();
		if ( defaultValuesGenerator == null 
				|| context.containsKey(defaultValuesGenerator.getContextPropertyName()) ) {
			result.add(addMappedContextPropertiesAndCheckContext(runner, context, null));
		} else {
			String defaultValuesContextPropertyName = defaultValuesGenerator.getContextPropertyName();
			if ( getContextPropertyDefinitions(context, runner).get(defaultValuesContextPropertyName)==null ) {
				throw new IllegalStateException("Unknown context property: "+defaultValuesContextPropertyName);
			}
			Map<Object, Context> defaultValues = defaultValuesGenerator.getDefaultValuesWithExtraContextProperties(context);
			if ( defaultValues==null || defaultValues.isEmpty() ) {
				LOG.info("[Process] No default values available");
			} else {
				for ( Map.Entry<Object, Context> defaultValue : defaultValues.entrySet() ) {
					Context contextWithDefaultValue = mergeContexts(context, defaultValue.getValue());
					contextWithDefaultValue.put(defaultValuesContextPropertyName, defaultValue.getKey());
					result.add(addMappedContextPropertiesAndCheckContext(runner, contextWithDefaultValue, defaultValuesGenerator));
				}
			}
		}
		return result;
	}

	/**
	 * Add mapped context properties using the configured {@link IContextPropertyMapper} instances,
	 * optionally ignoring the given {@link IContextPropertyMapper} (to avoid re-adding properties
	 * already provided through a default values generator)
	 * @param runner
	 * @param context
	 * @param ignore
	 * @return
	 */
	private Context addMappedContextPropertiesAndCheckContext(ProcessRunner runner, Context context, IContextPropertyMapper ignore) {
		Collection<IContextPropertyMapper> contextPropertyMappers = getContextPropertyMappers();
		for ( IContextPropertyMapper contextPropertyMapper : contextPropertyMappers ) {
			if ( contextPropertyMapper != ignore ) {
				Object contextPropertyValue = context.get(contextPropertyMapper.getContextPropertyName());
				contextPropertyMapper.addMappedContextProperties(context, contextPropertyValue);
			}
		}
		checkContext(context, getContextPropertyDefinitions(context, runner));
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
	 * the given {@link ContextPropertyDefinitions}
	 * @param context
	 * @param contextPropertyDefinitions
	 */
	protected final void checkContext(Context context, ContextPropertyDefinitions contextPropertyDefinitions) {
		for ( ContextPropertyDefinition contextProperty : contextPropertyDefinitions.values() ) {
			if ( contextProperty.isRequired() && !context.containsKey(contextProperty.getName()) ) {
				throw new IllegalStateException("ERROR: Required option -"+contextProperty.getName()+" not set");
			}
		}
	}

	protected final void addContextPropertyDefinitionsFromProcessRunner(ProcessRunner runner, ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		runner.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	protected final void addContextPropertyDefinitionsFromContext(ContextPropertyDefinitions contextPropertyDefinitions, Context context) {
		context.addContextPropertyDefinitions(contextPropertyDefinitions);
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
	 * Get the single configured instance of {@link IContextPropertyMapper} that
	 * is enabled for generating default values for some specific context property
	 * @return
	 */
	protected IContextPropertyMapper getDefaultValuesGenerator() {
		IContextPropertyMapper result = null;
		List<IContextPropertyMapper> contextPropertyMappers = new ArrayList<IContextPropertyMapper>(getContextPropertyMappers());
		contextPropertyMappers.removeIf(new Predicate<IContextPropertyMapper>() {
			public boolean test(IContextPropertyMapper contextPropertyMapper) {
				return !contextPropertyMapper.isDefaultValuesGenerator();
			}
		});
		if ( contextPropertyMappers.size()==1 ) {
			result = contextPropertyMappers.get(0);
		} else if ( contextPropertyMappers.size()>1 ) {
			throw new IllegalStateException("More than 1 default values generator found");
		}
		return result;
	}
	
	/**
	 * Get all configured {@link IContextPropertyMapper} instances
	 * @return
	 */
	protected Collection<IContextPropertyMapper> getContextPropertyMappers() {
		return appContext.getBeansOfType(IContextPropertyMapper.class).values();
	}
}
