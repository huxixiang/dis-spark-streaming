package com.dcits.dis;

import com.dcits.common.mysqldao.DBConnectionPool;
import com.dcits.common.util.ConfigUtil;
import com.dcits.dataprocessor.dispatcher.impl.DataProcessorImpl;
import com.dcits.dis.frameexecutor.FrameExecutor;
import com.huaweicloud.dis.DISConfig;
import com.huaweicloud.dis.adapter.kafka.clients.consumer.ConsumerConfig;
import com.huaweicloud.dis.adapter.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.dis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author huxx
 * @date 2019-06-13
 * @function 流式加工的入口类
 */
public class DISSparkStreamingApp {
    private static final Logger logger = LoggerFactory.getLogger(DISSparkStreamingApp.class);

    public static void main(String[] args) {

        // DIS终端节点，如 https://dis.cn-north-1.myhuaweicloud.com
        String endpoint;
        // DIS服务所在区域ID，如 cn-north-1
        String region;
        // 用户的AK
        String ak;
        // 用户的SK
        String sk;
        // 用户的项目ID
        String projectId;
        // DIS通道名称
        String streamName;
        // 消费策略，只有当分区没有Checkpoint或者Checkpoint过期时，才会使用此配置的策略；如果存在有效的Checkpoint，则会从此Checkpoint开始继续消费
        // 取值有： LATEST      从最新的数据开始消费，此策略会忽略通道中已有数据
        //         EARLIEST    从最老的数据开始消费，此策略会获取通道中所有的有效数据
        String startingOffsets;
        // Streaming程序批处理间隔(s)
        Long duration;
        // 消费组标识，同一个消费组下的不同客户端可以同时消费同一个通道
        String groupId;


        endpoint = ConfigUtil.ENDPOINT;
        region = ConfigUtil.REGION;
        ak = ConfigUtil.AK;
        sk = ConfigUtil.SK;
        projectId = ConfigUtil.PROJECTID;
        streamName = ConfigUtil.DIS_NAMES;
        startingOffsets = ConfigUtil.DIS_STARTINGOFFSET;
        duration = ConfigUtil.STREAMING_DURATION;
        groupId = ConfigUtil.DIS_GROUPID;
        try {
            SparkConf conf = new SparkConf().setAppName("SparkJobName");
            conf.set("spark.cores.max","3");
            conf.set("spark.executor.cores","3");
            conf.set("spark.executor.memory","3G");
            conf.setMaster("local[3]");
            JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(duration));

            Map<String, Object> params = new HashMap<>();
            params.put(DISConfig.PROPERTY_ENDPOINT, endpoint);
            params.put(DISConfig.PROPERTY_REGION_ID, region);
            params.put(DISConfig.PROPERTY_AK, ak);
            params.put(DISConfig.PROPERTY_SK, sk);
            params.put(DISConfig.PROPERTY_PROJECT_ID, projectId);
            params.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, startingOffsets);
            params.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            params.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

            // 需要消费的通道名称
            final Collection<String> topics = Collections.singletonList(streamName);

            JavaInputDStream<ConsumerRecord<String, String>> stream = DISUtils.createDirectStream(jsc,
                    ConsumerStrategies.Subscribe(topics, params));

            // 处理每个ConsumerRecord
            stream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String, String>>>() {
                @Override
                public void call(JavaRDD<ConsumerRecord<String, String>> consumerRecordJavaRDD) throws Exception {
                    consumerRecordJavaRDD.foreachPartition(new VoidFunction<Iterator<ConsumerRecord<String, String>>>() {
                        @Override
                        public void call(Iterator<ConsumerRecord<String, String>> consumerRecordIterator) throws Exception {
                            /**
                             * 1、根据数据源名称获取mysql连接
                             */
                            Map<String, Connection> mysqlConnMap = new HashMap<>();
                            String[] dataSources = ConfigUtil.get("db.names").split(",");
                            for(String ds:dataSources){
                                mysqlConnMap.put(ds, DBConnectionPool.getConnection(ds));
                            }
                            consumerRecordIterator.forEachRemaining(new Consumer<ConsumerRecord<String, String>>() {
                                @Override
                                public void accept(ConsumerRecord<String, String> record) {
                                    //根据topic获取具体业务处理类的Set
                                    Set<FrameExecutor> bussinessClassSet = null;
                                    try{
                                        bussinessClassSet = DataProcessorImpl.getExecutorClasses(record.topic());
                                        Iterator iterator = bussinessClassSet.iterator();
                                        while(iterator.hasNext()){
                                            FrameExecutor frameExecutor = (FrameExecutor)iterator.next();
                                            frameExecutor.handleProcessingChain(record,mysqlConnMap);
                                        }
                                    }catch (Exception e){
                                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date startTime = new Date();
                                        String startTimeStr = sf.format(startTime);
                                        logger.error(startTimeStr+":【主题"+record.topic()+"】"+",业务加工失败！");
                                        e.printStackTrace();
                                    }
                                }
                            });
                            Iterator iterator = mysqlConnMap.entrySet().iterator();
                            while(iterator.hasNext()){
                                Map.Entry entry = (Map.Entry)iterator.next();
                                DBConnectionPool.closeConnection(null,null,(Connection) entry.getValue());
                            }

                        }
                    });
                }
            });
            // 消费数据之后，提交offset
            stream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String, String>>>() {
                @Override
                public void call(JavaRDD<ConsumerRecord<String, String>> rdd) {
                    OffsetRange[] offset = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
                    ((CanCommitOffsets) stream.dstream()).commitAsync(offset);
                }
            });
            jsc.start();
            jsc.awaitTermination();

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
