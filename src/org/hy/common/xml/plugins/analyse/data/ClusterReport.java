package org.hy.common.xml.plugins.analyse.data;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.thread.TaskPool;
import org.hy.common.xml.SerializableDef;

import com.sun.management.OperatingSystemMXBean;





/**
 * 集群的服务器信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-02
 * @version     v1.0
 *              v2.0  2018-12-20  添加1：当前系统时间
 *                                添加2：尝试计算Linux的真实内存使用率
 */
public class ClusterReport extends SerializableDef
{

    private static final long serialVersionUID = 849137073888555779L;
    
    
    /** 主机名称 */
    private String hostName;
    
    /** 启动时间 */
    private String startTime;
    
    /** 操作系统的当前时间 */
    private String systemTime;
    
    /** 操作系统CPU使用率 */
    private double osCPURate;
    
    /** 操作系统内存使用率 */
    private double osMemoryRate;
    
    /** Linux系统内存使用率(通过Free命令计算的) */
    private double linuxMemoryRate;
    
    /** JVM最大内存：Java虚拟机（这个进程）能构从操作系统那里挖到的最大的内存。JVM参数为：-Xmx */
    private long   maxMemory;
    
    /** JVM内存总量：Java虚拟机现在已经从操作系统那里挖过来的内存大小。JVM参数为：-Xms */
    private long   totalMemory;
    
    /** JVM空闲内存 */
    private long   freeMemory;
    
    /** 线程总数 */
    private long   threadCount;
    
    /** 队列等待的任务数 */
    private long   queueCount;
    
    /** 服务器情况（正常、异常） */
    private String serverStatus;
    
    
    
    public ClusterReport()
    {
        this.hostName        = "";
        this.osCPURate       = 0;
        this.osMemoryRate    = 0;
        this.linuxMemoryRate = -1;
        this.maxMemory       = 0;
        this.totalMemory     = 0;
        this.freeMemory      = 0;
        this.threadCount     = 0;
        this.queueCount      = 0;
        this.startTime       = "";
        this.systemTime      = new Date().getFullMilli();
        this.serverStatus    = "";
    }
    
    
    
    public ClusterReport(Date i_StartTime)
    {
        Runtime               v_RunTime = Runtime.getRuntime();
        OperatingSystemMXBean v_OS      = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        
        ThreadGroup v_PT  = null;
        for (v_PT = Thread.currentThread().getThreadGroup(); v_PT.getParent() != null; v_PT = v_PT.getParent());
        
        this.startTime       = i_StartTime.getFull();
        this.systemTime      = new Date().getFullMilli();
        this.maxMemory       = v_RunTime.maxMemory();
        this.totalMemory     = v_RunTime.totalMemory();
        this.freeMemory      = v_RunTime.freeMemory();
        this.threadCount     = v_PT.activeCount();
        this.queueCount      = TaskPool.size();
        this.osCPURate       = Help.round(Help.multiply(v_OS.getSystemCpuLoad() ,100) ,2);
        this.osMemoryRate    = Help.round(Help.multiply(1 - Help.division(v_OS.getFreePhysicalMemorySize() ,v_OS.getTotalPhysicalMemorySize()) ,100) ,2);
        this.linuxMemoryRate = Help.round(Help.multiply(    Help.division(this.calcLinuxMemory()           ,v_OS.getTotalPhysicalMemorySize()) ,100) ,2);
        this.hostName        = "";
        this.serverStatus    = "";
    }
    
    
    
