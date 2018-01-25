# Default values for command line options
Every configuration file can contain a util:map bean with id `contextProperties`. This bean can contain a map of
default values for one or more action-specific command line options. In this map, the key specifies the name of the 
command line option, and the value specifies the corresponding default value. For example, you can use this approach
for defining default values for source and target system URL's and credentials, such that these no longer need to be
manually specified on the command line.

For example, you could define default SSC URL and credentials using the following bean definition in your configuration
file. Note that all example configuration files already contain a contextProperties bean, so you can just add the 
entries to the existing bean definition.

```xml
<util:map id="contextProperties">
	<entry key="SSCBaseUrl" value="https://fortify-ssc.mycompany.com/ssc"/>
	<entry key="SSCAuthToken" value="688d24c3-a7ef-4ea1-b76f-5ee666393ebc"/>
</util:map>
```


