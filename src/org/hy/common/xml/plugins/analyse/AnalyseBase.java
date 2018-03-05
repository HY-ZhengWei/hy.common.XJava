package org.hy.common.xml.plugins.analyse;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hy.common.Busway;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.Queue;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.app.Param;
import org.hy.common.file.FileHelp;
import org.hy.common.net.ClientSocket;
import org.hy.common.net.ClientSocketCluster;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.thread.Job;
import org.hy.common.thread.JobReport;
import org.hy.common.thread.ThreadReport;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJSONObject;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.XSQLDBMetadata;
import org.hy.common.xml.XSQLLog;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.AppInitConfig;
import org.hy.common.xml.plugins.XSQLGroup;





/**
 * 分析服务的基础类
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-12-15
 * @version     v1.0
 * @version     v2.0  2017-01-04  添加：查看XSQL对象执行错误的SQL语句
 * @version     v2.1  2017-01-06  添加：查看XSQL对象相关的触发器执行错误的SQL语句
 *                                添加：查看XSQL对象相关的触发器执行统计信息
 *              v3.0  2017-01-17  添加：集群重新加载XJava配置文件的功能
 *                                添加：集群执行对象方法
 *                                添加：查看集群服务列表
 *              v4.0  2017-01-22  添加：查看集群数据库访问量的概要统计数据
 *                                添加：查看集群数据库组合SQL访问量的概要统计数据 
 *                                添加：查看集群查看XSQL对象执行错误的SQL语句
 *              v5.0  2017-01-25  添加：跨域的单点登陆 和 集群的单点登陆功能
 *              v6.0  2017-03-01  添加：查看前缀匹配的对象列表页面，添加显示对象.toString()的信息。
 *                                     特殊情况1: 对于Java默认的toString()返回值不予显示。
 *                                     特殊情况2: 对于集合对象，不予显示。
 *              v7.0  2018-02-11  添加：删除并重新创建数据库对象
 *              v8.0  2018-02-12  添加：支持集群中每个服务可以有不一样XSQL列表，并且可以在集群列表中显示。
 *                                添加：集群监控SQL异常时，显示是哪台服务器上的SQL出的错。
 *              v9.0  2018-02-27  添加：本机线程池运行情况监控
 *                                添加：集群线程池运行情况监控
 *              v10.0 2018-03-01  添加：本机定时任务运行情况。之前合并在 "查看前缀匹配的对象列表" 任务中
 *              v11.0 2018-03-05  添加：本机数据库连接池组使用情况。之前合并在 "查看前缀匹配的对象列表" 任务中
 *                                添加：集群数据库连接池组使用情况。之前合并在 "查看前缀匹配的对象列表" 任务中
 */
@Xjava
public class AnalyseBase
{
    
    /** 模板信息的缓存 */
    private final static Map<String ,String>          $TemplateCaches  = new Hashtable<String ,String>();
    
    /** 服务器启动时间 */
    private       static Date                         $ServerStartTime = null;
    
    
    
    public AnalyseBase()
    {
        if ( $ServerStartTime == null )
        {
            $ServerStartTime = new Date();
        }
    }
    
    
    
    /**
     * 验证登录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-07
     * @version     v1.0
     *
     * @param  i_LogonPath       登录的URL。如：http://127.0.0.1:80/hy/../analyseObject  (可选：附加用户输入的参数)
     * @return
     */
    public String login(String i_LogonPath)
    {
        String v_Content = this.getTemplateLogon();
        
        return StringHelp.replaceAll(v_Content ,":LoginPath" ,i_LogonPath);
    }
    
    
    
