package org.hy.common.xml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hy.common.Help;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONAware;
import net.minidev.json.JSONAwareEx;
import net.minidev.json.JSONStreamAwareEx;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import net.minidev.json.reader.JsonWriter;





/**
 * 重写 JSONObject 类，
 *    不同处1：继承了 LinkedHashMap。
 *    不同处2：Map入参的构造器，对Map排序。
 * 
 * 主要目的是使JSON格式的字符串，有一个排列顺序
 * 
 * @author      ZhengWei(HY)
 * @createDate  2014-11-30
 * @version     v1.0
 *              v2.0  2021-12-13  添加：从JSONObject转本类时，自动排序后初始的功能
 *              v3.0  2026-05-08  升级：引用Json-Smart 2.6.0 版本的源码
 *                                升级：对于末尾是数字的按自然数排序
 * 
 * @see net.minidev.json.JSONObject;
 */
public class XJSONObject extends LinkedHashMap<String ,Object> implements JSONAware ,JSONAwareEx ,JSONStreamAwareEx
{

    private static final long serialVersionUID = -503443796854799292L;

    public XJSONObject() {
      super();
    }

    public XJSONObject(int initialCapacity) {
      super(initialCapacity);
    }

    /**
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
     * It's the same as JSONValue.escape() only for compatibility here.
     *
     * @see JSONValue#escape(String)
     */
    public static String escape(String s) {
      return JSONValue.escape(s);
    }

    public static String toJSONString(Map<String, ? extends Object> map) {
      return toJSONString(map, JSONValue.COMPRESSION);
    }

    /**
     * Convert a map to JSON text. The result is a JSON object. If this map is also a JSONAware,
     * JSONAware specific behaviours will be omitted at this top level.
     *
     * @see net.minidev.json.JSONValue#toJSONString(Object)
     * @param map
     * @return JSON text, or "null" if map is null.
     */
    public static String toJSONString(Map<String, ? extends Object> map, JSONStyle compression) {
      StringBuilder sb = new StringBuilder();
      try {
        writeJSON(map, sb, compression);
      } catch (IOException e) {
        // can not append on a StringBuilder
      }
      return sb.toString();
    }

    /** Write a Key : value entry to a stream */
    public static void writeJSONKV(String key, Object value, Appendable out, JSONStyle compression)
        throws IOException {
      if (key == null) out.append("null");
      else if (!compression.mustProtectKey(key)) out.append(key);
      else {
        out.append('"');
        JSONValue.escape(key, out, compression);
        out.append('"');
      }
      out.append(':');
      if (value instanceof String) compression.writeString(out, (String) value);
      else JSONValue.writeJSONString(value, out, compression);
    }

    /**
     * Puts value to object and returns this. Handy alternative to put(String key, Object value)
     * method.
     *
     * @param fieldName key with which the specified value is to be associated
     * @param fieldValue value to be associated with the specified key
     * @return this
     */
    public XJSONObject appendField(String fieldName, Object fieldValue) {
      put(fieldName, fieldValue);
      return this;
    }

    /**
     * A Simple Helper object to String
     *
     * @return a value.toString() or null
     */
    public String getAsString(String key) {
      Object obj = this.get(key);
      if (obj == null) return null;
      return obj.toString();
    }

    /**
     * A Simple Helper cast an Object to an Number
     *
     * @return a Number or null
     */
    public Number getAsNumber(String key) {
      Object obj = this.get(key);
      if (obj == null) return null;
      if (obj instanceof Number) return (Number) obj;
      return Long.valueOf(obj.toString());
    }

    // /**
    // * return a Key:value entry as stream
    // */
    // public static String toString(String key, Object value) {
    // return toString(key, value, JSONValue.COMPRESSION);
    // }

    // /**
    // * return a Key:value entry as stream
    // */
    // public static String toString(String key, Object value, JSONStyle
    // compression) {
    // StringBuilder sb = new StringBuilder();
    // try {
    // writeJSONKV(key, value, sb, compression);
    // } catch (IOException e) {
    // // can not append on a StringBuilder
    // }
    // return sb.toString();
    // }

