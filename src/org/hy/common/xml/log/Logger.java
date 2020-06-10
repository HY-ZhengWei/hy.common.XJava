package org.hy.common.xml.log;

import java.lang.reflect.Method;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StaticReflect;
import org.hy.common.StringHelp;





/**
 * 日志引擎
 * 
 * 同时支持Log4j 1.x 和 Log4j 2.x 两个版本的功能。
 * 
 * 同时支持SLF4J日志引擎的类库。
 * 
 * 同时支持没有任何 Log4j 引包的情况。
 * 
 * 
 * 注意1：当 Log4j 2.x 与 Log4j 1.x 两版本的引包均存在时，优先使用高版本 Log4j 2.x 。
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-05-27
 * @version     v1.0
 *              v2.0  2020-01-06  添加：在没有任何Log4j版本时，可采用System.out.println()方法输出
 *              v3.0  2020-06-09  添加：info(String ,Throwable)系列的日志方法。建议人：邹德福
 *                                添加：info(Throwable)系列的日志方法。建议人：邹德福
 *                                添加：info(Object)系列的日志方法。建议人：邹德福
 *                                添加：支持SLF4J的日志类库。建议人：邹德福
 *              v4.0  2020-06-10  添加：Log4j出于性能考虑，公开了判定日志级别的方法。建议人：李浩
 */
public class Logger
{
    
    /** 全局控制参数：是否启用SLF4J。目标对象实例化前的有效，日志对象实例化后，修改是没有任何效果的 */
    public static boolean $IsEnabled_SLF4J = true;
    
    /** 全局控制参数：是否启用Log4J。目标对象实例化前的有效，日志对象实例化后，修改是没有任何效果的 */
    public static boolean $IsEnabled_Log4J = true;
    
    /** 全局控制参数：是否启用System.out.println输出日志。目标对象实例化前的有效，日志对象实例化后，修改是没有任何效果的 */
    public static boolean $IsEnabled_Print = false;
    
    
    /** 常量：日志引擎的类型为：SLF4J */
    public static final int $LogType_SLF4J = 1;
    
    /** 常量：日志引擎的类型为：Log4J */
    public static final int $LogType_Log4J = 2;
    
    
    
    private Object   log;
    
    /** 日志实现类库的类型（1：SLF4J  2：Log4J） */
    private int      logType;
    
    /** 日志实现类库的版本 */
    private int      logVersion;
    
    /** 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了 */
    private Method   fatal;
    
    private Method   fatalThrowable;
    
    private Method   fatalMarker;
    
    private Method   fatalMarkerThrowable;
    
    private Method   fatalIsEnabled;
    
    /** 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别 */
    private Method   error;
    
    private Method   errorThrowable;
    
    private Method   errorMarker;
    
    private Method   errorMarkerThrowable;
    
    private Method   errorIsEnabled;
    
    /** 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。 */
    private Method   warn;
    
    private Method   warnThrowable;
    
    private Method   warnMarker;
    
    private Method   warnMarkerThrowable;
    
    private Method   warnIsEnabled;
    
    /** 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息 */
    private Method   info;
    
    private Method   infoThrowable;
    
    private Method   infoMarker;
    
    private Method   infoMarkerThrowable;
    
    private Method   infoIsEnabled;
    
    /** 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息 */
    private Method   debug;
    
    private Method   debugThrowable;
    
    private Method   debugMarker;
    
    private Method   debugMarkerThrowable;
    
    private Method   debugIsEnabled;
    
    /** 最低的日志级别 */
    private Method   trace;
    
    private Method   traceThrowable;
    
    private Method   traceMarker;
    
    private Method   traceMarkerThrowable;
    
    private Method   traceIsEnabled;
    
    /** 没有任何Log4j版本时，是否采用System.out.println()方法输出 */
    private Class<?> logClass;  
    
    
    
    /**
     * 构建日志类。可用于替换Log4J的构建
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Class
     * @return
     */
    public static Logger getLogger(Class<?> i_Class)
    {
        return new Logger(i_Class ,null);
    }
    
    
    
