# Introduction
FortifyBugTrackerUtility is a command-line Java program that allows for exporting vulnerabilities from Fortify on Demand (FoD) and Fortify Software Security Center (SSC) to external systems like bug trackers or risk management systems. The utility can either be invoked manually for an ad-hoc export, or scheduled to run on a regular basis for automatic exports.

FortifyBugTrackerUtility is highly modularized, with modules that provide the general infrastructure, modules for loading data from source systems (FoD and SSC), and modules for submitting data to target systems like ALM Octane or JIRA. New modules can be easily added to add support for additional source or target systems.

Source and target modules are wired together using XML configuration files, based on the Spring Framework. A configuration file describes which source and target system to use, and various settings for both the source and target system.

Depending on capabilities of source and target modules, the general infrastructure allows for the following functionality:

- Automatic selection of SSC application versions or FoD releases to be processed, based on configurable selection criteria
- Automatic selection of vulnerabilities to be submitted to the external system, based on configurable selection criteria
- Automatic grouping of multiple vulnerabilities, based on configurable grouping criteria
- State management; automatically closing or re-opening issues in the external system based on current SSC/FoD vulnerability state
- Bi-directional sync; information about current issue state in the external system can be displayed in SSC (not yet supported for FoD)
- A single vulnerability can be submitted to multiple external systems

The following table lists the target systems that are currently supported, together with some of their main capabilities based on the example configuration files:

| Target System | From FoD | From SSC | Grouping | State Management | Remarks |
| ------------- | -------- | -------- |--------- | ---------------- | ------- |
| RSA Archer    | Yes      | Yes      | No       | No               | Currently only supports text and value list field types |
| CSV File      | Yes      | Yes      | No       | No               | By default, a separate output file is written for each application version/release. All relevant vulnerabilities are exported on each run, overwriting any existing files |
| Atlassian Jira | Yes     | Yes      | Yes      | Yes              | May require customizations if custom Jira configurations are being used |
| ALM Octane     | Yes     | Yes      | Yes      | Yes              | State transition comments are not yet supported |
| Microsoft TFS  | Yes     | Yes      | Yes      | Yes              | Additional state transitions may need to be configured |
| SSC Bug Trackers | No    | Yes      | Yes      | Performed by SSC | Sample configuration for SSC TFS bug tracker included, other SSC bug trackers require corresponding configuration files to be added |

