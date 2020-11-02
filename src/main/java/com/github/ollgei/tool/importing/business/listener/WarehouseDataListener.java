package com.github.ollgei.tool.importing.business.listener;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.github.ollgei.tool.importing.business.ZhongRuiBusiness;
import com.github.ollgei.tool.importing.common.model.WarehouseExcelModel;
import lombok.extern.slf4j.Slf4j;

/**
 * desc.
 * @author ollgei
 * @since 1.0
 */
@Slf4j
public class WarehouseDataListener extends AnalysisEventListener<WarehouseExcelModel> {

    private List<WarehouseExcelModel> list = new ArrayList<>(8);

    private String token;

    private ZhongRuiBusiness zhongRuiBusiness;

    public WarehouseDataListener(String token, ZhongRuiBusiness zhongRuiBusiness) {
        this.token = token;
        this.zhongRuiBusiness = zhongRuiBusiness;
    }

    @Override
    public void invoke(WarehouseExcelModel data, AnalysisContext context) {
        log.info(data.toString());
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("list的列表{}", list);
    }
}
