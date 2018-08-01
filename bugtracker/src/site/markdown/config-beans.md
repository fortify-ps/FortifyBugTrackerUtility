# Beans element
All configuration elements are wrapped inside a `<beans`> element. Each configuration file
must start with the following `<beans>` tag, and end with a corresponding `</beans>` closing tag:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:lang="http://www.springframework.org/schema/lang" xmlns:util="http://www.springframework.org/schema/util"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.2.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
		http://www.springframework.org/schema/lang http://www.springframework.org/schema/lang/spring-lang-4.2.xsd
		http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-4.2.xsd">
```

