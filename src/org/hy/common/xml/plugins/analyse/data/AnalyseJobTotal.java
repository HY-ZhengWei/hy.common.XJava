package org.hy.common.xml.plugins.analyse.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hy.common.thread.Job;
import org.hy.common.thread.JobReport;
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
