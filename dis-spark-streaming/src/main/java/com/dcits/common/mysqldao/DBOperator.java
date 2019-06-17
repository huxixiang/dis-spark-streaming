package com.dcits.common.mysqldao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author huxx
 * @date 2019-06-13
 * @function 数据库操作接口
 */
public interface DBOperator {



    public int Execute(String query, List<Object> params)throws Exception;

    public int batchExecute(String query, List<List<Object>> params)throws Exception;

    public Map<String, Object> QueryOne(String sql, List<Object> params)throws Exception;
    public List<Map<String, Object>> Query(String query, List<Object> params)throws Exception;





}
