package org.hy.common.xml.plugins;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Counter;
import org.hy.common.Help;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.plugins.XSQLGroup.XConnection;





/**
 * 组合一组XSQL，有顺序，有参数关联的执行的执行结果
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-03-04
 * @version     v1.0
 */
public class XSQLGroupResult
{
    
    /** 执行结果是否成功 */
    private boolean                           success;
    
    /** 返回多个查询结果集 */
    private Map<String ,Object>               returns;
    
    /** 累计影响(Insert、Update、Delete)的总行数 */
    private Counter<String>                   execSumCount;
    
    /** 
     * 成功时：为最后一个有效执行XSQL的索引位置。下标从0开始。
     * 异常时：为异常时执行XSQL的索引位置。下标从0开始。 
     */
    private int                               execLastNode;
    
    /** 为异常时执行的SQL */
    private String                            exceptionSQL;
    
    /** 为异常对象 */
    private Exception                         exception;
    
    
    
    /** SQL组 */
    private XSQLGroup                         xsqlGroup;
    
    /** 
     * 数据库连接池控制(提交、回滚、关闭)集合。
     * 
     * 当 XSQLGroup.isAutoCommit = false 时有效：由外界决定是否提交、是否回滚的功能。
     */
    private Map<DataSourceGroup ,XConnection> dsgConns;
    
    
    
    public XSQLGroupResult()
    {
        this(false);
    }
    
    
    
    public XSQLGroupResult(boolean i_Result)
    {
        this(i_Result ,new HashMap<String ,Object>() ,new Counter<String>());
    }
    
    
    
    public XSQLGroupResult(boolean i_Success ,Map<String ,Object> i_Returns ,Counter<String> i_ExecSumCount)
    {
        this.success      = i_Success;
        this.returns      = i_Returns;
        this.execSumCount = i_ExecSumCount;
    }

    
    
    public XSQLGroupResult(XSQLGroupResult i_XResult)
    {
        this.success      = i_XResult.success;
        this.returns      = i_XResult.returns;
        this.execSumCount = i_XResult.execSumCount;
        this.execLastNode = i_XResult.execLastNode;
        this.exceptionSQL = i_XResult.exceptionSQL;
        this.exception    = i_XResult.exception;
        this.xsqlGroup    = null;
        this.dsgConns     = null;
    }
    
    
    
    /**
     * 统一提交的事务功能
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-11-03
     * @version     v1.0
     *
     */
    public void commits()
    {
        if ( this.xsqlGroup != null && !Help.isNull(this.dsgConns) )
        {
            this.xsqlGroup.commits(this.dsgConns ,this.execSumCount);
            
            // 统一关闭数据库连接
            this.xsqlGroup.closeConnections(this.dsgConns);
        }
    }
    
    
    
    /**
     * 统一回滚的事务功能
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-11-03
     * @version     v1.0
     *
     */
    public void rollbacks()
    {
        if ( this.xsqlGroup != null && !Help.isNull(this.dsgConns) )
        {
            this.xsqlGroup.rollbacks(this.dsgConns);
            
            // 统一关闭数据库连接
            this.xsqlGroup.closeConnections(this.dsgConns);
        }
    }
    
    
    
    /**
     * 获取：执行结果是否成功
     */
    public boolean isSuccess()
    {
        return success;
    }

    
    /**
     * 设置：执行结果是否成功
     * 
     * @param success 
     */
    public XSQLGroupResult setSuccess(boolean success)
    {
        this.success = success;
        return this;
    }


    /**
     * 获取：返回多个查询结果集
     */
    public Map<String ,Object> getReturns()
    {
        return returns;
    }

    
    /**
     * 设置：返回多个查询结果集
     * 
     * @param returns 
     */
    public XSQLGroupResult setReturns(Map<String ,Object> returns)
    {
        this.returns = returns;
        return this;
    }

    
    /**
     * 获取：累计影响(Insert、Update、Delete)的总行数
     */
    public Counter<String> getExecSumCount()
    {
        return execSumCount;
    }

    
    /**
     * 设置：累计影响(Insert、Update、Delete)的总行数
     * 
     * @param execSumCount 
     */
    public XSQLGroupResult setExecSumCount(Counter<String> execSumCount)
    {
        this.execSumCount = execSumCount;
        return this;
    }

    
    /**
     * 成功时：为最后一个有效执行XSQL的索引位置。下标从0开始。
     * 异常时：为异常时执行XSQL的索引位置。下标从0开始。
     */
    public int getExecLastNode()
    {
        return execLastNode;
    }

    
    /**
     * 成功时：为最后一个有效执行XSQL的索引位置。下标从0开始。
     * 异常时：为异常时执行XSQL的索引位置。下标从0开始。
     * 
     * @param execLastNode 
     */
    public XSQLGroupResult setExecLastNode(int execLastNode)
    {
        this.execLastNode = execLastNode;
        return this;
    }
    
    
    /**
     * 异常时：为异常时执行XSQL的索引位置。下标从0开始。
     */
    public int getExceptionNode()
    {
        return execLastNode;
    }

    
    /**
     * 异常时：为异常时执行XSQL的索引位置。下标从0开始。
     * 
     * @param exceptionNode 
     */
    public XSQLGroupResult setExceptionNode(int exceptionNode)
    {
        this.execLastNode = exceptionNode;
        this.success      = false;
        return this;
    }

    
    /**
     * 获取：为异常时执行的SQL
     */
    public String getExceptionSQL()
    {
        return exceptionSQL;
    }

    
    /**
     * 设置：为异常时执行的SQL
     * 
     * @param exceptionSQL 
     */
    public XSQLGroupResult setExceptionSQL(String exceptionSQL)
    {
        this.exceptionSQL = exceptionSQL;
        this.success      = false;
        return this;
    }

    
    /**
     * 获取：为异常对象
     */
    public Exception getException()
    {
        return exception;
    }

    
    /**
     * 设置：为异常对象
     * 
     * @param exception 
     */
    public XSQLGroupResult setException(Exception exception)
    {
        this.exception = exception;
        this.success   = false;
        return this;
    }


    
    /**
     * 设置：SQL组
     * 
     * 为了安全不提供 getter() 方法 
     * 
     * @param xsqlGroup 
     */
    public void setXsqlGroup(XSQLGroup xsqlGroup)
    {
        this.xsqlGroup = xsqlGroup;
    }
    

    
    /**
     * 设置：数据库连接池控制(提交、回滚、关闭)集合。
     * 
     * 当 XSQLGroup.isAutoCommit = false 时有效：由外界决定是否提交、是否回滚的功能。
     * 
     * 为了安全不提供 getter() 方法 
     * 
     * @param dsgConns 
     */
    public void setDsgConns(Map<DataSourceGroup ,XConnection> dsgConns)
    {
        this.dsgConns = dsgConns;
    }
    
}
