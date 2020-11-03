package com.github.ollgei.tool.importing.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "tb_employee")
public class EmployeeEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "login_name")
    private String loginName;

    @TableField(value = "sid")
    private String sid;

    @TableField(value = "zh_name")
    private String zhName;

    @TableField(value = "dept_id")
    private String deptId;

    @TableField(value = "dept_name")
    private String deptName;

    public static final String COL_ID = "id";

    public static final String COL_LOGIN_NAME = "login_name";

    public static final String COL_SID = "sid";

    public static final String COL_ZH_NAME = "zh_name";

    public static final String COL_DEPT_ID = "dept_id";

    public static final String COL_DEPT_NAME = "dept_name";

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
     * @return login_name
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * @param loginName
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * @return sid
     */
    public String getSid() {
        return sid;
    }

    /**
     * @param sid
     */
    public void setSid(String sid) {
        this.sid = sid;
    }

    /**
     * @return zh_name
     */
    public String getZhName() {
        return zhName;
    }

    /**
     * @param zhName
     */
    public void setZhName(String zhName) {
        this.zhName = zhName;
    }

    /**
     * @return dept_id
     */
    public String getDeptId() {
        return deptId;
    }

    /**
     * @param deptId
     */
    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    /**
     * @return dept_name
     */
    public String getDeptName() {
        return deptName;
    }

    /**
     * @param deptName
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}