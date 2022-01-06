package org.hy.common.xml.plugins.analyse.data;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.net.common.ServerOperation;
import org.hy.common.net.data.ClientUserInfo;
import org.hy.common.xml.SerializableDef;
import org.hy.common.xml.XJava;





/**
 * 通讯连接的分析统计类
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-01-05
 * @version     v1.0
 */
public class AnalyseNetTotal extends SerializableDef
{
    
    private static final long serialVersionUID = 9221124624853645077L;
    
    /** 通讯连接统计信息 */
    private Map<String ,NetReport> reports;
    
    
    
    public AnalyseNetTotal()
    {
        this.reports = new Hashtable<String ,NetReport>();
    }
    
    
    
    /**
     * 生成本机的通讯连接统计
     *
     * @author      ZhengWei(HY)
     * @createDate  2022-01-05
     * @version     v1.0
     *
     * @param i_TotalType  统计类型(LocalSocket、User、ClientIP、UserClientIP)
     */
    public AnalyseNetTotal(String i_TotalType)
    {
        this.reports = new Hashtable<String ,NetReport>();
        Map<String ,Object> v_TotalMap = XJava.getObjects(ServerOperation.class);
        
        if ( Help.isNull(v_TotalMap) )
        {
            return;
        }
        
        // 按“客户IP”分组统计
        if ( "ClientIP".equalsIgnoreCase(i_TotalType) )
        {
            for (Object v_Total : v_TotalMap.values())
            {
                ServerOperation      v_Server  = (ServerOperation)v_Total;
                List<ClientUserInfo> v_Clients = v_Server.getClientUsers();
                
                for (ClientUserInfo v_Client : v_Clients)
                {
                    String    v_TotalID = v_Client.getHost();
                    NetReport v_Report  = this.reports.get(v_TotalID);
                    
                    if ( v_Report == null )
                    {
                        v_Report = new NetReport(v_Client);
                        v_Report.setTotalID(v_TotalID);
                        v_Report.setServerPort(v_Server.getPort());
                        v_Report.setSessionLimit(v_Server.getSessionTime() + ":" + v_Server.getSameUserOnlineMaxCount());
                        
                        this.reports.put(v_TotalID ,v_Report);
                    }
                    else
                    {
                        v_Report.addTotal(v_Client);
                    }
                }
            }
        }
        // 按“账户”分组统计
        else if ( "User".equalsIgnoreCase(i_TotalType) )
        {
            for (Object v_Total : v_TotalMap.values())
            {
                ServerOperation      v_Server  = (ServerOperation)v_Total;
                List<ClientUserInfo> v_Clients = v_Server.getClientUsers();
                
                for (ClientUserInfo v_Client : v_Clients)
                {
                    String    v_TotalID = v_Client.getSystemName() + "_" + v_Client.getUserName();
                    NetReport v_Report  = this.reports.get(v_TotalID);
                    
                    if ( v_Report == null )
                    {
                        v_Report = new NetReport(v_Client);
                        v_Report.setTotalID(v_TotalID);
                        v_Report.setServerPort(v_Server.getPort());
                        v_Report.setSessionLimit(v_Server.getSessionTime() + ":" + v_Server.getSameUserOnlineMaxCount());
                        
                        this.reports.put(v_TotalID ,v_Report);
                    }
                    else
                    {
                        v_Report.addTotal(v_Client);
                    }
                }
            }
        }
        // 按“本机开启的服务端口”分组统计
        else if ( "LocalSocket".equalsIgnoreCase(i_TotalType) )
        {
            for (Object v_Total : v_TotalMap.values())
            {
                ServerOperation      v_Server  = (ServerOperation)v_Total;
                List<ClientUserInfo> v_Clients = v_Server.getClientUsers();
                
                for (ClientUserInfo v_Client : v_Clients)
                {
                    String    v_TotalID = v_Server.getPort() + "";
                    NetReport v_Report  = this.reports.get(v_TotalID);
                    
                    if ( v_Report == null )
                    {
                        v_Report = new NetReport(v_Client);
                        v_Report.setTotalID(v_TotalID);
                        v_Report.setServerPort(v_Server.getPort());
                        v_Report.setSessionLimit(v_Server.getSessionTime() + ":" + v_Server.getSameUserOnlineMaxCount());
                        
                        this.reports.put(v_TotalID ,v_Report);
                    }
                    else
                    {
                        v_Report.addTotal(v_Client);
                    }
                }
            }
        }
        // 按“服务端口+账户+客户IP+客户端口”分组统计。(默认分组方式)
        else
        {
            for (Object v_Total : v_TotalMap.values())
            {
                ServerOperation      v_Server  = (ServerOperation)v_Total;
                List<ClientUserInfo> v_Clients = v_Server.getClientUsers();
                
                for (ClientUserInfo v_Client : v_Clients)
                {
                    String    v_TotalID = v_Server.getPort() + ":" + v_Client.getSystemName() + "_" + v_Client.getUserName() + "_" + v_Client.getHost() + ":" + v_Client.getPort();
                    NetReport v_Report  = this.reports.get(v_TotalID);
                    
                    if ( v_Report == null )
                    {
                        v_Report = new NetReport(v_Client);
                        v_Report.setTotalID(v_TotalID);
                        v_Report.setServerPort(v_Server.getPort());
                        v_Report.setSessionLimit(v_Server.getSessionTime() + ":" + v_Server.getSameUserOnlineMaxCount());
                        
                        this.reports.put(v_TotalID ,v_Report);
                    }
                    else
                    {
                        v_Report.addTotal(v_Client);
                    }
                }
            }
        }
    }
    
    
    
    /**
     * 获取：通讯连接的分析信息
     */
    public Map<String ,NetReport> getReports()
    {
        return reports;
    }
    

    
    /**
     * 设置：通讯连接的分析信息
     * 
     * @param reports
     */
    public void setReports(Map<String ,NetReport> reports)
    {
        this.reports = reports;
    }
    
}
