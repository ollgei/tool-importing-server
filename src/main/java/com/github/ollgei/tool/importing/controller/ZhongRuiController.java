package com.github.ollgei.tool.importing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.excel.EasyExcel;
import com.github.ollgei.base.commonj.api.ApiHelper;
import com.github.ollgei.base.commonj.api.BearResponse;
import com.github.ollgei.base.commonj.utils.SpringHelper;
import com.github.ollgei.tool.importing.business.ZhongRuiBusiness;
import com.github.ollgei.tool.importing.business.listener.WarehouseDataListener;
import com.github.ollgei.tool.importing.common.model.WarehouseExcelModel;
import com.github.ollgei.tool.importing.common.model.WarehouseModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 中瑞.
 * @author zjw
 * @since 1.0
 */
@RestController
@RequestMapping("/zr")
@Tag(name = "zhongrui", description = "中瑞工具")
public class ZhongRuiController {

    private ZhongRuiBusiness zhongRuiBusiness;

    @Autowired
    public ZhongRuiController(ZhongRuiBusiness zhongRuiBusiness) {
        this.zhongRuiBusiness = zhongRuiBusiness;
    }

    @Operation(description = "批量导入仓库")
    @GetMapping("/chewu/warehouse/batch")
    public BearResponse chewuWarehouseBatch(@RequestParam("filePath") String filePath, @RequestHeader("accessToken") String accessToken) {
        EasyExcel.read(filePath, WarehouseExcelModel.class, new WarehouseDataListener(accessToken, zhongRuiBusiness)).sheet().doRead();
        return ApiHelper.builder().build().success();
    }

    @GetMapping("/create/warehouse")
    public BearResponse createWarehouse(@RequestParam("token") String token) {
        zhongRuiBusiness.createWarehouse(token);
        return ApiHelper.builder().build().success();
    }

    @GetMapping("/init/employee")
    public BearResponse initEmployee(@RequestParam("token") String token) {
        zhongRuiBusiness.initEmployee(token);
        return ApiHelper.builder().build().success();
    }

    @GetMapping("/fetch/province")
    public BearResponse fetchProvince(@RequestHeader("token") String token) {
        zhongRuiBusiness.fetchProvince(token);
        return ApiHelper.builder().build().success();
    }

    @GetMapping("/fetch/province/code")
    public String fetchProvince(@RequestHeader("token") String token, @RequestParam("name") String name) {
        return zhongRuiBusiness.fetchProvinceCode(token, name);
    }

    @GetMapping("/fetch/city")
    public BearResponse fetchCity(@RequestHeader("token") String token, @RequestParam("code") String provinceCode) {
        zhongRuiBusiness.fetchCity(token, provinceCode);
        return ApiHelper.builder().build().success();
    }

    @GetMapping("/fetch/city/code")
    public String fetchCity(@RequestHeader("token") String token, @RequestParam("code") String code, @RequestParam("name") String name) {
        return zhongRuiBusiness.fetchCityCode(token, code, name);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            WarehouseExcelModel model = new WarehouseExcelModel();
            model.setCityName("123445");
            WarehouseModel model1 = SpringHelper.copyObject0(model, new WarehouseModel());
            System.out.println(model1);
        }
    }


}
