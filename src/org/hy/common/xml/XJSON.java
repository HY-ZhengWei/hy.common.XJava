package org.hy.common.xml;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.Return;
import org.hy.common.StaticReflect;
import org.hy.common.StringHelp;
import org.hy.common.xml.log.Logger;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;





/**
 * 与市面上其它转Json类的主要区别有以下几点：
 *   1. 有顺序的生成Json字符串，使后续的加密工作变的可行。
 *   2. 可控制Json字符串中key的首写字母是否大写。
 *   3. 可选择性的只对getter\setter方法成对的属性转Json或转Java。
 *   4. 对NULL值是否生成Json字符串，也有控制属性。
 *   5. 可四舍五入小数，可控制保留小数位数。
 * 
 * 
 * 主要功能1：解释Java对象成为JSON
 * 主要功能2：解释JSON字符串成为Java实例对象
 * 
 * 1. 支持将对象解释为JSON
 * 2. 支持将 对象.对象 解释为复杂的JSON
 * 3. 支持将Map集合解释为JSON，并且支持Map集合与对象的混合
 * 
 * 4  支持将JSONObject解释成Java实例对象
 * 5. 支持将JSONArray数组解释成Java实例对象
 * 6. 支持将混合的JSONObject、JSONArray解释成Java实例对象
 * 7. 支持将解释JSON字符串成为Java实例对象
 * 8. 支持将解释JSON字符串成为纯Map集合对象
 * 
 * 9. 支持有排列顺序的生成JSON字符串
 * 10.支持按JSon字符串的顺序，生成对应顺序的集合对象(默认是没有顺序的。由 this.parserObjectClass 控制)
 * 
 * 11.支持生成的JSON字符串的key的首写字母是否大写 (见 this.firstCharIsUpper 属性)
 * 12.支持是否精确解释 (见 this.isAccuracy 属性)
 * 13.支持Java对象方法返回值为null时，是否生成JSON字符串 (见 this.isReturnNVL 属性)
 * 
 * 14.支持格式化Json字符串
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0
 * @createDate  2013-08-13
 *              2017-08-18  V2.0  修复：对于对象的getClass()方法，不进行转Json处理。
 *              2017-08-30  V3.0  修复：对于Json字符串去除回车符\n，特别是最后一个回车符会影响net.minidev.json.JSONObject的解释出错。
 *                                     发现人：李浩
 *              2018-05-08  V3.1  添加：支持枚举toString()的匹配
 *                                     支持枚举名称的匹配
 *              2018-05-15  V3.2  添加：数据库java.sql.Timestamp时间的转换
 *              2018-05-18  V3.3  添加：将Java转Json时，防止用户递归引用，而造成无法解释的问题。
 *              2018-07-05  V3.4  添加：将Java转Json时，支持为BigDecimal类型的转换。发现人：马龙
 *              2018-07-06  V3.5  添加：1.通过isBigDecimalFormat属性控制BigDecimal类型生成Json字符串时的格式，是否为科学计数法。
 *                                     2.通过digit属性控制Java转Json时，数值转字符串时保留小数位数。建议人：马龙
 *                                     3.将parser分为toJava、toJson两个系列的方法，方便使用者好理解。
 *              2018-07-10  V3.6  修复：1. Json转Java（Map对象）时，Json字符串中的 "key":null 情况的异常处理。发现人：马龙。
 *                                        建议：当需要Map.value为NULL时，请在转换前设置此句this.setObjectClass(HashMap.class);
 *                                             否则，NULL的Json字符串将转成""空字符串写入Hashtable中。
 *                                     2. 防止递归功能，添加允许递归次数。允许一定范围内的递归或重复数据。发现人：马龙。
 *              2020-01-15  V3.7  添加：当Java对象转Json字符串时，是否包含对成员方法的转换输出。
 *              2021-01-15  V3.8  添加：简单判定字符串是否为Json格式的字符串
 *              2021-09-30  V4.0  添加：控制参数isSerializable。实现：Json序列化和反序列化
 *                                     在Java对象转Json字符串时，对于getter方法的返回类型为java.lang.Object时，
 *                                     是否在Json字符串中包含getter方法的返回值的真实Java类型（ClassName）。
 *                                     控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
 *              2021-12-09  V4.1  添加：Json字符串转Java对象时，当Setter方法的入传为数组时支持
 * 
 */
public final class XJSON
{
    private static final Logger $Logger = new Logger(XJSON.class);
    
    /** Json字符串的名称中是否包含"值"的Java类名称，形式是：{ "name@java.lang.Integer" : "value" } */
    private static String    $Serializable_SplitKey = "@";
    
    private XJSONObject rootJSON;
    
    /**
     * 首写字母是否大写（默认：首写字母小写）
     * 
     * 当使用 object.getter() 的方法名称作为JSONObject.put(name ,value)的 name 值时，首写字母是否大小
     * 
     * 只用于Java对象实例解释成JSONObject对象
     */
    private boolean     firstCharIsUpper;
    
    /**
     * 是否精确解释（默认：false）
     * 
     * 当精确解释时=true，   JSON字符串中的Key必须找到Java的setter方法，否则报错。
     * 当精确解释时=true，   Java对象转为JSON字符串时，必须getter方法与setter方法成对出现时，才会生成JSON字符串。
     * 当非精确解释时=false，JSON字符串中的Key找到不Java的setter方法时，忽略JSON字符串Key对应的值。
     * 当非精确解释时=false，Java对象转为JSON字符串时，不要求getter方法与setter方法必须成对出现。
     * 
     * 只用于解释JSON字符串成为Java实例对象
     */
    private boolean     isAccuracy;
    
    /**
     * 将对象转为Json字符串时，对象的方法返回 null 或 ""空字符串时，
     * 是否还将其生成在Json字符串中。
     * 
     * (默认：true  即空值也生成Json字符串)
     */
    private boolean     isReturnNVL;
    
    /**
     * 在Java对象转Json字符串时，是否包含对成员方法的转换输出
     * 默认为：false，不转换输出
     */
    private boolean     isJsonMethod;
    
    /**
     * 实现：Json序列化和反序列化
     * 
     * 在Java对象转Json字符串时，对于getter方法的返回类型为java.lang.Object时，
     * 是否在Json字符串中包含getter方法的返回值的真实Java类型（ClassName）。
     * 
     * 控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
     * 
     * 默认为：false，不转换输出
     */
    private boolean     isSerializable;
    
    /** Java转Json时，BigDecimal的显示格式。是否启用科学计数法（默认为：自然数字，而非科学计数法） */
    private boolean     isBigDecimalFormat;
    
    /** Java转Json时，数值转字符串时保留小数位数（默认为：NULL表示原样转Json字符串） */
    private Integer     digit;
    
    /** 允许递归出现在次数。Java转Json时，防止对象中递归引用对象，而造成无法解释的问题（默认值：0，表示不防止递归 ）  */
    private int         recursionCount;
    
    /**
     * 解释成为Java实例对象的元类型（默认为：java.util.Hashtable.class）
     * 
     * 只用于解释JSON字符串成为Java实例对象
     */
    private Class<?>    parserObjectClass;
    
    
    
    public XJSON()
    {
        this.firstCharIsUpper    = false;
        this.isAccuracy          = false;
        this.isReturnNVL         = true;
        this.isJsonMethod        = false;
        this.isSerializable      = false;
        this.isBigDecimalFormat  = false;
        this.digit               = null;
        this.recursionCount      = 0;
        this.parserObjectClass   = Hashtable.class;
    }
    
    
    
    public XJSON(Object i_Obj) throws Exception
    {
        this();
        
        this.parser(i_Obj);
    }
    
    
    
