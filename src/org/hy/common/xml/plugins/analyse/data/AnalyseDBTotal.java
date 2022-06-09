package org.hy.common.xml.plugins.analyse.data;

import org.hy.common.Counter;
import org.hy.common.Max;
import org.hy.common.Sum;
import org.hy.common.xml.SerializableDef;





/**
 * 1. 数据库访问量的概要统计数据
 * 2. 数据库组合SQL访问量的概要统计数据
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-01-22
 * @version     v1.0
 *              v2.0  2022-06-09  添加：最大总时长 和 清理缓存的方法
 */
public class AnalyseDBTotal extends SerializableDef
{

    private static final long serialVersionUID = -6574380145543983376L;
    
    
    
    /** 请求量。K:为XSQL的ID */
    private Counter<String> requestCount;
    
    /** 成功量。K:为XSQL的ID */
    private Counter<String> successCount;
    
    /** 读写行数。K:为XSQL的ID */
    private Counter<String> ioRowCount;
    
    /** XSQL应用级触发器的个数。K:为XSQL的ID */
    private Counter<String> triggerCount;
    
    /** 触发器的请求量。K:为XSQL的ID */
    private Counter<String> triggerReqCount;
    
    /** 触发器的成功量。K:为XSQL的ID */
    private Counter<String> triggerSucCount;
    
    /** 最后一次的操作时间。K:为XSQL的ID */
    private Max<String>     maxExecTime;
    
    /** 总时长。K:为XSQL的ID */
    private Sum<String>     totalTimeLen;
    
    /** 最大总时长。K:为XSQL的ID */
    private Max<String>     totalTimeLenMax;
    
    
    
    public AnalyseDBTotal()
    {
        this.requestCount    = new Counter<String>();
        this.successCount    = new Counter<String>();
        this.ioRowCount      = new Counter<String>();
        this.triggerCount    = new Counter<String>();
        this.triggerReqCount = new Counter<String>();
        this.triggerSucCount = new Counter<String>();
        this.maxExecTime     = new Max<String>();
        this.totalTimeLen    = new Sum<String>();
        this.totalTimeLenMax = new Max<String>();
    }
    
    
    
    public void clear()
    {
        this.requestCount   .clear();
        this.successCount   .clear();
        this.ioRowCount     .clear();
        this.triggerCount   .clear();
        this.triggerReqCount.clear();
        this.triggerSucCount.clear();
        this.maxExecTime    .clear();
        this.totalTimeLen   .clear();
        this.totalTimeLenMax.clear();
    }
    
    
    
    /**
     * 获取：请求量。K:为XSQL的ID
     */
    public Counter<String> getRequestCount()
    {
        return requestCount;
    }

    
    
    /**
     * 设置：请求量。K:为XSQL的ID
     * 
     * @param requestCount
     */
    public void setRequestCount(Counter<String> requestCount)
    {
        this.requestCount = requestCount;
    }

    
    
    /**
     * 获取：成功量。K:为XSQL的ID
     */
    public Counter<String> getSuccessCount()
    {
        return successCount;
    }

    
    
    /**
     * 设置：成功量。K:为XSQL的ID
     * 
     * @param successCount
     */
    public void setSuccessCount(Counter<String> successCount)
    {
        this.successCount = successCount;
    }

    
    
    /**
     * 获取：读写行数。K:为XSQL的ID
     */
    public Counter<String> getIoRowCount()
    {
        return ioRowCount;
    }


    
    /**
     * 设置：读写行数。K:为XSQL的ID
     * 
     * @param ioRowCount
     */
    public void setIoRowCount(Counter<String> ioRowCount)
    {
        this.ioRowCount = ioRowCount;
    }


    
    /**
     * 获取：XSQL应用级触发器的个数。K:为XSQL的ID
     */
    public Counter<String> getTriggerCount()
    {
        return triggerCount;
    }


    
    /**
     * 设置：XSQL应用级触发器的个数。K:为XSQL的ID
     * 
     * @param triggerCount
     */
    public void setTriggerCount(Counter<String> triggerCount)
    {
        this.triggerCount = triggerCount;
    }



    /**
     * 获取：触发器的请求量。K:为XSQL的ID
     */
    public Counter<String> getTriggerReqCount()
    {
        return triggerReqCount;
    }


    
    /**
     * 设置：触发器的请求量。K:为XSQL的ID
     * 
     * @param triggerReqCount
     */
    public void setTriggerReqCount(Counter<String> triggerReqCount)
    {
        this.triggerReqCount = triggerReqCount;
    }


    
    /**
     * 获取：触发器的成功量。K:为XSQL的ID
     */
    public Counter<String> getTriggerSucCount()
    {
        return triggerSucCount;
    }


    
    /**
     * 设置：触发器的成功量。K:为XSQL的ID
     * 
     * @param triggerSucCount
     */
    public void setTriggerSucCount(Counter<String> triggerSucCount)
    {
        this.triggerSucCount = triggerSucCount;
    }



    /**
     * 获取：最后一次的操作时间。K:为XSQL的ID
     */
    public Max<String> getMaxExecTime()
    {
        return maxExecTime;
    }

    
    
    /**
     * 设置：最后一次的操作时间。K:为XSQL的ID
     * 
     * @param maxExecTime
     */
    public void setMaxExecTime(Max<String> maxExecTime)
    {
        this.maxExecTime = maxExecTime;
    }

    
    
    /**
     * 获取：总时长。K:为XSQL的ID
     */
    public Sum<String> getTotalTimeLen()
    {
        return totalTimeLen;
    }

    
    
    /**
     * 设置：总时长。K:为XSQL的ID
     * 
     * @param totalTimeLen
     */
    public void setTotalTimeLen(Sum<String> totalTimeLen)
    {
        this.totalTimeLen = totalTimeLen;
    }



    /**
     * 获取：最大总时长。K:为XSQL的ID
     */
    public Max<String> getTotalTimeLenMax()
    {
        return totalTimeLenMax;
    }



    /**
     * 设置：最大总时长。K:为XSQL的ID
     * 
     * @param totalTimeLen
     */
    public void setTotalTimeLenMax(Max<String> totalTimeLenMax)
    {
        this.totalTimeLenMax = totalTimeLenMax;
    }
    
}
