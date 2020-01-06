package org.hy.common.xml.log;

import java.lang.reflect.Method;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StaticReflect;





/**
 * 同时支持Log4j 1.x 和 Log4j 2.x 两个版本的功能.
 * 
 * 同时支持没有任何 Log4j 引包的情况。
 * 
 * 
 * 注意1：当 Log4j 1.x 与 Log4j 2.x 两版本的引包均存在时，优先使用低版本 Log4j 1.x 。
 * 注意2：不支持TRACE级别的日志，原因是：不常用哈。
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-05-27
 * @version     v1.0
 *              v2.0  2020-01-06  添加：在没有任何Log4j版本时，可采用System.out.println()方法输出
 */
public class Logger
{
        
    private Object   log;
    
    private Method   fatal;       // 指出每个严重的错误事件将会导致应用程序的退出。这个级别比较高了。重大错误，这种级别你可以直接停止程序了。
    
    private Method   error;       // 指出虽然发生错误事件，但仍然不影响系统的继续运行。打印错误和异常信息，如果不想输出太多的日志，可以使用这个级别。
    
    private Method   warn;        // 表明会出现潜在错误的情形，有些信息不是错误信息，但是也要给程序员的一些提示。
    
    private Method   info;        // 消息在粗粒度级别上突出强调应用程序的运行过程。打印一些你感兴趣的或者重要的信息
    
    private Method   debug;       // 指出细粒度信息事件对调试应用程序是非常有帮助的，主要用于开发过程中打印一些运行信息
    
    /** 没有任何Log4j版本时，是否采用System.out.println()方法输出 */
    private Class<?> logClass;  
    
    
    
    public Logger(Class<?> i_Class)
    {
        this(i_Class ,false);
    }
    
    
    
    public Logger(Class<?> i_Class ,boolean i_IsPrintln)
    {
        Class<?> v_LogClass   = null;
        Class<?> v_LogManager = null;
        int      v_Version    = 0;
        
        try
        {
            // Log4j 1.x 的版本
            v_LogClass   = Help.forName("org.apache.log4j.Logger");
            v_LogManager = Help.forName("org.apache.log4j.LogManager");
            v_Version  = 1;
        }
        catch (Exception exce)
        {
            // Nothing.
        }
        
        if (  v_LogClass == null )
        {
            try
            {
                // Log4j 2.x 的版本
                v_LogClass   = Help.forName("org.apache.logging.log4j.Logger");
                v_LogManager = Help.forName("org.apache.logging.log4j.LogManager");
                v_Version  = 2;
            }
            catch (Exception exce)
            {
                // Nothing.
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
                    Class<?> v_PClass = null;
                    
                    if ( v_Version == 1 )
                    {
                        v_PClass = Object.class;
                    }
                    else if ( v_Version == 2 )
                    {
                        v_PClass = String.class;
                    }
                    
                    this.fatal = v_LogClass.getMethod("fatal" ,v_PClass);
                    this.error = v_LogClass.getMethod("error" ,v_PClass);
                    this.warn  = v_LogClass.getMethod("warn"  ,v_PClass);
                    this.info  = v_LogClass.getMethod("info"  ,v_PClass);
                    this.debug = v_LogClass.getMethod("debug" ,v_PClass);
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
        }
        else if ( i_IsPrintln )
        {
            this.logClass = i_Class;
        }
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
        if ( this.fatal != null )
        {
            try
            {
                this.fatal.invoke(this.log ,i_Message);
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
        if ( this.error != null )
        {
            try
            {
                this.error.invoke(this.log ,i_Message);
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
        if ( this.warn != null )
        {
            try
            {
                this.warn.invoke(this.log ,i_Message);
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
        if ( this.info != null )
        {
            try
            {
                this.info.invoke(this.log ,i_Message);
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
        if ( this.debug != null )
        {
            try
            {
                this.debug.invoke(this.log ,i_Message);
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
    
}
