# Introduction
FortifyBugTrackerUtility is a command-line Java program that allows for exporting vulnerabilities from Fortify on Demand (FoD) and Fortify Software Security Center (SSC) to external systems like bug trackers or risk management systems. The utility can either be invoked manually for an ad-hoc export, or scheduled to run on a regular basis for automated exports.

## Reasons for using FortifyBugTrackerUtility
Both FoD and SSC provide their own native bug tracker integrations, allowing to interactively submit vulnerabilities to bug trackers. SSC also supports custom-developed integrations to support other bug tracking systems. Depending on your requirements, these native integrations may be sufficient. 

However, FortifyBugTrackerUtility allows for more advanced use cases that the native integrations do not cater for. In general, FortifyBugTrackerUtility provides support for the following:

- Full automation without requiring any user interaction
  - Automatic selection of SSC application versions or FoD releases to be processed, based on configurable selection criteria
  - Automatic selection of vulnerabilities to be submitted to the external system, based on configurable selection criteria
  - Automatic grouping of multiple vulnerabilities, based on configurable grouping criteria
- Fields to be submitted to the external system are fully configurable
- More comprehensive and fully configurable bug state management compared to native integrations
- Bi-directional sync; information about current issue state in the external system can be displayed in SSC (not yet supported for FoD)
- Submit a single vulnerability to multiple external systems (for example bug tracker and risk management system)
- For FoD: Submit vulnerabilities to an on-premise system without requiring a direct connection from FoD to that system

Note that FortifyBugTrackerUtility can even integrate with the native SSC bug tracker integrations. In this scenario, you use FortifyBugTrackerUtility for automatically selecting and grouping vulnerabilities, but the vulnerabilities are submitted through the native SSC bug tracker integration, and bug state management is performed by SSC.

The following table lists the target systems that are currently supported, together with some of their main capabilities based on the example configuration files:

| Target System | From FoD | From SSC | Grouping | State Management | Remarks |
| ------------- | -------- | -------- |--------- | ---------------- | ------- |
| RSA Archer    | Yes      | Yes      | No       | No               | Currently only supports text and value list field types |
| CSV File      | Yes      | Yes      | No       | No               | By default, a separate output file is written for each application version/release. All relevant vulnerabilities are exported on each run, overwriting any existing files |
| Atlassian Jira | Yes     | Yes      | Yes      | Yes              | May require customizations if custom Jira configurations are being used |
| ALM Octane     | Yes     | Yes      | Yes      | Yes              | State transition comments are not yet supported |
| Azure DevOps   | Yes     | Yes      | Yes      | Yes              | Additional state transitions may need to be configured |
| SSC Bug Trackers | No    | Yes      | Yes      | Performed by SSC | Sample configuration for SSC Jira bug tracker included, other SSC bug trackers require corresponding configuration files to be added |

