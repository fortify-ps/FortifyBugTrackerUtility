package com.fortify.processrunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.util.spring.SpringContextUtil;

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
 */
public class RunProcessRunner {
	private static final Log LOG = LogFactory.getLog(RunProcessRunner.class);
	private static final String DEFAULT_CONFIG_FILE = "processRunnerConfig.xml";
	private static final String DEFAULT_BEAN_NAME = "defaultProcessRunner";
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
		GenericApplicationContext appContext = SpringContextUtil.loadApplicationContextFromFiles(true, configFile);
		
		List<String> remainingArgs = cl.getArgList(); 
		String processRunnerBeanName = getProcessRunnerBeanName(remainingArgs, appContext);
		LOG.info("Using process runner "+processRunnerBeanName);
		try {
			ProcessRunner runner = appContext.getBean(processRunnerBeanName, ProcessRunner.class);
			updateAndCheckContext(runner, remainingArgs);
			runner.run();
		} catch (Throwable t) {
			LOG.fatal("Error during process run", t);
		} finally {
			LOG.info("Processing complete for " + processRunnerBeanName);
			appContext.close();
		}
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

	protected final void updateAndCheckContext(ProcessRunner runner, List<String> args) {
		Context context = runner.getContext();
		Collection<ContextProperty> contextProperties = getContextProperties(runner, context);
		updateContextFromArgs(context, contextProperties, args);
		checkContext(context, contextProperties);
		context.refresh();
	}

	protected final void updateContextFromArgs(Context context, Collection<ContextProperty> contextProperties, List<String> args) {
		while ( args.size() > 0 ) {
			String opt = args.remove(0);
			if ( !opt.startsWith("-") ) { handleErrorAndExit(null, contextProperties, "ERROR: Invalid option "+opt, 3); }
			if ( "--help".equals(opt) ) { printUsage(null, contextProperties, 0); }
			context.put(opt.substring(1), args.remove(0));
		}
	}
	
	protected final void checkContext(Context context, Collection<ContextProperty> contextProperties) {
		for ( ContextProperty contextProperty : contextProperties ) {
			if ( contextProperty.isRequired() && !context.containsKey(contextProperty.getName()) ) {
				handleErrorAndExit(null, contextProperties, "ERROR: Required option -"+contextProperty.getName()+" not set", 4);
			}
		}
	}

	protected final Collection<ContextProperty> getContextProperties(ProcessRunner runner, Context context) {
		Set<ContextProperty> result = new LinkedHashSet<ContextProperty>();
		addContextPropertiesFromProcessRunner(runner, result, context);
		addContextPropertiesFromContext(result, context);
		return result;
	}

	protected final void addContextPropertiesFromProcessRunner(ProcessRunner runner, Collection<ContextProperty> contextProperties, Context context) {
		runner.getProcessor().addContextProperties(contextProperties, context);
	}
	
	protected final void addContextPropertiesFromContext(Collection<ContextProperty> contextProperties, Context context) {
		context.addContextProperties(contextProperties);
	}

	protected final String getConfigFileName(CommandLine cl) {
		String configFile = cl.getOptionValue(OPT_CONFIG_FILE.getLongOpt(), getDefaultConfigFilePathAndName());
		checkConfigFile(configFile);
		LOG.info("Using Spring configuration file "+configFile);
		return configFile;
	}
	
	/**
	 * Check whether the given configuration file exists and is readable. 
	 * @param configFile
	 */
	protected final void checkConfigFile(String configFile) {
		Resource resource = new FileSystemResource(configFile);
		if ( !resource.exists() ) {
			handleErrorAndExit(null, null, "ERROR: Configuration file "+configFile+" does not exist", 1);
		}
		if ( !resource.isReadable() ) {
			handleErrorAndExit(null, null, "ERROR: Configuration file "+configFile+" is not readable", 2);
		}
	}
	
	/**
	 * Get the bean name for the {@link ProcessRunner} configuration to run.
	 * @param args
	 * @param context
	 * @return
	 */
	protected final String getProcessRunnerBeanName(List<String> args, ApplicationContext context) {
		Set<String> processorBeanNames = new LinkedHashSet<String>(Arrays.asList(context.getBeanNamesForType(ProcessRunner.class)));
		if ( LOG.isDebugEnabled() ) { LOG.debug("Available process runners: "+processorBeanNames); }
		String errorMessage = null;
		if ( args.size() == 0 || args.get(0).startsWith("-") ) {
			if ( processorBeanNames.contains(DEFAULT_BEAN_NAME) ) {
				return DEFAULT_BEAN_NAME;
			} else if ( processorBeanNames.size()==1 ) {
				return processorBeanNames.iterator().next();
			} else {
				errorMessage = "ERROR: No process runner id specified";
			}
		} else if ( !processorBeanNames.contains(args.get(0)) ) {
			errorMessage = "ERROR: process runner id "+args.get(0)+" is not valid";
		} else {
			return args.remove(0);
		}
		handleErrorAndExit(context, null, errorMessage, 3);
		return null;
	}
	
	/**
	 * Handle the given error by printing the relevant information on standard out,
	 * and exit the application afterwards.
	 * @param context
	 * @param errorMessage
	 * @param errorCode
	 */
	protected final void handleErrorAndExit(ApplicationContext context, Collection<ContextProperty> contextProperties, String errorMessage, int errorCode) {
		LOG.error(errorMessage);
		printUsage(context, contextProperties, errorCode);
	}
	
	/**
	 * Print the usage information for this command.
	 * @param context
	 */
	protected final void printUsage(ApplicationContext appContext, Collection<ContextProperty> contextProperties, int returnCode) {
		LOG.info("Usage: "+getBaseCommand()+" [--configFile <configFile>] [--logFile <logFile>] [--logLevel <logLevel>] [processorRunnerId] [--help] [options]");
		LOG.info("");
		LOG.info("  --configFile <configFile> specifies the configuration file to use. Default is ");
		LOG.info("    "+getDefaultConfigFilePathAndName());
		LOG.info("  --logFile <logFile> specifies the log file to use. Default is "+getDefaultLogFileName());
		LOG.info("  --logLevel <logLevel> specifies the log level. Can be one of trace, debug, info, warn, error, or fatal.");
		LOG.info("");
		LOG.info("    By default no logging is performed unless at least either --logFile or --logLevel is specified.");
		LOG.info("    Note that log levels debug or trace may generate big log files that contain sensitive information.");
		
		if ( appContext != null ) {
			Map<String, ProcessRunner> processRunnersMap = appContext.getBeansOfType(ProcessRunner.class);
			if ( processRunnersMap!=null && processRunnersMap.size() > 0 ) {
				LOG.info("");
				LOG.info("  Available process runner id's:");
				for ( Map.Entry<String, ProcessRunner> entry : processRunnersMap.entrySet() ) {
					LOG.info("    "+entry.getKey());
					if ( StringUtils.isNotBlank(entry.getValue().getDescription()) ) {
						LOG.info("      "+entry.getValue().getDescription()+"\n");
					}
				}
			}
		}
		if ( contextProperties != null && contextProperties.size()>0 ) {
			LOG.info("");
			LOG.info("  [options] for the current process runner:");
			for ( ContextProperty cp : contextProperties ) {
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
