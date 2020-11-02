package com.github.ollgei.tool.importing.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ToolImportingProperties.class)
public class ToolImportingConfiguration {

}
