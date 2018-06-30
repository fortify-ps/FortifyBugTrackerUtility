# SSC Configuration
The SSC-related configuration in each configuration file consists of the following elements:

- `<context:component-scan base-package="com.fortify.processrunner.ssc"/>`  
  Automatically loads various components required for loading vulnerability data from SSC. This line
  should always be present for configurations that load vulnerability data from SSC.
- `<bean ... class="com.fortify.processrunner.ssc.processor.composite.SSCBugTrackerProcessorConfiguration">`
  This bean definition should always be present, and contains various SSC-related configuration settings. For example:
    - Vulnerability selection criteria
    - Criteria for considering a vulnerability either open or closed
    - How to store the submitted issue link in SSC
    - Additional custom tags to be set, based on the current state of the submitted issue
- One or more bean definitions for defining the application versions to be automatically processed, and how to generate
  additional action-specific command line options (context properties) for each application version being processed.
  
The following sections describe these configuration options in more detail.

## Vulnerability Data
Both the SSC-related configuration and the configuration related to the target system, are based on various expressions
that reference SSC vulnerability details. These expressions can reference the following SSC vulnerability data:

- All vulnerability properties returned by the SSC `/api/v1/projectVersions/{SSCApplicationVersionId}/issues` endpoint
- `vulnState`  
  The current state of the vulnerability, based on `isVulnerabilityOpenExpression` (see 'Vulnerability open/closed expression' below)
- `deepLink`  
  The browser-viewable deep link for the current vulnerability
- `bugURL`  
  Either the native bug URL stored in SSC, or the bug URL as stored in a vulnerability custom tag
- `details`  
  Contain all data returned by the SSC `/api/v1/issueDetails/{vulnId}` endpoint. This data is loaded on-demand; the 
  issue details will only be loaded from SSC if the `details` property is being referenced in any expressions. 

## Vulnerability selection criteria
Vulnerabilities to be exported can be filtered either by SSC directly, or by FortifyBugTrackerUtility itself. Having the filtering 
performed by SSC directly provides the best performance, as this reduces the amount of data returned by SSC. However, in some
cases SSC-based filtering is not sufficient, so you can configure additional filters that will be evaluated by FortifyBugTrackerUtility
on the data returned by SSC.

The `com.fortify.processrunner.ssc.processor.composite.SSCBugTrackerProcessorConfiguration` bean provides
the following properties to configure vulnerability filters:

- `filterStringForVulnerabilitiesToBeSubmitted`  
  This string is sent to SSC as-is to allow SSC to filter the list of vulnerabilities. The syntax for this search string is
  the same as for the 'Search issues' box in the SSC web interface. As such, you can test your filter string in SSC itself
  to verify that it returns the correct subset of issues. Please refer to the SSC search box syntax guide for more details.
  The default configuration files use `analysis:exploitable` as the SSC filter string, meaning that only vulnerabilities
  for which the Analysis custom tag has been set to 'Exploitable' will be submitted to the external system.
- `regExFiltersForVulnerabilitiesToBeSubmitted`  
  After SSC has returned the list of vulnerabilities to FortifyBugTrackerUtility, this list of vulnerabilities can optionally
  be filtered further using regular expressions. This property takes a map with vulnerability field names as keys, and the
  corresponding regular expressions to be matched as values. The default configuration files do not perform any regular expression 
  based filtering.
 
## Vulnerability open/closed expression
For bug state management purposes, FortifyBugTrackerUtility needs to know whether a vulnerability is open or closed. This is
determined by evaluating the expression that is configured through the `isVulnerabilityOpenExpression` property on the
`com.fortify.processrunner.ssc.processor.composite.SSCBugTrackerProcessorConfiguration` bean. In the
sample configuration files, vulnerabilities that are not suppressed and not removed are considered as 'open', all other vulnerabilities
will be considered as 'closed'.

## Storing the link to the submitted issue
FortifyBugTrackerUtility needs to keep track of which SSC vulnerabilities have been submitted to which issue in the external
system. This information is used for bug state management, as well as to avoid vulnerabilities from being submitted to the
external system multiple times. 

The only exceptions (at this moment) are the following integrations:

- SSC to File export; all relevant vulnerabilities will be exported to the file on each invocation (independent of whether 
  they have been exported before), and the file export does not support bug state management
