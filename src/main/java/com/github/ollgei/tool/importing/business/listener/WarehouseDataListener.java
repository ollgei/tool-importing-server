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

    private List<WarehouseExcelModel> fail = new ArrayList<>(8);

    private String token;

    private ZhongRuiBusiness zhongRuiBusiness;

    public WarehouseDataListener(String token, ZhongRuiBusiness zhongRuiBusiness) {
        this.token = token;
        this.zhongRuiBusiness = zhongRuiBusiness;
    }

    @Override
    public void invoke(WarehouseExcelModel data, AnalysisContext context) {
        log.info(data.toString());
        fail.add(data);
    }

    private void buildWarehouseList() {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //拼接创建仓库所需要的数据
        //查询当前仓库编码是否存在
        //拼接创建库位所需要的数据
        log.info("创建失败的数据列表{}", fail);
    }
}
