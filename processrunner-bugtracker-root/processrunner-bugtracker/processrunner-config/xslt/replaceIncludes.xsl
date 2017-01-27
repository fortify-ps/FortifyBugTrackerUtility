<xsl:transform version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:b="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:lang="http://www.springframework.org/schema/lang" xmlns:util="http://www.springframework.org/schema/util"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.2.xsd
		http://www.springframework.org/schema/lang http://www.springframework.org/schema/lang/spring-lang-4.2.xsd
		http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-4.2.xsd">
	<xsl:param name="processrunnerConfigLocation"/>
	
	<xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="b:import">
    	<xsl:copy-of select="document(concat($processrunnerConfigLocation,@resource))/b:beans/node()"/>
    </xsl:template>
  

</xsl:transform>