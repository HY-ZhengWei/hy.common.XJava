package org.hy.common.xml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.log.Logger;





/**
 * XSQL功能中Insert语句的具体操作与实现。
 * 
 * 核心区别：它与executeUpdate方法的核心区别是：本类的所有方法将尝试返回【数据库级的自增ID】，方法返回类型为：XSQLData
 * 独立原因：从XSQL主类中分离的主要原因是：减少XSQL主类的代码量，方便维护。使XSQL主类向外提供统一的操作，本类重点关注实现。
 * 静态原因：用static方法的原因：不想再构建太多的类实例，减少内存负担
 * 接口选择：未使用接口的原因：本类的每个方法的首个入参都有一个XSQL类型，并且都是static方法
 * 
 * @author      ZhengWei(HY)
 * @createDate  2022-05-23
 * @version     v1.0
 *              v2.0  2023-04-20  添加：单行数据的批量操作（预解释执行模式）
 *              v2.1  2023-05-17  添加：预解释执行模式异常时，同样输出具体数值，而不是一大堆?问号
 *              v3.0  2023-10-17  添加：是否附加触发额外参数的功能
 */
public class XSQLOPInsert
{
    
    private final static Logger $Logger = new Logger(XSQLOPInsert.class);
    
    
    
    /**
     * 占位符SQL的Insert语句的执行。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_XSQL
     * @return        返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,(Object) null);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            XSQLData v_XSQLData = XSQLOPInsert.executeInsert_Inner(i_XSQL ,v_SQL ,v_DSG);
            v_IORowCount = v_XSQLData.getRowCount();
            return v_XSQLData;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes();
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert的写入操作。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,v_SQL ,v_DSG);
            v_IORowCount = v_Ret.getRowCount();
            XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,(int)v_Ret.getRowCount());
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句的执行。
     * 
     * 1. 按对象 i_Values 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert的写入操作。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充对象。
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final Object i_Values)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,v_SQL ,v_DSG);
            v_IORowCount = v_Ret.getRowCount();
            XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,(int)v_Ret.getRowCount());
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规Insert语句的执行。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_SQL              常规SQL语句
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final String i_SQL)
    {
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,(Object) null);
        long                v_IORowCount    = 0L;
        
        try
        {
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup());
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes();
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规Insert语句的执行。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_SQL              常规SQL语句
     * @return                   返回语句影响的记录数及自增长ID。
     */
    private static XSQLData executeInsert_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
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
            
            v_Conn      = i_XSQL.getConnection("XSQLOPInsert.executeInsert_Inner" ,i_DSG);
            v_Statement = v_Conn.createStatement();
            
            int           v_Count     = v_Statement.executeUpdate(i_SQL ,Statement.RETURN_GENERATED_KEYS);
            List<Integer> v_Identitys = null;
            i_XSQL.log(i_SQL);
            
            if ( v_Count >= 1 )
            {
                v_Identitys = XSQLOPInsert.readIdentitys(v_Statement);
            }
            
            Date v_EndTime = Date.getNowTime();
            long v_TimeLen = v_EndTime.getTime() - v_BeginTime;
            i_XSQL.success(v_EndTime ,v_TimeLen ,1 ,v_Count);
            
