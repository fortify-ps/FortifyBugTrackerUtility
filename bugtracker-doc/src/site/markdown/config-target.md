# Target System Configuration
Most of the target system configurations are pretty similar, so the general approach is described on this page.
Additional information about specific target systems may be available in other sections.

In general, the target system configuration consists of up to three bean definitions:

- A bean definition that specifies the target system field configuration, i.e. the fields to be submitted or updated and their contents
- A bean definition for grouping and submitting vulnerabilities to the target system
- A bean definition for updating the target system with updated vulnerability state (optional; not supported for some integrations)

## Target System Field Configuration
All configuration files contain a bean of class `com.fortify.processrunner.common.bugtracker.issue.BugTrackerFieldConfiguration`. On this bean, the
following properties can be configured:

- `fields`  
  This field is configured with a map, where the map key is the target system field name, and the map value is
  an expression to generate the target system field value. When vulnerability grouping is enabled, the expression
  is evaluated against the first available vulnerability in each group.
- `appendedFields`  
  This field again is configured with a map, similar to the `fields` property described above. When vulnerability
  grouping is enabled, the expressions are evaluated for every vulnerability in each group, and the result is appended
  to the corresponding field.
- `fieldsToUpdateDuringStateManagement`   
  If the integration supports bug state management, it can optionally update one or more fields (as defined through the
  `fields` and `appendedFields` properties) while performing state management. For example, this can be used
  to update the description field in the target system with updated vulnerability information (line numbers, vulnerability
  state, ...)
  
The field value expressions are based on the [Spring Expression Language (SpEL)](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html). These expressions can reference
vulnerability fields using `${[vulnerabilityFieldName]}` notation, as well as context properties (action-specific command
line parameters) using `${#ctx.[contextPropertyName]}` notation. You can also use other SpEL functionality to build
more advanced expressions. Please refer to the [SSC](config-ssc.html) and [FoD](config-fod.html) sections for more information
about which vulnerability fields can be referenced in the expressions.
  
## Issue Submitter
All configuration files contain an implementation-specific bean definition that is responsible for submitting vulnerabilities
to the target system. Most of these implementations support a single `groupTemplateExpression` property, which can 
be used to group multiple vulnerabilities into a single issue to be submitted to the target system. The sample configuration
files use an expression that groups vulnerabilities by Fortify vulnerability category and name of the source file. For example,
all 'Cross-Site Scripting: Reflected' vulnerabilities in a single Sample.jsp file will be grouped together into a single issue.

## Issue Updater
Target systems for which bug state management is supported, can be configured with an implementation-specific bean for
updating the target system based on updated vulnerability information. For example, the description for an issue in the 
target system can be updated with current vulnerability information (see `fieldsToUpdateDuringStateManagement`
above), and issues in the target system can be automatically closed or re-opened based on current vulnerability state.

Most target systems for which bug state management is supported use workflow-based transitions for closing or re-opening
issues. Such implementations can be configured with the following properties:

- `transitionsForOpeningIssue` and `transitionsForClosingIssue`  
  These properties are both configured with a map. The map key contains an expression that is evaluated on the current
  state of the issue in the target system; usually this expression determines whether the issue is in a specific workflow
  state. If the issue needs to be re-opened or closed (based on current vulnerability state), and the current issue state
  matches an entry in the corresponding map (`transitionsForOpeningIssue` or `transitionsForClosingIssue`),
  the transition(s) as defined through the corresponding map value will be performed on the target issue. 
- `isIssueOpenableExpression` and `isIssueCloseableExpression`  
  Usually an issue will simply be re-opened or closed if a corresponding transitions exist (see above). However, with these
  two expressions you can optionally configure additional criteria to be met in order for an issue to be considered re-openable
  or closeable. For example, you can use this functionality to only re-open issues if the resolution field matches specific
  values.
     

