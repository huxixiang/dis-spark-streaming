package com.dcits.dataprocessor.configure;

import com.dcits.dataprocessor.configure.commonconf.BussinessCommonConfig;

import java.util.ArrayList;

/**
 * @author huxx
 * @date 2019-06-13
 * @function 根据每个业务的需求进行相应的配置
 */
public class BussinessExampleConfig extends BussinessCommonConfig {

    //修改默认的配置
    static{
        //mysql配置
        saveToMysql = true;
        mysqlTableName = "huxxe";
        dataSourceName = "dw_iot_mysql";
        mysqlPrimaryKeys = new ArrayList<>();
        mysqlPrimaryKeys.add("ID");
        //回写kafka配置
        writeBack = true;
        writeBackTopic = "HUAWEI_DIS";
    }

}