            return new XSQLData(v_Identitys ,v_Count ,1 ,v_TimeLen ,null);
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(null ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句的执行。 -- 无填充值的（内部不再关闭数据库连接）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,(Object) null);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,v_SQL ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes();
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,v_SQL ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Values 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充对象。
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final Object i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,v_SQL ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规Insert语句的执行。（内部不再关闭数据库连接）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsert(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn)
    {
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsert" ,(Object) null);
        long                v_IORowCount    = 0L;

        try
        {
            XSQLData v_Ret = XSQLOPInsert.executeInsert_Inner(i_XSQL ,i_SQL ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes();
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规Insert语句的执行。（内部不再关闭数据库连接）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数及自增长ID。
     */
    private static XSQLData executeInsert_Inner(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn)
    {
        Statement v_Statement = null;
        long      v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn )
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement();
            
            int           v_Count     = v_Statement.executeUpdate(i_SQL ,Statement.RETURN_GENERATED_KEYS);
            List<Integer> v_Identitys = null;
            i_XSQL.log(i_SQL);
            
            if ( v_Count >= 1 )
            {
                v_Identitys = XSQLOPInsert.readIdentitys(v_Statement);
            }
            
            Date v_EndTime = Date.getNowTime();
            long v_TimeLen = v_EndTime.getTime() - v_BeginTime;
            i_XSQL.success(v_EndTime ,v_TimeLen ,1 ,v_Count);
            
            return new XSQLData(v_Identitys ,v_Count ,1 ,v_TimeLen ,null);
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(null ,v_Statement ,null);
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public static XSQLData executeInserts(final XSQL i_XSQL ,final List<?> i_ObjList)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInserts" ,(Object) null);
        long                v_IORowCount    = 0L;

        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            XSQLData v_Ret = XSQLOPInsert.executeInserts_Inner(i_XSQL ,i_ObjList ,null);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executeUpdates(i_ObjList);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    public static XSQLData executeInserts(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInserts" ,(Object) null);
        long                v_IORowCount    = 0L;

        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            XSQLData v_Ret = XSQLOPInsert.executeInserts_Inner(i_XSQL ,i_ObjList ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executeUpdates(i_ObjList);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    private static XSQLData executeInserts_Inner(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        DataSourceGroup v_DSG        = null;
        Connection      v_Conn       = null;
        Statement       v_Statement  = null;
        boolean         v_AutoCommit = false;
        int             v_Ret        = 0;
        long            v_BeginTime  = i_XSQL.request().getTime();
        String          v_SQL        = null;
        int             v_SQLCount   = 0;
        List<Integer>   v_Identitys  = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + v_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(i_ObjList) )
            {
                throw new NullPointerException("Batch execute update List<Object> is null.");
            }
            
            if ( i_Conn == null )
            {
                v_Conn = i_XSQL.getConnection("XSQLOPInsert.executeInserts_Inner" ,v_DSG);
            }
            else
            {
                v_Conn = i_Conn;
            }
            v_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_Statement  = v_Conn.createStatement();
            v_Identitys  = new ArrayList<Integer>();
            
            if ( i_XSQL.getBatchCommit() <= 0 )
            {
                for (int i=0; i<i_ObjList.size(); i++)
                {
                    if ( i_ObjList.get(i) != null )
                    {
                        v_SQL       = i_XSQL.getContent().getSQL(i_ObjList.get(i) ,v_DSG);
                        v_SQLCount = v_Statement.executeUpdate(v_SQL ,Statement.RETURN_GENERATED_KEYS);
                        if ( v_SQLCount >= 1 )
                        {
                            v_Ret += v_SQLCount;
                            XSQLOPInsert.readIdentitys(v_Statement ,v_Identitys);
                        }
                        i_XSQL.log(v_SQL);
                    }
                }
                
                if ( i_Conn == null )
                {
                    v_Conn.commit();  // 它与i_Conn.commit();同作用
                }
            }
            else
            {
                boolean v_IsCommit = true;
                
                for (int i=0 ,v_EC=0; i<i_ObjList.size(); i++)
                {
                    if ( i_ObjList.get(i) != null )
                    {
                        v_SQL      = i_XSQL.getContent().getSQL(i_ObjList.get(i) ,v_DSG);
                        v_SQLCount = v_Statement.executeUpdate(v_SQL ,Statement.RETURN_GENERATED_KEYS);
                        if ( v_SQLCount >= 1 )
                        {
                            v_Ret += v_SQLCount;
                            XSQLOPInsert.readIdentitys(v_Statement ,v_Identitys);
                        }
                        i_XSQL.log(v_SQL);
                        v_EC++;
                        
                        if ( v_EC % i_XSQL.getBatchCommit() == 0 )
                        {
                            v_Conn.commit();
                            v_IsCommit = true;
                        }
                        else
                        {
                            v_IsCommit = false;
                        }
                    }
                }
                
                if ( !v_IsCommit )
                {
                    v_Conn.commit();
                }
            }
            
            Date v_EndTime = Date.getNowTime();
            long v_TimeLen = v_EndTime.getTime() - v_BeginTime;
            i_XSQL.success(v_EndTime ,v_TimeLen ,i_ObjList.size() ,v_Ret);
            return new XSQLData(v_Identitys ,v_Ret ,1 ,v_TimeLen ,null);
        }
        catch (Exception exce)
        {
            XSQL.erroring(v_SQL ,exce ,i_XSQL);
            
            try
            {
                if ( i_Conn == null && v_Conn != null )
                {
                    v_Conn.rollback();
                }
            }
            catch (Exception e)
            {
                $Logger.error(e);
            }
            
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( i_Conn == null )
            {
                try
                {
                    if ( v_Conn != null )
                    {
                        v_Conn.setAutoCommit(v_AutoCommit);
                    }
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                }
                
                i_XSQL.closeDB(null ,v_Statement ,v_Conn);
            }
            else
            {
                i_XSQL.closeDB(null ,v_Statement ,null);
            }
        }
    }
    
    
    
    /**
     * 一行数据的批量执行：占位符SQL的Insert语句的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert的写入操作。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsertPrepared(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsertPrepared" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsertPrepared_Inner(i_XSQL ,i_Values ,null);
            v_IORowCount = v_Ret.getRowCount();
            XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,(int)v_Ret.getRowCount());
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 一行数据的批量执行：占位符SQL的Insert语句的执行。
     * 
     * 1. 按对象 i_Values 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert的写入操作。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充对象。
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsertPrepared(final XSQL i_XSQL ,final Object i_Values)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsertPrepared" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsertPrepared_Inner(i_XSQL ,i_Values ,null);
            v_IORowCount = v_Ret.getRowCount();
            XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,(int)v_Ret.getRowCount());
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 一行数据的批量执行：占位符SQL的Insert语句的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert的写入操作。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsertPrepared(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsertPrepared" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsertPrepared_Inner(i_XSQL ,i_Values ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,(int)v_Ret.getRowCount());
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
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
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 一行数据的批量执行：占位符SQL的Insert语句的执行。
     * 
     * 1. 按对象 i_Values 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert的写入操作。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v3.0
     * 
     * @param i_Values           占位符SQL的填充对象。
     * @return                   返回语句影响的记录数及自增长ID。
     */
    public static XSQLData executeInsertPrepared(final XSQL i_XSQL ,final Object i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsertPrepared" ,i_Values);
        long                v_IORowCount    = 0L;
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            XSQLData v_Ret = XSQLOPInsert.executeInsertPrepared_Inner(i_XSQL ,i_Values ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,(int)v_Ret.getRowCount());
            return v_Ret;
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Values));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executes(i_Values);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 一行数据的批量执行：占位符SQL的Insert语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v1.0
     * 
     * @param i_ObjList          占位符SQL的填充对象。
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    @SuppressWarnings("unchecked")
    private static XSQLData executeInsertPrepared_Inner(final XSQL i_XSQL ,final Object i_Obj ,final Connection i_Conn)
    {
        DataSourceGroup   v_DSG         = null;
        Connection        v_Conn        = null;
        PreparedStatement v_PStatement  = null;
        boolean           v_AutoCommit  = false;
        int               v_Ret         = 0;
        int               v_CommitCount = 0;
        long              v_BeginTime   = i_XSQL.request().getTime();
        String            v_SQL         = null;
        List<Integer>     v_Identitys   = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + v_DSG.getXJavaID() + "] is not valid..");
            }
            
            if ( i_Obj == null )
            {
                throw new NullPointerException("Batch execute update Object is null.");
            }
            
            if ( i_Conn == null )
            {
                v_Conn = i_XSQL.getConnection("XSQLOPInsert.executeInsertPrepared_Inner" ,v_DSG);
            }
            else
            {
                v_Conn = i_Conn;
            }
            v_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_SQL        = i_XSQL.getContent().getPreparedSQL().getSQL();
            v_PStatement = v_Conn.prepareStatement(v_SQL ,Statement.RETURN_GENERATED_KEYS);
            v_Identitys  = new ArrayList<Integer>();
            
            if ( MethodReflect.isExtendImplement(i_Obj ,Map.class) )
            {
                int v_ParamIndex = 0;
                for (String v_PlaceHolder : i_XSQL.getContent().getPreparedSQL().getPlaceholders())
                {
                    Object v_Value = MethodReflect.getMapValue((Map<String ,?>)i_Obj ,v_PlaceHolder);
                    
                    XSQLOPUpdate.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_Value ,null);
                }
            }
            else
            {
                int v_ParamIndex = 0;
                for (String v_PlaceHolder : i_XSQL.getContent().getPreparedSQL().getPlaceholders())
                {
                    MethodReflect v_MethodReflect = new MethodReflect(i_Obj ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                    
                    XSQLOPUpdate.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_MethodReflect.invoke() ,v_MethodReflect.getReturnType());
                    
                    v_MethodReflect.clearDestroy();
                    v_MethodReflect = null;
                }
            }
            
