package org.hy.common.xml;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Help;
import org.hy.common.MethodInfo;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.TablePartitionRID;
import org.hy.common.TablePartitionSet;
import org.hy.common.db.DBSQL;
import org.hy.common.xml.annotation.Xparam;
import org.hy.common.xml.annotation.Xsql;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.XSQLGroup;
import org.hy.common.xml.plugins.XSQLGroupResult;





/**
 * XSQL代理。
 * 
 * 详见 @see org.hy.common.xml.annotation.Xsql
 * 详见 @see org.hy.common.xml.annotation.XParam
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-14
 * @version     v1.0
 *              v1.1  2018-01-20  修复：@Xsql.names() 与 @Xparam.name() 在判定合计数量上的问题。
 *              v1.2  2018-01-25  添加：对查询SQL的记录行数功能的支持。
 *              v1.3  2018-01-27  添加：普通方式的，批量数据的更新功能。
 *                                添加：预解析方式，批量数据的更新功能。
 *              v1.4  2018-04-27  添加：returnOne注解属性支持Map、Set集合随机获取一个元素的功能。
 *              v1.5  2018-07-21  添加：支持分页模板自动封装的查询。建议人：李浩
 *              v1.6  2018-07-26  优化：及时释放资源，自动的GC太慢了。
 *              v1.7  2018-08-08  添加：@Xsql.execute()属性，支持多种类不同的SQL在同一XSQL中执行。
 *              v1.8  2020-06-24  添加：通过日志引擎规范输出日志
 */
public class XSQLProxy implements InvocationHandler ,Serializable
{

    private static final long   serialVersionUID = -4219520889151933542L;

    private static final Logger $Logger          = new Logger(XSQLProxy.class);
    
    /**
     * names()[x] 值为"ToMap"时，表示将方法入参转为Map集合后再putAll()整合后的大Map集合中。
     * 
     * @see org.hy.common.xml.annotation.Xsql.names()
     */
    public  static final String $ParamName_ToMap = "ToMap";
    
    /** 代理的接口类 */
    private final Class<?>                    xsqlInterface;
    
    /**
     * 代理接口的实现类。
     * 
     * 当相同XJava的ID同时在接口类及接口的实现类上标记时，此属性才生效。
     * 
     * 类似于JDK 9版本中的新特性：私有接口方法，不过此方法还可以public的。
     * 
     * 可实现如下新奇功能：
     *    接口中被@Xsql注释的方法用代理实现。
     *    同时，接口的实现类，也可实现其中未被@Xsql注释的方法。
     */
    private Object                            xsqlInstace;
    
    /** 被@Xsql注解的方法集合 */
    private final Map<MethodInfo ,XSQLAnnotation> methods;
    
    
    
    /**
     * 创建代理实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_XSQLInterface
     * @return
     */
    public static Object newProxy(Class<?> i_XSQLInterface)
    {
        return newProxy(i_XSQLInterface ,null);
    }
    
    
    
    /**
     * 创建代理实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_XSQLInterface
     * @param i_XSQLInstace
     * @return
     */
    public static Object newProxy(Class<?> i_XSQLInterface ,Object i_XSQLInstace)
    {
        XSQLProxy v_XSQLProxy = new XSQLProxy(i_XSQLInterface);
        
        v_XSQLProxy.setXsqlInstace(i_XSQLInstace);
        
        return Proxy.newProxyInstance(i_XSQLInterface.getClassLoader() ,new Class[]{i_XSQLInterface} ,v_XSQLProxy);
    }
    
    
    
    /**
     * 通过代理实例，获取XSQL代理实例（即本类实例）。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Proxy
     * @return
     */
    public static XSQLProxy getXSQLProxy(Object i_Proxy)
    {
        if ( i_Proxy instanceof Proxy )
        {
            InvocationHandler v_Invocation = Proxy.getInvocationHandler(i_Proxy);
            
            if ( v_Invocation instanceof XSQLProxy )
            {
                return (XSQLProxy)v_Invocation;
            }
        }
        
        return null;
    }
    
    
    
