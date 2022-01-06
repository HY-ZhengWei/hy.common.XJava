package org.hy.common.xml.plugins.analyse.data;

import org.hy.common.Help;
import org.hy.common.net.data.ClientUserInfo;





/**
 * 通讯连接的统计分析类
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-01-05
 * @version     v1.0
 */
public class NetReport extends ClientUserInfo
{

    private static final long serialVersionUID = -5066726756027709286L;
    
    /** 统计编号 */
    private String  totalID;
    
    /** 服务端的端口 */
    private int     serverPort;
    
    /** 会话限制 */
    private String  sessionLimit;
    
    /** 连接数量 */
    private int     connectCount;
    
    /** 在线数量 */
    private int     onlineCount;
    
    
    
    public NetReport()
    {
        this.connectCount = 0;
        this.onlineCount  = 0;
    }
    
    
    
    public NetReport(ClientUserInfo i_ClientUser)
    {
        this.initNotNull(i_ClientUser);
        this.connectCount = 1;
        this.onlineCount  = this.isOnline() ? 1 : 0;
    }
    
    
    
    /**
     * 添加统计
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-01-05
     * @version     v1.0
     * 
     * @param i_ClientUser
     */
    public void addTotal(ClientUserInfo i_ClientUser)
    {
        this.connectCount++;
        this.onlineCount += i_ClientUser.isOnline() ? 1 : 0;
        this.setRequestCount( this.getRequestCount()  + i_ClientUser.getRequestCount());
        this.setActiveCount(  this.getActiveCount()   + i_ClientUser.getActiveCount());
        this.setActiveTimeLen(this.getActiveTimeLen() + i_ClientUser.getActiveTimeLen());
        
        // 登录时间：取最早
        if ( this.getLoginTime() == null )
        {
            this.setLoginTime(i_ClientUser.getLoginTime());
        }
        else if ( i_ClientUser.getLoginTime() == null )
        {
            // Nothing.
        }
        else if ( this.getLoginTime().getTime() > i_ClientUser.getLoginTime().getTime() )
        {
            this.setLoginTime(i_ClientUser.getLoginTime());
        }
        
        // 登出时间：取最新
        if ( this.getLogoutTime() == null )
        {
            this.setLogoutTime(i_ClientUser.getLogoutTime());
        }
        else if ( i_ClientUser.getLogoutTime() == null )
        {
            // Nothing.
        }
        else if ( this.getLogoutTime().getTime() < i_ClientUser.getLogoutTime().getTime() )
        {
            this.setLogoutTime(i_ClientUser.getLogoutTime());
        }
        
        // 心跳时间：取最新
        if ( this.getIdleTime() == null )
        {
            this.setIdleTime(i_ClientUser.getIdleTime());
        }
        else if ( i_ClientUser.getIdleTime() == null )
        {
            // Nothing.
        }
        else if ( this.getIdleTime().getTime() < i_ClientUser.getIdleTime().getTime() )
        {
            this.setIdleTime(i_ClientUser.getIdleTime());
        }
        
        // 通讯时间：取最新
        if ( this.getActiveTime() == null )
        {
            this.setActiveTime(i_ClientUser.getActiveTime());
        }
        else if ( i_ClientUser.getActiveTime() == null )
        {
            // Nothing.
        }
        else if ( this.getActiveTime().getTime() < i_ClientUser.getActiveTime().getTime() )
        {
            this.setActiveTime(i_ClientUser.getActiveTime());
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
    
}
