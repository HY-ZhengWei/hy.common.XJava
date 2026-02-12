package org.hy.common.xml.plugins;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hy.common.Busway;
import org.hy.common.ExpireCache;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.xml.XJava;





/**
 * 记录一次页面访问所对应执行的SQL信息
 * 
 * 过滤器的初始化参数：1. exclusions  排除哪些URL不被记录。多个正则表达式规则间用,逗号分隔
 *                  2. cachesize   缓存大小。用于 $SQLBusway
 *                  3. timeout     超时时长，单位：秒。用于 $Requests
 *                  
 *                  
    <!-- 记录一次页面访问所对应执行的SQL信息  ZhengWei(HY) Add 2017-07-13 -->
    <filter>
        <filter-name>XSQLFilter</filter-name>
        <filter-class>org.hy.common.xml.plugins.XSQLFilter</filter-class>
        <init-param>
            <param-name>exclusions</param-name>
            <param-value>*.js,*.gif,*.jpg,*.png,*.css,*.ico,*.swf</param-value>
        </init-param>
        <init-param>
            <param-name>cachesize</param-name>
            <param-value>1000</param-value>
        </init-param>
        <init-param>
            <param-name>timeout</param-name>
            <param-value>60</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>XSQLFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-13
 * @version     v1.0
 */
public class XSQLFilter implements Filter 
{
    
    private static final ExpireCache<Long ,XSQLURL> $Requests  = new ExpireCache<Long ,XSQLURL>();
    
    private static final Busway<XSQLURL>            $SQLBusway = new Busway<XSQLURL>(1000);
    
    
    
    /** 排除哪些URL不被记录 */
    private String [] filters;
    
    /** 超时时长。单位：秒 */
    private int       timeOut = 60;
    
    
    
    /**
     * 执行SQL的记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-13
     * @version     v1.0
     *
     * @param i_ThreadID
     * @param i_SQL
     */
    public synchronized static void logXSQL(Long i_ThreadID ,String i_SQL)
    {
        XSQLURL v_XSQLURL = $Requests.get(i_ThreadID);
        
        if ( v_XSQLURL != null )
        {
            if ( v_XSQLURL.getSqls() == null )
            {
                v_XSQLURL.setSqls(new ArrayList<String>());
                
                $SQLBusway.put(v_XSQLURL);
            }
            
            v_XSQLURL.getSqls().add(i_SQL);
        }
    }
    
    
    
    /**
     * 显示日志 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-13
     * @version     v1.0
     *
     * @return
     */
    public Busway<XSQLURL> logs()
    {
        return $SQLBusway;
    }
    
    
    
    /**
     * 获取日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-13
     * @version     v1.0
     *
     * @return
     */
    public Busway<XSQLURL> getLogs()
    {
        return $SQLBusway;
    }
    
    
    
    public void setLogs(Busway<XSQLURL> i_SQLBusway)
    {
        // Nothing.
    }
    
    
    
    /**
     * 清除日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-13
     * @version     v1.0
     *
     */
    public void clearLogs()
    {
        $SQLBusway.clear();
        $Requests .clear();
    }
    
    
    
    /**
     * 排除哪些URL不被记录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-13
     * @version     v1.0
     *
     * @param i_URL
     * @return
     */
    private boolean isExclusions(String i_URL)
    {
        if ( Help.isNull(this.filters) )
        {
            return true;
        }
        else if ( StringHelp.getCount(i_URL ,this.filters) >= 1 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    
    public void init(FilterConfig i_FilterConfig) throws ServletException
    {
        XJava.putObject("XSQLFilter" ,this);
        
        String v_Exclusions = i_FilterConfig.getInitParameter("exclusions");
        String v_Cachesize  = i_FilterConfig.getInitParameter("cachesize");
        String v_Timeout    = i_FilterConfig.getInitParameter("timeout");
        
        if ( !Help.isNull(v_Exclusions) )
        {
            this.filters = v_Exclusions.split(",");
            
            for (int i=0; i<this.filters.length; i++)
            {
                this.filters[i] = StringHelp.replaceAll(this.filters[i] ,new String[]{"*." ,"*"} ,new String[]{"[\\S]+"});
            }
        }
        else
        {
            this.filters = new String[0];
        }
        
        if ( Help.isNumber(v_Cachesize) )
        {
            $SQLBusway.setWayLength(Integer.parseInt(v_Cachesize));
        }
        
        if ( Help.isNumber(v_Timeout) )
        {
            this.timeOut = Integer.parseInt(v_Timeout);
        }
    }
    
    
    
    public void doFilter(ServletRequest i_Request ,ServletResponse i_Response ,FilterChain i_Chain) throws IOException ,ServletException
    {
        HttpServletRequest v_Request = (HttpServletRequest)i_Request;
        String             v_URL     = v_Request.getRequestURL().toString();
        
        if ( !this.isExclusions(v_URL) )
        {
            XSQLURL v_XSQLURL = new XSQLURL();
            if ( !Help.isNull(v_Request.getQueryString()) )
            {
                v_XSQLURL.setUrl(v_URL + "?" + v_Request.getQueryString());
            }
            else
            {
                v_XSQLURL.setUrl(v_URL);
            }
            
            $Requests.put(Thread.currentThread().getId() ,v_XSQLURL ,this.timeOut);
        }
        
        i_Chain.doFilter(i_Request ,i_Response);
    }
    
    
    
    public void destroy() 
    {
        
    }
    
}
