# Use Cases
Both FoD and SSC provide their own native bug tracker integrations, allowing to interactively submit vulnerabilities to bug trackers. SSC also supports custom-developed integrations to support other bug tracking systems. Depending on your requirements, these native integrations may be sufficient. 

However, FortifyBugTrackerUtility allows for more advanced use cases that the native integrations do not cater for. If you need any of the following, FortifyBugTrackerUtility may be a better choice than the native integrations:

- Full automation without requiring any user interaction
- Fields to be submitted to the external system are fully configurable
- More comprehensive and fully configurable bug state management
- Submit a single vulnerability to multiple external systems (for example bug tracker and risk management system)
- For FoD: Submit vulnerabilities to an on-premise system without requiring a direct connection from FoD to that system

Note that for SSC, FortifyBugTrackerUtility can even integrate with the native SSC bug tracker integrations. In this scenario, you use FortifyBugTrackerUtility for automatically selecting and grouping vulnerabilities, but the vulnerabilities are submitted through the native SSC bug tracker integration, and bug state management is performed by SSC.
