package org.hy.common.xml.plugins.analyse.data;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.hy.common.Counter;
import org.hy.common.Help;
import org.hy.common.Max;
import org.hy.common.Sum;
import org.hy.common.xml.SerializableDef;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.log.LoggerMethod;





/**
 * 日志引擎的监控统计类
 *
 * @author      ZhengWei(HY)
 * @createDate  2020-06-17
 * @version     v1.0
 *              v2.0  2026-01-07  添加：最大用时的日志统计
 *              v3.0  2026-01-17  添加：离散度的计算
 *                                优化：用新的平均用时计算方法
 */
public class AnalyseLoggerTotal extends SerializableDef
{

    private static final long serialVersionUID = 3096761169935928427L;
    
    

    /** 日志引擎监控信息 */
    private Map<String ,LoggerReport>    reports;
    
    
    
    
    /**
     * 计算平均业务用时（单位：毫秒）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-22
     * @version     v1.0
     *
     * @param i_LoggerReport
     * @return
     */
    public static double calcExecAvgTime(LoggerReport i_Report)
    {
        // 业务累计用时 / ((请求数 - 错误数) / 非错误的代码量)
        return Help.division(i_Report.getExecSumTime() ,Help.division(i_Report.getRequestCount() - i_Report.getErrorFatalCount() ,i_Report.getCountNoError()));
    }
    
    
    
    public AnalyseLoggerTotal()
    {
        this.reports = new Hashtable<String ,LoggerReport>();
    }
    
    
    
