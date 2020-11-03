package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.ollgei.base.commonj.gson.Gson;
import com.github.ollgei.base.commonj.gson.GsonBuilder;
import com.github.ollgei.base.commonj.gson.JsonElement;
import com.github.ollgei.base.commonj.gson.JsonParser;
import com.github.ollgei.boot.autoconfigure.httpclient.FeignClientManager;
import com.github.ollgei.tool.importing.business.ZhongRuiWarehouseBusiness;
import com.github.ollgei.tool.importing.common.model.WarehouseModel;
import com.github.ollgei.tool.importing.configuration.ToolImportingProperties;
import com.github.ollgei.tool.importing.dao.entity.EmployeeEntity;
import com.github.ollgei.tool.importing.dao.entity.StorehouseEntity;
import com.github.ollgei.tool.importing.dao.service.EmployeeService;
import com.github.ollgei.tool.importing.dao.service.StorehouseService;
import lombok.extern.slf4j.Slf4j;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Service
@Slf4j
public class ZhongRuiWarehouseBusinessImpl implements ZhongRuiWarehouseBusiness, InitializingBean {

    private Map<String, List<StorehouseEntity>> caches = new ConcurrentHashMap<>();

    private StorehouseService storehouseService;

    private EmployeeService employeeService;

    private Gson gson = new GsonBuilder().create();

    private FeignClientManager clientManager;

    private ToolImportingProperties properties;

    public ZhongRuiWarehouseBusinessImpl(ToolImportingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void create(String token) {
        final List<StorehouseEntity> list = storehouseService.list(
                Wrappers.<StorehouseEntity>query().
                        eq(StorehouseEntity.COL_CREATED, "2")
                        //.eq(StorehouseEntity.COL_CODE, "361000020001")
                );
        int i = 0;
        final List<StorehouseEntity> fails = new ArrayList<>();
        for (StorehouseEntity entity : list) {
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);
            final JsonElement request = JsonParser.parseString(entity.getJson());
            log.info("创建仓库请求的数据:" + request.toString());
            final JsonElement response = clientManager.postForJson(properties.getFdUrl() + "/warehouse-controller/save-one-warehouse-area", headers, request);
            if (!response.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
                fails.add(entity);
                continue;
            }
            log.info("创建仓库返回的数据:" + response.toString());
            StorehouseEntity entity1 = new StorehouseEntity();
            entity1.setId(entity.getId());
            entity1.setCreated("1");
            storehouseService.updateById(entity1);
            i++;
        }
        log.info("成功导入:{}条", i);
        log.info("失败的数据", fails);
    }

    @Override
    public void save(String code, List<WarehouseModel> models) {
        int i = caches.containsKey(code) ? caches.get(code).size() : 0;
        for (WarehouseModel model : models) {
            String newCode = String.format("%s%04d", model.getCode(), i + 1);
            final WarehouseRequest request = buildWarehouseRequest(model, newCode);

            StorehouseEntity entity = new StorehouseEntity();
            entity.setCode(newCode);
            entity.setName(model.getName());
            if (request == null) {
                entity.setJson(gson.toJson(model));
                entity.setCreated("3");
            } else {
                entity.setJson(gson.toJson(request));
                entity.setCreated("2");
            }

            storehouseService.save(entity);
            i++;
        }
    }

    private WarehouseRequest buildWarehouseRequest(WarehouseModel model, String code) {
        WarehouseRequest request = new WarehouseRequest();
        request.setAddress(model.getAddr());
        request.setCityCode(model.getCityCode());
        request.setCityName(model.getCityName());
        request.setCode(code);
        request.setCreatedById("0");
        request.setName(model.getName());
        request.setParentId("");
        request.setProvinceCode(model.getProviceCode());
        request.setProvinceName(model.getProviceName());

        final EmployeeEntity adminEntity = employeeService.getOne(Wrappers.<EmployeeEntity>query().
                eq(EmployeeEntity.COL_LOGIN_NAME, model.getAdminNo().trim().toUpperCase()));
        if (adminEntity == null) {
            return null;
        }
        List<WarehouseRequest.Employee> employees = new ArrayList<>();

        final WarehouseRequest.Employee adminEmployee = buildWarehouseEmployee(adminEntity, true);
        employees.add(adminEmployee);

        if (StringUtils.hasText(model.getEmployeeNo())) {
            final EmployeeEntity employeeEntity = employeeService.getOne(Wrappers.<EmployeeEntity>query().
                    eq(EmployeeEntity.COL_LOGIN_NAME, model.getEmployeeNo().trim().toUpperCase()));
            if (employeeEntity == null) {
                return null;
            }
            employees.add(buildWarehouseEmployee(employeeEntity, false));
        }
        request.setEmployees(employees);
        return request;
    }

    private WarehouseRequest.Employee buildWarehouseEmployee(EmployeeEntity entity, boolean isAdmin) {
        WarehouseRequest.Employee employee = new WarehouseRequest.Employee();
        if (entity == null) {
            log.warn("employee为空");
        }
        employee.setId(entity.getSid());
        employee.setLoginName(entity.getLoginName());
        employee.setName(entity.getZhName());
        employee.setIsAdmin(isAdmin);
        return employee;
    }

    private void initWarehouse() {
        final List<StorehouseEntity> list =
                storehouseService.list(Wrappers.<StorehouseEntity>query().eq(StorehouseEntity.COL_CREATED, "1"));
        for (StorehouseEntity entity : list) {
            if (entity.getCode() != null) {
                if (entity.getCode().length() < 8) {
                    continue;
                }
                final String code = entity.getCode().substring(0, 8);
                if (caches.containsKey(code)) {
                    caches.get(code).add(entity);
                } else {
                    final ArrayList<StorehouseEntity> arr = new ArrayList<>();
                    arr.add(entity);
                    caches.put(code, arr);
                }
            }
        }
        log.info("初始化的仓库数据:{}", caches);
    }


    @Autowired
    public void setStorehouseService(StorehouseService storehouseService) {
        this.storehouseService = storehouseService;
    }

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Autowired
    public void setClientManager(FeignClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public void afterPropertiesSet()  {
        initWarehouse();
    }
}
