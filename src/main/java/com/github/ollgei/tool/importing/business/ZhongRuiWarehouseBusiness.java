package com.github.ollgei.tool.importing.business;

import java.util.List;

import com.github.ollgei.tool.importing.common.model.WarehouseModel;

public interface ZhongRuiWarehouseBusiness {
    void create(String token);
    void createStorespace(String token);
    void updateId(String token);
    void save(String code, List<WarehouseModel> models);
}
