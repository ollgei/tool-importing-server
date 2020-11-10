package com.github.ollgei.tool.importing.business;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ollgei.tool.importing.common.model.WarehouseModel;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Service
public class ZhongRuiBusiness {

    private ZhongRuiEmployeeBusiness employeeBusiness;

    private ZhongruiProviceCityBusiness proviceCityBusiness;

    private ZhongRuiWarehouseBusiness warehouseBusiness;

    public void initEmployee(String token) {
        employeeBusiness.init(token);
    }

    public void fetchProvince(String accessToken) {
        proviceCityBusiness.fetchProvinces(accessToken);
    }

    public String fetchProvinceCode(String accessToken, String name) {
        return proviceCityBusiness.fetchProvinceCode(accessToken, name);
    }

    public void fetchCity(String accessToken, String provinceCode) {
        proviceCityBusiness.fetchCities(accessToken, provinceCode);
    }

    public String fetchCityCode(String accessToken, String provinceCode, String name) {
        return proviceCityBusiness.fetchCityCode(accessToken, provinceCode, name);
    }

    public void saveWarehouse(String code, List<WarehouseModel> models) {
        warehouseBusiness.save(code, models);
    }

    public void createWarehouse(String token) {
        warehouseBusiness.create(token);
    }

    public void updateWarehouseId(String token) {
        warehouseBusiness.updateId(token);
    }

    public void updateFail() {
        warehouseBusiness.updateFail();
    }

    public void createStorespace(String token) {
        warehouseBusiness.createStorespace(token);
    }

    @Autowired
    public void setEmployeeBusiness(ZhongRuiEmployeeBusiness employeeBusiness) {
        this.employeeBusiness = employeeBusiness;
    }

    @Autowired
    public void setProviceCityBusiness(ZhongruiProviceCityBusiness proviceCityBusiness) {
        this.proviceCityBusiness = proviceCityBusiness;
    }

    @Autowired
    public void setWarehouseBusiness(ZhongRuiWarehouseBusiness warehouseBusiness) {
        this.warehouseBusiness = warehouseBusiness;
    }
}
