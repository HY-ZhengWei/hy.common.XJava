package org.hy.common.xml.xcql;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodInfo;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.TablePartitionRID;
import org.hy.common.TablePartitionSet;
import org.hy.common.xcql.DBCQL;
import org.hy.common.xcql.XCQL;
import org.hy.common.xcql.XCQLData;
import org.hy.common.xml.SerializableClass;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XParamAnnotation;
import org.hy.common.xml.annotation.Xcql;
import org.hy.common.xml.annotation.Xparam;
import org.hy.common.xml.log.Logger;





/**
 * XCQL代理。
 * 
 * 详见 @see org.hy.common.xml.annotation.Xcql
 * 详见 @see org.hy.common.xml.annotation.XParam
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-06-24
 * @version     v1.0
 */
public class XCQLProxy implements InvocationHandler ,Serializable
{

    private static final long   serialVersionUID = 5194164371858661026L;

    private static final Logger $Logger          = new Logger(XCQLProxy.class);
    
    /**
     * names()[x] 值为"ToMap"时，表示将方法入参转为Map集合后再putAll()整合后的大Map集合中。
     * 
     * @see org.hy.common.xml.annotation.Xcql.names()
     */
    public  static final String $ParamName_ToMap = "ToMap";
    
    /** 代理的接口类 */
    private final Class<?>                        xcqlInterface;
    
    /**
     * 代理接口的实现类。
     * 
     * 当相同XJava的ID同时在接口类及接口的实现类上标记时，此属性才生效。
     * 
     * 类似于JDK 9版本中的新特性：私有接口方法，不过此方法还可以public的。
     * 
     * 可实现如下新奇功能：
     *    接口中被@Xcql注释的方法用代理实现。
     *    同时，接口的实现类，也可实现其中未被@Xcql注释的方法。
     */
    private Object                                xcqlInstace;
    
    /** 被@Xcql注解的方法集合 */
    private final Map<MethodInfo ,XCQLAnnotation> methods;
    
    
    
    /**
     * 创建代理实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_XCQLInterface
     * @return
     */
    public static Object newProxy(Class<?> i_XCQLInterface)
    {
        return newProxy(i_XCQLInterface ,null);
    }
    
    
    
    /**
     * 创建代理实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_XCQLInterface
     * @param i_XCQLInstace
     * @return
     */
    public static Object newProxy(Class<?> i_XCQLInterface ,Object i_XCQLInstace)
    {
        XCQLProxy v_XCQLProxy = new XCQLProxy(i_XCQLInterface);
        
        v_XCQLProxy.setXcqlInstace(i_XCQLInstace);
        
        return Proxy.newProxyInstance(i_XCQLInterface.getClassLoader() ,new Class[]{i_XCQLInterface} ,v_XCQLProxy);
    }
    
    
    
    /**
     * 通过代理实例，获取XCQL代理实例（即本类实例）。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Proxy
     * @return
     */
    public static XCQLProxy getXCQLProxy(Object i_Proxy)
    {
        if ( i_Proxy instanceof Proxy )
        {
            InvocationHandler v_Invocation = Proxy.getInvocationHandler(i_Proxy);
            
            if ( v_Invocation instanceof XCQLProxy )
            {
                return (XCQLProxy)v_Invocation;
            }
        }
        
        return null;
    }
    
    
    