            v_PStatement.addBatch();
            
            int [] v_CountArr = v_PStatement.executeBatch();
            XSQLOPInsert.readIdentitys(v_PStatement ,v_Identitys);
            
            if ( i_Conn == null )
            {
                v_Conn.commit();  // 它与i_Conn.commit();同作用
                v_CommitCount++;
            }
            
            for (int v_Count : v_CountArr)
            {
                if ( v_Count >= 1 )
                {
                    v_Ret += v_Count;
                }
                else if ( Statement.SUCCESS_NO_INFO == v_Count )
                {
                    // 执行成功了，但不知道影响的行数
                    v_Ret++;
                }
            }
            
            i_XSQL.log(v_SQL);
            Date v_EndTime = Date.getNowTime();
            long v_TimeLen = v_EndTime.getTime() - v_BeginTime;
            i_XSQL.success(v_EndTime ,v_TimeLen ,v_CommitCount ,v_Ret);
            
            return new XSQLData(v_Identitys ,v_Ret ,1 ,v_TimeLen ,null);
        }
        catch (Exception exce)
        {
            v_SQL = i_XSQL.getContent().getPreparedSQL().getSQL(i_Obj);
            XSQL.erroring(v_SQL ,exce ,i_XSQL);
            
            try
            {
                if ( i_Conn == null && v_Conn != null )
                {
                    v_Conn.rollback();
                }
            }
            catch (Exception e)
            {
                $Logger.error(e);
            }
            
            throw new RuntimeException(exce.getMessage() + "：" + v_SQL);
        }
        finally
        {
            if ( i_Conn == null )
            {
                try
                {
                    if ( v_Conn != null )
                    {
                        v_Conn.setAutoCommit(v_AutoCommit);
                    }
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                }
                
                i_XSQL.closeDB(null ,v_PStatement ,v_Conn);
            }
            else
            {
                i_XSQL.closeDB(null ,v_PStatement ,null);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     *              v2.0  2022-05-24  1. 添加：支持自增长ID的获取及返回
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public static XSQLData executeInsertsPrepared(final XSQL i_XSQL ,final List<?> i_ObjList)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsertsPrepared" ,(Object) null);
        long                v_IORowCount    = 0L;

        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            XSQLData v_Ret = XSQLOPInsert.executeInsertsPrepared_Inner(i_XSQL ,i_ObjList ,null);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executeUpdatesPrepared(i_ObjList);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     *              v2.0  2022-05-24  1. 添加：支持自增长ID的获取及返回
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    public static XSQLData executeInsertsPrepared(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("executeInsertsPrepared" ,(Object) null);
        long                v_IORowCount    = 0L;
        
        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            XSQLData v_Ret = XSQLOPInsert.executeInsertsPrepared_Inner(i_XSQL ,i_ObjList ,i_Conn);
            v_IORowCount = v_Ret.getRowCount();
            return v_Ret;
        }
        catch (NullPointerException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError   = true;
            v_ErrorInfo = Help.NVL(exce.getMessage() ,"E");
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        finally
        {
            if ( i_XSQL.isTriggers(v_IsError) )
            {
                if ( v_TriggerParams == null )
                {
                    i_XSQL.getTrigger().executeUpdatesPrepared(i_ObjList);
                }
                else
                {
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IORowCount ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     * @version     v2.0  2022-05-18  1. 修改：统计数据中的 "请求数" ，从原来的集合元素个数，调整为提交次数
     *              v3.0  2022-05-24  1. 添加：支持自增长ID的获取及返回
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    @SuppressWarnings("unchecked")
    private static XSQLData executeInsertsPrepared_Inner(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        DataSourceGroup   v_DSG         = null;
        Connection        v_Conn        = null;
        PreparedStatement v_PStatement  = null;
        boolean           v_AutoCommit  = false;
        int               v_Ret         = 0;
        int               v_CommitCount = 0;
        long              v_BeginTime   = i_XSQL.request().getTime();
        String            v_SQL         = null;
        List<Integer>     v_Identitys   = null;
        Object            v_Object      = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + v_DSG.getXJavaID() + "] is not valid..");
            }
            
            if ( Help.isNull(i_ObjList) )
            {
                throw new NullPointerException("Batch execute update List<Object> is null.");
            }
            
            if ( i_Conn == null )
            {
                v_Conn = i_XSQL.getConnection("XSQLOPInsert.executeInsertsPrepared_Inner" ,v_DSG);
            }
            else
            {
                v_Conn = i_Conn;
            }
            v_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_SQL        = i_XSQL.getContent().getPreparedSQL().getSQL();
            v_PStatement = v_Conn.prepareStatement(v_SQL ,Statement.RETURN_GENERATED_KEYS);
            v_Identitys  = new ArrayList<Integer>();
            
            if ( i_XSQL.getBatchCommit() <= 0 )
            {
                for (int i=0; i<i_ObjList.size(); i++)
                {
                    v_Object = i_ObjList.get(i);
                    if ( v_Object != null )
                    {
                        if ( MethodReflect.isExtendImplement(v_Object ,Map.class) )
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : i_XSQL.getContent().getPreparedSQL().getPlaceholders())
                            {
                                Object v_Value = MethodReflect.getMapValue((Map<String ,?>)v_Object ,v_PlaceHolder);
                                
                                XSQLOPUpdate.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_Value ,null);
                            }
                        }
                        else
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : i_XSQL.getContent().getPreparedSQL().getPlaceholders())
                            {
                                MethodReflect v_MethodReflect = new MethodReflect(v_Object ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                                
                                XSQLOPUpdate.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_MethodReflect.invoke() ,v_MethodReflect.getReturnType());
                                
                                v_MethodReflect.clearDestroy();
                                v_MethodReflect = null;
                            }
                        }
                        
                        v_PStatement.addBatch();
                    }
                }
                
                int [] v_CountArr = v_PStatement.executeBatch();
                XSQLOPInsert.readIdentitys(v_PStatement ,v_Identitys);
                
                if ( i_Conn == null )
                {
                    v_Conn.commit();  // 它与i_Conn.commit();同作用
                    v_CommitCount++;
                }
                
                for (int v_Count : v_CountArr)
                {
                    if ( v_Count >= 1 )
                    {
                        v_Ret += v_Count;
                    }
                    else if ( Statement.SUCCESS_NO_INFO == v_Count )
                    {
                        // 执行成功了，但不知道影响的行数
                        v_Ret++;
                    }
                }
            }
            else
            {
                boolean v_IsCommit = true;  // 2017-11-06  修正：当预处理 this.executeUpdatesPrepared_Inner() 执行的同时 batchCommit >= 1时，可能出现未"执行executeBatch"情况
                
                for (int i=0 ,v_EC=0; i<i_ObjList.size(); i++)
                {
                    v_Object = i_ObjList.get(i);
                    if ( v_Object != null )
                    {
                        if ( MethodReflect.isExtendImplement(v_Object ,Map.class) )
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : i_XSQL.getContent().getPreparedSQL().getPlaceholders())
                            {
                                Object v_Value = MethodReflect.getMapValue((Map<String ,?>)v_Object ,v_PlaceHolder);
                                
                                XSQLOPUpdate.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_Value ,null);
                            }
                        }
                        else
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : i_XSQL.getContent().getPreparedSQL().getPlaceholders())
                            {
                                MethodReflect v_MethodReflect = new MethodReflect(v_Object ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                                
                                XSQLOPUpdate.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_MethodReflect.invoke() ,v_MethodReflect.getReturnType());
                                
                                v_MethodReflect.clearDestroy();
                                v_MethodReflect = null;
                            }
                        }
                        
                        v_PStatement.addBatch();
                        v_EC++;
                        
                        if ( v_EC % i_XSQL.getBatchCommit() == 0 )
                        {
                            int [] v_CountArr = v_PStatement.executeBatch();
                            XSQLOPInsert.readIdentitys(v_PStatement ,v_Identitys);
                            v_Conn.commit();
                            v_CommitCount++;
                            
                            for (int v_Count : v_CountArr)
                            {
                                if ( v_Count >= 1 )
                                {
                                    v_Ret += v_Count;
                                }
                                else if ( Statement.SUCCESS_NO_INFO == v_Count )
                                {
                                    // 执行成功了，但不知道影响的行数
                                    v_Ret++;
                                }
                            }
                            
                            v_PStatement.clearBatch();
                            v_IsCommit = true;
                        }
                        else
                        {
                            v_IsCommit = false;
                        }
                    }
                }
                
                if ( !v_IsCommit )
                {
                    int [] v_CountArr = v_PStatement.executeBatch();
                    XSQLOPInsert.readIdentitys(v_PStatement ,v_Identitys);
                    v_Conn.commit();
                    v_CommitCount++;
                    
                    for (int v_Count : v_CountArr)
                    {
                        if ( v_Count >= 1 )
                        {
                            v_Ret += v_Count;
                        }
                        else if ( Statement.SUCCESS_NO_INFO == v_Count )
                        {
                            // 执行成功了，但不知道影响的行数
                            v_Ret++;
                        }
                    }
                }
            }
            
            i_XSQL.log(v_SQL);
            Date v_EndTime = Date.getNowTime();
            long v_TimeLen = v_EndTime.getTime() - v_BeginTime;
            i_XSQL.success(v_EndTime ,v_TimeLen ,v_CommitCount ,v_Ret);
            
            return new XSQLData(v_Identitys ,v_Ret ,1 ,v_TimeLen ,null);
        }
        catch (Exception exce)
        {
            v_SQL = i_XSQL.getContent().getPreparedSQL().getSQL(v_Object);
            XSQL.erroring(v_SQL ,exce ,i_XSQL);
            
            try
            {
                if ( i_Conn == null && v_Conn != null )
                {
                    v_Conn.rollback();
                }
            }
            catch (Exception e)
            {
                $Logger.error(e);
            }
            
            throw new RuntimeException(exce.getMessage() + "：" + v_SQL);
        }
        finally
        {
            if ( i_Conn == null )
            {
                try
                {
                    if ( v_Conn != null )
                    {
                        v_Conn.setAutoCommit(v_AutoCommit);
                    }
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                }
                
                i_XSQL.closeDB(null ,v_PStatement ,v_Conn);
            }
            else
            {
                i_XSQL.closeDB(null ,v_PStatement ,null);
            }
        }
    }
    
    
    
    /**
     * 读取自增长ID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Statement
     * @return
     * @throws SQLException
     */
    public static List<Integer> readIdentitys(Statement i_Statement) throws SQLException
    {
        return XSQLOPInsert.readIdentitys(i_Statement.getGeneratedKeys());
    }
    
    
    
    /**
     * 读取自增长ID
     * 
     * 一般用于批量处理数据的插入
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_Statement
     * @param io_Identitys
     * @return
     * @throws SQLException
     */
    public static List<Integer> readIdentitys(Statement i_Statement ,List<Integer> io_Identitys) throws SQLException
    {
        return XSQLOPInsert.readIdentitys(i_Statement.getGeneratedKeys() ,io_Identitys);
    }
    
    
    
    /**
     * 读取自增长ID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_ResultSet
     * @return
     * @throws SQLException
     */
    public static List<Integer> readIdentitys(ResultSet i_ResultSet) throws SQLException
    {
        return readIdentitys(i_ResultSet ,new ArrayList<Integer>());
    }
    
    
    
    /**
     * 读取自增长ID
     * 
     * 一般用于批量处理数据的插入
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-23
     * @version     v3.0
     * 
     * @param i_ResultSet
     * @param io_Identitys
     * @return
     * @throws SQLException
     */
    public static List<Integer> readIdentitys(ResultSet i_ResultSet ,List<Integer> io_Identitys) throws SQLException
    {
        while ( i_ResultSet.next() )
        {
            // 下标从1开始
            io_Identitys.add(i_ResultSet.getInt(1));
        }
        
        return io_Identitys;
    }
    
    
    
    /**
     * 本类不允许构建
     */
    private XSQLOPInsert()
    {
        
    }
    
}
