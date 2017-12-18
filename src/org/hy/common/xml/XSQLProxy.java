package org.hy.common.xml;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.TablePartitionRID;
import org.hy.common.TablePartitionSet;
import org.hy.common.db.DBSQL;
import org.hy.common.xml.annotation.Xsql;
import org.hy.common.xml.plugins.XSQLGroup;
import org.hy.common.xml.plugins.XSQLGroupResult;





/**
 * XSQL代理。
 * 
 * 详见 @see org.hy.common.xml.annotation.Xsql
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-14
 * @version     v1.0
 */
public class XSQLProxy implements InvocationHandler ,Serializable
{

    private static final long serialVersionUID   = -4219520889151933542L;
    
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
    private final Map<Method ,XSQLAnnotation> methods;
    
    
    
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
        this.methods           = new HashMap<Method ,XSQLAnnotation>();
        List<Method> v_Methods = MethodReflect.getAnnotationMethods(i_XSQLInterface ,Xsql.class);
        
        if ( !Help.isNull(v_Methods) )
        {
            for (Method v_Method : v_Methods)
            {
                this.methods.put(v_Method ,new XSQLAnnotation(v_Method.getAnnotation(Xsql.class)));
            }
        }
    }
    
    

    @Override
    public Object invoke(Object i_Proxy ,Method i_Method ,Object [] i_Args) throws Throwable
    {
        if ( Object.class.equals(i_Method.getDeclaringClass()) )
        {
            return i_Method.invoke(this ,i_Args);
        }
        else
        {
            XSQLAnnotation v_Anno = this.methods.get(i_Method);
            
            if ( v_Anno == null )
            {
                return i_Method.invoke(this ,i_Args);
            }
            else
            {
                // 第二次执行时，就不用重复检查了
                if ( v_Anno.isCheckOK() )
                {
                    Object v_XObject = XJava.getObject(v_Anno.getXid());
                    
                    if ( v_XObject instanceof XSQL )
                    {
                        return execute(i_Method ,v_Anno ,(XSQL)v_XObject ,i_Args);
                    }
                    else if ( v_XObject instanceof XSQLGroup )
                    {
                        return execute(i_Method ,v_Anno ,(XSQLGroup)v_XObject ,i_Args);
                    }
                    else
                    {
                        // 按理，此句不会被执行的。
                        return null;
                    }
                }
                else
                {
                    // 方法入参个数大于1，应设置@Xsql(paramNames)
                    if ( Help.isNull(v_Anno.getXsql().names()) && i_Method.getParameterTypes().length >= 2 )
                    {
                        return errorLog(i_Method ,"Method parameter count >= 2 ,but @Xsql(paramNames) count is 0.");
                    }
                    // @Xsql中设置的参数名称个数，应于方法入参个数数量相同
                    else if ( !Help.isNull(v_Anno.getXsql().names()) 
                           && v_Anno.getXsql().names().length > i_Method.getParameterTypes().length )
                    {
                        return errorLog(i_Method ,"@Xsql(paramNames) count greater than method parameter count.");
                    }
                    else
                    {
                        v_Anno.setXid(Help.NVL(v_Anno.getXsql().id() ,Help.NVL(v_Anno.getXsql().value() ,i_Method.getName())));
                        Object v_XObject = XJava.getObject(v_Anno.getXid());
                        
                        if ( v_XObject == null )
                        {
                            return errorLog(i_Method ,"XID [" + v_Anno.getXid() + "] is not exists.");
                        }
                        else if ( v_XObject instanceof XSQL )
                        {
                            v_Anno.setCheckOK(true);
                            return execute(i_Method ,v_Anno ,(XSQL)v_XObject ,i_Args);
                        }
                        else if ( v_XObject instanceof XSQLGroup )
                        {
                            v_Anno.setCheckOK(true);
                            return execute(i_Method ,v_Anno ,(XSQLGroup)v_XObject ,i_Args);
                        }
                        else
                        {
                            return errorLog(i_Method ,"XID [" + v_Anno.getXid() + "] java class type is not XSQL or XSQLGroup.");
                        }
                    }
                }
            }
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
     * @param i_ErrorInfo
     * @return
     */
    private Object errorLog(Method i_Method ,String i_ErrorInfo)
    {
        System.err.println("Call " + this.xsqlInterface.getName() + "." + i_Method.getName() + "：" + i_ErrorInfo);
        
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
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " + i_Anno.getXsql().log() + "，共 " + ((List<?>)i_Return).size() + " 个记录。");
        }
        else if ( i_Return instanceof Set )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，共 " + ((Set<?>)i_Return).size() + " 个记录。");
        }
        else if ( MethodReflect.isExtendImplement(i_Return ,PartitionMap.class) )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，共 " + ((PartitionMap<? ,?>)i_Return).size() + " 个分区，" + ((PartitionMap<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof TablePartitionRID )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，共 " + ((TablePartitionRID<? ,?>)i_Return).size() + " 个分区，" + ((TablePartitionRID<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof TablePartitionSet )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，共 " + ((TablePartitionSet<? ,?>)i_Return).size() + " 个分区，" + ((TablePartitionSet<? ,?>)i_Return).rowCount() + " 个记录。");
        }
        else if ( i_Return instanceof Map )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，共 " + ((Map<? ,?>)i_Return).size() + " 个记录。");
        }
        else if ( i_Return instanceof Boolean )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，返回 " + i_Return + " 。");
        }
        else if ( i_Return instanceof XSQLGroupResult )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，执行成功，共影响 " + ((XSQLGroupResult)i_Return).getExecSumCount().getSumValue() + "行。");
        }
        else
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + "  " +i_Anno.getXsql().log() + "，返回 " + i_Return.toString() + " 。");
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
        Object          v_Params = getExecuteParams(i_Anno.getXsql() ,i_Args);
        XSQLGroupResult v_Ret    = null;
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XSQLGroup.executes();
        }
        else
        {
            v_Ret = i_XSQLGroup.executes(v_Params);
        }
        
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
            return null;
        }
        // 返回指定ID的数据结果集
        else if ( !Help.isNull(i_Anno.getXsql().returnID()) )
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
        // 返回多个数据结果集
        else if ( Map.class == i_Method.getReturnType() )
        {
            if ( v_Ret.isSuccess() )
            {
                cacheData(i_Anno ,v_Ret.getReturns() ,v_Ret);
                return v_Ret.getReturns();
            }
            else
            {
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
            return v_Ret.isSuccess();
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
        if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_SELECT )
        {
            return executeXSQL_Query(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_INSERT
               || i_XSQL.getContentDB().getSQLType() == DBSQL.$DBSQL_TYPE_UPDATE
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
        if ( !Help.isNull(i_Anno.getXsql().updateCacheID()) )
        {
            return executeXSQL_Query_UpdateCache(i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else if ( !Help.isNull(i_Anno.getXsql().cacheID()) )
        {
            return executeXSQL_Query_Cache      (i_Method ,i_Anno ,i_XSQL ,i_Args);
        }
        else
        {
            return executeXSQL_Query_Normal     (i_Method ,i_Anno ,i_XSQL ,i_Args);
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
     *
     * @param i_Method
     * @param i_Anno
     * @param i_XSQL
     * @param i_Args
     * @return
     */
    private Object executeXSQL_Query_Normal(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        Object v_Ret    = null;
        Object v_Params = getExecuteParams(i_Anno.getXsql() ,i_Args);
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XSQL.query();
        }
        else
        {
            v_Ret = i_XSQL.query(v_Params);
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
        if ( !Help.isNull(i_Anno.getXsql().updateCacheID()) )
        {
            XJava.putObject(i_Anno.getXsql().updateCacheID() ,v_CacheData);
            succeedLog(i_Anno ,v_CacheData);
        }
        else if ( !Help.isNull(i_Anno.getXsql().cacheID()) )
        {
            XJava.putObject(i_Anno.getXsql().cacheID()       ,v_CacheData);
            succeedLog(i_Anno ,v_CacheData);
        }
        else
        {
            succeedLog(i_Anno ,i_XSQLGroupResult == null ? v_CacheData : i_XSQLGroupResult);
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
    private Object executeXSQL_ExecuteUpdate(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        Object v_Params = getExecuteParams(i_Anno.getXsql() ,i_Args);
        int    v_Ret    = -1;
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XSQL.executeUpdate();
        }
        else
        {
            v_Ret = i_XSQL.executeUpdate(v_Params);
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
    private Boolean executeXSQL_Execute(Method i_Method ,XSQLAnnotation i_Anno ,XSQL i_XSQL ,Object [] i_Args)
    {
        Object  v_Params = getExecuteParams(i_Anno.getXsql() ,i_Args);
        boolean v_Ret    = false;
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XSQL.execute();
        }
        else
        {
            v_Ret = i_XSQL.execute(v_Params);
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
        Object v_Params = getExecuteParams(i_Anno.getXsql() ,i_Args);
        Object v_Ret    = null;
        
        if ( i_Args == null || i_Args.length == 0 )
        {
            v_Ret = i_XSQL.call();
        }
        else
        {
            v_Ret = i_XSQL.call(v_Params);
        }
        
        succeedLog(i_Anno ,v_Ret);
        
        if ( Void.TYPE == i_Method.getReturnType() )
        {
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
     * 详见 @Xsql.paramNames
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-15
     * @version     v1.0
     *
     * @param i_Xsql
     * @param i_Args
     * @return
     */
    private Object getExecuteParams(Xsql i_Xsql ,Object [] i_Args)
    {
        if ( Help.isNull(i_Xsql.names()) )
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
            for (int v_PIndex=0; v_PIndex<i_Xsql.names().length; v_PIndex++)
            {
                String v_ParamName = i_Xsql.names()[v_PIndex];
                if ( !Help.isNull(v_ParamName) )
                {
                    if ( $ParamName_ToMap.equalsIgnoreCase(v_ParamName) )
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
                                throw new RuntimeException(exce);
                            }
                        }
                    }
                    else
                    {
                        v_Params.put(v_ParamName ,i_Args[v_PIndex]);
                    }
                }
            }
            
            return v_Params;
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
