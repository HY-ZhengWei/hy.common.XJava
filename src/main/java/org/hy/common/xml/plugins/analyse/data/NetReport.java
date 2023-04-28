package org.hy.common.xml.plugins.analyse.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.net.data.NetException;
import org.hy.common.net.data.SessionInfo;





/**
 * 通讯连接的统计分析类
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-01-05
 * @version     v1.0
 */
public class NetReport extends SessionInfo
{

    private static final long serialVersionUID = -5066726756027709286L;
    
    public  static final int  $Type_Server     = -1;
    
    public  static final int  $Type_Client     = 1;
    
    
    
    /** 类型（1：请求-客户端； -1:响应-服务端） */
    private int                type;
                               
    /** 统计编号 */
    private String             totalID;
    
    /** 服务端的端口 */
    private int                serverPort;
                               
    /** 会话限制 */
    private String             sessionLimit;
                               
    /** 连接数量 */
    private int                connectCount;
                               
    /** 在线数量 */
    private int                onlineCount;
    
    /** 通讯异常日志 */
    private List<NetException> netErrorLogs;
    
    
    
    public NetReport()
    {
        this.connectCount = 0;
        this.onlineCount  = 0;
        this.netErrorLogs = new ArrayList<NetException>();
    }
    
    
    
    public NetReport(SessionInfo i_Session)
    {
        this.initNotNull(i_Session);
        this.connectCount = 1;
        this.onlineCount  = this.isOnline() ? 1 : 0;
        this.netErrorLogs = new ArrayList<NetException>();
        
        if ( i_Session.getNetExceptions() != null )
        {
            Iterator<NetException> v_ErrorLogArr = i_Session.getNetExceptions().iterator();
            while ( v_ErrorLogArr.hasNext() )
            {
                NetException v_Log = null;
                try
                {
                    v_Log = v_ErrorLogArr.next();
                }
                catch (Exception exce)
                {
                    break;
                }
                
                this.netErrorLogs.add(v_Log);
            }
        }
    }
    
    
    
    /**
     * 添加统计
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-01-05
     * @version     v1.0
     * 
     * @param i_Session
     */
    public void addTotal(SessionInfo i_Session)
    {
        this.connectCount++;
        this.onlineCount += i_Session.isOnline() ? 1 : 0;
        this.setRequestCount( this.getRequestCount()  + i_Session.getRequestCount());
        this.setActiveCount(  this.getActiveCount()   + i_Session.getActiveCount());
        this.setActiveTimeLen(this.getActiveTimeLen() + i_Session.getActiveTimeLen());
        
        // 登录时间：取最早
        if ( this.getLoginTime() == null )
        {
            this.setLoginTime(i_Session.getLoginTime());
        }
        else if ( i_Session.getLoginTime() == null )
        {
            // Nothing.
        }
        else if ( this.getLoginTime().getTime() > i_Session.getLoginTime().getTime() )
        {
            this.setLoginTime(i_Session.getLoginTime());
        }
        
        // 登出时间：取最新
        if ( this.getLogoutTime() == null )
        {
            this.setLogoutTime(i_Session.getLogoutTime());
        }
        else if ( i_Session.getLogoutTime() == null )
        {
            // Nothing.
        }
        else if ( this.getLogoutTime().getTime() < i_Session.getLogoutTime().getTime() )
        {
            this.setLogoutTime(i_Session.getLogoutTime());
        }
        
        // 心跳时间：取最新
        if ( this.getIdleTime() == null )
        {
            this.setIdleTime(i_Session.getIdleTime());
        }
        else if ( i_Session.getIdleTime() == null )
        {
            // Nothing.
        }
        else if ( this.getIdleTime().getTime() < i_Session.getIdleTime().getTime() )
        {
            this.setIdleTime(i_Session.getIdleTime());
        }
        
        // 通讯时间：取最新
        if ( this.getActiveTime() == null )
        {
            this.setActiveTime(i_Session.getActiveTime());
        }
        else if ( i_Session.getActiveTime() == null )
        {
            // Nothing.
        }
        else if ( this.getActiveTime().getTime() < i_Session.getActiveTime().getTime() )
        {
            this.setActiveTime(i_Session.getActiveTime());
        }
        
        if ( i_Session.getNetExceptions() != null )
        {
            Iterator<NetException> v_ErrorLogArr = i_Session.getNetExceptions().iterator();
            while ( v_ErrorLogArr.hasNext() )
            {
                NetException v_Log = null;
                try
                {
                    v_Log = v_ErrorLogArr.next();
                }
                catch (Exception exce)
                {
                    break;
                }
                
                this.netErrorLogs.add(v_Log);
            }
        }
    }
    
    
    
    /**
     * 获取：未成功量
     */
    public long getErrorCount()
    {
        return this.getRequestCount() - this.getActiveCount();
    }
    
    
    /**
     * 获取：平均用时
     */
    public double getAvgActiveTimeLen()
    {
        return Help.division(this.getActiveTimeLen() ,this.getActiveCount());
    }
    
    
    /**
     * 获取：统计编号
     */
    public String getTotalID()
    {
        return totalID;
    }


    /**
     * 设置：统计编号
     * 
     * @param totalID
     */
    public void setTotalID(String totalID)
    {
        this.totalID = totalID;
    }


    /**
     * 获取：服务端的端口
     */
    public int getServerPort()
    {
        return serverPort;
    }


    /**
     * 设置：服务端的端口
     * 
     * @param serverPort
     */
    public void setServerPort(int serverPort)
    {
        this.serverPort = serverPort;
    }
    
    
    /**
     * 获取：会话限制
     */
    public String getSessionLimit()
    {
        return sessionLimit;
    }


    /**
     * 设置：会话限制
     * 
     * @param sessionLimit
     */
    public void setSessionLimit(String sessionLimit)
    {
        this.sessionLimit = sessionLimit;
    }


    /**
     * 获取：连接数量
     */
    public int getConnectCount()
    {
        return connectCount;
    }


    /**
     * 设置：连接数量
     * 
     * @param connectCount
     */
    public void setConnectCount(int connectCount)
    {
        this.connectCount = connectCount;
    }


    /**
     * 获取：在线数量
     */
    public int getOnlineCount()
    {
        return onlineCount;
    }


    /**
     * 设置：在线数量
     * 
     * @param onlineCount
     */
    public void setOnlineCount(int onlineCount)
    {
        this.onlineCount = onlineCount;
    }


    /**
     * 获取：类型（1：请求-客户端； -1:响应-服务端）
     */
    public int getType()
    {
        return type;
    }


    /**
     * 设置：类型（1：请求-客户端； -1:响应-服务端）
     * 
     * @param type
     */
    public void setType(int type)
    {
        this.type = type;
    }


    /**
     * 获取：通讯异常日志
     */
    public List<NetException> getNetErrorLogs()
    {
        return netErrorLogs;
    }


    /**
     * 设置：通讯异常日志
     * 
     * @param i_NetErrorLogs
     */
    public void setNetErrorLogs(List<NetException> i_NetErrorLogs)
    {
        this.netErrorLogs = i_NetErrorLogs;
    }
    
}
