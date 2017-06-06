package com.fortify.processrunner;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import com.fortify.util.spring.SpringContextUtil;

/**
 * This class tests whether the available configuration files can be successfully loaded by Spring.
 * @author Ruud Senden
 *
 */
public class ConfigFilesTest {
	@Test
	public void testConfigFiles() {
		String[] files = new File("processrunner-config").list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		for ( String file : files ) {
			GenericApplicationContext ctx = SpringContextUtil.loadApplicationContextFromFiles(true, "processrunner-config/"+file);
			System.out.println(ctx+": "+Arrays.asList(ctx.getBeanDefinitionNames()));
		}
	}
}