    public XCQLProxy(Class<?> i_XCQLInterface)
    {
        this.xcqlInterface     = i_XCQLInterface;
        this.methods           = new HashMap<MethodInfo ,XCQLAnnotation>();
        List<Method> v_Methods = MethodReflect.getAnnotationMethods(i_XCQLInterface ,Xcql.class);
        
        if ( !Help.isNull(v_Methods) )
        {
            for (Method v_Method : v_Methods)
            {
                try
                {
                    List<Xparam>   v_XParams = MethodReflect.getParameterAnnotations(v_Method ,Xparam.class);
                    XCQLAnnotation v_Anno    = new XCQLAnnotation(v_Method ,v_Method.getAnnotation(Xcql.class) ,v_XParams);
                    
                    this.methods.put(new MethodInfo(v_Method) ,v_Anno);
                    
                    // 方法入参个数大于1，应设置@Xcql(names)
                    if ( Help.isNull(v_Anno.getXparams()) && v_Method.getParameterTypes().length >= 2 )
                    {
                        this.errorLog(v_Method ,"Method parameter count >= 2 ,but @Xcql(names) count is 0." ,null);
                        return;
                    }
                    // @Xcql中设置的参数名称个数，应于方法入参个数数量相同
                    else if ( !Help.isNull(v_Anno.getXparams())
                           && v_Anno.getXparams().size() > v_Method.getParameterTypes().length )
                    {
                        this.errorLog(v_Method ,"@Xcql(names) count greater than method parameter count." ,null);
                        return;
                    }
                }
                catch (Exception exce)
                {
                    this.errorLog(v_Method ,exce.toString() ,exce);
                }
            }
            
            v_Methods.clear();
            v_Methods = null;
        }
    }
    
    

    @Override
    public Object invoke(Object i_Proxy ,Method i_Method ,Object [] i_Args) throws Throwable
    {
        try
        {
            if ( Object.class.equals(i_Method.getDeclaringClass()) )
            {
                return i_Method.invoke(this ,i_Args);
            }
            else
            {
                XCQLAnnotation v_Anno = this.methods.get(new MethodInfo(i_Method));
                
                if ( v_Anno == null )
                {
                    return i_Method.invoke(this ,i_Args);
                }
                else
                {
                    v_Anno.setXid(Help.NVL(v_Anno.getXcql().id() ,Help.NVL(v_Anno.getXcql().value() ,i_Method.getName())));
                    Object v_XObject = XJava.getObject(v_Anno.getXid());
                    
                    if ( v_XObject == null )
                    {
                        return errorLog(i_Method ,"XID [" + v_Anno.getXid() + "] is not exists." ,null);
                    }
                    else if ( v_XObject instanceof XCQL )
                    {
                        return execute(i_Method ,v_Anno ,(XCQL)v_XObject ,i_Args);
                    }
                    else
                    {
                        return errorLog(i_Method ,"XID [" + v_Anno.getXid() + "] java class type is not XCQL." ,null);
                    }
                }
            }
        }
        catch (Exception exce)
        {
            // 输出异常后，并断续向外抛
            $Logger.error(exce);
            exce.printStackTrace();
            throw exce;
        }
    }
    
    
    
