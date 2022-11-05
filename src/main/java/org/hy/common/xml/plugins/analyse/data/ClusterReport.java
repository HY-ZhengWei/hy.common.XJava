package org.hy.common.xml.plugins.analyse.data;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 *              v3.0  2018-12-21  添加1：计算Linux磁盘使用率
 *                                添加2：计算Window磁盘使用率
 *              v3.1  2019-02-27  修正1：对于SUSE 15系统上实际内存使用大小的修正。
 */
public class ClusterReport extends SerializableDef
{

    private static final long serialVersionUID = 849137073888555779L;
    
    
    /** 操作类型（1:Linux，1:Unix，2:Windows） */
    private int    osType;
    
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
    
    /** Linux系统上最大的磁盘使用率 */
    private double linuxDiskMaxRate;
    
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
    
    /** 运行时的JDK版本 */
    private String javaVersion;
    
    
    
    public ClusterReport()
    {
        this.osType           = this.calcOSType();
        this.hostName         = "";
        this.osCPURate        = 0;
        this.osMemoryRate     = 0;
        this.linuxMemoryRate  = -1;
        this.linuxDiskMaxRate = -1;
        this.maxMemory        = 0;
        this.totalMemory      = 0;
        this.freeMemory       = 0;
        this.threadCount      = 0;
        this.queueCount       = 0;
        this.startTime        = "";
        this.systemTime       = new Date().getFullMilli();
        this.serverStatus     = "";
        this.javaVersion      = "";
    }
    
    
    
    public ClusterReport(Date i_StartTime)
    {
        Runtime               v_RunTime = Runtime.getRuntime();
        OperatingSystemMXBean v_OS      = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        
        ThreadGroup v_PT  = null;
        for (v_PT = Thread.currentThread().getThreadGroup(); v_PT.getParent() != null; v_PT = v_PT.getParent());
        
        this.osType           = this.calcOSType();
        this.startTime        = i_StartTime.getFull();
        this.systemTime       = new Date().getFullMilli();
        this.maxMemory        = v_RunTime.maxMemory();
        this.totalMemory      = v_RunTime.totalMemory();
        this.freeMemory       = v_RunTime.freeMemory();
        this.threadCount      = v_PT.activeCount();
        this.queueCount       = TaskPool.size();
        this.osCPURate        = Help.round(Help.multiply(v_OS.getSystemCpuLoad() ,100) ,2);
        this.osMemoryRate     = Help.round(Help.multiply(1 - Help.division(v_OS.getFreePhysicalMemorySize() ,v_OS.getTotalPhysicalMemorySize()) ,100) ,2);
        this.linuxDiskMaxRate = Help.round(this.calcDiskMaxRate() ,2);
        this.hostName         = "";
        this.serverStatus     = "";
        this.javaVersion      = Help.getJavaVersion();
        
        if ( this.osType == 1 )
        {
            this.linuxMemoryRate  = this.calcLinuxMemory();
        }
        else
        {
            this.linuxMemoryRate = -1;
        }
        
        if ( this.linuxMemoryRate >= 0 )
        {
            this.linuxMemoryRate = Help.round(Help.multiply(Help.division(this.linuxMemoryRate ,v_OS.getTotalPhysicalMemorySize()) ,100) ,2);
        }
    }
    
    
    
