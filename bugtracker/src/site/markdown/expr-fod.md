# Available FoD properties

This section lists the available FoD properties that can be used in the configuration file.
Properties are listed as template expressions (surrounded by `${` and `}`); for standard
SpEL expressions like `isVulnerabilityOpenExpression`, you can reference these
properties directly without the surrounding `${` and `}`.

## Application releases configuration

The `FoDSourceReleasesConfiguration` bean loads application release information
from FoD in order to generate a list of application releases to be processed, and to map application 
releases to CLI options. The following FoD application release properties can be referenced in this 
configuration bean:

* `${fieldName}`

    Any field returned by the FoD `/api/v3/Releases/` endpoint

* `${deepLink}`
		       
    Browser-viewable deep link to FoD application release
    
* `${applicationWithAttributesMap.fieldName}`

	Any field returned by the FoD `/api/v3/applications` endpoint

* `${applicationWithAttributesMap.deepLink}`

	Browser-viewable deep link to FoD application
	
* `${applicationWithAttributesMap.attributesMap["attributeName"]}`

	Application attribute value for given attributeName
    
* Any additional fields as loaded through the `extraData` property on [FoDSourceReleasesConfiguration](config-FoDSourceReleasesConfiguration.html). All fields returned by such an endpoint can then be referenced through ${extraDataKey.fieldName}.

## Vulnerabilities and target field mappings

On all other beans, like `FoDSourceVulnerabilitiesConfiguration` and target configurations, the following
FoD vulnerability properties can be referenced:

* `${fieldName}`

    Any field returned by the FoD `/api/v3/Releases/{ReleaseId}/vulnerabilities` endpoint
		    
* `${deepLink}`
		       
    Browser-viewable deep link to FoD vulnerability
    
* `${bugLink}`

    Native FoD bug link, or bug link as stored in comments
    
* `${summary.fieldName}`
		       
    Any field returned by the FoD `/api/v3/releases/{ReleaseId}/vulnerabilities/{vulnId}/summary` endpoint
		     
* `${release.fieldName}`

    Any of the fields as listed under 'Application releases configuration' above
		       
* Any additional fields as loaded through the `extraVulnerabilityData` property on [FoDSourceVulnerabilitiesConfiguration](config-FoDSourceVulnerabilitiesConfiguration.html). All fields returned by such an endpoint can then be referenced through ${extraVulnerabilityDataKey.fieldName}.