/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
package com.fortify.processrunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fortify.processrunner.cli.CLIOptionDefinition;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.cli.ICLIOptionDefinitionProvider;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.util.spring.SpringContextUtil;

/**
 * This class allows for running {@link AbstractProcessRunner} instances based on Spring configuration.
 * 
 * @author Ruud Senden
 */
public class RunProcessRunnerFromSpringConfig {
	private static final Log LOG = LogFactory.getLog(RunProcessRunnerFromSpringConfig.class);
	private static final String CLI_OPTIONS_DEFAULT_VALUES_BEAN_NAME = "cliOptionsDefaultValues";
	private static final String ALLOWED_SOURCE_DEFAULT_VALUES_BEAN = CLI_OPTIONS_DEFAULT_VALUES_BEAN_NAME+" bean"; 
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
	 * Run the {@link AbstractProcessRunner} instance defined in the configuration file.
	 * The {@link AbstractProcessRunner} will be run once for every {@link Context} 
	 * returned by our {@link #getContexts(AbstractProcessRunner, Context)} method.
	 * @param externalContext
	 * @param processRunnerName
	 */
	public void run(Context initialContext) {
		AbstractProcessRunner runner = getProcessRunner();
		addDefaultValues(initialContext);
		Collection<Context> contexts = getContexts(initialContext);
		for ( Context context : contexts ) {
			try {
				checkContext(context);
				runner.run(context);
			} catch (Throwable t) {
				LOG.error("[Process] Error during process run: "+t.getLocalizedMessage(), t);
			}
		}
		LOG.info("[Process] Processing complete");
	}

	/**
	 * Get the {@link CLIOptionDefinitions} for the configured {@link AbstractProcessRunner} 
	 * and {@link IContextGenerator} instances.
	 * @param context
	 * @return
	 */
	public final CLIOptionDefinitions getCLIOptionDefinitions(Context context) {
		CLIOptionDefinitions defsFromContextGenerator = getCLIOptionDefinitions(context, getContextGenerator());
		CLIOptionDefinitions defsFromProcessRunner = getCLIOptionDefinitions(context, getProcessRunner());
		updateCLIOptionDefinitions(defsFromContextGenerator, defsFromProcessRunner);
		return new CLIOptionDefinitions(defsFromContextGenerator, defsFromProcessRunner);
	}
	
	/**
	 * Update the given {@link CLIOptionDefinitions} with default values from
	 * the configuration file, and with information from the configured
	 * {@link IContextGenerator}.
	 * 
	 * @param optionDefinitions
	 */
	private void updateCLIOptionDefinitions(CLIOptionDefinitions optionDefinitionsFromContextGenerator, CLIOptionDefinitions optionDefinitionsFromProcessRunner) {
		Context cliOptionsDefaultValuesFromConfig = getCLIOptionsDefaultValuesFromConfig();
		updateCLIOptionDefinitionsDefaultValues(optionDefinitionsFromContextGenerator, cliOptionsDefaultValuesFromConfig);
		updateCLIOptionDefinitionsDefaultValues(optionDefinitionsFromProcessRunner, cliOptionsDefaultValuesFromConfig);
		if ( getContextGenerator()!=null ) {
			getContextGenerator().updateCLIOptionDefinitionsDefaultValueDescriptions(optionDefinitionsFromProcessRunner);
			optionDefinitionsFromProcessRunner.allowedSources(CLIOptionDefinition.ALLOWED_SOURCE_CLI, ALLOWED_SOURCE_DEFAULT_VALUES_BEAN, getContextGenerator().getCLIOptionDefinitionAllowedSource());
		} else {
			optionDefinitionsFromProcessRunner.allowedSources(CLIOptionDefinition.ALLOWED_SOURCE_CLI, ALLOWED_SOURCE_DEFAULT_VALUES_BEAN);
		}
		optionDefinitionsFromContextGenerator.allowedSources(CLIOptionDefinition.ALLOWED_SOURCE_CLI, ALLOWED_SOURCE_DEFAULT_VALUES_BEAN);
	}
	
	/**
	 * Update default values for CLI options based on the cliOptionsDefaultValuesFromConfig bean.
	 * @param optionDefinitions
	 * @param cliOptionsDefaultValuesFromConfig
	 */
	private void updateCLIOptionDefinitionsDefaultValues(CLIOptionDefinitions optionDefinitions, Context cliOptionsDefaultValuesFromConfig) {
		if ( cliOptionsDefaultValuesFromConfig != null ) {
			for ( Map.Entry<String, Object> entry : cliOptionsDefaultValuesFromConfig.entrySet() ) {
				CLIOptionDefinition def = optionDefinitions.getCLIOptionDefinitionByName(entry.getKey());
				if ( def != null ) {
					def.defaultValue((String)entry.getValue());
				}
			}
		}
		
	}

	/**
	 * <p>Get the {@link Context} instances to use to run the given {@link AbstractProcessRunner} instance.
	 * This method will combine the provided external context and a configured context (if available),
	 * and use this combined context to generate {@link Context} instances.</p>
	 * 
	 * <p>If an enabled {@link IContextGenerator} has been configured, it will be invoked to generate
	 * the {@link Context} instances. If not, this method will simply return the single (combined)
	 * {@link Context} instance.</p>
	 * @param externalContext
	 * @return
	 */
	protected Collection<Context> getContexts(Context initialContext) {
		IContextGenerator contextGenerator = getContextGenerator();
		
		return contextGenerator == null
			? Arrays.asList(initialContext)
			: contextGenerator.generateContexts(initialContext);
	}

	/**
	 * Generate an initial {@link Context} from the Spring configuration file. 
	 * This method never returns null, even if no {@link Context} properties 
	 * have been defined in the Spring configuration file.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Context getCLIOptionsDefaultValuesFromConfig() {
		Context result = new Context();
		if ( appContext.containsBean(CLI_OPTIONS_DEFAULT_VALUES_BEAN_NAME) ) {
			result.putAll(appContext.getBean(CLI_OPTIONS_DEFAULT_VALUES_BEAN_NAME, Map.class));
		}
		return result;
	}

	/**
	 * Check the given {@link Context} for any missing context property values, based on
	 * the available {@link CLIOptionDefinitions}
	 * @param context
	 */
	protected final void checkContext(Context context) {
		CLIOptionDefinitions cliOptionDefinitions = getCLIOptionDefinitions(context);
		for ( CLIOptionDefinition cLIOptionDefinition : cliOptionDefinitions.getCLIOptionDefinitions() ) {
			String name = cLIOptionDefinition.getName();
			if ( cLIOptionDefinition.isRequiredAndNotIgnored(context) && !context.hasValueForKey(name) ) {
				throw new IllegalStateException("ERROR: Required option -"+name+" not set");
			}
		}
	}
	
	/**
	 * Add the default values to the {@link Context} for any CLI options that have not 
	 * been specified otherwise. This is necessary in case CLI options are accessed 
	 * directly from the {@link Context} instead of through the {@link CLIOptionDefinition#getValue(Context)}
	 * method, for example when accessing CLI options through Spring expressions. 
	 * @param runner
	 * @param context
	 */
	protected final void addDefaultValues(Context context) {
		CLIOptionDefinitions cLIOptionDefinitions = getCLIOptionDefinitions(context);
		for ( CLIOptionDefinition cLIOptionDefinition : cLIOptionDefinitions.getCLIOptionDefinitions() ) {
			String name = cLIOptionDefinition.getName();
			if ( !context.hasValueForKey(name) ) {
				String defaultValue = cLIOptionDefinition.getDefaultValue();
				if ( StringUtils.isNotBlank(defaultValue) ) {
					context.put(name, defaultValue);
				}
			}
		}
	}
	
	private final CLIOptionDefinitions getCLIOptionDefinitions(Context context, Object provider) {
		CLIOptionDefinitions defs = new CLIOptionDefinitions();
		if ( provider != null && provider instanceof ICLIOptionDefinitionProvider ) {
			((ICLIOptionDefinitionProvider)provider).addCLIOptionDefinitions(defs, context);
		}
		return defs;
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
	 * Get the configured {@link IContextGenerator} instance
	 * @return
	 */
	protected IContextGenerator getContextGenerator() {
		return appContext.getBean(IContextGenerator.class);
	}
	
	protected AbstractProcessRunner getProcessRunner() {
		return appContext.getBean(AbstractProcessRunner.class);
	}

	public void checkForUnknownCLIOptions(Context context, CLIOptionDefinitions globalCLIOptionDefinitions) {
		CLIOptionDefinitions optionDefinitions = getCLIOptionDefinitions(context);
		for ( String key : context.keySet() ) {
			if ( !globalCLIOptionDefinitions.containsCLIOptionDefinitionName(key) && !optionDefinitions.containsCLIOptionDefinitionName(key) ) {
				LOG.warn("[process] Ignoring unknown CLI option "+key);
			}
		}
		
	}
}
