package org.hy.common.xml.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Counter;
import org.hy.common.Help;





/**
 * 组合一组XSQL，有顺序，有参数关联的执行的执行结果
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-03-04
 * @version     v1.0
 */
public class XSQLGroupResult extends XSQLGroupControl
{
    
    /** 执行结果是否成功 */
    private boolean                           success;
    
    /** 返回多个查询结果集 */
    private Map<String ,Object>               returns;
    
    /** 
     * 成功时：为最后一个有效执行XSQL的索引位置。下标从0开始。
     * 异常时：为异常时执行XSQL的索引位置。下标从0开始。 
     */
    private int                               execLastNode;
    
    /** 为异常时执行的SQL */
    private String                            exceptionSQL;
    
    /** 为异常对象 */
    private Exception                         exception;
    
    /** 计算过程中产生的临时缓存。将在XSQL组整体执行完成前集中释放 */
    private List<Object>                      tempCaches;
    
    
    
    public XSQLGroupResult()
    {
        this(false);
    }
    
    
    
    public XSQLGroupResult(boolean i_Result)
    {
        this(i_Result ,new HashMap<String ,Object>() ,new Counter<String>() ,new ArrayList<Object>());
    }
    
    
    
    public XSQLGroupResult(boolean             i_Success 
                          ,Map<String ,Object> i_Returns 
                          ,Counter<String>     i_ExecSumCount 
                          ,List<Object>        i_TempCacheMap)
    {
        this.success      = i_Success;
        this.returns      = i_Returns;
        this.execSumCount = i_ExecSumCount;
        this.tempCaches   = i_TempCacheMap;
    }

    
    
    public XSQLGroupResult(XSQLGroupResult i_XResult)
    {
        super(i_XResult);
        this.success      = i_XResult.success;
        this.returns      = i_XResult.returns;
        this.execLastNode = i_XResult.execLastNode;
        this.exceptionSQL = i_XResult.exceptionSQL;
        this.exception    = i_XResult.exception;
        this.tempCaches   = i_XResult.tempCaches;
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
     * 获取：计算过程中产生的临时缓存。将在XSQL组整体执行完成前集中释放
     */
    public List<Object> getTempCaches()
    {
        return tempCaches;
    }

    
    /**
     * 设置：计算过程中产生的临时缓存。将在XSQL组整体执行完成前集中释放
     * 
     * @param tempCaches 
     */
    public void setTempCaches(List<Object> tempCaches)
    {
        this.tempCaches = tempCaches;
    }
    
    
    /**
     * 记录计算过程中产生的临时缓存
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-02-23
     * @version     v1.0
     *
     * @param i_TempCache
     */
    public void addTempCache(Object i_TempCache)
    {
        this.tempCaches.add(i_TempCache);
    }
    
    
    /**
     * 清除计算过程中产生的临时缓存。应在XSQL组整体执行完成前集中释放
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-02-23
     * @version     v1.0
     */
    public synchronized void clearTempCaches()
    {
        if ( !Help.isNull(this.tempCaches) )
        {
            for (int i=this.tempCaches.size() - 1; i >= 0; i--)
            {
                Object v_TempCache = this.tempCaches.remove(i);
                
                if ( v_TempCache instanceof List )
                {
                    ((List<?>)v_TempCache).clear();
                }
                else if ( v_TempCache instanceof Set )
                {
                    ((Set<?>)v_TempCache).clear();
                }
                else if ( v_TempCache instanceof Map )
                {
                    ((Map<? ,?>)v_TempCache).clear();
                }
                
                v_TempCache = null;
            }
        }
    }
    
}
