package com.dcits.dataprocessor.dispatcher.impl;


import com.dcits.common.util.ConfigUtil;
import com.dcits.dataprocessor.dispatcher.DataProcessor;
import com.dcits.dis.frameexecutor.FrameExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


public class DataProcessorImpl implements DataProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessorImpl.class);
    /**
     * @author huxx
     * @@date 2019-06-13
     * @param topic
     * @return
     * @throws Exception
     * @function 根据传入的topic从配置文件中获取数据加工类的全限定名
     */
    public static Set<FrameExecutor> getExecutorClasses(String topic)throws Exception{
        if(topic==null || topic.trim().equals("")){
            logger.error("kafka topic为空!");
            throw new Exception("kafka topic为空!");
        }
        String classNames = ConfigUtil.get(topic);
        System.out.println("classNames:"+classNames);
        if(classNames==null || classNames.trim().equals("")){
            logger.error("根据topic："+topic+"，未找到对应的处理类，请检查配置文件是否正确！");
            throw new Exception("根据topic："+topic+"，未找到对应的处理类，请检查配置文件是否正确！");
        }
        String[] names = classNames.split(",");
        Set<String> classNameSet = new HashSet<>();
        Set<FrameExecutor> classSet = new HashSet<>();
        for(int i = 0;i<names.length;i++){
            if(!classNameSet.contains(names[i])){
                FrameExecutor frameExecutor = (FrameExecutor) Class.forName(names[i]).newInstance();
                classSet.add(frameExecutor);
                classNameSet.add(names[i]);
            }
        }
        return classSet;
    }

}
