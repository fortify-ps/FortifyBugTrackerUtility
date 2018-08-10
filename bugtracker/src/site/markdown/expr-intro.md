# SpEL expressions

FortifyBugTrackerUtility makes extensive use of the Spring Expression Language (SpEL). For the full
SpEL reference, please see [https://docs.spring.io/spring/docs/4.2.5.RELEASE/spring-framework-reference/html/expressions.html].

Two types of SpEL expressions are being used; normal expressions and template expressions. In normal 
expressions, properties are referenced directly and static strings are surrounded by single or double quotes.
Template expressions are literal strings that can embed normal SpEL expressions by surrounding those
expressions with `${` and `}`. For example:

- Normal expression: `'Hello' + name + ', good morning!'`
- Template expression: `Hello ${name}, good morning!`

With FortifyBugTrackerUtility, SpEL expressions are mostly used for the following two purposes:
 
 - Building text strings by combining one or more properties from JSON objects, for example `Hello ${firstName} ${lastName}`
 - Decision making (filtering, selection) using expressions that return `true` or `false`, for example `firstName=='Jim'`
 
As such, most expressions will be relatively simple, directly referencing JSON properties. However more advanced
expressions are allowed, for example:
 
 - Map Fortify priority order to a corresponding target system priority: `${{'Critical':'Highest','High':'High','Medium':'Medium','Low':'Low'}.get(friority)}`
 - Build an array instead of simple text string: `${{'SSC', 'FortifyBugTrackerUtility'}}`
 
Note the extra `{` and `}` in these expressions, which are SpEL constructs for creating either a map or array. 

Please see the sub-topics in this section for more information about the JSON objects available for the
various source and target systems.
 
