package org.hy.common.xml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.ListMap;





/**
 * XML的工具类
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2014-04-28
 */
public final class XmlHelp
{
    private static ListMap<Class<?> ,SerializableClass> $Buffer;
    
    /**
     * 缓存最大值
     * 
     * 当缓存达到一定大小($BufferSize)后，最早缓存的数据将被移出缓存
     */
    private static int                                  $BufferSize = 1024;
    
    
    
    /**
     * 将Java对象转为XML字符串
     * 
     * @param i_Obj
     * @return
     */
    public static String toXML(Object i_Obj)
    {
        return toXML(i_Obj ,null);
    }
    
    
    
    /**
     * 将Java对象转为XML字符串
     * 
     * @param i_Obj
     * @param i_RowName  XML字符串行级标签名
     * @return
     */
    public static String toXML(Object i_Obj ,String i_RowName)
    {
        if ( i_Obj == null )
        {
            return "";
        }
        initBuffer();
        
        
        StringBuilder     v_Ret          = new StringBuilder();
        SerializableClass v_Serializable = null;
        
        
        
        if ( i_Obj instanceof List )
        {
            v_Ret.append("<").append(Help.NVL(i_RowName ,"LIST")).append(">");
            
            List<?> v_ChildElement = (List<?>)i_Obj;
            
            for (int x=0; x<v_ChildElement.size(); x++)
            {
                Object v_Value = v_ChildElement.get(x);
                
                v_Ret.append("<C").append(x).append(">");
                v_Ret.append(v_Value == null ? "" : v_Value);
                v_Ret.append("</C").append(x).append(">");
            }
            
            v_Ret.append("</").append(Help.NVL(i_RowName ,"LIST")).append(">");
        }
        else if ( i_Obj instanceof Map )
        {
            v_Ret.append("<").append(Help.NVL(i_RowName ,"MAP")).append(">");
            
            Map<? ,?>   v_ChildElement = (Map<? ,?>)i_Obj;
            Iterator<?> v_Iterator     = v_ChildElement.values().iterator();
            int         x              = 0;
            
            while ( v_Iterator.hasNext() )
            {
                Object v_Value = v_Iterator.next();
                
                v_Ret.append("<C").append(x).append(">");
                v_Ret.append(v_Value == null ? "" : v_Value);
                v_Ret.append("</C").append(x).append(">");
                
                x++;
            }
            
            v_Ret.append("</").append(Help.NVL(i_RowName ,"MAP")).append(">");
        }
        else
        {
            v_Ret.append("<").append(Help.NVL(i_RowName ,i_Obj.getClass().getSimpleName())).append(">");
            
            if ( $Buffer.containsKey(i_Obj.getClass()) )
            {
                v_Serializable = $Buffer.get(i_Obj.getClass());
            }
            else
            {
                v_Serializable = addBuffer(i_Obj.getClass());
            }
            
            for (int x=0; x<v_Serializable.gatPropertySize(); x++)
            {
                Object v_Value = v_Serializable.gatPropertyValue(x ,i_Obj);
                
                v_Ret.append("<").append(v_Serializable.gatPropertyName(x)).append(">");
                v_Ret.append(v_Value == null ? "" : v_Value);
                v_Ret.append("</").append(v_Serializable.gatPropertyName(x)).append(">");
            }
            
            v_Ret.append("</").append(Help.NVL(i_RowName ,i_Obj.getClass().getSimpleName())).append(">");
        }
        
        return v_Ret.toString();
    }
    
    
    
    /**
     * 将集合对象转为XML字符串
     * 
     * @param i_List
     * @return
     */
    public static String toXML(List<?> i_List)
    {
        return toXML(i_List ,null ,null);
    }
    
    
    
    /**
     * 将集合对象转为XML字符串
     * 
     * @param i_List
     * @param i_RootName  XML字符串的根标签名
     * @return
     */
    public static String toXML(List<?> i_List ,String i_RootName)
    {
        return toXML(i_List ,i_RootName ,null);
    }
    
    
    
