package com.github.ollgei.tool.importing.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Service
public class ZhongRuiBusiness {

    private ZhongRuiEmployeeBusiness employeeBusiness;

    private ZhongruiProviceCityBusiness proviceCityBusiness;

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

    @Autowired
    public void setEmployeeBusiness(ZhongRuiEmployeeBusiness employeeBusiness) {
        this.employeeBusiness = employeeBusiness;
    }

    @Autowired
    public void setProviceCityBusiness(ZhongruiProviceCityBusiness proviceCityBusiness) {
        this.proviceCityBusiness = proviceCityBusiness;
    }
}
