# Troubleshooting

This page describes some troubleshooting tips.

- JRE/JVM crash: There is a known JVM defect in many Java versions which impacts the ability to use command line arguments starting with the 'J' character. See: [Bug ID 8132379](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8132379)
    - Work around this issue by preceding all options that start with the 'J' character with double dashes instead of a single dash, for example `--JiraProjectKey` instead of `-JiraProjectKey`.


- java.lang.OutOfMemoryError: FortifyBugTrackerUtility uses temporary files while grouping vulnerabilities, so even with limited amounts of memory, vulnerability processing should usually not cause any out of memory errors. However, in case you do run into these kinds of errors, the following approaches can be tried:
    - Increase the amount of available memory by adding the java `-Xmx<size>` option, for example `java -Xmx4G -jar ...`
    - Add/update filtering settings in the configuration file to match a lower number of vulnerabilities


- SSL-related errors: Java places a strict policy on SSL certificates. For example, connections to HTTPS endpoints will fail if the endpoint provides a certificate that is not signed by a known CA, or if the host name in the certificate does not match the host name that the utility is trying to connect to. When SSL-related errors occur:
    - Make sure that the configured URL matches the URL provided in the server certificate (for example you may need to use host name instead of IP address or DNS alias)
    - If the server uses a self-signed certificate, or a certificate signed by an unknown CA, you will need to import the certificate into the Java trust store. To do so, use your browser to export the server certificate, and then import it to the Java cacerts file as follows: `"<JRE_HOME>\bin\keytool" -import -trustcacerts -noprompt -keystore "<JRE_HOME>\lib\security\cacerts" -storepass changeit -alias "<hostname>" -file "<SavedCertificate.cer>"`
    
- Error `Conflict detected! There is a newer revision of the selected issue on the server.`:
     - Please see the `enableRevisionWorkAround` property in [`SSCSourceVulnerabilitiesConfiguration`](config-SSCSourceVulnerabilitiesConfiguration.html)
     
     
