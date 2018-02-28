package org.hy.common.xml.plugins.analyse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hy.common.Help;
import org.hy.common.thread.Job;
import org.hy.common.xml.SerializableDef;
import org.hy.common.xml.XJava;





/**
 * 定时任务监控统计类
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-02-28
 * @version     v1.0
 */
public class AnalyseJobTotal extends SerializableDef
{

    private static final long serialVersionUID = 3096761169935928427L;
    
    

    /** 主机名称 */
    private String             hostName;
    
    /** 定时任务监控信息 */
    private List<JobReport>    reports;
    
    
    
    public AnalyseJobTotal(String i_HostName)
    {
        this.hostName = i_HostName;
        this.reports  = new ArrayList<JobReport>();
    }
    
    
    
    public AnalyseJobTotal()
    {
        this.hostName = "";
        this.reports  = new ArrayList<JobReport>();
        
        Map<String ,Object> v_Jobs = XJava.getObjects(Job.class ,false);
        v_Jobs = Help.toSort(v_Jobs);
        
        for (Entry<String, Object> v_Item : v_Jobs.entrySet())
        {
            this.reports.add(new JobReport(v_Item.getKey() ,(Job)v_Item.getValue()));
        }
    }
    
    
    
    /**
     * 获取：主机名称
     */
    public String getHostName()
    {
        return hostName;
    }

    
    
    /**
     * 获取：定时任务监控信息
     */
    public List<JobReport> getReports()
    {
        return reports;
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
     * 设置：定时任务监控信息
     * 
     * @param reports 
     */
    public void setReports(List<JobReport> reports)
    {
        this.reports = reports;
    }
    
}





/**
 * 定时任务监控用到的信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-02-28
 * @version     v1.0
 */
class JobReport extends SerializableDef
{

    private static final long serialVersionUID = 467967958670829923L;

    /** Job ID */
    private String jobID;
    
    /** 间隔类型 */
    private String intervalType;
    
    /** 间隔长度 */
    private String intervalLen;
    
    /** 最后执行时间 */
    private String lastTime;
    
    /** 计划执行时间 */
    private String nextTime;
    
    /** 描述 */
    private String jobDesc;
    
    
    
    public JobReport(String i_JobID ,Job i_Job)
    {
        this.jobID       = i_JobID;
        this.intervalLen = i_Job.getIntervalLen() + "";
        this.lastTime    = i_Job.getLastTime() == null ? "-" : i_Job.getLastTime().getFullMilli();
        this.nextTime    = i_Job.getNextTime() == null ? "-" : i_Job.getNextTime().getFullMilli();
        this.jobDesc     = i_Job.getTaskDesc();
        
        switch ( i_Job.getIntervalType() )
        {
            case Job.$IntervalType_Second:
                this.intervalType = "秒";   break;
                
            case Job.$IntervalType_Minute:
                this.intervalType = "分钟"; break;
                
            case Job.$IntervalType_Hour:
                this.intervalType = "小时"; break;
                
            case Job.$IntervalType_Day:
                this.intervalType = "天";   break;
                
            case Job.$IntervalType_Week:
                this.intervalType = "周";   break;
                
            case Job.$IntervalType_Month:
                this.intervalType = "月";   break;
                
            default:
                this.intervalType = "手工"; 
                this.intervalLen  = "-";
                break;
        }
    }

    
    /**
     * 获取：Job ID
     */
    public String getJobID()
    {
        return jobID;
    }

    
    /**
     * 获取：间隔类型
     */
    public String getIntervalType()
    {
        return intervalType;
    }
    

    /**
     * 获取：间隔长度
     */
    public String getIntervalLen()
    {
        return intervalLen;
    }
    
    
    /**
     * 获取：最后执行时间
     */
    public String getLastTime()
    {
        return lastTime;
    }
    
    
    /**
     * 获取：计划执行时间
     */
    public String getNextTime()
    {
        return nextTime;
    }

    
    /**
     * 获取：描述
     */
    public String getJobDesc()
    {
        return jobDesc;
    }

    
    /**
     * 设置：Job ID
     * 
     * @param jobID 
     */
    public void setJobID(String jobID)
    {
        this.jobID = jobID;
    }


    /**
     * 设置：间隔类型
     * 
     * @param intervalType 
     */
    public void setIntervalType(String intervalType)
    {
        this.intervalType = intervalType;
    }

    
    /**
     * 设置：间隔长度
     * 
     * @param intervalLen 
     */
    public void setIntervalLen(String intervalLen)
    {
        this.intervalLen = intervalLen;
    }
    
    
    /**
     * 设置：最后执行时间
     * 
     * @param lastTime 
     */
    public void setLastTime(String lastTime)
    {
        this.lastTime = lastTime;
    }

    
    /**
     * 设置：计划执行时间
     * 
     * @param nextTime 
     */
    public void setNextTime(String nextTime)
    {
        this.nextTime = nextTime;
    }

    
    /**
     * 设置：描述
     * 
     * @param jobDesc 
     */
    public void setJobDesc(String jobDesc)
    {
        this.jobDesc = jobDesc;
    }
    
}
