package com.fortify.processrunner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextPropertyDefinition;
import com.fortify.processrunner.context.ContextPropertyDefinitions;

/**
 * <p>This class allows for instantiating a {@link RunProcessRunnerFromSpringConfig}
 * instance based on command line options. The following command line options are
 * available:</p>
 * <ul>
 *   <li>--configFile: Spring configuration file to be used by {@link RunProcessRunnerFromSpringConfig}</li>
 *   <li>--logFile: Log file to write logging information</li>
 *   <li>--logLevel: Log level (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
 * <ul>
 *    
 * <p>Any remaining command line options are used to identify the
 * {@link ProcessRunner} instance to use, and to build the initial
 * context for that {@link ProcessRunner} instance.</p>
 * 
 * <p>When invoked with invalid arguments, or with the --help option, an message with
 * general usage information will be printed on standard out.</p>
 * 
 * @author Ruud Senden
 */
public class RunProcessRunnerFromCLI {
	private static final Log LOG = LogFactory.getLog(RunProcessRunnerFromCLI.class);
	private static final String DEFAULT_CONFIG_FILE = "processRunnerConfig.xml";
	private static final String DEFAULT_LOG_FILE = "processRunner.log";
	private static final String DEFAULT_LOG_LEVEL = "info";
	
	private static final Option OPT_CONFIG_FILE = Option.builder().longOpt("configFile").hasArg().build();
	private static final Option OPT_LOG_FILE = Option.builder().longOpt("logFile").hasArg().build();
	private static final Option OPT_LOG_LEVEL = Option.builder().longOpt("logLevel").hasArg().build();
	
	private static final Options OPTIONS = new Options()
		.addOption(OPT_CONFIG_FILE).addOption(OPT_LOG_FILE).addOption(OPT_LOG_LEVEL);
	
	/**
	 * Main method for running a {@link ProcessRunner} configuration. This will 
	 * parse the command line options and then invoke {@link RunProcessRunnerFromSpringConfig}
	 * @param args
	 */
	public final void runProcessRunner(String[] argsArray) {
		CommandLine cl = parseCommandLine(argsArray);
		updateLogConfig(cl);
		
		String configFile = getConfigFileName(cl);
		RunProcessRunnerFromSpringConfig springRunner = new RunProcessRunnerFromSpringConfig(configFile);
		List<String> remainingArgs = cl.getArgList(); 
		String processRunnerName = getProcessRunnerNameFromArgs(remainingArgs);
		Context cliContext = getContextFromArgs(springRunner.getContextPropertyDefinitions(processRunnerName), remainingArgs);
		
		springRunner.run(cliContext, processRunnerName);
	}

	/**
	 * Parse the command line options using Apache Commons CLI
	 * @param argsArray
	 * @return
	 */
	protected CommandLine parseCommandLine(String[] argsArray) {
		try {
			return new DefaultParser().parse(OPTIONS, argsArray, true);
		} catch ( ParseException e ) {
			handleErrorAndExit(null, null, "ERROR: Cannot parse command line: "+e.getMessage(), 6);
			return null;
		}
	}

