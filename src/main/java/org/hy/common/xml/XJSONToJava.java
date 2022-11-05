package org.hy.common.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.hy.common.ByteHelp;
import org.hy.common.Date;
import org.hy.common.ExpireMap;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.StaticReflect;
import org.hy.common.license.base64.Base64Factory;

import net.minidev.json.JSONArray;





/**
 * 最小粒度级的Json转Java功能。
 * 
 * 注：为了性能，此类所有方法均不考虑入参为NULL的情况
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2021-12-10
 *           V2.0  2022-06-22 添加：支持特殊类型ExpireMap的转Java
 */
public class XJSONToJava
{
    /** Java元类型与toJava方法的映射关系。为了性能而存在，优化之前多if语句的判定 */
    private static final Map<Class<?> ,Method> $ToJavaMethods      = new HashMap<Class<?> ,Method>();
    
    /** List系列实现的映射关系的ToJava方法 */
    private static       Method                $ToJavaDefault_List = null;
    
    /** Set系列实现的映射关系的ToJava方法 */
    private static       Method                $ToJavaDefault_Set  = null;
    
    /** Map系列实现的映射关系的ToJava方法 */
    private static       Method                $ToJavaDefault_Map  = null;
    
    /** Enum系列实现的映射关系的ToJava方法 */
    private static       Method                $ToJavaDefault_Enum = null;
    
    
    
    /** 初始化Java元类型与toJava方法的映射关系 */
    static
    {
        List<Method> v_ToJsonMethods = MethodReflect.getStartMethods(XJSONToJava.class ,"toJava" ,4);
        
        if ( !Help.isNull(v_ToJsonMethods) )
        {
            for (Method v_Method : v_ToJsonMethods)
            {
                Class<?> v_JavaValueClass = v_Method.getReturnType();  // 变相实现方法返回值的重载
                $ToJavaMethods.put(v_JavaValueClass ,v_Method);
                
                if ( v_JavaValueClass == List.class )
                {
                    $ToJavaDefault_List = v_Method;
                }
                else if ( v_JavaValueClass == Set.class )
                {
                    $ToJavaDefault_Set = v_Method;
                }
                else if ( v_JavaValueClass == Map.class )
                {
                    $ToJavaDefault_Map = v_Method;
                }
                else if ( v_JavaValueClass == Enum.class )
                {
                    $ToJavaDefault_Enum = v_Method;
                }
            }
        }
    }
    
    
    
