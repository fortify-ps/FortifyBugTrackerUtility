# FoD Configuration
The FoD-related configuration in each configuration file consists of the following elements:

- `<context:component-scan base-package="com.fortify.processrunner.fod"/>`  
  Automatically loads various components required for loading vulnerability data from FoD. This line
  should always be present for configurations that load vulnerability data from FoD.
- `<bean ... class="com.fortify.processrunner.fod.processor.composite.FoDBugTrackerProcessorConfiguration">`
  This bean definition should always be present, and contains various FoD-related configuration settings. For example:
    - Vulnerability selection criteria
    - Criteria for considering a vulnerability either open or closed
    - How to store the submitted issue link in FoD
- A bean definition for defining the application releases to be automatically processed, and how to generate
  additional action-specific command line options (context properties) for each application release being processed.
  
The following sections describe these configuration options in more detail.

## Vulnerability selection criteria
Vulnerabilities to be exported can be filtered either by FoD directly, or by FortifyBugTrackerUtility itself. Having the filtering 
performed by FoD directly provides the best performance, as this reduces the amount of data returned by FoD. However, in some
cases FoD-based filtering is not sufficient, so you can configure additional filters that will be evaluated by FortifyBugTrackerUtility
on the data returned by FoD.

The `com.fortify.processrunner.fod.processor.composite.FoDBugTrackerProcessorConfiguration` bean provides
the following properties to configure vulnerability filters:

- `filterStringForVulnerabilitiesToBeSubmitted`  
  This string is sent to FoD as-is to allow FoD to filter the list of vulnerabilities. The syntax for this search string is
  described in the FoD REST API documentation. You can use the FoD Swagger utility to test your filter string.
  The default configuration files use `severityString:Critical|High+isSuppressed=false` as the FoD filter string, 
  meaning that only non-suppressed Critical and High vulnerabilities will be submitted to the external system.
- `regExFiltersForVulnerabilitiesToBeSubmitted`  
  After FoD has returned the list of vulnerabilities to FortifyBugTrackerUtility, this list of vulnerabilities can optionally
  be filtered further using regular expressions. This property takes a map with vulnerability field names as keys, and the
  corresponding regular expressions to be matched as values. The default configuration files do not perform any regular expression 
  based filtering.
 
## Vulnerability open/closed expression
For bug state management purposes, FortifyBugTrackerUtility needs to know whether a vulnerability is open or closed. This is
determined by evaluating the expression that is configured through the `isVulnerabilityOpenExpression` property on the
`com.fortify.processrunner.fod.processor.composite.FoDBugTrackerProcessorConfiguration` bean. In the
sample configuration files, vulnerabilities that are not suppressed and not removed are considered as 'open', all other vulnerabilities
will be considered as 'closed'.

## Storing the link to the submitted issue
FortifyBugTrackerUtility needs to keep track of which FoD vulnerabilities have been submitted to which issue in the external
system. This information is used for bug state management, as well as to avoid vulnerabilities from being submitted to the
external system multiple times. 

The only exception (at this moment) is the FoD to File export; all relevant vulnerabilities will be exported to the file on each 
invocation (independent of whether they have been exported before), and the file export does not support bug state management.

The FoD implementation can store the issue link in two ways; either as vulnerability comments, or as a native FoD bug tracker
link. Both can be configured through properties on the `com.fortify.processrunner.fod.processor.composite.FoDBugTrackerProcessorConfiguration` bean:

- `addBugDataAsComment`  
  This property is set to 'true' in the sample configuration files (apart from the file export configuration file), meaning that 
  the issue link and id are stored as vulnerability comments. This property can be set to 'false' if you do not want to store this 
  information in comments, for example if you want to store the issue link as a native FoD bug link instead.
- `addNativeBugLink`  
  This property can be set to 'true' if you want to store the issue link as a native FoD bug link, allowing users to click
  the 'bug' icon in FoD to navigate to the corresponding issue in the external system. This option requires the FoD application
  to be configured with bug tracker 'Other'.
  
Note that FoD only allows one native bug tracker integration for each application version. When using the 'addNativeBugLink'
approach, you will not be able to use any other native FoD bug tracker integration. If you want users to be able to also
manually submit issues to a bug tracker through a native FoD integration, or if you want to submit issues to multiple 
external systems, you should use the comment-based approach.   

## Bi-directional sync
The SSC implementation can make information about the current state of a submitted issue in an external system
visible in SSC through custom tags. At the moment, bi-directional sync is not supported for FoD.

## Processing multiple application versions
The most straight-forward way for invoking FortifyBugTrackerUtility is by specifying an FoD application release on the command
line, together with information about the location/project of the external system to which the vulnerabilities need to be 
exported. For example, when exporting vulnerabilities to JIRA, you can specify both the FoD application release and corresponding 
JIRA project key as command line options. 

However, if you have a lot of application releases, it may become cumbersome to separately invoke FortifyBugTrackerUtility
for each individual application version. Therefore FortifyBugTrackerUtility allows you to configure a mapping between
FoD application releases and corresponding target system command line options. This mapping can either be hard-coded in
the configuration file, or you can configure a mapping based on application version attributes:

- A bean with class `com.fortify.processrunner.fod.releases.FoDReleaseNameFilterAndMapper`
  allows you to hard-code a mapping between application release names and corresponding command line options. On this bean,
  you can configure a property names `releaseNameMappings` with a map, where the key specifies a regular
  expression to match [application name]:[release name], and the value specifies a comma-separated list of command line options
  together with their values. 

- A bean with class `com.fortify.processrunner.fod.releases.FoDApplicationAttributeFilterAndMapper`
  allows you to map application attribute values to corresponding action-specific command line options. You can configure both 
  `requiredAttributeMappings` and `optionalAttributeMappings` on this bean. Both properties can be configured with a 
  map, where the key specifies the FoD application attribute name, and the value specifies the corresponding command line
  option to be set based on the value of this application attribute.   
  For example, you could have an application attribute named 'Jira Project Key' that is automatically mapped to the
  'JiraProjectKey' command line option. If you run FortifyBugTrackerUtility for a specific FoD application release, it will
  automatically add the '-JiraProjectKey' command line option based on the corresponding application attribute. If you run 
  FortifyBugTrackerUtility without specifying any FoD application releases, it will automatically process all applications 
  for which all application attributes specified through `requiredAttributeMappings` have a non-empty value.  
  Note that all releases within each matching application will be processed. As this may be undesirable, this functionality
  is disabled in the sample configuration files. You can however use a combination of static and dynamic configuration to
  allow command line options to be set through application attributes, but for every application process only a release
  with a specific name. For example, you could define a static filter `.*:Current` to only process releases named
  'Current' for every application.
  




