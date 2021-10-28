package com.liangzhicheng.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class JSONUtil {

    /**
     * GsonBuilder构造器
     */
    static GsonBuilder gb = new GsonBuilder();

    /**
     * 初始化Gson
     */
    private static final Gson gson;

    /**
     * 静态代码块
     */
    static {
        gb.disableHtmlEscaping();
        gson = gb.create();
    }

    /**
     * Object对象转成bytes字节数组
     * @param object
     * @return byte[]
     */
    public static byte[] objectToBytes(Object object) {
        try {
            return objectToString(object).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Object对象转成String字符串
     * @param object
     * @return String
     */
    public static String objectToString(Object object) {
        return gson.toJson(object);
    }

    /**
     * bytes字节数组转成对应泛型T
     * @param bytes
     * @param clazz
     * @param <T>
     * @return T
     */
    public static <T> T bytesToObject(byte[] bytes, Class<T> clazz) {
        try {
            return stringToObject(new String(bytes, "UTF-8"), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * String字符串转成对应泛型T
     * @param value
     * @param clazz
     * @param <T>
     * @return T
     */
    public static <T> T stringToObject(String value, Class<T> clazz) {
        return gson.fromJson(value, clazz);
    }

    /**
     * 根据TypeToken将String字符串转成对应泛型T
     * @param value
     * @param typeToken
     * @param <T>
     * @return T
     */
    public static <T> T stringToObject(String value, TypeToken typeToken) {
        return gson.fromJson(value, typeToken.getType());
    }

}
