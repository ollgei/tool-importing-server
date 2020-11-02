package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.ollgei.base.commonj.gson.Gson;
import com.github.ollgei.base.commonj.gson.GsonBuilder;
import com.github.ollgei.base.commonj.gson.JsonArray;
import com.github.ollgei.base.commonj.gson.JsonElement;
import com.github.ollgei.base.commonj.gson.JsonObject;
import com.github.ollgei.boot.autoconfigure.httpclient.FeignClientManager;
import com.github.ollgei.tool.importing.business.ZhongruiProviceCityBusiness;
import com.github.ollgei.tool.importing.configuration.ToolImportingProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Service
@Slf4j
public class ZhongruiProviceCityBusinessImpl implements ZhongruiProviceCityBusiness {

    private ToolImportingProperties properties;

    private FeignClientManager feignClientManager;

    private Gson gson = new GsonBuilder().create();

    private Map<String, JsonElement> cacheProvince = new ConcurrentHashMap<>();
    private Map<String, JsonElement> cacheCity = new ConcurrentHashMap<>();

    @Autowired
    public ZhongruiProviceCityBusinessImpl(ToolImportingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void fetchProvinces(String accessToken) {
        log.info("开始获取省份数据");

        if (cacheProvince.size() > 0) {
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        final JsonElement response = feignClientManager.getForJson(properties.getProvinceUrl(), headers);
        log.info("获取省份的数据:" + response.toString());
        if (response.isJsonArray()) {
            final JsonArray array = response.getAsJsonArray();
            for (JsonElement element : array) {
                cacheProvince.put(element.getAsJsonObject().getAsJsonPrimitive("id").getAsString(), element);
            }
        }
    }

    @Override
    public String fetchProvinceCode(String accessToken, String name) {
        fetchProvinces(accessToken);
        for (Map.Entry<String, JsonElement> entry : cacheProvince.entrySet()) {
            try {
                String k = entry.getKey();
                JsonObject v = entry.getValue().getAsJsonObject();
                if (v.getAsJsonPrimitive("name").getAsString().equals(name)) {
                    return v.getAsJsonPrimitive("code").getAsString();
                }
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
        return "";
    }


    @Override
    public void fetchCities(String accessToken, String provinceCode) {
        log.info("开始获取市数据{}", provinceCode);

        if (cacheCity.containsKey(provinceCode)) {
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        final JsonElement response = feignClientManager.getForJson(properties.getCityUrl() + "/"+provinceCode + "/children", headers);
        log.info("获取市份的数据:" + response.toString());
        if (response.isJsonArray()) {
            final JsonArray array = response.getAsJsonArray();
            cacheCity.put(provinceCode, array);
        }
    }

    @Override
    public String fetchCityCode(String accessToken, String provinceCode, String name) {
        fetchCities(accessToken, provinceCode);
        final JsonArray array = cacheCity.get(provinceCode).getAsJsonArray();
        final String found = walkCity(array, name);
        if (StringUtils.hasText(found)) {
            return found;
        }
        if (!name.endsWith("市")) {
            return walkCity(array, name + "市");
        }
        return "";
    }

    public String walkCity(JsonArray array, String name) {
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.getAsJsonPrimitive("name").getAsString().equals(name)) {
                return obj.getAsJsonPrimitive("code").getAsString();
            }
        }
        return "";
    }

    private String mapName(String name) {
        if (name.equals("阿拉善左旗")) {
            return "阿拉善盟";
        }
        return name;
    }

    @Autowired
    public void setFeignClientManager(FeignClientManager feignClientManager) {
        this.feignClientManager = feignClientManager;
    }
}
