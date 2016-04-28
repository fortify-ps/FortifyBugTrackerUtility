package com.fortify.processrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * overridden using the <code>--config</code> command line parameter.</p>
 * 
 * <p>If no command line arguments are provided to this Main class, and the
 * configuration file only contains a single {@link ProcessRunner} configuration
 * or a {@link ProcessRunner} configuration named 'defaultPRocessRunner', then
 * this {@link ProcessRunner} configuration will be invoked. Otherwise, it is
 * required to specify a {@link ProcessRunner} configuration id on the command 
 * line to specify which {@link ProcessRunner} configuration to run.</p>
 * 
 * <p>When invoked with invalid arguments, an error message together with
 * general usage information will be printed on standard out.</p>
 */
// TODO Set up logging
public class Main {
	private static final Log LOG = LogFactory.getLog(Main.class);
	private static final String DEFAULT_CONFIG_FILE = "processRunnerConfig.xml";
	private static final String DEFAULT_BEAN_NAME = "defaultProcessorRunner";
	
	/**
	 * Main method for running a {@link ProcessRunner} configuration. This will 
	 * load the Spring configuration file containing {@link ProcessRunner} 
	 * definitions, and then run the specified process runner.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] argsArray) throws Exception {
		List<String> args = new ArrayList<String>(Arrays.asList(argsArray));
		String configFile = getConfigFileName(args);
		
		GenericApplicationContext context = SpringContextUtil.loadApplicationContextFromFiles(true, configFile);
		
		String processRunnerBeanName = getProcessRunnerBeanName(args, context);
		LOG.info("Using process runner "+processRunnerBeanName);
		try {
			ProcessRunner runner = context.getBean(processRunnerBeanName, ProcessRunner.class);
			updateContext(runner, args);
			checkContext(runner);
			runner.run();
		} catch (Throwable t) {
			LOG.fatal("Error during process run", t);
		} finally {
			context.close();
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

	private static final String getConfigFileName(List<String> args) {
		String configFile = DEFAULT_CONFIG_FILE;
		if ( args.size() > 1 && "--config".equals(args.get(0)) ) {
			args.remove(0);
			configFile = args.remove(0);
		}
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
		System.out.println("Usage: java -jar <jarName> [--config <configFile>] [processorRunnerId] [--help] [options]");
		System.out.println("\n\t--config <configFile> specifies the configuration file to use. Default is "+DEFAULT_CONFIG_FILE);
		if ( contextProperties != null && contextProperties.size()>0 ) {
			System.out.println("\n\t[options] for this process runner:");
			for ( ContextProperty cp : contextProperties ) {
				System.out.println("\t-"+cp.getName()+" <value> "+(cp.isRequired()?"(required)":"(optional)"));
				System.out.println("\t\t"+cp.getDescription()+"\n");
			}
		}
		if ( context != null ) {
			String[] availableProcessorRunnerNames = context.getBeanNamesForType(ProcessRunner.class);
			if ( availableProcessorRunnerNames!=null && availableProcessorRunnerNames.length > 0 ) {
				System.out.println("Available process runner id's:");
				System.out.println("\t"+StringUtils.arrayToDelimitedString(availableProcessorRunnerNames, "\n\t"));
			}
		}	
		System.exit(returnCode);
	}
}
