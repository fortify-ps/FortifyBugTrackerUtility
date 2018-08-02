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

import java.io.File;
import java.util.LinkedHashSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.fortify.processrunner.cli.CLIOptionDefinition;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.HelpPrinter;

/**
 * <p>
 * This class is the main entry point for running arbitrary processes.
 * Based on the '-configFile' command line parameter, this class will 
 * instantiate and run a {@link RunProcessRunnerFromSpringConfig} 
 * instance, which loads the process-related definitions from a Spring 
 * configuration file. 
 * </p>
 * 
 * <p>Other responsibilities for this class include the following:</p>
 * <ul>
 * <li>Parse any process-specific command line parameters, to be provided
 *     to {@link RunProcessRunnerFromSpringConfig}</li>
 * <li>Initialize logging based on the '-logFile' and '-logLevel' command
 *     line parameters</li>
 * <li>Update command line parameter values based on default values
 *     provided through {@link RunProcessRunnerFromSpringConfig}
 *     configuration</li>
 * <li>Print help information if the '-help' option is specified, taking 
 *     into account the process definition loaded through 
 *     {@link RunProcessRunnerFromSpringConfig}</li>
 * </ul>
 * 
 * @author Ruud Senden
 */
public class RunProcessRunnerFromCLI {
	static {
		// We need to do this first, before initializing any of the other (static) fields,
		// to make sure the correct log manager is used
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
	}
	private static final Log LOG = LogFactory.getLog(RunProcessRunnerFromCLI.class);
	
	private static final CLIOptionDefinition CLI_HELP = new CLIOptionDefinition("global", "help", "Show help information", false).isFlag(true);
	private static final CLIOptionDefinition CLI_CONFIG_FILE = new CLIOptionDefinition("global", "configFile", "Configuration file to use", true);
	private static final CLIOptionDefinition CLI_LOG_FILE = new CLIOptionDefinition("global", "logFile", "Log file; only used if logLevel is specified", true).defaultValue(getDefaultLogFileName()).dependsOnOptions("logLevel");
	private static final CLIOptionDefinition CLI_LOG_LEVEL = new CLIOptionDefinition("global", "logLevel", "Log level", false)
			.allowedValue("TRACE", "Detailed log information; may result in large log files containing sensitive information")
			.allowedValue("DEBUG", "Debug information; may result in large log files containing sensitive information")
			.allowedValue("INFO", "Log informational messages")
			.allowedValue("WARN", "Log only warning, error or fatal messages")
			.allowedValue("ERROR", "Log only error or fatal messages")
			.allowedValue("FATAL", "Log only fatal messages");

	/**
	 * Main method for running a process. This method performs the following actions:
	 * <ul>
	 *  <li>Initial parse of CLI options to identify configuration file and logging options</li>
	 *  <li>Instantiate {@link RunProcessRunnerFromSpringConfig} with the provided configuration file</li>
	 *  <li>Re-parse CLI options based on {@link CLIOptionDefinitions} provided by {@link RunProcessRunnerFromSpringConfig}</li>
	 *  <li>Log a warning for any unknown command line options</li>
	 *  <li>Print help information if the '-help' option was specified, or if no configuration file was specified</li>
	 *  <li>Invoke {@link RunProcessRunnerFromSpringConfig} to actually run the process</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public final void run(String[] args) {
		try {
			CLIOptionDefinitions cliOptionDefinitions = getCLIOptionDefinitions(null);
			ContextWithUnknownCLIOptionsList cliContext = parseCLIOptionsAndUpdateLogger(args, cliOptionDefinitions);
			RunProcessRunnerFromSpringConfig springRunner = getSpringRunner(cliContext);
			if ( springRunner != null ) {
				// Parse command line again with additional CLIOptionDefinitions from springRunner
				cliOptionDefinitions = getCLIOptionDefinitions(springRunner);
				cliContext = parseCLIOptionsAndUpdateLogger(args, cliOptionDefinitions);
			}
			if ( CollectionUtils.isNotEmpty(cliContext.getUnknownCLIOptions()) ) {
				cliContext.getUnknownCLIOptions().forEach(unknownOption -> LOG.warn("[process] Ignoring unknown command line option "+unknownOption));
			}
			if ( cliContext.containsKey("help") || springRunner==null ) {
				printUsage(cliOptionDefinitions, cliContext, 0);
			}
			springRunner.run(cliOptionDefinitions, cliContext);
		} catch (RuntimeException e) {
			LOG.error("[Process] Error processing", e);
			System.exit(1);
		}
	}
	
	/**
	 * Get a {@link CLIOptionDefinitions} instances that combines both our own global {@link CLIOptionDefinition}
	 * instances, and any {@link CLIOptionDefinition} instances provided by the given {@link RunProcessRunnerFromSpringConfig}
	 * instance (if not null).
	 * 
	 * @param springRunner, may be null
	 * @return
	 */
	private CLIOptionDefinitions getCLIOptionDefinitions(RunProcessRunnerFromSpringConfig springRunner) {
		CLIOptionDefinitions result = new CLIOptionDefinitions();
		result.add(CLI_HELP, CLI_LOG_LEVEL, CLI_LOG_FILE);
		if ( springRunner != null ) { springRunner.addCLIOptionDefinitions(result); }
		result.add(CLI_CONFIG_FILE);
		return result;
	}

