package com.github.ollgei.tool.importing.business;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
public interface ZhongruiProviceCityBusiness {

    /**
     * desc.
     * @param accessToken
     * @return
     */
    void fetchProvinces(String accessToken);

    /**
     * desc.
     * @param accessToken
     * @param name
     * @return
     */
    String fetchProvinceCode(String accessToken, String name);

    /**
     * desc.
     * @return
     */
    void fetchCities(String accessToken, String provinceCode);

    String fetchCityCode(String accessToken, String provinceCode, String name);

}