    /**
     * 生成本机的日志统计
     *
     * @author      ZhengWei(HY)
     * @createDate  2020-06-17
     * @version     v1.0
     *
     * @param i_TotalType    统计类型(class、method、lineNumber)
     */
    public AnalyseLoggerTotal(String i_TotalType)
    {
        this.reports  = new Hashtable<String ,LoggerReport>();
        
        // 按“方法”分组统计。(默认分组方式)
        if ( Help.isNull(i_TotalType) || "method".equalsIgnoreCase(i_TotalType) )
        {
            for (Map.Entry<String, List<Logger>> v_ClassForLoggers : Logger.getLoggers().entrySet())
            {
                for (Logger v_Logger : v_ClassForLoggers.getValue())
                {
                    Counter<String> v_MethodCounter        = new Counter<String>();   // 代码量：总代码量
                    Counter<String> v_MethodCounterNoError = new Counter<String>();   // 代码量：非异常、非警告的代码量
                    Counter<String> v_MethodRequestCounter = new Counter<String>();   // 实际执行的次数
                    Counter<String> v_MethodErrorCounter   = new Counter<String>();   // 实际发生异常的次数
                    Counter<String> v_MethodWarnCounter    = new Counter<String>();   // 实际发生警告的次数
                    Max<String>     v_LastTimes            = new Max<String>();
                    
                    for (Map.Entry<String, Long> v_Method : v_Logger.getRequestCount().entrySet())
                    {
                        // 日志级别:方法名称:代码行 
                        String [] v_MInfos     = v_Method.getKey().split(":");
                        long      v_ErrorCount = 0L; 
                        long      v_WarnCount  = 0L;
                        
                        if ( "warn".equalsIgnoreCase(v_MInfos[0]) )
                        {
                            v_WarnCount = v_Method.getValue();
                            v_MethodCounterNoError.put(v_MInfos[1] ,0L);
                        }
                        else if ( "error".equalsIgnoreCase(v_MInfos[0]) || "fatal".equalsIgnoreCase(v_MInfos[0]) )
                        {
                            v_ErrorCount = v_Method.getValue();
                            v_MethodCounterNoError.put(v_MInfos[1] ,0L);
                        }
                        else
                        {
                            v_MethodCounterNoError.put(v_MInfos[1] ,1L);
                        }
                        
                        v_MethodCounter       .put(v_MInfos[1] ,1L);
                        v_MethodRequestCounter.put(v_MInfos[1] ,v_Method.getValue());
                        v_MethodErrorCounter  .put(v_MInfos[1] ,v_ErrorCount);
                        v_MethodWarnCounter   .put(v_MInfos[1] ,v_WarnCount);
                        v_LastTimes           .put(v_MInfos[1] ,v_Logger.getRequestTime().get(v_Method.getKey()));
                    }
                    
                    for (Map.Entry<String, Long> v_Method : v_MethodRequestCounter.entrySet())
                    {
                        LoggerReport v_Data       = new LoggerReport();
                        String       v_MethodName = v_Method.getKey();
                        
                        v_Data.setClassName(      v_ClassForLoggers.getKey());
                        v_Data.setMethodName(     v_Method              .getKey());
                        v_Data.setCount(          v_MethodCounter       .get(v_MethodName));
                        v_Data.setCountNoError(   v_MethodCounterNoError.get(v_MethodName));
                        v_Data.setRequestCount(   v_Method              .getValue());
                        v_Data.setErrorFatalCount(v_MethodErrorCounter  .get(v_MethodName));
                        v_Data.setWarnCount(      v_MethodWarnCounter   .get(v_MethodName));
                        v_Data.setLastTime(       v_LastTimes           .get(v_MethodName).longValue());
                        v_Data.setId(v_Data.getClassName() + v_Data.getMethodName());
                        
                        Long v_ExecSumTimes = v_Logger.getMethodExecSumTimes().get(v_MethodName);
                        if ( v_ExecSumTimes != null )
                        {
                            LoggerMethod v_MethodExceDatas = v_Logger.getMethodExecDatas().get(v_MethodName);
                            v_Data.setExecSumTime(v_Logger.getMethodExecSumTimes().get(v_MethodName));
                            v_Data.setExecMaxTime(v_Logger.getMethodExecMaxTimes().get(v_MethodName).longValue());
                            v_Data.setExecAvgTime(v_MethodExceDatas.getExecAvgTime());  // 2026-01-17 丢用旧算法：calcExecAvgTime(v_Data)
                            v_Data.setDispersion( v_MethodExceDatas.getDispersion());
                        }
                        else
                        {
                            v_Data.setExecSumTime(-1L);
                            v_Data.setExecMaxTime(-1L);
                            v_Data.setExecAvgTime(-1D);
                            v_Data.setDispersion( -1D);
                        }
                        
                        this.reports.put(v_Data.getId() ,v_Data);
                    }
                }
            }
        }
        // 按“类”分组统计
        else if ( "class".equalsIgnoreCase(i_TotalType) )
        {
            for (Map.Entry<String, List<Logger>> v_ClassForLoggers : Logger.getLoggers().entrySet())
            {
                Counter<String> v_ClassCounter        = new Counter<String>();   // 代码量：总代码量
                Counter<String> v_ClassCounterNoError = new Counter<String>();   // 代码量：非异常、非警告的代码量
                Counter<String> v_ClassRequestCounter = new Counter<String>();   // 实际执行的次数
                Counter<String> v_MethodErrorCounter  = new Counter<String>();   // 实际发生异常的次数
                Counter<String> v_MethodWarnCounter   = new Counter<String>();   // 实际发生警告的次数
                Max<String>     v_LastTimes           = new Max<String>();
                Sum<String>     v_ExecSumTimes        = new Sum<String>();
                Max<String>     v_ExecMaxTimes        = new Max<String>();
                
                for (Logger v_Logger : v_ClassForLoggers.getValue())
                {
                    for (Map.Entry<String, Long> v_Method : v_Logger.getRequestCount().entrySet())
                    {
                        String [] v_MInfos     = v_Method.getKey().split(":");
                        long      v_ErrorCount = 0L; 
                        long      v_WarnCount  = 0L; 
                        
                        if ( "warn".equalsIgnoreCase(v_MInfos[0]) )
                        {
                            v_WarnCount = v_Method.getValue();
                            v_ClassCounterNoError.put(v_MInfos[1] ,0L);
                        }
                        else if ( "error".equalsIgnoreCase(v_MInfos[0]) || "fatal".equalsIgnoreCase(v_MInfos[0]) )
                        {
                            v_ErrorCount = v_Method.getValue();
                            v_ClassCounterNoError.put(v_ClassForLoggers.getKey() ,0L);
                        }
                        else
                        {
                            v_ClassCounterNoError.put(v_ClassForLoggers.getKey() ,1L);
                        }
                        
                        v_ClassCounter       .put(v_ClassForLoggers.getKey() ,1L);
                        v_ClassRequestCounter.put(v_ClassForLoggers.getKey() ,v_Method.getValue());
                        v_MethodErrorCounter .put(v_ClassForLoggers.getKey() ,v_ErrorCount);
                        v_MethodWarnCounter  .put(v_ClassForLoggers.getKey() ,v_WarnCount);
                        v_LastTimes          .put(v_ClassForLoggers.getKey() ,v_Logger.getRequestTime().get(v_Method.getKey()));
                        v_ExecSumTimes       .put(v_ClassForLoggers.getKey() ,v_Logger.getMethodExecSumTimes().getSumValue());
                        v_ExecMaxTimes       .put(v_ClassForLoggers.getKey() ,v_Logger.getMethodExecMaxTimes().getMaxValue());
                    }
                }
                
                for (Map.Entry<String, Long> v_Class : v_ClassRequestCounter.entrySet())
                {
                    LoggerReport v_Data = new LoggerReport();
                    
                    v_Data.setClassName(      v_ClassForLoggers.getKey());
                    v_Data.setCount(          v_ClassCounter.get(v_Class.getKey()));
                    v_Data.setCountNoError(Help.NVL(v_ClassCounterNoError.get(v_Class.getKey())));
                    v_Data.setRequestCount(                            v_Class.getValue());
                    v_Data.setErrorFatalCount(v_MethodErrorCounter.get(v_Class.getKey()));
                    v_Data.setWarnCount(      v_MethodWarnCounter .get(v_Class.getKey()));
                    v_Data.setLastTime(       v_LastTimes.get(         v_Class.getKey()).longValue());
                    v_Data.setId(v_Data.getClassName());
                    
                    Object v_IsHaveValue = v_ExecSumTimes.get(v_ClassForLoggers.getKey());
                    if ( v_IsHaveValue != null )
                    {
                        v_Data.setExecSumTime(v_ExecSumTimes.get(v_ClassForLoggers.getKey()).longValue());
                        v_Data.setExecMaxTime(v_ExecMaxTimes.get(v_ClassForLoggers.getKey()).longValue());
                        v_Data.setExecAvgTime(calcExecAvgTime(v_Data));
                        v_Data.setDispersion(-1D);
                    }
                    else
                    {
                        v_Data.setExecSumTime(-1L);
                        v_Data.setExecMaxTime(-1L);
                        v_Data.setExecAvgTime(-1D);
                        v_Data.setDispersion( -1D);
                    }
                    
                    this.reports.put(v_Data.getId() ,v_Data);
                }
            }
        }
        // 按“日志输出代码行”统计
        else 
        {
            for (Map.Entry<String, List<Logger>> v_ClassForLoggers : Logger.getLoggers().entrySet())
            {
                for (Logger v_Logger : v_ClassForLoggers.getValue())
                {
                    for (Map.Entry<String, Long> v_Method : v_Logger.getRequestCount().entrySet())
                    {
                        String [] v_MInfos       = v_Method.getKey().split(":");
                        long      v_ErrorCount   = 0L; 
                        long      v_WarnCount    = 0L; 
                        long      v_CountNoError = 0L;
                        
                        if ( "warn".equalsIgnoreCase(v_MInfos[0]) )
                        {
                            v_WarnCount = v_Method.getValue();
                        }
                        else if ( "error".equalsIgnoreCase(v_MInfos[0]) || "fatal".equalsIgnoreCase(v_MInfos[0]) )
                        {
                            v_ErrorCount = v_Method.getValue();
                        }
                        else
                        {
                            v_CountNoError = 1L;
                        }
                        
                        LoggerReport v_Data = new LoggerReport();
                        
                        v_Data.setClassName( v_ClassForLoggers.getKey());
                        v_Data.setLevelName( v_MInfos[0]);
                        v_Data.setMethodName(v_MInfos[1]);
                        v_Data.setLineNumber(v_MInfos[2]);
                        v_Data.setCount(1L);
                        v_Data.setCountNoError(   v_CountNoError);
                        v_Data.setRequestCount(   v_Method.getValue());
                        v_Data.setErrorFatalCount(v_ErrorCount);
                        v_Data.setWarnCount(      v_WarnCount);
                        v_Data.setLastTime(v_Logger.getRequestTime().get(v_Method.getKey()));
                        v_Data.setId(v_Data.getClassName() + v_Data.getMethodName() + v_Data.getLineNumber());
                        
                        v_Data.setExecSumTime(-1L);
                        v_Data.setExecMaxTime(-1L);
                        v_Data.setExecAvgTime(-1D);
                        v_Data.setDispersion( -1D);
                        
                        this.reports.put(v_Data.getId() ,v_Data);
                    }
                }
            }
        }
    }
    
    
    
    /**
     * 获取：日志引擎的监控信息
     */
    public Map<String ,LoggerReport> getReports()
    {
        return reports;
    }
    

    
    /**
     * 设置：日志引擎的监控信息
     * 
     * @param reports 
     */
    public void setReports(Map<String ,LoggerReport> reports)
    {
        this.reports = reports;
    }
    
}
