# Fortify on Demand bug tracker integrations

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
| ALM Octane     | Yes     | Yes      | Yes      | Yes              | State transition comments are not yet supported |
| Microsoft TFS  | Yes     | Yes      | Yes      | Yes              | Additional state transitions may need to be configured |
| SSC Bug Trackers | No    | Yes      | Yes      | Performed by SSC | Sample configuration for SSC TFS bug tracker included, other SSC bug trackers require corresponding configuration files to be added |

For more information about configuring and running the utility, please see the documentation included with the binary distribution.
