package com.dcits.dis.frameexecutor.impl;

import com.alibaba.fastjson.JSON;
import com.dcits.common.mysqldao.DBConnectionPool;
import com.dcits.common.mysqldao.impl.DBOperatorImpl;
import com.dcits.common.util.JsonUtil;
import com.dcits.common.util.KafkaUtil;
import com.dcits.dataprocessor.configure.commonconf.BussinessCommonConfig;
import com.dcits.dis.frameexecutor.FrameExecutor;
import com.huaweicloud.dis.adapter.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author huxx
 * @date 2019-06-13
 * @function 处理流式加工链中的框架部分
 */
public abstract class FrameExecutorImpl implements FrameExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FrameExecutorImpl.class);
    public List models = new ArrayList<>();//用来保存业务加工后的model类
    private BussinessCommonConfig bussinessConfig;//通过反射获取业务加工的配置类


    /**
     * @date 2019-06-13
     * @throws Exception
     * @function 处理流式加工链中的各个节点业务具体实现方法
     */
    @Override
    public void handleProcessingChain(ConsumerRecord record, Map<String, Connection> mysqlConnMap)throws Exception{
        String bussinessClassName = this.getClass().getName();//具体的业务加工处理类
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date();
        String startTimeStr = sf.format(startTime);
        String topic;
        String jsonMsg;
        try{
            topic = record.topic();//dis通道名
            jsonMsg = record.value().toString();//json报文
        }catch (Exception e){
            logger.error(startTimeStr+":【"+bussinessClassName+"】,json报文为空！");
            throw new Exception(startTimeStr+":【"+bussinessClassName+"】,json报文为空！");
        }
        //解析json报文
        List<Map<String,Object>> reocrdListMap;
        try{
            reocrdListMap = JsonUtil.praseJsonStr(jsonMsg);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(startTimeStr+":【"+bussinessClassName+"】,json报文解析出错！");
            throw new Exception(startTimeStr+":【"+bussinessClassName+"】,json报文解析出错！");
        }
        try{
            //业务加工
            handleBussinessChain(reocrdListMap,mysqlConnMap);
        }catch (Exception e){
            logger.error(startTimeStr+":【"+bussinessClassName+"】,业务加工出错！错误原因："+e.toString());
            throw new Exception(startTimeStr+":【"+bussinessClassName+"】,业务加工出错！错误原因："+e.toString());
        }
        String configClassName ;//业务处类对应的配置类名称
        try{
            configClassName = getBussinessConfigClassName();
        }catch (Exception e){
            logger.error(startTimeStr+":【"+bussinessClassName+"】,根据业务处理类无法获取对应的配置类！错误信息："+e.toString());
            throw new Exception(startTimeStr+":【"+bussinessClassName+"】,根据业务处理类无法获取对应的配置类！");
        }
        try{
            bussinessConfig = (BussinessCommonConfig)Class.forName(configClassName).newInstance();
        }catch (Exception e){
            logger.error(startTimeStr+":【"+bussinessClassName+"】,根据业务加工类无法初始化对应的业务配置类！");
            throw new Exception(startTimeStr+":【"+bussinessClassName+"】,根据业务加工类无法初始化对应的业务配置类！");
        }
        if(models.isEmpty()){
            throw new Exception("models为空，业务加工失败！");
        }
        Class<?> modelClass = models.get(0).getClass();//获取model类的Class对象
        //保存到mysql
        boolean saveToMysql = bussinessConfig.saveToMysql;
        if(saveToMysql){
            String mysqlTableName = bussinessConfig.mysqlTableName;
            String dataSourceName = bussinessConfig.dataSourceName;
            List<String> mysqlPrimaryKeys = bussinessConfig.mysqlPrimaryKeys;

            try{
                Connection conn = DBConnectionPool.getConnection(dataSourceName);
                DBOperatorImpl dbOperator = new DBOperatorImpl(conn);
                //根据设置的业务主键删除已经存在的记录
                if(mysqlPrimaryKeys!=null && !mysqlPrimaryKeys.isEmpty()){
                    List<Map<String,Object>> paramList = new ArrayList<>();
                    for(int i = 0;i<models.size();i++){
                        Map<String,Object> param = new HashMap<>();
                        for(String pkCol:mysqlPrimaryKeys){
                            if(!pkCol.trim().equals("")){
                                //根据反射拿到model类中的
                                Method method = modelClass.getDeclaredMethod("get"+pkCol.toUpperCase());
                                Object value = method.invoke(models.get(i));
                                param.put(pkCol.toUpperCase(),value);
                            }
                        }
                        paramList.add(param);
                    }
                    dbOperator.deleteRecords(mysqlTableName,paramList);
                }
                //将models中的javaBean数据保存到数据库中
                dbOperator.upsertListBeanToMysql(mysqlTableName,models);
            }catch (Exception e){
                logger.error(startTimeStr+":【"+bussinessClassName+"】,数据保存到mysql失败！错误原因："+e.toString());
                throw new Exception(startTimeStr+":【"+bussinessClassName+"】,数据保存到mysql失败！错误原因："+e.toString());
            }
        }
        boolean writeBack = bussinessConfig.writeBack;
        //回写到指定的kafka主题
        if(writeBack){
            String writeBackTopic = bussinessConfig.writeBackTopic;
            try{
                List<String> kafkaKeys = bussinessConfig.kafkaKeys;
                for(int i = 0;i<models.size();i++){
                    Object model = models.get(i);
                    if(kafkaKeys!=null && !kafkaKeys.isEmpty()){
                        StringBuffer kafkaKey = new StringBuffer();
                        for(String keyCol:kafkaKeys){
                            Method method = modelClass.getDeclaredMethod("get"+keyCol.toUpperCase());
                            Object value = method.invoke(model);
                            kafkaKey.append(value);
                            kafkaKey.append("##");
                        }
                        String kafkaKeyVal = kafkaKey.substring(0,kafkaKey.length()-2);
                        KafkaUtil.sendMsgToKafka(writeBackTopic,kafkaKeyVal, JSON.toJSONString(model));
                    }else{
                        KafkaUtil.sendMsgToKafka(writeBackTopic,null, JSON.toJSONString(model));
                    }
                }
            }catch (Exception e){
                logger.error(startTimeStr+":【"+bussinessClassName+"】,回写kafka主题失败！");
                throw new Exception(startTimeStr+":【"+bussinessClassName+"】,回写kafka主题失败！");
            }
        }
    }

    /**
     * @author huxx
     * @return
     * @date 2019-06-17
     * @function 根据当前业务处理类类名得到业务处理类对应的配置类类名
     */
    public String getBussinessConfigClassName(){
        String fullQualifiedName = this.getClass().getName();
        String[] nameArray = fullQualifiedName.split("\\.");
        String oldName = nameArray[nameArray.length-1];
        String newName = oldName.replace("Impl","Config");
        fullQualifiedName = fullQualifiedName.replace("executor.bussiness.impl","configure").replace(oldName,newName);
        return fullQualifiedName;
    }


}