    /**
     * 计算Linux系统实际内存使用大小(通过Free命令计算的)
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-20
     * @version     v1.0
     *
     * @return      单位：Byte
     */
    public double calcLinuxMemory()
    {
        try
        {
            List<String> v_CRet = Help.executeCommand(true , true,"free -k");
            if ( Help.isNull(v_CRet) || v_CRet.size() < 2)
            {
                return -1;
            }
            
            String v_LineInfo = v_CRet.get(1).trim();
            if ( Help.isNull(v_LineInfo) )
            {
                return -1;
            }
            
            while ( v_LineInfo.indexOf("  ") >= 0 )
            {
                v_LineInfo = StringHelp.replaceAll(v_LineInfo ,"  " ," ");
            }
            
            String [] v_Memorys = v_LineInfo.split(" ");
            if ( v_Memorys.length < 7 )
            {
                return -1;
            }
            
            return Help.multiply(Help.subtract(v_Memorys[2] ,v_Memorys[5] ,v_Memorys[6]) ,1024);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return -1;
    }

    
    
    /**
     * 获取：操作系统CPU使用率
     */
    public double getOsCPURate()
    {
        return osCPURate;
    }


    
    /**
     * 获取：操作系统内存使用率
     */
    public double getOsMemoryRate()
    {
        return osMemoryRate;
    }
    
    
    
    /**
     * 设置：操作系统CPU使用率
     * 
     * @param osCPURate 
     */
    public void setOsCPURate(double osCPURate)
    {
        this.osCPURate = osCPURate;
    }
    

    
    /**
     * 设置：操作系统内存使用率
     * 
     * @param osMemoryRate 
     */
    public void setOsMemoryRate(double osMemoryRate)
    {
        this.osMemoryRate = osMemoryRate;
    }
    
    

    /**
     * 获取：启动时间
     */
    public String getStartTime()
    {
        return startTime;
    }


    
    /**
     * 获取：JVM最大内存：Java虚拟机（这个进程）能构从操作系统那里挖到的最大的内存。JVM参数为：-Xmx
     */
    public long getMaxMemory()
    {
        return maxMemory;
    }


    
    /**
     * 获取：JVM内存总量：Java虚拟机现在已经从操作系统那里挖过来的内存大小。JVM参数为：-Xms
     */
    public long getTotalMemory()
    {
        return totalMemory;
    }


    
    /**
     * 获取：JVM空闲内存
     */
    public long getFreeMemory()
    {
        return freeMemory;
    }
    

    
    /**
     * 设置：启动时间
     * 
     * @param startTime 
     */
    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }
    

    
    /**
     * 设置：JVM最大内存：Java虚拟机（这个进程）能构从操作系统那里挖到的最大的内存。JVM参数为：-Xmx
     * 
     * @param maxMemory 
     */
    public void setMaxMemory(long maxMemory)
    {
        this.maxMemory = maxMemory;
    }
    

    
    /**
     * 设置：JVM内存总量：Java虚拟机现在已经从操作系统那里挖过来的内存大小。JVM参数为：-Xms
     * 
     * @param totalMemory 
     */
    public void setTotalMemory(long totalMemory)
    {
        this.totalMemory = totalMemory;
    }
    

    
    /**
     * 设置：JVM空闲内存
     * 
     * @param freeMemory 
     */
    public void setFreeMemory(long freeMemory)
    {
        this.freeMemory = freeMemory;
    }

    
    
    /**
     * 获取：线程总数
     */
    public long getThreadCount()
    {
        return threadCount;
    }


    
    /**
     * 设置：线程总数
     * 
     * @param threadCount 
     */
    public void setThreadCount(long threadCount)
    {
        this.threadCount = threadCount;
    }


    
    /**
     * 获取：队列等待的任务数
     */
    public long getQueueCount()
    {
        return queueCount;
    }


    
    /**
     * 设置：队列等待的任务数
     * 
     * @param queueCount 
     */
    public void setQueueCount(long queueCount)
    {
        this.queueCount = queueCount;
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
     * 获取：服务器情况（正常、异常）
     */
    public String getServerStatus()
    {
        return serverStatus;
    }
    

    
    /**
     * 设置：服务器情况（正常、异常）
     * 
     * @param serverStatus 
     */
    public void setServerStatus(String serverStatus)
    {
        this.serverStatus = serverStatus;
    }


    
    /**
     * 获取：操作系统的当前时间
     */
    public String getSystemTime()
    {
        return systemTime;
    }


    
    /**
     * 设置：操作系统的当前时间
     * 
     * @param systemTime 
     */
    public void setSystemTime(String systemTime)
    {
        this.systemTime = systemTime;
    }



    /**
     * 获取：Linux系统内存使用率(通过Free命令计算的)
     */
    public double getLinuxMemoryRate()
    {
        return linuxMemoryRate;
    }


    
    /**
     * 设置：Linux系统内存使用率(通过Free命令计算的)
     * 
     * @param linuxMemoryRate 
     */
    public void setLinuxMemoryRate(double linuxMemoryRate)
    {
        this.linuxMemoryRate = linuxMemoryRate;
    }
    
}
