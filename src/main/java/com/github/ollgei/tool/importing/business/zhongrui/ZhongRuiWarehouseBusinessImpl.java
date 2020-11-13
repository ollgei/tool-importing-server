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
import com.github.ollgei.base.commonj.gson.JsonArray;
import com.github.ollgei.base.commonj.gson.JsonElement;
import com.github.ollgei.base.commonj.gson.JsonObject;
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
 *  1 已经创建好仓库和库位
 *  3 有问题数据
 *  2 准备数据没有问题
 *  创建仓库 (2 -> 6)
 *  更新仓库ID(6 -> 4)
 *  创建库位(4 -> 5)
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
//                        .eq(StorehouseEntity.COL_CODE, "361000020001")
                );
        int i = 0;
        final List<StorehouseEntity> fails = new ArrayList<>();
        for (StorehouseEntity entity : list) {
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);
            final JsonElement request = JsonParser.parseString(entity.getJson());
            log.info("创建仓库请求的数据:" + request.toString());
            final JsonElement response = clientManager.postForJson(properties.getFdUrl() + "/warehouse-controller/save-one-warehouse-area", headers, request);
            log.info("创建仓库返回的数据:" + response.toString());
            if (!response.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
                fails.add(entity);
                continue;
            }
            StorehouseEntity entity1 = new StorehouseEntity();
            entity1.setId(entity.getId());
            entity1.setCreated("6");
            storehouseService.updateById(entity1);
            i++;
        }
        log.info("成功导入:{}条", i);
        log.info("失败的数据", fails);
    }

    @Override
    public void createStorespace(String token) {
        final List<StorehouseEntity> list = storehouseService.list(
                Wrappers.<StorehouseEntity>query().
                        eq(StorehouseEntity.COL_CREATED, "4")
//                .eq(StorehouseEntity.COL_CODE, "361000020001")
        );
        int i = 0;
        final List<StorehouseEntity> fails = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        for (StorehouseEntity entity : list) {
            //{"name":"测试","code":"K1234567qwer","createdById":"dacg","warehouseCode":"12345678qwer",
            //"warehouseId":"CZWI0000000035"}
            String code = entity.getCode();
            JsonObject request = new JsonObject();
            request.addProperty("name", entity.getName());
            request.addProperty("createdById", "0");
            request.addProperty("code", "K" + code.substring(0, 6) + "0" +
                    code.substring(code.length() - 4));
            request.addProperty("warehouseCode", entity.getCode());
            request.addProperty("warehouseId", entity.getWarehouseId());

            log.info("创建库位请求的数据:" + request.toString());
            final JsonElement response = clientManager.postForJson(properties.getFdUrl() + "/storespace-controller/save", headers, request);
            log.info("创建库位返回的数据:" + response.toString());
            if (!response.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
                fails.add(entity);
                continue;
            }
            StorehouseEntity entity1 = new StorehouseEntity();
            entity1.setId(entity.getId());
            entity1.setCreated("5");
            storehouseService.updateById(entity1);
            i++;

        }
        log.info("成功导入:{}条", i);
        log.info("失败的数据：{}", fails);
    }

    @Override
    public void updateId(String token) {
        final List<StorehouseEntity> list = storehouseService.list(
                Wrappers.<StorehouseEntity>query().
                        eq(StorehouseEntity.COL_CREATED, "6")
//                .eq(StorehouseEntity.COL_CODE, "361000020001")
        );
        int i = 0;
        final List<StorehouseEntity> fails = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        for (StorehouseEntity entity : list) {
            if (StringUtils.hasText(entity.getCode()) && StringUtils.hasText(entity.getName())) {
                JsonElement request = buildQuery(entity.getCode());
                log.info("查询仓库请求的数据:" + request.toString());
                final JsonElement response = clientManager.postForJson(properties.getFdUrl() + "/warehouse-controller/warehouses/page", headers, request);
                log.info("查询仓库返回的数据:" + response.toString());
                if (!response.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
                    fails.add(entity);
                    continue;
                }
                //{"status":0,"data":{"total":1,"pageIndex":1,"records":[{"id":"CZWI0000000760","code":"230000010001","name":"黑龙江一级仓","provinceName":"黑龙江省","address":"黑龙江省哈尔滨市道里区群力第六大道与朗江路交口恒祥空间10栋1单元2402","stockNums":0,"adminName":"孟祥福"}],"pageSize":10},"success":true}
                StorehouseEntity entity1 = new StorehouseEntity();
                entity1.setId(entity.getId());
                entity1.setCreated("4");
                entity1.setWarehouseId(response.getAsJsonObject().
                        getAsJsonObject("data").
                        getAsJsonArray("records").
                        get(0).getAsJsonObject().
                        getAsJsonPrimitive("id").getAsString()
                );
                storehouseService.updateById(entity1);
                i++;
            } else {
                log.warn("Code为空");
            }
        }
        log.info("成功导入:{}条", i);
        log.info("失败的数据：{}", fails);
    }

    private JsonElement buildQuery(String code) {
//        {"pageSize":10,"pageIndex":1,"sort":[],
//            "filters":[{"field":"code","op":"cn","term":"eqeqe"}],
//            "filter":{"op":"and","groups":[],
//            "rules":[{"field":"code","op":"cn","data":"eqeqe"},{"field":"name","op":"cn","data":""},
//            {"field":"pro","op":"cn","data":""},{"field":"city","op":"cn","data":""}]}}
        JsonObject object = new JsonObject();
        object.addProperty("pageSize", 10);
        object.addProperty("pageIndex", 1);
        object.add("sort", new JsonArray());

        JsonArray filters = new JsonArray();
        final JsonObject filtersEle = new JsonObject();
        filtersEle.addProperty("field", "code");
        filtersEle.addProperty("op", "cn");
        filtersEle.addProperty("term", code);
        filters.add(filtersEle);
        object.add("filters", filters);

        final JsonObject filter = new JsonObject();
        filter.addProperty("op", "and");
        filter.add("groups", new JsonArray());

        JsonArray rules = new JsonArray();
        final JsonObject filterEle = new JsonObject();
        filterEle.addProperty("field", "code");
        filterEle.addProperty("op", "cn");
        filterEle.addProperty("data", code);
        rules.add(filterEle);
        filter.add("rules", rules);

        object.add("filter", filter);
        return object;
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

    @Override
    public void updateFail() {
        final List<StorehouseEntity> list = storehouseService.list(
                Wrappers.<StorehouseEntity>query().
                        eq(StorehouseEntity.COL_CREATED, "3")
        );
        list.forEach(s -> {
            WarehouseModel model = gson.fromJson(s.getJson(), WarehouseModel.class);
            final WarehouseRequest request = buildWarehouseRequest(model, s.getCode());

            StorehouseEntity entity = new StorehouseEntity();
            entity.setId(s.getId());
            entity.setCode(s.getCode());
            entity.setName(model.getName());
            if (request == null) {
                return;
            } else {
                entity.setJson(gson.toJson(request));
                entity.setCreated("2");
            }

            storehouseService.updateById(entity);
        });
    }

    @Override
    public void remove(String token) {

        final List<StorehouseEntity> list = storehouseService.list(
                Wrappers.<StorehouseEntity>query().
                        eq(StorehouseEntity.COL_CREATED, "5")
        );
        final List<StorehouseEntity> fails = new ArrayList<>();

        int i = 0;
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        for (StorehouseEntity s : list) {
            final String url = "http://fd-prod.lunz.cn:7400/v1/api/warehouse-controller/remove-warehouse-are?deletedById=0&ids=" + s.getWarehouseId();
            log.info("删除仓库请求的数据:" + url);
            final JsonElement response = clientManager.getForJson(url, headers);
            log.info("删除仓库返回的数据:" + response.toString());
            if (!response.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
                fails.add(s);
                continue;
            }
            StorehouseEntity entity1 = new StorehouseEntity();
            entity1.setId(s.getId());
            entity1.setCreated("99");
            storehouseService.updateById(entity1);
            i++;
        }

        log.info("成功删除:{}条", i);
        log.info("失败的数据：{}", fails);
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

        final String adminNo = model.getAdminNo().trim().toUpperCase();
        final EmployeeEntity adminEntity = employeeService.getOne(Wrappers.<EmployeeEntity>query().
                eq(EmployeeEntity.COL_LOGIN_NAME, adminNo));
        if (adminEntity == null) {
            return null;
        }
        List<WarehouseRequest.Employee> employees = new ArrayList<>();

        final WarehouseRequest.Employee adminEmployee = buildWarehouseEmployee(adminEntity, true);

        if (StringUtils.hasText(model.getEmployeeNo())) {
            final String employeeNo = model.getEmployeeNo().trim().toUpperCase();
            if (!adminNo.equals(employeeNo)) {
                adminEmployee.setIfOwner(false);
                final EmployeeEntity employeeEntity = employeeService.getOne(Wrappers.<EmployeeEntity>query().
                        eq(EmployeeEntity.COL_LOGIN_NAME, model.getEmployeeNo().trim().toUpperCase()));
                if (employeeEntity == null) {
                    return null;
                }
                WarehouseRequest.Employee employee =
                        buildWarehouseEmployee(employeeEntity, false);
                employee.setIfOwner(true);
                employees.add(employee);
            }
        }
        employees.add(adminEmployee);
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
        employee.setIfOwner(isAdmin);
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

    public static void main(String[] args) {
        String code = "12321";
        JsonObject object = new JsonObject();
        object.addProperty("pageSize", 10);
        object.addProperty("pageIndex", 1);
        object.add("sort", new JsonArray());

        JsonArray filters = new JsonArray();
        final JsonObject filtersEle = new JsonObject();
        filtersEle.addProperty("field", "code");
        filtersEle.addProperty("op", "cn");
        filtersEle.addProperty("term", code);
        filters.add(filtersEle);
        object.add("filters", filters);

        final JsonObject filter = new JsonObject();
        filter.addProperty("op", "and");
        filter.add("groups", new JsonArray());

        JsonArray rules = new JsonArray();
        final JsonObject filterEle = new JsonObject();
        filterEle.addProperty("field", "code");
        filterEle.addProperty("op", "cn");
        filterEle.addProperty("data", code);
        rules.add(filterEle);
        filter.add("rules", rules);

        object.add("filter", filter);
        System.out.println(object);

//        String s = "{\"status\":0,\"data\":{\"total\":1,\"pageIndex\":1,\"records\":[{\"id\":\"CZWI0000000760\",\"code\":\"230000010001\",\"name\":\"黑龙江一级仓\",\"provinceName\":\"黑龙江省\",\"address\":\"黑龙江省哈尔滨市道里区群力第六大道与朗江路交口恒祥空间10栋1单元2402\",\"stockNums\":0,\"adminName\":\"孟祥福\"}],\"pageSize\":10},\"success\":true}";
//
//        JsonElement response = JsonParser.parseString(s);
//        System.out.println(response.getAsJsonObject().
//                getAsJsonObject("data").
//                getAsJsonArray("records").
//                get(0).getAsJsonObject().
//                getAsJsonPrimitive("id").getAsString());
        code = "152500020002";
        System.out.println("K" + code.substring(0, 6) + "0" +
                code.substring(code.length() - 4));
    }
}
