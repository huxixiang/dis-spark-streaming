package com.dcits.common.util;

import com.dcits.common.mysqldao.DBConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static final Properties properties = new Properties();
    public static String ENDPOINT;
    public static String REGION;
    public static String AK;
    public static String SK;
    public static String PROJECTID;
    public static String KAFKA_HOSTS;
    public static String ZOOKEEPER_HOSTS;
    public static String DIS_NAMES;
    public static String DIS_STARTINGOFFSET;
    public static String DIS_GROUPID;
    public static Long STREAMING_DURATION;

    //数据源基本信息散列表
    public static Map<String, DBConnectionInfo> dsInfoMap = new HashMap<>();

    static {
        try{
            properties.load(ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties"));
            ENDPOINT = properties.getProperty("dis_endpoint");
            REGION = properties.getProperty("dis_region");
            AK = properties.getProperty("dis_ak");
            SK = properties.getProperty("dis_sk");
            PROJECTID = properties.getProperty("dis_projectid");
            KAFKA_HOSTS = properties.getProperty("kafka.hosts");
            ZOOKEEPER_HOSTS = properties.getProperty("zookeeper.server");
            DIS_NAMES = properties.getProperty("dis_names");
            DIS_STARTINGOFFSET = properties.getProperty("dis_startingoffset");
            DIS_GROUPID = properties.getProperty("dis_groupid");
            STREAMING_DURATION = Long.parseLong(properties.getProperty("streaming_duration"));

            //初始化mysql数据源信息
            String[] dbNames = properties.getProperty("db.names").split(",");
            for(int i = 0;i<dbNames.length;i++){
                DBConnectionInfo dbInfo = new DBConnectionInfo();
                dbInfo.setUrl(properties.getProperty("db."+dbNames[i]+".url"));
                dbInfo.setDriver(properties.getProperty("db."+dbNames[i]+".driver"));
                dbInfo.setUsername(properties.getProperty("db."+dbNames[i]+".username"));
                dbInfo.setPassword(properties.getProperty("db."+dbNames[i]+".password"));
                dsInfoMap.put(dbNames[i],dbInfo);
            }
        }catch (Exception e){
            logger.error("配置文件初始化失败！");
            logger.error("失败原因："+e.toString());
            e.printStackTrace();
        }
    }

    public static String get(String key){
        return properties.getProperty(key);
    }


}
