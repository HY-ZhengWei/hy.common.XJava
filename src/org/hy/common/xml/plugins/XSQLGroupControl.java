package org.hy.common.xml.plugins;

import java.util.Map;

import org.hy.common.Counter;
import org.hy.common.Help;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.thread.TaskGroup;
import org.hy.common.xml.plugins.XSQLGroup.XConnection;





/**
 * XSQL组的控制中心。如，统一事务提交、统一事务回滚
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-22
 * @version     v1.0
 */
public class XSQLGroupControl
{
    
    /** SQL组 */
    protected XSQLGroup                         xsqlGroup;
    
    /** 
     * 数据库连接池控制(提交、回滚、关闭)集合。
     * 
     * 当 XSQLGroup.isAutoCommit = false 时有效：由外界决定是否提交、是否回滚的功能。
     */
    protected Map<DataSourceGroup ,XConnection> dsgConns;
    
    /** 累计影响(Insert、Update、Delete)的总行数 */
    protected Counter<String>                   execSumCount;
    
    protected TaskGroup                         taskGroup;
    
    
    
    public XSQLGroupControl()
    {
        this(null ,null ,null);
    }
    
    
    
    public XSQLGroupControl(XSQLGroup i_XSQLGroup ,Map<DataSourceGroup ,XConnection> i_DsgConns ,Counter<String> i_ExecSumCount)
    {
        this.xsqlGroup    = i_XSQLGroup;
        this.dsgConns     = i_DsgConns;
        this.execSumCount = i_ExecSumCount;
    }
    
    
    
    /**
     * 统一提交的事务功能
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-22
     * @version     v1.0
     */
    public synchronized void commits()
    {
        this.commits(this.execSumCount);
    }
    
    
    
    /**
     * 统一提交的事务功能
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-11-03
     * @version     v1.0
     *
     * @param i_ExecSumCount   累计总行数（可选参数，可为null）
     */
    public synchronized void commits(Counter<String> i_ExecSumCount)
    {
        if ( this.xsqlGroup != null && !Help.isNull(this.dsgConns) )
        {
            this.xsqlGroup.commits(this.dsgConns ,i_ExecSumCount);
            
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
    public synchronized void rollbacks()
    {
        if ( this.xsqlGroup != null && !Help.isNull(this.dsgConns) )
        {
            this.xsqlGroup.rollbacks(this.dsgConns);
            
            // 统一关闭数据库连接
            this.xsqlGroup.closeConnections(this.dsgConns);
        }
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
    public void setExecSumCount(Counter<String> execSumCount)
    {
        this.execSumCount = execSumCount;
    }
    
}
