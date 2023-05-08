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





/**
 * 重写 JSONObject 类，不同是：继承了 LinkedHashMap。其它都是一样的。
 * 
 * 主要目的是使JSON格式的字符串，有一个排列顺序
 * 
 * @author      ZhengWei(HY)
 * @createDate  2014-11-30
 * @version     v1.0
 *              v2.0  2021-12-13  添加：从JSONObject转本类时，自动排序后初始的功能
 * 
 * @see net.minidev.json.JSONObject;
 */
public class XJSONObject extends LinkedHashMap<String ,Object> implements JSONAware ,JSONAwareEx ,JSONStreamAwareEx
{

    private static final long serialVersionUID = -503443796854799292L;



    public XJSONObject()
    {
        super();
    }



    // /**
    // * Allow simply casting to Map<String, XX>
    // */
    // @SuppressWarnings("unchecked")
    // public <T> T cast() {
    // return (T) this;
    // }
    /**
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
     * (U+0000 through U+001F). It's the same as JSONValue.escape() only for
     * compatibility here.
     * 
     * @see JSONValue#escape(String)
     */
    public static String escape(String s)
    {
        return JSONValue.escape(s);
    }



    public static String toJSONString(Map<String ,? extends Object> map)
    {
        return toJSONString(map ,JSONValue.COMPRESSION);
    }



    /**
     * Convert a map to JSON text. The result is a JSON object. If this map is
     * also a JSONAware, JSONAware specific behaviours will be omitted at this
     * top level.
     * 
     * @see net.minidev.json.JSONValue#toJSONString(Object)
     * 
     * @param map
     * @return JSON text, or "null" if map is null.
     */
    public static String toJSONString(Map<String ,? extends Object> map ,JSONStyle compression)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            writeJSON(map ,sb ,compression);
        }
        catch (IOException e)
        {
            // can not append on a StringBuilder
        }
        return sb.toString();
    }



    /**
     * Write a Key : value entry to a stream
     */
    public static void writeJSONKV(String key ,Object value ,Appendable out ,JSONStyle compression) throws IOException
    {
        if ( key == null )
            out.append("null");
        else if ( !compression.mustProtectKey(key) )
            out.append(key);
        else
        {
            out.append('"');
            JSONValue.escape(key ,out ,compression);
            out.append('"');
        }
        out.append(':');
        if ( value instanceof String )
        {
            if ( !compression.mustProtectValue((String) value) )
                out.append((String) value);
            else
            {
                out.append('"');
                JSONValue.escape((String) value ,out ,compression);
                out.append('"');
            }
        }
        else
            JSONValue.writeJSONString(value ,out ,compression);
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
     * Allows creation of a JSONObject from a Map. After that, both the
     * generated JSONObject and the Map can be modified independently.
     */
    public XJSONObject(Map<String ,?> map)
    {
        super(Help.toSort(map));
    }



    public static void writeJSON(Map<String ,Object> map ,Appendable out) throws IOException
    {
        writeJSON(map ,out ,JSONValue.COMPRESSION);
    }



    /**
     * Encode a map into JSON text and write it to out. If this map is also a
     * JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific
     * behaviours will be ignored at this top level.
     * 
     * @see JSONValue#writeJSONString(Object, Appendable)
     */
    public static void writeJSON(Map<String ,? extends Object> map ,Appendable out ,JSONStyle compression) throws IOException
    {
        if ( map == null )
        {
            out.append("null");
            return;
        }
        // JSONStyler styler = compression.getStyler();
        boolean first = true;
        // if (styler != null) {
        // styler.objectIn();
        // }
        out.append('{');
        /**
         * do not use <String, Object> to handle non String key maps
         */
        for (Map.Entry<? ,?> entry : map.entrySet())
        {
            if ( first )
            {
                first = false;
            }
            else
            {
                out.append(',');
            }
            // if (styler != null)
            // out.append(styler.getNewLine());
            writeJSONKV(entry.getKey().toString() ,entry.getValue() ,out ,compression);
        }
        // if (styler != null) {
        // styler.objectOut();
        // }
        out.append('}');
        // if (styler != null) {
        // out.append(styler.getNewLine());
        // }
    }



    /**
     * serialize Object as json to an stream
     */
    @Override
    public void writeJSONString(Appendable out) throws IOException
    {
        writeJSON(this ,out ,JSONValue.COMPRESSION);
    }



    /**
     * serialize Object as json to an stream
     */
    @Override
    public void writeJSONString(Appendable out ,JSONStyle compression) throws IOException
    {
        writeJSON(this ,out ,compression);
    }



    public void merge(Object o2)
    {
        merge(this ,o2);
    }



    protected static XJSONObject merge(XJSONObject o1 ,Object o2)
    {
        if ( o2 == null )
        {
            return o1;
        }
        if ( o2 instanceof XJSONObject )
        {
            return merge(o1 ,(XJSONObject) o2);
        }
        throw new RuntimeException("JSON megre can not merge JSONObject with " + o2.getClass());
    }



    private static XJSONObject merge(XJSONObject o1 ,XJSONObject o2)
    {
        if ( o2 == null )
        {
            return o1;
        }
        for (String key : o1.keySet())
        {
            Object value1 = o1.get(key);
            Object value2 = o2.get(key);
            if ( value2 == null )
                continue;
            if ( value1 instanceof JSONArray )
            {
                o1.put(key ,merge((JSONArray) value1 ,value2));
                continue;
            }
            if ( value1 instanceof XJSONObject )
            {
                o1.put(key ,merge((XJSONObject) value1 ,value2));
                continue;
            }
            if ( value1.equals(value2) )
                continue;
            throw new RuntimeException("JSON megre can not merge " + value1.getClass() + " with " + value2.getClass());
        }
        for (String key : o2.keySet())
        {
            if ( o1.containsKey(key) )
                continue;
            o1.put(key ,o2.get(key));
        }
        return o1;
    }



    protected static JSONArray merge(JSONArray o1 ,Object o2)
    {
        if ( o2 == null )
        {
            return o1;
        }
        if ( o1 instanceof JSONArray )
        {
            return merge(o1 ,(JSONArray) o2);
        }
        o1.add(o2);
        return o1;
    }



    private static JSONArray merge(JSONArray o1 ,JSONArray o2)
    {
        o1.addAll(o2);
        return o1;
    }



    @Override
    public String toJSONString()
    {
        return toJSONString(this ,JSONValue.COMPRESSION);
    }



    @Override
    public String toJSONString(JSONStyle compression)
    {
        return toJSONString(this ,compression);
    }



    public String toString(JSONStyle compression)
    {
        return toJSONString(this ,compression);
    }



    @Override
    public String toString()
    {
        return toJSONString(this ,JSONValue.COMPRESSION);
    }
    
}