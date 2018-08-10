# Available SSC JSON properties

The following fields can be referenced in configuration files when using SSC as the source system:

* `${fieldName}`

    Any field returned by the SSC `/api/v1/projectVersions/${projectVersionId}/issues` endpoint

* `${deepLink}`

    Browser-viewable deep link to SSC vulnerability
    
* `${bugURL}`

    Native SSC bug link, or bug link as stored in custom tag
    
* `${details.fieldName}`
 
    Any field returned by the SSC `/api/v1/issueDetails/${id}` endpoint
    
* `${#ctx.SSCApplicationVersion.fieldName}`

    Any field returned by the SSC `/api/v1/projectVersions` endpoint

* `${#ctx.SSCApplicationVersion.deepLink}`
		       
    Browser-viewable deep link to SSC application version
    
* `${#ctx.SSCApplicationVersion.attributeValuesByName["sttributeName"]}`
		       
    Application version attribute value for given attributeName
		       
* Any additional fields as loaded through the `extraVulnerabilityData` property on [SSCSourceVulnerabilitiesConfiguration](config-SSCSourceVulnerabilitiesConfiguration). All fields returned by such an endpoint can then be referenced through ${extraVulnerabilityDataKey.fieldName}.
 
