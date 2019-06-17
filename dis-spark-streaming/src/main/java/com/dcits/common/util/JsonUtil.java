package com.dcits.common.util;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.*;



public class JsonUtil {

    /**
     * @author huxx
     * @param jsonStr
     * @return
     * @date 2019-06-17
     * @function 将输入的json字符串解析成List<Map>数据类型
     */
    public static List<Map<String,Object>> praseJsonStr(String jsonStr)throws Exception{
        List<Map<String,Object>> tempResult = new ArrayList<>();
        Object jsonObj = JSON.parse(jsonStr);
        if(jsonObj instanceof Map){
            praseMap((Map<?, ?>) jsonObj, null, tempResult);
        }else if(jsonObj instanceof List){
            praseList((List<?>)jsonObj, null, tempResult);
        }else{
            throw new Exception("json报文解析失败！");
        }
        return tempResult;
    }

    /**
     * 行增加、列不增加
     */
    public static void praseList(List<?> source, String parentName, List<Map<String, Object>> limitHandleRows){
        List<Map<String, Object>> template = depCopy(limitHandleRows);
        boolean firstFlag = true;
        for(Object obj : source){
            List<Map<String, Object>>  handleRows = null;

            if(firstFlag){
                handleRows = limitHandleRows;
            }else{//增加新行
                handleRows = depCopy(template);
                //limitHandleRows.addAll(handleRows);
            }

            if(obj instanceof Map){
                praseMap((Map<?, ?>)obj, parentName.trim(), handleRows);
            }else if(obj instanceof List){
                praseList((List<?>)obj, parentName.trim(), handleRows);
            }else{
                if(handleRows.size() == 0){
                    Map<String,Object> map = new HashMap<>();
                    map.put(parentName.toUpperCase().trim(),obj.toString().trim());
                    handleRows.add(map);
                }else{
                    for(Map<String, Object> tempRow: handleRows){
                        tempRow.put(parentName.toUpperCase().trim(), obj.toString().trim());
                    }
                }
            }
            if(!firstFlag){
                limitHandleRows.addAll(handleRows);
            }
            if(firstFlag){
                firstFlag = false;
            }
        }

    }

    /**
     * 列增加、行不增加
     */
    public static void praseMap(Map<?, ?> source, String parentName, List<Map<String, Object>> limitHandleRows){
        for(Map.Entry<?, ?> entry : source.entrySet()){
            String colName = null;
            Object colValue=entry.getValue();
            if (null == parentName || "".equals(parentName.trim())) {
                colName = entry.getKey().toString().trim();
            } else {
                colName = parentName.trim() + "." + entry.getKey().toString().trim();
            }

            if(entry.getValue() instanceof Map){
                praseMap((Map<?, ?>)colValue, colName, limitHandleRows);
            }else if(entry.getValue() instanceof List){
                praseList((List<?>) colValue, colName, limitHandleRows);
            }else{
                String value = entry.getValue() == null ? "" : entry.getValue().toString().trim();
                if(limitHandleRows.size() == 0){
                    Map<String, Object> tmpRow = new HashMap<String, Object>();
                    limitHandleRows.add(tmpRow);
                    tmpRow.put(colName.toUpperCase().trim(), value);
                }else{
                    for(Map<String, Object> tempRow : limitHandleRows){
                        tempRow.put(colName.toUpperCase().trim(), value);
                    }
                }
            }
        }

    }

    /***
     * 对集合进行深拷贝 注意需要对泛型类进行序列化(实现Serializable)
     *
     * @param srcList
     * @param <T>
     * @return
     */
    public static <T> List<T> depCopy(List<T> srcList) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(srcList);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream inStream = new ObjectInputStream(byteIn);
            @SuppressWarnings("unchecked")
            List<T> destList = (List<T>) inStream.readObject();
            return destList;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
