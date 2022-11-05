package org.hy.common.xml.plugins.analyse.data;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.thread.TaskPool;
import org.hy.common.thread.ThreadPool;
import org.hy.common.thread.ThreadReport;
import org.hy.common.xml.SerializableDef;





/**
 * 线程池监控统计类
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-02-27
 * @version     v1.0
 */
public class AnalyseThreadPoolTotal extends SerializableDef
{
    
    private static final long serialVersionUID = -743136698837969618L;
    
    
    /** 主机名称 */
    private String             hostName;
    
    /** 线程的总个数 */
    private long               threadCount;
    
    /** 空闲的线程数 */
    private long               idleThreadCount;
    
    /** 活动的线程数 */
    private long               activeThreadCount;
    
    /** 队排中等待的任务数 */
    private long               waitTaskCount;
    
    /** 线程池监控信息 */
    private List<ThreadReport> reports;
    
    
    
    public AnalyseThreadPoolTotal(String i_HostName)
    {
        this.hostName = i_HostName;
        this.reports  = new ArrayList<ThreadReport>();
    }
    
    
    
    public AnalyseThreadPoolTotal()
    {
        this.hostName          = "";
        this.waitTaskCount     = TaskPool.size();
        this.threadCount       = ThreadPool.getThreadCount();
        this.idleThreadCount   = ThreadPool.getIdleThreadCount();
        this.activeThreadCount = ThreadPool.getActiveThreadCount();
        this.reports           = ThreadPool.getThreadReports();
    }

    
    
    /**
     * 获取：主机名称
     */
    public String getHostName()
    {
        return hostName;
    }
    

    
    /**
     * 设置：主机名称
     * 
     * @param hostName
     */
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }
    


    /**
     * 获取：线程的总个数
     */
    public long getThreadCount()
    {
        return threadCount;
    }
    

    
    /**
     * 获取：空闲的线程数
     */
    public long getIdleThreadCount()
    {
        return idleThreadCount;
    }
    

    
    /**
     * 获取：活动的线程数
     */
    public long getActiveThreadCount()
    {
        return activeThreadCount;
    }
    

    
    /**
     * 获取：线程池监控信息
     */
    public List<ThreadReport> getReports()
    {
        return reports;
    }
    

    
    /**
     * 设置：线程的总个数
     * 
     * @param threadCount
     */
    public void setThreadCount(long threadCount)
    {
        this.threadCount = threadCount;
    }
    

    
    /**
     * 设置：空闲的线程数
     * 
     * @param idleThreadCount
     */
    public void setIdleThreadCount(long idleThreadCount)
    {
        this.idleThreadCount = idleThreadCount;
    }
    

    
    /**
     * 设置：活动的线程数
     * 
     * @param activeThreadCount
     */
    public void setActiveThreadCount(long activeThreadCount)
    {
        this.activeThreadCount = activeThreadCount;
    }
    

    
    /**
     * 设置：线程池监控信息
     * 
     * @param reports
     */
    public void setReports(List<ThreadReport> reports)
    {
        this.reports = reports;
    }


    
    /**
     * 获取：队排中等待的任务数
     */
    public long getWaitTaskCount()
    {
        return waitTaskCount;
    }
    

    
    /**
     * 设置：队排中等待的任务数
     * 
     * @param waitTaskCount
     */
    public void setWaitTaskCount(long waitTaskCount)
    {
        this.waitTaskCount = waitTaskCount;
    }
    
}
