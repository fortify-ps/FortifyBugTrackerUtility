# Available target issue properties

This section lists the available target issue properties that can be used in the configuration file
for state management, for example for determining which transitions to use to re-open or close target
issues. 

Note that this section is only applicable for target integrations that support state management. The following
integrations do not support state management, so this section is not applicable to these integrations:

* RSA Archer
* CSV File Export
* Submitting issues through a native SSC bug tracker plugin

Properties are listed as template expressions (surrounded by `${` and `}`); 
for standard SpEL expressions like `extraCustomTags` on `SSCSourceVulnerabilitiesConfiguration`, 
you can reference these properties directly without the surrounding `${` and `}`.

For integrations that do support state management, the following target issue properties are available: 

* `${locator.id}`

    Target issue id. note that this property is not always available.
    
* `${locator.deepLink}`

    Browser-viewable deep link to the target issue.
    
* `${fields.fieldName}`

    Target issue fields. The following target issue fields are available:
    
    * Atlassian Jira: All fields returned by the `/rest/api/latest/issue/{id}` endpoint
    * ALM Octane: All fields returned by the `/api/shared_spaces/{sharedSpaceUid}/workspaces/{workspaceId}/defects/{id}` endpoint
    * Microsoft Azure DevOps: All fields returned by the `/_apis/wit/workitems/{id}` endpoint

