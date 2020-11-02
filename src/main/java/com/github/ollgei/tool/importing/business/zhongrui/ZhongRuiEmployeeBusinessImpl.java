package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ollgei.base.commonj.gson.JsonObject;
import com.github.ollgei.base.commonj.utils.CommonHelper;
import com.github.ollgei.boot.autoconfigure.httpclient.FeignClientManager;
import com.github.ollgei.tool.importing.business.ZhongRuiEmployeeBusiness;
import com.github.ollgei.tool.importing.configuration.ToolImportingProperties;
import com.github.ollgei.tool.importing.dao.entity.EmployeeEntity;
import com.github.ollgei.tool.importing.dao.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Slf4j
@Service
public class ZhongRuiEmployeeBusinessImpl implements ZhongRuiEmployeeBusiness {

    private FeignClientManager feignClientManager;

    private ToolImportingProperties properties;

    private EmployeeService employeeService;

    @Autowired
    public ZhongRuiEmployeeBusinessImpl(ToolImportingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void init(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        final EmployeeResponse repsonse = feignClientManager.getForType(buildTopOrgUrl(), headers, EmployeeResponse.class);
        log.info("获取的数据:" + repsonse.toString());
        repsonse.getData().stream().filter(e -> !e.getId().equals("cdbbe0e4-0461-4ae3-9495-38e03b08c319")).forEach(e -> fetchOrgTree(token, e.getId(), e.getName()));
//        fetchOrgTree(token, repsonse.getData().get(0).getId(), repsonse.getData().get(0).getName());
    }

    private void fetchUser(String token, String parentId, String parentName) {
        CommonHelper.sleepS(2);
        log.info("开始获取用户[{}-{}]的数据", parentId, parentName);
        JsonObject body = new JsonObject();
        body.addProperty("pageIndex", 1);
        body.addProperty("pageSize", 1000);
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        UserReponse repsonse = feignClientManager.postForType(buildUserUrl(parentId), headers, body, UserReponse.class);
        repsonse.getData().stream().map(u -> createEmployeeEntity(u, parentId, parentName)).
                forEach(u -> employeeService.save(u));
        log.info("获取用户的数据:" + repsonse.toString());
    }

    private EmployeeEntity createEmployeeEntity(UserReponse.UserData data, String parentId, String parentName ) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setSid(data.getId());
        entity.setLoginName(data.getLoginName());
        entity.setZhName(data.getName());
        entity.setDeptId(parentId);
        entity.setDeptName(parentName);
        return entity;
    }

    private void fetchOrgTree(String token, String parentId, String parentName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        log.info("开始获取组织架构[{}]的数据", parentId);
        final EmployeeResponse repsonse = feignClientManager.getForType(buildOrgTreeUrl(parentId), headers, EmployeeResponse.class);
        log.info("获取组织架构的数据:" + repsonse.toString());
        repsonse.getData().stream().forEach(e -> walkOrgTree(token, e));
        fetchUser(token, parentId, parentName);
    }

    private void walkOrgTree(String token, EmployeeResponse.EmployeeData employeeData) {
        final List<EmployeeResponse.EmployeeData> children = employeeData.getChildren();
        if (!employeeData.getLeaf().booleanValue() && children != null && children.size() > 0) {
            children.forEach(e -> walkOrgTree(token, e));
        }
        fetchUser(token, employeeData.getId(), employeeData.getName());
    }

    private String buildUserUrl(String parentId) {
        StringJoiner joiner = new StringJoiner("/");
        joiner.add(properties.getUcm().getUrl());
        joiner.add("usercenter/getUserListByDeptId");
        joiner.add(parentId);
        return joiner.toString();
    }

    private String buildOrgTreeUrl(String parentId) {
        StringJoiner joiner = new StringJoiner("/");
        joiner.add(properties.getUcm().getUrl());
        joiner.add("organization/getOrganizationTreeById");
        joiner.add(parentId);
        return joiner.toString();
    }

    private String buildTopOrgUrl() {
        StringJoiner joiner = new StringJoiner("/");
        joiner.add(properties.getUcm().getUrl());
        joiner.add("organization/getTopOrgListByOrganizationType");
        joiner.add(properties.getUcm().getDeptId());
        return joiner.toString();
    }


    @Autowired
    public void setFeignClientManager(FeignClientManager feignClientManager) {
        this.feignClientManager = feignClientManager;
    }

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
}
