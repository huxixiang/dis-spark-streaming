package com.dcits.dataprocessor.configure.commonconf;


import java.util.List;

/**
 * @author huxx
 * @date 2019-06-13
 * @function 数据加工类的配置信息，根据配置信息对数据模型中的数据进行相应的操作
 */
public class BussinessCommonConfig {
    //mysql配置信息
    public static boolean saveToMysql = true;//是否将model中的数据保存到mysql数据库，默认保存
    public static String mysqlTableName;//保存到mysql数据库的表名
    public static String dataSourceName;//数据源名称
    public static List<String> mysqlPrimaryKeys;//mysql主键字段列表

    //hbase配置信息
    public static boolean saveToHbase = false;//是否将model中的数据保存到hbase数据库中，默认不保存
    public static String hbaseTableName;//保存到hbase数据的表名称
    public static List<String> hbaseRowKeyColNames;//构成hbase表rowkey的model类中的字段名称

    //回写kafka配置信息
    public static boolean writeBack = true;//是否回写kafka
    public static String writeBackTopic;//回写kafka主题名称
    public static List<String> kafkaKeys;//回写kafka主题时分区策略依据字段，从model类中获取



}
