# Usage
In order to run the utility, you will need to have a Java 8 Runtime Environment installed. After unpacking the release zip file you can get usage information by running the following command:

`java -jar FortifyBugTrackerUtility-[version].jar -help`

In order to do any useful work, the utility requires a configuration file. The utility comes bundled with multiple sample configuration files for different use cases. Currently there are configuration files to submit issues from either FoD or SSC to Jira, CSV File, ALM Octane, RSA Archer, and TFS/Visual Studio Online. 

The configuration file can be specified using the `-configFile` option. Once a valid configuration file has been specified,
you can view additional options by specifying the `-help` option again:

`java -jar FortifyBugTrackerUtility-[version].jar -configFile FoDToJira.xml -help`

The `-help` option shows available option names, a short option description, and the following information if applicable:

* Default value

    The default value for this option. This can be a hardcoded default value, a default
    value automatically calculated by the utility, or a default value as configured in
    the configuration file.
    
* Current value:

	The current value for this option. This can be the default value, or a value as currently
	specified on the command line.

* Allowed values:  

	The list of allowed values for this option; an error will be given if the provided value
	does not correspond to any of the allowed values.
	
* Allowed sources: 

    Describes where this option can be set:
    
    * CLI option: Option can be specified on the command line
    * cliOptionsDefaultValues bean: Option can be configured through the cliOptionsDefaultValues bean
      in the configuration file
    * SSCSourceApplicationVersionsConfiguration mappings: Option can be configured
      through the `SSCSourceApplicationVersionsConfiguration` bean in the configuration file
    * FoDSourceReleasesConfiguration mappings: Option can be configured
      through the `FoDSourceReleasesConfiguration` bean in the configuration file

* Used for:      

    Describes whether this option is used for submitting new vulnerabilities, updating state for
    previously submitted vulnerabilities, or both.
 

In order to actually run the utility, you will need to provide appropriate values for all required options. Please make sure to also
review the optional options though. Note that options can be specified on the command line, but most options also allow default
values to be specified in the configuration file through the [cliOptionsDefaultValues](config-cliOptionsDefaultValues.html) bean in the
configuration file. In addition, option values can be generated for individual FoD releases or SSC application versions; see
[FoDSourceVulnerabilitiesConfiguration](config-FoDSourceVulnerabilitiesConfiguration.html) and 
[SSCSourceApplicationVersionsConfiguration](config-SSCSourceApplicationVersionsConfiguration.html) respectively for more details.

Following are some examples (based on the default configuration files):

- Export all Exploitable vulnerabilities for all SSC application versions to CSV files

    `java -jar FortifyBugTrackerUtility-[version].jar -configFile SSCToFile.xml -SSCBaseUrl http://localhost:1810/ssc -SSCUserName ssc -SSCPassword Fortify123!`

- Export all Exploitable vulnerabilities for all SSC application versions in the EightBall and RabbitMq applications to CSV files:

    `java -jar FortifyBugTrackerUtility-[version].jar -configFile SSCToFile.xml -SSCBaseUrl http://localhost:1810/ssc -SSCUserName ssc -SSCPassword Fortify123! -SSCApplicationVersionNamePatterns EightBall:.*,RabbitMq:.*`

- Export all Exploitable vulnerabilities for the 'WebGoat 5.0' application version to the specified Octane workspace

    `java -jar FortifyBugTrackerUtility-[version].jar -configFile SSCToOctane.xml -SSCBaseUrl http://localhost:1810/ssc -SSCUserName ssc -SSCPassword Fortify123! -SSCApplicationVersionNamePatterns WebGoat:5\.0 -OctaneBaseUrl https://mqast001pngx.saas.hpe.com -OctaneUserName ruud.senden@hpe.com -OctanePassword [password] -OctaneSharedSpaceUid 136002 -OctaneWorkspaceId 1002`


Before running any actions, you may want to review the configuration file to verify the various selection and grouping
criteria, and the fields to be submitted to the external system. Please see the configuration-related sections in this documentation.