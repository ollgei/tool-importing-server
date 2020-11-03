package com.github.ollgei.tool.importing.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "tb_storehouse")
public class StorehouseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "code")
    private String code;

    @TableField(value = "warehouse_id")
    private String warehouseId;

    @TableField(value = "json")
    private String json;

    /**
     * 1已经创建 2 未创建
     */
    @TableField(value = "created")
    private String created;

    public static final String COL_ID = "id";

    public static final String COL_NAME = "name";

    public static final String COL_CODE = "code";

    public static final String COL_WAREHOUSE_ID = "warehouse_id";

    public static final String COL_JSON = "json";

    public static final String COL_CREATED = "created";

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return warehouse_id
     */
    public String getWarehouseId() {
        return warehouseId;
    }

    /**
     * @param warehouseId
     */
    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    /**
     * @return json
     */
    public String getJson() {
        return json;
    }

    /**
     * @param json
     */
    public void setJson(String json) {
        this.json = json;
    }

    /**
     * 获取1已经创建 2 未创建
     *
     * @return created - 1已经创建 2 未创建
     */
    public String getCreated() {
        return created;
    }

    /**
     * 设置1已经创建 2 未创建
     *
     * @param created 1已经创建 2 未创建
     */
    public void setCreated(String created) {
        this.created = created;
    }
}