	/**
	 * Parse the given args array based on the given {@link CLIOptionDefinitions} into a {@link Context}
	 * object, update this {@link Context} with default values provided by {@link CLIOptionDefinitions},
	 * and update the logging configuration.
	 * @param args Command line arguments to be parsed
	 * @param cliOptionDefinitions Describes all available command line options
	 * @return
	 */
	private ContextWithUnknownCLIOptionsList parseCLIOptionsAndUpdateLogger(String[] args, CLIOptionDefinitions cliOptionDefinitions) {
		ContextWithUnknownCLIOptionsList result = parseContextFromCLI(cliOptionDefinitions, args);
		addCLIOptionDefaultValuesToContext(cliOptionDefinitions, result);
		updateLogConfig(result);
		return result;
	}

	/**
	 * Create a {@link RunProcessRunnerFromSpringConfig} instance based on the configuration
	 * file specified on the command line.
	 * @param cliContext
	 * @return
	 */
	private RunProcessRunnerFromSpringConfig getSpringRunner(Context cliContext) {
		String configFileName = CLI_CONFIG_FILE.getValueFromContext(cliContext);
		return configFileName==null ? null : new RunProcessRunnerFromSpringConfig(CLI_CONFIG_FILE.getValue(cliContext));
	}

	/**
	 * This method iterates over the given args array, and matches each argument against the given
	 * {@link CLIOptionDefinitions}. If an argument matches with a {@link CLIOptionDefinition},
	 * the argument value is added to the result {@link ContextWithUnknownCLIOptionsList} (or in case
	 * of a flag option, "true" is added to the result). Any unknown options will be added to the
	 * unknown options set.  
	 * 
	 * @param cliOptionDefinitions
	 * @param args
	 * @return
	 */
	private ContextWithUnknownCLIOptionsList parseContextFromCLI(CLIOptionDefinitions cliOptionDefinitions, String[] args) {
		ContextWithUnknownCLIOptionsList result = new ContextWithUnknownCLIOptionsList();
		for ( int i = 0 ; i < args.length ; i++ ) {
			String optionName = StringUtils.stripStart(args[i], "-");
			if ( cliOptionDefinitions.containsCLIOptionDefinitionName(optionName) ) {
				if ( cliOptionDefinitions.getCLIOptionDefinitionByName(optionName).isFlag() ) {
					result.put(optionName, "true");
				} else {
					result.put(optionName, args[++i]);
				}
			} else {
				result.addUnknowCLIOption(optionName);
				// Skip next argument if it looks like a value for the unknown option
				if ( args.length > i+1 && !args[i+1].startsWith("-") ) {i++;}
			}
		}
		return result;
	}
	
	/**
	 * Add the default values to the {@link Context} for any CLI options that do not yet have a value (i.e.
	 * not specified on the command line). This is necessary as we cannot access the default value from
	 * {@link CLIOptionDefinition} in the following cases:
	 * <ul>
	 * 	<li>The default value was updated on a copy of {@link CLIOptionDefinition}, whereas the application
	 *      code is accessing the original {@link CLIOptionDefinition} instance.</li>
	 *  <li>The value is accessed from the {@link Context} directly, for example when accessing CLI
	 *      options through Spring expressions.</li>
	 * </ul> 
	 *
	 * @param cliOptionDefinitions
	 * @param context
	 */
	protected final void addCLIOptionDefaultValuesToContext(CLIOptionDefinitions cliOptionDefinitions, Context context) {
		for ( CLIOptionDefinition cliOptionDefinition : cliOptionDefinitions.getCLIOptionDefinitions() ) {
			String name = cliOptionDefinition.getName();
			if ( !context.hasValueForKey(name) ) {
				String defaultValue = cliOptionDefinition.getDefaultValue();
				if ( StringUtils.isNotBlank(defaultValue) ) {
					context.put(name, defaultValue);
				}
			}
		}
	}

