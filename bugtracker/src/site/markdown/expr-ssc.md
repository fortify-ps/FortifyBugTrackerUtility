# Available SSC properties

This section lists the available SSC properties that can be used in the configuration file.
Properties are listed as template expressions (surrounded by `${` and `}`); for standard
SpEL expressions like `isVulnerabilityOpenExpression`, you can reference these
properties directly without the surrounding `${` and `}`.

## Application versions configuration

The `SSCSourceApplicationVersionsConfiguration` bean loads application version information
from SSC in order to generate a list of application versions to be processed, and to map application 
versions to CLI options. The following SSC application version properties can be referenced in this 
configuration bean:

* `${fieldName}`

    Any field returned by the SSC `/api/v1/projectVersions` endpoint

* `${deepLink}`
		       
    Browser-viewable deep link to SSC application version
    
* `${attributeValuesByName["attributeName"]}`
		       
    Application version attribute value for given attributeName
    
* Any additional fields as loaded through the `extraData` property on [SSCSourceApplicationVersionsConfiguration](config-SSCSourceApplicationVersionsConfiguration.html). All fields returned by such an endpoint can then be referenced through ${extraDataKey.fieldName}.
    

## Vulnerabilities and target field mappings

On all other beans, like `SSCSourceVulnerabilitiesConfiguration` and target configurations, the following
SSC vulnerability properties can be referenced:

* `${fieldName}`

    Any field returned by the SSC `/api/v1/projectVersions/${projectVersionId}/issues` endpoint

* `${deepLink}`

    Browser-viewable deep link to SSC vulnerability
    
* `${bugURL}`

    Native SSC bug link, or bug link as stored in custom tag
    
* `${details.fieldName}`
 
    Any field returned by the SSC `/api/v1/issueDetails/${id}` endpoint. Note that for ease of use,
    FortifyBugTrackerUtility will also add a `customTagName` property to the `customTagValues` 
    object returned by this endpoint; by default SSC only returns the customTagGuid property.
    
* `${#ctx.SSCApplicationVersion.fieldName}`

    Any of the fields as listed under 'Application versions configuration' above
		       
* Any additional fields as loaded through the `extraVulnerabilityData` property on [SSCSourceVulnerabilitiesConfiguration](config-SSCSourceVulnerabilitiesConfiguration.html). All fields returned by such an endpoint can then be referenced through ${extraVulnerabilityDataKey.fieldName}.
 
