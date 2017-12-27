/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;

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
 * {@link ProcessRunner} instance to use, and to build the initial context for
 * that {@link ProcessRunner} instance.
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
	private static final String DEFAULT_LOG_FILE = "processRunner.log";
	private static final String DEFAULT_LOG_LEVEL = "info";

	private static final Option OPT_CONFIG_FILE = Option.builder().longOpt("configFile").hasArg().build();
	private static final Option OPT_LOG_FILE = Option.builder().longOpt("logFile").hasArg().build();
	private static final Option OPT_LOG_LEVEL = Option.builder().longOpt("logLevel").hasArg().build();

	private static final Options OPTIONS = new Options().addOption(OPT_CONFIG_FILE).addOption(OPT_LOG_FILE)
			.addOption(OPT_LOG_LEVEL);

	/**
	 * Main method for running a {@link ProcessRunner} configuration. This will
	 * parse the command line options and then invoke {@link RunProcessRunnerFromSpringConfig}
	 * 
	 * @param args
	 */
	public final void runProcessRunner(String[] argsArray) {
		CommandLine cl = parseCommandLine(argsArray);
		updateLogConfig(cl);

		String configFile = getConfigFileName(cl);
		if ( configFile == null ) {
			handleErrorAndExit(null, null, null, "No configuration file specified", 1);
		}
		RunProcessRunnerFromSpringConfig springRunner = new RunProcessRunnerFromSpringConfig(configFile);
		List<String> remainingArgs = cl.getArgList();
		String processRunnerName = getProcessRunnerNameFromArgs(remainingArgs);
		Context cliContext = getContextFromArgs(springRunner, processRunnerName, remainingArgs);

		springRunner.run(cliContext, processRunnerName);
	}

	/**
	 * Parse the command line options using Apache Commons CLI
	 * 
	 * @param argsArray
	 * @return
	 */
	protected CommandLine parseCommandLine(String[] argsArray) {
		try {
			return new DefaultParser().parse(OPTIONS, argsArray, true);
		} catch (ParseException e) {
			handleErrorAndExit(null, null, null, "ERROR: Cannot parse command line: " + e.getMessage(), 6);
			return null;
		}
	}

	/**
	 * Update the log configuration based on command line options
	 * 
	 * @param cl
	 */
	protected final void updateLogConfig(CommandLine cl) {
		String logFile = cl.getOptionValue(OPT_LOG_FILE.getLongOpt(), null);
		String logLevel = cl.getOptionValue(OPT_LOG_LEVEL.getLongOpt(), null);
		if (logFile != null || logLevel != null) {
			logFile = logFile != null ? logFile : getDefaultLogFileName();
			logLevel = logLevel != null ? logLevel : DEFAULT_LOG_LEVEL;
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
		    Configuration configuration = context.getConfiguration();
		    FileAppender appender = FileAppender.newBuilder()
		    		.withName("File")
		    		.withFileName(logFile)
		    		.withLayout(PatternLayout.newBuilder().withPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN).build())
		    		.withAppend(false)
		    		.build();
		    appender.start();
		    configuration.getRootLogger().addAppender(appender, Level.getLevel(logLevel), null);
		    configuration.getRootLogger().setLevel(Level.getLevel(logLevel));
		    context.updateLoggers();
		}
	}

	/**
	 * Generate initial context based on command line options
	 * @param args 
	 * @param processRunnerName 
	 * @param springRunner 
	 * 
	 * @param contextPropertyDefinitions
	 * @param args
	 * @return
	 */
	protected final Context getContextFromArgs(RunProcessRunnerFromSpringConfig springRunner, String processRunnerName, List<String> args) {
		ContextPropertyDefinitions contextPropertyDefinitions = springRunner.getContextPropertyDefinitions(processRunnerName);
		Context context = new Context();
		while (args.size() > 0) {
			String opt = args.remove(0);
			if (!opt.startsWith("-")) {
				handleErrorAndExit(springRunner, processRunnerName, contextPropertyDefinitions, "ERROR: Invalid option " + opt, 3);
			}
			if ("--help".equals(opt)) {
				printUsage(springRunner, processRunnerName, contextPropertyDefinitions, 0);
			}
			// Allow options to start with either - or --, to work around JDK
			// bug if multiple options starting with -J are given
			if (opt.startsWith("--")) {
				opt = opt.substring(1);
			}
			context.put(opt.substring(1), args.remove(0));
		}
		return context;
	}

	/**
	 * Get the Spring configuration file name from command line options
	 * 
	 * @param cl
	 * @return
	 */
	protected final String getConfigFileName(CommandLine cl) {
		return cl.getOptionValue(OPT_CONFIG_FILE.getLongOpt());
	}

	/**
	 * Get the name for the {@link ProcessRunner} configuration to run.
	 * 
	 * @param args
	 * @param context
	 * @return {@link ProcessRunner} name, or null if not provided via command
	 *         line
	 */
	protected final String getProcessRunnerNameFromArgs(List<String> args) {
		String result = null;
		if (args.size() > 0 && !args.get(0).startsWith("-")) {
			result = args.remove(0);
		}
		return result;
	}

	/**
	 * Handle the given error by printing the relevant information on standard
	 * out, and exit the application afterwards.
	 * @param processRunnerName 
	 * 
	 * @param context
	 * @param errorMessage
	 * @param errorCode
	 */
	protected final void handleErrorAndExit(RunProcessRunnerFromSpringConfig springRunner, String processRunnerName, ContextPropertyDefinitions contextProperties, String errorMessage, int errorCode) {
		LOG.error("[Process] " + errorMessage);
		printUsage(springRunner, processRunnerName, contextProperties, errorCode);
	}

	/**
	 * Print the usage information for this command.
	 * @param processRunnerName 
	 * 
	 * @param context
	 */
	protected final void printUsage(RunProcessRunnerFromSpringConfig springRunner, String processRunnerName, ContextPropertyDefinitions contextPropertyDefinitions, int returnCode) {
		LOG.info("Usage: " + getBaseCommand()
				+ " --configFile <configFile> [--logFile <logFile>] [--logLevel <logLevel>] [action] [--help] [options]");
		LOG.info("");
		LOG.info("  --configFile <configFile> specifies the configuration file to use.");
		LOG.info("  --logFile <logFile> specifies the log file to use. Default is " + getDefaultLogFileName());
		LOG.info(
				"  --logLevel <logLevel> specifies the log level. Can be one of trace, debug, info, warn, error, or fatal.");
		LOG.info("");
		LOG.info("By default no logging is performed unless at least either --logFile or --logLevel is specified.");
		LOG.info("Note that log levels debug or trace may generate big log files that contain sensitive information.\n");

		
		if (springRunner == null ) {
			LOG.info("Available actions will be shown when a valid configuration file has been specified.");
		} else {
			Map<String, ProcessRunner> processRunners = springRunner.getEnabledProcessRunners();
			LOG.info("Available actions: ");
			for ( Map.Entry<String, ProcessRunner> processRunnerEntry : processRunners.entrySet() ) {
				String id = processRunnerEntry.getKey();
				ProcessRunner processRunner = processRunnerEntry.getValue();
				LOG.info("  "+id+(processRunner.isDefault()?" (default)":""));
				LOG.info("  "+processRunner.getDescription());
				LOG.info("");
			}
			
			if (contextPropertyDefinitions == null || contextPropertyDefinitions.size() == 0) {
				LOG.info("Available options will be shown when a valid action has been specified.");
			} else {
				LOG.info("Available options for the current action ("+springRunner.getProcessRunnerNameOrDefault(processRunnerName)+"):");
				for (ContextPropertyDefinition cp : contextPropertyDefinitions.values()) {
					LOG.info("  -" + cp.getName() + " <value> " + (cp.isRequired() ? "(required)" : "(optional)"));
					LOG.info("   " + cp.getDescription());
					if (StringUtils.isNotBlank(cp.getDefaultValueDescription())) {
						LOG.info("   Default value: " + cp.getDefaultValueDescription());
					}
					LOG.info("");
				}
			}
		} 
		System.exit(returnCode);
	}

	/**
	 * Get the default log file name
	 * @return
	 */
	protected String getDefaultLogFileName() {
		return DEFAULT_LOG_FILE;
	}

	/**
	 * Get the base command for running this utility
	 * @return
	 */
	protected String getBaseCommand() {
		return "java -jar " + getJarName();
	}

	/**
	 * Get the name of the JAR file used to invoke the utility,
	 * or "&lt;jar name&gt;" if unknown
	 * @return
	 */
	protected String getJarName() {
		File jar = getJarFile();
		if (jar == null) {
			return "<jar name>";
		} else {
			return jar.getName();
		}
	}

	/**
	 * Get the JAR file used to invoke the utility, or null if
	 * JAR file cannot be identified
	 * @return
	 */
	protected File getJarFile() {
		try {
			return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
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