    /**
     * 构建日志类。可用于替换Log4J的构建
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Class
     * @param i_IsPrintln  没有任何Log4j版本时，是否采用System.out.println()方法输出
     * @return
     */
    public static Logger getLogger(Class<?> i_Class ,Boolean i_IsPrintln)
    {
        return new Logger(i_Class ,i_IsPrintln);
    }
    
    
    
    
    /**
     * 构建日志类
     *
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Class
     */
    public Logger(Class<?> i_Class)
    {
        this(i_Class ,null);
    }
    
    
    
    
    /**
     * 构建日志类
     *
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Class
     * @param i_IsPrintln  没有任何Log4j版本时，是否采用System.out.println()方法输出
     */
    public Logger(Class<?> i_Class ,Boolean i_IsPrintln)
    {
        Class<?> v_LogClass    = null;
        Class<?> v_LogManager  = null;
        Class<?> v_MarkerClass = null;
        this.logType           = -1;
        this.logVersion        = -1;
        
        if ( $IsEnabled_SLF4J )
        {
            try
            {
                // SLF4J
                v_LogClass      = Help.forName("org.slf4j.Logger");
                v_LogManager    = Help.forName("org.slf4j.LoggerFactory");
                v_MarkerClass   = Help.forName("org.slf4j.Marker");
                this.logType    = $LogType_SLF4J;
                this.logVersion = 1;
            }
            catch (Exception exce)
            {
                // Nothing.
            }
        }
        
        if ( $IsEnabled_Log4J )
        {
            if ( v_LogClass == null )
            {
                try
                {
                    // Log4j 2.x 的版本
                    v_LogClass      = Help.forName("org.apache.logging.log4j.Logger");
                    v_LogManager    = Help.forName("org.apache.logging.log4j.LogManager");
                    v_MarkerClass   = Help.forName("org.apache.logging.log4j.Marker");
                    this.logType    = $LogType_Log4J;
                    this.logVersion = 2;
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
            }
            
            if ( v_LogClass == null )
            {
                try
                {
                    // Log4j 1.x 的版本
                    v_LogClass      = Help.forName("org.apache.log4j.Logger");
                    v_LogManager    = Help.forName("org.apache.log4j.LogManager");
                    v_MarkerClass   = null;
                    this.logType    = $LogType_Log4J;
                    this.logVersion = 1;
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
            }
        }
        
        if ( v_LogClass != null )
        {
            try
            {
                Method v_Methd = v_LogManager.getMethod("getLogger" ,Class.class);
                this.log = StaticReflect.invoke(v_Methd ,i_Class);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            if ( this.log != null )
            {
                try
                {
                    if ( this.logType == $LogType_SLF4J )
                    {
                        this.fatal                = v_LogClass.getMethod("error" ,String.class ,Object[].class);
                        this.error                = this.fatal;
                        this.warn                 = v_LogClass.getMethod("warn"  ,String.class ,Object[].class);
                        this.info                 = v_LogClass.getMethod("info"  ,String.class ,Object[].class);
                        this.debug                = v_LogClass.getMethod("debug" ,String.class ,Object[].class);
                        this.trace                = v_LogClass.getMethod("trace" ,String.class ,Object[].class);
                                                  
                        this.fatalThrowable       = v_LogClass.getMethod("error" ,String.class ,Throwable.class);
                        this.errorThrowable       = this.fatalThrowable;
                        this.warnThrowable        = v_LogClass.getMethod("warn"  ,String.class ,Throwable.class);
                        this.infoThrowable        = v_LogClass.getMethod("info"  ,String.class ,Throwable.class);
                        this.debugThrowable       = v_LogClass.getMethod("debug" ,String.class ,Throwable.class);
                        this.traceThrowable       = v_LogClass.getMethod("trace" ,String.class ,Throwable.class);
                                                  
                        this.fatalMarker          = v_LogClass.getMethod("error" ,v_MarkerClass ,String.class ,Object[].class);
                        this.errorMarker          = this.fatalMarker;
                        this.warnMarker           = v_LogClass.getMethod("warn"  ,v_MarkerClass ,String.class ,Object[].class);
                        this.infoMarker           = v_LogClass.getMethod("info"  ,v_MarkerClass ,String.class ,Object[].class);
                        this.debugMarker          = v_LogClass.getMethod("debug" ,v_MarkerClass ,String.class ,Object[].class);
                        this.traceMarker          = v_LogClass.getMethod("trace" ,v_MarkerClass ,String.class ,Object[].class);
                        
                        this.fatalMarkerThrowable = v_LogClass.getMethod("error" ,v_MarkerClass ,String.class ,Throwable.class);
                        this.errorMarkerThrowable = this.fatalMarkerThrowable;
                        this.warnMarkerThrowable  = v_LogClass.getMethod("warn"  ,v_MarkerClass ,String.class ,Throwable.class);
                        this.infoMarkerThrowable  = v_LogClass.getMethod("info"  ,v_MarkerClass ,String.class ,Throwable.class);
                        this.debugMarkerThrowable = v_LogClass.getMethod("debug" ,v_MarkerClass ,String.class ,Throwable.class);
                        this.traceMarkerThrowable = v_LogClass.getMethod("trace" ,v_MarkerClass ,String.class ,Throwable.class);
                        
                        this.fatalIsEnabled       = null;
                        this.errorIsEnabled       = null;
                        this.warnIsEnabled        = null;
                        this.infoIsEnabled        = null;
                        this.debugIsEnabled       = null;
                        this.traceIsEnabled       = null;
                    }
                    else if ( this.logType == $LogType_Log4J )
                    {
                        if ( this.logVersion == 1 )
                        {
                            this.fatal                = v_LogClass.getMethod("fatal" ,Object.class);
                            this.error                = v_LogClass.getMethod("error" ,Object.class);
                            this.warn                 = v_LogClass.getMethod("warn"  ,Object.class);
                            this.info                 = v_LogClass.getMethod("info"  ,Object.class);
                            this.debug                = v_LogClass.getMethod("debug" ,Object.class);
                            this.trace                = null;
                                                      
                            this.fatalThrowable       = v_LogClass.getMethod("fatal" ,Object.class ,Throwable.class);
                            this.errorThrowable       = v_LogClass.getMethod("error" ,Object.class ,Throwable.class);
                            this.warnThrowable        = v_LogClass.getMethod("warn"  ,Object.class ,Throwable.class);
                            this.infoThrowable        = v_LogClass.getMethod("info"  ,Object.class ,Throwable.class);
                            this.debugThrowable       = v_LogClass.getMethod("debug" ,Object.class ,Throwable.class);
                            this.traceThrowable       = null;
                                                      
                            this.fatalMarker          = null;
                            this.errorMarker          = null;
                            this.warnMarker           = null;
                            this.infoMarker           = null;
                            this.debugMarker          = null;
                            this.traceMarker          = null;
                            
                            this.fatalMarkerThrowable = null;
                            this.errorMarkerThrowable = null;
                            this.warnMarkerThrowable  = null;
                            this.infoMarkerThrowable  = null;
                            this.debugMarkerThrowable = null;
                            this.traceMarkerThrowable = null;
                            
                            this.fatalIsEnabled       = null;
                            this.errorIsEnabled       = null;
                            this.warnIsEnabled        = null;
                            this.infoIsEnabled        = null;
                            this.debugIsEnabled       = v_LogClass.getMethod("isDebugEnabled");
                            this.traceIsEnabled       = null;
                        }
                        else if ( this.logVersion == 2 )
                        {
                            this.fatal                = v_LogClass.getMethod("fatal" ,String.class ,Object[].class);
                            this.error                = v_LogClass.getMethod("error" ,String.class ,Object[].class);
                            this.warn                 = v_LogClass.getMethod("warn"  ,String.class ,Object[].class);
                            this.info                 = v_LogClass.getMethod("info"  ,String.class ,Object[].class);
                            this.debug                = v_LogClass.getMethod("debug" ,String.class ,Object[].class);
                            this.trace                = v_LogClass.getMethod("trace" ,String.class ,Object[].class);
                                                      
                            this.fatalThrowable       = v_LogClass.getMethod("fatal" ,Object.class ,Throwable.class);
                            this.errorThrowable       = v_LogClass.getMethod("error" ,Object.class ,Throwable.class);
                            this.warnThrowable        = v_LogClass.getMethod("warn"  ,Object.class ,Throwable.class);
                            this.infoThrowable        = v_LogClass.getMethod("info"  ,Object.class ,Throwable.class);
                            this.debugThrowable       = v_LogClass.getMethod("debug" ,Object.class ,Throwable.class);
                            this.traceThrowable       = v_LogClass.getMethod("trace" ,Object.class ,Throwable.class);
                                                      
                            this.fatalMarker          = v_LogClass.getMethod("fatal" ,v_MarkerClass ,String.class ,Object[].class);
                            this.errorMarker          = v_LogClass.getMethod("error" ,v_MarkerClass ,String.class ,Object[].class);
                            this.warnMarker           = v_LogClass.getMethod("warn"  ,v_MarkerClass ,String.class ,Object[].class);
                            this.infoMarker           = v_LogClass.getMethod("info"  ,v_MarkerClass ,String.class ,Object[].class);
                            this.debugMarker          = v_LogClass.getMethod("debug" ,v_MarkerClass ,String.class ,Object[].class);
                            this.traceMarker          = v_LogClass.getMethod("trace" ,v_MarkerClass ,String.class ,Object[].class);
                            
                            this.fatalMarkerThrowable = v_LogClass.getMethod("error" ,v_MarkerClass ,String.class ,Throwable.class);
                            this.errorMarkerThrowable = this.fatalMarkerThrowable;
                            this.warnMarkerThrowable  = v_LogClass.getMethod("warn"  ,v_MarkerClass ,String.class ,Throwable.class);
                            this.infoMarkerThrowable  = v_LogClass.getMethod("info"  ,v_MarkerClass ,String.class ,Throwable.class);
                            this.debugMarkerThrowable = v_LogClass.getMethod("debug" ,v_MarkerClass ,String.class ,Throwable.class);
                            this.traceMarkerThrowable = v_LogClass.getMethod("trace" ,v_MarkerClass ,String.class ,Throwable.class);
                            
                            this.fatalIsEnabled       = v_LogClass.getMethod("isFatalEnabled");
                            this.errorIsEnabled       = v_LogClass.getMethod("isErrorEnabled");
                            this.warnIsEnabled        = v_LogClass.getMethod("isWarnEnabled");
                            this.infoIsEnabled        = v_LogClass.getMethod("isInfoEnabled");
                            this.debugIsEnabled       = v_LogClass.getMethod("isDebugEnabled");
                            this.traceIsEnabled       = v_LogClass.getMethod("isTraceEnabled");
                        }
                    }
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
        }
        else if ( ($IsEnabled_Print && i_IsPrintln == null) || (i_IsPrintln !=null && i_IsPrintln.booleanValue()) )
        {
            this.logClass = i_Class;
        }
    }
    
    
    
    /**
     * Log4j出于性能考虑，公开了判定日志级别的方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @return
     */
    public boolean isFatalEnabled()
    {
        if ( this.fatalIsEnabled != null )
        {
            try
            {
                return (boolean)this.fatalIsEnabled.invoke(this.log);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return true;
    }
    
    
    
    /**
     * Log4j出于性能考虑，公开了判定日志级别的方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @return
     */
    public boolean isErrorEnabled()
    {
        if ( this.errorIsEnabled != null )
        {
            try
            {
                return (boolean)this.errorIsEnabled.invoke(this.log);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return true;
    }
    
    
    
    /**
     * Log4j出于性能考虑，公开了判定日志级别的方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @return
     */
    public boolean isWarnEnabled()
    {
        if ( this.warnIsEnabled != null )
        {
            try
            {
                return (boolean)this.warnIsEnabled.invoke(this.log);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return true;
    }
    
    
    
    /**
     * Log4j出于性能考虑，公开了判定日志级别的方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @return
     */
    public boolean isInfoEnabled()
    {
        if ( this.infoIsEnabled != null )
        {
            try
            {
                return (boolean)this.infoIsEnabled.invoke(this.log);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return true;
    }
    
    
    
    /**
     * Log4j出于性能考虑，公开了判定日志级别的方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @return
     */
    public boolean isDebugEnabled()
    {
        if ( this.debugIsEnabled != null )
        {
            try
            {
                return (boolean)this.debugIsEnabled.invoke(this.log);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return true;
    }
    
    
    
    /**
     * Log4j出于性能考虑，公开了判定日志级别的方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @return
     */
    public boolean isTraceEnabled()
    {
        if ( this.traceIsEnabled != null )
        {
            try
            {
                return (boolean)this.traceIsEnabled.invoke(this.log);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return true;
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void fatal(final String i_Message)
    {
        this.fatal(i_Message ,"");
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Argument
     */
    public void fatal(final String i_Message ,final String i_Argument)
    {
        if ( this.fatal != null )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J )
                {
                    this.fatal.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                }
                else if ( this.logType == $LogType_Log4J )
                {
                    if ( this.logVersion == 1 )
                    {
                        this.fatal.invoke(this.log ,i_Message + i_Argument);
                    }
                    else
                    {
                        this.fatal.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                    }
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("fatal> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     */
    public void fatal(final Object i_Message)
    {
        if ( this.fatal != null )
        {
            try
            {
                if ( i_Message != null )
                {
                    this.fatal.invoke(this.log ,i_Message.toString());
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("fatal> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Throwable
     */
    public void fatal(final String i_Message, final Throwable i_Throwable) 
    {
        if ( this.fatalThrowable != null )
        {
            try
            {
                this.fatalThrowable.invoke(this.log ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("fatal> " + i_Message);
            i_Throwable.printStackTrace();
        }
        else
        {
            i_Throwable.printStackTrace();
        }
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Throwable
     */
    public void fatal(final Throwable i_Throwable) 
    {
        this.fatal("" ,i_Throwable);
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Arguments
     */
    public void fatal(final Object i_Marker ,String i_Message, final Object ... i_Arguments) 
    {
        if ( this.fatalMarker != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                Object [] v_Arguments = i_Arguments;
                
                if ( v_Arguments == null )
                {
                    v_Arguments = new Object[0];
                }
                
                this.fatalMarker.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( this.fatal != null && i_Marker != null && "java.long.String".equals(i_Marker.getClass().getName()) )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J || (this.logType == $LogType_Log4J && this.logVersion >= 2 ) )
                {
                    Object [] v_Arguments = null;
                    
                    if ( i_Arguments == null )
                    {
                        v_Arguments = new Object[] {i_Message};
                    }
                    else
                    {
                        v_Arguments = new Object[i_Arguments.length + 1];
                        v_Arguments[0] = i_Message;
                        Help.fillArray(i_Arguments ,v_Arguments ,1);
                    }
                    
                    
                    this.fatal.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
                }
                else
                {
                    this.fatal.invoke(this.log ,i_Marker + i_Message + StringHelp.toString(i_Arguments));
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("fatal> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Throwable
     */
    public void fatal(final Object i_Marker ,String i_Message, final Throwable i_Throwable) 
    {
        if ( this.fatalMarkerThrowable != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                this.fatalMarkerThrowable.invoke(this.log ,i_Marker ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("fatal> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void error(final String i_Message)
    {
        this.error(i_Message ,"");
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Argument
     */
    public void error(final String i_Message ,final String i_Argument)
    {
        if ( this.error != null )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J )
                {
                    this.error.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                }
                else if ( this.logType == $LogType_Log4J )
                {
                    if ( this.logVersion == 1 )
                    {
                        this.error.invoke(this.log ,i_Message + i_Argument);
                    }
                    else
                    {
                        this.error.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                    }
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("error> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-06-09
     * @version     v1.0
     *
     * @param i_Message
     */
    public void error(final Object i_Message)
    {
        if ( this.error != null )
        {
            try
            {
                if ( i_Message != null )
                {
                    this.error.invoke(this.log ,i_Message.toString());
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("error> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Throwable
     */
    public void error(final String i_Message, final Throwable i_Throwable) 
    {
        if ( this.errorThrowable != null )
        {
            try
            {
                this.errorThrowable.invoke(this.log ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("error> " + i_Message);
            i_Throwable.printStackTrace();
        }
        else
        {
            i_Throwable.printStackTrace();
        }
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Throwable
     */
    public void error(final Throwable i_Throwable) 
    {
        this.error("" ,i_Throwable);
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Arguments
     */
    public void error(final Object i_Marker ,String i_Message, final Object ... i_Arguments) 
    {
        if ( this.errorMarker != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                Object [] v_Arguments = i_Arguments;
                
                if ( v_Arguments == null )
                {
                    v_Arguments = new Object[0];
                }
                
                this.errorMarker.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( this.error != null && i_Marker != null && "java.long.String".equals(i_Marker.getClass().getName()) )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J || (this.logType == $LogType_Log4J && this.logVersion >= 2 ) )
                {
                    Object [] v_Arguments = null;
                    
                    if ( i_Arguments == null )
                    {
                        v_Arguments = new Object[] {i_Message};
                    }
                    else
                    {
                        v_Arguments = new Object[i_Arguments.length + 1];
                        v_Arguments[0] = i_Message;
                        Help.fillArray(i_Arguments ,v_Arguments ,1);
                    }
                    
                    
                    this.error.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
                }
                else
                {
                    this.error.invoke(this.log ,i_Marker + i_Message + StringHelp.toString(i_Arguments));
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("error> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Throwable
     */
    public void error(final Object i_Marker ,String i_Message, final Throwable i_Throwable) 
    {
        if ( this.errorMarkerThrowable != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                this.errorMarkerThrowable.invoke(this.log ,i_Marker ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("error> " + i_Message);
        }
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void warn(final String i_Message)
    {
        this.warn(i_Message ,"");
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Argument
     */
    public void warn(final String i_Message ,final String i_Argument)
    {
        if ( this.warn != null )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J )
                {
                    this.warn.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                }
                else if ( this.logType == $LogType_Log4J )
                {
                    if ( this.logVersion == 1 )
                    {
                        this.warn.invoke(this.log ,i_Message + i_Argument);
                    }
                    else
                    {
                        this.warn.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                    }
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("warn> " + i_Message);
        }
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void warn(final Object i_Message)
    {
        if ( this.warn != null )
        {
            try
            {
                if ( i_Message != null )
                {
                    this.warn.invoke(this.log ,i_Message.toString());
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("warn> " + i_Message);
        }
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Throwable
     */
    public void warn(final String i_Message, final Throwable i_Throwable) 
    {
        if ( this.warnThrowable != null )
        {
            try
            {
                this.warnThrowable.invoke(this.log ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("warn> " + i_Message);
            i_Throwable.printStackTrace();
        }
        else
        {
            i_Throwable.printStackTrace();
        }
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Throwable
     */
    public void warn(final Throwable i_Throwable) 
    {
        this.warn("" ,i_Throwable);
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Arguments
     */
    public void warn(final Object i_Marker ,String i_Message, final Object ... i_Arguments) 
    {
        if ( this.warnMarker != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                Object [] v_Arguments = i_Arguments;
                
                if ( v_Arguments == null )
                {
                    v_Arguments = new Object[0];
                }
                
                this.warnMarker.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( this.warn != null && i_Marker != null && "java.long.String".equals(i_Marker.getClass().getName()) )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J || (this.logType == $LogType_Log4J && this.logVersion >= 2 ) )
                {
                    Object [] v_Arguments = null;
                    
                    if ( i_Arguments == null )
                    {
                        v_Arguments = new Object[] {i_Message};
                    }
                    else
                    {
                        v_Arguments = new Object[i_Arguments.length + 1];
                        v_Arguments[0] = i_Message;
                        Help.fillArray(i_Arguments ,v_Arguments ,1);
                    }
                    
                    
                    this.warn.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
                }
                else
                {
                    this.warn.invoke(this.log ,i_Marker + i_Message + StringHelp.toString(i_Arguments));
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("warn> " + i_Message);
        }
    }
    
    
    
    /**
     * 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Throwable
     */
    public void warn(final Object i_Marker ,String i_Message, final Throwable i_Throwable) 
    {
        if ( this.warnMarkerThrowable != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                this.warnMarkerThrowable.invoke(this.log ,i_Marker ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("warn> " + i_Message);
        }
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void info(final String i_Message)
    {
        this.info(i_Message ,"");
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Argument
     */
    public void info(final String i_Message ,final String i_Argument)
    {
        if ( this.info != null )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J )
                {
                    this.info.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                }
                else if ( this.logType == $LogType_Log4J )
                {
                    if ( this.logVersion == 1 )
                    {
                        this.info.invoke(this.log ,i_Message + i_Argument);
                    }
                    else
                    {
                        this.info.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                    }
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("info> " + i_Message);
        }
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     */
    public void info(final Object i_Message)
    {
        if ( this.info != null )
        {
            try
            {
                if ( i_Message != null )
                {
                    this.info.invoke(this.log ,i_Message.toString());
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("info> " + i_Message);
        }
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Throwable
     */
    public void info(final String i_Message, final Throwable i_Throwable) 
    {
        if ( this.infoThrowable != null )
        {
            try
            {
                this.infoThrowable.invoke(this.log ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("info> " + i_Message);
            i_Throwable.printStackTrace();
        }
        else
        {
            i_Throwable.printStackTrace();
        }
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Throwable
     */
    public void info(final Throwable i_Throwable) 
    {
        this.info("" ,i_Throwable);
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Arguments
     */
    public void info(final Object i_Marker ,String i_Message, final Object ... i_Arguments) 
    {
        if ( this.infoMarker != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                Object [] v_Arguments = i_Arguments;
                
                if ( v_Arguments == null )
                {
                    v_Arguments = new Object[0];
                }
                
                this.infoMarker.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( this.info != null && i_Marker != null && "java.long.String".equals(i_Marker.getClass().getName()) )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J || (this.logType == $LogType_Log4J && this.logVersion >= 2 ) )
                {
                    Object [] v_Arguments = null;
                    
                    if ( i_Arguments == null )
                    {
                        v_Arguments = new Object[] {i_Message};
                    }
                    else
                    {
                        v_Arguments = new Object[i_Arguments.length + 1];
                        v_Arguments[0] = i_Message;
                        Help.fillArray(i_Arguments ,v_Arguments ,1);
                    }
                    
                    
                    this.info.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
                }
                else
                {
                    this.info.invoke(this.log ,i_Marker + i_Message + StringHelp.toString(i_Arguments));
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("info> " + i_Message);
        }
    }
    
    
    
    /**
     * 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Throwable
     */
    public void info(final Object i_Marker ,String i_Message, final Throwable i_Throwable) 
    {
        if ( this.infoMarkerThrowable != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                this.infoMarkerThrowable.invoke(this.log ,i_Marker ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("info> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void debug(final String i_Message)
    {
        this.debug(i_Message ,"");
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Argument
     */
    public void debug(final String i_Message ,final String i_Argument)
    {
        if ( this.debug != null )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J )
                {
                    this.debug.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                }
                else if ( this.logType == $LogType_Log4J )
                {
                    if ( this.logVersion == 1 )
                    {
                        this.debug.invoke(this.log ,i_Message + i_Argument);
                    }
                    else
                    {
                        this.debug.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                    }
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("debug> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     */
    public void debug(final Object i_Message)
    {
        if ( this.debug != null )
        {
            try
            {
                if ( i_Message != null )
                {
                    this.debug.invoke(this.log ,i_Message.toString());
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("debug> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Throwable
     */
    public void debug(final String i_Message, final Throwable i_Throwable) 
    {
        if ( this.debugThrowable != null )
        {
            try
            {
                this.debugThrowable.invoke(this.log ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("debug> " + i_Message);
            i_Throwable.printStackTrace();
        }
        else
        {
            i_Throwable.printStackTrace();
        }
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Throwable
     */
    public void debug(final Throwable i_Throwable) 
    {
        this.debug("" ,i_Throwable);
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Arguments
     */
    public void debug(final Object i_Marker ,String i_Message, final Object ... i_Arguments) 
    {
        if ( this.debugMarker != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                Object [] v_Arguments = i_Arguments;
                
                if ( v_Arguments == null )
                {
                    v_Arguments = new Object[0];
                }
                
                this.debugMarker.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( this.debug != null && i_Marker != null && "java.long.String".equals(i_Marker.getClass().getName()) )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J || (this.logType == $LogType_Log4J && this.logVersion >= 2 ) )
                {
                    Object [] v_Arguments = null;
                    
                    if ( i_Arguments == null )
                    {
                        v_Arguments = new Object[] {i_Message};
                    }
                    else
                    {
                        v_Arguments = new Object[i_Arguments.length + 1];
                        v_Arguments[0] = i_Message;
                        Help.fillArray(i_Arguments ,v_Arguments ,1);
                    }
                    
                    
                    this.debug.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
                }
                else
                {
                    this.debug.invoke(this.log ,i_Marker + i_Message + StringHelp.toString(i_Arguments));
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("debug> " + i_Message);
        }
    }
    
    
    
    /**
     * 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Throwable
     */
    public void debug(final Object i_Marker ,String i_Message, final Throwable i_Throwable) 
    {
        if ( this.debugMarkerThrowable != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                this.debugMarkerThrowable.invoke(this.log ,i_Marker ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("debug> " + i_Message);
        }
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-05-27
     * @version     v1.0
     *
     * @param i_Message
     */
    public void trace(final String i_Message)
    {
        this.trace(i_Message ,"");
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Argument
     */
    public void trace(final String i_Message ,final String i_Argument)
    {
        if ( this.trace != null )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J )
                {
                    this.trace.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                }
                else if ( this.logType == $LogType_Log4J )
                {
                    if ( this.logVersion == 1 )
                    {
                        this.trace.invoke(this.log ,i_Message + i_Argument);
                    }
                    else
                    {
                        this.trace.invoke(this.log ,i_Message ,new Object[] {i_Argument});
                    }
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("trace> " + i_Message);
        }
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     */
    public void trace(final Object i_Message)
    {
        if ( this.trace != null )
        {
            try
            {
                if ( i_Message != null )
                {
                    this.trace.invoke(this.log ,i_Message.toString());
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("trace> " + i_Message);
        }
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Message
     * @param i_Throwable
     */
    public void trace(final String i_Message, final Throwable i_Throwable) 
    {
        if ( this.traceThrowable != null )
        {
            try
            {
                this.traceThrowable.invoke(this.log ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("trace> " + i_Message);
            i_Throwable.printStackTrace();
        }
        else
        {
            i_Throwable.printStackTrace();
        }
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-09
     * @version     v1.0
     *
     * @param i_Throwable
     */
    public void trace(final Throwable i_Throwable) 
    {
        this.trace("" ,i_Throwable);
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Arguments
     */
    public void trace(final Object i_Marker ,String i_Message, final Object ... i_Arguments) 
    {
        if ( this.traceMarker != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                Object [] v_Arguments = i_Arguments;
                
                if ( v_Arguments == null )
                {
                    v_Arguments = new Object[0];
                }
                
                this.traceMarker.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( this.trace != null && i_Marker != null && "java.long.String".equals(i_Marker.getClass().getName()) )
        {
            try
            {
                if ( this.logType == $LogType_SLF4J || (this.logType == $LogType_Log4J && this.logVersion >= 2 ) )
                {
                    Object [] v_Arguments = null;
                    
                    if ( i_Arguments == null )
                    {
                        v_Arguments = new Object[] {i_Message};
                    }
                    else
                    {
                        v_Arguments = new Object[i_Arguments.length + 1];
                        v_Arguments[0] = i_Message;
                        Help.fillArray(i_Arguments ,v_Arguments ,1);
                    }
                    
                    
                    this.trace.invoke(this.log ,i_Marker ,i_Message ,v_Arguments);
                }
                else
                {
                    this.trace.invoke(this.log ,i_Marker + i_Message + StringHelp.toString(i_Arguments));
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("trace> " + i_Message);
        }
    }
    
    
    
    /**
     * 最低的日志级别
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-10
     * @version     v1.0
     *
     * @param i_Marker
     * @param i_Message
     * @param i_Throwable
     */
    public void trace(final Object i_Marker ,String i_Message, final Throwable i_Throwable) 
    {
        if ( this.traceMarkerThrowable != null && i_Marker != null && i_Marker.getClass().getName().endsWith("Marker") )
        {
            try
            {
                this.traceMarkerThrowable.invoke(this.log ,i_Marker ,i_Message ,i_Throwable);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        else if ( logClass != null )
        {
            this.println("trace> " + i_Message);
        }
    }
    
    
    
    /**
     * 采用System.out.println()方法输出
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-01-06
     * @version     v1.0
     *
     * @param i_Message
     */
    public void println(final String i_Message)
    {
        if ( logClass != null )
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + " [" + this.logClass.getName() + "] " + i_Message);
        }
        else
        {
            System.out.println("-- " + Date.getNowTime().getFullMilli() + " " + i_Message);
        }
    }
    
    
    
    /**
     * 获取：日志实现类库的类型（1：SLF4J  2：Log4J）
     */
    public int getLogType()
    {
        return logType;
    }


    
    /**
     * 获取：日志实现类库的版本
     */
    public int getLogVersion()
    {
        return logVersion;
    }
    
}
