package com.github.ollgei.tool.importing.business.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private WarehouseModel buildWarehouseModel(WarehouseExcelModel data) {
        final WarehouseModel model =
                CglibBeanMapHelper.of().copyObject0(data, new WarehouseModel(), new WarehouseModel());
        final String provinceCode =
                zhongRuiBusiness.fetchProvinceCode(accessToken, data.getProviceName());
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
        caches.forEach((k, v) -> {
            zhongRuiBusiness.saveWarehouse(k, v);
        });

    }


}
