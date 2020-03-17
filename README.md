# FortifyBugTrackerUtility

Disclaimer
====
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.

Introduction
====
The Maven projects in this repository make up a utility for submitting vulnerabilities from Fortify on Demand (FoD) 
and Fortify Software Security Center (SSC) to bug trackers and other external systems. The following table
lists the currently supported functionalities.

| Target System | From FoD | From SSC | Grouping | State Management | Remarks |
| ------------- | -------- | -------- |--------- | ---------------- | ------- |
| RSA Archer    | Yes      | Yes      | No       | No               | Currently only supports text and value list field types |
| CSV File      | Yes      | Yes      | No       | No               | By default, a separate output file is written for each application version/release. All relevant vulnerabilities are exported on each run, overwriting any existing files |
| Atlassian Jira | Yes     | Yes      | Yes      | Yes              | |
| ALM Octane     | Yes     | Yes      | Yes      | Yes              | |
| Microsoft TFS  | Yes     | Yes      | Yes      | Yes              | Additional state transitions may need to be configured |
| SSC Bug Trackers | No    | Yes      | Yes      | Performed by SSC | Sample configuration for SSC TFS bug tracker included, other SSC bug trackers require corresponding configuration files to be added |

For more information about configuring and running the utility, please see the documentation included with the binary distribution.

Note that this utility provides functionality that is similar to the bug tracker integrations
provided with FoD and SSC, however it takes a different approach than these product-provided 
integrations (with various advantages and disadvantages as described in the documentation).
This is a stand-alone utility that can be invoked manually from the command line, or automatically 
from for example build jobs or as a scheduled task. In particular, note that the utility jar file 
cannot be loaded as a bug tracker plugin in either FoD or SSC.


Building from source
====

Prerequisites
----

### Tools
In order to retrieve the source code and build the project, you will need to have the following tools installed:

* Git client
* Maven 3.x

### Fortify Client API dependencies
Development/snapshot versions of this project (i.e. the master branch or other non-release branches) may depend on
a snapshot version of fortify-client-api. You can verify this by searching the root pom.xml file in this project 
for the following dependency declaration:

```xml
<dependency>
	<groupId>com.fortify.client.api</groupId>
	<artifactId>client-api-root</artifactId>
	<version>5.2-SNAPSHOT</version>
	<type>pom</type>
	<scope>import</scope>
</dependency>
```

If, as illustrated in this example, the version of this dependency ends with `-SNAPSHOT`, you will first need to 
build this dependency and install the corresponding artifacts in your local Maven repository by following these steps:

* `git clone https://github.com/fortify-ps/fortify-client-api.git`
* `cd fortify-client-api`
* `mvn clean install`

Notes:

* Non-snapshot versions of fortify-client-api are available at https://github.com/fortify-ps/FortifyMavenRepo,
  which is automatically included during the build of this project. As such, non-snapshot versions of 
  fortify-client-api do not need to be built and installed manually.
* By nature, snapshot versions are unstable and may change at any time. As such, you may need to repeat the
  steps described above if there are any changes in fortify-client-api. Also, there is no guarantee that this 
  project will build without errors with the latest snapshot version of fortify-client-api. In this case, you 
  may need to check out a specific commit of fortify-client-api. 
  
Building the project
----
Once all prerequisites have been met, you can use the following commands to build this project:

* `git clone https://github.com/fortify-ps/FortifyBugTrackerUtility.git`
* `cd FortifyBugTrackerUtility`
* `git checkout [branch or tag that you want to build]`
* `mvn clean package`

Once completed, build output like executable JAR file, sample configuration files, and the 
binary distribution zip file, can be found in the bugtracker/target directory.


# Licensing

See [LICENSE.TXT](LICENSE.TXT)

