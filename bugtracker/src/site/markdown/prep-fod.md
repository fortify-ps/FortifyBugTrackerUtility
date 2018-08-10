# FoD Preparations

The following sections describe the preparations that you will need to do on FoD in order to use FortifyBugTrackerUtility.

## Storing information about submitted target issues

This information applies to all target integrations, apart from the following:

- File export
 
FortifyBugTrackerUtility will need to keep track of which vulnerabilities have been submitted to which target issue, 
to avoid re-submitting vulnerabilities that have been submitted previously, and to allow for state management (for 
target implementations that support state management).

FortifyBugTrackerUtility supports the following approaches for storing information about submitted issues in FoD:

- Storing the target issue link in vulnerability comments

    When using this approach, FortifyBugTrackerUtility will store the id and hyperlink for 
    submitted issues as vulnerability comments. The main advantages of this approach are that 
    no preparations are necessary on FoD, and flexibility; for example the utility 
    can submit a single vulnerability to multiple target systems by using different types of comments. 
    Also you can still use one of the native FoD bug tracker integrations. The main disadvantage is 
    that FoD does not consider comment contents as hyperlinks, so in order to navigate to the 
    corresponding target issue, you will need to manually copy the link and paste it in your browser.
    Also this approach has a negative performance impact on FortifyBugTrackerUtility, as the utility
    will need to load all relevant vulnerabilities and analyze the comments in order to determine
    whether a vulnarability has already been submitted to the target system or not.

- Storing the target issue link as a native FoD bug link

	In order to use this approach, you will need to configure the FoD application to use bug tracker 
	'Other'. FortifyBugTrackerUtility 	will then add the target issue link as a native bug link for
	each submitted vulnerability. The main advantage is that users can simply click the 'View Bug' 
	icon in FoD to navigate to the corresponding target issue. Main disadvantage
	is that FoD only supports a single bug tracker plugin for each application version, so you can
	no longer use any of the other native FoD bug tracker plugins.
	  
Which approach will be used by FortifyBugTrackerUtility can be specified in the configuration file; see 
[FoD Vulnerabilities Configuration](config-FoDSourceVulnerabilitiesConfiguration.html).
Note that you will need to choose an approach up-front and this choice cannot be easily changed later on; if you
change the approach after vulnerabilities have already been submitted to the target system, the 
utility will consider all previously submitted issues as not having been submitted before, and thus will re-submit all 
relevant vulnerabilities to the target system. 

## Application release selection

This information applies to all target integrations.

FortifyBugTrackerUtility allows for specifying the FoD application version(s) to be processed on the command line. Usually
you will want to submit vulnerabilities from a specific application version to a corresponding target project/workspace,
so you will need to provide the relevant target system command line options as well.

However, FortifyBugTrackerUtility also provides various methods for mapping application releases to corresponding target
projects/workspaces through the configuration file. These mappings can be based on application release name, or application
attributes. The name-based mapping basically allows for defining application releases to target project/workspace
mapping in the FortifyBugTrackerUtility configuration file, whereas the attribute-based mapping allows for defining this
mapping in FoD.

If no application releases have been specified on the command line, FortifyBugTrackerUtility will automatically process all
application releases for which a valid mapping exists. 

Because attributes are defined at application level in FoD, and not release level, the attribute-based mappings apply to
all releases within a single application. As such, contrary to the default SSC-related configuration files, the default FoD 
configuration files do not provide default attribute-based mappings, as this would cause the utility to process all
releases for each application that defines a value for these attributes. As such you will usually want to use a combination
or attribute-based mappings and other filters, for example for selecting only releases with a specific name like 'current'
or 'master' within each application.

To summarize, you can run FortifyBugTrackerUtility in the following ways:

- Specify application release(s) and target project/workspace on the command line.
- Specify application release(s) on the command line, and have FortifyBugTrackerUtility automatically determine the corresponding target project/workspace based on configured mappings.
- Run the utility without specifying any application releases on the command line, and have FortifyBugTrackerUtility automatically process all application releases for which a valid mapping exists.

Please see [FoD Releases Configuration](config-FoDSourceReleasesConfiguration.html) for more details on
how to configure these mappings.