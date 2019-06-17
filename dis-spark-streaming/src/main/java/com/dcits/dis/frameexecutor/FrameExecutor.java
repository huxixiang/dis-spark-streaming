package com.dcits.dis.frameexecutor;

import com.huaweicloud.dis.adapter.kafka.clients.consumer.ConsumerRecord;

import java.sql.Connection;
import java.util.List;
import java.util.Map;


/**
 * @author huxx
 * @date 2019-06-13
 * @function 处理流式加工链中的框架部分，整合业务加工类中的方法
 */
public interface FrameExecutor {

    /**
     * @date 2019-06-13
     * @throws Exception
     * @function 处理流式加工链中业务加工部分接口
     */
    public void handleBussinessChain(List<Map<String,Object>> reocrdListMap, Map<String, Connection> mysqlConnMap)throws Exception;

    /**
     * @date 2019-06-13
     * @throws Exception
     * @function 处理流式加工链中的各个节点业务接口
     */
    public void handleProcessingChain(ConsumerRecord record, Map<String, Connection> mysqlConnMap)throws Exception;
}
