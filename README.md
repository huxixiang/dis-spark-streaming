# dis-spark-streaming
基于华为云平台的sparkstreaming程序


基于华为云平台的sparkstreaming程序

本程序流式数据的来源是华为云平台的DIS通道（kafka）,从DIS通道中读入流式数据到SparkStreaming中，在配置文件中配置DIS通道名和具体的业务处理类之间的映射关系，程序在初始化时加载配置文件中的配置信息，根据配置文件中映射关系，将DIS通道中的流式数据交给对应的业务处理类处理，并根据com.dcits.dataprocessor.configure包中对应的配置类型信息将加工后的数据保存到Mysql或者Kafka中。
