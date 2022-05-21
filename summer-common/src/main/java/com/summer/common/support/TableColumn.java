package com.summer.common.support;

import java.io.Serializable;

public class TableColumn implements Serializable {
    private static final long serialVersionUID = -6327371018722764443L;
    /** 字段 **/
    public String column;
    /** 数据类型 **/
    public String dt;
    /** 字符长度/数字总长度 **/
    public int size;
    /** 数字小数部分长度 **/
    public int scale;
    /** 是否可为空 **/
    public boolean nullAble;
    /** 备注 **/
    public String comment;
}
