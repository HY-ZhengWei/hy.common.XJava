package org.hy.common.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.ByteHelp;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.Return;
import org.hy.common.StaticReflect;
import org.hy.common.StringHelp;
import org.hy.common.app.Param;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.event.DefaultBLobEvent;

import oracle.sql.BLOB;
import oracle.sql.CLOB;





/**
 * XSQL功能中Insert\Update\Delete语句的具体操作与实现。
 * 
 * 独立原因：从XSQL主类中分离的主要原因是：减少XSQL主类的代码量，方便维护。使XSQL主类向外提供统一的操作，本类重点关注实现。
 * 静态原因：用static方法的原因：不想再构建太多的类实例，减少内存负担
 * 接口选择：未使用接口的原因：本类的每个方法的首个入参都有一个XSQL类型，并且都是static方法
 * 
 * @author      ZhengWei(HY)
 * @createDate  2022-05-24
 * @version     v1.0
 */
public class XSQLOPUpdate
{
    
    /**
     * 占位符SQL的Insert语句与Update语句的执行。 -- 无填充值的
     * 
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,v_SQL ,v_DSG);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final Map<String ,?> i_Values)
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
            int v_Ret = XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,v_SQL ,v_DSG);
            
            return XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Values ,v_Ret);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final Object i_Obj)
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
            int v_Ret = XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,v_SQL ,v_DSG);
            return XSQLOPUpdate.executeUpdate_AfterWriteLob(i_XSQL ,i_Obj ,v_Ret);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 常规Insert语句与Update语句的执行。
     * 
     * @param i_SQL              常规SQL语句
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final String i_SQL)
    {
        boolean v_IsError = false;

        try
        {
            return XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,i_SQL ,i_XSQL.getDataSourceGroup());
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 常规Insert语句与Update语句的执行。
     * 
     * @param i_SQL              常规SQL语句
     * @return                   返回语句影响的记录数。
     */
    private static int executeUpdate_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG)
    {
        Connection v_Conn      = null;
        Statement  v_Statement = null;
        long       v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement();
            int v_Count = v_Statement.executeUpdate(i_SQL);
            i_XSQL.log(i_SQL);
            
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Count);
            
            return v_Count;
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
     * 占位符SQL的Insert语句与Update语句的执行。 -- 无填充值的（内部不再关闭数据库连接）
     * 
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final Connection i_Conn)
    {
        i_XSQL.checkContent();
        
        boolean         v_IsError = false;
        DataSourceGroup v_DSG     = null;
        String          v_SQL     = null;

        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 占位符SQL的Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final Map<String ,?> i_Values ,Connection i_Conn)
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
            return XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 占位符SQL的Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final Object i_Obj ,Connection i_Conn)
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
            return XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,v_SQL ,i_Conn);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 常规Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdate(final XSQL i_XSQL ,final String i_SQL ,Connection i_Conn)
    {
        boolean v_IsError = false;

        try
        {
            return XSQLOPUpdate.executeUpdate_Inner(i_XSQL ,i_SQL ,i_Conn);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
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
     * 常规Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    private static int executeUpdate_Inner(final XSQL i_XSQL ,final String i_SQL ,Connection i_Conn)
    {
        Statement v_Statement = null;
        long      v_BeginTime = i_XSQL.request().getTime();
        
        try
        {
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn)
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement();
            int v_Count = v_Statement.executeUpdate(i_SQL);
            i_XSQL.log(i_SQL);
            
            Date v_EndTime = Date.getNowTime();
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,v_Count);
            
            return v_Count;
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
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdates(final XSQL i_XSQL ,final List<?> i_ObjList)
    {
        boolean v_IsError = false;

        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            return XSQLOPUpdate.executeUpdates_Inner(i_XSQL ,i_ObjList ,null);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
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
                i_XSQL.getTrigger().executeUpdates(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交i_XSQL.getBatchCommit()大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdates(final XSQL i_XSQL ,final List<?> i_ObjList ,Connection i_Conn)
    {
        boolean v_IsError = false;

        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            return XSQLOPUpdate.executeUpdates_Inner(i_XSQL ,i_ObjList ,i_Conn);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
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
                i_XSQL.getTrigger().executeUpdates(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交i_XSQL.getBatchCommit()大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    private static int executeUpdates_Inner(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        DataSourceGroup v_DSG        = null;
        Connection      v_Conn       = null;
        Statement       v_Statement  = null;
        boolean         v_AutoCommit = false;
        int             v_Ret        = 0;
        long            v_BeginTime  = i_XSQL.request().getTime();
        String          v_SQL        = null;
        int             v_SQLCount   = 0;
        
        try
        {
            if ( i_XSQL.getContent() == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_ObjList) )
            {
                throw new NullPointerException("Batch execute update List<Object> is null.");
            }
            
            v_Conn       = i_Conn == null ? i_XSQL.getConnection(v_DSG) : i_Conn;
            v_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_Statement  = v_Conn.createStatement();
            
            if ( i_XSQL.getBatchCommit() <= 0 )
            {
                for (int i=0; i<i_ObjList.size(); i++)
                {
                    if ( i_ObjList.get(i) != null )
                    {
                        v_SQL       = i_XSQL.getContent().getSQL(i_ObjList.get(i) ,v_DSG);
                        v_SQLCount = v_Statement.executeUpdate(v_SQL);
                        if ( v_SQLCount >= 1 )
                        {
                            v_Ret += v_SQLCount;
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
                        v_SQLCount = v_Statement.executeUpdate(v_SQL);
                        if ( v_SQLCount >= 1 )
                        {
                            v_Ret += v_SQLCount;
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
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,i_ObjList.size() ,v_Ret);
            return v_Ret;
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
                // Nothing.
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
                    // Nothing.
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
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
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
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdatesPrepared(final XSQL i_XSQL ,final List<?> i_ObjList)
    {
        boolean v_IsError = false;

        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            return XSQLOPUpdate.executeUpdatesPrepared_Inner(i_XSQL ,i_ObjList ,null);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
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
                i_XSQL.getTrigger().executeUpdatesPrepared(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
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
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交i_XSQL.getBatchCommit()大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdatesPrepared(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        boolean v_IsError = false;
        
        try
        {
            i_XSQL.fireBeforeRule(i_ObjList);
            return XSQLOPUpdate.executeUpdatesPrepared_Inner(i_XSQL ,i_ObjList ,i_Conn);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            v_IsError = true;
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_XSQL.getContent().getSQL(i_XSQL.getDataSourceGroup()) ,exce ,i_XSQL).setValuesList(i_ObjList));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
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
                i_XSQL.getTrigger().executeUpdatesPrepared(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
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
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交i_XSQL.getBatchCommit()大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    @SuppressWarnings("unchecked")
    private static int executeUpdatesPrepared_Inner(final XSQL i_XSQL ,final List<?> i_ObjList ,final Connection i_Conn)
    {
        DataSourceGroup   v_DSG         = null;
        Connection        v_Conn        = null;
        PreparedStatement v_PStatement  = null;
        boolean           v_AutoCommit  = false;
        int               v_Ret         = 0;
        int               v_CommitCount = 0;
        long              v_BeginTime   = i_XSQL.request().getTime();
        String            v_SQL         = null;
        
        try
        {
            if ( i_XSQL.getContent() == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            v_DSG = i_XSQL.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_ObjList) )
            {
                throw new NullPointerException("Batch execute update List<Object> is null.");
            }
            
            v_Conn       = i_Conn == null ? i_XSQL.getConnection(v_DSG) : i_Conn;
            v_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_SQL        = i_XSQL.getContent().getPreparedSQL().getSQL();
            v_PStatement = v_Conn.prepareStatement(v_SQL);
            
            if ( i_XSQL.getBatchCommit() <= 0 )
            {
                for (int i=0; i<i_ObjList.size(); i++)
                {
                    Object v_Object = i_ObjList.get(i);
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
                            }
                        }
                        
                        v_PStatement.addBatch();
                    }
                }
                
                int [] v_CountArr = v_PStatement.executeBatch();
                
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
                boolean v_IsCommit = true;  // 2017-11-06  修正：当预处理 i_XSQL.executeUpdatesPrepared_Inner() 执行的同时 batchCommit >= 1时，可能出现未"执行executeBatch"情况
                
                for (int i=0 ,v_EC=0; i<i_ObjList.size(); i++)
                {
                    Object v_Object = i_ObjList.get(i);
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
                            }
                        }
                        
                        v_PStatement.addBatch();
                        v_EC++;
                        
                        if ( v_EC % i_XSQL.getBatchCommit() == 0 )
                        {
                            int [] v_CountArr = v_PStatement.executeBatch();
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
            i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,v_CommitCount ,v_Ret);
            
            return v_Ret;
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
                // Nothing.
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
                    // Nothing.
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
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注: 1. 支持多种不同SQL语句的执行
     *     2. 支持不同类型的多个不同数据库的操作
     *     3. 如果要有顺序的执行，请java.util.LinkedHashMap
     * 
     * 重点注意：2014-12-04
     *         建议入参使用 TablePartition。为什么呢？
     *         原因是，Hashtable.put() 同样的key多次，只保存一份value。
     *         而 TablePartition.putRows() 会将同样key的多份不同的value整合在一起。
     *         特别适应于同一份Insert语句的SQL，执行多批数据的插入的情况
     * 
     * @param i_XSQLs            XSQL及占位符SQL的填充对象的集合。
     *                           1. List<?>集合元素可以是Object
     *                           2. List<?>集合元素可以是Map<String ,?>
     *                           3. List<?>更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    @SuppressWarnings({"unchecked" ,"rawtypes"})
    public static int executeUpdates(final PartitionMap<XSQL ,?> i_XSQLs)
    {
        return XSQLOPUpdate.executeUpdates((Map)i_XSQLs ,0);
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注: 1. 支持多种不同SQL语句的执行
     *     2. 支持不同类型的多个不同数据库的操作
     *     3. 如果要有顺序的执行，请java.util.LinkedHashMap
     * 
     * 重点注意：2014-12-04
     *         建议入参使用 TablePartition。为什么呢？
     *         原因是，Hashtable.put() 同样的key多次，只保存一份value。
     *         而 TablePartition.putRows() 会将同样key的多份不同的value整合在一起。
     *         特别适应于同一份Insert语句的SQL，执行多批数据的插入的情况
     * 
     * @param i_XSQLs            XSQL及占位符SQL的填充对象的集合。
     *                           1. List<?>集合元素可以是Object
     *                           2. List<?>集合元素可以是Map<String ,?>
     *                           3. List<?>更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdates(final Map<XSQL ,List<?>> i_XSQLs)
    {
        return XSQLOPUpdate.executeUpdates(i_XSQLs ,0);
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注: 1. 支持多种不同SQL语句的执行
     *     2. 支持不同类型的多个不同数据库的操作
     *     3. 如果要有顺序的执行，请java.util.LinkedHashMap
     *
     * 重点注意：2014-12-04
     *         建议入参使用 TablePartition<XSQL ,?>，（注意不是 TablePartition<XSQL ,List<?>>）
     *         为什么呢？
     *         原因是，Hashtable.put() 同样的key多次，只保存一份value。
     *         而 TablePartition.putRows() 会将同样key的多份不同的value整合在一起。
     *         特别适应于同一份Insert语句的SQL，执行多批数据的插入的情况
     * 
     * @param i_XSQLs            XSQL及占位符SQL的填充对象的集合。
     *                           1. List<?>集合元素可以是Object
     *                           2. List<?>集合元素可以是Map<String ,?>
     *                           3. List<?>更可以是上面两者的混合元素组成的集合
     * @param i_BatchCommit      批量执行 Insert、Update、Delete 时，达到提交的提交点
     * @return                   返回语句影响的记录数。
     */
    public static <R> int executeUpdates(final Map<XSQL ,List<?>> i_XSQLs ,final int i_BatchCommit)
    {
        Map<XSQL ,DataSourceGroup> v_DSGMap         = new HashMap<XSQL ,DataSourceGroup>();
        DataSourceGroup            v_DSG            = null;
        List<Connection>           v_Conns          = new ArrayList<Connection>();
        XSQL                       v_XSQL           = null;
        XSQL                       v_XSQLError      = null;
        Object                     v_ParamObj       = null;
        int                        v_Ret            = 0;
        long                       v_TimeLenSum     = 0;                               // 每段SQL用时时长的累计值。此值一般情况下小于 v_TimeLenTotal
        long                       v_TimeLenTotal   = 0;                               // 总体用时时长
        long                       v_BeginTimeTotal = Date.getNowTime().getTime();     // 总体开始时间
        long                       v_BeginTime      = 0;
        Date                       v_EndTime        = null;
        List<Return<XSQL>>         v_Totals         = null;
        Return<XSQL>               v_TotalCache     = null;
        
        try
        {
            if ( Help.isNull(i_XSQLs) )
            {
                throw new NullPointerException("XSQLs is null.");
            }
            
            for (XSQL v_XSQLTemp : i_XSQLs.keySet())
            {
                if ( v_XSQLTemp.getContent() == null )
                {
                    throw new NullPointerException("Content is null of XSQL.");
                }
                
                if ( Help.isNull(i_XSQLs.get(v_XSQLTemp)) )
                {
                    throw new NullPointerException("Batch execute update List<Object> is null.");
                }
                
                v_DSG = v_XSQLTemp.getDataSourceGroup();
                if ( !v_DSG.isValid() )
                {
                    throw new RuntimeException("DataSourceGroup is not valid.");
                }
                v_DSGMap.put(v_XSQLTemp ,v_DSG);
            }
            
            v_Totals = new ArrayList<Return<XSQL>>(i_XSQLs.size());
            v_XSQL   = i_XSQLs.keySet().iterator().next();
            
            if ( i_BatchCommit <= 0 )
            {
                for (XSQL v_XSQLTemp : i_XSQLs.keySet())
                {
                    Connection v_Conn = v_XSQL.getConnection(v_DSG);
                    v_Conn.setAutoCommit(false);
                    v_Conns.add(v_Conn);
                    
                    v_XSQLError = v_XSQLTemp;
                    List<?> v_ObjList = i_XSQLs.get(v_XSQLTemp);
                    
                    for (int i=0; i<v_ObjList.size(); i++)
                    {
                        v_ParamObj = v_ObjList.get(i);
                        
                        if ( v_ParamObj != null )
                        {
                            v_BeginTime = Date.getNowTime().getTime();
                            v_Ret += v_XSQLTemp.executeUpdate(v_ParamObj ,v_Conn);
                            
                            v_TotalCache = new Return<XSQL>();
                            v_TotalCache.paramInt((int)(Date.getNowTime().getTime() - v_BeginTime));
                            v_Totals.add(v_TotalCache.paramObj(v_XSQLTemp));
                            
                            v_TimeLenSum += v_TotalCache.paramInt;
                        }
                    }
                }
                
                XSQL.commits(1 ,v_Conns);
            }
            else
            {
                boolean v_IsCommit = true;
                
                for (XSQL v_XSQLTemp : i_XSQLs.keySet())
                {
                    Connection v_Conn = v_XSQL.getConnection(v_DSG);
                    v_Conn.setAutoCommit(false);
                    v_Conns.add(v_Conn);
                    
                    v_XSQLError = v_XSQLTemp;
                    List<?> v_ObjList = i_XSQLs.get(v_XSQLTemp);
                    
                    for (int i=0; i<v_ObjList.size(); i++)
                    {
                        v_ParamObj = v_ObjList.get(i);
                        
                        if ( v_ParamObj != null )
                        {
                            v_BeginTime = Date.getNowTime().getTime();
                            v_Ret += v_XSQLTemp.executeUpdate(v_ParamObj ,v_Conn);
                            
                            v_TotalCache = new Return<XSQL>();
                            v_TotalCache.paramInt((int)(Date.getNowTime().getTime() - v_BeginTime));
                            v_Totals.add(v_TotalCache.paramObj(v_XSQLTemp));
                            
                            v_TimeLenSum += v_TotalCache.paramInt;
                            
                            if ( i_BatchCommit > 0 && v_Totals.size() % i_BatchCommit == 0 )
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
                        v_IsCommit = true;
                    }
                }
            }
            
            // 计算出总用时与每段SQL累计用时之差，后平摊到每段SQL的用时时长上。
            v_EndTime      = new Date();
            v_TimeLenTotal = v_EndTime.getTime() - v_BeginTimeTotal;
            double v_Per   = Help.division(v_TimeLenTotal - v_TimeLenSum ,v_Totals.size());
            
            // 每个XSQL已自行统计了时间。但当操作数据库成功后，再将平摊的耗时记录在每个XSQL上
            for (Return<XSQL> v_Total : v_Totals)
            {
                v_Total.paramObj.success(v_EndTime ,v_Per ,1 ,0);
            }
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            String v_SQLError = "";
            if ( v_XSQLError != null && v_XSQLError.getContent() != null )
            {
                v_SQLError = v_XSQLError.getContent().getSQL(v_ParamObj ,v_XSQLError.getDataSourceGroup());
            }
            XSQL.erroring(v_SQLError ,exce ,v_XSQLError);
            
            if ( !Help.isNull(v_Conns) )
            {
                XSQL.rollbacks(v_Conns);
            }
            
            // 计算出总用时与每段SQL累计用时之差，后平摊到每段SQL的用时时长上。
            v_EndTime      = new Date();
            v_TimeLenTotal = v_EndTime.getTime() - v_BeginTimeTotal;
            double v_Per   = Help.division(v_TimeLenTotal - v_TimeLenSum ,v_Totals.size());
            
            // 每个XSQL已自行统计了时间。但当操作数据库成功后，再将平摊的耗时记录在每个XSQL上
            for (int i=0; i<v_Totals.size() ; i++)
            {
                Return<XSQL> v_Total = v_Totals.get(i);
                v_Total.paramObj.success(v_EndTime ,v_Per ,1 ,0);
            }
            
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            XSQL.setAutoCommits(v_Conns ,true);
            XSQL.closeDB(v_Conns);
        }
    }
    
    
    
    /**
     * 预解释SQL的方式填充数值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *              v2.0  2019-12-25  添加：i_ValueClass
     *
     * @param io_PStatement  预解释SQL的对象
     * @param i_ParamIndex   填充数值的位置。下标从1开始
     * @param i_Value        填充的数值
     * @param i_ValueClass   填充数据的类型。可用于填充数据为NULL的判定情况
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    protected static void preparedStatementSetValue(PreparedStatement io_PStatement ,int i_ParamIndex ,Object i_Value ,Class<?> i_ValueClass) throws SQLException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Class<?> v_Class = null;
        if ( i_Value == null )
        {
            if ( i_ValueClass == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.VARCHAR);
                return;
            }
            else
            {
                v_Class = i_ValueClass;
            }
        }
        else
        {
            v_Class = i_Value.getClass();
        }
        
        if ( v_Class == String.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.VARCHAR);
            }
            else
            {
                io_PStatement.setString(i_ParamIndex ,(String)i_Value);
            }
        }
        else if ( v_Class == Integer.class || v_Class == int.class)
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.INTEGER);
            }
            else
            {
                io_PStatement.setInt(i_ParamIndex ,((Integer)i_Value).intValue());
            }
        }
        else if ( v_Class == Double.class || v_Class == double.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.DECIMAL);
            }
            else
            {
                io_PStatement.setDouble(i_ParamIndex ,((Double)i_Value).doubleValue());
            }
        }
        else if ( v_Class == Float.class || v_Class == float.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.FLOAT);
            }
            else
            {
                io_PStatement.setFloat(i_ParamIndex ,((Float)i_Value).floatValue());
            }
        }
        else if ( v_Class == Boolean.class || v_Class == boolean.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.BOOLEAN);
            }
            else
            {
                io_PStatement.setBoolean(i_ParamIndex ,((Boolean)i_Value).booleanValue());
            }
        }
        else if ( v_Class == Long.class || v_Class == long.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.LONGVARCHAR);
            }
            else
            {
                io_PStatement.setLong(i_ParamIndex ,((Long)i_Value).longValue());
            }
        }
        else if ( v_Class == Date.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.DATE);
            }
            else
            {
                // getSQLDate()的精度只到天，未到时分秒，所以换成getSQLTimestamp()方法  ZhengWei(HY) Edit 2018-05-11
                io_PStatement.setTimestamp(i_ParamIndex ,((Date)i_Value).getSQLTimestamp());
            }
        }
        else if ( v_Class == java.util.Date.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.DATE);
            }
            else
            {
                // getSQLDate()的精度只到天，未到时分秒，所以换成getSQLTimestamp()方法  ZhengWei(HY) Edit 2018-05-11
                io_PStatement.setTimestamp(i_ParamIndex ,(new Date((java.util.Date)i_Value)).getSQLTimestamp());
            }
        }
        // 添加对数据库时间的转换 Add ZhengWei(HY) 2018-05-15
        else if ( v_Class == Timestamp.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.TIMESTAMP);
            }
            else
            {
                io_PStatement.setTimestamp(i_ParamIndex ,(Timestamp)i_Value);
            }
        }
        else if ( v_Class == BigDecimal.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.DECIMAL);
            }
            else
            {
                io_PStatement.setBigDecimal(i_ParamIndex ,(BigDecimal)i_Value);
            }
        }
        else if ( v_Class == MethodReflect.class )
        {
            XSQLOPUpdate.preparedStatementSetValue(io_PStatement ,i_ParamIndex ,((MethodReflect)i_Value).invoke() ,((MethodReflect)i_Value).getReturnType());
        }
        else if ( MethodReflect.isExtendImplement(v_Class ,Enum.class) )
        {
            @SuppressWarnings("unchecked")
            Enum<?> [] v_EnumValues = StaticReflect.getEnums((Class<? extends Enum<?>>) v_Class);
            String     v_Value      = i_Value.toString();
            boolean    v_Continue   = true;
            
            // ZhengWei(HY) Add 2018-05-08  支持枚举toString()的匹配
            for (Enum<?> v_Enum : v_EnumValues)
            {
                if ( v_Value.equalsIgnoreCase(v_Enum.toString()) )
                {
                    io_PStatement.setInt(i_ParamIndex ,v_Enum.ordinal());
                    v_Continue = false;
                }
            }
            
            if ( v_Continue )
            {
                // ZhengWei(HY) Add 2018-05-08  支持枚举名称的匹配
                for (Enum<?> v_Enum : v_EnumValues)
                {
                    if ( v_Value.equalsIgnoreCase(v_Enum.name()) )
                    {
                        io_PStatement.setInt(i_ParamIndex ,v_Enum.ordinal());
                        v_Continue = false;
                    }
                }
            }
            
            if ( v_Continue )
            {
                // 尝试用枚举值匹配
                if ( Help.isNumber(v_Value) )
                {
                    int v_IntValue = Integer.parseInt(v_Value.trim());
                    if ( 0 <= v_IntValue && v_IntValue < v_EnumValues.length )
                    {
                        io_PStatement.setInt(i_ParamIndex ,v_EnumValues[v_IntValue].ordinal());
                        v_Continue = false;
                    }
                }
            }
            
            if ( v_Continue )
            {
                throw new java.lang.IndexOutOfBoundsException("Enum [" + v_Class.getName() + "] is not find Value[" + i_Value.toString() + "].");
            }
        }
        else if ( v_Class == Short.class || v_Class == short.class)
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.TINYINT);
            }
            else
            {
                io_PStatement.setShort(i_ParamIndex ,((Short)i_Value).shortValue());
            }
        }
        else if ( v_Class == Byte.class || v_Class == byte.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.BINARY);
            }
            else
            {
                io_PStatement.setShort(i_ParamIndex ,((Byte)i_Value).byteValue());
            }
        }
        else if ( v_Class == Character.class || v_Class == char.class )
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.CHAR);
            }
            else
            {
                io_PStatement.setString(i_ParamIndex ,i_Value.toString());
            }
        }
        else
        {
            if ( i_Value == null )
            {
                io_PStatement.setNull(i_ParamIndex ,java.sql.Types.VARCHAR);
            }
            else
            {
                io_PStatement.setString(i_ParamIndex ,i_Value.toString());
            }
        }
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行 -- 无填充值的
     * 
     * 1. CLob类型必须在SELECT语句的第一个输出字段的位置
     * 2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateCLob(final XSQL i_XSQL ,final String ... i_ClobTexts)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPUpdate.executeUpdateCLobSQL_Inner(i_XSQL ,v_SQL ,v_DSG ,i_ClobTexts);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. CLob类型必须在SELECT语句的第一个输出字段的位置
     * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateCLob(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final String ... i_ClobTexts)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPUpdate.executeUpdateCLobSQL_Inner(i_XSQL ,v_SQL ,v_DSG ,i_ClobTexts);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. CLob类型必须在SELECT语句的第一个输出字段的位置
     * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateCLob(final XSQL i_XSQL ,final Object i_Obj ,final String ... i_ClobTexts)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPUpdate.executeUpdateCLobSQL_Inner(i_XSQL ,v_SQL ,v_DSG ,i_ClobTexts);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 写入Clob方法1（本类实现的方法）：
     *    Oracle中Clob数据类型是不能够直接插入的，但是可以通过流的形式对Clob类型数据写入或者读取。
     *    INSERT INTO tablename
     *               (
     *                id
     *               ,clobColumn
     *               )
     *        VALUES (
     *                1
     *               ,EMPTY_CLOB()
     *               );
     * 
     *    SELECT  clobColumn
     *      FROM  tablename
     *     WHERE  id = 1
     *       FOR  UPDATE
     * 
     * 写入Clob方法2：
     *    通过TO_CLOB将字符转为clob类型，每个转换的参数不能超过2000个字符，多个部分通过连接符 || 连接
     *    INSERT INTO tablename
     *               (
     *                varcharColumn
     *               ,clobColumn
     *               )
     *        VALUES (
     *                'string part'
     *               ,TO_CLOB('clob chars part1 ') || TO_CLOB('clob chars part2')
     *               );
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_SQL              带有Clob字段的查询SQL语句
     *                           1. CLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     *                           3. 只操作首条数据记录
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateCLobSQL(final XSQL i_XSQL ,final String i_SQL ,final String ... i_ClobTexts)
    {
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            return XSQLOPUpdate.executeUpdateCLobSQL_Inner(i_XSQL ,v_SQL ,v_DSG ,i_ClobTexts);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 写入Clob方法1（本类实现的方法）：
     *    Oracle中Clob数据类型是不能够直接插入的，但是可以通过流的形式对Clob类型数据写入或者读取。
     *    INSERT INTO tablename
     *               (
     *                id
     *               ,clobColumn
     *               )
     *        VALUES (
     *                1
     *               ,EMPTY_CLOB()
     *               );
     * 
     *    SELECT  clobColumn
     *      FROM  tablename
     *     WHERE  id = 1
     *       FOR  UPDATE
     * 
     * 写入Clob方法2：
     *    通过TO_CLOB将字符转为clob类型，每个转换的参数不能超过2000个字符，多个部分通过连接符 || 连接
     *    INSERT INTO tablename
     *               (
     *                varcharColumn
     *               ,clobColumn
     *               )
     *        VALUES (
     *                'string part'
     *               ,TO_CLOB('clob chars part1 ') || TO_CLOB('clob chars part2')
     *               );
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_SQL              带有Clob字段的查询SQL语句
     *                           1. CLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     *                           3. 只操作首条数据记录
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    @SuppressWarnings("deprecation")
    private static int executeUpdateCLobSQL_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final String ... i_ClobTexts)
    {
        Connection v_Conn           = null;
        Statement  v_Statement      = null;
        ResultSet  v_ResultSet      = null;
        boolean    v_Old_AutoCommit = false; // 保存原始状态，使用完后，再恢复原状
        int        v_ExecResult     = -1;    // 执行结果。0:没有执行  1:需要Commit  -1:需要RollBack
        Writer     v_Output         = null;
        long       v_BeginTime      = i_XSQL.request().getTime();
        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( Help.isNull(i_ClobTexts) )
            {
                throw new NullPointerException("ClobTexts is null of XSQL.");
            }
            
            
            v_Conn           = i_XSQL.getConnection(i_DSG);
            v_Old_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_Statement      = v_Conn.createStatement();
            v_ResultSet      = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            if ( v_ResultSet.next() )
            {
                int v_ColCount  = v_ResultSet.getMetaData().getColumnCount();
                int v_CLobIndex = 0;
                for (int v_ColIndex=1; v_ColIndex<=v_ColCount; v_ColIndex++)
                {
                    try
                    {
                        int v_ColType = v_ResultSet.getMetaData().getColumnType(v_ColIndex);
                        if ( Types.CLOB  == v_ColType
                          || Types.NCLOB == v_ColType )
                        {
                            // 获取数据流
                            CLOB v_CLob = (CLOB)v_ResultSet.getClob(v_ColCount);
                            v_Output = v_CLob.getCharacterOutputStream();
                        }
                        else
                        {
                            continue;
                        }
                        
                        v_Output.write(Help.NVL(i_ClobTexts[v_CLobIndex++]));
                        v_Output.close();
                    }
                    catch (Exception exce)
                    {
                        XSQL.erroring(i_SQL ,exce ,i_XSQL);
                    }
                    finally
                    {
                        if ( v_Output != null )
                        {
                            try
                            {
                                v_Output.flush();
                                v_Output.close();
                            }
                            catch (IOException exce)
                            {
                                // Nothing.
                            }
                        }
                        
                        v_Output = null;
                    }
                }
                
                Date v_EndTime = Date.getNowTime();
                i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                v_ExecResult = 1;
            }
            else
            {
                v_ExecResult = 0;
            }
        }
        catch (Exception exce)
        {
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( v_Conn != null )
            {
                try
                {
                    if ( v_ExecResult == 1 )
                    {
                        v_Conn.commit();
                    }
                    else if ( v_ExecResult == -1 )
                    {
                        v_Conn.rollback();
                    }
                    
                    v_Conn.setAutoCommit(v_Old_AutoCommit);
                }
                catch (Exception exce)
                {
                    XSQL.erroring(i_SQL ,exce ,i_XSQL);
                }
            }
            
            i_XSQL.closeDB(v_ResultSet ,v_Statement ,v_Conn);
        }
        
        return v_ExecResult;
    }
    
    
    
    /**
     * 针对数据库的BLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行 -- 无填充值的
     * 
     * 1. BLob类型必须在SELECT语句的第一个输出字段的位置
     * 2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @param i_File             文件对象
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateBLob(final XSQL i_XSQL ,final File i_File)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPUpdate.executeUpdateBLob_Inner(i_XSQL ,v_SQL ,v_DSG ,i_File);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. BLob类型必须在SELECT语句的第一个输出字段的位置
     * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_File             文件对象
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateBLob(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final File i_File)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPUpdate.executeUpdateBLob_Inner(i_XSQL ,v_SQL ,v_DSG ,i_File);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. BLob类型必须在SELECT语句的第一个输出字段的位置
     * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_File             文件对象
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateBLob(final XSQL i_XSQL ,final Object i_Obj ,final File i_File)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPUpdate.executeUpdateBLob_Inner(i_XSQL ,v_SQL ,v_DSG ,i_File);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 写入Blob方法：
     *    Oracle中Blob数据类型是不能够直接插入的，但是可以通过流的形式对Blob类型数据写入或者读取。
     *    INSERT INTO tablename
     *               (
     *                id
     *               ,blobColumn
     *               )
     *        VALUES (
     *                1
     *               ,EMPTY_BLOB()
     *               );
     * 
     *    SELECT  blobColumn
     *      FROM  tablename
     *     WHERE  id = 1
     *       FOR  UPDATE
     * 
     * @param i_SQL              带有Blob字段的查询SQL语句
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     *                           3. 只操作首条数据记录
     * @param i_File             文件对象
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public static int executeUpdateBLob(final XSQL i_XSQL ,final String i_SQL ,final File i_File)
    {
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            return XSQLOPUpdate.executeUpdateBLob_Inner(i_XSQL ,i_SQL ,v_DSG ,i_File);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 写入Blob方法：
     *    Oracle中Blob数据类型是不能够直接插入的，但是可以通过流的形式对Blob类型数据写入或者读取。
     *    INSERT INTO tablename
     *               (
     *                id
     *               ,blobColumn
     *               )
     *        VALUES (
     *                1
     *               ,EMPTY_BLOB()
     *               );
     * 
     *    SELECT  blobColumn
     *      FROM  tablename
     *     WHERE  id = 1
     *       FOR  UPDATE
     * 
     * @param i_SQL              带有Blob字段的查询SQL语句
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     *                           3. 只操作首条数据记录
     * @param i_File             文件对象
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    @SuppressWarnings("deprecation")
    private static int executeUpdateBLob_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final File i_File)
    {
        Connection          v_Conn           = null;
        Statement           v_Statement      = null;
        ResultSet           v_ResultSet      = null;
        boolean             v_Old_AutoCommit = false; // 保存原始状态，使用完后，再恢复原状
        int                 v_ExecResult     = -1;    // 执行结果。0:没有执行  1:需要Commit  -1:需要RollBack
        PrintStream         v_Output         = null;
        BufferedInputStream v_Input          = null;
        byte []             v_ByteBuffer     = new byte[XSQL.$BufferSize];
        int                 v_DataLen        = 0;
        DefaultBLobEvent    v_Event          = null;
        long                v_BLobingSize    = 0;
        boolean             v_IsContinue     = true;
        long                v_BeginTime      = i_XSQL.request().getTime();
        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( i_File == null )
            {
                throw new NullPointerException("File is null of XSQL.");
            }
            
            v_Event = new DefaultBLobEvent(i_XSQL ,i_File.length());
            v_Event.setActionType(1);
            
            v_Conn           = i_XSQL.getConnection(i_DSG);
            v_Old_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_Statement      = v_Conn.createStatement();
            v_ResultSet      = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            
            v_IsContinue = i_XSQL.fireBLobBeforeListener(v_Event);
            
            
            if ( v_IsContinue && v_ResultSet.next() )
            {
                // 获取数据流
                BLOB v_BLob = (BLOB)v_ResultSet.getBlob(1);
                v_Output = new PrintStream(v_BLob.getBinaryOutputStream());
                v_Input  = new BufferedInputStream(new FileInputStream(i_File));
                
                
                if ( i_XSQL.isBlobSafe() )
                {
                    while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 )
                    {
                        v_Output.write(ByteHelp.xorMV(v_ByteBuffer ,0 ,v_DataLen) ,0 ,v_DataLen);
                        
                        v_BLobingSize += v_DataLen;
                        
                        v_Event.setCompleteSize(v_BLobingSize);
                        v_IsContinue = i_XSQL.fireBLobingListener(v_Event);
                    }
                }
                else
                {
                    while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 )
                    {
                        v_Output.write(v_ByteBuffer ,0 ,v_DataLen);
                        
                        v_BLobingSize += v_DataLen;
                        
                        v_Event.setCompleteSize(v_BLobingSize);
                        v_IsContinue = i_XSQL.fireBLobingListener(v_Event);
                    }
                }
                
                Date v_EndTime = Date.getNowTime();
                i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                v_ExecResult = 1;
                v_Event.setSucceedFinish();
            }
            else
            {
                v_ExecResult = 0;
            }
        }
        catch (Exception exce)
        {
            if ( v_Event == null )
            {
                v_Event = new DefaultBLobEvent(i_XSQL ,0);
            }
            v_Event.setEndTime();
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( v_Input != null )
            {
                try
                {
                    v_Input.close();
                }
                catch (Exception exce)
                {
                    XSQL.erroring(i_SQL ,exce ,i_XSQL);
                }
                
                v_Input = null;
            }
            
            if ( v_Output != null )
            {
                try
                {
                    v_Output.flush();
                    v_Output.close();
                }
                catch (Exception exce)
                {
                    XSQL.erroring(i_SQL ,exce ,i_XSQL);
                }
                
                v_Output = null;
            }
            
            if ( v_Conn != null )
            {
                try
                {
                    if ( v_ExecResult == 1 )
                    {
                        v_Conn.commit();
                    }
                    else if ( v_ExecResult == -1 )
                    {
                        v_Conn.rollback();
                    }
                    
                    v_Conn.setAutoCommit(v_Old_AutoCommit);
                }
                catch (Exception exce)
                {
                    XSQL.erroring(i_SQL ,exce ,i_XSQL);
                }
            }
            
            i_XSQL.closeDB(v_ResultSet ,v_Statement ,v_Conn);
            
            i_XSQL.fireBLobAfterListener(v_Event);
        }
        
        return v_ExecResult;
    }
    
    
    
    /**
     * 针对数据库的BLob类型转换成文件并保存
     * 
     * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 只操作首条数据记录
     * @return
     */
    public static boolean executeGetBLob(final XSQL i_XSQL ,final File io_SaveFile)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(v_DSG);
            return XSQLOPUpdate.executeGetBLob_Inner(i_XSQL ,v_SQL ,v_DSG ,io_SaveFile);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型转换成文件并保存
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 只操作首条数据记录
     * @return
     */
    public static boolean executeGetBLob(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final File io_SaveFile)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Values);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Values ,v_DSG);
            return XSQLOPUpdate.executeGetBLob_Inner(i_XSQL ,v_SQL ,v_DSG ,io_SaveFile);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesMap(i_Values));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型转换成文件并保存
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 只操作首条数据记录
     * @return
     */
    public static boolean executeGetBLob(final XSQL i_XSQL ,final Object i_Obj ,final File io_SaveFile)
    {
        i_XSQL.checkContent();
        
        String          v_SQL = null;
        DataSourceGroup v_DSG = null;
        
        try
        {
            i_XSQL.fireBeforeRule(i_Obj);
            v_DSG = i_XSQL.getDataSourceGroup();
            v_SQL = i_XSQL.getContent().getSQL(i_Obj ,v_DSG);
            return XSQLOPUpdate.executeGetBLob_Inner(i_XSQL ,v_SQL ,v_DSG ,io_SaveFile);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(v_SQL ,exce ,i_XSQL).setValuesObject(i_Obj));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型转换成文件并保存
     * 
     * @param i_SQL              常规SQL语句
     * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 只操作首条数据记录
     * @return
     */
    public static boolean executeGetBLob(final XSQL i_XSQL ,final String i_SQL ,final File io_SaveFile)
    {
        DataSourceGroup v_DSG = null;
        
        try
        {
            v_DSG = i_XSQL.getDataSourceGroup();
            return XSQLOPUpdate.executeGetBLob_Inner(i_XSQL ,i_SQL ,v_DSG ,io_SaveFile);
        }
        /* try{}已有中捕获所有异常，并仅出外抛出Null和Runtime两种异常。为保持异常类型不变，写了两遍一样的 */
        catch (NullPointerException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
        catch (RuntimeException exce)
        {
            if ( i_XSQL.getError() != null )
            {
                i_XSQL.getError().errorLog(new XSQLErrorInfo(i_SQL ,exce ,i_XSQL));
            }
            throw exce;
        }
    }
    
    
    
    /**
     * 针对数据库的BLob类型转换成文件并保存
     * 
     * @param i_SQL              常规SQL语句
     * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
     *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 只操作首条数据记录
     * @return
     */
    private static boolean executeGetBLob_Inner(final XSQL i_XSQL ,final String i_SQL ,final DataSourceGroup i_DSG ,final File io_SaveFile)
    {
        Connection          v_Conn        = null;
        Statement           v_Statement   = null;
        ResultSet           v_ResultSet   = null;
        BufferedInputStream v_Input       = null;
        PrintStream         v_Output      = null;
        byte []             v_ByteBuffer  = new byte[XSQL.$BufferSize];
        int                 v_DataLen     = 0;
        boolean             v_ExecResult  = false;
        DefaultBLobEvent    v_Event       = new DefaultBLobEvent(i_XSQL);
        long                v_BLobingSize = 0;
        boolean             v_IsContinue  = true;
        long                v_BeginTime   = i_XSQL.request().getTime();
        
        
        try
        {
            if ( !i_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( io_SaveFile == null )
            {
                throw new NullPointerException("SaveFile is null of XSQL.");
            }
            
            v_Conn      = i_XSQL.getConnection(i_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_ResultSet = v_Statement.executeQuery(i_SQL);
            i_XSQL.log(i_SQL);
            
            if ( v_ResultSet.next() )
            {
                // 获取数据流
                BLOB v_BLob = (BLOB)v_ResultSet.getBlob(1);
                
                v_Event.setActionType(2);
                v_Event.setSize(v_BLob.length());
                
                v_IsContinue = i_XSQL.fireBLobBeforeListener(v_Event);
                
                if ( v_IsContinue )
                {
                    v_Input  = new BufferedInputStream(v_BLob.getBinaryStream());
                    v_Output = new PrintStream(io_SaveFile);
                    
                    if ( i_XSQL.isBlobSafe() )
                    {
                        while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 )
                        {
                            v_Output.write(ByteHelp.xorMV(v_ByteBuffer ,0 ,v_DataLen) ,0 ,v_DataLen);
                            
                            v_BLobingSize += v_DataLen;
                            
                            v_Event.setCompleteSize(v_BLobingSize);
                            v_IsContinue = i_XSQL.fireBLobingListener(v_Event);
                        }
                    }
                    else
                    {
                        while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 )
                        {
                            v_Output.write(v_ByteBuffer ,0 ,v_DataLen);
                            
                            v_BLobingSize += v_DataLen;
                            
                            v_Event.setCompleteSize(v_BLobingSize);
                            v_IsContinue = i_XSQL.fireBLobingListener(v_Event);
                        }
                    }
                    
                    Date v_EndTime = Date.getNowTime();
                    i_XSQL.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,1 ,1L);
                    v_ExecResult = true;
                    v_Event.setSucceedFinish();
                }
            }
        }
        catch (Exception exce)
        {
            v_Event.setEndTime();
            XSQL.erroring(i_SQL ,exce ,i_XSQL);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( v_Input != null )
            {
                try
                {
                    v_Input.close();
                }
                catch (Exception exce)
                {
                    XSQL.erroring(i_SQL ,exce ,i_XSQL);
                }
                
                v_Input = null;
            }
            
            if ( v_Output != null )
            {
                try
                {
                    v_Output.flush();
                    v_Output.close();
                }
                catch (Exception exce)
                {
                    XSQL.erroring(i_SQL ,exce ,i_XSQL);
                }
                
                v_Output = null;
            }
            
            i_XSQL.closeDB(v_ResultSet ,v_Statement ,v_Conn);
            i_XSQL.fireBLobAfterListener(v_Event);
        }
        
        return v_ExecResult;
    }
    
    
    
    /**
     * 插入或更新一行数据之后再写入大数据字段（如,CLob）。
     * 
     * 在executeUpdate()或execute()方法内部被自动执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *              v2.0  2018-07-25  添加：支持多个大数据字段（如，CLob）的写入。
     *
     * @param i_Values     占位符SQL的填充集合。
     * @param i_DataCount  插入或更新语句影响的记录数
     * @return
     */
    public static int executeUpdate_AfterWriteLob(final XSQL i_XSQL ,final Map<String ,?> i_Values ,final int i_DataCount)
    {
        if ( i_DataCount <= 0 )
        {
            return i_DataCount;
        }
        
        if ( !Help.isNull(i_XSQL.getLobName()) && !Help.isNull(i_XSQL.getLobWheres()) )
        {
            String [] v_LobNames  = i_XSQL.getLobName().split(XSQL.$LobName_Split);
            String [] v_LobValues = new String[v_LobNames.length];
            for (int i=0; i<v_LobNames.length; i++)
            {
                Object v_LobValue = MethodReflect.getMapValue(i_Values ,v_LobNames[i].trim());
                if ( v_LobValue != null )
                {
                    v_LobValues[i] = v_LobValue.toString();
                }
                else
                {
                    v_LobValues[i] = "";
                }
            }
            
            if ( !Help.isNull(v_LobValues) )
            {
                return XJava.getXSQL(XSQLOPUpdate.makeLobXSQLID(i_XSQL)).executeUpdateCLob(i_Values ,v_LobValues);
            }
        }
        
        return i_DataCount;
    }
    
    
    
    /**
     * 插入或更新一行数据之后再写入大数据字段（如,CLob）。
     * 
     * 在executeUpdate()或execute()方法内部被自动执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @param i_Values     占位符SQL的填充集合。
     * @param i_DataCount  插入或更新语句影响的记录数
     * @return
     */
    public static int executeUpdate_AfterWriteLob(final XSQL i_XSQL ,Object i_Obj ,int i_DataCount)
    {
        if ( i_DataCount <= 0 )
        {
            return i_DataCount;
        }
        
        if ( !Help.isNull(i_XSQL.getLobName()) && !Help.isNull(i_XSQL.getLobWheres()) )
        {
            String [] v_LobNames  = i_XSQL.getLobName().split(XSQL.$LobName_Split);
            String [] v_LobValues = new String[v_LobNames.length];
            for (int i=0; i<v_LobNames.length; i++)
            {
                try
                {
                    MethodReflect v_MethodReflect = new MethodReflect(i_Obj ,v_LobNames[i].trim() ,true ,MethodReflect.$NormType_Getter);
                    
                    if ( v_MethodReflect != null )
                    {
                        Object v_LobValue = v_MethodReflect.invoke();
                        if ( v_LobValue != null )
                        {
                            v_LobValues[i] = v_LobValue.toString();
                        }
                        else
                        {
                            v_LobValues[i] = "";
                        }
                    }
                }
                catch (Exception exce)
                {
                    // 有些:xx占位符可能找不到对应Java的Getter方法，所以忽略
                    // Nothing.
                    return i_DataCount;
                }
            }
            
            if ( !Help.isNull(v_LobValues) )
            {
                return XJava.getXSQL(XSQLOPUpdate.makeLobXSQLID(i_XSQL)).executeUpdateCLob(i_Obj ,v_LobValues);
            }
        }
        
        return i_DataCount;
    }
    
    
    
    /**
     * 获取或创建处理Lob类型的XSQL的标记XJavaID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @return
     */
    private static String makeLobXSQLID(final XSQL i_XSQL)
    {
        synchronized ( i_XSQL )
        {
            if ( Help.isNull(i_XSQL.getLobXSQLID()) )
            {
                i_XSQL.setLobXSQLID(XSQLOPUpdate.createLobXSQL(i_XSQL));
            }
        }
        
        return i_XSQL.getLobXSQLID();
    }
    
    
    
    /**
     * 根据写入大数据的SQL语法(如下)，创建一个XSQL对象。
     * 
     *    SELECT  clobColumn
     *      FROM  tablename
     *     WHERE  id = 1
     *       FOR  UPDATE
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @return  返回创建XSQL对象实例，对应的XJava标记
     */
    private static String createLobXSQL(final XSQL i_XSQL)
    {
        DataSourceGroup v_DSG      = i_XSQL.getDataSourceGroup();
        String          v_DBType   = i_XSQL.getDataSourceGroup().getDbProductType();
        Param           v_Template = XSQLWriteLob.getLobTempalte(v_DBType);
        
        if ( v_Template == null || Help.isNull(v_Template.getValue()) )
        {
            return null;
        }
        
        String v_SQL = StringHelp.replaceAll(v_Template.getValue()
                                            ,new String[]{":TableName"                        ,":LobName"   ,":IdWheres"}
                                            ,new String[]{i_XSQL.getContent().getSqlTableName() ,i_XSQL.getLobName() ,i_XSQL.getLobWheres()});
        
        XSQL v_LobXSQL = new XSQL();
        
        v_LobXSQL.setDataSourceGroup(v_DSG);
        v_LobXSQL.setContent(v_SQL);
        v_LobXSQL.setXJavaID(Help.NVL(i_XSQL.getXJavaID() ,"XSQL_WriteLob") + "_" + i_XSQL.getLobName() + "_" + Date.getNowTime().getFull_ID());
        
        XJava.putObject(v_LobXSQL.getXJavaID() ,v_LobXSQL);
        
        return v_LobXSQL.getXJavaID();
    }
    
    
    
    /**
     * 本类不允许构建
     */
    private XSQLOPUpdate()
    {
        
    }
    
}