    public XSQLProxy(Class<?> i_XSQLInterface)
    {
        this.xsqlInterface     = i_XSQLInterface;
        this.methods           = new HashMap<MethodInfo ,XSQLAnnotation>();
        List<Method> v_Methods = MethodReflect.getAnnotationMethods(i_XSQLInterface ,Xsql.class);
        
        if ( !Help.isNull(v_Methods) )
        {
            for (Method v_Method : v_Methods)
            {
                try
                {
                    List<Xparam>   v_XParams = MethodReflect.getParameterAnnotations(v_Method ,Xparam.class);
                    XSQLAnnotation v_Anno    = new XSQLAnnotation(v_Method ,v_Method.getAnnotation(Xsql.class) ,v_XParams);
                    
                    this.methods.put(new MethodInfo(v_Method) ,v_Anno);
                    
                    // 方法入参个数大于1，应设置@Xsql(names)
                    if ( Help.isNull(v_Anno.getXparams()) && v_Method.getParameterTypes().length >= 2 )
                    {
                        this.errorLog(v_Method ,"Method parameter count >= 2 ,but @Xsql(names) count is 0." ,null);
                        return;
                    }
                    // @Xsql中设置的参数名称个数，应于方法入参个数数量相同
                    else if ( !Help.isNull(v_Anno.getXparams())
                           && v_Anno.getXparams().size() > v_Method.getParameterTypes().length )
                    {
                        this.errorLog(v_Method ,"@Xsql(names) count greater than method parameter count." ,null);
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
                XSQLAnnotation v_Anno = this.methods.get(new MethodInfo(i_Method));
                
                if ( v_Anno == null )
                {
                    return i_Method.invoke(this ,i_Args);
                }
                else
                {
                    v_Anno.setXid(Help.NVL(v_Anno.getXsql().id() ,Help.NVL(v_Anno.getXsql().value() ,i_Method.getName())));
                    Object v_XObject = XJava.getObject(v_Anno.getXid());
                    
                    if ( v_XObject == null )
                    {
                        return errorLog(i_Method ,"XID [" + v_Anno.getXid() + "] is not exists." ,null);
                    }
                    else if ( v_XObject instanceof XSQL )
                    {
                        return execute(i_Method ,v_Anno ,(XSQL)v_XObject ,i_Args);
                    }
                    else if ( v_XObject instanceof XSQLGroup )
                    {
                        return execute(i_Method ,v_Anno ,(XSQLGroup)v_XObject ,i_Args);
                    }
                    else
                    {
                        return errorLog(i_Method ,"XID [" + v_Anno.getXid() + "] java class type is not XSQL or XSQLGroup." ,null);
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
            $Logger.error("\nError: Call " + this.xsqlInterface.getName() + "." + i_Method.getName() + "：" + i_ErrorInfo + "\n" ,i_Exce);
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
     * 成功时输出的日志，当 @Xsql.log 有值时。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_Return
     */
    private void succeedLog(XSQLAnnotation i_Anno ,Object i_Return)
    {
        if ( Help.isNull(i_Anno.getXsql().log()) )
        {
            return;
        }
        
        if ( i_Return instanceof List )
        {
            $Logger.info(i_Anno.getXsql().log() + "，共 " + ((List<?>)i_Return).size() + " 个记录。");
        }
        else if ( i_Return instanceof Set )
        {
            $Logger.info(i_Anno.getXsql().log() + "，共 " + ((Set<?>)i_Return).size() + " 个记录。");
        }
        else if ( MethodReflect.isExtendImplement(i_Return ,PartitionMap.class) )
        {
            $Logger.info(i_Anno.getXsql().log() + "，共 " + ((PartitionMap<? ,?>)i_Return).size() + " 个分区，" + ((PartitionMap<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof TablePartitionRID )
        {
            $Logger.info(i_Anno.getXsql().log() + "，共 " + ((TablePartitionRID<? ,?>)i_Return).size() + " 个分区，" + ((TablePartitionRID<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof TablePartitionSet )
        {
            $Logger.info(i_Anno.getXsql().log() + "，共 " + ((TablePartitionSet<? ,?>)i_Return).size() + " 个分区，" + ((TablePartitionSet<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof Map )
        {
            $Logger.info(i_Anno.getXsql().log() + "，共 " + ((Map<? ,?>)i_Return).size() + " 个记录。");
        }
        else if ( i_Return instanceof Boolean )
        {
            $Logger.info(i_Anno.getXsql().log() + "，返回 " + i_Return + " 。");
        }
        else if ( i_Return instanceof XSQLGroupResult )
        {
            $Logger.info(i_Anno.getXsql().log() + "，执行成功，共影响 " + ((XSQLGroupResult)i_Return).getExecSumCount().getSumValue() + "行。");
        }
        else
        {
            $Logger.info(i_Anno.getXsql().log() + "，返回 " + i_Return.toString() + " 。");
        }
    }
    
    
    
    /**
     * 执行XSQL组
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQLGroup
     * @param i_Args
     * @return
     */
    private Object execute(Method i_Method ,XSQLAnnotation i_Anno ,XSQLGroup i_XSQLGroup ,Object [] i_Args)
    {
        if ( !Help.isNull(i_Anno.getXsql().updateCacheID()) )
        {
            return execute_XSQLGroup_UpdateCache(i_Method ,i_Anno ,i_XSQLGroup ,i_Args);
        }
        else if ( !Help.isNull(i_Anno.getXsql().cacheID()) )
        {
            return execute_XSQLGroup_Cache      (i_Method ,i_Anno ,i_XSQLGroup ,i_Args);
        }
        else
        {
            return execute_XSQLGroup_Normal     (i_Method ,i_Anno ,i_XSQLGroup ,i_Args);
        }
    }
    
    
    
    /**
     * 执行XSQL组（带同步锁的，用于高速缓存的更新功能）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQLGroup
     * @param i_Args
     * @return
     */
    private synchronized Object execute_XSQLGroup_UpdateCache(Method i_Method ,XSQLAnnotation i_Anno ,XSQLGroup i_XSQLGroup ,Object [] i_Args)
    {
        return execute_XSQLGroup_Normal(i_Method ,i_Anno ,i_XSQLGroup ,i_Args);
    }
    
    
    
    /**
     * 执行XSQL组（带同步锁的，用于高速缓存功能）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQLGroup
     * @param i_Args
     * @return
     */
    private synchronized Object execute_XSQLGroup_Cache(Method i_Method ,XSQLAnnotation i_Anno ,XSQLGroup i_XSQLGroup ,Object [] i_Args)
    {
        Object v_Ret = XJava.getObject(i_Anno.getXsql().cacheID());
        
        if ( v_Ret != null )
        {
            return v_Ret;
        }
        
        return execute_XSQLGroup_Normal(i_Method ,i_Anno ,i_XSQLGroup ,i_Args);
    }
    
    
    
    /**
     * 及时清理，释放资源
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-26
     * @version     v1.0
     *
     * @param io_Ret
     */
    private void clear(XSQLGroupResult io_Ret)
    {
        if ( io_Ret.getReturns() != null )
        {
            io_Ret.getReturns().clear();
            io_Ret.setReturns(null);
            io_Ret = null;
        }
    }
    
    
    
    /**
     * 执行XSQL组
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQLGroup
     * @param i_Args
     * @return
     */
    private Object execute_XSQLGroup_Normal(Method i_Method ,XSQLAnnotation i_Anno ,XSQLGroup i_XSQLGroup ,Object [] i_Args)
    {
        Object          v_Params = null;
        XSQLGroupResult v_Ret    = null;
        
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
            v_Ret = i_XSQLGroup.executes();
        }
        else
        {
            v_Ret = i_XSQLGroup.executes(v_Params);
        }
        
        // 及时释放资源
        if ( i_Args != null && i_Args.length > 1 && MethodReflect.isExtendImplement(v_Params ,Map.class) )
        {
            ((Map<? ,?>)v_Params).clear();
        }
        v_Params = null;
        
        // 异常时输出执行日志
        if ( !v_Ret.isSuccess() )
        {
            i_XSQLGroup.logReturn(v_Ret);
        }
        
        // 定义的方法无返回类型：void
        if ( Void.TYPE == i_Method.getReturnType() )
        {
            if ( v_Ret.isSuccess() )
            {
                if ( !Help.isNull(i_Anno.getXsql().returnID()) )
                {
                    cacheData(i_Anno ,v_Ret.getReturns().get(i_Anno.getXsql().returnID()) ,v_Ret);
                }
                else
                {
                    cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
                }
            }
            
            this.clear(v_Ret);
            v_Ret = null;
            
            return null;
        }
        // 返回指定ID的数据结果集
        else if ( !Help.isNull(i_Anno.getXsql().returnID()) )
        {
            try
            {
                if ( v_Ret.isSuccess() )
                {
                    cacheData(i_Anno ,v_Ret.getReturns().get(i_Anno.getXsql().returnID()) ,v_Ret);
                    return v_Ret.getReturns().get(i_Anno.getXsql().returnID());
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                this.clear(v_Ret);
                v_Ret = null;
            }
        }
        // 返回多个数据结果集
        else if ( Map.class == i_Method.getReturnType() )
        {
            if ( v_Ret.isSuccess() )
            {
                cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
                
                try
                {
                    return v_Ret.getReturns();
                }
                finally
                {
                    v_Ret.setReturns(null);
                    v_Ret = null;
                }
            }
            else
            {
                this.clear(v_Ret);
                v_Ret = null;
                
                return null;
            }
        }
        // 返回执行成功与否标记
        else if ( Boolean.class == i_Method.getReturnType()
               || boolean.class == i_Method.getReturnType() )
        {
            if ( v_Ret.isSuccess() )
            {
                cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
            }
            
            try
            {
                return v_Ret.isSuccess();
            }
            finally
            {
                this.clear(v_Ret);
                v_Ret = null;
            }
        }
        // 返回XSQL组的执行结果。可由外部控制提交、回滚等操作，及多个数据结果集的返回。
        else if ( XSQLGroupResult.class == i_Method.getReturnType() )
        {
            if ( v_Ret.isSuccess() )
            {
                cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
            }
            return v_Ret;
        }
        // 默认返回XSQL组的执行结果
        else if ( Object.class == i_Method.getReturnType() )
        {
            if ( v_Ret.isSuccess() )
            {
                cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
            }
            return v_Ret;
        }
        else
        {
            if ( v_Ret.isSuccess() )
            {
                cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
            }
            
            this.clear(v_Ret);
            v_Ret = null;
            
            return null;
        }
    }
    
    
    
    /**
     * 执行XSQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object execute(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        if (  i_Anno.getXsql().execute() )
        {
            return executeXSQL_Execute(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_SELECT )
        {
            return executeXSQL_Query(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_INSERT )
        {
            if ( XSQLData.class == i_Method.getReturnType() )
            {
                return executeXSQL_ExecuteInsert(i_Method ,i_Anno ,i_XSQL ,i_Args);
            }
            else if ( List.class == i_Method.getReturnType() )
            {
                XSQLData v_XData = executeXSQL_ExecuteInsert(i_Method ,i_Anno ,i_XSQL ,i_Args);
                return v_XData.getIdentitys();
            }
            else if ( i_Anno.getXsql().getID() )
            {
                XSQLData v_XData = executeXSQL_ExecuteInsert(i_Method ,i_Anno ,i_XSQL ,i_Args);
                if ( v_XData.getIdentitys().size() >= 1 )
                {
                    return v_XData.getIdentitys().get(0);
                }
                else
                {
                    return 0;
                }
            }
            else
            {
                return executeXSQL_ExecuteUpdate(i_Method ,i_Anno ,i_XSQL ,i_Args);
            }
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_UPDATE
               || i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_DELETE )
        {
            return executeXSQL_ExecuteUpdate(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_DDL )
        {
            return executeXSQL_Execute(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_CALL )
        {
            return executeXSQL_Call(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_UNKNOWN )
        {
            return executeXSQL_Execute(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        
        return null;
    }
    
    
    
    /**
     * 执行XSQL -- 查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object executeXSQL_Query(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        // 支持分页模板自动封装的查询  2018-07-21
        XSQL v_XSQL = i_XSQL;
        if ( i_Anno.getXsql().paging() )
        {
            v_XSQL = XSQL.queryPaging(v_XSQL);
            
            if ( v_XSQL == null )
            {
                v_XSQL = i_XSQL;
            }
        }
        
        if ( !Help.isNull(i_Anno.getXsql().updateCacheID()) )
        {
            return executeXSQL_Query_UpdateCache(i_Method ,i_Anno ,v_XSQL ,i_Args);
        }
        else if ( !Help.isNull(i_Anno.getXsql().cacheID()) )
        {
            return executeXSQL_Query_Cache      (i_Method ,i_Anno ,v_XSQL ,i_Args);
        }
        else
        {
            return executeXSQL_Query_Normal     (i_Method ,i_Anno ,v_XSQL ,i_Args);
        }
    }
    
    
    
    /**
     * 执行XSQL -- 查询（带同步锁的，用于高速缓存的更新功能）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private synchronized Object executeXSQL_Query_UpdateCache(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        return executeXSQL_Query_Normal(i_Method ,i_Anno ,i_XSQL ,i_Args);
    }
    
    
    
    /**
     * 执行XSQL -- 查询（带同步锁的，用于高速缓存功能）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-18
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private synchronized Object executeXSQL_Query_Cache(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        Object v_Ret = XJava.getObject(i_Anno.getXsql().cacheID());
        
        if ( v_Ret != null )
        {
            return v_Ret;
        }
        
        return executeXSQL_Query_Normal(i_Method ,i_Anno ,i_XSQL ,i_Args);
    }
    
    
    
    /**
     * 执行XSQL -- 查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *              v2.0  2018-04-27  添加：returnOne注解属性支持Map、Set集合随机获取一个元素的功能。
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object executeXSQL_Query_Normal(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
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
        
        // 2018-01-25 Add 如果方法返回值是整数，则按查询记录行数的SELECT Count(1) FROM ... 返回
        boolean v_IsQueryCount = false;
        boolean v_IsLong       = false;
        if ( i_Method.getReturnType() == Integer.class
          || i_Method.getReturnType() == int.class )
        {
            v_IsQueryCount = true;
        }
        else if ( i_Method.getReturnType() == Long.class
               || i_Method.getReturnType() == long.class )
        {
            v_IsQueryCount = true;
            v_IsLong       = true;
        }
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            if ( v_IsQueryCount )
            {
                long v_Count = i_XSQL.getSQLCount();
                return v_IsLong ? v_Count : Integer.parseInt("" + v_Count);
            }
            else
            {
                v_Ret = i_XSQL.query();
            }
        }
        else
        {
            if ( v_IsQueryCount )
            {
                long v_Count = i_XSQL.getSQLCount(v_Params);
                return v_IsLong ? v_Count : Integer.parseInt("" + v_Count);
            }
            else
            {
                v_Ret = i_XSQL.query(v_Params);
            }
            
            // 及时释放资源
            if ( i_Args != null && i_Args.length > 1 && MethodReflect.isExtendImplement(v_Params ,Map.class) )
            {
                ((Map<? ,?>)v_Params).clear();
            }
            v_Params = null;
        }
        
        if ( v_Ret != null )
        {
            if ( Void.TYPE == i_Method.getReturnType() )
            {
                cacheData(i_Anno ,v_Ret ,null);
                return null;
            }
            
            if ( i_Anno.getXsql().returnOne() )
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
            }
            
            cacheData(i_Anno ,v_Ret ,null);
            
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
    private void cacheData(XSQLAnnotation i_Anno ,Object v_CacheData ,XSQLGroupResult i_XSQLGroupResult)
    {
        // Object v_OldCacheData = null;
        
        if ( !Help.isNull(i_Anno.getXsql().updateCacheID()) )
        {
            // v_OldCacheData = XJava.getObject(i_Anno.getXsql().updateCacheID());
            
            XJava.putObject(i_Anno.getXsql().updateCacheID() ,v_CacheData);
            succeedLog(i_Anno ,v_CacheData);
        }
        else if ( !Help.isNull(i_Anno.getXsql().cacheID()) )
        {
            // v_OldCacheData = XJava.getObject(i_Anno.getXsql().cacheID());
            
            XJava.putObject(i_Anno.getXsql().cacheID()       ,v_CacheData);
            succeedLog(i_Anno ,v_CacheData);
        }
        else
        {
            succeedLog(i_Anno ,i_XSQLGroupResult == null ? v_CacheData : i_XSQLGroupResult);
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
     * 执行XSQL（专用于Insert语句）
     * 
     * 有能力返回自增长ID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-27
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private XSQLData executeXSQL_ExecuteInsert(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        Object   v_Params   = null;
        XSQLData v_RetXData = null;
        
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
            v_RetXData = i_XSQL.executeInsert();
        }
        else
        {
            if ( v_Params instanceof List )
            {
                // 批量数据的更新功能  2018-01-27
                if ( i_Anno.getXsql().batch() )
                {
                    v_RetXData = i_XSQL.executeInsertsPrepared((List<?>)v_Params);
                }
                else
                {
                    v_RetXData = i_XSQL.executeInserts((List<?>)v_Params);
                }
            }
            else
            {
                v_RetXData = i_XSQL.executeInsert(v_Params);
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
     * 执行XSQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object executeXSQL_ExecuteUpdate(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
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
            v_Ret = i_XSQL.executeUpdate();
        }
        else
        {
            if ( v_Params instanceof List )
            {
                // 批量数据的更新功能  2018-01-27
                if ( i_Anno.getXsql().batch() )
                {
                    v_Ret = i_XSQL.executeUpdatesPrepared((List<?>)v_Params);
                }
                else
                {
                    v_Ret = i_XSQL.executeUpdates((List<?>)v_Params);
                }
            }
            else
            {
                v_Ret = i_XSQL.executeUpdate(v_Params);
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
     * 执行XSQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object executeXSQL_Execute(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
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
            v_Ret = i_XSQL.execute();
        }
        else
        {
            v_Ret = i_XSQL.execute(v_Params);
            
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
     * 执行XSQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object executeXSQL_Call(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
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
            v_Ret = i_XSQL.call();
        }
        else
        {
            v_Ret = i_XSQL.call(v_Params);
            
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
            v_Ret = null;
            return null;
        }
        else
        {
            return v_Ret;
        }
    }
    
    
    
    /**
     * 获取并生成执行参数。
     * 
     * 当方法入参个数大于1时，需要整合成一个Map集合。
     * 
     * 详见 @Xsql.names
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
    private Object getExecuteParams(XSQLAnnotation i_Anno ,Object [] i_Args) throws Exception
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
     *    接口中被@Xsql注释的方法用代理实现。
     *    同时，接口的实现类，也可实现其中未被@Xsql注释的方法。
     */
    public Object getXsqlInstace()
    {
        return xsqlInstace;
    }
    

    
    /**
     * 设置：代理接口的实现类。
     * 
     * 当相同XJava的ID同时在接口类及接口的实现类上标记时，此属性才生效。
     * 
     * 类似于JDK 9版本中的新特性：私有接口方法，不过此方法还可以public的。
     * 
     * 可实现如下新奇功能：
     *    接口中被@Xsql注释的方法用代理实现。
     *    同时，接口的实现类，也可实现其中未被@Xsql注释的方法。
     * 
     * @param xsqlInstace
     */
    public void setXsqlInstace(Object xsqlInstace)
    {
        this.xsqlInstace = xsqlInstace;
    }

}
