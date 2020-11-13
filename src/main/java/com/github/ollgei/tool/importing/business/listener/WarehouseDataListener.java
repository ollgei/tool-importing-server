package com.github.ollgei.tool.importing.business.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.github.ollgei.base.commonj.utils.CglibBeanMapHelper;
import com.github.ollgei.tool.importing.business.ZhongRuiBusiness;
import com.github.ollgei.tool.importing.common.model.WarehouseExcelModel;
import com.github.ollgei.tool.importing.common.model.WarehouseModel;
import lombok.extern.slf4j.Slf4j;

/**
 * desc.
 * @author ollgei
 * @since 1.0
 */
@Slf4j
public class WarehouseDataListener extends AnalysisEventListener<WarehouseExcelModel> {

    private Map<String, List<WarehouseModel>> caches = new ConcurrentHashMap<>();

    private String accessToken;

    private ZhongRuiBusiness zhongRuiBusiness;

    private List<WarehouseExcelModel> fails = new ArrayList<>(8);

    public WarehouseDataListener(String accessToken, ZhongRuiBusiness zhongRuiBusiness) {
        this.accessToken = accessToken;
        this.zhongRuiBusiness = zhongRuiBusiness;
    }

    @Override
    public void invoke(WarehouseExcelModel data, AnalysisContext context) {
        if (!check(data)) {
            return;
        }
        WarehouseModel model = buildWarehouseModel(data);
        if (model == null) {
            return;
        }
        if (caches.containsKey(model.getCode())) {
            caches.get(model.getCode()).add(model);
        } else {
            final ArrayList<WarehouseModel> arr = new ArrayList<>();
            arr.add(model);
            caches.put(model.getCode(), arr);
        }
    }

    private boolean check(WarehouseExcelModel data) {
        final WarehouseModel model =
                CglibBeanMapHelper.of().copyObject0(data, new WarehouseModel(), new WarehouseModel());
        StringJoiner joiner = new StringJoiner(";");
        if (!data.getName().equals(data.getName2())) {
            joiner.add("名称不正确");
        }
        final String proviceName = data.getProviceName();
        if (proviceName.startsWith("北京市")) {
            data.setProviceName("北京市");
            data.setCityName("北京市市辖区");
        } else if (proviceName.startsWith("重庆市")) {
            data.setProviceName("重庆市");
            data.setCityName("重庆市市辖区");
        } else if (proviceName.startsWith("天津市")) {
            data.setProviceName("天津市");
            data.setCityName("天津市市辖区");
        } else if (proviceName.startsWith("上海市")) {
            data.setProviceName("上海市");
            data.setCityName("上海市市辖区");
        } else {
            final int proviceIndex = proviceName.indexOf("省");
            final int proviceIndex2 = proviceName.indexOf("自治区");
            int cityStartIndex = 0;
            if (proviceIndex == -1 && proviceName.indexOf("自治区") == -1) {
                joiner.add("没有包含省或者自治区");
            } else {
                if (proviceIndex > 0) {
                    data.setProviceName(proviceName.substring(0, proviceIndex + 1));
                    cityStartIndex = proviceIndex + 1;
                } else {
                    data.setProviceName(proviceName.substring(0, proviceIndex2 + 3));
                    cityStartIndex = proviceIndex2 + 3;
                }
                final int cityIndex = proviceName.indexOf("市", cityStartIndex);
                final int cityIndex2 = proviceName.indexOf("自治州", cityStartIndex);
                if (proviceName.indexOf("内蒙古自治区") >= 0 && proviceName.indexOf("锡林郭勒") > 0) {
                    data.setCityName("锡林郭勒盟");
                } else if (proviceName.indexOf("大兴安岭地区") > 0) {
                    data.setCityName("大兴安岭地区");
                } else if (proviceName.indexOf("内蒙古自治区") >= 0 && proviceName.indexOf("鄂尔多斯") > 0) {
                    data.setCityName("鄂尔多斯市");
                } else if (proviceName.indexOf("内蒙古自治区") >= 0 && proviceName.indexOf("阿拉善") > 0) {
                    data.setCityName("阿拉善盟");
                } else if (cityIndex == -1 && cityIndex2 == -1) {
                    joiner.add("没有包含市");
                } else {
                    if (cityIndex > 0) {
                        if (cityIndex2 > 0 && cityIndex2 < cityIndex) {
                            data.setCityName(proviceName.substring(cityStartIndex, cityIndex2 + 3));
                        } else {
                            data.setCityName(proviceName.substring(cityStartIndex, cityIndex + 1));
                        }
                    } else {
                        if (cityIndex > 0 && cityIndex < cityIndex2) {
                            data.setCityName(proviceName.substring(cityStartIndex, cityIndex + 1));
                        } else {
                            data.setCityName(proviceName.substring(cityStartIndex, cityIndex2 + 3));
                        }
                    }
                }
            }
        }

        if (joiner.length() > 0) {
            model.setDescribe(joiner.toString());
            fails.add(model);
            return false;
        }
        return true;
    }

    private WarehouseModel buildWarehouseModel(WarehouseExcelModel data) {
        final WarehouseModel model =
                CglibBeanMapHelper.of().copyObject0(data, new WarehouseModel(), new WarehouseModel());
        final String provinceCode =
                zhongRuiBusiness.fetchProvinceCode(accessToken, data.getProviceName());
        if (!StringUtils.hasText(provinceCode)) {
            model.setDescribe("省Code为空");
            fails.add(model);
            return null;
        }
        model.setProviceCode(provinceCode);
        String codeTemp = provinceCode;
        if (StringUtils.hasText(data.getCityName())) {
            final String cityCode = zhongRuiBusiness.fetchCityCode(accessToken, provinceCode, data.getCityName());
            model.setCityCode(cityCode);
            codeTemp = cityCode;
        }
        if (!StringUtils.hasText(codeTemp)) {
            log.warn("{}:的省:{}市:{}名称不对", data.getName(), data.getProviceName(), data.getCityName());
            fails.add(data);
            return null;
        }
        final String code = String.format("%s%s", codeTemp,
                (data.getName().endsWith("一级仓")) ? "01" : "02");
        model.setCode(code);
        return model;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //拼接创建仓库所需要的数据
        //查询当前仓库编码是否存在
        //拼接创建库位所需要的数据
        log.info("创建数据列表{}", caches);
        if (fails.size() > 0) {
            log.info("错误的数据:{}", fails);
            return;
        }
        log.info("OK!!!");
        caches.forEach((k, v) -> {
            zhongRuiBusiness.saveWarehouse(k, v);
        });

    }


}
