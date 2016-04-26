# Fortify on Demand bug tracker integrations

Disclaimer
====
The code in the repository is still under development and has not been thoroughly tested. Issues may arise if used inappropriately.

Introduction
====
The Maven projects in this repository make up a utility for submitting vulnerabilities from Fortify on Demand to bug trackers. At the moment, this utility supports the following bug trackers:
- JIRA
- Export to file

Quick Start
====
After building the utility (see below), one of the following pre-defined processes can be run.

JIRA
----
Copy the 'processRunnerConfig-fod-jira-short.xml' to a location and name of your liking (referred to as [configFile] below. Edit [configFile] to update connection properties like base URL's and credentials for FoD and Jira, and optional proxy. Optionally, you can also change filter criteria, issue grouping criteria, and issue format. See the comments in [configFile] for details. 

Once [configFile] has been updated, the utility can be started as follows:

  java -jar processrunner-bugtracker-1.0.jar --config [configFile] -FoDReleaseId [FoD release id] -JiraProjectKey [JIRA project key]
  
For all FoD vulnerabilities in the given release id that match the filter criteria, new issues will be submitted to JIRA according to the configured grouping criteria and issue format.

Export to file
----
Copy the 'processRunnerConfig-fod-file-short.xml' to a location and name of your liking (referred to as [configFile] below. Edit [configFile] to update connection properties like FoD base URL and credentials, and optional proxy. Optionally, you can also change filter criteria and file output format. See the comments in [configFile] for details. 

Once [configFile] has been updated, the utility can be started as follows:

  java -jar processrunner-bugtracker-1.0.jar --config [configFile] -FoDReleaseId [FoD release id] -OutputFile [output file]
  
For all FoD vulnerabilities in the given release id that match the filter criteria, new records will be appended to the given output file.


General usage
====
After building the utility (see below), it can be invoked as follows:

  java -jar processrunner-bugtracker-1.0.jar [--config [configFile]] [processorRunnerId] [--help] [options]

  - [--config [configFile]] can be used to specify a configuration file path and name. By default, 'processRunnerConfig.xml' in the current working directory is used.
  - [processRunnerId] identifies the process runner to run. This option is required if the configuration file contains multiple process runner definitions and none of them is named 'defaultProcessRunner'.
  - [--help] shows the available options for the current process runner.
  - [options] are additional options specific for the current process runner.


Building from source
====
To build the utility from source, navigate to the processrunner-bugtracker-root directory and run 'mvn clean package'. This will build all necessary modules, and generate the following relevant files in the processrunner-bugtracker/target directory:
- processrunner-bugtracker-1.0.jar: Runnable jar-file containing all relevant code and dependencies
- processRunnerConfig.xml: Default configuration file that outputs a message on how to properly configure the utility
- ConfigFileExamples/*.xml: Sample configuration files for submitting FoD vulnerabilities to bug trackers

Module overview
====
- fortify-util: Contains various utility modules. This includes utility modules for working with Fortify products, as well as generic utility modules that are used by other modules. These modules can be re-used for other types of projects if necessary.
- processrunner: Contains the generic process runner architecture, allowing to run arbitrary processes. This module can be re-used for other types of projects if necessary.
- processrunner-bugtracker-root: Contains various modules that actually implement the functionality as described on this page. This includes:
  - processrunner-bugtracker: Combines all functionality from the other modules, and builds a shaded jar-file and sample configuration files.
  - processrunner-bugtracker-common: Common functionality that is used by multiple other modules.
  - processrunner-bugtracker-file: Functionality for submitting vulnerability information to a file.
  - processrunner-bugtracker-fod: Functionality for retrieving vulnerability information from FoD.
  - processrunner-bugtracker-jira: Functionality for submitting issues to JIRA.

Adding support for new bug trackers
====
Given the generic architecture of the utility, it should be quite easy to add support for new bug trackers. In general, the following steps should be followed:
- Create a new Maven module named processrunner-bugtracker-[name] under processrunner-bugtracker-root. Use processrunner-bugtracker-root as the parent module.
- Add the module to pom.xml in processrunner-bugtracker-root, both as a module-element and in the dependencyManagement section
- Add the module as a dependency in the pom.xml in processrunner-bugtracker
- Implement the actual functionality for submitting issues by implementing one or more IProcessor implementations and supporting classes
- Add sample configuration file(s) to processrunner-bugtracker

