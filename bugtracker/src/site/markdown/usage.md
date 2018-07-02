# Usage
In order to run the utility, you will need to have a Java 8 Runtime Environment installed. After unpacking the release zip file you can get usage information by running the following command:

`java -jar FortifyBugTrackerUtility-[version].jar --help`

In order to do any useful work, the utility requires a configuration file. The utility comes bundled with multiple sample configuration files for different use cases. Currently there are configuration files to submit issues from either FoD or SSC to Jira, CSV File, ALM Octane, RSA Archer, and TFS/Visual Studio Online. 

The configuration file can be specified using the `--configFile` option. Once a valid configuration file has been specified,
one or more actions may be available. To list the available actions, you can again specify the `--help` option. For example:

`java -jar FortifyBugTrackerUtility-[version].jar --configFile FoDToJira.xml --help`

If the configuration file includes a default action, the command above will also show all available options for that default action.
If you want to list the available options for a non-default action, you can specify the action name on the command line together 
with the `--help` option. For example:

`java -jar FortifyBugTrackerUtility-[version].jar --configFile FoDToJira.xml submitVulnerabilities --help`

Note that the `--help` option must always be specified as the last command line option, after the configuration file and
action options.

Once you have identified the action that you want to run, and the corresponding options for that action, you can invoke the
action as follows. You can omit the `[action]` parameter if you want to run the default action, and you can omit the
action-specific options if the default option values are appropriate. 

`java -jar FortifyBugTrackerUtility-[version].jar --configFile [configFile] [action] [action-specific options]`

Before running any actions though, you may want to review the configuration file to verify the various selection and grouping
criteria, and the fields to be submitted to the external system. Please see the configuration-related sections in this documentation.
Note that other parts of the documentation and sample configuration files may refer to the 'action-specific options' as 'context properties'; both terms are equivalent.
