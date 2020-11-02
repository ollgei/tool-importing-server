package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.List;

import lombok.Data;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Data
final class EmployeeResponse {
    private Integer code;
    private List<EmployeeData> data;
    private String message;
    private Boolean success;

    @Data
    public static class EmployeeData {
        private String id;
        private String label;
        private Boolean leaf;
        private String name;
        private String nodeType;
        private Integer organizationCategory;
        private String parentId;
        private Boolean selectable;
        private List<EmployeeData> children;
    }
}
