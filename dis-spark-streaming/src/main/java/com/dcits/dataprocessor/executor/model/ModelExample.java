package com.dcits.dataprocessor.executor.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.dcits.dataprocessor.executor.model.commoncolumns.ModelCommonCols;

import java.sql.Timestamp;


/**
 * @author huxx
 * @date 2019-06-13
 * @function 模型类，数据加工后的数据保存到模型类中，与数据库中的表模型字段对应
 */
public class ModelExample extends ModelCommonCols {
    private int ID;
    private String NAME;
    private Timestamp TIME_STAMP;

    @JSONField(name="ID")
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    @JSONField(name="NAME")
    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    @JSONField(name="TIME_STAMP")
    public Timestamp getTIME_STAMP() {
        return TIME_STAMP;
    }

    public void setTIME_STAMP(Timestamp TIME_STAMP) {
        this.TIME_STAMP = TIME_STAMP;
    }
}
