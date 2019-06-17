package com.dcits.dataprocessor.executor.bussiness.impl;


import com.dcits.dataprocessor.configure.commonconf.BussinessCommonConfig;
import com.dcits.dataprocessor.executor.model.ModelExample;
import com.dcits.dis.frameexecutor.impl.FrameExecutorImpl;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class BussinessExampleImpl extends FrameExecutorImpl {

    @Override
    public void handleBussinessChain(List<Map<String,Object>> reocrdListMap, Map<String, Connection> mysqlConnMap)throws Exception{
//        System.out.println("reocrdListMap:"+reocrdListMap);
        for(int i = 0;i<reocrdListMap.size();i++){
            Map<String,Object> record = reocrdListMap.get(i);
            ModelExample modelExample = new ModelExample();
            modelExample.setID(Integer.parseInt(record.get("ALERT_ID").toString()));
            modelExample.setNAME(record.get("TASK_ID").toString());

            modelExample.setTIME_STAMP(new Timestamp(Long.parseLong(record.get("TIME_STAMP").toString())));

            models.add(modelExample);
        }
    }


    public static void main(String[] args) throws Exception{
        BussinessExampleImpl bussinessExample = new BussinessExampleImpl();
        String className = bussinessExample.getBussinessConfigClassName();
        System.out.println(className);
        BussinessCommonConfig bussinessConfig = (BussinessCommonConfig)Class.forName(className).newInstance();
        System.out.println(bussinessConfig.writeBackTopic);

    }





}
