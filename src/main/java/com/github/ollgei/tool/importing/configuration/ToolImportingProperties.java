package com.github.ollgei.tool.importing.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "ollgei.tool.importing")
public class ToolImportingProperties {

    private UserCenter ucm;

    private String provinceUrl;
    private String cityUrl;
    private String fdUrl;

    @Data
    public static class UserCenter {
        private String url;
        private String deptId;
    }

}
