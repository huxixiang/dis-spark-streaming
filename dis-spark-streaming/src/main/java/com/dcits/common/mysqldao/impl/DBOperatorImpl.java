package com.dcits.common.mysqldao.impl;

import com.dcits.common.mysqldao.DBConnectionPool;
import com.dcits.common.mysqldao.DBOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * @author huxx
 * @date 2019-06-13
 * @function 数据库操作具体实现类，初始化时需要传入对应的数据库连接
 */
public class DBOperatorImpl implements DBOperator {
    private static final Logger logger  = LoggerFactory.getLogger(DBOperatorImpl.class);
    private Connection connection;


    public DBOperatorImpl(Connection conn){
        connection = conn;
    }



    @Override
    public int Execute(String query, List<Object> params)throws Exception{
        Connection connLocal = this.connection;
        PreparedStatement pstmt = null;
        int res = -1;
        try {
            if(this.connection==null){
                logger.error("数据源连接为空！");
                throw new Exception("数据源连接为空！");
            }
            pstmt = connLocal.prepareStatement(query);
            int index = 1;
            if(params != null && !params.isEmpty()){
                for(int i=0; i<params.size(); i++){
                    pstmt.setObject(index++, params.get(i));
                }
            }
            res = pstmt.executeUpdate();
            connLocal.commit();
        } finally{
            if(this.connection==null){ //对于全局的连接不做释放，由外部进行资源释放
                DBConnectionPool.closeConnection(null, pstmt, connLocal);
            }else{
                DBConnectionPool.closeConnection(null, pstmt, null);
            }
        }
        return res;
    }



    @Override
    public int batchExecute(String query, List<List<Object>> params)throws Exception{
        Connection connLocal = this.connection;
        PreparedStatement pstmt = null;
        int ret = -1;
        try {
            if(this.connection==null){
                logger.error("数据源连接为空！");
                throw new Exception("数据源连接为空！");
            }
            pstmt = connLocal.prepareStatement(query);
            for(List<Object> line:params){
                int index = 1;
                if(line != null && !line.isEmpty()){
                    for(int i=0; i<line.size(); i++){
                        pstmt.setObject(index++, line.get(i));
                    }
                    pstmt.addBatch();
                }
            }
            int[] iArray = pstmt.executeBatch();
            ret = 0;
            for(int retTmp : iArray){
                ret += retTmp;
            }
            connLocal.commit();
        } finally{
            if(this.connection==null){ //对于全局的连接不做释放，由外部进行资源释放
                DBConnectionPool.closeConnection(null, pstmt, connLocal);
            }else{
                DBConnectionPool.closeConnection(null, pstmt, null);
            }
        }
        return ret;
    }

