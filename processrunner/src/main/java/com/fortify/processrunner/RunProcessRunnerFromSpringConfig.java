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
package com.fortify.processrunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fortify.processrunner.cli.CLIOptionDefinition;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.IContextGenerator;
import com.fortify.util.spring.SpringContextUtil;

/**
 * This class allows for running {@link AbstractProcessRunner} instances based on Spring configuration.
 * Base on the Spring configuration, this will:
 * <ul>
 *  <li>Load the Spring configuration file</li>
 *  <li>Get an {@link AbstractProcessRunner} instance based on Spring configuration</li>
 *  <li>Generate one or more {@link Context} instances based on configured {@link IContextGenerator}</li>
 *  <li>Invoke the configured {@link AbstractProcessRunner} for each generated {@link Context}</li>
 * </ul>
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
	 * returned by our {@link #getContexts(Context)} method.
	 * @param cliOptionDefinitions
	 * @param initialContext
	 */
	public void run(CLIOptionDefinitions cliOptionDefinitions, Context initialContext) {
		AbstractProcessRunner runner = getProcessRunner();
		Collection<Context> contexts = getContexts(initialContext);
		for ( Context context : contexts ) {
			try {
				checkContext(cliOptionDefinitions, context);
				runner.run(context);
			} catch (Throwable t) {
				LOG.error("[Process] Error during process run: "+t.getLocalizedMessage(), t);
			}
		}
		runner.close(initialContext);
		LOG.info("[Process] Processing complete");
	}

	/**
	 * Add the {@link CLIOptionDefinition} for the configured {@link AbstractProcessRunner} 
	 * and {@link IContextGenerator} instances.
	 * @param cliOptionDefinitions
	 */
	public final void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		IContextGenerator contextGenerator = getContextGenerator();
		// Create a temporary CLIOptionDefinitions instance to differentiate between 
		// CLIOptionDefinition instances provided by the contextGenerator and provided
		// by the process runner.
		CLIOptionDefinitions newDefs = new CLIOptionDefinitions()
				.addAll("contextGenerator", contextGenerator)
				.addAll(getProcessRunner());
		// Call the contextGenerator to update any CLIOptionDefinition instances that were
		// solely provided by the process runner (excluding any CLIOptionDefinition instances
		// that were provided by both the process runner and context generator)
		if ( contextGenerator!=null ) {
			contextGenerator.updateProcessRunnerCLIOptionDefinitions(newDefs.getCLIOptionDefinitionsExludingSource("contextGenerator"));
		}
		// Add the updated CLIOptionDefinition instances to the given cliOptionDefinitions
		cliOptionDefinitions.addAll(newDefs);
		// Update the default values for all cliOptionDefinitions, based on the cliOptionsDefaultValues bean
		updateCLIOptionDefinitionsDefaultValues(cliOptionDefinitions);
		// Update all cliOptionDefinitions to indicate that default values may be configured through the cliOptionsDefaultValues bean
		cliOptionDefinitions.getCLIOptionDefinitions().forEach(o -> o.addAllowedSources(ALLOWED_SOURCE_DEFAULT_VALUES_BEAN));
	}

	/**
	 * Update default values for CLI options based on the cliOptionsDefaultValuesFromConfig bean.
	 * @param optionDefinitions
	 */
	private void updateCLIOptionDefinitionsDefaultValues(CLIOptionDefinitions optionDefinitions) {
		Context cliOptionsDefaultValuesFromConfig = getCLIOptionsDefaultValuesFromConfig();
		if ( MapUtils.isNotEmpty(cliOptionsDefaultValuesFromConfig) ) {
			for ( Map.Entry<String, Object> entry : cliOptionsDefaultValuesFromConfig.entrySet() ) {
				CLIOptionDefinition def = optionDefinitions.getCLIOptionDefinitionByName(entry.getKey());
				if ( def != null ) {
					def.defaultValue((String)entry.getValue());
				}
			}
		}
		
	}

	/**
	 * <p>Get the {@link Context} instances to use to run the configured {@link AbstractProcessRunner} 
	 * instance based on the configured {@link IContextGenerator}. If an 
	 * {@link IContextGenerator} has been configured, it will be invoked to generate
	 * the {@link Context} instances. If not, this method will simply return the single initialContext 
	 * instance.</p>
	 * @param initialContext
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
	private Context getCLIOptionsDefaultValuesFromConfig() {
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
	protected final void checkContext(CLIOptionDefinitions cliOptionDefinitions, Context context) {
		for ( CLIOptionDefinition cLIOptionDefinition : cliOptionDefinitions.getCLIOptionDefinitions() ) {
			String name = cLIOptionDefinition.getName();
			if ( cLIOptionDefinition.isRequiredAndNotIgnored(context) && !context.hasValueForKey(name) ) {
				throw new IllegalStateException("ERROR: Required option -"+name+" not set");
			}
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
	 * Get the configured {@link IContextGenerator} instance
	 * @return
	 */
	protected IContextGenerator getContextGenerator() {
		return appContext.getBean(IContextGenerator.class);
	}
	
	/**
	 * Get the configured {@link AbstractProcessRunner} instance
	 * @return
	 */
	protected AbstractProcessRunner getProcessRunner() {
		return appContext.getBean(AbstractProcessRunner.class);
	}
}
