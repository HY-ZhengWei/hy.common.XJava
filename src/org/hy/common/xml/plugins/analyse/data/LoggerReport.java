package org.hy.common.xml.plugins.analyse.data;

import org.hy.common.xml.SerializableDef;





/**
 * 日志引擎的统计监控
 *
 * @author      ZhengWei(HY)
 * @createDate  2020-06-13
 * @version     v1.0
 *              v2.0  2021-01-27  添加：警告级的日志统计
 */
public class LoggerReport extends SerializableDef
{

    private static final long serialVersionUID = 2095080687077872408L;
    
    /** 标记主键 */
    private String id;
    
    /** 类名称 */
    private String className;
    
    /** 方法名称 */
    private String methodName;
    
    /** 日志输出所在代码行 */
    private String lineNumber;
    
    /** 日志级别 */
    private String levelName;
    
    /** 日志代码量 */
    private long   count;
    
    /** 日志代码量（不包含Error级、Fatal级的、Warn级的） */
    private long   countNoError;
    
    /** 日志执行量 */
    private long   requestCount;
    
    /** Error级日志量（包含Fatal级的） */
    private long   errorFatalCount;
    
    /** Warn级日志量 */
    private long   warnCount;
    
    /** 最后时间 */
    private long   lastTime;
    
    /** 方法累计用时 */
    private long   execSumTime;
    
    /** 方法平均用时 */
    private double execAvgTime;
    
    
    
    /**
     * 获取：标记主键
     */
    public String getId()
    {
        return id;
    }


    /**
     * 设置：标记主键
     * 
     * @param id 
     */
    public void setId(String id)
    {
        this.id = id;
    }


    /**
     * 获取：类名称
     */
    public String getClassName()
    {
        return className;
    }

    
    /**
     * 获取：方法名称
     */
    public String getMethodName()
    {
        return methodName;
    }

    
    /**
     * 获取：日志输出所在代码行
     */
    public String getLineNumber()
    {
        return lineNumber;
    }

    
    /**
     * 获取：日志级别
     */
    public String getLevelName()
    {
        return levelName;
    }

    
    /**
     * 获取：日志代码量
     */
    public long getCount()
    {
        return count;
    }

    
    /**
     * 获取：日志执行量
     */
    public long getRequestCount()
    {
        return requestCount;
    }

    
    /**
     * 获取：最后时间
     */
    public long getLastTime()
    {
        return lastTime;
    }

    
    /**
     * 设置：类名称
     * 
     * @param className 
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    
    /**
     * 设置：方法名称
     * 
     * @param methodName 
     */
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    
    /**
     * 设置：日志输出所在代码行
     * 
     * @param lineNumber 
     */
    public void setLineNumber(String lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    
    /**
     * 设置：日志级别
     * 
     * @param levelName 
     */
    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }

    
    /**
     * 设置：日志代码量
     * 
     * @param count 
     */
    public void setCount(long count)
    {
        this.count = count;
    }

    
    /**
     * 设置：日志执行量
     * 
     * @param requestCount 
     */
    public void setRequestCount(long requestCount)
    {
        this.requestCount = requestCount;
    }

    
    /**
     * 设置：最后时间
     * 
     * @param lastTime 
     */
    public void setLastTime(long lastTime)
    {
        this.lastTime = lastTime;
    }

    
    /**
     * 获取：Error级日志量（包含Fatal级的）
     */
    public long getErrorFatalCount()
    {
        return errorFatalCount;
    }

    
    /**
     * 设置：Error级日志量（包含Fatal级的）
     * 
     * @param errorFatalCount 
     */
    public void setErrorFatalCount(long errorFatalCount)
    {
        this.errorFatalCount = errorFatalCount;
    }

    
    /**
     * 获取：方法累计用时
     */
    public long getExecSumTime()
    {
        return execSumTime;
    }

    
    /**
     * 设置：方法累计用时
     * 
     * @param execSumTime 
     */
    public void setExecSumTime(long execSumTime)
    {
        this.execSumTime = execSumTime;
    }

    
    /**
     * 获取：方法平均用时
     */
    public double getExecAvgTime()
    {
        return execAvgTime;
    }

    
    /**
     * 设置：方法平均用时
     * 
     * @param execAvgTime 
     */
    public void setExecAvgTime(double execAvgTime)
    {
        this.execAvgTime = execAvgTime;
    }

    
    /**
     * 获取：日志代码量（不包含Error级、Fatal级的、Warn级的）
     */
    public long getCountNoError()
    {
        return countNoError;
    }

    
    /**
     * 设置：日志代码量（不包含Error级、Fatal级的、Warn级的）
     * 
     * @param countNoError 
     */
    public void setCountNoError(long countNoError)
    {
        this.countNoError = countNoError;
    }

    
    /**
     * 获取：Warn级日志量
     */
    public long getWarnCount()
    {
        return warnCount;
    }

    
    /**
     * 设置：Warn级日志量
     * 
     * @param warnCount 
     */
    public void setWarnCount(long warnCount)
    {
        this.warnCount = warnCount;
    }
    
}