	/**
	 * Update the log configuration based on the current {@link Context}
	 * 
	 * @param context
	 */
	protected final void updateLogConfig(Context context) {
		String logLevel = CLI_LOG_LEVEL.getValue(context);
		if (logLevel != null) {
			String logFile = CLI_LOG_FILE.getValue(context);
			LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		    Configuration configuration = loggerContext.getConfiguration();
		    FileAppender appender = FileAppender.newBuilder()
		    		.withName("File")
		    		.withFileName(logFile)
		    		.withLayout(PatternLayout.newBuilder().withPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN).build())
		    		.withAppend(false)
		    		.build();
		    appender.start();
		    configuration.getRootLogger().removeAppender(appender.getName()); // Remove if previously added
		    configuration.getRootLogger().addAppender(appender, Level.getLevel(logLevel), null);
		    configuration.getRootLogger().setLevel(Level.getLevel(logLevel));
		    loggerContext.updateLoggers();
		    LOG.info("[process] Logging to "+logFile+" with level "+logLevel);
		}
	}

	/**
	 * Print the usage information for this command.
	 * @param cliOptionDefinitions
	 * @param context
	 * @param returnCode
	 */
	protected final void printUsage(CLIOptionDefinitions cliOptionDefinitions, Context context, int returnCode) {
		HelpPrinter hp = new HelpPrinter();
		hp.append(0, "Usage:");
		hp.append(2, getBaseCommand() + " [options]");
		appendOptions(hp, context, cliOptionDefinitions);
		hp.printHelp();
		System.exit(returnCode);
	}

	protected final void appendOptions(HelpPrinter hp, Context context, CLIOptionDefinitions cliOptionDefinitions) {
		for ( String group : cliOptionDefinitions.getCLIOptionDefinitionGroups() ) {
			boolean hasAppendedHeader = false;
			for (CLIOptionDefinition o : cliOptionDefinitions.getCLIOptionDefinitionsByGroup(group)) {
				if ( !o.hideFromHelp() ) {
					if ( !hasAppendedHeader ) {
						hp.appendEmptyLn();
						hp.append(0, StringUtils.capitalize(group)+" options:");
						hasAppendedHeader = true;
					}
					hp.appendEmptyLn();
					hp.append(2, "-" + o.getName() + (o.isFlag()?" ":" <value> ") + (o.isRequiredAndNotIgnored(context) ? "(required)" : "(optional)"));
					hp.append(4, o.getDescription());
					hp.keyValueGroupBuilder()
						.append("Default value", o.getDefaultValueDescription())
						.append("Current value", o.getCurrentValueDescription(context))
						.append("Requires options", o.getDependsOnOptions())
						.append("Alternative options", o.getIsAlternativeForOptions())
						.append("Allowed values", o.getAllowedValues())
						.append("Allowed sources", o.getAllowedSources())
						.append(o.getExtraInfo())
						.build(4);
				}
			}
		}
	}

	/**
	 * Get the default log file name. If the current jar name that we are running 
	 * from is known, the log file will have the same name as the jar name but 
	 * with '.log' extension. If the jar name is not known, the default log file 
	 * name will be 'processrunner.log'.
	 *  
	 * @return Default log file name
	 */
	protected static final String getDefaultLogFileName() {
		String result = "processrunner.log";
		String jarName = getJarName();
		if ( jarName != null ) {
			result = StringUtils.removeEnd(jarName, ".jar") + ".log";
		}
		return result;
	}

	/**
	 * Get the base command for running this utility
	 * @return
	 */
	protected String getBaseCommand() {
		return "java -jar "+StringUtils.defaultIfBlank(getJarName(), "<jar name>");
	}

	/**
	 * Get the name of the JAR file used to invoke the utility,
	 * or null if unknown
	 * @return
	 */
	protected static final String getJarName() {
		File jar = getJarFile();
		if (jar == null || "classes".equals(jar.getName()) ) {
			return null;
		} else {
			return jar.getName();
		}
	}

	/**
	 * Get the JAR file used to invoke the utility, or null if
	 * JAR file cannot be identified
	 * @return
	 */
	protected static final File getJarFile() {
		try {
			return new File(RunProcessRunnerFromCLI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Main method for invoking the utility from the command line.
	 * @param args
	 */
	public static final void main(String[] args) {
		new RunProcessRunnerFromCLI().run(args);
	}
	
	/**
	 * This {@link Context} extension adds functionality for gathering a
	 * set of unknown CLI options that were provided on the command line.
	 *  
	 * @author Ruud Senden
	 *
	 */
	private static final class ContextWithUnknownCLIOptionsList extends Context {
		private static final long serialVersionUID = 1L;
		private final LinkedHashSet<String> unknownCLIOptions = new LinkedHashSet<>();
		
		public void addUnknowCLIOption(String name) {
			unknownCLIOptions.add(name);
		}
		
		public LinkedHashSet<String> getUnknownCLIOptions() {
			return unknownCLIOptions;
		}
	}
}