    /**
     * 匹配ToJava方法
     * 
     * 兜兜转转一圈，只为了优化之前多if语句的判定 和 易扩展性的设计。
     * 
     * 为什么重载ToJava方法还是不能达到优化多if的目标呢？
     * 原因：在多次动态反映时要用到Method.invoke()方法，而它的返回值是Object类型，
     *      并且返回结果也是要参与ToJson方法的转换，此时引发重载ToJava无效，JVM只能识别ToJava(XJSON ,Map ,Object)方法，
     *      无法识别其它重载的ToJava方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_JsonDataType   转换成的类型。一般为Java Setter方法的入参类型
     * @return                 约定：允许为NULL，表示未知类型
     */
    public static Method findToJava(Class<?> i_JsonDataType)
    {
        Method v_Ret = $ToJavaMethods.get(i_JsonDataType);
        
        if ( v_Ret == null )
        {
            // 对常用接口，做适配性处理
            if ( MethodReflect.isExtendImplement(i_JsonDataType ,List.class) )
            {
                v_Ret = $ToJavaDefault_List;
            }
            else if ( MethodReflect.isExtendImplement(i_JsonDataType ,Set.class) )
            {
                v_Ret = $ToJavaDefault_Set;
            }
            else if ( MethodReflect.isExtendImplement(i_JsonDataType ,Map.class) )
            {
                v_Ret = $ToJavaDefault_Map;
            }
            else if ( MethodReflect.isExtendImplement(i_JsonDataType ,Enum.class) )
            {
                v_Ret = $ToJavaDefault_Enum;
            }
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 匹配并执行ToJava方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param <T>
     * @param i_XJson
     * @param i_JsonData       待转的对象
     * @param i_JsonDataType   转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass  Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Object executeToJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
    {
        Method v_ToJavaMethod = findToJava(i_JsonDataType);
        
        if ( v_ToJavaMethod == null )
        {
            return null;
        }
        
        if ( i_JsonDataType.isArray() && Help.isNull(i_JsonData) )
        {
            // 2022-05-05
            // 数组应当走另一个 executeToJava() 方法，但能走到此，表示 i_JsonData 是一个普通字符串（未启动高级转换），或是一个空字符串的情况
            return v_ToJavaMethod.invoke(null ,new Object[] {i_XJson ,null       ,i_JsonDataType ,i_JsonDataClass});
        }
        else
        {
            return v_ToJavaMethod.invoke(null ,new Object[] {i_XJson ,i_JsonData ,i_JsonDataType ,i_JsonDataClass});
        }
    }
    
    
    
    /**
     * 匹配并执行ToJava方法（专用于转换为数组）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param <T>
     * @param i_XJson
     * @param i_JsonDatas      待转的对象
     * @param i_JsonDataType   转换成的类型。数组的元素类型
     * @param i_JsonDataClass  Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object executeToJava(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method v_ToJavaMethod = findToJava(i_JsonDataType);
        
        if ( v_ToJavaMethod == null || v_ToJavaMethod.getParameterTypes()[1] != JSONArray.class )
        {
            return null;
        }
        
        return v_ToJavaMethod.invoke(null ,new Object[] {i_XJson ,i_JsonDatas ,i_JsonDataType.getComponentType() ,i_JsonDataClass});
    }
    
    
    
    /**
     * Json转String
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static String toJavaString(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return i_JsonData;
    }
    
    
    
    /**
     * Json转boolean
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static boolean toJavaboolean(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Boolean.parseBoolean(i_JsonData);
    }
    
    
    
    /**
     * Json转Boolean
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Boolean toJavaBoolean(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Boolean.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转Enum
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Enum<?> toJavaEnum(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            @SuppressWarnings("unchecked")
            Enum<?> [] v_EnumValues = StaticReflect.getEnums((Class<? extends Enum<?>>) i_JsonDataType);
            boolean    v_EnumOK     = false;
            Enum<?>    v_Ret        = null;
            
            // ZhengWei(HY) Add 2018-05-08  支持枚举toString()的匹配
            for (Enum<?> v_Enum : v_EnumValues)
            {
                if ( i_JsonData.equalsIgnoreCase(v_Enum.toString()) )
                {
                    v_Ret    = v_Enum;
                    v_EnumOK = true;
                    break;
                }
            }
            
            if ( !v_EnumOK )
            {
                // ZhengWei(HY) Add 2018-05-08  支持枚举名称的匹配
                for (Enum<?> v_Enum : v_EnumValues)
                {
                    if ( i_JsonData.equalsIgnoreCase(v_Enum.name()) )
                    {
                        v_Ret    = v_Enum;
                        v_EnumOK = true;
                        break;
                    }
                }
            }
            
            // 尝试用枚举值匹配
            if ( !v_EnumOK && Help.isNumber(i_JsonData) )
            {
                int v_ParamValueInt = Integer.parseInt(i_JsonData);
                if ( 0 <= v_ParamValueInt && v_ParamValueInt < v_EnumValues.length )
                {
                    v_Ret = v_EnumValues[v_ParamValueInt];
                }
            }
            
            return v_Ret;
        }
    }
    
    
    
    /**
     * Json转byte
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static byte toJavabyte(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Byte.parseByte(i_JsonData);
    }
    
    
    
    /**
     * Json转Byte
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Byte toJavaByte(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Byte.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转char
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static char toJavachar(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return i_JsonData.charAt(0);
    }
    
    
    
    /**
     * Json转Character
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Character toJavaCharacter(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return i_JsonData.charAt(0);
        }
    }
    
    
    
    /**
     * Json转short
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static short toJavashort(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Short.parseShort(i_JsonData);
    }
    
    
    
    /**
     * Json转Short
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Short toJavaShort(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Short.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转int
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static int toJavaint(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Integer.parseInt(i_JsonData);
    }
    
    
    
    /**
     * Json转Integer
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Integer toJavaInteger(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Integer.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转long
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static long toJavalong(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Long.parseLong(i_JsonData);
    }
    
    
    
    /**
     * Json转Long
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Long toJavaLong(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Long.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转double
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static double toJavadouble(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Double.parseDouble(i_JsonData);
    }
    
    
    
    /**
     * Json转Double
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Double toJavaDouble(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Double.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转float
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static float toJavafloat(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        return Float.parseFloat(i_JsonData);
    }
    
    
    
    /**
     * Json转Float
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Float toJavaFloat(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Float.valueOf(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转BigDecimal
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static BigDecimal toJavaBigDecimal(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return new BigDecimal(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转Date
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Date toJavaDate(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return new Date(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转java.util.Date
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static java.util.Date toJavajavautilDate(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return new Date(i_JsonData).getDateObject();
        }
    }
    
    
    
    /**
     * Json转java.sql.Date
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static java.sql.Date toJavajavasqlDate(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return new Date(i_JsonData).getSQLDate();
        }
    }
    
    
    
    /**
     * Json转Timestamp
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     */
    public static Timestamp toJavaTimestamp(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return new Date(i_JsonData).getSQLTimestamp();
        }
    }
    
    
    
    /**
     * Json转Class
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> toJavaClass(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws ClassNotFoundException
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return Help.forName(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转byte []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     * @throws ClassNotFoundException
     */
    public static byte [] toJavabyteArray(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws ClassNotFoundException
    {
        if ( Help.isNull(i_JsonData) )
        {
            return new byte[0];
        }
        else
        {
            return Base64Factory.getIntance().decode(i_JsonData);
        }
    }
    
    
    
    /**
     * Json转Byte []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return
     * @throws ClassNotFoundException
     */
    public static Byte [] toJavaByteArray(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws ClassNotFoundException
    {
        if ( Help.isNull(i_JsonData) )
        {
            return null;
        }
        else
        {
            return ByteHelp.byteToByte(Base64Factory.getIntance().decode(i_JsonData));
        }
    }
    
    
    
    /**
     * Json转String []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static String [] toJavaStringArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new String[0];
        }
        else
        {
            String []       v_RetArr   = new String[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaString(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转boolean []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static boolean [] toJavabooleanArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new boolean[0];
        }
        else
        {
            boolean []      v_RetArr   = new boolean[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaboolean(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Boolean []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Boolean [] toJavaBooleanArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Boolean[0];
        }
        else
        {
            Boolean []      v_RetArr   = new Boolean[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaBoolean(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转char []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static char [] toJavacharArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new char[0];
        }
        else
        {
            char []         v_RetArr   = new char[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavachar(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Character []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Character [] toJavaCharacterArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Character[0];
        }
        else
        {
            Character []    v_RetArr   = new Character[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaCharacter(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转short []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static short [] toJavashortArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new short[0];
        }
        else
        {
            short []        v_RetArr   = new short[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavashort(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Short []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Short [] toJavaShortArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Short[0];
        }
        else
        {
            Short []        v_RetArr   = new Short[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaShort(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转int []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static int [] toJavaintArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new int[0];
        }
        else
        {
            int []          v_RetArr   = new int[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaint(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转int []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Integer [] toJavaIntegerArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Integer[0];
        }
        else
        {
            Integer []      v_RetArr   = new Integer[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaInteger(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转long []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static long [] toJavalongArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new long[0];
        }
        else
        {
            long []         v_RetArr   = new long[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavalong(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Long []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Long [] toJavaLongArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Long[0];
        }
        else
        {
            Long []         v_RetArr   = new Long[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaLong(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转double []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static double [] toJavadoubleArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new double[0];
        }
        else
        {
            double []       v_RetArr   = new double[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavadouble(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Double []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Double [] toJavaDoubleArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Double[0];
        }
        else
        {
            Double []       v_RetArr   = new Double[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaDouble(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转float []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static float [] toJavafloatArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new float[0];
        }
        else
        {
            float []        v_RetArr   = new float[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavafloat(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Float []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static Float [] toJavaFloatArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Float[0];
        }
        else
        {
            Float []        v_RetArr   = new Float[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaFloat(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转BigDecimal []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     */
    public static BigDecimal [] toJavaBigDecimalArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass)
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new BigDecimal[0];
        }
        else
        {
            BigDecimal []   v_RetArr   = new BigDecimal[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaBigDecimal(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Class []
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-11
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。数组的元素类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     * @return                       转成数组时，不返回NULL，无值时，用 new T[0] 表示。
     * @throws ClassNotFoundException
     */
    public static Class<?> [] toJavaClassArray(XJSON i_XJson ,JSONArray i_JsonDatas ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws ClassNotFoundException
    {
        if ( Help.isNull(i_JsonDatas) )
        {
            return new Class[0];
        }
        else
        {
            Class<?> []        v_RetArr   = new Class[i_JsonDatas.size()];
            int             v_Index    = 0;
            ListIterator<?> v_Iterator = i_JsonDatas.listIterator();
            
            while ( v_Iterator.hasNext() )
            {
                Object v_ElementJson = v_Iterator.next();
                v_RetArr[v_Index++] = XJSONToJava.toJavaClass(i_XJson ,(String)v_ElementJson ,i_JsonDataType ,null);
            }
            
            return v_RetArr;
        }
    }
    
    
    
    /**
     * Json转Object
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-12-10
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_JsonData             待转的Json数据
     * @param i_JsonDataType         转换成的类型。一般为Java Setter方法的入参类型
     * @param i_JsonDataClass        Json字符串名称中定义的Java类型。如 xxx@java.lang.String
     *                               2021-09-30
     *                               在Java对象转Json字符串时，对于getter方法的返回类型为java.lang.Object时，
     *                               是否在Json字符串中包含getter方法的返回值的真实Java类型（ClassName）。
     *                               控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
     * @return
     * @throws Exception
     */
    public static Object toJavaObject(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws Exception
    {
        Object v_VValue = null;
        
        if ( !Help.isNull(i_JsonDataClass) )
        {
            Class<?> v_VClass = Help.forName(i_JsonDataClass.toString());
            
            Method v_ToJavaMethod = findToJava(v_VClass);
            
            // 防止递归
            if ( v_ToJavaMethod == null || v_ToJavaMethod.getReturnType() == Object.class )
            {
                return i_JsonData;
            }
            
            return v_ToJavaMethod.invoke(null ,new Object[] {i_XJson ,i_JsonData ,v_VClass ,i_JsonDataClass});
            
        }
        else
        {
            v_VValue = i_JsonData;
        }
        
        return v_VValue;
    }
    
    
    /**
     * Json转Map中的元素值
     * 
     * 注：内部不做NULL效验
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-06-22
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_Map
     * @param i_MapKey
     * @param i_MapValue
     */
    public static void mapValueToJava(XJSON i_XJson ,Map<Object ,Object> i_Map ,Object i_MapKey ,Object i_MapValue)
    {
        i_Map.put(i_MapKey ,i_MapValue);
    }
    
    
    
    /**
     * Json转Map中的元素值
     * 
     * 注：内部不做NULL效验
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-06-22
     * @version     v1.0
     * 
     * @param i_XJson
     * @param i_Map
     * @param i_MapKey
     * @param i_MapValue
     */
    @SuppressWarnings("unchecked")
    public static void mapValueToJava(XJSON i_XJson ,ExpireMap<String ,Object> i_Map ,String i_MapKey ,Object i_MapValue)
    {
        Map<String ,Object> v_MapValue = (Map<String ,Object>)i_MapValue;
        i_Map.putMilli(i_MapKey ,v_MapValue.get("value") ,(Date)v_MapValue.get("createTime") ,(Long)v_MapValue.get("time"));
    }
    
    
    
    private XJSONToJava()
    {
        // 私有的
    }
    
}
