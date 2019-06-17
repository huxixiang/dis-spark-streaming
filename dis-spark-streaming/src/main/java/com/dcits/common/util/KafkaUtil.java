package com.dcits.common.util;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author huxx
 * @date 2019-06-13
 */
public class KafkaUtil {
    private static Logger logger = LoggerFactory.getLogger(KafkaUtil.class);
    //消费者配置信息
    static final Map consumerConfMap = new HashMap<>();
    //生产者配置信息
    static final Properties producerConfProp = new Properties();
    private static KafkaProducer<String, String> producer;
    static{

        producerConfProp.put("bootstrap.servers", ConfigUtil.KAFKA_HOSTS);
        producerConfProp.put("acks", "0");
        producerConfProp.put("retries", 3);
        producerConfProp.put("batch.size", 16384);
        producerConfProp.put("linger.ms", 0);
        producerConfProp.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfProp.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    private static KafkaProducer<String,String> getProducer(){
        if(producer==null){
            synchronized (KafkaUtil.class){
                producer = new KafkaProducer<String, String>(producerConfProp);
                Runtime.getRuntime().addShutdownHook(new Thread(){
                    @Override
                    public void run(){
                        if(producer!=null){
                            producer.close();
                        }
                    }
                });
            }
        }
        return producer;

    }



    /**
     * 将消息发送到kafka主题
     * @param topic 主题
     * @param key 分区依据的key值，可为空
     * @param json json报文
     */
    public static void sendMsgToKafka(String topic,String key,String json){
        KafkaProducer<String,String> producer = getProducer();
        producer.send(new ProducerRecord<String,String>(topic,key,json));
        producer.flush();
        logger.info("消息:"+json+"回写主题:"+topic+",成功。");


    }




}
