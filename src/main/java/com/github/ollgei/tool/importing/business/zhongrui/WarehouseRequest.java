package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.List;

import lombok.Data;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Data
final class WarehouseRequest {

    private String address;

    private String cityCode;

    private String cityName;

    private String code;

    private String createdById;

    private String name;

    private String parentId;

    private String provinceCode;

    private String provinceName;

    private List<Employee> employees;

    @Data
    static class Employee {
        private String id;
        private String name;
        private String loginName;
        private Boolean isAdmin;
    }

}
