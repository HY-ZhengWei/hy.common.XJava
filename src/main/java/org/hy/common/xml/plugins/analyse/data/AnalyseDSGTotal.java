package org.hy.common.xml.plugins.analyse.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.SerializableDef;
import org.hy.common.xml.XJava;





/**
 * 数据库连接池监控统计类
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-05
 * @version     v1.0
 */
public class AnalyseDSGTotal extends SerializableDef
{

    private static final long serialVersionUID = 3096761169935928427L;
    
    

    /** 主机名称 */
    private String                             hostName;
    
    /** 数据库连接池信息 */
    private Map<String ,DataSourceGroupReport> reports;
    
    
    
    public AnalyseDSGTotal(String i_HostName)
    {
        this.hostName = i_HostName;
        this.reports  = new HashMap<String ,DataSourceGroupReport>();
    }
    
    
    
    public AnalyseDSGTotal()
    {
        this.hostName = "";
        this.reports  = new HashMap<String ,DataSourceGroupReport>();
        
        Map<String ,Object> v_DSGs = XJava.getObjects(DataSourceGroup.class ,false);
        for (Entry<String, Object> v_Item : v_DSGs.entrySet())
        {
            DataSourceGroup v_DSG = (DataSourceGroup)v_Item.getValue();
            
            this.reports.put(v_Item.getKey() ,new DataSourceGroupReport(v_Item.getKey() ,v_DSG));
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
     * 获取：数据库连接池信息
     */
    public Map<String ,DataSourceGroupReport> getReports()
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
     * 设置：数据库连接池信息
     * 
     * @param reports 
     */
    public void setReports(Map<String ,DataSourceGroupReport> reports)
    {
        this.reports = reports;
    }
    
}