    /**
     * 验证单点登录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-25
     * @version     v1.0
     *
     * @param  i_RequestURL      请求的路径。如：http://127.0.0.1:80/hy/../analyseObject  (无用户附加的请求参数)
     * @param  i_LoginPath       登录的URL。如：http://127.0.0.1:80/hy/../analyseObject  (当用户有附加参数时，也一同带上)
     * @return
     */
    public String loginSSO(int i_ServerPort ,String i_RequestURL ,String i_LoginPath)
    {
        StringBuilder      v_Buffer  = new StringBuilder();
        String             v_Content = "<script type='text/javascript' src=':BasePath?SSOCallBack=getUSID&r=" + Math.random() + "'></script>";
        List<ClientSocket> v_Servers = Cluster.getClusters();
        
        // 给登陆的URL带上一个r参数
        String v_LoginPath = i_LoginPath;
        if ( v_LoginPath.indexOf("?") >= 0 )
        {
            if ( v_LoginPath.indexOf("&r=") < 0 && v_LoginPath.indexOf("?r=") < 0 )
            {
                v_LoginPath += "&r=" + Math.random();
            }
        }
        else
        {
            v_LoginPath += "?r=" + Math.random();
        }
        
        if ( Help.isNull(v_Servers) )
        {
            return this.login(v_LoginPath);
        }
        
        String v_RequestURL = i_RequestURL.split("//")[1].split("/")[0];
        v_RequestURL = StringHelp.replaceAll(i_RequestURL ,v_RequestURL ,":IPPort");
        
        for (ClientSocket v_Server : v_Servers)
        {
            v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                 ,":BasePath" 
                                                 ,StringHelp.replaceAll(v_RequestURL ,":IPPort" ,v_Server.getHostName() + ":" + i_ServerPort)));
        }
        
        return StringHelp.replaceAll(this.getTemplateLogonSSO() 
                                    ,new String[]{":LoginPath" ,":Content"} 
                                    ,new String[]{v_LoginPath  ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 功能1. 显示大纲目录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-08-01
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @return
     */
    public String showCatalogue(String i_BasePath ,String i_ObjectValuePath)
    {
        List<Param>   v_Objects = AnalysesCatalogue.getCatalogue();
        StringBuilder v_Buffer  = new StringBuilder();
        int           v_Index   = 0;
        String        v_Content = this.getTemplateShowObjectsContent();
        
        v_Buffer.append(StringHelp.replaceAll(v_Content 
                ,new String[]{":No" 
                             ,":Name" 
                             ,":Info"
                             ,":OperateURL" 
                             ,":OperateTitle"} 
                ,new String[]{String.valueOf(++v_Index)
                             ,"系统启动时间"
                             ,$ServerStartTime.getFull()
                             ,""
                             ,""
                             })
                );
        
        for (Param v_Item : v_Objects)
        {
            String v_URL     = "";
            String v_Command = "";
            
            if ( !Help.isNull(v_Item.getValue()) )
            {
                v_URL     = i_ObjectValuePath + v_Item.getValue().trim();
                v_Command = "查看详情";
            }
            
            v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                 ,new String[]{":No" 
                                                              ,":Name" 
                                                              ,":Info"
                                                              ,":OperateURL" 
                                                              ,":OperateTitle"} 
                                                 ,new String[]{String.valueOf(++v_Index)
                                                              ,v_Item.getName()
                                                              ,Help.NVL(v_Item.getComment())
                                                              ,v_URL
                                                              ,v_Command
                                                              })
                           );
        }
        
        return StringHelp.replaceAll(this.getTemplateShowObjects()
                                    ,new String[]{":Title"  ,":Column01Title" ,":Column02Title"  ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"分析中心" ,"功能"            ,"说明"            ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取数据库组合SQL访问量的概要统计数据（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-15
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseDB
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analyseDBGroup(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster)
    {
        Map<String ,Object> v_Objs         = XJava.getObjects(XSQLGroup.class);
        StringBuilder       v_Buffer       = new StringBuilder();
        int                 v_Index        = 0;
        String              v_Content      = this.getTemplateShowTotalContent();
        long                v_RequestCount = 0;
        long                v_SuccessCount = 0;
        double              v_TotalTimeLen = 0;
        double              v_AvgTimeLen   = 0;
        Date                v_MaxExecTime  = null;
        long                v_NowTime      = 0L;
        AnalyseDBTotal      v_Total        = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseDBGroup_Total();
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseDBTotal();
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDBGroup_Total");
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof AnalyseDBTotal )
                        {
                            AnalyseDBTotal v_TempTotal = (AnalyseDBTotal)v_ResponseData.getData();
                            
                            v_Total.getRequestCount().putAll(v_TempTotal.getRequestCount());
                            v_Total.getSuccessCount().putAll(v_TempTotal.getSuccessCount());
                            v_Total.getMaxExecTime() .putAll(v_TempTotal.getMaxExecTime());
                            v_Total.getTotalTimeLen().putAll(v_TempTotal.getTotalTimeLen());
                        }
                    }
                }
            }
        }
        
        v_Objs    = Help.toSort(v_Objs);
        v_NowTime = new Date().getMinutes(-2).getTime();
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                // XSQLGroup v_GXSQL = (XSQLGroup)v_Item.getValue();
                
                v_RequestCount = v_Total.getRequestCount().getSumValue(v_Item.getKey());
                v_SuccessCount = v_Total.getSuccessCount().getSumValue(v_Item.getKey());
                v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue(v_Item.getKey());
                v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
                v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue(v_Item.getKey()).longValue());
                
                v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                         .replaceAll(":Name"         ,v_Item.getKey())
                                         .replaceAll(":RequestCount" ,String.valueOf(v_RequestCount))
                                         .replaceAll(":SuccessCount" ,String.valueOf(v_SuccessCount))
                                         .replaceAll(":FailCount"    ,String.valueOf(v_RequestCount - v_SuccessCount))
                                         .replaceAll(":ParamURL"     ,"#")
                                         .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : (v_MaxExecTime.getTime() >= v_NowTime ? v_MaxExecTime.getFull() : "<span style='color:gray;'>" + v_MaxExecTime.getFull() + "</span>"))
                                         .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                         .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                               );
                
            }
        }
        
        v_RequestCount = v_Total.getRequestCount().getSumValue();
        v_SuccessCount = v_Total.getSuccessCount().getSumValue();
        v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue();
        v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
        v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue().longValue());
        
        v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                 .replaceAll(":Name"         ,"合计")
                                 .replaceAll(":RequestCount" ,String.valueOf(v_RequestCount))
                                 .replaceAll(":SuccessCount" ,String.valueOf(v_SuccessCount))
                                 .replaceAll(":FailCount"    ,String.valueOf(v_RequestCount - v_SuccessCount))
                                 .replaceAll(":ParamURL"     ,"#")
                                 .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : v_MaxExecTime.getFull())
                                 .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                 .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                       );
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='analyseDB' style='color:#AA66CC'>查看SQL</a>" + StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseDB?type=Group' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseDB?type=Group&cluster=Y' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDBGroup_Reset' style='color:#AA66CC'>重置统计</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDBGroup_Reset&cluster=Y&sameTime=Y' style='color:#AA66CC'>集群重置</a>";
        }
        
        return StringHelp.replaceAll(this.getTemplateShowTotal()
                                    ,new String[]{":NameTitle"             ,":Title"                    ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"组合SQL访问标识" + v_Goto ,"数据库组合SQL访问量的概要统计" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取数据库访问量的概要统计数据（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseDB
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analyseDB(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster)
    {
        Map<String ,Object> v_XSQLs           = XJava.getObjects(XSQL.class);
        StringBuilder       v_Buffer          = new StringBuilder();
        int                 v_Index           = 0;
        String              v_Content         = this.getTemplateShowTotalContent();
        String              v_OperateURL      = "";
        long                v_RequestCount    = 0;
        long                v_SuccessCount    = 0;
        long                v_TriggerReqCount = 0;
        long                v_TriggerSucCount = 0;
        long                v_TriggerFaiCount = 0;
        double              v_TotalTimeLen    = 0;
        double              v_AvgTimeLen      = 0;
        Date                v_MaxExecTime     = null;
        long                v_NowTime         = 0L;
        AnalyseDBTotal      v_Total           = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseDB_Total();
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseDBTotal();
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDB_Total");
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof AnalyseDBTotal )
                        {
                            AnalyseDBTotal v_TempTotal = (AnalyseDBTotal)v_ResponseData.getData();
                            
                            v_Total.getRequestCount()   .putAll(v_TempTotal.getRequestCount());
                            v_Total.getSuccessCount()   .putAll(v_TempTotal.getSuccessCount());
                            v_Total.getTriggerReqCount().putAll(v_TempTotal.getTriggerReqCount());
                            v_Total.getTriggerSucCount().putAll(v_TempTotal.getTriggerSucCount());
                            v_Total.getMaxExecTime()    .putAll(v_TempTotal.getMaxExecTime());
                            v_Total.getTotalTimeLen()   .putAll(v_TempTotal.getTotalTimeLen());
                        }
                    }
                }
            }
        }
        
        Set<String> v_XSQLIDs = Help.toSort(v_Total.getRequestCount()).keySet();
        v_NowTime = new Date().getMinutes(-2).getTime();
        
        for (String v_XSQLID : v_XSQLIDs)
        {
            v_RequestCount = v_Total.getRequestCount().getSumValue(v_XSQLID);
            v_SuccessCount = v_Total.getSuccessCount().getSumValue(v_XSQLID);
            v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue(v_XSQLID);
            v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
            v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue(v_XSQLID).longValue());
            
            if ( v_RequestCount > v_SuccessCount )
            {
                v_OperateURL = i_ObjectValuePath + "?xsqlxid=" + v_XSQLID;
                
                if ( i_Cluster )
                {
                    v_OperateURL += "&cluster=Y"; 
                }
            }
            else
            {
                v_OperateURL = "#";
            }
           
            // 触发器的执行统计
            XSQL v_XSQL = (XSQL)v_XSQLs.get(v_XSQLID);
            if ( v_XSQL != null && v_XSQL.isTriggers() )
            {
                v_TriggerReqCount = v_Total.getTriggerReqCount().getSumValue(v_XSQLID);
                v_TriggerSucCount = v_Total.getTriggerSucCount().getSumValue(v_XSQLID);
            }
            else
            {
                v_TriggerReqCount = 0;
                v_TriggerSucCount = 0;
            }
            
            v_TriggerFaiCount = v_TriggerReqCount - v_TriggerSucCount;
            
            v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                     .replaceAll(":Name"         ,v_XSQLID)
                                     .replaceAll(":RequestCount" ,String.valueOf(v_RequestCount)                  + (v_TriggerReqCount > 0 ? "-T"+v_TriggerReqCount : ""))
                                     .replaceAll(":SuccessCount" ,String.valueOf(v_SuccessCount)                  + (v_TriggerReqCount > 0 ? "-T"+v_TriggerSucCount : ""))
                                     .replaceAll(":FailCount"    ,String.valueOf(v_RequestCount - v_SuccessCount) + (v_TriggerFaiCount > 0 ? "-T"+v_TriggerFaiCount : ""))
                                     .replaceAll(":ParamURL"     ,v_OperateURL)
                                     .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : (v_MaxExecTime.getTime() >= v_NowTime ? v_MaxExecTime.getFull() : "<span style='color:gray;'>" + v_MaxExecTime.getFull() + "</span>"))
                                     .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                     .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                           );
        }
        
        v_RequestCount = v_Total.getRequestCount().getSumValue();
        v_SuccessCount = v_Total.getSuccessCount().getSumValue();
        v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue();
        v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
        v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue().longValue());
        
        v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                 .replaceAll(":Name"         ,"合计")
                                 .replaceAll(":RequestCount" ,String.valueOf(v_RequestCount))
                                 .replaceAll(":SuccessCount" ,String.valueOf(v_SuccessCount))
                                 .replaceAll(":FailCount"    ,String.valueOf(v_RequestCount - v_SuccessCount))
                                 .replaceAll(":ParamURL"     ,"#")
                                 .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : v_MaxExecTime.getFull())
                                 .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                 .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                       );
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='analyseDB?type=Group' style='color:#AA66CC'>查看SQL组</a>" + StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseDB' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseDB?cluster=Y' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDB_RestTotal' style='color:#AA66CC'>重置统计</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDB_RestTotal&cluster=Y&sameTime=Y' style='color:#AA66CC'>集群重置</a>";
        }
        
        return StringHelp.replaceAll(this.getTemplateShowTotal()
                                    ,new String[]{":NameTitle"          ,":Title"              ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"SQL访问标识" + v_Goto ,"数据库访问量的概要统计" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取数据库组合SQL访问量的概要统计数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-22
     * @version     v1.0
     *
     * @return
     */
    public AnalyseDBTotal analyseDBGroup_Total()
    {
        AnalyseDBTotal      v_Total = new AnalyseDBTotal();
        Map<String ,Object> v_Objs  = XJava.getObjects(XSQLGroup.class);
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                XSQLGroup v_XSQLGroup = (XSQLGroup)v_Item.getValue();
                
                v_Total.getRequestCount().put(v_Item.getKey() ,v_XSQLGroup.getRequestCount());
                v_Total.getSuccessCount().put(v_Item.getKey() ,v_XSQLGroup.getSuccessCount());
                v_Total.getMaxExecTime() .put(v_Item.getKey() ,v_XSQLGroup.getExecuteTime() == null ? 0L : v_XSQLGroup.getExecuteTime().getTime());
                v_Total.getTotalTimeLen().put(v_Item.getKey() ,v_XSQLGroup.getSuccessTimeLen());
            }
        }
        
        return v_Total;
    }
    
    
    
    /**
     * 获取数据库访问量的概要统计数据 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-20
     * @version     v1.0
     *
     * @return
     */
    public AnalyseDBTotal analyseDB_Total()
    {
        AnalyseDBTotal      v_Total = new AnalyseDBTotal();
        Map<String ,Object> v_Objs  = XJava.getObjects(XSQL.class);
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                XSQL v_XSQL = (XSQL)v_Item.getValue();
                
                v_Total.getRequestCount().put(v_Item.getKey() ,v_XSQL.getRequestCount());
                v_Total.getSuccessCount().put(v_Item.getKey() ,v_XSQL.getSuccessCount());
                
                // 触发器的执行统计
                if ( v_XSQL.isTriggers() )
                {
                    v_Total.getTriggerReqCount().put(v_Item.getKey() ,v_XSQL.getTrigger().getRequestCount());
                    v_Total.getTriggerSucCount().put(v_Item.getKey() ,v_XSQL.getTrigger().getSuccessCount());
                }
                
                v_Total.getMaxExecTime() .put(v_Item.getKey() ,v_XSQL.getExecuteTime() == null ? 0L : v_XSQL.getExecuteTime().getTime());
                v_Total.getTotalTimeLen().put(v_Item.getKey() ,v_XSQL.getSuccessTimeLen());
            }
        }
        
        return v_Total;
    }
    
    
    
    /**
     * 重置数据库组合SQL访问量的概要统计数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-05
     * @version     v1.0
     *
     * @return
     */
    public void analyseDBGroup_Reset()
    {
        Map<String ,Object> v_Objs  = XJava.getObjects(XSQLGroup.class);
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                XSQLGroup v_XSQLGroup = (XSQLGroup)v_Item.getValue();
                
                v_XSQLGroup.reset();
            }
        }
    }
    
    
    
    /**
     * 重置数据库访问量的概要统计数据 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-05
     * @version     v1.0
     *
     * @return
     */
    public void analyseDB_RestTotal()
    {
        Map<String ,Object> v_Objs  = XJava.getObjects(XSQL.class);
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                XSQL v_XSQL = (XSQL)v_Item.getValue();
                
                v_XSQL.reset();
            }
        }
    }
    
    
    
    /**
     * 功能1. 查看XSQL对象执行错误的SQL语句（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-04
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseDB
     * @param  i_XSQLXID         XSQL对象的XID
     * @param  i_Cluster         是否为集群
     * @return
     */
    @SuppressWarnings("unchecked")
    public String analyseDBError(String i_BasePath ,String i_ObjectValuePath ,String i_XSQLXID ,boolean i_Cluster)
    {
        if ( Help.isNull(i_XSQLXID) )
        {
            return "";
        }
        
        try
        {
            List<XSQLLog> v_ErrorLogs = null;
            
            // 本机统计
            if ( !i_Cluster )
            {
                v_ErrorLogs = this.analyseDBError_Total(i_XSQLXID);
            }
            // 集群统计
            else
            {
                List<ClientSocket> v_Servers = Cluster.getClusters();
                v_ErrorLogs = new ArrayList<XSQLLog>();
                
                if ( !Help.isNull(v_Servers) )
                {
                    Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDBError_Total" ,new Object[]{i_XSQLXID});
                    
                    for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                    {
                        CommunicationResponse v_ResponseData = v_Item.getValue();
                        ClientSocket          v_Client       = v_Item.getKey();
                        String                v_ClientName   = "【" + v_Client.getHostName() + ":" + v_Client.getPort() + "】 ";
                        
                        if ( v_ResponseData.getResult() == 0 )
                        {
                            if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof List )
                            {
                                List<XSQLLog> v_XSQLLogs = (List<XSQLLog>)v_ResponseData.getData();
                                
                                for (XSQLLog v_XSQLLog : v_XSQLLogs)
                                {
                                    v_XSQLLog.setE(v_ClientName + Help.NVL(v_XSQLLog.getE()));
                                }
                                
                                v_ErrorLogs.addAll(v_XSQLLogs);
                            }
                        }
                    }
                }
            }
            
            String v_Content      = "";
            String v_OperateURL   = "#";
            String v_OperateTitle = "";
            XJSON  v_XJSON        = new XJSON();
            v_XJSON.setReturnNVL(true);
            v_XJSON.setAccuracy(true);
            
            XJSONObject v_Ret = v_XJSON.parser(v_ErrorLogs);
            if ( null != v_Ret )
            {
                v_Content = v_Ret.toJSONString();
            }
            else
            {
                v_Content = "{}";
            }
            
            return StringHelp.replaceAll(this.getTemplateShowObject() 
                                        ,new String[]{":HttpBasePath" ,":TitleInfo"      ,":XJavaObjectID" ,":Content" ,":OperateURL1" ,":OperateTitle1" ,":OperateURL2" ,":OperateTitle2" ,":OperateURL3" ,":OperateTitle3" ,":OperateURL4" ,":OperateTitle4" ,":OperateURL5" ,":OperateTitle5"} 
                                        ,new String[]{i_BasePath      ,"执行异常的SQL语句" ,i_XSQLXID        ,v_Content  ,v_OperateURL   ,v_OperateTitle   ,v_OperateURL   ,v_OperateTitle   ,v_OperateURL   ,v_OperateTitle   ,v_OperateURL   ,v_OperateTitle   ,v_OperateURL   ,v_OperateTitle});
        }
        catch (Exception exce)
        {
            return exce.toString();
        }
    }
    
    
    
    /**
     * 查看XSQL对象执行错误的SQL语句
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-22
     * @version     v1.0
     *
     * @param  i_XSQLXID         XSQL对象的XID
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<XSQLLog> analyseDBError_Total(String i_XSQLXID)
    {
        XSQL          v_XSQLMaster = XJava.getXSQL(i_XSQLXID ,false);
        String        v_XSQLOID    = v_XSQLMaster.getObjectID();
        List<XSQLLog> v_ErrorLogs  = new ArrayList<XSQLLog>();
        
        Busway<XSQLLog> v_SQLBuswayError = (Busway<XSQLLog>)XJava.getObject("$SQLBuswayError");
        if ( v_SQLBuswayError == null || v_SQLBuswayError.size() <= 0 )
        {
            return v_ErrorLogs;
        }
        
        for (Object v_Item : v_SQLBuswayError.getArray())
        {
            XSQLLog v_XSQLLog = (XSQLLog)v_Item;
            if ( v_XSQLLog != null )
            {
                if ( v_XSQLOID.equals(v_XSQLLog.getOid()) )
                {
                    v_ErrorLogs.add(v_XSQLLog);
                }
                else if ( v_XSQLMaster.isTriggers() )
                {
                    // 同时添加主XSQL相关的触发器的异常SQL信息  ZengWei(HY) Add 2017-01-06
                    for (XSQL v_XSQL : v_XSQLMaster.getTrigger().getXsqls())
                    {
                        if ( v_XSQL.getObjectID().equals(v_XSQLLog.getOid()) )
                        {
                            v_ErrorLogs.add(v_XSQLLog);
                        }
                    }
                }
            }
        }
        
        return v_ErrorLogs;
    }
    
    
    
    /**
     * 删除并重新创建数据库对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-11
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseDB
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analyseDBCreate(String i_BasePath ,String i_ObjectValuePath)
    {
        Map<String ,Object> v_XSQLMap      = XJava.getObjects(XSQL.class);
        StringBuilder       v_Buffer       = new StringBuilder();
        int                 v_Index        = 0;
        String              v_Content      = this.getTemplateShowResultContent();
        int                 v_TotalCount   = 0;
        int                 v_DropCount    = 0;
        int                 v_CreateCount  = 0;
        
        
        for (Map.Entry<String, Object> v_Item : v_XSQLMap.entrySet())
        {
            if ( v_Item.getValue() == null )
            {
                continue;
            }
            
            XSQL v_XSQL = (XSQL)v_Item.getValue();
            if ( Help.isNull(v_XSQL.getCreateObjectName()) )
            {
                continue;
            }
            
            v_TotalCount++;
            
            try
            {
                XSQLDBMetadata v_XSQLDBMetadata = new XSQLDBMetadata();
                boolean        v_IsExists       = v_XSQLDBMetadata.isExists(v_XSQL);
                boolean        v_DropRet        = false;
                boolean        v_CreateRet      = false;
                String         v_OprStatus      = "";
                
                if ( v_IsExists )
                {
                    v_DropRet = v_XSQLDBMetadata.dropObject(v_XSQL);
                    
                    if ( v_DropRet )
                    {
                        v_DropCount++;
                        v_CreateRet = v_XSQL.createObject();
                        
                        if ( v_CreateRet )
                        {
                            v_CreateCount++;
                            v_OprStatus = "成功";
                        }
                        else
                        {
                            v_OprStatus = "创建对象异常";
                        }
                    }
                    else
                    {
                        v_OprStatus = "删除对象异常";
                    }
                }
                else
                {
                    v_DropCount++;
                    v_CreateRet = v_XSQL.createObject();
                    if ( v_CreateRet )
                    {
                        v_CreateCount++;
                        v_OprStatus = "成功";
                    }
                    else
                    {
                        v_OprStatus = "创建对象异常";
                    }
                }
                
                
                v_Buffer.append(StringHelp.replaceAll(v_Content 
                                ,new String[]{":No" 
                                             ,":OprName"
                                             ,":OprTime" 
                                             ,":OprStatus"} 
                                ,new String[]{String.valueOf(++v_Index)
                                             ,v_XSQL.getCreateObjectName() + "  " + Help.NVL(v_XSQL.getComment())
                                             ,Date.getNowTime().getFullMilli()
                                             ,v_OprStatus
                                }));
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        String v_SumInfo = "";
        if ( v_TotalCount == v_CreateCount )
        {
            v_SumInfo = "共成功创建 " + v_CreateCount + " 个对象";
        }
        else
        {
            v_SumInfo = "成功创建 " + v_CreateCount + " 个对象；"
                      + "创建异常 " + (v_DropCount  - v_CreateCount) + " 个对象；"
                      + "删除异常 " + (v_TotalCount - v_DropCount) + " 个对象；";
        }
        
        v_Buffer.append(StringHelp.replaceAll(v_Content 
                       ,new String[]{":No" 
                                    ,":OprName"
                                    ,":OprTime" 
                                    ,":OprStatus"} 
                       ,new String[]{String.valueOf(++v_Index)
                                    ,"合计：" + v_SumInfo
                                    ,Date.getNowTime().getFullMilli()
                                    ,v_TotalCount == 0 ? "未找配置" : v_TotalCount == v_CreateCount ? "全部成功" : "有异常"
                       }));

        return StringHelp.replaceAll(this.getTemplateShowResult()
                                    ,new String[]{":Title"          ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"重建数据库对象列表" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 功能1. 查看对象信息
     * 功能2. 执行对象方法（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-16
     * @version     v1.0
     *              v2.0  2017-01-17  添加：集群顺次执行对象方法的功能
     *              v3.0  2017-01-20  添加：集群同时执行对象方法的功能（并发）
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @param  i_XJavaObjectID   对象标识ID 
     * @param  i_CallMethod      对象方法的全名称（可为空）
     * @param  i_Cluster         是否为集群
     * @param  i_SameTime        是否为同时执行（并发操作）
     * @return
     */
    public String analyseObject(String i_BasePath ,String i_ObjectValuePath ,String i_XJavaObjectID ,String i_CallMethod ,boolean i_Cluster ,boolean i_SameTime)
    {
        if ( Help.isNull(i_XJavaObjectID) )
        {
            return "";
        }
        
        try
        {
            Object v_Object = XJava.getObject(i_XJavaObjectID);
            if ( v_Object == null )
            {
                return "";
            }
            
            String v_Content       = "";
            String v_OperateURL1   = "#";
            String v_OperateTitle1 = "";
            String v_OperateURL2   = "#";
            String v_OperateTitle2 = "";
            String v_OperateURL3   = "#";
            String v_OperateTitle3 = "";
            String v_OperateURL4   = "#";
            String v_OperateTitle4 = "";
            String v_OperateURL5   = "#";
            String v_OperateTitle5 = "";
            XJSON  v_XJSON         = new XJSON();
            v_XJSON.setReturnNVL(true);
            v_XJSON.setAccuracy(true);
            
            // 功能1. 查看对象信息
            if ( Help.isNull(i_CallMethod) )
            {
                XJSONObject v_Ret = v_XJSON.parser(v_Object);
                if ( null != v_Ret )
                {
                    v_Content = v_Ret.toJSONString();
                }
                else
                {
                    v_Content = "{}";
                }
                
                if ( v_Object.getClass() == Job.class )
                {
                    Job v_Job = (Job)v_Object;
                    v_OperateURL1   = i_ObjectValuePath + "?xid=" + v_Job.getXjavaID() + "&call=" + v_Job.getMethodName();
                    v_OperateTitle1 = "执行任务";
                    v_OperateURL2   = v_OperateURL1 + "&cluster=Y";
                    v_OperateTitle2 = "集群顺次执行任务";
                    v_OperateURL3   = v_OperateURL2 + "&sameTime=Y";
                    v_OperateTitle3 = "集群同时执行任务";
                }
                else if ( v_Object.getClass() == XSQLGroup.class )
                {
                    v_OperateURL1   = i_ObjectValuePath + "?xid=" + i_XJavaObjectID + "&call=executes";
                    v_OperateTitle1 = "执行SQL组";
                    v_OperateURL2   = v_OperateURL1 + "&cluster=Y";
                    v_OperateTitle2 = "集群顺次执行SQL组";
                    v_OperateURL3   = v_OperateURL2 + "&sameTime=Y";
                    v_OperateTitle3 = "集群同时执行SQL组";
                    v_OperateURL4   = i_ObjectValuePath + "?xid=" + i_XJavaObjectID + "&call=openLog";
                    v_OperateTitle4 = "开启日志";
                    v_OperateURL5   = i_ObjectValuePath + "?xid=" + i_XJavaObjectID + "&call=closeLog";
                    v_OperateTitle5 = "关闭日志";
                }
                
                return StringHelp.replaceAll(this.getTemplateShowObject() 
                                            ,new String[]{":HttpBasePath" ,":TitleInfo"  ,":XJavaObjectID" ,":Content" ,":OperateURL1" ,":OperateTitle1" ,":OperateURL2" ,":OperateTitle2" ,":OperateURL3" ,":OperateTitle3" ,":OperateURL4"  ,":OperateTitle4" ,":OperateURL5" ,":OperateTitle5"} 
                                            ,new String[]{i_BasePath      ,"对象信息"     ,i_XJavaObjectID  ,v_Content  ,v_OperateURL1  ,v_OperateTitle1  ,v_OperateURL2  ,v_OperateTitle2  ,v_OperateURL3  ,v_OperateTitle3  ,v_OperateURL4   ,v_OperateTitle4  ,v_OperateURL5  ,v_OperateTitle5});
            }
            // 功能2. 执行对象方法 
            else
            {
                Return<String> v_RetInfo = this.analyseObject_Execute(i_XJavaObjectID ,i_CallMethod ,i_Cluster ,i_SameTime);
                v_Content = v_RetInfo.paramStr;
                
                if ( !i_Cluster )
                {
                    List<Method> v_Methods = MethodReflect.getMethodsIgnoreCase(v_Object.getClass() ,i_CallMethod ,0);
                    return StringHelp.replaceAll(this.getTemplateShowObject() 
                                                ,new String[]{":HttpBasePath" ,":TitleInfo"    ,":XJavaObjectID"                                           ,":Content" ,":OperateURL1" ,":OperateTitle1" ,":OperateURL2" ,":OperateTitle2" ,":OperateURL3" ,":OperateTitle3" ,":OperateURL4" ,":OperateTitle4" ,":OperateURL5" ,":OperateTitle5"} 
                                                ,new String[]{i_BasePath      ,"对象方法执行结果" ,i_XJavaObjectID + "." + v_Methods.get(0).getName() + "()"  ,v_Content  ,""});
                }
                else
                {
                    return v_Content;
                }
            }
        }
        catch (Exception exce)
        {
            return exce.toString();
        }
    }
    
    
    
    /**
     * 执行对象方法（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-17
     * @version     v1.0
     *
     * @param  i_XJavaObjectID   对象标识ID 
     * @param  i_CallMethod      对象方法的全名称（可为空）
     * @param  i_Cluster         是否为集群
     * @param  i_SameTime        是否为同时执行（并发操作）
     * @return
     */
    @SuppressWarnings("unchecked")
    public Return<String> analyseObject_Execute(String i_XJavaObjectID ,String i_CallMethod ,boolean i_Cluster ,boolean i_SameTime)
    {
        Return<String> v_RetInfo = new Return<String>();
        Object         v_Object  = XJava.getObject(i_XJavaObjectID);
        
        if ( v_Object == null )
        {
            return v_RetInfo.paramStr("XID is not exists.");
        }
        
        List<Method> v_Methods = MethodReflect.getMethodsIgnoreCase(v_Object.getClass() ,i_CallMethod ,0);
        if ( Help.isNull(v_Methods) )
        {
            return v_RetInfo.paramStr("Can not find method [" + i_XJavaObjectID + "." + i_CallMethod + "()]!");
        }
        
        // 本机重新加载
        if ( !i_Cluster )
        {
            String v_Content = "";
            XJSON  v_XJSON   = new XJSON();
            v_XJSON.setReturnNVL(true);
            v_XJSON.setAccuracy(true);
            
            try
            {
                
                XSQLGroup v_GXSQ   = null;
                boolean   v_OldLog = false; 
                if ( XSQLGroup.class.equals(v_Object.getClass()) )
                {
                    // 当为XSQL组时，自动打开日志模式
                    v_GXSQ = (XSQLGroup)v_Object;
                    v_OldLog = v_GXSQ.isLog();
                    v_GXSQ.setLog(true);
                }
                
                Object v_CallRet = v_Methods.get(0).invoke(v_Object);
                
                if ( v_GXSQ != null )
                {
                    v_GXSQ.setLog(v_OldLog);
                }
                
                if ( v_CallRet != null )
                {
                    XJSONObject v_Ret = v_XJSON.parser(v_CallRet);
                    if ( null != v_Ret )
                    {
                        v_Content = v_Ret.toJSONString();
                    }
                    else
                    {
                        v_Content = "{\"return\":\"" + v_CallRet.toString() + "\"}";
                    }
                }
                else if ( null != v_Methods.get(0).getReturnType()
                       && "void".equals(v_Methods.get(0).getReturnType().getName()) )
                {
                    v_Content = "{\"return\":\"void\"}";
                }
                else
                {
                    v_Content = "{\"return\":\"null\"}";
                }
    
                return v_RetInfo.paramStr(v_Content).set(true);
            }
            catch (Exception exce)
            {
                return v_RetInfo.paramStr(exce.toString());
            }
        }
        // 集群重新加载
        else
        {
            long                                     v_StartTime        = Date.getNowTime().getTime();
            StringBuilder                            v_Ret              = new StringBuilder();
            Map<ClientSocket ,CommunicationResponse> v_ClusterResponses = null;
            
            if ( i_SameTime )
            {
                v_ClusterResponses = ClientSocketCluster.sendCommands(Cluster.getClusters() ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseObject_Execute" ,new Object[]{i_XJavaObjectID ,i_CallMethod ,false ,false});
            }
            else
            {
                v_ClusterResponses = ClientSocketCluster.sendCommands(Cluster.getClusters()                           ,"AnalyseBase" ,"analyseObject_Execute" ,new Object[]{i_XJavaObjectID ,i_CallMethod ,false ,false});
            }
            
            v_Ret.append("总体用时：").append(Date.toTimeLen(Date.getNowTime().getTime() - v_StartTime)).append("<br><br>");
            
            // 处理结果
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ClusterResponses.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                v_ClusterResponses.put(v_Item.getKey() ,v_ResponseData);
                
                v_Ret.append(v_ResponseData.getEndTime().getFullMilli()).append("：").append(v_Item.getKey().getHostName()).append(" execute ");
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() == null || !(v_ResponseData.getData() instanceof Return) )
                    {
                        v_Ret.append("is Error.");
                    }
                    else
                    {
                        Return<String> v_ExecRet = (Return<String>)v_ResponseData.getData();
                        
                        if ( v_ExecRet.booleanValue() )
                        {
                            v_Ret.append("is OK.");
                        }
                        else
                        {
                            v_Ret.append("is Error(").append(v_ExecRet.paramStr).append(").");
                        }
                    }
                }
                else
                {
                    v_Ret.append("is Error(").append(v_ResponseData.getResult()).append(").");
                }
                v_Ret.append("<br>");
            }
            
            return v_RetInfo.paramStr(v_Ret.toString());
        }
    }
    
    
    
    /**
     * 功能1. 查看前缀匹配的对象列表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-06
     * @version     v1.0
     *              v2.0  2017-03-01  添加：显示对象.toString()的信息。
     *                                     特殊情况1: 对于Java默认的toString()返回值不予显示。
     *                                     特殊情况2: 对于集合对象，不予显示。
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @param  i_XIDPrefix       对象标识符的前缀(区分大小写)
     * @return
     */
    public String analyseObjects(String i_BasePath ,String i_ObjectValuePath ,String i_XIDPrefix)
    {
        Map<String ,Object>  v_Objects      = (Map<String ,Object>)XJava.getObjects(i_XIDPrefix);
        StringBuilder        v_Buffer       = new StringBuilder();
        int                  v_Index        = 0;
        String               v_Content      = this.getTemplateShowObjectsContent();
        
        v_Objects = Help.toSort(v_Objects);
        
        for (Entry<String, Object> v_Item : v_Objects.entrySet())
        {
            String v_Info = "";
            
            if ( v_Item.getValue() != null )
            {
                v_Info = "";
                
                if ( MethodReflect.isExtendImplement(v_Item.getValue() ,Set.class)
                  || MethodReflect.isExtendImplement(v_Item.getValue() ,Map.class)
                  || MethodReflect.isExtendImplement(v_Item.getValue() ,List.class)
                  || MethodReflect.isExtendImplement(v_Item.getValue() ,Queue.class) )
                {
                    // 对于集合对象，不予显示
                    v_Info = "";
                }
                else
                {
                    v_Info = Help.NVL(v_Item.getValue().toString());
                    
                    if ( v_Info.startsWith(v_Item.getValue().getClass().getName()) )
                    {
                        // 对于默认的toString()返回值不予显示
                        v_Info = "";
                    }
                }
            }
            
            v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                 ,new String[]{":No" 
                                                              ,":Name" 
                                                              ,":Info"
                                                              ,":OperateURL" 
                                                              ,":OperateTitle"} 
                                                 ,new String[]{String.valueOf(++v_Index)
                                                              ,v_Item.getKey()
                                                              ,v_Info
                                                              ,i_ObjectValuePath + "?xid=" + v_Item.getKey()
                                                              ,"查看详情"
                                                              })
                           );
        }
        
        return StringHelp.replaceAll(this.getTemplateShowObjects()
                                    ,new String[]{":Title"  ,":Column01Title" ,":Column02Title"  ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"对象列表" ,"对象标识"         ,"对象.toString()" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 功能1：查看XJava配置文件列表
     * 功能2：重新加载XJava配置文件（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-04
     * @version     v1.0
     *              v2.0  2017-01-17  添加：集群重新加载XJava配置文件的功能
     *
     * @param  i_BasePath       服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ReLoadPath     重新加载的URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @param  i_XFile          XJava配置文件名称（可为空）
     * @param  i_Cluster        是否为集群
     * @return
     */
    @SuppressWarnings("unchecked")
    public String analyseXFile(String i_BasePath ,String i_ReLoadPath ,String i_XFile ,boolean i_Cluster)
    {
        Map<String ,Object>  v_XFileNames   = (Map<String ,Object>)XJava.getObject(AppInitConfig.$XFileNames_XID);
        StringBuilder        v_Buffer       = new StringBuilder();
        int                  v_Index        = 0;
        String               v_Content      = this.getTemplateShowXFilesContent();
        
        if ( Help.isNull(i_XFile) )
        {
            for (String v_XFile : v_XFileNames.keySet())
            {
                if ( !Help.isNull(v_XFile) )
                {
                    v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                         ,new String[]{":No" 
                                                                      ,":Name" 
                                                                      ,":OperateURL1" 
                                                                      ,":OperateTitle1"
                                                                      ,":OperateURL2" 
                                                                      ,":OperateTitle2"} 
                                                         ,new String[]{String.valueOf(++v_Index)
                                                                      ,v_XFile
                                                                      ,i_ReLoadPath + "?xfile=" + v_XFile
                                                                      ,"重新加载"
                                                                      ,i_ReLoadPath + "?xfile=" + v_XFile + "&cluster=Y"
                                                                      ,"集群重新加载"
                                                                      })
                                   );
                }
            }
            
            String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='analyseObject?cluster=Y' style='color:#AA66CC'>查看集群服务</a>";
            
            return StringHelp.replaceAll(this.getTemplateShowXFiles()
                                        ,new String[]{":Title"          ,":Column01Title"        ,":HttpBasePath" ,":Content"}
                                        ,new String[]{"XJava配置文件列表" ,"XJava配置文件" + v_Goto ,i_BasePath      ,v_Buffer.toString()});
        }
        else
        {
            return this.analyseXFile_Reload(i_XFile ,i_Cluster);
        }
    }
    
    
    
    /**
     * 重新加载XJava配置文件（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-17
     * @version     v1.0
     *
     * @param i_XFile    XJava配置文件名称
     * @param i_Cluster  是否为集群
     * @return
     */
    @SuppressWarnings("unchecked")
    public String analyseXFile_Reload(String i_XFile ,boolean i_Cluster)
    {
        Map<String ,Object> v_XFileNames = (Map<String ,Object>)XJava.getObject(AppInitConfig.$XFileNames_XID);
        
        if ( v_XFileNames.containsKey(i_XFile) )
        {
            // 本机重新加载
            if ( !i_Cluster )
            {
                AppInitConfig v_AConfig  = (AppInitConfig)v_XFileNames.get(i_XFile);
                File          v_XFileObj = new File(Help.getWebINFPath() + i_XFile);
                
                if ( v_XFileObj.exists() && v_XFileObj.isFile() )
                {
                    v_AConfig.initW(i_XFile ,Help.getWebINFPath());
                }
                else
                {
                    v_AConfig.init(i_XFile);
                }
                
                return Date.getNowTime().getFullMilli() + ": Has completed re loading, please check the console log.";
            }
            // 集群重新加载 
            else
            {
                StringBuilder v_Ret = new StringBuilder();
                for (ClientSocket v_Client : Cluster.getClusters())
                {
                    CommunicationResponse v_ResponseData = v_Client.sendCommand("AnalyseBase" ,"analyseXFile_Reload" ,new Object[]{i_XFile ,false});
                    
                    v_Ret.append(Date.getNowTime().getFullMilli()).append("：").append(v_Client.getHostName()).append(" reload ");
                    v_Ret.append(v_ResponseData.getResult() == 0 ? "OK." : "Error(" + v_ResponseData.getResult() + ").").append("<br>");
                }
                
                return v_Ret.toString();
            }
        }
        else
        {
            return Date.getNowTime().getFullMilli() + ": Configuration file not found.";
        }
    }
    
    
    
    /**
     * 功能1：查看集群服务列表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-18
     * @version     v1.0
     *
     * @param  i_BasePath       服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ReLoadPath     重新加载的URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @return
     */
    public String analyseCluster(String i_BasePath ,String i_ReLoadPath)
    {
        List<ClientSocket>   v_Servers      = Cluster.getClusters();
        StringBuilder        v_Buffer       = new StringBuilder();
        int                  v_Index        = 0;
        String               v_Content      = this.getTemplateShowClusterContent();
        List<ClusterReport>  v_Clusters     = new ArrayList<ClusterReport>();
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseCluster_Info");
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData  = v_Item.getValue();
                ClusterReport         v_ClusterReport = null;
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof ClusterReport )
                    {
                        v_ClusterReport = (ClusterReport)v_ResponseData.getData();
                        v_ClusterReport.setServerStatus("正常");
                    }
                }
                
                if ( v_ClusterReport == null )
                {
                    v_ClusterReport = new ClusterReport();
                    v_ClusterReport.setStartTime("-");
                    v_ClusterReport.setServerStatus("<font color='red'>异常</font>");
                }
                
                v_ClusterReport.setHostName(v_Item.getKey().getHostName());
                v_Clusters.add(v_ClusterReport);
            }
        }
        else
        {
            v_Clusters.add(this.analyseCluster_Info());
            v_Clusters.get(0).setHostName("127.0.0.1");
        }
        
        Help.toSort(v_Clusters ,"startTime Desc" ,"hostName");
        
        for (ClusterReport v_CReport : v_Clusters)
        {
            Map<String ,String> v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"           ,String.valueOf(++v_Index));
            v_RKey.put(":ServerName"   ,v_CReport.getHostName());
            v_RKey.put(":MaxMemory"    ,StringHelp.getComputeUnit(v_CReport.getMaxMemory()));
            v_RKey.put(":TotalMemory"  ,StringHelp.getComputeUnit(v_CReport.getTotalMemory()));
            v_RKey.put(":FreeMemory"   ,StringHelp.getComputeUnit(v_CReport.getFreeMemory()));
            v_RKey.put(":ThreadCount"  ,v_CReport.getThreadCount() + "");
            v_RKey.put(":StartTime"    ,v_CReport.getStartTime());
            v_RKey.put(":ServerStatus" ,v_CReport.getServerStatus());
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        }
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='analyseObject' style='color:#AA66CC'>查看XJava配置</a>";
        
        return StringHelp.replaceAll(this.getTemplateShowCluster()
                                    ,new String[]{":Title"     ,":Column01Title"   ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"集群服务列表" ,"集群服务" + v_Goto ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取服务信息（如启动时间等）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-18
     * @version     v1.0
     *
     * @return
     */
    public ClusterReport analyseCluster_Info()
    {
        return new ClusterReport($ServerStartTime);
    }
    
    
    
    /**
     * 查看线程池运行情况（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-26
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?ThreadPool=Y
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analyseThreadPool(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster)
    {
        StringBuilder          v_Buffer         = new StringBuilder();
        int                    v_Index          = 0;
        String                 v_Content        = this.getTemplateShowThreadPoolContent();
        int                    v_TotalExecCount = 0;
        AnalyseThreadPoolTotal v_Total          = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseThreadPool_Total();
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseThreadPoolTotal("合计");
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseThreadPool_Total");
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof AnalyseThreadPoolTotal )
                        {
                            AnalyseThreadPoolTotal v_TempTotal = (AnalyseThreadPoolTotal)v_ResponseData.getData();
                            
                            v_TempTotal.setHostName(v_Item.getKey().getHostName() + ":" + v_Item.getKey().getPort());
                            
                            // 线程号前加主机IP:Port
                            for (ThreadReport v_TReport : v_TempTotal.getReports())
                            {
                                v_TReport.setThreadNo(v_TempTotal.getHostName() + v_TReport.getThreadNo());
                            }
                            
                            v_Total.getReports().addAll( v_TempTotal.getReports());
                            v_Total.setThreadCount(      v_Total.getThreadCount()       + v_TempTotal.getThreadCount());
                            v_Total.setIdleThreadCount(  v_Total.getIdleThreadCount()   + v_TempTotal.getIdleThreadCount());
                            v_Total.setActiveThreadCount(v_Total.getActiveThreadCount() + v_TempTotal.getActiveThreadCount());
                            v_Total.setWaitTaskCount(    v_Total.getWaitTaskCount()     + v_TempTotal.getWaitTaskCount());
                        }
                    }
                }
            }
        }
        
        Help.toSort(v_Total.getReports() ,"threadNo");
        
        for (ThreadReport v_TReport : v_Total.getReports())
        {
            Map<String ,String> v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"        ,String.valueOf(++v_Index));
            v_RKey.put(":ThreadNo"  ,v_TReport.getThreadNo());
            v_RKey.put(":TaskName"  ,v_TReport.getTaskName());
            v_RKey.put(":TotalTime" ,Date.toTimeLen(v_TReport.getTotalTime()));
            v_RKey.put(":RunStatus" ,v_TReport.getRunStatus());
            v_RKey.put(":LastTime"  ,v_TReport.getLastTime());
            v_RKey.put(":ExecCount" ,v_TReport.getExecCount() + "");
            v_RKey.put(":TaskDesc"  ,v_TReport.getTaskDesc());
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
            
            v_TotalExecCount += v_TReport.getExecCount();
        }
        
        v_Buffer.append(v_Content.replaceAll(":No"        ,String.valueOf(++v_Index))
                                 .replaceAll(":ThreadNo"  ,"合计")
                                 .replaceAll(":TaskName"  ,"-")
                                 .replaceAll(":TotalTime" ,"-")
                                 .replaceAll(":RunStatus" ,"-")
                                 .replaceAll(":LastTime"  ,"-")
                                 .replaceAll(":ExecCount" ,v_TotalExecCount + "")
                                 .replaceAll(":TaskDesc"  ,"Total: "             + v_Total.getThreadCount() 
                                                         + "  Idle: "            + v_Total.getIdleThreadCount() 
                                                         + "  Active: "          + v_Total.getActiveThreadCount()
                                                         + "  Queue wait task: " + v_Total.getWaitTaskCount())
                       );
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseObject?ThreadPool=Y' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?ThreadPool=Y&cluster=Y' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;") + Date.getNowTime().getFull();
        
        return StringHelp.replaceAll(this.getTemplateShowThreadPool()
                                    ,new String[]{":GotoTitle" ,":Title"       ,":HttpBasePath" ,":Content"}
                                    ,new String[]{v_Goto       ,"线程池运行情况" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 本机线程池运行情况
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-27
     * @version     v1.0
     *
     * @return
     */
    public AnalyseThreadPoolTotal analyseThreadPool_Total()
    {
        return new AnalyseThreadPoolTotal();
    }
    
    
    
    /**
     * 查看定时任务运行情况（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-28
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?Job=Y
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analyseJob(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster)
    {
        StringBuilder   v_Buffer  = new StringBuilder();
        int             v_Index   = 0;
        String          v_Content = this.getTemplateShowJobContent();
        AnalyseJobTotal v_Total   = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseJob_Total();
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseJobTotal("合计");
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseJob_Total");
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof AnalyseJobTotal )
                        {
                            AnalyseJobTotal v_TempTotal = (AnalyseJobTotal)v_ResponseData.getData();
                            
                            v_Total.getReports().addAll( v_TempTotal.getReports());
                        }
                    }
                }
            }
        }
        
        Help.toSort(v_Total.getReports() ,"nextTime" ,"lastTime");
        
        for (JobReport v_JReport : v_Total.getReports())
        {
            Map<String ,String> v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"           ,String.valueOf(++v_Index));
            v_RKey.put(":JobID"        ,v_JReport.getJobID());
            v_RKey.put(":IntervalType" ,v_JReport.getIntervalType());
            v_RKey.put(":IntervalLen"  ,v_JReport.getIntervalLen());
            v_RKey.put(":LastTime"     ,v_JReport.getLastTime());
            v_RKey.put(":NextTime"     ,v_JReport.getNextTime());
            v_RKey.put(":JobDesc"      ,v_JReport.getJobDesc());
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        }
        
        /*
        v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                 .replaceAll(":JobID"        ,"合计")
                                 .replaceAll(":IntervalType" ,"-")
                                 .replaceAll(":IntervalLen"  ,"-")
                                 .replaceAll(":LastTime"     ,"-")
                                 .replaceAll(":NextTime"     ,"-")
                                 .replaceAll(":JobDesc"      ,"Total: " + v_Total.getReports().size())
                       );
        */
        
        String v_GotoTitle = StringHelp.lpad("" ,4 ,"&nbsp;") + Date.getNowTime().getFull();
        
        return StringHelp.replaceAll(this.getTemplateShowJob()
                                    ,new String[]{":GotoTitle" ,":Title"         ,":HttpBasePath" ,":Content"}
                                    ,new String[]{v_GotoTitle  ,"定时任务运行情况" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 本机定时任务运行情况
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-28
     * @version     v1.0
     *
     * @return
     */
    public AnalyseJobTotal analyseJob_Total()
    {
        return new AnalyseJobTotal();
    }
    
    
    
    /**
     * 功能1：本机数据库连接池信息
     * 功能2：集群数据库连接池信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-05
     * @version     v1.0
     *
     * @param  i_BasePath       服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ReLoadPath     重新加载的URL。如：http://127.0.0.1:80/hy/../analyseObject?DBG=Y
     * @param  i_Cluster        是否为集群
     * @return
     */
    public String analyseDataSourceGroup(String i_BasePath ,String i_ReLoadPath ,boolean i_Cluster)
    {
        StringBuilder   v_Buffer  = new StringBuilder();
        int             v_Index   = 0;
        String          v_Content = this.getTemplateShowDSGContent();
        AnalyseDSGTotal v_Total   = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseDataSourceGroup_Total();
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseDSGTotal("合计");
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDataSourceGroup_Total");
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof AnalyseDSGTotal )
                        {
                            AnalyseDSGTotal v_TempTotal = (AnalyseDSGTotal)v_ResponseData.getData();
                            
                            if ( !Help.isNull(v_TempTotal.getReports()) )
                            {
                                for (DataSourceGroupReport v_Report : v_TempTotal.getReports().values())
                                {
                                    DataSourceGroupReport v_TR = v_Total.getReports().get(v_Report.getDsgID());
                                    
                                    if ( v_TR == null )
                                    {
                                        v_Total.getReports().put(v_Report.getDsgID() ,v_Report);
                                    }
                                    else
                                    {
                                        if ( v_TR.getDbProductType().indexOf(v_Report.getDbProductType()) < 0 )
                                        {
                                            // 不同数据库类型就拼接
                                            String v_DBPType = v_TR.getDbProductType();
                                            if ( !Help.isNull(v_DBPType) )
                                            {
                                                v_DBPType += "<br>";
                                            }
                                            v_TR.setDbProductType(v_DBPType + v_Report.getDbProductType());
                                        }
                                        
                                        if ( "异常".equals(v_Report.getDsgStatus()) )
                                        {
                                            String v_DsgStatus = v_TR.getDsgStatus();
                                            if ( !Help.isNull(v_DsgStatus) )
                                            {
                                                v_DsgStatus += "<br>";
                                            }
                                            v_TR.setDsgStatus(v_DsgStatus + "<font color='red'>异常：</font>" + v_Item.getKey().getHostName());
                                        }
                                        
                                        v_TR.setConnActiveCount(v_TR.getConnActiveCount() + v_Report.getConnActiveCount());          // 合计值
                                        v_TR.setConnMaxUseCount(Math.max(v_TR.getConnMaxUseCount() ,v_Report.getConnMaxUseCount())); // 最大峰值
                                        v_TR.setDataSourcesSize((v_TR.getDataSourcesSize() + v_Report.getDataSourcesSize()) / 2);    // 平均值
                                        v_TR.setConnLastTime(v_TR.getConnLastTime().compareTo(v_Report.getConnLastTime()) >= 0 ? v_TR.getConnLastTime() : v_Report.getConnLastTime());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Help.toSort(v_Total.getReports());
        
        for (DataSourceGroupReport v_Report : v_Total.getReports().values())
        {
            Map<String ,String> v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"              ,String.valueOf(++v_Index));
            v_RKey.put(":DSGID"           ,v_Report.getDsgID());
            v_RKey.put(":DBProductType"   ,v_Report.getDbProductType());
            v_RKey.put(":DataSourcesSize" ,v_Report.getDataSourcesSize() + "");
            v_RKey.put(":ConnActiveCount" ,v_Report.getConnActiveCount() + "");
            v_RKey.put(":ConnMaxUseCount" ,v_Report.getConnMaxUseCount() + "");
            v_RKey.put(":ConnLastTime"    ,v_Report.getConnLastTime());
            v_RKey.put(":DSGStatus"       ,v_Report.getDsgStatus());
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        }
        
        String v_GotoTitle = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_GotoTitle += "<a href='analyseObject?DBG=Y' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_GotoTitle += "<a href='analyseObject?DBG=Y&cluster=Y' style='color:#AA66CC'>查看集群</a>";
        }
        
        return StringHelp.replaceAll(this.getTemplateShowDSG()
                                    ,new String[]{":GotoTitle" ,":Title"              ,":HttpBasePath" ,":Content"}
                                    ,new String[]{v_GotoTitle  ,"数据库连接池组使用情况" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取数据库连接池信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-05
     * @version     v1.0
     *
     * @return
     */
    public AnalyseDSGTotal analyseDataSourceGroup_Total()
    {
        return new AnalyseDSGTotal();
    }
    
    
    
    private String getTemplateLogon()
    {
        return this.getTemplateContent("template.login.html");
    }
    
    
    
    private String getTemplateLogonSSO()
    {
        return this.getTemplateContent("template.loginSSO.html");
    }
    
    
    
    private String getTemplateShowTotal()
    {
        return this.getTemplateContent("template.showTotal.html");
    }
    
    
    
    private String getTemplateShowTotalContent()
    {
        return this.getTemplateContent("template.showTotalContent.html");
    }
    
    
    
    private String getTemplateShowObject()
    {
        return this.getTemplateContent("template.showObject.html");
    }
    
    
    
    private String getTemplateShowObjects()
    {
        return this.getTemplateContent("template.showObjects.html");
    }
    
    
    
    private String getTemplateShowObjectsContent()
    {
        return this.getTemplateContent("template.showObjectsContent.html");
    }
    
    
    
    private String getTemplateShowXFiles()
    {
        return this.getTemplateContent("template.showXFiles.html");
    }
    
    
    
    private String getTemplateShowXFilesContent()
    {
        return this.getTemplateContent("template.showXFilesContent.html");
    }
    
    
    
    private String getTemplateShowCluster()
    {
        return this.getTemplateContent("template.showCluster.html");
    }
    
    
    
    private String getTemplateShowClusterContent()
    {
        return this.getTemplateContent("template.showClusterContent.html");
    }
    
    
    
    private String getTemplateShowResult()
    {
        return this.getTemplateContent("template.showResult.html");
    }
    
    
    
    private String getTemplateShowResultContent()
    {
        return this.getTemplateContent("template.showResultContent.html");
    }
    
    
    
    private String getTemplateShowThreadPool()
    {
        return this.getTemplateContent("template.showThreadPool.html");
    }
    
    
    
    private String getTemplateShowThreadPoolContent()
    {
        return this.getTemplateContent("template.showThreadPoolContent.html");
    }
    
    
    
    private String getTemplateShowJob()
    {
        return this.getTemplateContent("template.showJob.html");
    }
    
    
    
    private String getTemplateShowJobContent()
    {
        return this.getTemplateContent("template.showJobContent.html");
    }
    
    
    
    private String getTemplateShowDSG()
    {
        return this.getTemplateContent("template.showDSG.html");
    }
    
    
    
    private String getTemplateShowDSGContent()
    {
        return this.getTemplateContent("template.showDSGContent.html");
    }
    
    
    
    /**
     * 获取模板内容（有缓存机制）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_TemplateName  模板名称
     * @return
     */
    protected String getTemplateContent(String i_TemplateName)
    {
        if ( $TemplateCaches.containsKey(i_TemplateName) )
        {
            return $TemplateCaches.get(i_TemplateName);
        }
        
        String v_Content = "";
        
        try
        {
            v_Content = this.getFileContent(i_TemplateName);
            $TemplateCaches.put(i_TemplateName ,v_Content);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Content;
    }
    
    
    
    /**
     * 获取文件内容
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_FileName  文件名称(无须文件路径)。此文件应在同级目录中保存
     * @return
     * @throws Exception
     */
    protected String getFileContent(String i_FileName) throws Exception
    {
        FileHelp    v_FileHelp    = new FileHelp();
        String      v_PackageName = "org.hy.common.xml.plugins.analyse".replaceAll("\\." ,"/");
        InputStream v_InputStream = this.getClass().getResourceAsStream("/" + v_PackageName + "/" + i_FileName);
        
        return v_FileHelp.getContent(v_InputStream ,"UTF-8");
    }
    
    
    
    /**
     * 获取参数Param对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_XJavaID
     * @return
     */
    protected Param getParam(String i_XJavaID)
    {
        return (Param)XJava.getObject(i_XJavaID);
    }
    
}
