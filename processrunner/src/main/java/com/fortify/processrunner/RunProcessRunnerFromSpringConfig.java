package com.fortify.processrunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.mapper.IContextPropertyMapper;
import com.fortify.util.spring.SpringContextUtil;

public class RunProcessRunnerFromSpringConfig {
	private static final Log LOG = LogFactory.getLog(RunProcessRunnerFromSpringConfig.class);
	private static final String DEFAULT_BEAN_NAME = "defaultProcessRunner";
	private final GenericApplicationContext appContext;
	
	public RunProcessRunnerFromSpringConfig(String springConfigFileName) {
		checkConfigFile(springConfigFileName);
		LOG.info("[Process] Using Spring configuration file "+springConfigFileName);
		this.appContext = SpringContextUtil.loadApplicationContextFromFiles(true, springConfigFileName);
	}
	
	public void run(Context externalContext, String processRunnerName) {
		processRunnerName = getProcessRunnerNameOrDefault(processRunnerName);
		LOG.info("[Process] Using process runner "+processRunnerName);
		ProcessRunner runner = getProcessRunner(processRunnerName);
		List<Context> contexts = getContexts(runner, externalContext);
		for ( Context context : contexts ) {
			try {
				runner.run(context);
			} catch (Throwable t) {
				LOG.fatal("[Process] Error during process run for "+processRunnerName, t);
			} finally {
				LOG.info("[Process] Processing complete for " + processRunnerName);
			}
		}
	}

	public boolean hasDefaultProcessRunner() {
		return getDefaultProcessRunnerName() != null;
	}
	
	public Collection<String> getEnabledProcessRunnerNames() {
		return getEnabledProcessRunners().keySet();
	}
	
	private ProcessRunner getProcessRunner(String processRunnerName) {
		return appContext.getBean(processRunnerName, ProcessRunner.class);
	}
	
	public Collection<ContextPropertyDefinition> getContextPropertyDefinitions(String processRunnerName) {
		return getContextPropertyDefinitions(new Context(), getProcessRunner(getProcessRunnerNameOrDefault(processRunnerName)));
	}
	
	private final Collection<ContextPropertyDefinition> getContextPropertyDefinitions(Context context, ProcessRunner runner) {
		Set<ContextPropertyDefinition> result = new LinkedHashSet<ContextPropertyDefinition>();
		addContextPropertyDefinitionsFromProcessRunner(runner, result, context);
		addContextPropertyDefinitionsFromContext(result, context);
		return result;
	}

	private String getProcessRunnerNameOrDefault(String processRunnerName) {
		if ( processRunnerName==null ) {
			processRunnerName = getDefaultProcessRunnerName();
		}
		if ( processRunnerName==null ) {
			throw new IllegalArgumentException("No process runner names specified, and no default process runners available");
		}
		return processRunnerName;
	}
	
	protected List<Context> getContexts(ProcessRunner runner, Context externalContext) {
		List<Context> result = new ArrayList<Context>();
		Context context = mergeContexts(getConfigContext(), externalContext);
		IContextPropertyMapper defaultValuesGenerator = getDefaultValuesGenerator();
		if ( defaultValuesGenerator == null 
				|| context.containsKey(defaultValuesGenerator.getContextPropertyName()) ) {
			result.add(addMappedContextPropertiesAndCheckContext(runner, context));
			context.refresh();
		} else {
			String defaultValuesContextPropertyName = defaultValuesGenerator.getContextPropertyName();
			if ( getContextPropertyDefinition(context, runner, defaultValuesContextPropertyName)==null ) {
				throw new IllegalStateException("TODO");
			}
			Collection<Object> defaultValues = defaultValuesGenerator.getDefaultValues();
			for ( Object defaultValue : defaultValues ) {
				Context contextWithDefaultValue = new Context(context);
				contextWithDefaultValue.put(defaultValuesContextPropertyName, defaultValue);
				result.add(addMappedContextPropertiesAndCheckContext(runner, contextWithDefaultValue));
				contextWithDefaultValue.refresh();
			}
		}
		return result;
	}

	private Object getContextPropertyDefinition(Context context, ProcessRunner runner, String contextPropertyName) {
		Collection<ContextPropertyDefinition> defs = getContextPropertyDefinitions(context, runner);
		for ( ContextPropertyDefinition def : defs ) {
			if ( def.getName().equals(contextPropertyName) ) {
				return def;
			}
		}
		return null;
	}

	private Context addMappedContextPropertiesAndCheckContext(ProcessRunner runner, Context context) {
		Collection<IContextPropertyMapper> contextPropertyMappers = getContextPropertyMappers();
		for ( IContextPropertyMapper contextPropertyMapper : contextPropertyMappers ) {
			Object contextPropertyValue = context.get(contextPropertyMapper.getContextPropertyName());
			contextPropertyMapper.addMappedContextProperties(context, contextPropertyValue);
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
	
	protected final void checkContext(Context context, Collection<ContextPropertyDefinition> contextPropertyDefinitions) {
		for ( ContextPropertyDefinition contextProperty : contextPropertyDefinitions ) {
			if ( contextProperty.isRequired() && !context.containsKey(contextProperty.getName()) ) {
				throw new IllegalStateException("ERROR: Required option -"+contextProperty.getName()+" not set");
			}
		}
	}

	protected final void addContextPropertyDefinitionsFromProcessRunner(ProcessRunner runner, Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
		runner.addContextPropertyDefinitions(contextPropertyDefinitions, context);
	}
	
	protected final void addContextPropertyDefinitionsFromContext(Collection<ContextPropertyDefinition> contextPropertyDefinitions, Context context) {
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

	private Map<String, ProcessRunner> getEnabledProcessRunners() {
		Map<String, ProcessRunner> processRunnersMap = appContext.getBeansOfType(ProcessRunner.class);
		processRunnersMap.values().removeIf(new Predicate<ProcessRunner>() {
			public boolean test(ProcessRunner processRunner) {
				return !processRunner.isEnabled();
			}
		});
		return processRunnersMap;
	}
	
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
	
	protected Collection<IContextPropertyMapper> getContextPropertyMappers() {
		return appContext.getBeansOfType(IContextPropertyMapper.class).values();
	}
}
