package org.hy.common.xml;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.db.DataSourceGroup;





/**
 * XSQL功能中DDL语句的具体操作与实现。
 * 
 * 独立原因：从XSQL主类中分离的主要原因是：减少XSQL主类的代码量，方便维护。使XSQL主类向外提供统一的操作，本类重点关注实现。
 * 静态原因：用static方法的原因：不想再构建太多的类实例，减少内存负担
 * 接口选择：未使用接口的原因：本类的每个方法的首个入参都有一个XSQL类型，并且都是static方法
 * 
 * @author      ZhengWei(HY)
 * @createDate  2022-05-25
 * @version     v1.0
 *              v2.0  2023-10-17  添加：是否附加触发额外参数的功能
 */
public class XSQLOPDDL
{
    
    /**
     * 占位符SQL的执行。-- 无填充值的
     * 
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,(Object) null);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final Map<String ,?> i_Values)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,i_Values);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            boolean v_Ret = XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,v_DSG);
            return XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,v_Ret ? 1 : 0) >= 1;
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的执行。
     * 
     * 1. 按对象 i_Values 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
     * 
     * @param i_Values           占位符SQL的填充对象。
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final Object i_Values)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,i_Values);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            boolean v_Ret = XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,v_DSG);
            return XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,v_Ret ? 1 : 0) >= 1;
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规SQL的执行。
     * 
     * @param i_SQL              常规SQL语句
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final String i_SQL)
    {
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,(Object) null);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规SQL的执行。
     * 
     * @param i_SQL              常规SQL语句
     * @return                   是否执行成功。
     */
    private static boolean execute_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        String     v_SQL       = i_SQL;
        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup[" + i_DSG.getXJavaID() + "] is not valid.");
            }
            
            if ( Help.isNull(v_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement();
            
            if ( i_XSQL.isAllowExecutesSplit() )
            {
                String [] v_SQLs = v_SQL.split(XSQL.$Executes_Split);
                for (int i=0; i<v_SQLs.length; i++)
                {
                    v_SQL = v_SQLs[i].trim();
                    v_Statement.execute(v_SQL);
                    i_XSQL.log(v_SQL);
                }
            }
            else
            {
                v_Statement.execute(v_SQL);
                i_XSQL.log(v_SQL);
            }
            
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
            
            return true;
        }
        catch (Exception exce)
        {
            XSQL.erroring(v_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            i_XSQL.closeDB(null ,v_Statement ,v_Conn);
        }
    }
    
    
    
    /**
     * 占位符SQL的执行。-- 无填充值的（内部不再关闭数据库连接）
     * 
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,(Object) null);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,i_Values);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Values           占位符SQL的填充对象。
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final Object i_Values ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,i_Values);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规SQL的执行。（内部不再关闭数据库连接）
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public static boolean execute(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn)
    {
        boolean             v_IsError       = false;
        String              v_ErrorInfo     = null;
        Map<String ,Object> v_TriggerParams = i_XSQL.executeBeforeForTrigger("execute" ,(Object) null);
        DataSourceGroup     v_DSG           = null;
        String              v_SQL           = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPDDL.execute_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
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
                    i_XSQL.getTrigger().executes(i_XSQL.executeAfterForTrigger(v_TriggerParams ,v_IsError?0L:1L ,v_ErrorInfo));
                }
            }
        }
    }
    
    
    
    /**
     * 常规SQL的执行。（内部不再关闭数据库连接）
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    private static boolean execute_Inner(final XSQL i_XSQL ,final String i_SQL ,final Connection i_Conn)
    {
        Statement v_Statement = null;
        long      v_BeginTime = i_XSQL.request().getTime();
        String    v_SQL       = i_SQL;
        
        try
        {
            if ( Help.isNull(v_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn )
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement();
            
            if ( i_XSQL.isAllowExecutesSplit() )
            {
                String [] v_SQLs = v_SQL.split(XSQL.$Executes_Split);
                for (int i=0; i<v_SQLs.length; i++)
                {
                    v_SQL = v_SQLs[i].trim();
                    v_Statement.execute(v_SQL);
                    i_XSQL.log(i_SQL);
                }
            }
            else
            {
                v_Statement.execute(v_SQL);
                i_XSQL.log(v_SQL);
            }
            
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
            
            return true;
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
     * 本类不允许构建
     */
    private XSQLOPDDL()
    {
        
    }
    
}
