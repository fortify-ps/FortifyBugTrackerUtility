# Configuration
As described in the [Usage](usage.html) section, FortifyBugTrackerUtility requires a configuration file in order to do
any work. The configuration file can include information like:

- The source system (SSC or FoD) from which to retrieve the vulnerability data
- The target system (bug tracker, risk management system, file, ...) to which vulnerability data is to be exported
- Selection criteria for determining which vulnerabilities are to be exported to the target system
- Grouping criteria for grouping similar vulnerabilities into a single target issue
- The fields to be submitted to the external system
- How to store information about submitted issues in the source system, to allow the source system to keep track
  of which vulnerabilities have already been submitted to the target system
- Bug state management configuration, like target system issue transition work flows
- Default values for command line options, like source and target system URL's and credentials
- How to map source system application versions/releases to target system projects

Each configuration file contains various sections for configuring either settings related to the source system,
settings related to the target system, or general processing settings. The exact contents of the configuration file 
depends on the chosen source and target systems, and the capabilities of the corresponding integration module. For example:

- The SSC and FoD implementations use different approaches for storing information about submitted issues
- The SSC and FoD implementation have different vulnerability field names
- Different target systems require different fields and field contents to be submitted
- Some target system implementations may support bug state management, whereas others don't

FortifyBugTrackerUtility comes with various example configuration files for the various combinations of source and
target systems. These files can be used as-is, but the default configuration files may require specific source or
target system configurations. For example, most of the default SSC-related configuration files will require you
to configure an SSC custom tag to hold the hyperlink to submitted issues in the target system. 

As such, it is always a good idea to review the configuration file that you want to use, and modify any settings
according to your needs. For more details, please refer to the comments in the sample configuration files, as well 
as the corresponding sections in this documentation. 