	/**
	 * Update the log configuration based on command line options
	 * @param cl
	 */
	protected final void updateLogConfig(CommandLine cl) {
		String logFile = cl.getOptionValue(OPT_LOG_FILE.getLongOpt(), null);
		String logLevel = cl.getOptionValue(OPT_LOG_LEVEL.getLongOpt(), null);
		if ( logFile != null || logLevel != null ) {
			logFile = logFile!=null?logFile:getDefaultLogFileName();
			logLevel = logLevel!=null?logLevel:DEFAULT_LOG_LEVEL;
			try {
				Logger.getRootLogger().removeAllAppenders();
				Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), logFile, false));
				Logger.getRootLogger().setLevel(Level.toLevel(logLevel));
			} catch ( IOException e ) {
				handleErrorAndExit(null, null, "ERROR: Cannot open log file "+logFile, 5);
			}
		}
	}

	/**
	 * Generate initial context based on command line options
	 * @param contextPropertyDefinitions
	 * @param args
	 * @return
	 */
	protected final Context getContextFromArgs(ContextPropertyDefinitions contextPropertyDefinitions, List<String> args) {
		Context context = new Context();
		while ( args.size() > 0 ) {
			String opt = args.remove(0);
			if ( !opt.startsWith("-") ) { handleErrorAndExit(null, contextPropertyDefinitions, "ERROR: Invalid option "+opt, 3); }
			if ( "--help".equals(opt) ) { printUsage(null, contextPropertyDefinitions, 0); }
			// Allow options to start with either - or --, to work around JDK bug if multiple options starting with -J are given
			if ( opt.startsWith("--") ) { opt = opt.substring(1); }
			context.put(opt.substring(1), args.remove(0));
		}
		return context;
	}
	
	/**
	 * Get the Spring configuration file name from command line options
	 * @param cl
	 * @return
	 */
	protected final String getConfigFileName(CommandLine cl) {
		return cl.getOptionValue(OPT_CONFIG_FILE.getLongOpt(), getDefaultConfigFilePathAndName());
	}
	
	/**
	 * Get the name for the {@link ProcessRunner} configuration to run.
	 * @param args
	 * @param context
	 * @return {@link ProcessRunner} name, or null if not provided via command line
	 */
	protected final String getProcessRunnerNameFromArgs(List<String> args) {
		String result = null;
		if ( args.size() > 0 && !args.get(0).startsWith("-") ) {
			result = args.remove(0);
		} 
		return result;
	}
	
	/**
	 * Handle the given error by printing the relevant information on standard out,
	 * and exit the application afterwards.
	 * @param context
	 * @param errorMessage
	 * @param errorCode
	 */
	protected final void handleErrorAndExit(RunProcessRunnerFromSpringConfig springRunner, ContextPropertyDefinitions contextProperties, String errorMessage, int errorCode) {
		LOG.error("[Process] "+errorMessage);
		printUsage(springRunner, contextProperties, errorCode);
	}
	
	/**
	 * Print the usage information for this command.
	 * @param context
	 */
	protected final void printUsage(RunProcessRunnerFromSpringConfig springRunner, ContextPropertyDefinitions contextPropertyDefinitions, int returnCode) {
		LOG.info("Usage: "+getBaseCommand()+" [--configFile <configFile>] [--logFile <logFile>] [--logLevel <logLevel>] [processorRunnerId] [--help] [options]");
		LOG.info("");
		LOG.info("  --configFile <configFile> specifies the configuration file to use. Default is ");
		LOG.info("    "+getDefaultConfigFilePathAndName());
		LOG.info("  --logFile <logFile> specifies the log file to use. Default is "+getDefaultLogFileName());
		LOG.info("  --logLevel <logLevel> specifies the log level. Can be one of trace, debug, info, warn, error, or fatal.");
		LOG.info("");
		LOG.info("    By default no logging is performed unless at least either --logFile or --logLevel is specified.");
		LOG.info("    Note that log levels debug or trace may generate big log files that contain sensitive information.");
		
		if ( springRunner != null ) {
			Collection<String> processRunners = springRunner.getEnabledProcessRunnerNames();
			if ( processRunners!=null && processRunners.size() > 0 ) {
				LOG.info("");
				LOG.info("  Available process runners: "+processRunners);
				// TODO Add back process runner descriptions
			}
		}
		if ( contextPropertyDefinitions != null && contextPropertyDefinitions.size()>0 ) {
			LOG.info("");
			LOG.info("  [options] for the current process runner:");
			for ( ContextPropertyDefinition cp : contextPropertyDefinitions.values() ) {
				LOG.info("  -"+cp.getName()+" <value> "+(cp.isRequired()&&StringUtils.isBlank(cp.getDefaultValue())?"(required)":"(optional)"));
				LOG.info("    "+cp.getDescription());
				if ( StringUtils.isNotBlank(cp.getDefaultValue()) ) {
					LOG.info("    Default value: "+cp.getDefaultValue());
				}
				LOG.info("");
			}
		} else {
			LOG.info("\n  Available [options] will be shown when a valid process runner has been specified.");
		}
		System.exit(returnCode);
	}
	
	protected String getDefaultConfigFileName() {
		return DEFAULT_CONFIG_FILE;
	}
	
	protected String getDefaultConfigFilePathAndName() {
		File jar = getJarFile();
		if ( jar == null ) {
			return getDefaultConfigFileName();
		} else {
			return jar.getParentFile().getPath()+File.separator+getDefaultConfigFileName();
		}
	}
	
	protected String getDefaultLogFileName() {
		return DEFAULT_LOG_FILE;
	}
	
	protected String getBaseCommand() {
		return "java -jar "+getJarName();
	}
	
	protected String getJarName() {
		File jar = getJarFile();
		if ( jar == null ) {
			return "<jar name>";
		} else {
			return jar.getName();
		}
	}
	
	protected File getJarFile() {
		try {
			return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch ( Exception e ) {
			return null;
		}
	}
}
