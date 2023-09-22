# SSC Preparations

The following sections describe the preparations that you will need to do on SSC in order to use FortifyBugTrackerUtility.

## Storing information about submitted target issues

This information applies to all target integrations, apart from the following:

- File export
- Submitting vulnerabilities through a native SSC bug tracker integration
 
FortifyBugTrackerUtility will need to keep track of which vulnerabilities have been submitted to which target issue, 
to avoid re-submitting vulnerabilities that have been submitted previously, and to allow for state management (for 
target implementations that support state management).

FortifyBugTrackerUtility supports the following approaches for storing information about submitted issues in SSC:

- Storing the target issue link in an SSC custom tag

    In order to use this approach, you will need to define a custom tag in SSC and assign it 
    to the appropriate application versions. FortifyBugTrackerUtility will then store the 
    hyperlink to submitted issues in the custom tag for each individual vulnerability.
    The main advantage of this approach is flexibility, for example you can define multiple
    custom tags for different target systems, allowing the utility to submit a single 
    vulnerability to multiple target systems. Also you can still use one of the native SSC
    bug tracker integrations. The main disadvantage is that SSC does not consider custom tag 
    contents as hyperlinks, so in order to navigate to the corresponding target issue, you 
    will need to manually copy the link and paste it in your browser.

- Storing the target issue link as a native SSC bug link

	In order to use this approach, you will need to install the `Add existing Bugs` native SSC
	bug tracker plugin, and assign it to the appropriate application versions. FortifyBugTrackerUtility
	will then submit the target issue link through this `Add Existing Bugs` plugin, thereby
	having SSC store this link as a native bug link. The main advantage is that users can simply
	click the 'View Bug' icon in SSC to navigate to the corresponding target issue. Main disadvantage
	is that SSC only supports a single bug tracker plugin for each application version, so you can
	no longer use any of the other native SSC bug tracker plugins. Also current SSC versions do not 
	allow for filtering vulnerabilities based on whether a native bug link has been set or not, which
	negatively impacts FortifyBugTrackerUtility performance as it will need to load all relevant
	vulnerabilities from SSC in order to determine which vulnerabilities have been previously submitted 
	to the target system.
	  
Which approach will be used by FortifyBugTrackerUtility can be specified in the configuration file; see 
[SSC Vulnerabilities Configuration](config-SSCSourceVulnerabilitiesConfiguration.html).
Note that you will need to choose an approach up-front and this choice cannot be easily changed later on; if you
change the approach after vulnerabilities have already been submitted to the target system, the 
utility will consider all previously submitted issues as not having been submitted before, and thus will re-submit all 
relevant vulnerabilities to the target system. 

## Extra custom tags

FortifyBugTrackerUtility allows for storing additional information about target issues in SSC custom tags; see
the `extraCustomTags` property in [SSC Vulnerabilities Configuration](config-SSCSourceVulnerabilitiesConfiguration.html).
Of course, in order to allow FortifyBugTrackerUtility to actually store this information, the custom tags being
referred to in the `extraCustomTags` property will need to be defined on SSC and assigned to the applicable
application versions.

## Application version selection

This information applies to all target integrations.

FortifyBugTrackerUtility allows for specifying the SSC application version(s) to be processed on the command line. Usually
you will want to submit vulnerabilities from a specific application version to a corresponding target project/workspace,
so you will need to provide the relevant target system command line options as well.

However, FortifyBugTrackerUtility also provides various methods for mapping application versions to corresponding target
projects/workspaces through the configuration file. These mappings can be based on application version name, or application
version attributes. The name-based mapping basically allows for defining application version to target project/workspace
mapping in the FortifyBugTrackerUtility configuration file, whereas the attribute-based mapping allows for defining this
mapping in SSC.

Most of the default configuration files provide an attribute-based mapping. Based on this mapping, FortifyBugTrackerUtility
will automatically select the appropriate target project/workspace if the SSC application version has a value for the
configured application version attributes. In order to use these attribute-based mappings, you will need to define the
relevant SSC application version attributes (as type 'text'), and provide the appropriate values for these attributes for 
every relevant application version.

If no application versions have been specified on the command line, FortifyBugTrackerUtility will automatically process all
application versions for which a valid mapping exists. 

To summarize, you can run FortifyBugTrackerUtility in the following ways:

- Specify application version(s) and target project/workspace on the command line.
- Specify application version(s) on the command line, and have FortifyBugTrackerUtility automatically determine the corresponding target project/workspace based on configured mappings.
- Run the utility without specifying any application versions on the command line, and have FortifyBugTrackerUtility automatically process all application versions for which a valid mapping exists.

Please see [SSC Application Versions Configuration](config-SSCSourceApplicationVersionsConfiguration.html) for more details on
how to configure these mappings.

## SSC Token Definition

This information applies to all target integrations.

The utility can authenticate with SSC using either username/password, or using an authentication token. To use token-based authentication, you will need to add a token definition to SSC's WEB-INF/internal/serviceContext.xml, and then generate a corresponding token using the following command:

```
FortifyClient token -gettoken FortifyBugTrackerUtility -url http://<ssc host:port>/ssc ...
```

Below is the token definition to be added to serviceContext.xml. Notes:

- The FortifyBugTrackerUtility configuration files may contain settings to request additional information from SSC (for example through the extraVulnerabilityData property). If your customized configuration requests additional data from SSC, these extra URL's will need to be added to the token definition. 
- Whenever you make changes to the token definition, you will need to restart SSC and use the FortifyClient command to generate a new token.
- In SSC 18.10+, you can also generate the token through the SSC web interface instead of using the `FortifyClient` command. SSC will generate both an encoded and un-encoded token; FortifyBugTrackerUtility requires the un-encoded token.


```xml
	<bean id="FortifyBugTrackerUtility" class="com.fortify.manager.security.ws.AuthenticationTokenSpec">
		<property name="key" value="FortifyBugTrackerUtility"/>
		<property name="maxDaysToLive" value="90" />
		<property name="actionPermitted">
			<list value-type="java.lang.String">
				<value>GET=/api/v\d+/attributeDefinitions</value>
				<value>GET=/api/v\d+/customTags</value>
				<value>GET=/api/v\d+/issueDetails/\d+</value>
				<value>GET=/api/v\d+/projectVersions</value>
				<value>GET=/api/v\d+/projectVersions/\d+/attributes</value>
				<value>GET=/api/v\d+/projectVersions/\d+/bugfilingrequirements</value>
				<value>GET=/api/v\d+/projectVersions/\d+/bugtracker</value>
				<value>GET=/api/v\d+/projectVersions/\d+/customTags</value>
				<value>GET=/api/v\d+/projectVersions/\d+/issues</value>
				<value>POST=/api/v\d+/projectVersions/\d+/bugfilingrequirements/action</value>
				<value>POST=/api/v\d+/projectVersions/\d+/issues/action</value>
				<value>PUT=/api/v\d+/projectVersions/\d+/bugfilingrequirements</value>
				<value>PUT=/api/v\d+/projectVersions/\d+/issueSearchOptions</value>
				<value>POST=/api/v\d+/bulk/?</value>
			</list>
		</property>
		<property name="terminalActions">
			<list value-type="java.lang.String">
				<value>InvalidateTokenRequest</value>
				<value>DELETE=/api/v\d+/auth/token</value>
			</list>
		</property>
	</bean>
```