- Submitting vulnerabilities through native SSC bug tracker integrations; the native SSC bug tracker integration will
  store information about the submitted issue for each vulnerability

The SSC implementation can store the issue link in two ways; either in a custom tag of type 'Text', or as a native SSC bug tracker
link. Both can be configured through properties on the `com.fortify.processrunner.ssc.processor.composite.SSCBugTrackerProcessorConfiguration` bean:

- `bugLinkCustomTagName`  
  This property allows you to specify the custom tag name in which the issue link should be stored. You will need to add
  a custom tag with this name in SSC, and assign it to all application versions for which you want to use FortifyBugTrackerUtility.
- `addNativeBugLink`  
  This property can be set to 'true' if you want to store the issue link as a native SSC bug link, allowing users to click
  the 'bug' icon in SSC to navigate to the corresponding issue in the external system. This option requires to have the
  'Add Existing Bugs' native SSC bug tracker integration to be installed in SSC (included in the distribution bundle), and 
  having the 'Add Existing Bugs' bug tracker configured on all application versions for which you want to use FortifyBugTrackerUtility.
  
Note that SSC only allows one native bug tracker integration for each application version. When using the 'addNativeBugLink'
approach, you will not be able to use any other native SSC bug tracker integration. If you want users to be able to also
manually submit issues to a bug tracker through a native SSC integration, or if you want to submit issues to multiple 
external systems, you should use the custom tag based approach.   

## Bi-directional sync
Optionally, the SSC implementation can make information about the current state of a submitted issue in an external system
visible in SSC through custom tags. For example, this allows SSC users to see whether the submitted issue has state 'In Progress'
or 'Closed'. This information can be stored in SSC custom tags.

Custom tag contents can be configured through the `extraCustomTags` on the `com.fortify.processrunner.ssc.processor.composite.SSCBugTrackerProcessorConfiguration` bean. This property
takes a map with custom tag name as the key, and an expression as the value. After submitting new issues, or during bug state
management, FortifyBugTrackerUtility will evaluate the given expression(s) and update the custom tag values accordingly. 

The fields that can be used in the expression is dependent on the target system being used; see the examples for more information.
Note that FortifyBugTrackerUtility will silently ignore any custom tags that have not been defined in SSC, or that have not
been assigned to the current application version. 

## Processing multiple application versions
The most straight-forward way for invoking FortifyBugTrackerUtility is by specifying an SSC application version on the command
line, together with information about the location/project of the external system to which the vulnerabilities need to be 
exported. For example, when exporting vulnerabilities to JIRA, you can specify both the SSC application version and corresponding 
JIRA project key as command line options. 

However, if you have a lot of application versions, it may become cumbersome to separately invoke FortifyBugTrackerUtility
for each individual application version. Therefore FortifyBugTrackerUtility allows you to configure a mapping between
SSC application versions and corresponding target system command line options. This mapping can either be hard-coded in
the configuration file, or you can configure a mapping based on application version attributes:

- A bean with class `com.fortify.processrunner.ssc.appversion.SSCApplicationVersionAttributeFilterAndMapper`
  allows you to map application version attribute values to corresponding action-specific command line options. You can configure both 
  `requiredAttributeMappings` and `optionalAttributeMappings` on this bean. Both properties can be configured with a 
  map, where the key specifies the SSC application version attribute name, and the value specifies the corresponding command line
  option to be set based on the value of this application version attribute.   
  For example, you could have an application version attribute named 'Jira Project Key' that is automatically mapped to the
  'JiraProjectKey' command line option. If you run FortifyBugTrackerUtility for a specific SSC application version, it will
  automatically add the '-JiraProjectKey' command line option based on the corresponding application version attribute. If you run 
  FortifyBugTrackerUtility without specifying any SSC application version, it will automatically process all application versions
  for which all application version attributes specified through `requiredAttributeMappings` have a non-empty value.
  
- A bean with class `com.fortify.processrunner.ssc.appversion.SSCApplicationVersionNameFilterAndMapper`
  allows you to hard-code a mapping between application version names and corresponding command line options. On this bean,
  you can configure a property names `applicationVersionNameMappings` with a map, where the key specifies a regular
  expression to match [application name]:[version name], and the value specifies a comma-separated list of command line options
  together with their values. 



