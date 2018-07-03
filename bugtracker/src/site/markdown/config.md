# Configuration
As described in the [Usage](usage.html) section, FortifyBugTrackerUtility requires a configuration file in order to do
any work. FortifyBugTrackerUtility comes with example configuration files for the various combinations of source and
target systems; you can modify these example configuration files according to your needs.

In general, a configuration file contains the following elements (you can click on each element to see a more detailed description): 

- A [`<beans>`](config-beans.html) element that wraps all other configuration elements.
- A [`<context:component-scan>`](config-component-scan.html) element for loading source and target implementations.
- A [`<util:map id="contextProperties">`](config-contextProperties.html) for setting default values for command line options.
- One or more beans describing the configuration of the source system:
    - FoD releases: [`<bean class="com.fortify.bugtracker.src.fod.config.FoDSourceConfigurationReleases">`](config-SourceConfigurationFoDReleases.html)
    - FoD vulnerabilities: [`<bean class="com.fortify.bugtracker.src.fod.config.FoDSourceConfigurationVulnerabilities">`](config-SourceConfigurationFoDVulnerabilities.html)
    - SSC application versions: [`<bean class="com.fortify.bugtracker.src.ssc.config.SSCSourceConfigurationApplicationVersions">`](config-SourceConfigurationFoDReleases.html)
    - SSC vulnerabilities: [`<bean class="com.fortify.bugtracker.src.ssc.config.SSCSourceConfigurationVulnerabilities">`](config-SourceConfigurationSSCVulnerabilities.html)
- A bean describing the configuration of the target system (bug tracker, risk management system, file, ...):
    - RSA Archer: [`<bean class="com.fortify.bugtracker.tgt.archer.config.ArcherTargetConfiguration">`](config-TargetConfigurationArcher.html)
    - CSV File: [`<bean class="com.fortify.bugtracker.tgt.file.config.FileTargetConfiguration">`](config-TargetConfigurationFile.html)
    - Atlassian Jira: [`<bean class="com.fortify.bugtracker.tgt.jira.config.JiraTargetConfiguration">`](config-TargetConfigurationJira.html)
    - ALM Octane: [`<bean class="com.fortify.bugtracker.tgt.octane.config.OctaneTargetConfiguration">`](config-TargetConfigurationOctane.html)
    - TFS: [`<bean class="com.fortify.bugtracker.tgt.tfs.config.TFSTargetConfiguration">`](config-TargetConfigurationTFS.html)
    - Native SSC bug tracker: [`<bean class="com.fortify.bugtracker.tgt.ssc.config.SSCTargetConfiguration">`](config-TargetConfigurationSSC.html)
  

