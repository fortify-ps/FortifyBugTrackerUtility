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
 * This class allows for instantiating a
 * {@link RunProcessRunnerFromSpringConfig} instance based on command line
 * options. The following command line options are available:
 * </p>
 * <ul>
 * <li>--configFile: Spring configuration file to be used by
 * {@link RunProcessRunnerFromSpringConfig}</li>
 * <li>--logFile: Log file to write logging information</li>
 * <li>--logLevel: Log level (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
 * <ul>
 * 
 * <p>
 * Any remaining command line options are used to identify the
 * {@link AbstractProcessRunner} instance to use, and to build the initial context for
 * that {@link AbstractProcessRunner} instance.
 * </p>
 * 
 * <p>
 * When invoked with invalid arguments, or with the --help option, an message
 * with general usage information will be printed on standard out.
 * </p>
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
	
	private static final CLIOptionDefinitions CLI_OPTION_DEFINITIONS =
		new CLIOptionDefinitions().add(CLI_HELP, CLI_CONFIG_FILE, CLI_LOG_FILE, CLI_LOG_LEVEL);

	/**
	 * Main method for running a {@link AbstractProcessRunner} configuration. This will
	 * parse the command line options and then invoke {@link RunProcessRunnerFromSpringConfig}
	 * 
	 * @param args
	 */
	public final void runProcessRunner(String[] argsArray) {
		try {
			Context cliContext = parseContextFromCLI(argsArray);
			updateLogConfig(cliContext);
			RunProcessRunnerFromSpringConfig springRunner = getSpringRunner(cliContext);
			if ( cliContext.containsKey("help") || springRunner==null ) {
				printUsage(springRunner, cliContext, 0);
			}
			springRunner.checkForUnknownCLIOptions(cliContext, CLI_OPTION_DEFINITIONS);
			springRunner.run(cliContext);
		} catch (RuntimeException e) {
			LOG.error("[Process] Error processing", e);
			System.exit(1);
		}
	}

	private RunProcessRunnerFromSpringConfig getSpringRunner(Context cliContext) {
		String configFileName = CLI_CONFIG_FILE.getValueFromContext(cliContext);
		return configFileName==null ? null : new RunProcessRunnerFromSpringConfig(CLI_CONFIG_FILE.getValue(cliContext));
	}

	// TODO Make this more robust. For example, this will fail if any of the option values (like a password) starts with a '-'
	//      Problem is we need to parse the command line first to identify the configuration file and logging options,
	//      before we can get all supported CLIOptionDefinitions. Available options may even be dependent on some option
	//      settings, like -Action (see AbstractBugTrackerProcessRunner)
	private Context parseContextFromCLI(String[] args) {
		Context result = new Context();
		for ( int i = 0 ; i < args.length ; i++ ) {
			String optionName = StringUtils.stripStart(args[i], "-");
			if ( i==args.length-1 || args[i+1].startsWith("-") ) {
				result.put(optionName, "true");
			} else {
				result.put(optionName, args[++i]);
			}
		}
		return result;
	}

	/**
	 * Update the log configuration based on command line options
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
		    configuration.getRootLogger().addAppender(appender, Level.getLevel(logLevel), null);
		    configuration.getRootLogger().setLevel(Level.getLevel(logLevel));
		    loggerContext.updateLoggers();
		}
	}

	/**
	 * Print the usage information for this command.
	 * @param processRunnerName 
	 * 
	 * @param context
	 */
	protected final void printUsage(RunProcessRunnerFromSpringConfig springRunner, Context context, int returnCode) {
		HelpPrinter hp = new HelpPrinter();
		hp.append(0, "Usage:");
		hp.append(2, getBaseCommand() + " [options]");
		appendOptions(hp, context, CLI_OPTION_DEFINITIONS);
		if ( springRunner == null ) {
			hp.appendEmptyLn();
			hp.append(0, "Additional options will be shown when a valid configuration file has been specified");
		} else {
			appendOptions(hp, context, springRunner.getCLIOptionDefinitions(context));
		}
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
	 * Get the default log file name
	 * @return
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
		new RunProcessRunnerFromCLI().runProcessRunner(args);
	}
}
