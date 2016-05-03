package com.fortify.processrunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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
import org.springframework.util.StringUtils;

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
public class Main {
	private static final Log LOG = LogFactory.getLog(Main.class);
	private static final String DEFAULT_CONFIG_FILE = "processRunnerConfig.xml";
	private static final String DEFAULT_BEAN_NAME = "defaultProcessorRunner";
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
	public static void main(String[] argsArray) throws Exception {
		CommandLine cl = new DefaultParser().parse(OPTIONS, argsArray, true);
		updateLogConfig(cl);
		
		String configFile = getConfigFileName(cl);
		GenericApplicationContext context = SpringContextUtil.loadApplicationContextFromFiles(true, configFile);
		
		List<String> remainingArgs = cl.getArgList(); 
		String processRunnerBeanName = getProcessRunnerBeanName(remainingArgs, context);
		LOG.info("Using process runner "+processRunnerBeanName);
		try {
			ProcessRunner runner = context.getBean(processRunnerBeanName, ProcessRunner.class);
			updateContext(runner, remainingArgs);
			checkContext(runner);
			runner.run();
		} catch (Throwable t) {
			System.out.println("Error during process run:\n");
			t.printStackTrace();
			LOG.fatal("Error during process run", t);
		} finally {
			context.close();
		}
	}

	private static void updateLogConfig(CommandLine cl) {
		String logFile = cl.getOptionValue(OPT_LOG_FILE.getLongOpt(), null);
		String logLevel = cl.getOptionValue(OPT_LOG_LEVEL.getLongOpt(), null);
		if ( logFile != null || logLevel != null ) {
			logFile = logFile!=null?logFile:DEFAULT_LOG_FILE;
			logLevel = logLevel!=null?logLevel:DEFAULT_LOG_LEVEL;
			try {
				Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), logFile, false));
				Logger.getRootLogger().setLevel(Level.toLevel(logLevel));
			} catch ( IOException e ) {
				handleErrorAndExit(null, null, "ERROR: Cannot open log file "+logFile, 5);
			}
		}
		
	}

	private static final void updateContext(ProcessRunner runner, List<String> args) {
		Context context = runner.getContext();
		List<ContextProperty> contextProperties = runner.getProcessor().getContextProperties(context);
		while ( args.size() > 0 ) {
			String opt = args.remove(0);
			if ( !opt.startsWith("-") ) { handleErrorAndExit(null, contextProperties, "ERROR: Invalid option "+opt, 3); }
			if ( "--help".equals(opt) ) { printUsage(null, contextProperties, 0); }
			context.put(opt.substring(1), args.remove(0));
			// TODO Check that all required context properties have been set
		}
	}
	
	private static final void checkContext(ProcessRunner runner) {
		Context ctx = runner.getContext();
		List<ContextProperty> contextProperties = runner.getProcessor().getContextProperties(ctx);
		for ( ContextProperty contextProperty : contextProperties ) {
			if ( contextProperty.isRequired() && !ctx.containsKey(contextProperty.getName()) ) {
				handleErrorAndExit(null, contextProperties, "ERROR: Required option -"+contextProperty.getName()+" not set", 4);
			}
		}
	}

	private static final String getConfigFileName(CommandLine cl) {
		String configFile = cl.getOptionValue(OPT_CONFIG_FILE.getLongOpt(), DEFAULT_CONFIG_FILE);
		checkConfigFile(configFile);
		LOG.info("Using Spring configuration file "+configFile);
		return configFile;
	}
	
	/**
	 * Check whether the given configuration file exists and is readable. 
	 * @param configFile
	 */
	private static void checkConfigFile(String configFile) {
		Resource resource = new FileSystemResource(configFile);
		if ( !resource.exists() ) {
			handleErrorAndExit(null, null, "Error: Configuration file "+configFile+" does not exist", 1);
		}
		if ( !resource.isReadable() ) {
			handleErrorAndExit(null, null, "Error: Configuration file "+configFile+" is not readable", 2);
		}
	}
	
	/**
	 * Get the bean name for the {@link ProcessRunner} configuration to run.
	 * @param args
	 * @param context
	 * @return
	 */
	private static String getProcessRunnerBeanName(List<String> args, ApplicationContext context) {
		Set<String> processorBeanNames = new HashSet<String>(Arrays.asList(context.getBeanNamesForType(ProcessRunner.class)));
		if ( LOG.isDebugEnabled() ) { LOG.info("Available process runners: "+processorBeanNames); }
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
	private static final void handleErrorAndExit(ApplicationContext context, List<ContextProperty> contextProperties, String errorMessage, int errorCode) {
		LOG.error(errorMessage);
		System.out.println(errorMessage);
		printUsage(context, contextProperties, errorCode);
	}
	
	/**
	 * Print the usage information for this command.
	 * @param context
	 */
	private static final void printUsage(ApplicationContext context, List<ContextProperty> contextProperties, int returnCode) {
		System.out.println("Usage: java -jar <jarName> [--config <configFile>] [--logFile <logFile>] [--logLevel <logLevel>] [processorRunnerId] [--help] [options]");
		System.out.println("\n\t--configFile <configFile> specifies the configuration file to use. Default is "+DEFAULT_CONFIG_FILE);
		System.out.println("\t--logFile <logFile> specifies the log file to use. Default is "+DEFAULT_LOG_FILE);
		System.out.println("\t--logLevel <logLevel> specifies the log level. Can be one of trace, debug, info, warn, error, or fatal.");
		System.out.println("\t\tNote that levels debug or trace may generate big log files that contain sensitive information.");
		System.out.println("\n\tBy default no logging is performed unless at least either --logFile or --logLevel is specified.");
		
		if ( context != null ) {
			String[] availableProcessorRunnerNames = context.getBeanNamesForType(ProcessRunner.class);
			if ( availableProcessorRunnerNames!=null && availableProcessorRunnerNames.length > 0 ) {
				System.out.println("Available process runner id's:");
				System.out.println("\t"+StringUtils.arrayToDelimitedString(availableProcessorRunnerNames, "\n\t"));
			}
		} else {
			System.out.println("\n\tAvailable [processRunnerId] options will be shown when a valid configuration has been specified.");
		}
		if ( contextProperties != null && contextProperties.size()>0 ) {
			System.out.println("\n\t[options] for this process runner:");
			for ( ContextProperty cp : contextProperties ) {
				System.out.println("\t-"+cp.getName()+" <value> "+(cp.isRequired()?"(required)":"(optional)"));
				System.out.println("\t\t"+cp.getDescription()+"\n");
			}
		} else {
			System.out.println("\n\tAvailable [options] will be shown when a valid process runner has been specified.");
		}
		System.exit(returnCode);
	}
}
