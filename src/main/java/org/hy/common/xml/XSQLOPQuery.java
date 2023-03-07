package org.hy.common.xml;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.db.DataSourceGroup;





/**
 * XSQL功能中Select语句的具体操作与实现。
 * 
 * 独立原因：从XSQL主类中分离的主要原因是：减少XSQL主类的代码量，方便维护。使XSQL主类向外提供统一的操作，本类重点关注实现。
 * 静态原因：用static方法的原因：不想再构建太多的类实例，减少内存负担
 * 接口选择：未使用接口的原因：本类的每个方法的首个入参都有一个XSQL类型，并且都是static方法
 * 
 * @author      ZhengWei(HY)
 * @createDate  2022-05-26
 * @version     v1.0
 *              v2.0  2023-03-07  添加：querySQLValue，常用于查询返回仅只一个字符串的场景。建议人：王雨墨
 */
public class XSQLOPQuery
{
    
    /**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Conn
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Conn
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段名称过滤)
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_FilterColNames   按输出字段名称过滤。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final List<String> i_FilterColNames)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNames);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段位置过滤)
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_FilterColNoArr   按输出字段位置过滤。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final int [] i_FilterColNoArr)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNoArr);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Object i_Obj)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_Conn
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Object i_Obj ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段名称过滤)
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_FilterColNames   按输出字段名称过滤。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Object i_Obj ,final List<String> i_FilterColNames)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNames);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段位置过滤)
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_FilterColNoArr   按输出字段位置过滤。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Object i_Obj ,final int [] i_FilterColNoArr)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNoArr);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,i_SQL ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final String i_SQL)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup());
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @return
     */
    private static XSQLData queryXSQLData_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            i_XSQL.fireAfterRule(v_Ret);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 常规SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @return
     */
    private static XSQLData queryXSQLData_Inner(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn)
    {
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn)
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            i_XSQL.fireAfterRule(v_Ret);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,null);
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。游标的分页查询（可通用于所有数据库）。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final int i_StartRow ,final int i_PagePerSize)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final int i_StartRow ,final int i_PagePerSize)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final Object i_Obj ,final int i_StartRow ,final int i_PagePerSize)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     *
     * @param i_SQL              常规SQL语句
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final String i_SQL ,final int i_StartRow ,final int i_PagePerSize)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,i_SQL ,v_DSG ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段名称过滤)
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNames   按输出字段名称过滤。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final String i_SQL ,final List<String> i_FilterColNames)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,i_SQL ,v_DSG ,i_FilterColNames);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段位置过滤)
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNoArr   按输出字段位置过滤。
     * @return
     */
    public static XSQLData queryXSQLData(final XSQL i_XSQL ,final String i_SQL ,final int [] i_FilterColNoArr)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            return XSQLOPQuery.queryXSQLData_Inner(i_XSQL ,i_SQL ,v_DSG ,i_FilterColNoArr);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     *
     * @param i_SQL              常规SQL语句
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    private static XSQLData queryXSQLData_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final int i_StartRow ,final int i_PagePerSize)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset ,i_StartRow ,i_PagePerSize);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            i_XSQL.fireAfterRule(v_Ret);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段名称过滤)
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNames   按输出字段名称过滤。
     * @return
     */
    private static XSQLData queryXSQLData_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final List<String> i_FilterColNames)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset ,i_FilterColNames);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            i_XSQL.fireAfterRule(v_Ret);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段位置过滤)
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-22
     * @version     v1.0
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNoArr   按输出字段位置过滤。
     * @return
     */
    private static XSQLData queryXSQLData_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final int [] i_FilterColNoArr)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getDatas(v_Resultset ,i_FilterColNoArr);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            i_XSQL.fireAfterRule(v_Ret);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Values       占位符SQL的填充集合。
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Values       占位符SQL的填充集合。
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final Connection i_Conn ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,v_SQL ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_Values ,i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Obj          占位符SQL的填充对象。
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final Object i_Obj ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Obj          占位符SQL的填充对象。
     * @param i_Conn         数据库连接池
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final Object i_Obj ,final Connection i_Conn ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,v_SQL ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_XSQLBigData  大数据处理接口
     * 
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,v_SQL ,v_DSG ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final Connection i_Conn ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,v_SQL ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_SQL          常规SQL语句
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,i_SQL ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_SQL          常规SQL语句
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public static Object queryBigData(final XSQL i_XSQL ,final String i_SQL ,final XSQLBigData i_XSQLBigData)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryBigData_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup() ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_SQL          常规SQL语句
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    private static Object queryBigData_Inner(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn ,final XSQLBigData i_XSQLBigData)
    {
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn)
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getBigDatas(v_Resultset ,i_XSQLBigData);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            return v_Ret.getDatas();
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,null);
        }
    }
    
    
    
    /**
     * 常规SQL的查询。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_SQL          常规SQL语句
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    private static Object queryBigData_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final XSQLBigData i_XSQLBigData)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLData v_Ret = i_XSQL.getResult().getBigDatas(v_Resultset ,i_XSQLBigData);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Ret.getRowCount());
            
            return v_Ret.getDatas();
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。-- 超级大结果集的SQL查询
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Values           占位符SQL的填充集合。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_FilterColNames   按输出字段名称过滤。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final List<String> i_FilterColNames)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNames);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_FilterColNoArr   按输出字段位置过滤。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final int [] i_FilterColNoArr)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNoArr);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。 -- 超级大结果集的SQL查询
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Obj              占位符SQL的填充对象。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final Object i_Obj)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_FilterColNames   按输出字段名称过滤。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final Object i_Obj ,final List<String> i_FilterColNames)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNames);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_FilterColNoArr   按输出字段位置过滤。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final Object i_Obj ,final int [] i_FilterColNoArr)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,v_SQL ,v_DSG ,i_FilterColNoArr);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。 -- 超级大结果集的SQL查询
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
     * 
     * 2. 通过 getBiggerNextData() 方法获取下一页数据
     * 
     * @param i_SQL              常规SQL语句
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final String i_SQL)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup());
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
     * 
     * 2. 通过 getBiggerNextData() 方法获取下一页数据
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNames   按输出字段名称过滤。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final String i_SQL ,final List<String> i_FilterColNames)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup() ,i_FilterColNames);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
     * 
     * 2. 通过 getBiggerNextData() 方法获取下一页数据
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNoArr   按输出字段位置过滤。
     */
    public static XSQLBigger queryBigger(final XSQL i_XSQL ,final String i_SQL ,final int [] i_FilterColNoArr)
    {
        i_XSQL.checkContent();
        
        boolean v_IsError = false;

        try
        {
            return XSQLOPQuery.queryBigger_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup() ,i_FilterColNoArr);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。 -- 超级大结果集的SQL查询
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
     * 
     * 2. 通过 getBiggerNextData() 方法获取下一页数据
     * 
     * @param i_SQL              常规SQL语句
     */
    private static XSQLBigger queryBigger_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_RowSize   = 0;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_RowSize   = XSQLOPQuery.querySQLCount(i_XSQL ,"SELECT COUNT(1) FROM ( " + i_SQL + " ) HY");
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLBigger v_Bigger = new XSQLBigger(i_XSQL ,v_Resultset ,v_RowSize ,(new Date()).getTime() - v_BeginTime);
            
            i_XSQL.getResult().getDatas(v_Bigger);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
            
            return v_Bigger;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            // i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);  超大结果集，不自动关闭数据库连接
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
     * 
     * 2. 通过 getBiggerNextData() 方法获取下一页数据
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNames   按输出字段名称过滤。
     */
    private static XSQLBigger queryBigger_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final List<String> i_FilterColNames)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_RowSize   = 0;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_RowSize   = XSQLOPQuery.querySQLCount_Inner(i_XSQL ,"SELECT COUNT(1) FROM ( " + i_SQL + " ) HY" ,i_DSG);
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLBigger v_Bigger = new XSQLBigger(i_XSQL ,v_Resultset ,v_RowSize ,(new Date()).getTime() - v_BeginTime);
            
            i_XSQL.getResult().getDatas(v_Bigger ,i_FilterColNames);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
            
            return v_Bigger;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            // i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);  超大结果集，不自动关闭数据库连接
        }
    }
    
    
    
    /**
     * 常规SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
     * 
     * 2. 通过 getBiggerNextData() 方法获取下一页数据
     * 
     * @param i_SQL              常规SQL语句
     * @param i_FilterColNoArr   按输出字段位置过滤。
     */
    private static XSQLBigger queryBigger_Inner(final XSQL i_XSQL ,final String i_SQL ,DataSourceGroup i_DSG ,final int [] i_FilterColNoArr)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_RowSize   = 0;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( i_XSQL.getResult() == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_RowSize   = XSQLOPQuery.querySQLCount(i_XSQL ,"SELECT COUNT(1) FROM ( " + i_SQL + " ) HY");
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            XSQLBigger v_Bigger = new XSQLBigger(i_XSQL ,v_Resultset ,v_RowSize ,(new Date()).getTime() - v_BeginTime);
            
            i_XSQL.getResult().getDatas(v_Bigger ,i_FilterColNoArr);
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
            
            return v_Bigger;
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            // i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);  超大结果集，不自动关闭数据库连接
        }
    }
    
    
    
    /**
     * 统计记录数据：占位符SQL的查询。
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_XSQL    查询对象
     * @param i_Values  占位符SQL的填充集合。
     * @return
     */
    public static long querySQLCount(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.querySQLCount_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 统计记录数据：占位符SQL的查询。
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_XSQL  查询对象
     * @param i_Obj   占位符SQL的填充对象。
     * @return
     */
    public static long querySQLCount(final XSQL i_XSQL ,final Object i_Obj)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.querySQLCount_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 查询记录总数
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * @param i_XSQL  查询对象
     * @return
     */
    public static long querySQLCount(final XSQL i_XSQL)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.querySQLCount_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 查询记录总数
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * @param i_XSQL  查询对象
     * @param i_SQL   查询SQL
     * @return
     */
    public static long querySQLCount(final XSQL i_XSQL ,final String i_SQL)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.querySQLCount_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 查询记录总数
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     *
     * @param i_XSQL  查询对象
     * @param i_SQL   查询SQL
     * @param i_DSG   查询数据源连接池组
     * @return
     */
    private static long querySQLCount_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_SQLCount  = 0;
        long       v_BeginTime = i_XSQL.request().getTime();

        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            if ( v_Resultset.next() )
            {
                v_SQLCount = v_Resultset.getLong(1);
            }
            
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
        
        
        return v_SQLCount;
    }
    
    
    
    /**
     * 查询返回第一行第一列上的数值。常用于查询返回一个字符串
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-03-07
     * @version     v1.0
     * 
     * @param i_XSQL    查询对象
     * @param i_Values  占位符SQL的填充集合。
     * @return
     * @throws Exception
     */
    public static Object querySQLValue(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPQuery.querySQLValue_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 查询返回第一行第一列上的数值。常用于查询返回一个字符串
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-03-07
     * @version     v1.0
     * 
     * @param i_XSQL  查询对象
     * @param i_Obj   占位符SQL的填充对象。
     * @return
     * @throws Exception
     */
    public static Object querySQLValue(final XSQL i_XSQL ,final Object i_Obj)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPQuery.querySQLValue_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 查询返回第一行第一列上的数值。常用于查询返回一个字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-03-07
     * @version     v1.0
     * 
     * @param i_XSQL  查询对象
     * @return
     */
    public static Object querySQLValue(final XSQL i_XSQL)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.querySQLValue_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    
    
    /**
     * 查询返回第一行第一列上的数值。常用于查询返回一个字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-03-07
     * @version     v1.0
     * 
     * @param i_XSQL  查询对象
     * @param i_SQL   查询SQL
     * @return
     */
    public static Object querySQLValue(final XSQL i_XSQL ,final String i_SQL)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPQuery.querySQLValue_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                i_XSQL.getTrigger().executes();
            }
        }
    }
    
    

    /**
     * 查询返回第一行第一列上的数值。常用于查询返回一个字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-03-07
     * @version     v1.0
     *
     * @param i_XSQL   查询对象
     * @param i_SQL    查询SQL
     * @param i_DSG    数据库连接池组
     * @return
     */
    private static Object querySQLValue_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        Object     v_SQLValue  = null;
        long       v_BeginTime = i_XSQL.request().getTime();

        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            if ( v_Resultset.next() )
            {
                v_SQLValue = v_Resultset.getObject(1);
            }
            
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
        
        
        return v_SQLValue;
    }
    
    
    
    /**
     * 本类不允许构建
     */
    private XSQLOPQuery()
    {
        
    }
    
}
