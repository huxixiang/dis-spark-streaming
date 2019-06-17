package com.dcits.common.hbasedao;

import java.io.Serializable;

/**
 * 以后需要将数据保存到hbase数据库时打开该类
 */
public class HbaseConnectionUtil implements Serializable {
//    private static final Logger logger = LoggerFactory.getLogger(HbaseConnectionUtil.class);
//    public static org.apache.hadoop.hbase.client.Connection hbaseConn;
//    /**
//     * 得到HBase数据库连接
//     * @return connection
//     * @throws Exception
//     */
//    public static org.apache.hadoop.hbase.client.Connection getHbaseConnection() throws Exception {
//        if(hbaseConn==null || hbaseConn.isClosed()){
//            synchronized (HbaseConnectionUtil.class){
//                Configuration hbaseConf = HBaseConfiguration.create();
//                try {
//                    hbaseConf.set("hbase.zookeeper.quorum", ConfigUtil.get("hbase.zookeeper.host"));
//                    hbaseConf.set("hbase.zookeeper.property.clientPort", ConfigUtil.get("hbase.zookeeper.port"));
//                    hbaseConf.set("hbase.defaults.for.version.skip", "true");
//                    hbaseConf.set("zookeeper.znode.parent", ConfigUtil.get("hbase.zookeeper.parent"));
//                    hbaseConn = ConnectionFactory.createConnection(hbaseConf);
//                } catch (Exception e) {
//                    logger.error("获取HBase连接失败：" + e.getMessage());
//                    throw new RuntimeException("获取HBase连接失败：" + e.getMessage());
//                }
//            }
//        }
//
//        return hbaseConn;
//    }
//
//    /**
//     * 关闭hbase连接
//     * @param conn
//     */
//    public static void closeHBase(org.apache.hadoop.hbase.client.Connection conn) {
//        if (conn != null) {
//            try {
//                if(!conn.isClosed()){
//                    conn.close();
//                }
//            } catch (IOException e) {
//                logger.error("关闭hbase连接失败！");
//                logger.error("失败原因："+e.toString());
//                e.printStackTrace();
//            }
//        }
//    }

}
