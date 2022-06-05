package org.hy.common.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.ByteHelp;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.comparate.MethodComparator;
import org.hy.common.license.base64.Base64Factory;
import org.hy.common.net.data.protobuf.CommunicationProtoEncoder;

import net.minidev.json.JSONArray;





/**
 * 最小粒度级的Java转Json功能。
 * 
 * 注：为了性能，此类所有方法均不考虑入参为NULL的情况
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2021-12-09
 */
public class XJSONToJson
{
    /** 当Java对象转Json字符串时，排除哪些方法不输出 */
    private static final String [] $ExcludeMethodNames = {"getClass"
                                                         ,"hashCode"
                                                         ,"equals"
                                                         ,"clone"
                                                         ,"toString"
                                                         ,"notify"
                                                         ,"notifyAll"
                                                         ,"wait"
                                                         ,"finalize"
                                                         ,"compareTo"
                                                         ,"toString"};
    
    /** Java元类型与ToJson方法的映射关系。为了性能而存在，优化之前多if语句的判定 */
    private static final Map<Class<?> ,Method> $ToJsonMethods             = new HashMap<Class<?> ,Method>();
    
    /** List系列实现的映射关系的ToJson方法 */
    private static       Method                $ToJsonDefault_List        = null;
    
    /** Set系列实现的映射关系的ToJson方法 */
    private static       Method                $ToJsonDefault_Set         = null;
    
    /** Map系列实现的映射关系的ToJson方法 */
    private static       Method                $ToJsonDefault_Map         = null;
    
    /** Enum系列实现的映射关系的ToJson方法 */
    private static       Method                $ToJsonDefault_Enum        = null;
    
    /** 当找不到映射关系时，默认的ToJson方法。一般用于自定义Java Bean的情况 */
    private static       Method                $ToJsonDefault_Object      = null;
    
    /** 数组找不到映射关系时，默认的ToJson方法 */
    private static       Method                $ToJsonDefault_ArrayObject = null;
    
    
    
    /** 初始化Java元类型与ToJson方法的映射关系 */
    static
    {
        List<Method> v_ToJsonMethods = MethodReflect.getMethods(XJSONToJson.class ,"toJson" ,3);
        
        if ( !Help.isNull(v_ToJsonMethods) )
        {
            for (Method v_Method : v_ToJsonMethods)
            {
                Class<?> v_JavaValueClass = v_Method.getParameters()[2].getType();
                $ToJsonMethods.put(v_JavaValueClass ,v_Method);
                
                if ( v_JavaValueClass == Object.class )
                {
                    $ToJsonDefault_Object = v_Method;
                }
                else if ( v_JavaValueClass == Object[].class )
                {
                    $ToJsonDefault_ArrayObject = v_Method;
                }
                else if ( v_JavaValueClass == List.class )
                {
                    $ToJsonDefault_List = v_Method;
                }
                else if ( v_JavaValueClass == Set.class )
                {
                    $ToJsonDefault_Set = v_Method;
                }
                else if ( v_JavaValueClass == Map.class )
                {
                    $ToJsonDefault_Map = v_Method;
                }
                else if ( v_JavaValueClass == Enum.class )
                {
                    $ToJsonDefault_Enum = v_Method;
                }
            }
        }
    }
    
    
    
    /**
     * 匹配ToJson方法
     * 
     * 兜兜转转一圈，只为了优化之前多if语句的判定 和 易扩展性的设计。
     * 
     * 为什么重载ToJson方法还是不能达到优化多if的目标呢？
     * 原因：在多次动态反映时要用到Method.invoke()方法，而它的返回值是Object类型，
     *      并且返回结果也是要参与ToJson方法的转换，此时引发重载ToJson无效，JVM只能识别ToJson(XJSON ,Map ,Object)方法，
     *      无法识别其它重载的ToJson方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_JavaData  待转的对象
     * @return            约定：不会返回NULL
     */
    public static <T> Method findToJson(T i_JavaData)
    {
        Class<?> v_JavaDataClass = i_JavaData.getClass();
        Method   v_Ret           = $ToJsonMethods.get(v_JavaDataClass);
        
        if ( v_Ret == null )
        {
            // 对常用接口，做适配性处理
            if ( v_JavaDataClass.isArray() )
            {
                v_Ret = $ToJsonDefault_ArrayObject;
            }
            else if ( i_JavaData instanceof List )
            {
                v_Ret = $ToJsonDefault_List;
            }
            else if ( i_JavaData instanceof Set )
            {
                v_Ret = $ToJsonDefault_Set;
            }
            else if ( i_JavaData instanceof Map )
            {
                v_Ret = $ToJsonDefault_Map;
            }
            else if ( i_JavaData instanceof Enum )
            {
                v_Ret = $ToJsonDefault_Enum;
            }
        }
        
        return v_Ret == null ? $ToJsonDefault_Object : v_Ret;
    }
    
    
    
