package com.xiongdi.recognition.util;

/**
 * Created by moubiao on 2016/3/24.
 * 字符床工具类
 */
public class StringUtil {
    public static boolean hasLength(String str){
        if((str == null)){
            return false;
        }

        if(0 == str.length()){
            return false;
        }

        return true;
    }
}