    /**
     * 计算操作系统的类型
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-22
     * @version     v1.0
     *
     * @return
     */
    public int calcOSType()
    {
        try
        {
            File v_Disk = new File("C:");
            
            if ( v_Disk.exists() )
            {
                return 2;
            }
            else
            {
                return 1;
            }
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        return 1;
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
            List<String> v_CRet = Help.executeCommand(false , true,"free -k");
            if ( Help.isNull(v_CRet) || v_CRet.size() < 2 )
            {
                return -1;
            }
            
            String v_LineInfo = v_CRet.get(1).trim();
            if ( Help.isNull(v_LineInfo) )
            {
                return -1;
            }
            
            v_LineInfo = StringHelp.trimToDistinct(StringHelp.replaceAll(v_LineInfo ,":" ," ") ," ");
            String []    v_Memorys    = v_LineInfo.split(" ");
            List<String> v_MemoryList = new ArrayList<String>();
            for (String v_Memory : v_Memorys)
            {
                if ( !Help.isNull(v_Memory) && Help.isNumber(v_Memory) )
                {
                    v_MemoryList.add(v_Memory);
                }
            }
            if ( v_MemoryList.size() < 6 )
            {
                return -1;
            }
            
            if ( v_CRet.get(0).toLowerCase().indexOf("buff/cache") >= 0 )
            {
                // 实际使用内存 = 1 = Used 。测试系统 Open SUSE 15
                return Help.multiply(v_MemoryList.get(1) ,1024);
            }
            else
            {
                // 实际使用内存 = 1 - 3 - 4 - 5 = Used - Shared - Buffers - Cached 。测试系统 SUSE 12
                return Help.multiply(Help.subtract(v_MemoryList.get(1) ,v_MemoryList.get(3) ,v_MemoryList.get(4) ,v_MemoryList.get(5)) ,1024);
            }
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        return -1;
    }
    
    
    
    /**
     * 计算Linux和Win系统上最大的磁盘使用率
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-21
     * @version     v1.0
     *
     * @return
     */
    public double calcDiskMaxRate()
    {
        double v_Rate = -1;
        
        if ( this.osType == 1 )
        {
            v_Rate = this.calcLinuxDiskMaxRate();
        }
        else
        {
            v_Rate = this.calcWinDiskMaxRate();
        }
        
        return v_Rate;
    }
    
    
    
    /**
     * 计算Linux系统上最大的磁盘使用率
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-21
     * @version     v1.0
     *
     * @return
     */
    public double calcWinDiskMaxRate()
    {
        Map<String ,Double> v_Disks = this.calcWinDiskUseRatesV2();
        
        if ( Help.isNull(v_Disks) )
        {
            return -1;
        }
        
        Help.print(v_Disks);
        
        List<Double> v_Rate = Help.toList(v_Disks);
        return Help.max(-1D ,v_Rate.toArray(new Double[]{}));
    }
    
    
    
    /**
     * 计算Windows系统上每个磁盘的使用率
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-22
     * @version     v1.0
     *
     * @return
     */
    public Map<String ,Double> calcWinDiskUseRatesV2()
    {
        Map<String ,Double> v_Disks     = new HashMap<String ,Double>();
        String              v_DiskNames = "CDEFGHIJKLMNOPQRSTUVWXYZ";
        
        for (int i=0; i<v_DiskNames.length(); i++)
        {
            String v_DiskName = v_DiskNames.charAt(i) + ":";
            File   v_Disk     = new File(v_DiskName);
            
            if ( v_Disk.exists() )
            {
                long v_TotalSpace = v_Disk.getTotalSpace();
                long v_FreeSpace  = v_Disk.getFreeSpace();
                long v_UsedSpace  = v_TotalSpace - v_FreeSpace;
                
                v_Disks.put(v_DiskName ,Help.multiply(Help.division(v_UsedSpace ,v_TotalSpace) ,100));
            }
            else if ( i == 0 ) 
            {
                break;
            }
        }
        
        return v_Disks;
    }
    
    
    
    /**
     * 计算Windows系统上每个磁盘的使用率
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-21
     * @version     v1.0
     *
     * @return
     */
    public Map<String ,Double> calcWinDiskUseRates()
    {
        Map<String ,Double> v_Disks = new HashMap<String ,Double>();
        
        try
        {
            List<String> v_CRet = Help.executeCommand("GBK" ,false ,true ,5 ,"cmd.exe /c Wmic LogicalDisk Get Caption,DeviceID,FreeSpace,Size");
            if ( Help.isNull(v_CRet) )
            {
                return v_Disks;
            }
            
            for (String v_LineInfo : v_CRet)
            {
                if ( !Help.isNull(v_LineInfo) )
                {
                    v_LineInfo = StringHelp.trimToDistinct(v_LineInfo.trim() ," ");
                    String [] v_Infos = v_LineInfo.split(" "); 
                    
                    if ( v_Infos.length < 4 || Help.isNull(v_Infos[0]) || Help.isNull(v_Infos[2]) || Help.isNull(v_Infos[3]) )
                    {
                        continue;
                    }
                    
                    if ( !Help.isNumber(v_Infos[2]) || !Help.isNumber(v_Infos[3]) )
                    {
                        continue;
                    }
                    
                    v_Disks.put(v_Infos[0] ,Help.multiply(Help.division(Help.subtract(v_Infos[3] ,v_Infos[2]) ,v_Infos[3]) ,100));
                }
            }
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        return v_Disks;
    }
    
    
    
    /**
     * 计算Linux系统上最大的磁盘使用率
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-21
     * @version     v1.0
     *
     * @return
     */
    public double calcLinuxDiskMaxRate()
    {
        Map<String ,Double> v_Disks    = this.calcLinuxDiskSizes();
        Map<String ,Double> v_Rates    = null;
        List<Double>        v_RateList = null;
        StringBuilder       v_Buffer   = new StringBuilder();
        
        if ( Help.isNull(v_Disks) )
        {
            return -1;
        }
        
        v_Buffer.append("\n");
        
        try
        {
            v_RateList = new ArrayList<Double>();
            v_Rates    = this.calcLinuxDiskUseRate();
            if ( Help.isNull(v_Rates) )
            {
                return -1;
            }
            
            for (Map.Entry<String ,Double> v_Disk : v_Disks.entrySet())
            {
                Double v_UseRate = v_Rates.get(v_Disk.getKey());
                
                if ( v_UseRate == null )
                {
                    continue;
                }
                
                v_RateList.add(v_UseRate);
                v_Buffer
                .append(v_Disk.getKey())
                .append("\t\t\t")
                .append(v_Disk.getValue()).append(" GB")
                .append("\t\t\t")
                .append(v_UseRate)
                .append("%\n");
            }
            
            System.out.println(v_Buffer.toString());
            
            return Help.max(0D ,v_RateList.toArray(new Double[] {})).doubleValue();
        }
        finally
        {
            v_Disks.clear();
            v_Rates.clear();
            v_RateList.clear();
            
            v_Disks    = null;
            v_Rates    = null;
            v_RateList = null;
        }
    }
    
    
    
    /**
     * 计算Linux服务上每个挂载磁盘的大小
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-21
     * @version     v1.0
     *
     * @return      Map.key    磁盘名称
     *              Map.value  磁盘大小（单位：GB）
     */
    public Map<String ,Double> calcLinuxDiskSizes()
    {
        Map<String ,Double> v_Disks = new HashMap<String ,Double>();
        
        try
        {
            List<String> v_CRet = Help.executeCommand(false ,true ,"fdisk -l");
            if ( Help.isNull(v_CRet) )
            {
                return v_Disks;
            }
            
            for (String v_LineInfo : v_CRet)
            {
                if ( !Help.isNull(v_LineInfo) )
                {
                    if ( v_LineInfo.trim().startsWith("/") )
                    {
                        v_LineInfo = StringHelp.replaceAll(v_LineInfo.trim() ,"*" ," ");
                        v_LineInfo = StringHelp.trimToDistinct(v_LineInfo ," ");
                        String [] v_Infos = v_LineInfo.split(" "); 
                        
                        if ( v_Infos.length < 5 || Help.isNull(v_Infos[0]) || Help.isNull(v_Infos[4]) )
                        {
                            continue;
                        }
                        
                        String v_Size = v_Infos[4].toUpperCase();
                        double v_Pow  = 1;
                        if ( v_Size.endsWith("TB") || v_Size.endsWith("T") )
                        {
                            v_Pow = 1024;
                        }
                        else if ( v_Size.endsWith("GB") || v_Size.endsWith("G") )
                        {
                            v_Pow = 1;
                        }
                        else if ( v_Size.endsWith("MB") || v_Size.endsWith("M") )
                        {
                            v_Pow = 1 / 1024;
                        }
                        else if ( v_Size.endsWith("KB") || v_Size.endsWith("K") )
                        {
                            v_Pow = 1 / 1024 / 1024;
                        }
                        
                        v_Size = StringHelp.replaceAll(v_Size ,new String[]{"TB" ,"T" ,"GB" ,"G" ,"MB" ,"M" ,"KB" ,"K"} ,new String[]{""}).trim();
                        v_Disks.put(v_Infos[0].toLowerCase() ,Help.multiply(v_Size ,v_Pow));
                    }
                }
            }
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        return v_Disks;
    }
    
    
    
    /**
     * 计算Linux服务上每个磁盘的使用率
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-21
     * @version     v1.0
     *
     * @return      Map.key    磁盘名称
     *              Map.value  磁盘使用率%
     */
    public Map<String ,Double> calcLinuxDiskUseRate()
    {
        Map<String ,Double> v_Disks = new HashMap<String ,Double>();
        
        try
        {
            List<String> v_CRet = Help.executeCommand(false ,true ,"df");
            if ( Help.isNull(v_CRet) )
            {
                return v_Disks;
            }
            
            for (String v_LineInfo : v_CRet)
            {
                if ( !Help.isNull(v_LineInfo) )
                {
                    if ( v_LineInfo.trim().startsWith("/") )
                    {
                        v_LineInfo = StringHelp.trimToDistinct(v_LineInfo.trim() ," ");
                        String [] v_Infos = v_LineInfo.split(" "); 
                        
                        if ( v_Infos.length < 5 || Help.isNull(v_Infos[0]) || Help.isNull(v_Infos[4]) )
                        {
                            continue;
                        }
                        
                        String v_Rate = StringHelp.replaceAll(v_Infos[4].trim() ,"%" ,"");
                        if ( !Help.isNumber(v_Rate) )
                        {
                            continue;
                        }
                        
                        Double v_Old = v_Disks.get(v_Infos[0].toLowerCase());
                        if ( v_Old == null )
                        {
                            v_Old = 0D;
                        }
                        
                        v_Disks.put(v_Infos[0].toLowerCase() ,Help.max(Double.parseDouble(v_Rate) ,v_Old));
                    }
                }
            }
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        return v_Disks;
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


    
    /**
     * 获取：Linux系统上最大的磁盘使用率
     */
    public double getLinuxDiskMaxRate()
    {
        return linuxDiskMaxRate;
    }


    
    /**
     * 设置：Linux系统上最大的磁盘使用率
     * 
     * @param linuxDiskMaxRate 
     */
    public void setLinuxDiskMaxRate(double linuxDiskMaxRate)
    {
        this.linuxDiskMaxRate = linuxDiskMaxRate;
    }


    
    /**
     * 获取：操作类型（1:Linux，1:Unix，2:Windows）
     */
    public int getOsType()
    {
        return osType;
    }


    
    /**
     * 设置：操作类型（1:Linux，1:Unix，2:Windows）
     * 
     * @param osType 
     */
    public void setOsType(int osType)
    {
        this.osType = osType;
    }


    
    /**
     * 获取：运行时的JDK版本
     */
    public String getJavaVersion()
    {
        return javaVersion;
    }

    
    
    /**
     * 设置：运行时的JDK版本
     * 
     * @param javaVersion 
     */
    public void setJavaVersion(String javaVersion)
    {
        this.javaVersion = javaVersion;
    }
    
}