    /**
     * 匹配并执行ToJson方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param <T>
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static <T> Object executeToJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,T i_JavaData) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return findToJson(i_JavaData).invoke(null ,new Object[] {i_XJson ,i_ParserObjects ,i_JavaData});
    }
    
    
    
    /**
     * String转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,String i_JavaData)
    {
        return i_JavaData;
    }
    
    
    
    /**
     * boolean转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,boolean i_JavaData)
    {
        return String.valueOf(i_JavaData);
    }
    
    
    
    /**
     * Boolean转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Boolean i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * Boolean转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Enum<?> i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * byte转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,byte i_JavaData)
    {
        return String.valueOf(i_JavaData);
    }
    
    
    
    /**
     * Byte转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Byte i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * char转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,char i_JavaData)
    {
        return String.valueOf(i_JavaData);
    }
    
    
    
    /**
     * Character转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Character i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * short转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,short i_JavaData)
    {
        return String.valueOf(i_JavaData);
    }
    
    
    
    /**
     * Short转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Short i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * int转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,int i_JavaData)
    {
        return String.valueOf(i_JavaData);
    }
    
    
    
    /**
     * Integer转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Integer i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * long转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,long i_JavaData)
    {
        return String.valueOf(i_JavaData);
    }
    
    
    
    /**
     * Long转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Long i_JavaData)
    {
        return i_JavaData.toString();
    }
    
    
    
    /**
     * double转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,double i_JavaData)
    {
        if ( i_XJson.getDigit() != null )
        {
            return String.valueOf(Help.round(i_JavaData ,i_XJson.getDigit()));
        }
        else
        {
            return ((Double)i_JavaData).toString();
        }
    }
    
    
    
    /**
     * Double转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Double i_JavaData)
    {
        if ( i_XJson.getDigit() != null )
        {
            return String.valueOf(Help.round(i_JavaData ,i_XJson.getDigit()));
        }
        else
        {
            return i_JavaData.toString();
        }
    }
    
    
    
    /**
     * float转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,float i_JavaData)
    {
        if ( i_XJson.getDigit() != null )
        {
            return String.valueOf(Help.round(i_JavaData ,i_XJson.getDigit()));
        }
        else
        {
            return ((Float)i_JavaData).toString();
        }
    }
    
    
    
    /**
     * Float转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Float i_JavaData)
    {
        if ( i_XJson.getDigit() != null )
        {
            return String.valueOf(Help.round(i_JavaData ,i_XJson.getDigit()));
        }
        else
        {
            return i_JavaData.toString();
        }
    }
    
    
    
    /**
     * BigDecimal转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,BigDecimal i_JavaData)
    {
        BigDecimal v_BigValue = null;
        
        if ( i_XJson.getDigit() != null )
        {
            v_BigValue = new BigDecimal(Help.round(i_JavaData.toString() ,i_XJson.getDigit()));
        }
        else
        {
            v_BigValue = i_JavaData;
        }
        
        if ( i_XJson.isBigDecimalFormat() )
        {
            // 科学计数
            return v_BigValue.toString();
        }
        else
        {
            // 自然数字
            return v_BigValue.toPlainString();
        }
    }
    
    
    
    /**
     * Date转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Date i_JavaData)
    {
        return i_JavaData.getFull();
    }
    
    
    
    /**
     * java.util.Date转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,java.util.Date i_JavaData)
    {
        return (new Date(i_JavaData)).getFull();
    }
    
    
    
    /**
     * java.sql.Date转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,java.sql.Date i_JavaData)
    {
        return (new Date(i_JavaData)).getFull();
    }
    
    
    
    /**
     * Timestamp转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Timestamp i_JavaData)
    {
        return (new Date(i_JavaData)).getFullMilli();
    }
    
    
    
    /**
     * Class转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Class<?> i_JavaData)
    {
        return i_JavaData.getName();
    }
    
    
    
    /**
     * List转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,List<?> i_JavaData) throws Exception
    {
        if ( i_XJson.isRecursion(i_ParserObjects ,i_JavaData) )
        {
            return null;
        }
        
        JSONArray v_JSONArray = new JSONArray();
        List<?>   v_List      = i_JavaData;
        
        for (int i=0; i<v_List.size(); i++)
        {
            Object              v_Value   = v_List.get(i);
            Return<XJSONObject> v_RetTemp = i_XJson.parser("" + i ,v_Value == null ? "" : v_Value ,new XJSONObject() ,i_ParserObjects ,false);
            
            if ( v_RetTemp.paramObj != null && v_RetTemp.paramObj.size() >= 1 )
            {
                v_JSONArray.addAll(v_RetTemp.paramObj.values());
            }
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Set转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Set<?> i_JavaData) throws Exception
    {
        if ( i_XJson.isRecursion(i_ParserObjects ,i_JavaData) )
        {
            return null;
        }
        
        JSONArray   v_JSONArray = new JSONArray();
        Set<?>      v_Set       = i_JavaData;
        Iterator<?> v_Values    = v_Set.iterator();
        int         v_SetIndex  = 0;
        
        while ( v_Values.hasNext() )
        {
            Object              v_Value   = v_Values.next();
            Return<XJSONObject> v_RetTemp = i_XJson.parser("" + (v_SetIndex++) ,v_Value == null ? "" : v_Value ,new XJSONObject() ,i_ParserObjects ,false);
            
            if ( v_RetTemp.paramObj != null && v_RetTemp.paramObj.size() >= 1 )
            {
                v_JSONArray.addAll(v_RetTemp.paramObj.values());
            }
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Map转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static XJSONObject toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Map<? ,?> i_JavaData) throws Exception
    {
        if ( i_XJson.isRecursion(i_ParserObjects ,i_JavaData) )
        {
            return null;
        }
        
        XJSONObject v_ChildJsonObj = new XJSONObject();
        
        for (Map.Entry<?, ?> v_Item : i_JavaData.entrySet())
        {
            Object v_Name  = v_Item.getKey();
            Object v_Value = v_Item.getValue();
            
            if ( v_Name == null )
            {
                v_Name = "";
            }
            if ( v_Item.getValue() == null )
            {
                v_Value = "";
            }
            
            // 对于Map集合，是可以支持每个元素均不相同的功能，所以 i_MethodReturnIsObject 传 true，
            // 当作Object对象来处理  2021-09-30
            i_XJson.parser(v_Name.toString() ,v_Value ,v_ChildJsonObj ,i_ParserObjects ,true);
        }
        
        return v_ChildJsonObj;
    }
    
    
    
    /**
     * byte [] 转Json
     * 
     * 在大量传输byte[]数据时，如果按数组处理Json，生成的Json信息量太大了，所以按字符串传输
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return                 isSerializable = true 时，返回 XJSONObject
     * @return                 isSerializable = false时，返回 JSONArray
     * @throws Exception
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,byte [] i_JavaData)
    {
        return new String(Base64Factory.getIntance().encode(i_JavaData) ,CommunicationProtoEncoder.$Charset);
    }
    
    
    
    /**
     * Byte [] 转Json
     * 
     * 在大量传输byte[]数据时，如果按数组处理Json，生成的Json信息量太大了，所以按字符串传输
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return                 isSerializable = true 时，返回 XJSONObject
     * @return                 isSerializable = false时，返回 JSONArray
     * @throws Exception
     */
    public static String toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Byte [] i_JavaData)
    {
        return new String(Base64Factory.getIntance().encode(ByteHelp.byteToByte(i_JavaData)) ,CommunicationProtoEncoder.$Charset);
    }
    
    
    
    /**
     * String [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,String [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * boolean [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,boolean [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Boolean [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Boolean [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * char [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,char [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Character [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Character [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * short [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,short [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Short [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Short [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * int [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,int [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Integer [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Integer [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * long [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,long [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Long [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Long [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * double [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,double [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Double [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Double [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * float [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,float [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
            
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,i_JavaData[i]));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Float [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Float [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * BigDecimal [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,BigDecimal [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Class [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static JSONArray toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Class<?> [] i_JavaData)
    {
        JSONArray v_JSONArray = new JSONArray();
        
        for (int i=0; i<i_JavaData.length; i++)
        {
            v_JSONArray.add(XJSONToJson.toJson(i_XJson ,i_ParserObjects ,Help.NVL(i_JavaData[i])));
        }
        
        return v_JSONArray;
    }
    
    
    
    /**
     * Object [] 转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return                 isSerializable = true 时，返回 XJSONObject
     * @return                 isSerializable = false时，返回 JSONArray
     * @throws Exception
     */
    public static Object toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Object [] i_JavaData) throws Exception
    {
        if ( i_XJson.isRecursion(i_ParserObjects ,i_JavaData) )
        {
            return null;
        }
        
        if ( i_XJson.isSerializable() )
        {
            XJSONObject v_ChildJsonObj = new XJSONObject();
            
            for (int i=0; i<i_JavaData.length; i++)
            {
                Object v_Value = i_JavaData[i];
                // 对于数组，是可以支持每个元素均不相同的功能，所以 i_MethodReturnIsObject 传 true，
                // 当作Object对象来处理  2021-12-09
                i_XJson.parser("" + i ,v_Value == null ? "" : v_Value ,v_ChildJsonObj ,i_ParserObjects ,true);
            }
            
            return v_ChildJsonObj;
        }
        else
        {
            JSONArray v_JSONArray = new JSONArray();
            
            for (int i=0; i<i_JavaData.length; i++)
            {
                Object              v_Value   = i_JavaData[i];
                Return<XJSONObject> v_RetTemp = i_XJson.parser("" + i ,v_Value == null ? "" : v_Value ,new XJSONObject() ,i_ParserObjects ,false);
                
                if ( v_RetTemp.paramObj != null && v_RetTemp.paramObj.size() >= 1 )
                {
                    v_JSONArray.addAll(v_RetTemp.paramObj.values());
                }
            }
            
            return v_JSONArray;
        }
    }
    
    
    
    /**
     * Object转Json
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-09
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_ParserObjects  解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_JavaData       待转的对象
     * @return
     * @throws Exception
     */
    public static XJSONObject toJson(XJSON i_XJson ,Map<Object ,Integer> i_ParserObjects ,Object i_JavaData) throws Exception
    {
        if ( !Modifier.isPublic(i_JavaData.getClass().getModifiers()) )
        {
            return null;
        }
        if ( i_XJson.isRecursion(i_ParserObjects ,i_JavaData) )
        {
            return null;
        }
        
        XJSONObject v_ChildJsonObj = new XJSONObject();
        
        if ( i_XJson.isAccuracy() )
        {
            Map<String ,Method> v_Methods = MethodReflect.getGetMethodsMS(i_JavaData.getClass());
            
            if ( !Help.isNull(v_Methods) )
            {
                for (Map.Entry<String ,Method> v_ItemMethod : v_Methods.entrySet())
                {
                    String v_Name  = null;
                    Object v_Value = null;
                    
                    if ( i_XJson.isFirstCharIsUpper() )
                    {
                        v_Name = v_ItemMethod.getKey();
                    }
                    else
                    {
                        v_Name = StringHelp.toLowerCaseByFirst(v_ItemMethod.getKey());
                    }
                    
                    try
                    {
                        Method  v_Method     = v_ItemMethod.getValue();
                        boolean v_IsMRObject = Object.class.equals(v_Method.getReturnType()); // 2021-09-30 添加：判定方法的返回类型是否为java.lang.Object
                        v_Value = v_Method.invoke(i_JavaData);
                        if ( v_Value == null )
                        {
                            v_Value = "";
                        }
                        i_XJson.parser(v_Name ,v_Value ,v_ChildJsonObj ,i_ParserObjects ,v_IsMRObject);
                    }
                    catch (Exception e)
                    {
                        throw new Exception(v_Name + ":" + e.getMessage());
                    }
                }
            }
        }
        else
        {
            List<Method> v_Methods   = MethodReflect.getStartMethods(i_JavaData.getClass() ,new String[]{"get" ,"is"} ,0);
            Method []    v_MethodArr = v_Methods.toArray(new Method[]{});
            
            Arrays.sort(v_MethodArr ,MethodComparator.getInstance());
            
            if ( !Help.isNull(v_MethodArr) )
            {
                for (int i=0; i<v_MethodArr.length; i++)
                {
                    Method v_Method = v_MethodArr[i];
                    
                    if ( "getClass".equals(v_Method.getName()) )
                    {
                        continue;
                    }
                    if ( v_Method.getReturnType() == i_JavaData.getClass() )
                    {
                        // 防止死循环：方法返回值的类型就是类自己
                        continue;
                    }
                    if ( v_Method.getReturnType() == java.sql.Connection.class || v_Method.getReturnType() == org.hy.common.db.Connection.class )
                    {
                        // 不解释数据库连接
                        continue;
                    }
                    
                    String v_Name  = null;
                    Object v_Value = null;
                    
                    if ( v_Method.getName().startsWith("get") && v_Method.getName().length() > 3 )
                    {
                        if ( i_XJson.isFirstCharIsUpper() )
                        {
                            v_Name = v_Method.getName().substring(3);
                        }
                        else
                        {
                            v_Name = v_Method.getName().substring(3 ,4).toLowerCase();
                                    
                            if ( v_Method.getName().length() >= 5 )
                            {
                                v_Name += v_Method.getName().substring(4);
                            }
                        }
                    }
                    else if ( v_Method.getName().startsWith("is") && v_Method.getName().length() > 2 )
                    {
                        if ( i_XJson.isFirstCharIsUpper() )
                        {
                            v_Name = v_Method.getName().substring(2);
                        }
                        else
                        {
                            v_Name = v_Method.getName().substring(2 ,3).toLowerCase() + v_Method.getName().substring(3);
                        }
                    }
                    
                    if ( !Help.isNull(v_Name) )
                    {
                        try
                        {
                            boolean v_IsMRObject = Object.class.equals(v_Method.getReturnType()); // 2021-09-30 添加：判定方法的返回类型是否为java.lang.Object
                            v_Value = v_Method.invoke(i_JavaData);
                            
                            if ( v_Value != null )
                            {
                                i_XJson.parser(v_Name ,v_Value ,v_ChildJsonObj ,i_ParserObjects ,v_IsMRObject);
                            }
                            else
                            {
                                if ( v_ChildJsonObj == null )
                                {
                                    v_ChildJsonObj = new XJSONObject();
                                }
                                
                                if ( i_XJson.isReturnNVL() )
                                {
                                    v_ChildJsonObj.put(v_Name ,"");
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            throw new Exception(v_Name + ":" + e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Json方式显示成员方法  Add ZhengWei(HY) 2020-01-15
        if ( i_XJson.isJsonMethod() )
        {
            List<Method> v_Methods = MethodReflect.getMethodsExcludeStart(i_JavaData.getClass() ,new String[]{"get"
                                                                                                        ,"set"
                                                                                                        ,"is"
                                                                                                        ,"gatProperty"
                                                                                                        ,"gatDoc"});
            
            if ( !Help.isNull(v_Methods) )
            {
                Method [] v_MethodArr = v_Methods.toArray(new Method[]{});
                
                Arrays.sort(v_MethodArr ,MethodComparator.getInstance());
                
                for (int i=0; i<v_MethodArr.length; i++)
                {
                    Method v_Method = v_MethodArr[i];
                    
                    if ( StringHelp.isEquals(v_Method.getName() ,$ExcludeMethodNames) )
                    {
                        continue;
                    }
                    
                    StringBuilder v_Buffer = new StringBuilder();
                    
                    v_Buffer.append(v_Method.getName()).append("(");
                    
                    Class<?> [] v_ParamTypes = v_Method.getParameterTypes();
                    if ( !Help.isNull(v_ParamTypes) )
                    {
                        for (int pi=0; pi<v_ParamTypes.length; pi++)
                        {
                            if ( pi >= 1 )
                            {
                                v_Buffer.append(",");
                            }
                            v_Buffer.append(v_ParamTypes[pi].getSimpleName());
                        }
                    }
                    
                    v_Buffer.append("):").append(v_Method.getReturnType().getSimpleName());
                    
                    i_XJson.parser(v_Buffer.toString() ,"" ,v_ChildJsonObj ,i_ParserObjects ,false);
                }
            }
        }
        
        return v_ChildJsonObj;
    }
    
    
    
    private XJSONToJson()
    {
        // 私有的
    }
    
}
