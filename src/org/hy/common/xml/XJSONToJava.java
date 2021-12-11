package org.hy.common.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.ByteHelp;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.StaticReflect;
import org.hy.common.license.base64.Base64Factory;





/**
 * 最小粒度级的Json转Java功能。
 * 
 * 注：为了性能，此类所有方法均不考虑入参为NULL的情况
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2021-12-10
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
        List<Method> v_ToJsonMethods = MethodReflect.getMethods(XJSONToJava.class ,"toJava" ,5);
        
        if ( !Help.isNull(v_ToJsonMethods) )
        {
            for (Method v_Method : v_ToJsonMethods)
            {
                Class<?> v_JavaValueClass = v_Method.getParameters()[4].getType();
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
     */
    public static Object executeToJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method v_ToJavaMethod = findToJava(i_JsonDataType);
        
        if ( v_ToJavaMethod == null )
        {
            return null;
        }
        
        return v_ToJavaMethod.invoke(null ,new Object[] {i_XJson ,i_JsonData ,i_JsonDataType ,i_JsonDataClass ,null});
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static String toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,String i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static boolean toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,boolean i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Boolean toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Boolean i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Enum<?> toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Enum<?> i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static byte toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,byte i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Byte toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Byte i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static char toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,char i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Character toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Character i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static short toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,short i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Short toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Short i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static int toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,int i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Integer toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Integer i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static long toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,long i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Long toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Long i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static double toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,double i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Double toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Double i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static float toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,float i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Float toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Float i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static BigDecimal toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,BigDecimal i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Date toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Date i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static java.util.Date toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,java.util.Date i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static java.sql.Date toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,java.sql.Date i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     */
    public static Timestamp toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Timestamp i_JsonDataTypeForJava)
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Class<?> i_JsonDataTypeForJava) throws ClassNotFoundException
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     * @throws ClassNotFoundException
     */
    public static byte [] toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,byte [] i_JsonDataTypeForJava) throws ClassNotFoundException
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
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * @return
     * @throws ClassNotFoundException
     */
    public static Byte [] toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Byte [] i_JsonDataTypeForJava) throws ClassNotFoundException
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
     * 
     * @param i_JsonDataTypeForJava  实现执行时，此参数均传NULL，它存在的意义只为了方法重载（因为Java不支持回返值重载）
     * 
     * @return
     * @throws Exception
     */
    public static Object toJava(XJSON i_XJson ,String i_JsonData ,Class<?> i_JsonDataType ,String i_JsonDataClass ,Object i_JsonDataTypeForJava) throws Exception
    {
        Object v_VValue = null;
        
        if ( !Help.isNull(i_JsonDataClass) )
        {
            Class<?> v_VClass = Help.forName(i_JsonDataClass.toString());
            
            if ( Help.isBasicDataType(v_VClass) )
            {
                v_VValue = Help.toObject(v_VClass ,i_JsonData);
            }
            else
            {
                v_VValue = i_XJson.toJava(i_JsonData ,v_VClass);
            }
        }
        else
        {
            v_VValue = i_JsonData;
        }
        
        return v_VValue;
    }
    
    
    
    private XJSONToJava()
    {
        // 私有的
    }
    
}