    /**
     * 异常日志。同时返回执行方法的返回值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_ErrorInfo  为空，表示只返回不输出日志
     * @return
     */
    private Object errorLog(Method i_Method ,String i_ErrorInfo ,Exception i_Exce)
    {
        if ( !Help.isNull(i_ErrorInfo) )
        {
            $Logger.error("\nError: Call " + this.xcqlInterface.getName() + "." + i_Method.getName() + "：" + i_ErrorInfo + "\n" ,i_Exce);
        }
        
        // 定义的方法无返回类型：void
        if ( Void.TYPE == i_Method.getReturnType() )
        {
            return null;
        }
        // 返回执行成功与否标记
        else if ( Boolean.class == i_Method.getReturnType()
               || boolean.class == i_Method.getReturnType() )
        {
            return Boolean.FALSE;
        }
        else if ( Integer.class == i_Method.getReturnType()
               || int    .class == i_Method.getReturnType() )
        {
            return -1;
        }
        else if ( Long.class == i_Method.getReturnType()
               || long.class == i_Method.getReturnType() )
         {
             return -1L;
         }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 成功时输出的日志，当 @Xcql.log 有值时。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_Return
     */
    private void succeedLog(XCQLAnnotation i_Anno ,Object i_Return)
    {
        if ( Help.isNull(i_Anno.getXcql().log()) )
        {
            return;
        }
        
        if ( i_Return instanceof List )
        {
            $Logger.info(i_Anno.getXcql().log() + "，共 " + ((List<?>)i_Return).size() + " 个记录。");
        }
        else if ( i_Return instanceof Set )
        {
            $Logger.info(i_Anno.getXcql().log() + "，共 " + ((Set<?>)i_Return).size() + " 个记录。");
        }
        else if ( MethodReflect.isExtendImplement(i_Return ,PartitionMap.class) )
        {
            $Logger.info(i_Anno.getXcql().log() + "，共 " + ((PartitionMap<? ,?>)i_Return).size() + " 个分区，" + ((PartitionMap<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof TablePartitionRID )
        {
            $Logger.info(i_Anno.getXcql().log() + "，共 " + ((TablePartitionRID<? ,?>)i_Return).size() + " 个分区，" + ((TablePartitionRID<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof TablePartitionSet )
        {
            $Logger.info(i_Anno.getXcql().log() + "，共 " + ((TablePartitionSet<? ,?>)i_Return).size() + " 个分区，" + ((TablePartitionSet<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof Map )
        {
            $Logger.info(i_Anno.getXcql().log() + "，共 " + ((Map<? ,?>)i_Return).size() + " 个记录。");
        }
        else if ( i_Return instanceof Boolean )
        {
            $Logger.info(i_Anno.getXcql().log() + "，返回 " + i_Return + " 。");
        }
        else
        {
            $Logger.info(i_Anno.getXcql().log() + "，返回 " + i_Return.toString() + " 。");
        }
    }
    
    
    
    /**
     * 执行XCQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private Object execute(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        // 执行语句（用 execute 属性判定的）：如DDL
        if ( i_Anno.getXcql().execute() )
        {
            return executeXCQL_Execute(i_Method ,i_Anno ,i_XCQL ,i_Args);
        }
        // 查询语句：如MATCH (n) RETURN n ...
        else if ( i_XCQL.getContentDB().getCQLType() == DBCQL.$DBCQL_TYPE_MATCH )
        {
            return executeXCQL_Query(i_Method ,i_Anno ,i_XCQL ,i_Args);
        }
        // 插入语句：如Insert ...
        else if ( i_XCQL.getContentDB().getCQLType() == DBCQL.$DBCQL_TYPE_CREATE )
        {
            if ( XCQLData.class == i_Method.getReturnType() )
            {
                return executeXCQL_ExecuteInsert(i_Method ,i_Anno ,i_XCQL ,i_Args);
            }
            else
            {
                return executeXCQL_ExecuteUpdate(i_Method ,i_Anno ,i_XCQL ,i_Args);
            }
        }
        // 更新 & 删除语句
        else if ( i_XCQL.getContentDB().getCQLType() == DBCQL.$DBCQL_TYPE_SET
               || i_XCQL.getContentDB().getCQLType() == DBCQL.$DBCQL_TYPE_DELETE )
        {
            if ( XCQLData.class == i_Method.getReturnType() )
            {
                return executeXCQL_ExecuteInsert(i_Method ,i_Anno ,i_XCQL ,i_Args);
            }
            else
            {
                return executeXCQL_ExecuteUpdate(i_Method ,i_Anno ,i_XCQL ,i_Args);
            }
        }
        // 执行语句（用 CQL 类型的）：如DDL
        else if ( i_XCQL.getContentDB().getCQLType() == DBCQL.$DBCQL_TYPE_DDL )
        {
            return executeXCQL_Execute(i_Method ,i_Anno ,i_XCQL ,i_Args);
        }
        else if ( i_XCQL.getContentDB().getCQLType() == DBCQL.$DBCQL_TYPE_UNKNOWN )
        {
            return executeXCQL_Execute(i_Method ,i_Anno ,i_XCQL ,i_Args);
        }
        
        return null;
    }
    
    
    
    /**
     * 执行XCQL -- 查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private Object executeXCQL_Query(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        // 支持分页模板自动封装的查询  2018-07-21
        XCQL v_XCQL = i_XCQL;
        if ( i_Anno.getXcql().paging() )
        {
            v_XCQL = XCQL.queryPaging(v_XCQL);
            
            if ( v_XCQL == null )
            {
                v_XCQL = i_XCQL;
            }
        }
        
        // 更新缓存ID
        if ( !Help.isNull(i_Anno.getXcql().updateCacheID()) )
        {
            return executeXCQL_Query_UpdateCache(i_Method ,i_Anno ,v_XCQL ,i_Args);
        }
        // 定义缓存ID。
        else if ( !Help.isNull(i_Anno.getXcql().cacheID()) )
        {
            return executeXCQL_Query_Cache      (i_Method ,i_Anno ,v_XCQL ,i_Args);
        }
        // 常规查询
        else
        {
            return executeXCQL_Query_Normal     (i_Method ,i_Anno ,v_XCQL ,i_Args);
        }
    }
    
    
    
    /**
     * 执行XCQL -- 查询（带同步锁的，用于高速缓存的更新功能）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private synchronized Object executeXCQL_Query_UpdateCache(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        return executeXCQL_Query_Normal(i_Method ,i_Anno ,i_XCQL ,i_Args);
    }
    
    
    
    /**
     * 执行XCQL -- 查询（带同步锁的，用于高速缓存功能）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private synchronized Object executeXCQL_Query_Cache(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        Object v_Ret = XJava.getObject(i_Anno.getXcql().cacheID());
        
        if ( v_Ret != null )
        {
            return v_Ret;
        }
        
        return executeXCQL_Query_Normal(i_Method ,i_Anno ,i_XCQL ,i_Args);
    }
    
    
    
    /**
     * 执行XCQL -- 查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *              v2.0  2018-01-25 添加：方法返回值是整数，则按查询记录行数的SELECT Count(1) FROM ... 返回
     *              v3.0  2018-04-27 添加：returnOne注解属性支持Map、Set集合随机获取一个元素的功能。
     *              v4.0  2023-03-07 添加：方法返回类型是String、Integer、Double、Float、BigDecimal、Date的，则按第一行第一列数据返回
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private Object executeXCQL_Query_Normal(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        Object v_Params = null;
        Object v_Ret    = null;
        
        try
        {
            v_Params = getExecuteParams(i_Anno ,i_Args);
        }
        catch (Exception exce)
        {
            return this.errorLog(i_Method ,null ,exce);
        }
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            if ( !i_Anno.getXcql().firstValue() )
            {
                v_Ret = i_XCQL.query();
            }
            else if ( i_Method.getReturnType() == String.class )
            {
                v_Ret = i_XCQL.queryCQLValue();
                if ( v_Ret != null )
                {
                    v_Ret = v_Ret.toString();
                }
                return v_Ret;
            }
            else if ( i_Method.getReturnType() == Date.class
                   || i_Method.getReturnType() == java.util.Date.class )
            {
                v_Ret = i_XCQL.queryCQLValue();
                if ( v_Ret != null )
                {
                    v_Ret = new Date(v_Ret.toString());
                }
                return v_Ret;
            }
            else if ( i_Method.getReturnType() == Integer.class
                   || i_Method.getReturnType() == int.class )
            {
                 v_Ret = i_XCQL.queryCQLValue();
                 if ( v_Ret != null )
                 {
                     v_Ret = Integer.valueOf(v_Ret.toString());
                 }
                 else if ( i_Method.getReturnType() == int.class )
                 {
                     return 0;
                 }
                 return v_Ret;
            }
            else if ( i_Method.getReturnType() == Long.class
                   || i_Method.getReturnType() == long.class )
            {
                  v_Ret = i_XCQL.queryCQLValue();
                  if ( v_Ret != null )
                  {
                      v_Ret = Long.valueOf(v_Ret.toString());
                  }
                  else if ( i_Method.getReturnType() == long.class )
                  {
                      return 0L;
                  }
                  return v_Ret;
            }
            else if ( i_Method.getReturnType() == Double.class
                   || i_Method.getReturnType() == double.class )
            {
                v_Ret = i_XCQL.queryCQLValue();
                if ( v_Ret != null )
                {
                    v_Ret = Double.valueOf(v_Ret.toString());
                }
                else if ( i_Method.getReturnType() == double.class )
                {
                    return 0D;
                }
                return v_Ret;
            }
            else if ( i_Method.getReturnType() == Float.class
                   || i_Method.getReturnType() == float.class )
            {
                 v_Ret = i_XCQL.queryCQLValue();
                 if ( v_Ret != null )
                 {
                     v_Ret = Float.valueOf(v_Ret.toString());
                 }
                 else if ( i_Method.getReturnType() == float.class )
                 {
                     return 0F;
                 }
                 return v_Ret;
            }
            else if ( i_Method.getReturnType() == BigDecimal.class )
            {
                  v_Ret = i_XCQL.queryCQLValue();
                  if ( v_Ret != null )
                  {
                      v_Ret = new BigDecimal(v_Ret.toString());
                  }
                  return v_Ret;
            }
            else
            {
                v_Ret = i_XCQL.query();
            }
        }
        else
        {
            try
            {
                if ( !i_Anno.getXcql().firstValue() )
                {
                    v_Ret = i_XCQL.query(v_Params);
                }
                else if ( i_Method.getReturnType() == String.class )
                {
                    v_Ret = i_XCQL.queryCQLValue(v_Params);
                    if ( v_Ret != null )
                    {
                        v_Ret = v_Ret.toString();
                    }
                    return v_Ret;
                }
                else if ( i_Method.getReturnType() == Date.class
                       || i_Method.getReturnType() == java.util.Date.class )
                {
                    v_Ret = i_XCQL.queryCQLValue(v_Params);
                    if ( v_Ret != null )
                    {
                        v_Ret = new Date(v_Ret.toString());
                    }
                }
                else if ( i_Method.getReturnType() == Integer.class
                       || i_Method.getReturnType() == int.class )
                {
                     v_Ret = i_XCQL.queryCQLValue(v_Params);
                     if ( v_Ret != null )
                     {
                         v_Ret = Integer.valueOf(v_Ret.toString());
                     }
                     else if ( i_Method.getReturnType() == int.class )
                     {
                         return 0;
                     }
                     return v_Ret;
                }
                else if ( i_Method.getReturnType() == Long.class
                       || i_Method.getReturnType() == long.class)
                {
                      v_Ret = i_XCQL.queryCQLValue(v_Params);
                      if ( v_Ret != null )
                      {
                          v_Ret = Long.valueOf(v_Ret.toString());
                      }
                      else if ( i_Method.getReturnType() == long.class )
                      {
                          return 0L;
                      }
                      return v_Ret;
                }
                else if ( i_Method.getReturnType() == Double.class
                       || i_Method.getReturnType() == double.class )
                {
                    v_Ret = i_XCQL.queryCQLValue(v_Params);
                    if ( v_Ret != null )
                    {
                        v_Ret = Double.valueOf(v_Ret.toString());
                    }
                    else if ( i_Method.getReturnType() == double.class )
                    {
                        return 0D;
                    }
                    return v_Ret;
                }
                else if ( i_Method.getReturnType() == Float.class
                       || i_Method.getReturnType() == float.class )
                {
                     v_Ret = i_XCQL.queryCQLValue(v_Params);
                     if ( v_Ret != null )
                     {
                         v_Ret = Float.valueOf(v_Ret.toString());
                     }
                     else if ( i_Method.getReturnType() == float.class )
                     {
                         return 0F;
                     }
                     return v_Ret;
                }
                else if ( i_Method.getReturnType() == BigDecimal.class )
                {
                      v_Ret = i_XCQL.queryCQLValue(v_Params);
                      if ( v_Ret != null )
                      {
                          v_Ret = new BigDecimal(v_Ret.toString());
                      }
                      return v_Ret;
                }
                else
                {
                    v_Ret = i_XCQL.query(v_Params);
                }
            }
            finally
            {
                // 及时释放资源
                if ( i_Args != null && i_Args.length > 1 && MethodReflect.isExtendImplement(v_Params ,Map.class) )
                {
                    ((Map<? ,?>)v_Params).clear();
                }
                v_Params = null;
            }
        }
        
        // 复杂返回对象类型的处理。简单返回类型的已在上面return了
        if ( v_Ret != null )
        {
            if ( Void.TYPE == i_Method.getReturnType() )
            {
                this.cacheData(i_Anno ,v_Ret);
                return null;
            }
            
            if ( i_Anno.getXcql().returnOne() )
            {
                if ( MethodReflect.isExtendImplement(v_Ret ,List.class) )
                {
                    List<?> v_List = (List<?>)v_Ret;
                    
                    if ( v_List.size() >= 1 )
                    {
                        v_Ret = v_List.get(0);
                        v_List.clear();
                        v_List = null;
                    }
                    else
                    {
                        v_Ret = null;
                    }
                }
                // 支持Set集合随机获取一个元素的功能
                else if ( MethodReflect.isExtendImplement(v_Ret ,Set.class) )
                {
                    Set<?> v_Set = (Set<?>)v_Ret;
                    
                    if ( v_Set.size() >= 1 )
                    {
                        v_Ret = v_Set.iterator().next();
                        v_Set.clear();
                        v_Set = null;
                    }
                    else
                    {
                        v_Ret = null;
                    }
                }
                // 支持Map集合随机获取一个元素的功能
                else if ( MethodReflect.isExtendImplement(v_Ret ,Map.class) )
                {
                    Map<? ,?> v_Map = (Map<? ,?>)v_Ret;
                    
                    if ( v_Map.size() >= 1 )
                    {
                        v_Ret = v_Map.values().iterator().next();
                        v_Map.clear();
                        v_Map = null;
                    }
                    else
                    {
                        v_Ret = null;
                    }
                }
                // 支持Collection集合随机获取一个元素的功能
                else if ( MethodReflect.isExtendImplement(v_Ret ,Collection.class) )
                {
                    Collection<?> v_Collection = (Collection<?>)v_Ret;
                    
                    if ( v_Collection.size() >= 1 )
                    {
                        v_Ret = v_Collection.iterator().next();
                        v_Collection.clear();
                        v_Collection = null;
                    }
                    else
                    {
                        v_Ret = null;
                    }
                }
            }
            
            this.cacheData(i_Anno ,v_Ret);
            
            return v_Ret;
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 向缓存中保存或更新数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     */
    private void cacheData(XCQLAnnotation i_Anno ,Object v_CacheData)
    {
        // Object v_OldCacheData = null;
        
        if ( !Help.isNull(i_Anno.getXcql().updateCacheID()) )
        {
            // v_OldCacheData = XJava.getObject(i_Anno.getXcql().updateCacheID());
            
            XJava.putObject(i_Anno.getXcql().updateCacheID() ,v_CacheData);
            succeedLog(i_Anno ,v_CacheData);
        }
        else if ( !Help.isNull(i_Anno.getXcql().cacheID()) )
        {
            // v_OldCacheData = XJava.getObject(i_Anno.getXcql().cacheID());
            
            XJava.putObject(i_Anno.getXcql().cacheID()       ,v_CacheData);
            succeedLog(i_Anno ,v_CacheData);
        }
        else
        {
            succeedLog(i_Anno ,v_CacheData);
        }
        
        // 及时释放资源，自动的GC太慢了。
        /*
        if ( v_OldCacheData != null )
        {
            if ( MethodReflect.isExtendImplement(v_OldCacheData ,List.class) )
            {
                ((List<?>)v_OldCacheData).clear();
            }
            else if ( MethodReflect.isExtendImplement(v_OldCacheData ,Map.class) )
            {
                ((Map<? ,?>)v_OldCacheData).clear();
            }
            else if ( MethodReflect.isExtendImplement(v_OldCacheData ,Set.class) )
            {
                ((Set<?>)v_OldCacheData).clear();
            }
            
            v_OldCacheData = null;
        }
        */
    }
    
    
    
    /**
     * 执行XCQL（专用于Insert语句）
     * 
     * 有能力返回自增长ID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-27
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private XCQLData executeXCQL_ExecuteInsert(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        Object   v_Params   = null;
        XCQLData v_RetXData = null;
        
        try
        {
            v_Params = getExecuteParams(i_Anno ,i_Args);
        }
        catch (Exception exce)
        {
            this.errorLog(i_Method ,null ,exce);
            return v_RetXData;
        }
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_RetXData = i_XCQL.executeInsert();
        }
        else
        {
            if ( v_Params instanceof List )
            {
                v_RetXData = i_XCQL.executeInserts((List<?>)v_Params);
            }
            else
            {
                v_RetXData = i_XCQL.executeInsert(v_Params);
            }
            
            // 及时释放资源
            if ( i_Args != null && i_Args.length > 1 && MethodReflect.isExtendImplement(v_Params ,Map.class) )
            {
                ((Map<? ,?>)v_Params).clear();
            }
            v_Params = null;
        }
        
        succeedLog(i_Anno ,v_RetXData);
        return v_RetXData;
    }
    
    
    
    /**
     * 执行XCQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private Object executeXCQL_ExecuteUpdate(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        Object v_Params = null;
        int    v_Ret    = -1;
        
        try
        {
            v_Params = getExecuteParams(i_Anno ,i_Args);
        }
        catch (Exception exce)
        {
            return this.errorLog(i_Method ,null ,exce);
        }
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XCQL.executeUpdate();
        }
        else
        {
            if ( v_Params instanceof List )
            {
                v_Ret = i_XCQL.executeUpdates((List<?>)v_Params);
            }
            else
            {
                v_Ret = i_XCQL.executeUpdate(v_Params);
            }
            
            // 及时释放资源
            if ( i_Args != null && i_Args.length > 1 && MethodReflect.isExtendImplement(v_Params ,Map.class) )
            {
                ((Map<? ,?>)v_Params).clear();
            }
            v_Params = null;
        }
        
        succeedLog(i_Anno ,v_Ret);
        
        if ( Void.TYPE == i_Method.getReturnType() )
        {
            return null;
        }
        else if ( Boolean.class == i_Method.getReturnType()
               || boolean.class == i_Method.getReturnType() )
        {
            return v_Ret >= 1;
        }
        else if ( Integer.class == i_Method.getReturnType()
               ||     int.class == i_Method.getReturnType() )
        {
            return v_Ret;
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 执行XCQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XCQL
     * @param i_Args
     * @return
     */
    private Object executeXCQL_Execute(Method i_Method ,XCQLAnnotation i_Anno ,XCQL i_XCQL ,Object [] i_Args)
    {
        Object  v_Params = null;
        boolean v_Ret    = false;
        
        try
        {
            v_Params = getExecuteParams(i_Anno ,i_Args);
        }
        catch (Exception exce)
        {
            return this.errorLog(i_Method ,null ,exce);
        }
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XCQL.execute();
        }
        else
        {
            v_Ret = i_XCQL.execute(v_Params);
            
            // 及时释放资源
            if ( i_Args != null && i_Args.length > 1 && MethodReflect.isExtendImplement(v_Params ,Map.class) )
            {
                ((Map<? ,?>)v_Params).clear();
            }
            v_Params = null;
        }
        
        succeedLog(i_Anno ,v_Ret);
        
        if ( Void.TYPE == i_Method.getReturnType() )
        {
            return null;
        }
        else if ( Boolean.class == i_Method.getReturnType()
               || boolean.class == i_Method.getReturnType() )
        {
            return v_Ret;
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 获取并生成执行参数。
     * 
     * 当方法入参个数大于1时，需要整合成一个Map集合。
     * 
     * 详见 @Xcql.names
     * 详见 @Xparam
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Anno
     * @param i_Args
     * @return
     * @throws Exception
     */
    private Object getExecuteParams(XCQLAnnotation i_Anno ,Object [] i_Args) throws Exception
    {
        if ( Help.isNull(i_Anno.getXparams()) )
        {
            if ( i_Args == null || i_Args.length == 0 )
            {
                return null;
            }
            else
            {
                return i_Args[0];
            }
        }
        else
        {
            Map<String ,Object> v_Params = new HashMap<String ,Object>();
            for (int v_PIndex=0; v_PIndex<i_Anno.getXparams().size(); v_PIndex++)
            {
                XParamAnnotation v_XParamAnno = i_Anno.getXparams().get(v_PIndex);
                
                if ( v_XParamAnno.isNotNull() )
                {
                    if ( Help.isNull(i_Args[v_PIndex]) )
                    {
                        // 必要参数的非空验证。无须写异常描述。
                        throw new Exception("");
                    }
                    else
                    {
                        if ( !Help.isNull(v_XParamAnno.getNotNulls()) )
                        {
                            for (MethodReflect v_MethodReflect : v_XParamAnno.getNotNulls())
                            {
                                Object v_MethodRet = v_MethodReflect.invokeForInstance(i_Args[v_PIndex]);
                                
                                if ( Help.isNull(v_MethodRet) )
                                {
                                    // 必要参数属性的非空验证。无须写异常描述。
                                    throw new Exception("");
                                }
                            }
                        }
                    }
                }
                
                if ( !Help.isNull(v_XParamAnno.getName()) )
                {
                    if ( $ParamName_ToMap.equalsIgnoreCase(v_XParamAnno.getName()) )
                    {
                        if ( MethodReflect.isExtendImplement(i_Args[v_PIndex] ,SerializableClass.class) )
                        {
                            v_Params.putAll(((SerializableClass)i_Args[v_PIndex]).toMap());
                        }
                        else
                        {
                            try
                            {
                                v_Params.putAll(Help.toMap(i_Args[v_PIndex]));
                            }
                            catch (Exception exce)
                            {
                                $Logger.error(exce);
                                throw new RuntimeException(exce);
                            }
                        }
                    }
                    else
                    {
                        v_Params.put(v_XParamAnno.getName() ,i_Args[v_PIndex]);
                    }
                }
            }
            
            if ( Help.isNull(v_Params) && i_Args.length == 1 )
            {
                // 当未说明参数的占符名称，但要求参数为必须的时
                return i_Args[0];
            }
            else
            {
                return v_Params;
            }
        }
    }


    
    /**
     * 获取：代理接口的实现类。
     * 
     * 当相同XJava的ID同时在接口类及接口的实现类上标记时，此属性才生效。
     * 
     * 类似于JDK 9版本中的新特性：私有接口方法，不过此方法还可以public的。
     * 
     * 可实现如下新奇功能：
     *    接口中被@Xcql注释的方法用代理实现。
     *    同时，接口的实现类，也可实现其中未被@Xcql注释的方法。
     */
    public Object getXcqlInstace()
    {
        return xcqlInstace;
    }
    

    
    /**
     * 设置：代理接口的实现类。
     * 
     * 当相同XJava的ID同时在接口类及接口的实现类上标记时，此属性才生效。
     * 
     * 类似于JDK 9版本中的新特性：私有接口方法，不过此方法还可以public的。
     * 
     * 可实现如下新奇功能：
     *    接口中被@Xcql注释的方法用代理实现。
     *    同时，接口的实现类，也可实现其中未被@Xcql注释的方法。
     * 
     * @param xcqlInstace
     */
    public void setXcqlInstace(Object xcqlInstace)
    {
        this.xcqlInstace = xcqlInstace;
    }

}
