package com.github.ollgei.tool.importing.common.model;

import lombok.Data;
import lombok.ToString;

/**
 * desc.
 * @author ollgei
 * @since 1.0
 */
@Data
@ToString(callSuper = true)
public class WarehouseModel extends WarehouseExcelModel {

    private String proviceCode;

    private String cityCode;

    private String code;

}
