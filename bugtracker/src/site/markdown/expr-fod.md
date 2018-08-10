# Available FoD JSON properties

The following fields can be referenced in configuration files when using FoD as the source system:

* `${fieldName}`

    Any field returned by the FoD `/api/v3/Releases/{ReleaseId}/vulnerabilities` endpoint
		    
* `${deepLink}`
		       
    Browser-viewable deep link to FoD vulnerability
    
* `${bugLink}`

    Native FoD bug link, or bug link as stored in comments
    
* `${summary.fieldName}`
		       
    Any field returned by the FoD `/api/v3/releases/{ReleaseId}/vulnerabilities/{vulnId}/summary` endpoint
		     
* `${#ctx.Release.fieldName}`

    Any field returned by the FoD `/api/v3/releases` endpoint

* `${#ctx.Release.deepLink}`
		       
    Browser-viewable deep link to FoD application release
    
* `${#ctx.Release.applicationWithAttributesMap.fieldName}`

	Any field returned by the FoD `/api/v3/applications` endpoint

* `${#ctx.Release.applicationWithAttributesMap.deepLink}`

	Browser-viewable deep link to FoD application
	
* `${#ctx.Release.applicationWithAttributesMap.attributesMap["attributeName"]}`

	Application attribute value for given attributeName
		       
* Any additional fields as loaded through the `extraVulnerabilityData` property on [FoDSourceVulnerabilitiesConfiguration](config-FoDSourceVulnerabilitiesConfiguration). All fields returned by such an endpoint can then be referenced through ${extraVulnerabilityDataKey.fieldName}.