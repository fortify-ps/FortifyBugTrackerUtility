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
 * <p>This is the Main class used to run a {@link ProcessRunner} configuration
 * from the command line. By default it will load the available 
 * {@link ProcessRunner} configurations from the processRunnerConfig.xml file 
 * in the current directory. The file location and name can optionally be
 * overridden using the <code>--configFile</code> command line parameter.</p>
 * 
 * <p>Logging can be controlled using the --logFile and --logLevel parameters.
 * Any remaining command line arguments will identify the process to run,
 * together with process-specific parameters.</p>
 * 
 * <p>If no {@link ProcessRunner} configuration id argument is given, and the
 * configuration file only contains a single {@link ProcessRunner} configuration
 * or a {@link ProcessRunner} configuration named 'defaultPRocessRunner', then
 * this {@link ProcessRunner} configuration will be invoked. Otherwise, it is
 * required to specify a {@link ProcessRunner} configuration id on the command 
 * line to specify which {@link ProcessRunner} configuration to run.</p>
 * 
 * <p>When invoked with invalid arguments, an error message together with
 * general usage information will be printed on standard out.</p>
 * 
 * TODO Update JavaDoc
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
	 * load the Spring configuration file containing {@link ProcessRunner} 
	 * definitions, and then run the specified process runner.
	 * @param args
	 * @throws Exception
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

	protected CommandLine parseCommandLine(String[] argsArray) {
		try {
			return new DefaultParser().parse(OPTIONS, argsArray, true);
		} catch ( ParseException e ) {
			handleErrorAndExit(null, null, "ERROR: Cannot parse command line: "+e.getMessage(), 6);
			return null;
		}
	}

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
	
	protected final String getConfigFileName(CommandLine cl) {
		return cl.getOptionValue(OPT_CONFIG_FILE.getLongOpt(), getDefaultConfigFilePathAndName());
	}
	
	/**
	 * Get the bean name for the {@link ProcessRunner} configuration to run.
	 * @param args
	 * @param context
	 * @return
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