    /**
     * 将集合对象转为XML字符串
     * 
     * @param i_List
     * @param i_RootName  XML字符串的根标签名
     * @param i_RowName   XML字符串行级标签名
     * @return
     */
    public static String toXML(List<?> i_List ,String i_RootName ,String i_RowName)
    {
        if ( Help.isNull(i_List) )
        {
            return "";
        }
        initBuffer();
        
        
        Object            v_Element      = null;
        SerializableClass v_Serializable = null;
        StringBuilder      v_Ret         = new StringBuilder();
        
        
        v_Ret.append("<").append(Help.NVL(i_RootName ,"RS")).append(">");
        
        for (int i=0; i<i_List.size(); i++)
        {
            v_Element = i_List.get(i);
            
            if ( v_Element instanceof List )
            {
                v_Ret.append("<").append(Help.NVL(i_RowName ,"LIST")).append(">");
                
                List<?> v_ChildElement = (List<?>)v_Element;
                
                for (int x=0; x<v_ChildElement.size(); x++)
                {
                    Object v_Value = v_ChildElement.get(x);
                    
                    v_Ret.append("<C").append(x).append(">");
                    v_Ret.append(v_Value == null ? "" : v_Value);
                    v_Ret.append("</C").append(x).append(">");
                }
                
                v_Ret.append("</").append(Help.NVL(i_RowName ,"LIST")).append(">");
            }
            else if ( v_Element instanceof Map )
            {
                v_Ret.append("<").append(Help.NVL(i_RowName ,"MAP")).append(">");
                
                Map<? ,?>   v_ChildElement = (Map<? ,?>)v_Element;
                Iterator<?> v_Iterator     = v_ChildElement.values().iterator();
                int         x              = 0;
                
                while ( v_Iterator.hasNext() )
                {
                    Object v_Value = v_Iterator.next();
                    
                    v_Ret.append("<C").append(x).append(">");
                    v_Ret.append(v_Value == null ? "" : v_Value);
                    v_Ret.append("</C").append(x).append(">");
                    
                    x++;
                }
                
                v_Ret.append("</").append(Help.NVL(i_RowName ,"MAP")).append(">");
            }
            else
            {
                v_Ret.append("<").append(Help.NVL(i_RowName ,v_Element.getClass().getSimpleName())).append(">");
                
                if ( $Buffer.containsKey(v_Element.getClass()) )
                {
                    v_Serializable = $Buffer.get(v_Element.getClass());
                }
                else
                {
                    v_Serializable = addBuffer(v_Element.getClass());
                }
                
                for (int x=0; x<v_Serializable.gatPropertySize(); x++)
                {
                    Object v_Value = v_Serializable.gatPropertyValue(x ,v_Element);
                    
                    v_Ret.append("<").append(v_Serializable.gatPropertyName(x)).append(">");
                    v_Ret.append(v_Value == null ? "" : v_Value);
                    v_Ret.append("</").append(v_Serializable.gatPropertyName(x)).append(">");
                }
                
                v_Ret.append("</").append(Help.NVL(i_RowName ,v_Element.getClass().getSimpleName())).append(">");
            }
        }
        
        v_Ret.append("</").append(Help.NVL(i_RootName ,"RS")).append(">");
        
        return v_Ret.toString();
    }
    
    
    
    /**
     * 初始化缓存信息
     */
    private static synchronized void initBuffer()
    {
        if ( $Buffer == null )
        {
            $Buffer = new ListMap<Class<?> ,SerializableClass>();
        }
    }
    
    
    
    /**
     * 向缓存中添加新的信息
     * 
     * 当缓存达到一定大小($BufferSize)后，最早缓存的数据将被移出缓存。
     * 
     * @param i_Class
     * @return
     */
    private static synchronized SerializableClass addBuffer(Class<?> i_Class)
    {
        if ( $Buffer.size() > $BufferSize )
        {
            $Buffer.remove(0);
        }
        
        SerializableClass v_Serializable = new SerializableClass(i_Class);
        
        $Buffer.put(i_Class ,v_Serializable);
        
        return v_Serializable;
    }
    
}