    /**
     * Json字符串数组字符串解释成Java的List<T>集合实例对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-11-02
     * @version     v1.0
     * 
     * @param i_JsonArray    Json字符串数组。即左右两端是[]方括号。
     * @param i_ObjectClass  Json字符串数组的元素对应的Java类型
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> toJavaList(String i_JsonArray ,Class<? extends T> i_ObjectClass) throws Exception
    {
        return (List<T>)toJava("{\"XJSONDatas\":" + i_JsonArray + "}" ,"XJSONDatas" ,i_ObjectClass);
    }
    
    
    
    /**
     * 将JSONObject解释成纯Map的Java实例对象
     * 或
     * 将JSONObject解释成预设的Java实例对象
     * 
     * @param i_JSONObject
     * @return
     */
    public Object toJava(JSONObject i_JSONObject)
    {
        return parser(i_JSONObject ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSONObject解释成纯Map的Java实例对象
     * 或
     * 将JSONObject解释成预设的Java实例对象
     * 
     * @param i_JSONObject
     * @param i_ObjectClass  JSONObject对应的Java类型
     * @return
     */
    public Object toJava(JSONObject i_JSONObject ,Class<?> i_ObjectClass)
    {
        return parser(i_JSONObject ,i_ObjectClass);
    }
    
    
    
    /**
     * 将JSONObject解释成纯Map的Java实例对象
     * 或
     * 将JSONObject解释成预设的Java实例对象
     * 
     * @param i_JSONObject
     * @return
     */
    public Object toJava(XJSONObject i_JSONObject)
    {
        return parser(i_JSONObject ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSONObject解释成Java实例对象
     * 
     * @param i_JSONObject
     * @param i_ObjectClass  JSONObject对应的Java类型
     * @return
     */
    public Object toJava(XJSONObject i_JSONObject ,Class<?> i_ObjectClass)
    {
        return parser(i_JSONObject ,i_ObjectClass);
    }
    
    
    
    /**
     * 将JSONArray解释成纯Map的Java实例对象
     * 或
     * 将JSONArray解释成预设的Java实例对象
     * 
     * @param i_JSONArray   JSONArray数组
     * @return
     */
    public List<Object> toJava(JSONArray i_JSONArray)
    {
        return parser(i_JSONArray ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSONArray数组解释成Java实例对象
     * 
     * @param i_JSONArray     JSONArray数组
     * @param i_ElementClass  JSONArray数组中元素的Java类型
     * @return
     */
    public List<Object> toJava(JSONArray i_JSONArray ,Class<?> i_ElementClass)
    {
        return parser(i_JSONArray ,i_ElementClass);
    }
    
    
    
    /**
     * 将JSON字符串解释成纯Map的Java实例对象
     * 或
     * 将JSON字符串解释成预设的Java实例对象
     * 
     * @param i_JSONString   完整的Json字符串
     * @return
     * @throws Exception
     */
    public Object toJava(String i_JSONString) throws Exception
    {
        return parser(i_JSONString ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSON字符串解释成Java实例对象
     * 
     * @param i_JSONString   完整的Json字符串
     * @param i_ObjectClass  JSONObject对应的Java类型
     * @return
     * @throws Exception
     */
    public Object toJava(String i_JSONString ,Class<?> i_ObjectClass) throws Exception
    {
        return parser(i_JSONString ,i_ObjectClass);
    }
    
    
    
    /**
     * 将JSON字符串中的某个JSONKey解释成纯Map的Java实例对象
     * 或
     * 将JSON字符串中的某个JSONKey解释成预设的Java实例对象
     * 
     * @param i_JSONString  完整的Json字符串
     * @param i_JSONKey     只解析大Json字符串的某个子字符串
     * @return
     * @throws Exception
     */
    public Object toJava(String i_JSONString ,String i_JSONKey) throws Exception
    {
        return parser(i_JSONString ,i_JSONKey ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSON字符串中的某个JSONKey解释成Java实例对象
     * 
     * @param i_JSONString   完整的Json字符串
     * @param i_JSONKey      只解析大Json字符串的某个子字符串
     * @param i_ObjectClass  JSONObject对应的Java类型
     * @return
     * @throws Exception
     */
    public Object toJava(String i_JSONString ,String i_JSONKey ,Class<?> i_ObjectClass) throws Exception
    {
        return parser(i_JSONString ,i_JSONKey ,i_ObjectClass);
    }
    
    
    
    /**
     * 将JSONObject解释成纯Map的Java实例对象
     * 或
     * 将JSONObject解释成预设的Java实例对象
     * 
     * @param i_JSONObject
     * @return
     */
    public Object parser(JSONObject i_JSONObject)
    {
        return parser(new XJSONObject(i_JSONObject) ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSONObject解释成纯Map的Java实例对象
     * 或
     * 将JSONObject解释成预设的Java实例对象
     * 
     * @param i_JSONObject
     * @param i_ObjectClass
     * @return
     */
    public Object parser(JSONObject i_JSONObject ,Class<?> i_ObjectClass)
    {
        return parser(new XJSONObject(i_JSONObject) ,i_ObjectClass);
    }
    
    
    
    /**
     * 将JSONObject解释成纯Map的Java实例对象
     * 或
     * 将JSONObject解释成预设的Java实例对象
     * 
     * @param i_JSONObject
     * @return
     */
    public Object parser(XJSONObject i_JSONObject)
    {
        return parser(i_JSONObject ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSONArray解释成纯Map的Java实例对象
     * 或
     * 将JSONArray解释成预设的Java实例对象
     * 
     * @param i_JSONArray
     * @return
     */
    public List<Object> parser(JSONArray i_JSONArray)
    {
        return parser(i_JSONArray ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSON字符串解释成纯Map的Java实例对象
     * 或
     * 将JSON字符串解释成预设的Java实例对象
     * 
     * @param i_JSONString
     * @return
     * @throws Exception
     */
    public Object parser(String i_JSONString) throws Exception
    {
        return parser(i_JSONString ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSON字符串中的某个JSONKey解释成纯Map的Java实例对象
     * 或
     * 将JSON字符串中的某个JSONKey解释成预设的Java实例对象
     * 
     * @param i_JSONString
     * @param i_JSONKey
     * @return
     * @throws Exception
     */
    public Object parser(String i_JSONString ,String i_JSONKey) throws Exception
    {
        return parser(i_JSONString ,i_JSONKey ,this.getObjectClass());
    }
    
    
    
    /**
     * 将JSONObject解释成Java实例对象
     * 
     * @param i_JSONObject
     * @param i_ObjectClass         JSONObject对应的Java类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object parser(XJSONObject i_JSONObject ,Class<?> i_ObjectClass)
    {
        if ( i_JSONObject == null )
        {
            throw new NullPointerException("JSONObject is null.");
        }
        
        if ( i_ObjectClass == null )
        {
            throw new NullPointerException("JSON Parser Object Class is null.");
        }
        
        Object v_NewObj = null;
        try
        {
            if ( Map.class.equals(i_ObjectClass) )
            {
                v_NewObj = new HashMap<Object ,Object>();
            }
            else if ( List.class.equals(i_ObjectClass) )
            {
                v_NewObj = new ArrayList<Object>();
            }
            else if ( i_ObjectClass.isArray() )
            {
                v_NewObj = new ArrayList<Object>();
            }
            else if ( Set.class.equals(i_ObjectClass) )
            {
                v_NewObj = new HashSet<Object>();
            }
            else
            {
                v_NewObj = i_ObjectClass.newInstance();
            }
        }
        catch (Exception exce)
        {
            throw new NullPointerException(i_ObjectClass.getName() + " newInstance() is error.");
        }
        
        Iterator<?> v_Iter  = i_JSONObject.keySet().iterator();
        if ( MethodReflect.isExtendImplement(i_ObjectClass ,Map.class) )
        {
            // 解释JSON字符串成为纯Map集合对象
            while ( v_Iter.hasNext() )
            {
                String    v_JsonName      = v_Iter.next().toString();
                Object    v_Value         = i_JSONObject.get(v_JsonName);
                String [] v_NameAndClass  = v_JsonName.split($Serializable_SplitKey);
                String    v_ValueClass    = v_NameAndClass.length >= 2 ? v_NameAndClass[1] : null;
                String    v_MapKeyName    = v_NameAndClass[0];
                Class<?>  v_MapValueClass = this.getObjectClass();
                
                if ( !Help.isNull(v_ValueClass) )
                {
                    try
                    {
                        v_MapValueClass = Help.forName(v_ValueClass);
                    }
                    catch (Exception exce)
                    {
                        throw new RuntimeException(exce);
                    }
                }
                
                // 2018-07-10 Add 为NULL时，Map.value写入NULL对象
                if ( v_Value != null )
                {
                    if ( v_Value.getClass() == JSONArray.class )
                    {
                        v_Value = parser((JSONArray)v_Value ,v_MapValueClass);
                    }
                    else if ( v_Value.getClass() == JSONObject.class )
                    {
                        v_Value = parser(new XJSONObject((JSONObject)v_Value) ,v_MapValueClass);
                    }
                    else if ( v_Value.getClass() == XJSONObject.class )
                    {
                        v_Value = parser((XJSONObject)v_Value ,v_MapValueClass);
                    }
                    else
                    {
                        v_Value = Help.toObject(v_MapValueClass ,v_Value.toString());
                    }
                    
                    ((Map<String ,Object>)v_NewObj).put(v_MapKeyName ,v_Value);
                }
                else
                {
                    if ( MethodReflect.isExtendImplement(i_ObjectClass ,Hashtable.class) )
                    {
                        // Nothing Hashtable.value 不能为NULL
                        ((Map<String ,Object>)v_NewObj).put(v_MapKeyName ,"");
                    }
                    else
                    {
                        ((Map<String ,Object>)v_NewObj).put(v_MapKeyName ,v_Value);
                    }
                }
            }
        }
        else
        {
            // 解释JSON字符串成为Java实例对象
            while ( v_Iter.hasNext() )
            {
                String    v_JsonName     = v_Iter.next().toString();
                Object    v_Value        = i_JSONObject.get(v_JsonName);
                String [] v_NameAndClass = v_JsonName.split($Serializable_SplitKey);
                String    v_MethodName   = v_NameAndClass[0];
                String    v_ValueClass   = v_NameAndClass.length >= 2 ? v_NameAndClass[1] : null;
                String    v_ElementClass = v_NameAndClass.length >= 3 ? v_NameAndClass[2] : null;
                Method    v_Method       = null;
                Object    v_ParserObj    = null;
                
                if ( i_ObjectClass.isArray() )
                {
                    try
                    {
                        v_Method = ArrayList.class.getMethod("add" ,new Class<?>[] {Object.class});
                    }
                    catch (Exception exce)
                    {
                        $Logger.error(exce);
                    }
                }
                else
                {
                    v_Method = MethodReflect.getSetMethod(i_ObjectClass ,v_MethodName ,true);
                }
                
                if ( v_Value == null )
                {
                    // Nothing.
                }
                else if ( v_Method == null )
                {
                    if ( this.isAccuracy )
                    {
                        throw new NullPointerException(i_ObjectClass.getName() + " setter method["+ v_MethodName + "] is not find.");
                    }
                }
                else if ( v_Value.getClass() == JSONArray.class )
                {
                    Class<?> v_ParamClass = v_Method.getParameterTypes()[0];
                    Class<?> v_PEClass    = Object.class;
                    
                    // 2021-09-30  将Object对象转为真实的Java对象
                    if ( !Help.isNull(v_ValueClass) && !Help.isNull(v_ElementClass) )
                    {
                        try
                        {
                            v_ParamClass = Help.forName(v_ValueClass);
                            v_PEClass    = Help.forName(v_ElementClass);
                        }
                        catch (Exception exce)
                        {
                            // 当客户端无对应的Java类反序列化时，采取温柔方式：不抛异常，断续普通方式解析Json 2021-10-08
                            $Logger.warn(exce.getMessage());
                        }
                    }
                    
                    // Setter方法的入参是：List集合
                    if ( MethodReflect.isExtendImplement(v_ParamClass ,List.class) )
                    {
                        Class<?> v_ActualTypeClass = null;
                        
                        try
                        {
                            v_ActualTypeClass = MethodReflect.getGenerics(v_Method);
                            if ( v_ActualTypeClass == null )
                            {
                                v_ActualTypeClass = v_PEClass;
                            }
                        }
                        catch (Exception exce)
                        {
                            v_ActualTypeClass = v_PEClass;
                        }
                        
                        v_ParserObj = parser((JSONArray)v_Value ,v_ActualTypeClass);
                        
                        try
                        {
                            v_Method.invoke(v_NewObj ,v_ParserObj);
                        }
                        catch (Exception exce)
                        {
                            throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + "(List<" + v_ActualTypeClass.getName() + ">) is error." + exce.getMessage());
                        }
                    }
                    // Setter方法的入参是：Map集合
                    else if ( MethodReflect.isExtendImplement(v_ParamClass ,Map.class) )
                    {
                        if ( v_PEClass == Object.class )
                        {
                            v_ParserObj = parser((JSONArray)v_Value ,this.getObjectClass());
                        }
                        else
                        {
                            v_ParserObj = parser((JSONArray)v_Value ,v_PEClass);
                        }
                        
                        try
                        {
                            v_Method.invoke(v_NewObj ,v_ParserObj);
                        }
                        catch (Exception exce)
                        {
                            throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + "(Map) is error." + exce.getMessage());
                        }
                    }
                    // Setter方法的入参是：Set集合
                    else if ( MethodReflect.isExtendImplement(v_ParamClass ,Set.class) )
                    {
                        Class<?> v_ActualTypeClass = null;
                        
                        try
                        {
                            v_ActualTypeClass = MethodReflect.getGenerics(v_Method);
                            if ( v_ActualTypeClass == null )
                            {
                                v_ActualTypeClass = v_PEClass;
                            }
                        }
                        catch (Exception exce)
                        {
                            v_ActualTypeClass = v_PEClass;
                        }
                        
                        v_ParserObj = parser((JSONArray)v_Value ,v_ActualTypeClass);
                        Set<Object>  v_ParserSet  = new HashSet<Object>();
                        List<Object> v_ParserList = (List<Object>) v_ParserObj;
                        
                        for (Object v_Obj : v_ParserList)
                        {
                            v_ParserSet.add(v_Obj);
                        }
                        
                        try
                        {
                            v_Method.invoke(v_NewObj ,v_ParserSet);
                        }
                        catch (Exception exce)
                        {
                            throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + "(List<" + v_ActualTypeClass.getName() + ">) is error." + exce.getMessage());
                        }
                    }
                    // Setter方法的入参是：数组类型
                    else if ( v_ParamClass.isArray() )
                    {
                        Class<?> v_ActualTypeClass = null;
                        
                        try
                        {
                            v_ActualTypeClass = MethodReflect.getGenerics(v_Method);
                            if ( v_ActualTypeClass == null )
                            {
                                v_ActualTypeClass = v_PEClass;
                            }
                        }
                        catch (Exception exce)
                        {
                            v_ActualTypeClass = v_PEClass;
                        }
                        
                        v_ParserObj = parser((JSONArray)v_Value ,v_ActualTypeClass);
                        
                        try
                        {
                            v_Method.invoke(v_NewObj ,new Object[] {((List<?>)v_ParserObj).toArray()});
                        }
                        catch (Exception exce)
                        {
                            throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + "(List<" + v_ActualTypeClass.getName() + ">) is error." + exce.getMessage());
                        }
                    }
                    // Setter方法的入参是：Object类型
                    else if ( v_ParamClass == Object.class )
                    {
                        try
                        {
                            v_Method.invoke(v_NewObj ,v_Value);
                        }
                        catch (Exception exce)
                        {
                            throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + "(Object) is error." + exce.getMessage());
                        }
                    }
                    else
                    {
                        throw new NullPointerException(i_ObjectClass.getName() + " setter method["+ v_MethodName + "(List)] is not find.");
                    }
                }
                else if ( v_Value.getClass() == JSONObject.class )
                {
                    Class<?> v_VClass = v_Method.getParameterTypes()[0];
                    
                    // 控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
                    if ( !Help.isNull(v_ValueClass) )
                    {
                        try
                        {
                            // 2021-09-30  将Object对象转为真实的Java对象
                            v_VClass = Help.forName(v_ValueClass);
                        }
                        catch (Exception exce)
                        {
                            // 当客户端无对应的Java类反序列化时，采取温柔方式：不抛异常，断续普通方式解析Json 2021-10-08
                            $Logger.warn(exce.getMessage());
                        }
                    }
                    
                    v_ParserObj = this.parser((JSONObject)v_Value ,v_VClass);
                    
                    if ( v_ParserObj == null )
                    {
                        throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + " parameter is null.");
                    }
                    
                    try
                    {
                        if ( v_VClass.isArray() )
                        {
                            v_Method.invoke(v_NewObj ,new Object[] {((List<?>)v_ParserObj).toArray()});
                        }
                        else
                        {
                            v_Method.invoke(v_NewObj ,v_ParserObj);
                        }
                    }
                    catch (Exception exce)
                    {
                        throw new RuntimeException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + " is error." + exce.getMessage());
                    }
                }
                else if ( v_Value.getClass() == XJSONObject.class )
                {
                    v_ParserObj = parser((XJSONObject)v_Value ,v_Method.getParameterTypes()[0]);
                    
                    if ( v_ParserObj == null )
                    {
                        throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + " parameter is null.");
                    }
                    
                    try
                    {
                        v_Method.invoke(v_NewObj ,v_ParserObj);
                    }
                    catch (Exception exce)
                    {
                        throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + " is error." + exce.getMessage());
                    }
                }
                else
                {
                    Class<?> v_ParamClass = v_Method.getParameterTypes()[0];
                    
                    try
                    {
                        if ( String.class == v_ParamClass )
                        {
                            v_Method.invoke(v_NewObj ,v_Value.toString());
                        }
                        else if ( int.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Integer.parseInt(v_Value.toString()));
                            }
                        }
                        else if ( Integer.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Integer.valueOf(v_Value.toString()));
                            }
                            else
                            {
                                Integer v_Null = null;
                                v_Method.invoke(v_NewObj ,v_Null);
                            }
                        }
                        else if ( long.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Long.parseLong(v_Value.toString()));
                            }
                        }
                        else if ( Long.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Long.valueOf(v_Value.toString()));
                            }
                            else
                            {
                                Integer v_Null = null;
                                v_Method.invoke(v_NewObj ,v_Null);
                            }
                        }
                        else if ( double.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Double.parseDouble(v_Value.toString()));
                            }
                        }
                        else if ( Double.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Double.valueOf(v_Value.toString()));
                            }
                            else
                            {
                                Double v_Null = null;
                                v_Method.invoke(v_NewObj ,v_Null);
                            }
                        }
                        else if ( BigDecimal.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,new BigDecimal(v_Value.toString()));
                            }
                            else
                            {
                                BigDecimal v_Null = null;
                                v_Method.invoke(v_NewObj ,v_Null);
                            }
                        }
                        else if ( float.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Float.parseFloat(v_Value.toString()));
                            }
                        }
                        else if ( Float.class == v_ParamClass )
                        {
                            if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,Float.valueOf(v_Value.toString()));
                            }
                            else
                            {
                                Float v_Null = null;
                                v_Method.invoke(v_NewObj ,v_Null);
                            }
                        }
                        else if ( boolean.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,Boolean.parseBoolean(v_Value.toString()));
                            }
                        }
                        else if ( Boolean.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,Boolean.valueOf(v_Value.toString()));
                            }
                        }
                        else if ( short.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,Short.parseShort(v_Value.toString()));
                            }
                        }
                        else if ( Short.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,Short.valueOf(v_Value.toString()));
                            }
                        }
                        else if ( char.class == v_ParamClass || Character.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,v_Value.toString().charAt(0));
                            }
                        }
                        else if ( byte.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,Byte.parseByte(v_Value.toString()));
                            }
                        }
                        else if ( Byte.class == v_ParamClass )
                        {
                            if ( v_Value != null )
                            {
                                v_Method.invoke(v_NewObj ,Byte.valueOf(v_Value.toString()));
                            }
                        }
                        else if ( Date.class == v_ParamClass )
                        {
                            if ( v_Value == null || Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,(Date)null);
                            }
                            else
                            {
                                v_Method.invoke(v_NewObj ,new Date(v_Value.toString()));
                            }
                        }
                        else if ( java.util.Date.class == v_ParamClass )
                        {
                            if ( v_Value == null || Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,(java.util.Date)null);
                            }
                            else
                            {
                                v_Method.invoke(v_NewObj ,(new Date(v_Value.toString())).getDateObject());
                            }
                        }
                        // 添加对数据库时间的转换 Add ZhengWei(HY) 2018-05-15
                        else if ( Timestamp.class == v_ParamClass )
                        {
                            if ( v_Value == null || Help.isNull(v_Value.toString()) )
                            {
                                v_Method.invoke(v_NewObj ,(Timestamp)null);
                            }
                            else
                            {
                                v_Method.invoke(v_NewObj ,(new Date(v_Value.toString())).getSQLTimestamp());
                            }
                        }
                        else if ( MethodReflect.isExtendImplement(v_ParamClass ,List.class)
                               || MethodReflect.isExtendImplement(v_ParamClass ,Set.class)
                               || MethodReflect.isExtendImplement(v_ParamClass ,Map.class) )
                        {
                            /**
                             * ZhengWei(HY) Add 2015-08-24
                             * 代码能执行到此，有可能是第三方JSON字符串的格式不正确造成的
                             * 比如错误的语法是：xx : "[]"  ,想表示对象的属性类型是个集合，但这样表示是错误的
                             * 对应正确的语法是：xx : []
                             */
                        }
                        else if ( Class.class == v_ParamClass )
                        {
                            if ( !Help.isNull(v_Value) )
                            {
                                v_Method.invoke(v_NewObj ,Help.forName(v_Value.toString()));
                            }
                        }
                        else if ( MethodReflect.isExtendImplement(v_ParamClass ,Enum.class) )
                        {
                            Enum<?> [] v_EnumValues = StaticReflect.getEnums((Class<? extends Enum<?>>) v_ParamClass);
                            boolean    v_EnumOK     = false;
                            String     v_ValueStr   = v_Value.toString();
                            
                            // ZhengWei(HY) Add 2018-05-08  支持枚举toString()的匹配
                            for (Enum<?> v_Enum : v_EnumValues)
                            {
                                if ( v_ValueStr.equalsIgnoreCase(v_Enum.toString()) )
                                {
                                    v_Method.invoke(v_NewObj ,v_Enum);
                                    v_EnumOK = true;
                                    break;
                                }
                            }
                            
                            if ( !v_EnumOK )
                            {
                                // ZhengWei(HY) Add 2018-05-08  支持枚举名称的匹配
                                for (Enum<?> v_Enum : v_EnumValues)
                                {
                                    if ( v_ValueStr.equalsIgnoreCase(v_Enum.name()) )
                                    {
                                        v_Method.invoke(v_NewObj ,v_Enum);
                                        v_EnumOK = true;
                                        break;
                                    }
                                }
                            }
                            
                            // 尝试用枚举值匹配
                            if ( !v_EnumOK && Help.isNumber(v_ValueStr) )
                            {
                                int v_ParamValueInt = Integer.parseInt(v_ValueStr.trim());
                                if ( 0 <= v_ParamValueInt && v_ParamValueInt < v_EnumValues.length )
                                {
                                    v_Method.invoke(v_NewObj ,v_EnumValues[v_ParamValueInt]);
                                }
                            }
                        }
                        else if ( v_ParamClass.isArray() )
                        {
                            // TODO 2015-08-24 待后期实现：对于数组，暂时还没有想好用什么好方法实现。
                        }
                        else if ( v_Value == null || Help.isNull(v_Value.toString()) )
                        {
                            // Nothing.
                        }
                        else if ( Object.class == v_ParamClass )
                        {
                            // 2021-09-30
                            // 在Java对象转Json字符串时，对于getter方法的返回类型为java.lang.Object时，
                            // 是否在Json字符串中包含getter方法的返回值的真实Java类型（ClassName）。
                            // 控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
                            if ( !Help.isNull(v_ValueClass) )
                            {
                                Class<?> v_VClass = Help.forName(v_ValueClass);
                                Object   v_VValue = null;
                                
                                if ( Help.isBasicDataType(v_VClass) )
                                {
                                    v_VValue = Help.toObject(v_VClass ,v_Value.toString());
                                }
                                else
                                {
                                    v_VValue = this.toJava(v_Value.toString() ,v_VClass);
                                }
                                
                                v_Method.invoke(v_NewObj ,v_VValue);
                            }
                            else
                            {
                                v_Method.invoke(v_NewObj ,v_Value.toString());
                            }
                        }
                        else
                        {
                            throw new NullPointerException("Unknown Java Class type.");
                        }
                    }
                    catch (Exception exce)
                    {
                        throw new NullPointerException("Call " + i_ObjectClass.getName() + "." + v_Method.getName() + " is error." + exce.getMessage());
                    }
                }
            }
        }
        
        return v_NewObj;
    }
    
    
    
    /**
     * 将JSONArray数组解释成Java实例对象
     * 
     * @param i_JSONArray
     * @param i_ElementClass  JSONArray数组中元素的Java类型
     * @return
     */
    public List<Object> parser(JSONArray i_JSONArray ,Class<?> i_ElementClass)
    {
        if ( i_JSONArray == null )
        {
            throw new NullPointerException("JSONArray is null.");
        }
        
        if ( i_ElementClass == null )
        {
            throw new NullPointerException("JSON Parser Object Class is null.");
        }
        
        
        List<Object>    v_RetList      = new ArrayList<Object>();
        ListIterator<?> v_ListIterator = i_JSONArray.listIterator();
        
        while ( v_ListIterator.hasNext() )
        {
            Object v_ElementObject = v_ListIterator.next();
            Object v_ParserObj     = null;
            
            if ( v_ElementObject.getClass() == JSONArray.class )
            {
                v_ParserObj = parser((JSONArray)v_ElementObject ,i_ElementClass);
            }
            else if ( v_ElementObject.getClass() == JSONObject.class )
            {
                v_ParserObj = parser(new XJSONObject((JSONObject)v_ElementObject) ,i_ElementClass);
            }
            else if ( v_ElementObject.getClass() == XJSONObject.class )
            {
                v_ParserObj = parser((XJSONObject)v_ElementObject ,i_ElementClass);
            }
            else
            {
                v_ParserObj = v_ElementObject.toString();
            }
            
            if ( v_ParserObj != null )
            {
                v_RetList.add(v_ParserObj);
            }
        }
        
        return v_RetList;
    }
    
    
    
    /**
     * 将JSON字符串解释成Java实例对象
     * 
     * @param i_JSONString
     * @param i_Class
     * @return
     * @throws Exception
     */
    public Object parser(String i_JSONString ,Class<?> i_Class) throws Exception
    {
        if ( Help.isNull(i_JSONString) )
        {
            throw new NullPointerException("JSON String is null.");
        }
        
        if ( i_Class == null )
        {
            throw new NullPointerException("JSON Parser Object Class is null.");
        }
        
        
        JSONParser v_JsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        XJSONObject v_JSONRoot   = null;
        
        try
        {
            v_JSONRoot = new XJSONObject((JSONObject) v_JsonParser.parse(StringHelp.replaceAll(i_JSONString ,new String[]{"\n"} ,new String[]{""})));
        }
        catch (Exception exce)
        {
            throw new Exception(exce.getMessage());
        }
        
        return parser(v_JSONRoot ,i_Class);
    }
    
    
    
    /**
     * 将JSON字符串中的某个JSONKey解释成Java实例对象
     * 
     * @param i_JSONString
     * @param i_JSONKey
     * @param i_Class
     * @return
     * @throws Exception
     */
    public Object parser(String i_JSONString ,String i_JSONKey ,Class<?> i_Class) throws Exception
    {
        if ( Help.isNull(i_JSONString) )
        {
            throw new NullPointerException("JSON String is null.");
        }
        
        if ( Help.isNull(i_JSONKey) )
        {
            throw new NullPointerException("JSON Key is null.");
        }
        
        if ( i_Class == null )
        {
            throw new NullPointerException("JSON Parser Object Class is null.");
        }
        
        JSONParser  v_JsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        XJSONObject v_JSONRoot   = null;
        Object      v_Object     = null;
        
        try
        {
            v_JSONRoot = new XJSONObject((JSONObject) v_JsonParser.parse(StringHelp.replaceAll(i_JSONString ,new String[]{"\n"} ,new String[]{""})));
            v_Object   = v_JSONRoot.get(i_JSONKey);
        }
        catch (Exception exce)
        {
            throw new Exception(exce.getMessage());
        }
        
        
        if ( v_Object == null )
        {
            throw new NullPointerException("JSON Parser Object is null.");
        }
        else if ( v_Object.getClass() == JSONArray.class )
        {
            return parser((JSONArray)v_Object ,i_Class);
        }
        else if ( v_Object.getClass() == JSONObject.class )
        {
            return parser(new XJSONObject((JSONObject)v_Object) ,i_Class);
        }
        else if ( v_Object.getClass() == XJSONObject.class )
        {
            return parser((XJSONObject)v_Object ,i_Class);
        }
        else
        {
            throw new NullPointerException("Unknown JSON type.");
        }
    }
    
    
    
    /**
     * 解释Java对象成为JSON
     * 
     * @param i_JavaData      Java对象。
     *                             1. 参数可以是Java Bean
     *                             2. 参数可以是Map
     * @throws Exception
     */
    public XJSONObject toJson(Object i_JavaData) throws Exception
    {
        return parser(i_JavaData);
    }
    
    
    
    /**
     * 解释Java对象成为JSON
     * 
     * 主要用于 i_JavaData 的类型为 List集合的情况
     * 
     * @param i_JavaData      Java对象。
     *                             1. 参数可以是Java Bean
     *                             2. 参数可以是Map
     * @param i_JSONRootName  设置生成Json字符串的顶级节点名称
     * @throws Exception
     */
    public <T> XJSONObject toJson(T i_JavaData ,String i_JSONRootName) throws Exception
    {
        return parser(i_JavaData ,i_JSONRootName);
    }
    
    
    
    /**
     * 解释Java对象成为JSON
     * 
     * @param i_JavaData      Java对象。
     *                             1. 参数可以是Java Bean
     *                             2. 参数可以是Map
     * @throws Exception
     */
    public XJSONObject parser(Object i_JavaData) throws Exception
    {
        if ( i_JavaData == null )
        {
            throw new NullPointerException("XJSON parser(Object) parameter is null.");
        }
        
        Map<Object ,Integer> v_ParserObjects = new HashMap<Object ,Integer>();   // 解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
        Return<XJSONObject>  v_Ret           = parser("" ,i_JavaData ,null ,v_ParserObjects ,false);
        
        this.rootJSON = v_Ret.paramObj;
        
        return v_Ret.paramObj;
    }
    
    
    
    /**
     * 解释Java对象成为JSON
     * 
     * 主要用于 i_JavaData 的类型为 List集合的情况
     * 
     * @param i_JavaData      Java对象。
     *                             1. 参数可以是Java Bean
     *                             2. 参数可以是Map
     * @param i_JSONRootName  设置生成Json字符串的顶级节点名称
     * @throws Exception
     */
    public <T> XJSONObject parser(T i_JavaData ,String i_JSONRootName) throws Exception
    {
        if ( i_JavaData == null )
        {
            throw new NullPointerException("XJSON parser(Object) parameter is null.");
        }
        
        Map<Object ,Integer> v_ParserObjects = new HashMap<Object ,Integer>();   // 解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
        Return<XJSONObject>  v_Ret           = parser(i_JSONRootName ,i_JavaData ,null ,v_ParserObjects ,false);
        
        this.rootJSON = v_Ret.paramObj;
        
        return v_Ret.paramObj;
    }
    
    
    
    /**
     * 内部循环递归解释
     * 
     * @author      ZhengWei(HY)
     * @version     v1.0
     *              v2.0  2020-01-15  添加：Json方式显示成员方法。格式如：public:(参数类型):返回类型
     *
     * @param i_JSONName
     * @param i_JavaData              Java对象。
     *                                    1. 参数可以是Java Bean
     *                                    2. 参数可以是Map
     * @param i_JsonSuperObj
     * @param i_ParserObjects         解析过的对象，防止对象中递归引用对象，而造成无法解释的问题。
     * @param i_MethodReturnIsObject  判定方法的返回类型是否为java.lang.Object
     * @return
     * @throws Exception
     */
    protected Return<XJSONObject> parser(String i_JSONName ,final Object i_JavaData ,XJSONObject i_JsonSuperObj ,Map<Object ,Integer> i_ParserObjects ,boolean i_MethodReturnIsObject) throws Exception
    {
        Return<XJSONObject> v_Ret              = new Return<XJSONObject>(true);
        Class<?>            v_JavaClass        = i_JavaData.getClass();
        String              v_JSONName         = i_JSONName;
        Object              v_JsonValue        = XJSONToJson.executeToJson(this ,i_ParserObjects ,i_JavaData);
        boolean             v_JsonValueIsValid = true;
        XJSONObject         v_JsonSuper        = i_JsonSuperObj;
        
        // 确保Json的最外层是一个对象，即最外层是一对{}号
        if ( v_JsonValue == null )
        {
            return v_Ret;
        }
        else if ( v_JsonValue instanceof XJSONObject )
        {
            if ( v_JsonSuper == null )
            {
                // 递归循环时，只有一次执行到此的机会，即：根节点
                if ( Help.isNull(v_JSONName) )
                {
                    v_JsonSuper = (XJSONObject)v_JsonValue;
                }
                else
                {
                    v_JsonSuper = new XJSONObject();
                }
            }

            v_JsonValueIsValid = ((XJSONObject)v_JsonValue).size() > 0;
        }
        else if ( v_JsonValue instanceof JSONArray )
        {
            if ( v_JsonSuper == null )
            {
                // 递归循环时，只有一次执行到此的机会，即：根节点
                v_JsonSuper = new XJSONObject();
                v_JSONName  = Help.NVL(v_JSONName ,"xdatas");
            }

            v_JsonValueIsValid = ((JSONArray)v_JsonValue).size() > 0;
        }
        else
        {
            v_JsonValueIsValid = !"".equals(v_JsonValue);
        }

        // 允许返回空时，强制Json字符是有效的
        if ( this.isReturnNVL )
        {
            v_JsonValueIsValid = true;
        }
        
        if ( v_JsonSuper != null && !Help.isNull(v_JSONName) )
        {
            if ( v_JsonValueIsValid )
            {
                if ( this.isSerializable && i_MethodReturnIsObject )
                {
                    if ( i_JavaData instanceof List || i_JavaData instanceof Set )
                    {
                        // 2021-09-30 添加：判定方法的返回类型是否为java.lang.Object
                        // 预规定：List、Set中的所有元素类型均是一样的类型，不存在五花八门的不同Java类型的情况
                        v_JsonSuper.put(v_JSONName + $Serializable_SplitKey + v_JavaClass.getName() + $Serializable_SplitKey + ((Collection<?>)i_JavaData).iterator().next().getClass().getName() ,v_JsonValue);
                    }
                    else
                    {
                        v_JsonSuper.put(v_JSONName + $Serializable_SplitKey + v_JavaClass.getName() ,v_JsonValue);
                    }
                }
                else
                {
                    v_JsonSuper.put(v_JSONName ,v_JsonValue);
                }
            }
        }
        
        return v_Ret.paramObj(v_JsonSuper);
    }
    
    
    
    /**
     * Java转Json时，解析过的对象，防止对象中递归引用对象，而造成无法解释的问题
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-05-18
     * @version     v1.0
     *
     * @param io_ParserObjects
     * @param i_Obj
     * @return
     */
    protected boolean isRecursion(Map<Object ,Integer> io_ParserObjects ,Object i_Obj)
    {
        if ( this.recursionCount >= 1 )
        {
            Integer v_Count = io_ParserObjects.get(i_Obj);
            if ( v_Count != null )
            {
                if ( v_Count.intValue() >= this.recursionCount )
                {
                    return true;
                }
                
                io_ParserObjects.put(i_Obj ,v_Count.intValue() + 1);
            }
            else
            {
                io_ParserObjects.put(i_Obj ,1);
            }
        }
        
        return false;
    }
    
    
    
    @Deprecated
    public XJSONObject getRootJSON()
    {
        return this.rootJSON;
    }
    
    
    
    /**
     * 获取：首写字母是否大写（默认：首写字母小写）
     * 
     * 当使用 object.getter() 的方法名称作为JSONObject.put(name ,value)的 name 值时，首写字母是否大小
     * 
     * 只用于Java对象实例解释成JSONObject对象
     */
    public boolean isFirstCharIsUpper()
    {
        return firstCharIsUpper;
    }
    

    
    /**
     * 设置：首写字母是否大写（默认：首写字母小写）
     * 
     * 当使用 object.getter() 的方法名称作为JSONObject.put(name ,value)的 name 值时，首写字母是否大小
     * 
     * 只用于Java对象实例解释成JSONObject对象
     * 
     * @param firstCharIsUpper
     */
    public void setFirstCharIsUpper(boolean firstCharIsUpper)
    {
        this.firstCharIsUpper = firstCharIsUpper;
    }



    /**
     * 获取：* 是否精确解释（默认：false）
     * 
     * 当精确解释时=true，   JSON字符串中的Key必须找到Java的setter方法，否则报错。
     * 当精确解释时=true，   Java对象转为JSON字符串时，必须getter方法与setter方法成对出现时，才会生成JSON字符串。
     * 当非精确解释时=false，JSON字符串中的Key找到不Java的setter方法时，忽略JSON字符串Key对应的值。
     * 当非精确解释时=false，Java对象转为JSON字符串时，不要求getter方法与setter方法必须成对出现。
     * 
     * 只用于解释JSON字符串成为Java实例对象
     */
    public boolean isAccuracy()
    {
        return isAccuracy;
    }
    

    
    /**
     * 设置：* 是否精确解释（默认：false）
         * 
         * 当精确解释时=true，   JSON字符串中的Key必须找到Java的setter方法，否则报错。
         * 当精确解释时=true，   Java对象转为JSON字符串时，必须getter方法与setter方法成对出现时，才会生成JSON字符串。
         * 当非精确解释时=false，JSON字符串中的Key找到不Java的setter方法时，忽略JSON字符串Key对应的值。
         * 当非精确解释时=false，Java对象转为JSON字符串时，不要求getter方法与setter方法必须成对出现。
         * 
         * 只用于解释JSON字符串成为Java实例对象
     * 
     * @param isAccuracy
     */
    public void setAccuracy(boolean isAccuracy)
    {
        this.isAccuracy = isAccuracy;
    }
    



    /**
     * 获取：将对象转为Json字符串时，对象的方法返回 null 或 ""空字符串时，
     * 是否还将其生成在Json字符串中。
     * 
     * (默认：true  即空值也生成Json字符串)
     */
    public boolean isReturnNVL()
    {
        return isReturnNVL;
    }
    

    
    /**
     * 设置：将对象转为Json字符串时，对象的方法返回 null 或 ""空字符串时，
     * 是否还将其生成在Json字符串中。
     * 
     * (默认：true  即空值也生成Json字符串)
     * 
     * @param isReturnNVL
     */
    public void setReturnNVL(boolean isReturnNVL)
    {
        this.isReturnNVL = isReturnNVL;
    }
    

    
    /**
     * 获取：当Java对象转Json字符串时，是否包含对成员方法的转换输出
     * 默认为：false，不转换输出
     */
    public boolean isJsonMethod()
    {
        return isJsonMethod;
    }


    
    /**
     * 设置：当Java对象转Json字符串时，是否包含对成员方法的转换输出
     * 默认为：false，不转换输出
     * 
     * @param isJsonMethod
     */
    public void setJsonMethod(boolean isJsonMethod)
    {
        this.isJsonMethod = isJsonMethod;
    }


    
    /**
     * 实现：Json序列化和反序列化
     * 
     * 在Java对象转Json字符串时，对于getter方法的返回类型为java.lang.Object时，
     * 是否在Json字符串中包含getter方法的返回值的真实Java类型（ClassName）。
     * 
     * 控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
     * 
     * 默认为：false，不转换输出
     */
    public boolean isSerializable()
    {
        return isSerializable;
    }

    

    /**
     * 实现：Json序列化和反序列化
     * 
     * 在Java对象转Json字符串时，对于getter方法的返回类型为java.lang.Object时，
     * 是否在Json字符串中包含getter方法的返回值的真实Java类型（ClassName）。
     * 
     * 控制参数 isJsonClassByObject 只控制Java转Json的过程；Json转Java的过程将自动判定
     * 
     * 默认为：false，不转换输出
     */
    public void setSerializable(boolean isSerializable)
    {
        this.isSerializable = isSerializable;
    }



    /**
     * 获取：Java转Json时，BigDecimal的显示格式。是否启用科学计数法（默认为：自然数字，而非科学计数法）
     */
    public boolean isBigDecimalFormat()
    {
        return isBigDecimalFormat;
    }
    

    
    /**
     * 设置：Java转Json时，BigDecimal的显示格式。是否启用科学计数法（默认为：自然数字，而非科学计数法）
     * 
     * @param isBigDecimalFormat
     */
    public void setBigDecimalFormat(boolean isBigDecimalFormat)
    {
        this.isBigDecimalFormat = isBigDecimalFormat;
    }


    
    /**
     * 获取：Java转Json时，数值转字符串时保留小数位数（默认为：NULL表示原样转Json字符串）
     */
    public Integer getDigit()
    {
        return digit;
    }
    

    
    /**
     * 设置：Java转Json时，数值转字符串时保留小数位数（默认为：NULL表示原样转Json字符串）
     * 
     * @param i_Digit
     */
    public void setDigit(Integer i_Digit)
    {
        if ( i_Digit != null )
        {
            this.digit = Math.abs(i_Digit);
        }
        else
        {
            this.digit = i_Digit;
        }
    }
    

    
    /**
     * 获取：允许递归出现在次数。Java转Json时，防止对象中递归引用对象，而造成无法解释的问题（默认值：0，表示不防止递归 ）
     */
    public int getRecursionCount()
    {
        return recursionCount;
    }
    

    
    /**
     * 设置：允许递归出现在次数。Java转Json时，防止对象中递归引用对象，而造成无法解释的问题（默认值：0，表示不防止递归 ）
     * 
     * @param recursionCount
     */
    public void setRecursionCount(int recursionCount)
    {
        this.recursionCount = recursionCount;
    }
    


    public void setObjectClassName(String i_ClassName) throws ClassNotFoundException
    {
        if ( Help.isNull(i_ClassName) )
        {
            throw new NullPointerException("Class name is null.");
        }
        
        this.parserObjectClass = Help.forName(i_ClassName);
    }
    
    
    
    public void setObjectClass(Class<?> i_Class)
    {
        this.parserObjectClass = i_Class;
    }
    
    
    
    public Class<?> getObjectClass()
    {
        if ( this.parserObjectClass == null )
        {
            return Hashtable.class;
        }
        else
        {
            return this.parserObjectClass;
        }
    }
    
    
    
    @Deprecated
    public String toJSONString()
    {
        if ( this.rootJSON != null )
        {
            return this.rootJSON.toJSONString();
        }
        else
        {
            return "";
        }
    }



    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    protected void finalize() throws Throwable
    {
        if ( this.rootJSON != null )
        {
            this.rootJSON.clear();
            this.rootJSON = null;
        }
        
        super.finalize();
    }
    */
    
    
    
    /**
     * 简单判定字符串是否为Json格式的字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-01-15
     * @version     v1.0
     *
     * @param i_JsonText
     * @return
     */
    public static boolean isJson(String i_JsonText)
    {
        if ( Help.isNull(i_JsonText) )
        {
            return false;
        }
        
        String v_JT = StringHelp.replaceAll(i_JsonText ,new String[] {"\n" ,"\r" ,"\t" ," "} ,new String[] {""});
        
        int v_Len = v_JT.length();
        if ( v_Len < 2)
        {
            return false;
        }
        
        if ( (v_JT.startsWith("{") || v_JT.startsWith("[")) && (v_JT.endsWith("]") || v_JT.endsWith("}")) )
        {
            int[]   v_Datas    = new int[v_Len / 2];
            int     v_Size     = -1;
            char    v_One      = 0;
            boolean v_Goto     = false;   // 是否进入两个双引号内？
            boolean v_HaveGoto = false;   // i 之前是否有进入两个双引号？
            
            for (int i=0; i<v_Len; i++)
            {
                v_One = v_JT.charAt(i);
                
                if ( v_One == '"' )
                {
                    if ( v_Goto )
                    {
                        if ( v_HaveGoto )
                        {
                            return false;
                        }
                        v_Goto     = false;
                        v_HaveGoto = true;
                    }
                    else
                    {
                        v_Goto = true;
                    }
                }
                else if ( v_Goto )
                {
                    continue;
                }
                else if ( v_One == ':' )
                {
                    v_HaveGoto =  false;
                }
                else if ( v_One == ',' )
                {
                    if ( v_JT.charAt(i + 1) != '"'
                      && v_JT.charAt(i + 1) != ']'
                      && v_JT.charAt(i + 1) != '}'
                      && v_JT.charAt(i + 1) != '{'
                      && v_JT.charAt(i + 1) != '[' )
                    {
                        return false;
                    }
                    
                    v_HaveGoto =  false;
                }
                else if ( v_One == '[' )
                {
                    if ( v_HaveGoto )
                    {
                       return false;
                    }
                    
                    v_HaveGoto        = false;
                    v_Datas[++v_Size] = 2;
                }
                else if ( v_One == '{' )
                {
                    if ( v_HaveGoto )
                    {
                       return false;
                    }
                    
                    v_HaveGoto        = false;
                    v_Datas[++v_Size] = 3;
                }
                else if ( v_One == ']' )
                {
                    if ( v_Size >= 0 && v_Datas[v_Size] == 2 )
                    {
                        --v_Size;
                        v_HaveGoto = false;
                    }
                    else
                    {
                        return false;
                    }
                }
                else if ( v_One == '}' )
                {
                    if ( v_Size >= 0 && v_Datas[v_Size] == 3 )
                    {
                        --v_Size;
                        v_HaveGoto = false;
                    }
                    else
                    {
                        return false;
                    }
                }
                else if ( v_One == '\\' && i + 1 < v_Len && v_JT.charAt(i + 1) == '"' )
                {
                    // 跳过 \" 字符串的组合（转义双引号）
                    i++;
                }
            }

            return v_Size == -1;
        }
        
        return false;
    }
    
    
    
    /**
     * 格式化JSON字符串(默认)
     * 
     * @param i_Json
     * @return
     */
    public static String format(String i_Json)
    {
        return format(i_Json ,"\t" ," " ,"\n");
    }
    
    
    
    /**
     * 格式化JSON字符串为Html字符串
     * 
     * @param i_Json
     * @return
     */
    public static String formatHtml(String i_Json)
    {
        return format(i_Json ,"&nbsp;&nbsp;&nbsp;&nbsp;" ,"&nbsp;" ,"<br>");
    }
    
    
    
    /**
     * 格式化JSON字符串
     * 
     * @param i_Json
     * @param i_FillTab
     * @param i_FillSpace
     * @param i_NewLine
     * @return
     */
    public static String format(String i_Json ,String i_FillTab ,String i_FillSpace ,String i_NewLine)
    {
        if ( Help.isNull(i_Json) )
        {
            return null;
        }
        
        int          v_FixedLenth = 0;
        List<String> v_TokenList  = new ArrayList<String>();
        {
            String jsonTemp = i_Json;
            // 预读取
            while ( jsonTemp.length() > 0 )
            {
                String token = getToken(jsonTemp);
                jsonTemp = jsonTemp.substring(token.length());
                token = token.trim();
                v_TokenList.add(token);
            }
        }
        
        for (int i = 0; i < v_TokenList.size(); i++)
        {
            String token = v_TokenList.get(i);
            int length = token.getBytes().length;
            if ( length > v_FixedLenth && i < v_TokenList.size() - 1 && v_TokenList.get(i + 1).equals(":") )
            {
                v_FixedLenth = length;
            }
        }
        
        StringBuilder v_Buffer = new StringBuilder();
        int           v_Count  = 0;
        for (int i = 0; i < v_TokenList.size(); i++)
        {
            String v_Token = v_TokenList.get(i);
            
            if ( v_Token.equals(",") )
            {
                v_Buffer.append(v_Token);
                doFill(v_Buffer ,v_Count ,i_FillTab ,i_NewLine);
                continue;
            }
            
            if ( v_Token.equals(":") )
            {
                v_Buffer.append(i_FillSpace).append(v_Token).append(i_FillSpace);
                continue;
            }
            
            if ( v_Token.equals("{") )
            {
                String v_NextToken = v_TokenList.get(i + 1);
                if ( v_NextToken.equals("}") )
                {
                    i++;
                    v_Buffer.append("{ }");
                }
                else
                {
                    v_Count++;
                    v_Buffer.append(v_Token);
                    doFill(v_Buffer ,v_Count ,i_FillTab ,i_NewLine);
                }
                continue;
            }
            
            if ( v_Token.equals("}") )
            {
                v_Count--;
                doFill(v_Buffer ,v_Count ,i_FillTab ,i_NewLine);
                v_Buffer.append(v_Token);
                continue;
            }
            
            if ( v_Token.equals("[") )
            {
                String v_NextToken = v_TokenList.get(i + 1);
                if ( v_NextToken.equals("]") )
                {
                    i++;
                    v_Buffer.append("[ ]");
                }
                else
                {
                    v_Count++;
                    v_Buffer.append(v_Token);
                    doFill(v_Buffer ,v_Count ,i_FillTab ,i_NewLine);
                }
                continue;
            }
            
            if ( v_Token.equals("]") )
            {
                v_Count--;
                doFill(v_Buffer ,v_Count ,i_FillTab ,i_NewLine);
                v_Buffer.append(v_Token);
                continue;
            }
            
            v_Buffer.append(v_Token);
            // 左对齐
            /*
            if ( i < v_TokenList.size() - 1 && v_TokenList.get(i + 1).equals(":") )
            {
                int fillLength = v_FixedLenth - v_Token.getBytes().length;
                if ( fillLength > 0 )
                {
                    for (int j = 0; j < fillLength; j++)
                    {
                        v_Buffer.append("1 ");
                    }
                }
            }
            */
        }
        
        return v_Buffer.toString();
    }



    private static String getToken(String i_Json)
    {
        StringBuilder v_Bufffer  = new StringBuilder();
        boolean       isInYinHao = false;
        while ( i_Json.length() > 0 )
        {
            String v_Token = i_Json.substring(0 ,1);
            i_Json = i_Json.substring(1);
            
            if ( !isInYinHao && (v_Token.equals(":") || v_Token.equals("{") || v_Token.equals("}") || v_Token.equals("[") || v_Token.equals("]") || v_Token.equals(",")) )
            {
                if ( v_Bufffer.toString().trim().length() == 0 )
                {
                    v_Bufffer.append(v_Token);
                }
                break;
            }
            if ( v_Token.equals("\\") )
            {
                v_Bufffer.append(v_Token);
                v_Bufffer.append(i_Json.substring(0 ,1));
                i_Json = i_Json.substring(1);
                continue;
            }
            if ( v_Token.equals("\"") )
            {
                v_Bufffer.append(v_Token);
                if ( isInYinHao )
                {
                    break;
                }
                else
                {
                    isInYinHao = true;
                    continue;
                }
            }
            
            v_Bufffer.append(v_Token);
        }
        
        return v_Bufffer.toString();
    }



    private static void doFill(StringBuilder io_Buffer ,int i_Count ,String i_FillStr ,String i_NewLine)
    {
        io_Buffer.append(i_NewLine);
        
        for (int i = 0; i < i_Count; i++)
        {
            io_Buffer.append(i_FillStr);
        }
    }
    
}