    public Map<String, Object> QueryOne(String sql, List<Object> params)throws Exception{
        Connection connLocal = this.connection;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if(this.connection==null){
                logger.error("数据源连接为空！");
                throw new Exception("数据源连接为空！");
            }
            int index  = 1;
            pstmt = connLocal.prepareStatement(sql);
            if(null != params && !params.isEmpty()){
                for(int i=0; i<params.size(); i++){
                    pstmt.setObject(index++, params.get(i));
                }
            }
            resultSet = pstmt.executeQuery();//返回查询结果
            ResultSetMetaData metaData = resultSet.getMetaData();
            int col_len = metaData.getColumnCount();
            while(resultSet.next()){
                for(int i=0; i<col_len; i++ ){
                    String cols_name = metaData.getColumnName(i+1);
                    Object cols_value = resultSet.getObject(cols_name);
                    if(cols_value == null){
                        cols_value = null;
                    }
                    map.put(cols_name.toUpperCase(), cols_value);
                }
            }
        } finally{
            if(this.connection==null){ //对于全局的连接不做释放，由外部进行资源释放
                DBConnectionPool.closeConnection(resultSet, pstmt, connLocal);
            }else{
                DBConnectionPool.closeConnection(resultSet, pstmt, null);
            }
        }
        return map;
    }

    public List<Map<String, Object>> Query(String query, List<Object> params)throws Exception{
        Connection connLocal = this.connection;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            if(this.connection==null){
                logger.error("数据源连接为空！");
                throw new Exception("数据源连接为空！");
            }
            int index = 1;
            pstmt = connLocal.prepareStatement(query);
            if(null != params && !params.isEmpty()){
                for(int i = 0; i<params.size(); i++){
                    pstmt.setObject(index++, params.get(i));
                }
            }
            resultSet = pstmt.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cols_len = metaData.getColumnCount();
            while(resultSet.next()){
                Map<String, Object> map = new HashMap<String, Object>();
                for(int i=0; i<cols_len; i++){
                    String cols_name = metaData.getColumnName(i+1);
                    Object cols_value = resultSet.getObject(cols_name);
                    if(cols_value == null){
                        cols_value = "";
                    }
                    map.put(cols_name.toUpperCase(), cols_value);
                }
                list.add(map);
            }
        } finally{
            if(this.connection==null){ //对于全局的连接不做释放，由外部进行资源释放
                DBConnectionPool.closeConnection(resultSet, pstmt, connLocal);
            }else{
                DBConnectionPool.closeConnection(resultSet, pstmt, null);
            }
        }
        return list;
    }

    /**
     * @author huxx
     * @param tableName
     * @param params
     * @return
     * @throws Exception
     * @date 2019-06-14
     * @function 根据业务主键删除已存在的记录
     */
    public int deleteRecords(String tableName,List<Map<String,Object>>params)throws Exception{
        int res = -1;
        String sql = "delete from "+tableName+" where ";

        List<List<Object>> paramsList = new ArrayList<>();
        for(int i = 0;i<params.size();i++){
            Map<String,Object> param = params.get(i);
            Iterator iterator = param.entrySet().iterator();
            List<Object> tmpList = new ArrayList<>();
            if(i==0){
                while(iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    String key = (String)entry.getKey();
                    Object value = entry.getValue();
                    sql+=" "+key+"="+"? and";
                    tmpList.add(value);
                }
                paramsList.add(tmpList);
            }else{
                while(iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    Object value = entry.getValue();
                    tmpList.add(value);
                }
                paramsList.add(tmpList);
            }
        }
        sql = sql.substring(0,sql.length()-3);
        res = batchExecute(sql,paramsList);
        return res;
    }

    /**
     * @author huxx
     * @param tableName
     * @param models
     * @param <T>
     * @return
     * @throws Exception
     * @date 2019-06-14
     * @function 将javaBean中的数据更新到数据库中
     */
    public <T> int upsertListBeanToMysql( String tableName, List<T> models)throws Exception {
        if(models==null || models.isEmpty()){
            return -1;
        }
        Class<?> cls = models.get(0).getClass();
        Field[] fieldArray = cls.getDeclaredFields();
        int iRet = -1;
        String sql = "insert into " + tableName + " (";
        String values = "values(";
        String update = "on duplicate key update ";
        List<List<Object>> params = new ArrayList<List<Object>>();
        for (Field field : fieldArray) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            field.setAccessible(true);
            String name = field.getName();// key
            sql += " " + name + ",";
            values += " ?,";
            update += name + "=?,";
        }
        sql = sql.substring(0, sql.length() - 1) + ")";
        values = values.substring(0, values.length() - 1) + ")";
        update = update.substring(0, update.length() - 1);
        sql += values + update;
        for(T tmp:models){
            List<Object> paramsList1 = new ArrayList<>();
            List<Object> paramsList2 = new ArrayList<>();
            for (Field field : fieldArray) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                field.setAccessible(true);
                Object obj = field.get(tmp);// value
                paramsList1.add(obj);
                paramsList2.add(obj);
            }
            paramsList1.addAll(paramsList2);
            params.add(paramsList1);
        }
        iRet = batchExecute(sql, params);
        return iRet;
    }


}
