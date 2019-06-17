package com.dcits.common.mysqldao;

import com.alibaba.druid.pool.DruidDataSource;
import com.dcits.common.util.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;


/**
 * @author huxx
 * @date 2019-06-13
 * @function 初始化数据库连接池，根据数据源名称获取、关闭数据库连接等
 */
public class DBConnectionPool implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionPool.class);

    public static final HashMap<String,DruidDataSource> dsMap = new HashMap<>();

    /**
     * 根据数据源名称创建数据库连接池
     * @param dsName
     * @return
     * @throws Exception
     */
    private static DruidDataSource initMySqlDataSource(final String dsName) throws Exception{
        final DruidDataSource ds = new DruidDataSource();
        DBConnectionInfo dbInfo = ConfigUtil.dsInfoMap.get(dsName);
        if (dbInfo == null){
            throw new Exception("未获取到数据源" + dsName + "的配置信息。");
        }
        try {
            ds.setDriverClassName(dbInfo.getDriver());
            ds.setUsername(StringUtils.trimToEmpty(dbInfo.getUsername()));
            ds.setPassword(StringUtils.trimToEmpty(dbInfo.getPassword()));
            ds.setUrl(dbInfo.getUrl());
            //配置初始化大小、最小、最大
            ds.setInitialSize(5);
            ds.setMinIdle(5);
            ds.setMaxActive(200);
            //配置获取连接等待超时的时间
            ds.setMaxWait(10000);
            ds.setDefaultAutoCommit(false);
            //打开PSCache，并且指定每个连接上PSCache的大小
            ds.setPoolPreparedStatements(true);
            //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
            ds.setMaxPoolPreparedStatementPerConnectionSize(20);
            ds.setTimeBetweenEvictionRunsMillis(60000);
            //配置一个连接在池中最小生存的时间，单位是毫秒
            ds.setMinEvictableIdleTimeMillis(300000);
            ds.setTestWhileIdle(true);
            ds.setValidationQuery("select 1");
            dsMap.put(dsName, ds);
        } catch (Exception e) {
            throw new RuntimeException("创建连接池"+dsName+"失败：" + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(ds!=null){
                    logger.info("关闭"+dsName+"的连接池");
                    ds.close();
                }
            }
        });
        return ds;
    }

    /**
     * @author huxx
     * @param dsName
     * @return
     * @throws Exception
     * @function 根据数据源的名称从连接池中获取连接
     */
    public static Connection getConnection(String dsName)throws Exception{
        if(dsMap.get(dsName)==null){
            initMySqlDataSource(dsName);
        }
        Connection conn;
        try{
            DruidDataSource dds = dsMap.get(dsName);
            conn = dds.getConnection();
        }catch (Exception e){
            logger.error("获取数据源："+dsName+" 的连接时出错！");
            logger.error("错误信息："+e.toString());
            throw new Exception("获取数据源："+dsName+" 的连接时出错！");
        }
        return conn;
    }

    /**
     * 关闭连接资源
     * @author huxx
     * @param rs
     * @param stmt
     * @param conn
     */
    public static void closeConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



}
