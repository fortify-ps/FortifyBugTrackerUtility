# SSC Token Definition
The utility can authenticate with SSC using either username/password, or using an authentication token. To use token-based authentication, you will need to add a token definition to SSC's WEB-INF/internal/serviceContext.xml, and then generate a corresponding token using the following command:

```
FortifyClient token -gettoken FortifyBugTrackerUtility -url http://<ssc host:port>/ssc ...
```

Below is the token definition to be added to serviceContext.xml. Notes:

- The FortifyBugTrackerUtility configuration files may contain settings to request additional information from SSC (for example through the extraVulnerabilityData property). If your customized configuration requests additional data from SSC, these extra URL's will need to be added to the token definition. 
- Whenever you make changes to the token definition, you will need to restart SSC and use the FortifyClient command to generate a new token.
- In SSC 18.10+, instead of using the FortifyClient command, you can also generate tokens from within the SSC user interface.


```xml
	<bean id="FortifyBugTrackerUtility" class="com.fortify.manager.security.ws.AuthenticationTokenSpec">
		<property name="key" value="FortifyBugTrackerUtility"/>
		<property name="maxDaysToLive" value="90" />
		<property name="actionPermitted">
			<list value-type="java.lang.String">
				<value>GET=/api/v\d+/attributeDefinitions</value>
				<value>GET=/api/v\d+/customTags</value>
				<value>GET=/api/v\d+/issueDetails/\d+</value>
				<value>GET=/api/v\d+/projectVersions</value>
				<value>GET=/api/v\d+/projectVersions/\d+/attributes</value>
				<value>GET=/api/v\d+/projectVersions/\d+/bugfilingrequirements</value>
				<value>GET=/api/v\d+/projectVersions/\d+/bugtracker</value>
				<value>GET=/api/v\d+/projectVersions/\d+/customTags</value>
				<value>GET=/api/v\d+/projectVersions/\d+/issues</value>
				<value>POST=/api/v\d+/projectVersions/\d+/bugfilingrequirements/action</value>
				<value>POST=/api/v\d+/projectVersions/\d+/issues/action</value>
				<value>PUT=/api/v\d+/projectVersions/\d+/bugfilingrequirements</value>
				<value>PUT=/api/v\d+/projectVersions/\d+/issueSearchOptions</value>
			</list>
		</property>
		<property name="terminalActions">
			<list value-type="java.lang.String">
				<value>InvalidateTokenRequest</value>
				<value>DELETE=/api/v\d+/auth/token</value>
			</list>
		</property>
	</bean>
```
