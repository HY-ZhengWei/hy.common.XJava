package org.hy.common.xml.plugins.analyse.data;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.SerializableDef;





/**
 * 数据库连接池信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-05
 * @version     v1.0
 */
public class DataSourceGroupReport extends SerializableDef
{
    private static final long serialVersionUID = -6863414356276816702L;
    
    
    /** 对象ID */
    private String       dsgID;
    
    /** 数据库产品类型 */
    private String       dbProductType;
    
    /** 主备连接池数量 */
    private int          dataSourcesSize;
    
    /** 活动连接数量 */
    private long         connActiveCount;
    
    /** 连接使用峰值 */
    private long         connMaxUseCount;
    
    /** 最后一次正常连接的时间 */
    private String       connLastTime;
    
    /** 数据库连接池情况（正常、异常） */
    private String       dsgStatus;
    
    /** 数据库基本信息 */
    private List<String> databases;
    
    
    
    public DataSourceGroupReport()
    {
        this.dsgID           = "";
        this.dbProductType   = "";
        this.dataSourcesSize = 0;
        this.connActiveCount = 0;
        this.connMaxUseCount = 0;
        this.connLastTime    = ""; 
        this.dsgStatus       = "";
        this.databases       = new ArrayList<String>();
    }
    
    
    
    public DataSourceGroupReport(String i_DSGID ,DataSourceGroup i_DSG)
    {
        this.dsgID           = i_DSGID;
        this.dbProductType   = i_DSG.getDbProductType();
        this.dataSourcesSize = i_DSG.size();
        this.connActiveCount = i_DSG.getConnActiveCount();
        this.connMaxUseCount = i_DSG.getConnMaxUseCount();
        this.connLastTime    = i_DSG.getConnLastTime() == null ? "" : i_DSG.getConnLastTime().getFull(); 
        this.dsgStatus       = i_DSG.isException() ? "异常" : "";
        this.databases       = new ArrayList<String>();
    }



    
    /**
     * 获取：对象ID
     */
    public String getDsgID()
    {
        return dsgID;
    }
    

    
    /**
     * 获取：数据库产品类型
     */
    public String getDbProductType()
    {
        return dbProductType;
    }
    


    /**
     * 获取：主备连接池数量
     */
    public int getDataSourcesSize()
    {
        return dataSourcesSize;
    }
    

    
    /**
     * 获取：活动连接数量
     */
    public long getConnActiveCount()
    {
        return connActiveCount;
    }
    


    /**
     * 获取：连接使用峰值
     */
    public long getConnMaxUseCount()
    {
        return connMaxUseCount;
    }
    

    
    /**
     * 获取：最后一次正常连接的时间
     */
    public String getConnLastTime()
    {
        return connLastTime;
    }
    

    
    /**
     * 获取：数据库连接池情况（正常、异常）
     */
    public String getDsgStatus()
    {
        return dsgStatus;
    }


    
    /**
     * 获取：数据库基本信息
     */
    public List<String> getDatabases()
    {
        return databases;
    }


    
    /**
     * 设置：对象ID
     * 
     * @param dsgID 
     */
    public void setDsgID(String dsgID)
    {
        this.dsgID = dsgID;
    }
    

    
    /**
     * 设置：数据库产品类型
     * 
     * @param dbProductType 
     */
    public void setDbProductType(String dbProductType)
    {
        this.dbProductType = dbProductType;
    }
    

    
    /**
     * 设置：主备连接池数量
     * 
     * @param dataSourcesSize 
     */
    public void setDataSourcesSize(int dataSourcesSize)
    {
        this.dataSourcesSize = dataSourcesSize;
    }
    

    
    /**
     * 设置：活动连接数量
     * 
     * @param connActiveCount 
     */
    public void setConnActiveCount(long connActiveCount)
    {
        this.connActiveCount = connActiveCount;
    }
    

    
    /**
     * 设置：连接使用峰值
     * 
     * @param connMaxUseCount 
     */
    public void setConnMaxUseCount(long connMaxUseCount)
    {
        this.connMaxUseCount = connMaxUseCount;
    }
    

    
    /**
     * 设置：最后一次正常连接的时间
     * 
     * @param connLastTime 
     */
    public void setConnLastTime(String connLastTime)
    {
        this.connLastTime = connLastTime;
    }
    

    
    /**
     * 设置：数据库连接池情况（正常、异常）
     * 
     * @param dsgStatus 
     */
    public void setDsgStatus(String dsgStatus)
    {
        this.dsgStatus = dsgStatus;
    }
    

    
    /**
     * 设置：数据库基本信息
     * 
     * @param databases 
     */
    public void setDatabases(List<String> databases)
    {
        this.databases = databases;
    }
    
}