    /**
     * Allows creation of a JSONObject from a Map. After that, both the generated JSONObject and the
     * Map can be modified independently.
     */
    public XJSONObject(Map<String, ?> map) {
      // super(map);          ZhengWei(HY) 原类的方法
      super(Help.toSortStringInt(map));
    }

    public static void writeJSON(Map<String, ? extends Object> map, Appendable out)
        throws IOException {
      writeJSON(map, out, JSONValue.COMPRESSION);
    }

    /**
     * Encode a map into JSON text and write it to out. If this map is also a JSONAware or
     * JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top
     * level.
     *
     * @see JSONValue#writeJSONString(Object, Appendable)
     */
    public static void writeJSON(
        Map<String, ? extends Object> map, Appendable out, JSONStyle compression) throws IOException {
      if (map == null) {
        out.append("null");
        return;
      }
      JsonWriter.JSONMapWriter.writeJSONString(map, out, compression);
    }

    /** serialize Object as json to an stream */
    public void writeJSONString(Appendable out) throws IOException {
      writeJSON(this, out, JSONValue.COMPRESSION);
    }

    /** serialize Object as json to an stream */
    public void writeJSONString(Appendable out, JSONStyle compression) throws IOException {
      writeJSON(this, out, compression);
    }

    public void merge(Object o2) {
      merge(this, o2, false);
    }

    /**
     * merge two JSONObject with overwrite or not overwrite = false will not overwrite existing key
     * overwrite = true will overwrite the value with o2 of existing key
     */
    public void merge(Object o2, boolean overwrite) {
      merge(this, o2, overwrite);
    }

    protected static XJSONObject merge(XJSONObject o1, Object o2, boolean overwrite) {
      if (o2 == null) return o1;
      if (o2 instanceof XJSONObject) return merge(o1, (XJSONObject) o2, overwrite);
      throw new RuntimeException("JSON merge can not merge XJSONObject with " + o2.getClass());
    }

    private static XJSONObject merge(XJSONObject o1, XJSONObject o2, boolean overwrite) {
      if (o2 == null) return o1;
      for (String key : o1.keySet()) {
        Object value1 = o1.get(key);
        Object value2 = o2.get(key);
        if (value2 == null) continue;
        if (value1 instanceof JSONArray) {
          o1.put(key, merge((JSONArray) value1, value2));
          continue;
        }
        if (value1 instanceof XJSONObject) {
          o1.put(key, merge((XJSONObject) value1, value2, overwrite));
          continue;
        }
        if (value1.equals(value2)) continue;
        if (value1.getClass().equals(value2.getClass())) {
          if (overwrite) {
            o1.put(key, value2);
            continue;
          }
          throw new RuntimeException(
              "JSON merge can not merge two " + value1.getClass().getName() + " Object together");
        }
        throw new RuntimeException(
            "JSON merge can not merge "
                + value1.getClass().getName()
                + " with "
                + value2.getClass().getName());
      }
      for (String key : o2.keySet()) {
        if (o1.containsKey(key)) continue;
        o1.put(key, o2.get(key));
      }
      return o1;
    }

    protected static JSONArray merge(JSONArray o1, Object o2) {
      if (o2 == null) return o1;
      if (o2 instanceof JSONArray) {
        return merge(o1, (JSONArray) o2);
      }
      o1.add(o2);
      return o1;
    }

    private static JSONArray merge(JSONArray o1, JSONArray o2) {
      o1.addAll(o2);
      return o1;
    }

    public String toJSONString() {
      return toJSONString(this, JSONValue.COMPRESSION);
    }

    public String toJSONString(JSONStyle compression) {
      return toJSONString(this, compression);
    }

    public String toString(JSONStyle compression) {
      return toJSONString(this, compression);
    }

    public String toString() {
      return toJSONString(this, JSONValue.COMPRESSION);
    }
    
}