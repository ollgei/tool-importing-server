package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.ArrayList;
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
import com.github.ollgei.tool.importing.business.ZhongRuiWarehouseBusiness;
import com.github.ollgei.tool.importing.common.model.WarehouseModel;
import com.github.ollgei.tool.importing.dao.entity.EmployeeEntity;
import com.github.ollgei.tool.importing.dao.entity.StorehouseEntity;
import com.github.ollgei.tool.importing.dao.service.EmployeeService;
import com.github.ollgei.tool.importing.dao.service.StorehouseService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZhongRuiWarehouseBusinessImpl implements ZhongRuiWarehouseBusiness, InitializingBean {

    private Map<String, List<StorehouseEntity>> caches = new ConcurrentHashMap<>();

    private StorehouseService storehouseService;

    private EmployeeService employeeService;

    private Gson gson = new GsonBuilder().create();

    @Override
    public void save(String code, List<WarehouseModel> models) {
        int i = caches.containsKey(code) ? caches.get(code).size() : 0;
        for (WarehouseModel model : models) {
            final WarehouseRequest request = buildWarehouseRequest(model, i + 1);

            StorehouseEntity entity = new StorehouseEntity();
            entity.setName(model.getName());
            entity.setCode(model.getCode());
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

    private WarehouseRequest buildWarehouseRequest(WarehouseModel model, int index) {
        WarehouseRequest request = new WarehouseRequest();
        request.setAddress(model.getAddr());
        request.setCityCode(model.getCityCode());
        request.setCityName(model.getCityName());
        request.setCode(String.format("%s%04d", model.getCode(), index));
        request.setCreatedById("1");
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

    @Override
    public void afterPropertiesSet()  {
        initWarehouse();
    }
}
