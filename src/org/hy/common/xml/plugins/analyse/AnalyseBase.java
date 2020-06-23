package org.hy.common.xml.plugins.analyse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hy.common.Busway;
import org.hy.common.Counter;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.Max; 
import org.hy.common.MethodReflect;
import org.hy.common.Queue;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.TablePartitionRID;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.net.ClientSocket;
import org.hy.common.net.ClientSocketCluster;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.thread.Job;
import org.hy.common.thread.JobDisasterRecoveryReport;
import org.hy.common.thread.JobReport;
import org.hy.common.thread.Jobs;
import org.hy.common.thread.ThreadReport;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJSONObject;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.XSQLDBMetadata;
import org.hy.common.xml.XSQLLog;
import org.hy.common.xml.XSQLTriggerInfo;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.AppInitConfig;
import org.hy.common.xml.plugins.XSQLGroup;
import org.hy.common.xml.plugins.XSQLNode;
import org.hy.common.xml.plugins.analyse.data.AnalyseDBTotal;
import org.hy.common.xml.plugins.analyse.data.AnalyseDSGTotal;
import org.hy.common.xml.plugins.analyse.data.AnalyseJobTotal;
import org.hy.common.xml.plugins.analyse.data.AnalyseLoggerTotal;
import org.hy.common.xml.plugins.analyse.data.LoggerReport;
import org.hy.common.xml.plugins.analyse.data.AnalyseThreadPoolTotal;
import org.hy.common.xml.plugins.analyse.data.ClusterReport;
import org.hy.common.xml.plugins.analyse.data.DataSourceGroupReport;
import org.hy.common.xml.plugins.analyse.data.WindowsApp;
import org.hy.common.xml.plugins.analyse.data.XSQLGroupTree;
import org.hy.common.xml.plugins.analyse.data.XSQLRetTable;





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
 *                                添加：重置数据库访问量的概要统计数据 
 *                                添加：重置数据库组合SQL访问量的概要统计数据
 *              v12.0 2018-07-26  添加：查看创建数据库对象列表
 *              v13.0 2018-09-10  添加：显示XSQLGroup树目录流程图
 *              v14.0 2018-09-27  添加：查看XSQL及XSQL组实现标题排序功能。
 *              v15.0 2018-12-20  添加：可以集群服务列表中，查看操作系统的当前时间
 *                                优化：尝试计算Linux的真实内存使用率
 *              v16.0 2019-02-26  添加：定时灾备多活集群情况
 *              v17.0 2019-03-20  添加：XSQL及XSQL组的统计分析页面添加：读写行数
 *              v18.0 2019-05-29  添加：显示XSQL拥有的应用级触发器的个数
 *              v19.0 2019-07-05  添加：显示云桌面
 *              v20.0 2020-01-15  添加：查看对象信息时，显示成员方法
 *              v20.1 2020-01-21  添加：带参数执行方法
 *              v21.0 2020-04-17  添加：定时任务添加“云主机”IP的显示
 *              v22.0 2020-06-13  添加：日志引擎的监控
 *                                添加：集群日志引擎的监控
 *              v22.1 2020-06-20  添加：日志引擎的监控，添加类名称的过滤功能。建议人：李浩
 *                                      日志引擎的监控，添加“业务用时”的统计。建议人：李浩
 *                                      日志引擎的监控，估算方法的运行状态，是否在运行中。方案：李浩
 *                                      日志引擎的监控，定时刷新监控页面。建议人：李浩
 *              v22.2 2020-06-21  添加：集群监控的定时刷新
 *                                      定时任务的定时刷新
 *                                      XSQL监控的定时刷新
 *                                      XSQL组监控的定时刷新
 *                                      
 */
@Xjava
public class AnalyseBase extends Analyse
{
    
    private static final Logger $Logger = new Logger(AnalyseBase.class);
    
    /** 服务器启动时间 */
    private static Date $ServerStartTime = null;
    
    
    
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
     * 功能1. 显示云桌面
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-07-05
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @return
     */
    public String showWindows(String i_BasePath ,String i_ObjectValuePath)
    {
        XJSON            v_XJson          = new XJSON();
        List<WindowsApp> v_StartMenus     = AnalysesCatalogue.getCatalogue();
        List<WindowsApp> v_Apps           = new ArrayList<WindowsApp>();
        String           v_StartMenusJson = null;
        String           v_AppsJson       = null;
        
        try
        {
            if ( !Help.isNull(v_StartMenus) )
            {
                v_StartMenusJson = v_XJson.toJson(v_StartMenus ,"datas").toJSONString();
                
                for (WindowsApp v_App : v_StartMenus)
                {
                    if ( v_App.isDesktopShow() )
                    {
                        v_Apps.add(v_App);
                    }
                }
                
                if ( !Help.isNull(v_Apps) )
                {
                    v_AppsJson = v_XJson.toJson(v_Apps ,"datas").toJSONString();
                }
            }
            
            
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        if ( Help.isNull(v_StartMenusJson) )
        {
            v_StartMenusJson = "{datas:[]}";
        }
        
        if ( Help.isNull(v_AppsJson) )
        {
            v_AppsJson = "{datas:[]}";
        }
        
        return StringHelp.replaceAll(this.getTemplateShowWindows()
                                    ,new String[]{":Title"   ,":StartMenus"    ,":Apps"    ,":HttpBasePath" ,":HttpValuePath"}
                                    ,new String[]{"分析中心" ,v_StartMenusJson ,v_AppsJson ,i_BasePath      ,i_ObjectValuePath});
    }
    
    
    
    /**
     * 功能1. 显示多屏同显
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-07
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @return
     */
    public String showMultiWindows(String i_BasePath ,String i_ObjectValuePath)
    {
        return StringHelp.replaceAll(this.getTemplateShowMultiWindows()
                                    ,new String[]{":Title"   ,":HttpBasePath" ,":HttpValuePath"}
                                    ,new String[]{"积木大屏" ,i_BasePath      ,i_ObjectValuePath});
    }
    
    
    
    /**
     * 功能1. 显示工作日历
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-07
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @return
     */
    public String showWorkRest(String i_BasePath ,String i_ObjectValuePath)
    {
        return StringHelp.replaceAll(this.getTemplateShowWorkRest()
                                    ,new String[]{":Title"   ,":HttpBasePath" ,":HttpValuePath"}
                                    ,new String[]{"工作日历" ,i_BasePath      ,i_ObjectValuePath});
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
//    public String showCatalogue(String i_BasePath ,String i_ObjectValuePath)
//    {
//        List<Param>   v_Objects = AnalysesCatalogue.getCatalogue();
//        StringBuilder v_Buffer  = new StringBuilder();
//        int           v_Index   = 0;
//        String        v_Content = this.getTemplateShowObjectsContent();
//        
//        v_Buffer.append(StringHelp.replaceAll(v_Content 
//                ,new String[]{":No" 
//                             ,":Name" 
//                             ,":Info"
//                             ,":OperateURL" 
//                             ,":OperateTitle"} 
//                ,new String[]{String.valueOf(++v_Index)
//                             ,"系统启动时间"
//                             ,$ServerStartTime.getFull()
//                             ,""
//                             ,""
//                             })
//                );
//        
//        for (Param v_Item : v_Objects)
//        {
//            String v_URL     = "";
//            String v_Command = "";
//            
//            if ( !Help.isNull(v_Item.getValue()) )
//            {
//                if ( Help.NVL(v_Item.getValue()).trim().toLowerCase().startsWith("javascript:") )
//                {
//                    v_URL = v_Item.getValue().trim();
//                }
//                else
//                {
//                    v_URL = i_ObjectValuePath + v_Item.getValue().trim();
//                }
//                v_Command = "查看详情";
//            }
//            
//            v_Buffer.append(StringHelp.replaceAll(v_Content 
//                                                 ,new String[]{":No" 
//                                                              ,":Name" 
//                                                              ,":Info"
//                                                              ,":OperateURL" 
//                                                              ,":OperateTitle"} 
//                                                 ,new String[]{String.valueOf(++v_Index)
//                                                              ,v_Item.getName()
//                                                              ,Help.NVL(v_Item.getComment())
//                                                              ,v_URL
//                                                              ,v_Command
//                                                              })
//                           );
//        }
//        
//        return StringHelp.replaceAll(this.getTemplateShowObjects()
//                                    ,new String[]{":Title"  ,":Column01Title" ,":Column02Title"  ,":HttpBasePath" ,":HttpValuePath"  ,":Content"}
//                                    ,new String[]{"分析中心" ,"功能"            ,"说明"            ,i_BasePath      ,i_ObjectValuePath ,v_Buffer.toString()});
//    }
    
    
    
    /**
     * 生成XSQLGroup组的树结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-09-14
     * @version     v1.0
     *
     * @param i_XSQLGroup
     * @return
     */
    private XSQLGroupTree makeXSQLGroupTree(XSQLGroup i_XSQLGroup)
    {
        Map<XSQLGroup ,XSQLGroupTree> v_RecursionMap = new HashMap<XSQLGroup ,XSQLGroupTree>();
        
        try
        {
            return makeXSQLGroupTree(i_XSQLGroup ,v_RecursionMap);
        }
        finally
        {
            v_RecursionMap.clear();
            v_RecursionMap = null;
        }
    }
    
    
    
    /**
     * 生成XSQLGroup组的树结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-09-10
     * @version     v1.0
     *
     * @param i_XSQLGroup
     * @param io_RecursionMap  递归引用的信息
     * @return
     */
    private XSQLGroupTree makeXSQLGroupTree(XSQLGroup i_XSQLGroup ,Map<XSQLGroup ,XSQLGroupTree> io_RecursionMap)
    {
        XSQLGroupTree v_Tree = new XSQLGroupTree();
        
        v_Tree.setXid(    Help.NVL(i_XSQLGroup.getXJavaID()));
        v_Tree.setName(   Help.NVL(i_XSQLGroup.getComment() ,Help.NVL(i_XSQLGroup.getXJavaID())));
        v_Tree.setComment(Help.NVL(i_XSQLGroup.getComment()));
        v_Tree.setThreadType(i_XSQLGroup.isThread() ? "组级多线程" : "");
        v_Tree.setChildren(new ArrayList<XSQLGroupTree>());
        
        if ( i_XSQLGroup.getCloudWait() != null )
        {
            v_Tree.setCloudWait(Help.NVL(i_XSQLGroup.getCloudWait().getComment() ,Help.NVL(i_XSQLGroup.getCloudWait().getXJavaID())));
        }
        
        io_RecursionMap.put(i_XSQLGroup ,v_Tree);
        
        if ( Help.isNull(i_XSQLGroup.getXsqlNodes()) )
        {
            return v_Tree;
        }
        
        for (XSQLNode v_XSQLNode : i_XSQLGroup.getXsqlNodes())
        {
            XSQLGroupTree v_TreeNode = new XSQLGroupTree();
            
            v_TreeNode.setXid(      Help.NVL(v_XSQLNode.getXJavaID()));
            v_TreeNode.setName(     Help.NVL(v_XSQLNode.getComment() ,Help.NVL(v_XSQLNode.getXJavaID())));
            v_TreeNode.setComment(  Help.NVL(v_XSQLNode.getComment()));
            v_TreeNode.setCondition(Help.NVL(v_XSQLNode.getCondition()));
            v_TreeNode.setThreadType(v_XSQLNode.isThread() ? "节点级多线程" : "");
            
            if ( v_XSQLNode.getThreadWait() != null )
            {
                if ( v_XSQLNode.getThreadWait() == v_XSQLNode )
                {
                    v_TreeNode.setThreadWait("本循环自身等待");
                }
                else
                {
                    v_TreeNode.setThreadWait(Help.NVL(v_XSQLNode.getThreadWait().getComment() ,Help.NVL(v_XSQLNode.getThreadWait().getXJavaID())));
                }
            }
            else if ( v_XSQLNode.isThread() )
            {
                v_TreeNode.setThreadWait("不等待，或在其它节点处等待");
            }
            
            if ( !Help.isNull(v_XSQLNode.getCloudServersList()) )
            {
                v_TreeNode.setCloudServers("" + v_XSQLNode.getCloudServersList().size());
            }
            if ( v_XSQLNode.getCloudWait() != null )
            {
                if ( v_XSQLNode.getCloudWait() == v_XSQLNode )
                {
                    v_TreeNode.setCloudWait("本循环自身等待");
                }
                else
                {
                    v_TreeNode.setCloudWait(Help.NVL(v_XSQLNode.getCloudWait().getComment() ,Help.NVL(v_XSQLNode.getCloudWait().getXJavaID())));
                }
            }
            else if ( !Help.isNull(v_XSQLNode.getCloudServersList()) )
            {
                v_TreeNode.setCloudWait("不等待，或在其它节点处等待");
            }
            
            XSQLGroup v_Children = v_XSQLNode.getSqlGroup();
            if ( v_Children == null )
            {
                if ( XSQLNode.$Type_ExecuteJava.equals(v_XSQLNode.getType()) )
                {
                    v_TreeNode.setExecuteXID(v_XSQLNode.getXid() + "." + v_XSQLNode.getMethodName());
                }
                else if ( XSQLNode.$Type_CollectionToQuery.equals(v_XSQLNode.getType()) )
                {
                    v_TreeNode.setExecuteXID(Help.NVL(v_XSQLNode.getCollectionID()));
                }
                else if ( XSQLNode.$Type_CollectionToExecuteUpdate.equals(v_XSQLNode.getType()) )
                {
                    v_TreeNode.setExecuteXID(Help.NVL(v_XSQLNode.getCollectionID()));
                    if ( v_XSQLNode.getSql() != null )
                    {
                        v_TreeNode.setExecuteXID(Help.NVL(v_XSQLNode.getSql().getXJavaID()));
                        if ( v_XSQLNode.getSql().getDataSourceGroup() != null )
                        {
                            v_TreeNode.setDbgName(Help.NVL(v_XSQLNode.getSql().getDataSourceGroup().getXJavaID()));
                        }
                    }
                }
                else
                {
                    if ( v_XSQLNode.getSql() != null )
                    {
                        v_TreeNode.setExecuteXID(Help.NVL(v_XSQLNode.getSql().getXJavaID()));
                        if ( v_XSQLNode.getSql().getDataSourceGroup() != null )
                        {
                            v_TreeNode.setDbgName(Help.NVL(v_XSQLNode.getSql().getDataSourceGroup().getXJavaID()));
                        }
                    }
                    v_TreeNode.setReturnID(  Help.NVL(v_XSQLNode.getReturnID() ,Help.NVL(v_XSQLNode.getQueryReturnID())));
                }
                
                v_TreeNode.setNodeType(v_XSQLNode.getType());
                v_TreeNode.setName(Help.NVL(v_TreeNode.getName() ,v_TreeNode.getExecuteXID()));
            }
            // 递归引用的情况
            else if ( io_RecursionMap.containsKey(v_Children) )
            {
                XSQLGroupTree v_Recursion = io_RecursionMap.get(v_Children);
                v_TreeNode.setNodeType("XSQL组：循环递归");
                v_TreeNode.setExecuteXID(v_Recursion.getXid());
                
                if ( Help.isNull(v_TreeNode.getName()) )
                {
                    // 对于直接执行XSQL组的节点，一般都没有注释及XID的，所以取节点执行XSQL组的信息
                    v_TreeNode.setName(Help.NVL(v_Recursion.getName()));
                }
                if ( Help.isNull(v_TreeNode.getComment()) )
                {
                    // 对于直接执行XSQL组的节点，一般都没有注释及XID的，所以取节点执行XSQL组的信息
                    v_TreeNode.setComment(Help.NVL(v_Recursion.getComment()));
                }
            }
            else
            {
                v_TreeNode.setNodeType("XSQL组");
                v_TreeNode.setThreadType(v_Children.isThread() ? "组级多线程" : "");
                v_TreeNode.setExecuteXID(v_Children.getXJavaID());
                
                XSQLGroupTree v_ChildrenTree = makeXSQLGroupTree(v_Children);
                v_TreeNode.setChildren(v_ChildrenTree.getChildren());
                
                if ( Help.isNull(v_TreeNode.getName()) )
                {
                    // 对于直接执行XSQL组的节点，一般都没有注释及XID的，所以取节点执行XSQL组的信息
                    v_TreeNode.setName(Help.NVL(v_ChildrenTree.getName()));
                }
                if ( Help.isNull(v_TreeNode.getComment()) )
                {
                    // 对于直接执行XSQL组的节点，一般都没有注释及XID的，所以取节点执行XSQL组的信息
                    v_TreeNode.setComment(Help.NVL(v_ChildrenTree.getComment()));
                }
            }
            
            if ( Help.isNull(v_TreeNode.getChildren()) )
            {
                v_TreeNode.setChildren(null);
            }
            
            v_Tree.getChildren().add(v_TreeNode);
        }
        
        return v_Tree;
    }
    
    
    
    /**
     * 显示XSQLGroup树目录流程图
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-09-10
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @param i_XID              XJava对象ID
     * @return
     */
    public String showXSQLGroupFlow(String i_BasePath ,String i_ObjectValuePath ,String i_XID)
    {
        $Logger.debug("XSQL组流程图：" + i_XID);
        
        Object v_XObject = XJava.getObject(i_XID);
        if ( v_XObject == null )
        {
            return "Object(" + i_XID + ") is not exists.";
        }
        else if ( !(v_XObject instanceof XSQLGroup) )
        {
            return "Object(" + i_XID + ") is not XSQLGroup Class type.";
        }
        
        XSQLGroup     v_XSQLGroup     = (XSQLGroup)v_XObject;
        XSQLGroupTree v_XSQLGroupTree = this.makeXSQLGroupTree(v_XSQLGroup);
        XJSON         v_XJson         = new XJSON();
        String        v_JsonTree      = null;
        
        try
        {
            v_XJson.setReturnNVL(true);
            v_JsonTree = v_XJson.toJson(v_XSQLGroupTree).toJSONString();
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return exce.getMessage();
        }
        
        return StringHelp.replaceAll(this.getTemplateShowXSQLGroupFlow()
                                    ,new String[]{":Title"           ,":HttpBasePath" ,":HttpValuePath"  ,":XID" ,":JsonTree"}
                                    ,new String[]{"XSQL组的业务流程图" ,i_BasePath      ,i_ObjectValuePath ,i_XID ,v_JsonTree});
    }
    
    
    
    /**
     * 获取数据库组合SQL访问量的概要统计数据（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-15
     * @version     v1.0
     *              v2.0  2020-06-21  添加：定时刷新页面的功能
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseDB
     * @param  i_Cluster         是否为集群
     * @param  i_SortType        排序类型
     * @param  i_IsAll           是否显示所有XSQL组对象（解决：当对象十分庞大时，页面显示缓慢）
     * @param  i_Timer           定时刷新的时长（单位：毫秒）
     * @return
     */
    public String analyseDBGroup(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster ,String i_SortType ,boolean i_IsAll ,String i_Timer)
    {
        $Logger.debug("XSQL组概要统计");
        
        StringBuilder       v_Buffer       = new StringBuilder();
        int                 v_Index        = 0;
        String              v_Content      = this.getTemplateShowXSQLGroupContent();
        long                v_RequestCount = 0;
        long                v_SuccessCount = 0;
        long                v_FailCount    = 0;
        long                v_IORowCount   = 0;
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
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDBGroup_Total" ,true ,"XSQL组分析");
                
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
                            v_Total.getIoRowCount()  .putAll(v_TempTotal.getIoRowCount());
                            v_Total.getMaxExecTime() .putAll(v_TempTotal.getMaxExecTime());
                            v_Total.getTotalTimeLen().putAll(v_TempTotal.getTotalTimeLen());
                        }
                    }
                }
            }
        }
        
        
        Set<String>     v_XSQLIDs    = v_Total.getRequestCount().keySet();
        List<String>    v_XSQLIDList = Help.toList(v_XSQLIDs);
        Counter<String> v_FailCounts = new Counter<String>();
        Max<String>     v_AvgTimes   = new Max<String>();
        for (String v_XSQLID : v_XSQLIDList)
        {
            v_RequestCount = v_Total.getRequestCount().getSumValue(v_XSQLID);
            if ( !i_IsAll && v_RequestCount <= 0 )
            {
                v_Total.getRequestCount()   .remove(v_XSQLID);
                v_Total.getSuccessCount()   .remove(v_XSQLID);
                v_Total.getIoRowCount()     .remove(v_XSQLID);
                v_Total.getMaxExecTime()    .remove(v_XSQLID);
                v_Total.getTotalTimeLen()   .remove(v_XSQLID);
                
                continue;
            }
            
            v_SuccessCount = v_Total.getSuccessCount().getSumValue(v_XSQLID);
            v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue(v_XSQLID);
            
            v_FailCounts.put(v_XSQLID ,v_RequestCount - v_SuccessCount);
            v_AvgTimes  .put(v_XSQLID ,Help.division(v_TotalTimeLen ,v_SuccessCount));
        }
        
        
        if ( "1".equalsIgnoreCase(i_SortType) )
        {
            // 按请求量排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getRequestCount()).keySet();
        }
        else if ( "2".equalsIgnoreCase(i_SortType) )
        {
            // 按成功量排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getSuccessCount()).keySet();
        }
        else if ( "3".equalsIgnoreCase(i_SortType) )
        {
            // 按未成功量排序
            v_XSQLIDs = Help.toReverseByMap(v_FailCounts).keySet();
        }
        else if ( "4".equalsIgnoreCase(i_SortType) )
        {
            // 按操作时间排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getMaxExecTime()).keySet();
        }
        else if ( "5".equalsIgnoreCase(i_SortType) )
        {
            // 按总时长排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getTotalTimeLen()).keySet();
        }
        else if ( "6".equalsIgnoreCase(i_SortType) )
        {
            // 按平均用时(毫秒)排序
            v_XSQLIDs = Help.toReverseByMap(v_AvgTimes).keySet();
        }
        else if ( "7".equalsIgnoreCase(i_SortType) )
        {
            // 按IO读写行数排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getIoRowCount()).keySet();
        }
        else
        {
            v_XSQLIDs = Help.toSort(v_Total.getRequestCount()).keySet();
        }
        
        
        v_NowTime = new Date().getMinutes(-2).getTime();
        for (String v_XSQLID : v_XSQLIDs)
        {
            v_RequestCount = v_Total.getRequestCount().getSumValue(v_XSQLID);
            v_SuccessCount = v_Total.getSuccessCount().getSumValue(v_XSQLID);
            v_FailCount    = v_RequestCount - v_SuccessCount;
            v_IORowCount   = v_Total.getIoRowCount()  .getSumValue(v_XSQLID);
            v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue(v_XSQLID);
            v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
            v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue(v_XSQLID).longValue());
            
            v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                     .replaceAll(":Name"         ,"<a href='analyseObject?XSGFlow=Y&xid=" + v_XSQLID + "' class='Flow'>" + v_XSQLID + "</a>")
                                     .replaceAll(":RequestCount" ,"<span style='color:" + (v_RequestCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_RequestCount + "</span>")
                                     .replaceAll(":SuccessCount" ,"<span style='color:" + (v_SuccessCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_SuccessCount + "</span>")
                                     .replaceAll(":FailCount"    ,"<span style='color:" + (v_FailCount    > 0 ? "red;font-weight:bold"   : "gray") + ";'>" + (v_FailCount > 0 ? "<a href='#'>" + v_FailCount + "</a>" : v_FailCount) + "</span>")
                                     .replaceAll(":IORowCount"   ,"<span style='color:" + (v_IORowCount   > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_IORowCount   + "</span>")
                                     .replaceAll(":ParamURL"     ,"#")
                                     .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : (v_MaxExecTime.getTime() >= v_NowTime ? v_MaxExecTime.getFull() : "<span style='color:gray;'>" + v_MaxExecTime.getFull() + "</span>"))
                                     .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                     .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                           );
                
        }
        
        v_RequestCount = v_Total.getRequestCount().getSumValue();
        v_SuccessCount = v_Total.getSuccessCount().getSumValue();
        v_FailCount    = v_RequestCount - v_SuccessCount;
        v_IORowCount   = v_Total.getIoRowCount()  .getSumValue();
        v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue();
        v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
        v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue().longValue());
        
        v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                 .replaceAll(":Name"         ,"合计")
                                 .replaceAll(":RequestCount" ,"<span style='color:" + (v_RequestCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_RequestCount + "</span>")
                                 .replaceAll(":SuccessCount" ,"<span style='color:" + (v_SuccessCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_SuccessCount + "</span>")
                                 .replaceAll(":FailCount"    ,"<span style='color:" + (v_FailCount    > 0 ? "red;font-weight:bold"   : "gray") + ";'>" + v_FailCount    + "</span>")
                                 .replaceAll(":IORowCount"   ,"<span style='color:" + (v_IORowCount   > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_IORowCount   + "</span>")
                                 .replaceAll(":ParamURL"     ,"#")
                                 .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : v_MaxExecTime.getFull())
                                 .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                 .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                       );
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_IsAll )
        {
            v_Goto += "<a href='analyseDB?type=Group&cluster=" + (i_Cluster?"Y":"N") + "&S=" + i_SortType + "&scope=N' style='color:#AA66CC'>只显示非零</a>";
        }
        else
        {
            v_Goto += "<a href='analyseDB?type=Group&cluster=" + (i_Cluster?"Y":"N") + "&S=" + i_SortType + "&scope=Y' style='color:#AA66CC'>显示全部</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseDB?type=Group&cluster=N&S=" + i_SortType + "&scope=" + (i_IsAll?"Y":"N") + "' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseDB?type=Group&cluster=Y&S=" + i_SortType + "&scope=" + (i_IsAll?"Y":"N") + "' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDBGroup_Reset&cluster=Y&sameTime=Y' style='color:#AA66CC'>集群重置</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDBGroup_Reset' style='color:#AA66CC'>重置统计</a>";
        }
        
        v_FailCounts.clear();
        v_FailCounts = null;
        
        v_AvgTimes.clear();
        v_AvgTimes = null;
        
        v_XSQLIDs.clear();
        v_XSQLIDs = null;
        
        v_XSQLIDList.clear();
        v_XSQLIDList = null;
        
        return StringHelp.replaceAll(this.getTemplateShowXSQLGroup()
                                    ,new String[]{":NameTitle"    ,":GotoTitle" ,":Title"                            ,":HttpBasePath" ,":cluster"             ,":Sort"    ,":IsGroup" ,":scope"          ,":Timer" ,":Content"}
                                    ,new String[]{"组合SQL访问标识" ,v_Goto       ,"数据库组合SQL访问量的概要统计" ,i_BasePath      ,(i_Cluster ? "Y" : "") ,i_SortType ,"Y"        ,(i_IsAll?"Y":"N") ,i_Timer  ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取数据库访问量的概要统计数据（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *              v2.0  2019-05-29  添加：显示XSQL拥有的应用级触发器的个数
     *                                删除：不再显示触发器的请求量、成功量、失败量。因为，触发器将独立成为一行统计数据来显示。
     *              v3.0  2020-06-21  添加：定时刷新页面的功能
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseDB
     * @param  i_Cluster         是否为集群
     * @param  i_SortType        排序类型
     * @param  i_IsAll           是否显示所有XSQL组对象（解决：当对象十分庞大时，页面显示缓慢）
     * @param  i_Timer           定时刷新的时长（单位：毫秒）
     * @return
     */
    public String analyseDB(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster ,String i_SortType ,boolean i_IsAll ,String i_Timer)
    {
        $Logger.debug("XSQL概要统计");
        
        Map<String ,Object> v_XSQLs           = XJava.getObjects(XSQL.class);
        StringBuilder       v_Buffer          = new StringBuilder();
        int                 v_Index           = 0;
        String              v_Content         = this.getTemplateShowXSQLContent();
        String              v_OperateURL      = "";
        long                v_RequestCount    = 0;
        long                v_SuccessCount    = 0;
        long                v_FailCount       = 0;
        long                v_IORowCount      = 0;
        long                v_TriggerCount    = 0;
        /*
        long                v_TriggerReqCount = 0;
        long                v_TriggerSucCount = 0;
        long                v_TriggerFaiCount = 0;
        */
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
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDB_Total" ,true ,"XSQ分析");
                
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
                            v_Total.getIoRowCount()     .putAll(v_TempTotal.getIoRowCount());
                            v_Total.getMaxExecTime()    .putAll(v_TempTotal.getMaxExecTime());
                            v_Total.getTotalTimeLen()   .putAll(v_TempTotal.getTotalTimeLen());
                            v_Total.getTriggerCount()   .putAll(v_TempTotal.getTriggerCount());
                            /*
                            v_Total.getTriggerReqCount().putAll(v_TempTotal.getTriggerReqCount());
                            v_Total.getTriggerSucCount().putAll(v_TempTotal.getTriggerSucCount());
                            */
                        }
                    }
                }
            }
        }
        
        
        Set<String>     v_XSQLIDs    = v_Total.getRequestCount().keySet();
        List<String>    v_XSQLIDList = Help.toList(v_XSQLIDs);
        Counter<String> v_FailCounts = new Counter<String>();
        Max<String>     v_AvgTimes   = new Max<String>();
        for (String v_XSQLID : v_XSQLIDList)
        {
            v_RequestCount = v_Total.getRequestCount().getSumValue(v_XSQLID);
            if ( !i_IsAll && v_RequestCount <= 0 )
            {
                v_Total.getRequestCount()   .remove(v_XSQLID);
                v_Total.getSuccessCount()   .remove(v_XSQLID);
                v_Total.getIoRowCount()     .remove(v_XSQLID);
                v_Total.getMaxExecTime()    .remove(v_XSQLID);
                v_Total.getTotalTimeLen()   .remove(v_XSQLID);
                v_Total.getTriggerCount()   .remove(v_XSQLID);
                /*
                v_Total.getTriggerReqCount().remove(v_XSQLID);
                v_Total.getTriggerSucCount().remove(v_XSQLID);
                */
                
                continue;
            }
            
            v_SuccessCount = v_Total.getSuccessCount().getSumValue(v_XSQLID);
            v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue(v_XSQLID);
            
            v_FailCounts.put(v_XSQLID ,v_RequestCount - v_SuccessCount);
            v_AvgTimes  .put(v_XSQLID ,Help.division(v_TotalTimeLen ,v_SuccessCount));
        }
        
        
        if ( "1".equalsIgnoreCase(i_SortType) )
        {
            // 按请求量排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getRequestCount()).keySet();
        }
        else if ( "2".equalsIgnoreCase(i_SortType) )
        {
            // 按成功量排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getSuccessCount()).keySet();
        }
        else if ( "3".equalsIgnoreCase(i_SortType) )
        {
            // 按未成功量排序
            v_XSQLIDs = Help.toReverseByMap(v_FailCounts).keySet();
        }
        else if ( "4".equalsIgnoreCase(i_SortType) )
        {
            // 按操作时间排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getMaxExecTime()).keySet();
        }
        else if ( "5".equalsIgnoreCase(i_SortType) )
        {
            // 按总时长排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getTotalTimeLen()).keySet();
        }
        else if ( "6".equalsIgnoreCase(i_SortType) )
        {
            // 按平均用时(毫秒)排序
            v_XSQLIDs = Help.toReverseByMap(v_AvgTimes).keySet();
        }
        else if ( "7".equalsIgnoreCase(i_SortType) )
        {
            // 按IO读写行数排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getIoRowCount()).keySet();
        }
        else if ( "8".equalsIgnoreCase(i_SortType) )
        {
            // 按XSQL应用级触发器的个数排序
            v_XSQLIDs = Help.toReverseByMap(v_Total.getTriggerCount()).keySet();
        }
        else
        {
            v_XSQLIDs = Help.toSort(v_Total.getRequestCount()).keySet();
        }
        
        
        v_NowTime = new Date().getMinutes(-2).getTime();
        for (String v_XSQLID : v_XSQLIDs)
        {
            v_RequestCount = v_Total.getRequestCount().getSumValue(v_XSQLID);
            v_SuccessCount = v_Total.getSuccessCount().getSumValue(v_XSQLID);
            v_FailCount    = v_RequestCount - v_SuccessCount;
            v_IORowCount   = v_Total.getIoRowCount()  .getSumValue(v_XSQLID);
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
                v_TriggerCount    = v_Total.getTriggerCount()   .getSumValue(v_XSQLID);
                /*
                v_TriggerReqCount = v_Total.getTriggerReqCount().getSumValue(v_XSQLID);
                v_TriggerSucCount = v_Total.getTriggerSucCount().getSumValue(v_XSQLID);
                */
            }
            else
            {
                v_TriggerCount    = 0;
                /*
                v_TriggerReqCount = 0;
                v_TriggerSucCount = 0;
                */
            }
            
            // v_TriggerFaiCount = v_TriggerReqCount - v_TriggerSucCount;
            
            v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                     .replaceAll(":Name"         ,v_XSQLID)
                                     .replaceAll(":HaveTrigger"  ,"<span style='color:" + (v_TriggerCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_TriggerCount + "</span>")
                                     .replaceAll(":RequestCount" ,"<span style='color:" + (v_RequestCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_RequestCount + "</span>")
                                     .replaceAll(":SuccessCount" ,"<span style='color:" + (v_SuccessCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_SuccessCount + "</span>")
                                     .replaceAll(":FailCount"    ,"<span style='color:" + (v_FailCount    > 0 ? "red;font-weight:bold"   : "gray") + ";'>" + (v_FailCount > 0 ? "<a href='" + v_OperateURL + "'>" + v_FailCount + "</a>" : v_FailCount) + "</span>")
                                     .replaceAll(":IORowCount"   ,"<span style='color:" + (v_IORowCount   > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_IORowCount   + "</span>")
                                     .replaceAll(":ParamURL"     ,v_OperateURL)
                                     .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : (v_MaxExecTime.getTime() >= v_NowTime ? v_MaxExecTime.getFull() : "<span style='color:gray;'>" + v_MaxExecTime.getFull() + "</span>"))
                                     .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                     .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                           );
        }
        
        v_TriggerCount = v_Total.getTriggerCount().getSumValue();
        v_RequestCount = v_Total.getRequestCount().getSumValue();
        v_SuccessCount = v_Total.getSuccessCount().getSumValue();
        v_FailCount    = v_RequestCount - v_SuccessCount;
        v_IORowCount   = v_Total.getIoRowCount()  .getSumValue();
        v_TotalTimeLen = v_Total.getTotalTimeLen().getSumValue();
        v_AvgTimeLen   = Help.round(Help.division(v_TotalTimeLen ,v_SuccessCount) ,2);
        v_MaxExecTime  = new Date(v_Total.getMaxExecTime().getMaxValue().longValue());
        
        v_Buffer.append(v_Content.replaceAll(":No"           ,String.valueOf(++v_Index))
                                 .replaceAll(":Name"         ,"合计")
                                 .replaceAll(":HaveTrigger"  ,"<span style='color:" + (v_TriggerCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_TriggerCount + "</span>")
                                 .replaceAll(":RequestCount" ,"<span style='color:" + (v_RequestCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_RequestCount + "</span>")
                                 .replaceAll(":SuccessCount" ,"<span style='color:" + (v_SuccessCount > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_SuccessCount + "</span>")
                                 .replaceAll(":FailCount"    ,"<span style='color:" + (v_FailCount    > 0 ? "red;font-weight:bold"   : "gray") + ";'>" + v_FailCount    + "</span>")
                                 .replaceAll(":IORowCount"   ,"<span style='color:" + (v_IORowCount   > 0 ? "green;font-weight:bold" : "gray") + ";'>" + v_IORowCount   + "</span>")
                                 .replaceAll(":ParamURL"     ,"#")
                                 .replaceAll(":ExecuteTime"  ,v_MaxExecTime == null || v_MaxExecTime.getTime() <= 0L ? "" : v_MaxExecTime.getFull())
                                 .replaceAll(":SumTime"      ,Date.toTimeLen((long)v_TotalTimeLen))
                                 .replaceAll(":AvgTime"      ,String.valueOf(v_AvgTimeLen))
                       );
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_IsAll )
        {
            v_Goto += "<a href='analyseDB?cluster=" + (i_Cluster?"Y":"N") + "&S=" + i_SortType + "&scope=N' style='color:#AA66CC'>只显示非零</a>";
        }
        else
        {
            v_Goto += "<a href='analyseDB?cluster=" + (i_Cluster?"Y":"N") + "&S=" + i_SortType + "&scope=Y' style='color:#AA66CC'>显示全部</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseDB?cluster=N&S=" + i_SortType + "&scope=" + (i_IsAll?"Y":"N") + "' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseDB?cluster=Y&S=" + i_SortType + "&scope=" + (i_IsAll?"Y":"N") + "' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDB_RestTotal&cluster=Y&sameTime=Y' style='color:#AA66CC'>集群重置</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?xid=AnalyseBase&call=analyseDB_RestTotal' style='color:#AA66CC'>重置统计</a>";
        }
        
        v_FailCounts.clear();
        v_FailCounts = null;
        
        v_AvgTimes.clear();
        v_AvgTimes = null;
        
        v_XSQLIDs.clear();
        v_XSQLIDs = null;

        v_XSQLIDList.clear();
        v_XSQLIDList = null;
        
        v_XSQLs.clear();
        v_XSQLs = null;
        
        return StringHelp.replaceAll(this.getTemplateShowXSQL()
                                    ,new String[]{":NameTitle" ,":GotoTitle" ,":Title"                   ,":HttpBasePath"  ,":cluster"            ,":Sort"    ,":IsGroup" ,":scope"          ,":Timer" ,":Content"}
                                    ,new String[]{"SQL访问标识" ,v_Goto       ,"数据库访问量的概要统计" ,i_BasePath      ,(i_Cluster ? "Y" : "") ,i_SortType ,"N"        ,(i_IsAll?"Y":"N") ,i_Timer ,v_Buffer.toString()});
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
                v_Total.getIoRowCount()  .put(v_Item.getKey() ,v_XSQLGroup.getIoRowCount());
                v_Total.getMaxExecTime() .put(v_Item.getKey() ,v_XSQLGroup.getExecuteTime() == null ? 0L : v_XSQLGroup.getExecuteTime().getTime());
                v_Total.getTotalTimeLen().put(v_Item.getKey() ,v_XSQLGroup.getSuccessTimeLen());
            }
        }
        
        v_Objs.clear();
        v_Objs = null;
        
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
                v_Total.getIoRowCount()  .put(v_Item.getKey() ,v_XSQL.getIoRowCount());
                
                // 触发器的执行统计
                if ( v_XSQL.isTriggers() )
                {
                    v_Total.getTriggerCount()   .put(v_Item.getKey() ,v_XSQL.getTrigger().getXsqls().size());
                    /*
                    v_Total.getTriggerReqCount().put(v_Item.getKey() ,v_XSQL.getTrigger().getRequestCount());
                    v_Total.getTriggerSucCount().put(v_Item.getKey() ,v_XSQL.getTrigger().getSuccessCount());
                    */
                }
                else
                {
                    v_Total.getTriggerCount()   .put(v_Item.getKey() ,0);
                    /*
                    v_Total.getTriggerReqCount().put(v_Item.getKey() ,0);
                    v_Total.getTriggerSucCount().put(v_Item.getKey() ,0);
                    */
                }
                
                v_Total.getMaxExecTime() .put(v_Item.getKey() ,v_XSQL.getExecuteTime() == null ? 0L : v_XSQL.getExecuteTime().getTime());
                v_Total.getTotalTimeLen().put(v_Item.getKey() ,v_XSQL.getSuccessTimeLen());
            }
        }
        
        v_Objs.clear();
        v_Objs = null;
        
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
        $Logger.debug("重置XSQL组的概要统计");
        
        Map<String ,Object> v_Objs = XJava.getObjects(XSQLGroup.class);
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                XSQLGroup v_XSQLGroup = (XSQLGroup)v_Item.getValue();
                
                v_XSQLGroup.reset();
            }
        }
        
        v_Objs.clear();
        v_Objs = null;
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
        $Logger.debug("重置XSQL的概要统计");
        
        Map<String ,Object> v_Objs = XJava.getObjects(XSQL.class);
        
        for (Map.Entry<String, Object> v_Item : v_Objs.entrySet())
        {
            if ( v_Item.getValue() != null )
            {
                XSQL v_XSQL = (XSQL)v_Item.getValue();
                
                v_XSQL.reset();
            }
        }
        
        v_Objs.clear();
        v_Objs = null;
        
        XSQL.$SQLBusway     .clear();
        XSQL.$SQLBuswayError.clear();
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
        $Logger.debug("查看XSQL异常记录SQL：" + i_XSQLXID);
        
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
                    Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDBError_Total" ,new Object[]{i_XSQLXID} ,true ,"XSQL执行日志");
                    
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
                    for (XSQLTriggerInfo v_XSQLTrigger : v_XSQLMaster.getTrigger().getXsqls())
                    {
                        if ( v_XSQLTrigger.getXsql().getObjectID().equals(v_XSQLLog.getOid()) )
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
     * 功能1. 显示XSQL配置并创建的数据库对象
     * 功能2. 创建指定对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-26
     * @version     v1.0
     *              v2.0  2018-08-09  添加："创建对象"快捷链接按钮。
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?XSQLCreateList=Y
     * @return
     */
    public String showXSQLCreateList(String i_BasePath ,String i_ObjectValuePath)
    {
        $Logger.debug("查看XSQL创建DB对象列表");
        
        Map<String ,Object> v_XSQLMap = XJava.getObjects(XSQL.class);
        Map<String ,XSQL>   v_XSQLs   = new HashMap<String ,XSQL>();
        StringBuilder       v_Buffer  = new StringBuilder();
        int                 v_Index   = 0;
        String              v_Content = this.getTemplateShowObjectsContent2URL();
        
        for (Map.Entry<String, Object> v_Item : v_XSQLMap.entrySet())
        {
            if ( v_Item.getValue() == null )
            {
                continue;
            }
            
            XSQL v_XSQL = (XSQL)v_Item.getValue();
            if ( Help.isNull(v_XSQL.getCreateObjectName()) || v_XSQL.getDataSourceGroup() == null )
            {
                continue;
            }
            
            v_XSQLs.put(Help.NVL(v_XSQL.getDataSourceGroup().getXJavaID()) + "." + v_XSQL.getCreateObjectName() ,v_XSQL);
        }
        
        v_XSQLs = Help.toSort(v_XSQLs);
        
        for (Map.Entry<String, XSQL> v_Item : v_XSQLs.entrySet())
        {
            String v_URL01     = "";
            String v_Command01 = "";
            String v_URL02     = "";
            String v_Command02 = "";
            XSQL   v_XSQL      = v_Item.getValue();
            
            if ( !Help.isNull(v_XSQL.getXJavaID()) )
            {
                v_URL01     = "analyseObject?xid=" + v_XSQL.getXJavaID() + "&call=createObject";
                v_Command01 = "创建对象";
                v_URL02     = "analyseObject?xid=" + v_XSQL.getXJavaID();
                v_Command02 = "详情";
            }
            
            v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                 ,new String[]{":No" 
                                                              ,":Name" 
                                                              ,":Info"
                                                              ,":OperateURL01" 
                                                              ,":OperateTitle01"
                                                              ,":OperateURL02" 
                                                              ,":OperateTitle02"} 
                                                 ,new String[]{String.valueOf(++v_Index)
                                                              ,"<font color='gray'>" + v_XSQL.getDataSourceGroup().getXJavaID() + ".</font><b>" + v_XSQL.getCreateObjectName() + "</b>"
                                                              ,Help.NVL(v_XSQL.getComment())
                                                              ,v_URL01
                                                              ,v_Command01
                                                              ,v_URL02
                                                              ,v_Command02
                                                              })
                           );
        }
        
        v_XSQLMap.clear();
        v_XSQLMap = null;
        
        v_XSQLs.clear();
        v_XSQLs = null;
        
        return StringHelp.replaceAll(this.getTemplateShowObjects()
                                    ,new String[]{":Title"     ,":Column01Title" ,":Column02Title" ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"创建对象列表" ,"数据库.对象名称"  ,"说明"            ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 删除并重新创建数据库对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-11
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?XSQLCreate=Y
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analyseDBCreate(String i_BasePath ,String i_ObjectValuePath)
    {
        $Logger.debug("删除并重新创建数据库对象");
        
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

        v_XSQLMap.clear();
        v_XSQLMap = null;
        
        return StringHelp.replaceAll(this.getTemplateShowResult()
                                    ,new String[]{":Title"          ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"重建数据库对象列表" ,i_BasePath      ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 功能1. 查看XSQL与表的关系图
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-06-11
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?DSG=Y
     * @param  i_DSGID           数据库连接池的XID
     * @param  i_Sort            排序类型 
     * @return
     */
    public String showXSQLRefTable(String i_BasePath ,String i_ObjectValuePath ,String i_DSGID ,String i_Sort)
    {
        $Logger.debug("查看XSQL与表的关系图");
        
        List<XSQLRetTable> v_Tables = new ArrayList<XSQLRetTable>();
        List<XSQLRetTable> v_XSQLs  = new ArrayList<XSQLRetTable>();
        XJSON              v_XJSON  = new XJSON();
        String             v_RD     = "{datas:[]}";
        String             v_RT     = "{datas:[]}";
        String             v_RX     = "{datas:[]}";
        String []          v_RepKey = {"(" ,")"};
        String []          v_RepVal = {"（" ,"）"};
        String             v_DSGID  = i_DSGID;
        
        v_XJSON.setReturnNVL(false);
        
        try
        {
            Map<String ,Object> v_DSGMap  = XJava.getObjects(DataSourceGroup.class);
            Map<String ,Object> v_XSQLMap = XJava.getObjects(XSQL.class);
            
            if ( !Help.isNull(v_DSGMap) && !Help.isNull(v_XSQLMap) )
            {
                v_RD = v_XJSON.toJson(Help.toListKeys(v_DSGMap) ,"datas").toJSONString();
                if ( "*".equals(v_DSGID) )
                {
                    v_DSGID = Help.toListKeys(v_DSGMap).get(0);
                }
                
                DataSourceGroup v_DSG            = (DataSourceGroup)XJava.getObject(v_DSGID);
                XSQLDBMetadata  v_XSQLDBMetadata = new XSQLDBMetadata();
                List<String>    v_Objects        = Help.toDistinct(v_XSQLDBMetadata.getObjects(v_DSG));
                
                if ( !Help.isNull(v_Objects) )
                {
                    // 生成XSQL信息列表
                    for (Map.Entry<String ,Object> v_XSQLItem : v_XSQLMap.entrySet())
                    {
                        XSQL v_XSQL = (XSQL)v_XSQLItem.getValue();
                        
                        if ( v_DSG == v_XSQL.getDataSourceGroup() )
                        {
                            XSQLRetTable v_XSQLRef = new XSQLRetTable();
                            v_XSQLRef.setXsql(StringHelp.replaceAll(v_XSQLItem.getKey() ,v_RepKey ,v_RepVal));
                            v_XSQLRef.setType(v_XSQL.getContent().getSQLType());
                            v_XSQLRef.setSqlText(Help.NVL(v_XSQL.getContent().getSqlText()).toUpperCase());
                            v_XSQLs.add(v_XSQLRef);
                        }
                    }
                    
                    // 生成表与XSQL的关系数据
                    for (String v_OName : v_Objects)
                    {
                        String       v_ONameFindKey = " " + v_OName.toUpperCase() + " ";
                        List<String> v_RefXSQL      = new ArrayList<String>();
                        
                        for (XSQLRetTable v_XSQLItem : v_XSQLs)
                        {
                            if ( !Help.isNull(v_XSQLItem.getSqlText()) )
                            {
                                if ( v_XSQLItem.getSqlText().indexOf(v_ONameFindKey) > 0 )
                                {
                                    v_RefXSQL.add(v_XSQLItem.getXsql());
                                    v_XSQLItem.setRefCount(Help.NVL(v_XSQLItem.getRefCount()) + 1);
                                }
                            }
                        }
                        
                        if ( !Help.isNull(v_RefXSQL) )
                        {
                            v_RefXSQL = Help.toSort(v_RefXSQL);
                            
                            XSQLRetTable v_Table = new XSQLRetTable();
                            v_Table.setTableName(v_OName);
                            v_Table.setXsqls(v_RefXSQL);
                            v_Tables.add(v_Table);
                        }
                    }
                    
                    // 删除无引用的XSQL
                    for (int i=v_XSQLs.size() - 1; i>=0; i--)
                    {
                        if ( v_XSQLs.get(i).getRefCount() == null || v_XSQLs.get(i).getRefCount().intValue() <= 0 )
                        {
                            v_XSQLs.remove(i);
                        }
                    }
                    
                    
                    if ( "2".equals(i_Sort) )
                    {
                        Help.toSort(v_Tables ,"xsqlCount DESC" ,"orderTableName");
                        Help.toSort(v_XSQLs  ,"refCount DESC"   ,"xsql");
                    }
                    else
                    {
                        Help.toSort(v_Tables ,"tableName");
                        Help.toSort(v_XSQLs  ,"xsql");
                    }
                    
                    if ( !Help.isNull(v_Tables) )
                    {
                        v_RT = v_XJSON.toJson(v_Tables ,"datas").toJSONString();
                    }
                    if ( !Help.isNull(v_XSQLs) )
                    {
                        v_RX = v_XJSON.toJson(v_XSQLs ,"datas").toJSONString();
                    }
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return StringHelp.replaceAll(this.getTemplateShowXSQLRefTable()
                                    ,new String[]{":Title"         ,":DSGID" ,":DSGs" ,":Tables" ,":XSQLs" ,":OrderType" ,":HttpBasePath"}
                                    ,new String[]{"XSQL与表关系图" ,v_DSGID ,v_RD    ,v_RT      ,v_RX      ,i_Sort       ,i_BasePath});
    }
    
    
    
    /**
     * 功能1. 查看表的关系图
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-06-14
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?DSG=Y
     * @param  i_DSGID           数据库连接池的XID
     * @param  i_Sort            排序类型 
     * @return
     */
    public String showXSQLTablesRef(String i_BasePath ,String i_ObjectValuePath ,String i_DSGID ,String i_Sort)
    {
        $Logger.debug("查看表的关系图");
        
        List<XSQLRetTable>                v_XSQLs      = new ArrayList<XSQLRetTable>();
        TablePartitionRID<String ,String> v_XSQLTables = new TablePartitionRID<String ,String>();
        TablePartitionRID<String ,String> v_Tables     = new TablePartitionRID<String ,String>();
        List<XSQLRetTable>                v_RetTables  = new ArrayList<XSQLRetTable>();
        XJSON                             v_XJSON      = new XJSON();
        String                            v_RD         = "{datas:[]}";
        String                            v_RT         = "{datas:[]}";
        String []                         v_RepKey     = {"(" ,")"};
        String []                         v_RepVal     = {"（" ,"）"};
        String                            v_DSGID      = i_DSGID;
        
        v_XJSON.setReturnNVL(false);
        
        try
        {
            Map<String ,Object> v_DSGMap  = XJava.getObjects(DataSourceGroup.class);
            Map<String ,Object> v_XSQLMap = XJava.getObjects(XSQL.class);
            
            if ( !Help.isNull(v_DSGMap) && !Help.isNull(v_XSQLMap) )
            {
                v_RD = v_XJSON.toJson(Help.toListKeys(v_DSGMap) ,"datas").toJSONString();
                if ( "*".equals(v_DSGID) )
                {
                    v_DSGID = Help.toListKeys(v_DSGMap).get(0);
                }
                
                DataSourceGroup v_DSG            = (DataSourceGroup)XJava.getObject(v_DSGID);
                XSQLDBMetadata  v_XSQLDBMetadata = new XSQLDBMetadata();
                List<String>    v_Objects        = Help.toDistinct(v_XSQLDBMetadata.getObjects(v_DSG));
                
                if ( !Help.isNull(v_Objects) )
                {
                    // 生成XSQL信息列表
                    for (Map.Entry<String ,Object> v_XSQLItem : v_XSQLMap.entrySet())
                    {
                        XSQL v_XSQL = (XSQL)v_XSQLItem.getValue();
                        
                        if ( v_DSG == v_XSQL.getDataSourceGroup() )
                        {
                            XSQLRetTable v_XSQLRef = new XSQLRetTable();
                            v_XSQLRef.setXsql(StringHelp.replaceAll(v_XSQLItem.getKey() ,v_RepKey ,v_RepVal));
                            v_XSQLRef.setType(v_XSQL.getContent().getSQLType());
                            v_XSQLRef.setSqlText(Help.NVL(v_XSQL.getContent().getSqlText()).toUpperCase());
                            v_XSQLs.add(v_XSQLRef);
                        }
                    }
                    
                    // 生成表与XSQL的关系数据
                    for (String v_OName : v_Objects)
                    {
                        String v_ONameFindKey = " " + v_OName.toUpperCase() + " ";
                        
                        for (XSQLRetTable v_XSQLItem : v_XSQLs)
                        {
                            if ( !Help.isNull(v_XSQLItem.getSqlText()) )
                            {
                                if ( v_XSQLItem.getSqlText().indexOf(v_ONameFindKey) > 0 )
                                {
                                    v_XSQLTables.putRow(v_XSQLItem.getXsql() ,v_OName ,v_OName);
                                }
                            }
                        }
                    }
                    
                    // 生成表与表的关系数据
                    if ( !Help.isNull(v_XSQLTables) )
                    {
                        for (Map<String ,String> v_Item : v_XSQLTables.values())
                        {
                            for (String v_OName1 : v_Item.values())
                            {
                                for (String v_OName2 : v_Item.values())
                                {
                                    if ( !v_OName1.equals(v_OName2) )
                                    {
                                        v_Tables.putRow(v_OName1 ,v_OName2 ,v_OName2);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 将表与表的关系数据转换格式
                    if ( !Help.isNull(v_Tables) )
                    {
                        for (String v_OName : v_Tables.keySet())
                        {
                            List<String> v_Refs = new ArrayList<String>();
                            for (String v_RefOName : v_Tables.get(v_OName).keySet())
                            {
                                v_Refs.add(v_RefOName);
                            }
                            
                            if ( !Help.isNull(v_Refs) )
                            {
                                XSQLRetTable v_Table = new XSQLRetTable();
                                
                                v_Table.setTableName(v_OName);
                                v_Table.setRefs(Help.toSort(v_Refs));
                                
                                v_RetTables.add(v_Table);
                            }
                        }
                    }
                    
                    if ( !Help.isNull(v_RetTables) )
                    {
                        if ( "2".equals(i_Sort) )
                        {
                            Help.toSort(v_RetTables ,"refsCount DESC" ,"tableName");
                        }
                        else
                        {
                            Help.toSort(v_RetTables ,"tableName");
                        }
                        
                        v_RT = v_XJSON.toJson(v_RetTables ,"datas").toJSONString();
                    }
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return StringHelp.replaceAll(this.getTemplateShowXSQLTablesRef()
                                    ,new String[]{":Title"      ,":DSGID" ,":DSGs" ,":Tables" ,":OrderType" ,":HttpBasePath"}
                                    ,new String[]{"表的关系图" ,v_DSGID ,v_RD    ,v_RT       ,i_Sort       ,i_BasePath});
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
     *              v4.0  2020-01-15  添加：对象成员方法的Json输出
     *                                添加：带参数执行方法
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @param  i_XJavaObjectID   对象标识ID 
     * @param  i_CallMethod      对象方法的全名称（可为空）
     * @param  i_CallParams      对象方法的执行参数（可为空）
     * @param  i_Cluster         是否为集群
     * @param  i_SameTime        是否为同时执行（并发操作）
     * @return
     */
    public String analyseObject(String i_BasePath ,String i_ObjectValuePath ,String i_XJavaObjectID ,String i_CallMethod ,String i_CallParams ,boolean i_Cluster ,boolean i_SameTime)
    {
        $Logger.debug("集群查看或执行对象：" + i_XJavaObjectID + "." + i_CallMethod + ":" + i_CallParams + " is cluster " + (i_Cluster ? "Yes" : "No"));
        
        if ( Help.isNull(i_XJavaObjectID) )
        {
            return "{}";
        }
        
        try
        {
            Object v_Object = XJava.getObject(i_XJavaObjectID);
            if ( v_Object == null )
            {
                return "{}";
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
            v_XJSON.setJsonMethod(true);
            
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
                    v_OperateURL1 = i_ObjectValuePath + "?xid=" + v_Job.getXJavaID() + "&call=execute";
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
                List<Object> v_CallParams = new ArrayList<Object>();
                XJSON        v_Json       = new XJSON();
                
                if ( !Help.isNull(i_CallParams) )
                {
                    String [] v_CallParamArr = i_CallParams.split("%%%");
                    
                    for (String v_Param : v_CallParamArr)
                    {
                        String [] v_ParamTV    = v_Param.split("@@@");
                        Class<?>  v_ParamType  = Help.forName(v_ParamTV[0]);
                        String    v_ParamValue = v_ParamTV.length >= 2 ? v_ParamTV[1] : "";
                        Object    v_ParamObj   = null;
                        
                        if ( Help.isNull(v_ParamValue) )
                        {
                            v_ParamObj = Help.toObject(v_ParamType);
                        }
                        else if ( "NULL".equalsIgnoreCase(v_ParamValue) )
                        {
                            v_ParamObj = null;
                        }
                        else if ( !StringHelp.isStartsWith(v_ParamTV[0] ,"java.lang." ,"java.util.Date" ,"org.hy.common.Date") )
                        {
                            v_ParamObj = v_Json.toJava(v_ParamValue ,v_ParamType);
                        }
                        else
                        {
                            v_ParamObj = Help.toObject(v_ParamType ,v_ParamValue);
                        }
                        
                        v_CallParams.add(v_ParamObj);
                    }
                }
                
                v_Json.setReturnNVL(false);
                v_Json.setAccuracy(true);
                if ( Help.isNull(v_CallParams) )
                {
                    $Logger.info(Date.getNowTime().getFullMilli() + " Execute method is " + i_XJavaObjectID + "." + i_CallMethod + "().");
                }
                else
                {
                    $Logger.info(Date.getNowTime().getFullMilli() + " Execute method is " + i_XJavaObjectID + "." + i_CallMethod + "(" + v_Json.toJson(v_CallParams ,"params").toJSONString() + ")");
                }
                
                Return<String> v_RetInfo = this.analyseObject_Execute(i_XJavaObjectID ,i_CallMethod ,v_CallParams.toArray() ,i_Cluster ,i_SameTime);
                v_Content = v_RetInfo.paramStr;
                
                if ( !i_Cluster )
                {
                    List<Method> v_Methods = MethodReflect.getMethodsIgnoreCase(v_Object.getClass() ,i_CallMethod ,v_CallParams.size());
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
            exce.printStackTrace();
            return "{\"error\":\"" + StringHelp.replaceAll(exce.toString() ,"\"" ,"'") + "\"}";
        }
    }
    
    
    
    /**
     * 执行对象方法（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-17
     * @version     v1.0
     *              v2.0  2020-01-16  添加：支持带参数的方法执行
     *
     * @param  i_XJavaObjectID   对象标识ID 
     * @param  i_CallMethod      对象方法的全名称（可为空）
     * @param  i_CallParams      对象方法的参数对象（可为空）
     * @param  i_Cluster         是否为集群
     * @param  i_SameTime        是否为同时执行（并发操作）
     * @return
     */
    @SuppressWarnings("unchecked")
    public Return<String> analyseObject_Execute(String i_XJavaObjectID ,String i_CallMethod ,Object [] i_CallParams ,boolean i_Cluster ,boolean i_SameTime)
    {
        $Logger.debug("集群查看或执行对象：" + i_XJavaObjectID + "." + i_CallMethod + ":" + i_CallParams + " is cluster " + (i_Cluster ? "Yes" : "No"));
        
        Return<String> v_RetInfo = new Return<String>();
        Object         v_Object  = XJava.getObject(i_XJavaObjectID);
        
        if ( v_Object == null )
        {
            return v_RetInfo.paramStr("XID is not exists.");
        }
        
        int          v_ParamCount = Help.isNull(i_CallParams) ? 0 : i_CallParams.length;
        List<Method> v_Methods    = MethodReflect.getMethodsIgnoreCase(v_Object.getClass() ,i_CallMethod ,v_ParamCount);
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
                
                Object v_CallRet = null;
                if ( Help.isNull(i_CallParams) )
                {
                    v_CallRet = v_Methods.get(0).invoke(v_Object);
                }
                else
                {
                    v_CallRet = v_Methods.get(0).invoke(v_Object ,i_CallParams);
                }
                
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
                exce.printStackTrace();
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
                v_ClusterResponses = ClientSocketCluster.sendCommands(Cluster.getClusters() ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseObject_Execute" ,new Object[]{i_XJavaObjectID ,i_CallMethod ,i_CallParams ,false ,false} ,true ,"执行XJava对象");
            }
            else
            {
                v_ClusterResponses = ClientSocketCluster.sendCommands(Cluster.getClusters()                              ,"AnalyseBase" ,"analyseObject_Execute" ,new Object[]{i_XJavaObjectID ,i_CallMethod ,i_CallParams ,false ,false});
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
     * 功能1. 显示带参数执行方法的配置页面
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-01-21
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?XSQLCreateList=Y
     * @param  i_XJavaObjectID   XJava对象的XID值（可为空）
     * @param  i_CallMethod      XJava对象的方法名称（可为空）
     * @param  i_CallParams      对象方法的执行参数（可为空）
     * @return
     */
    public String showExecuteMethod(String i_BasePath ,String i_ObjectValuePath ,String i_XJavaObjectID ,String i_CallMethod ,String i_CallParams)
    {
        $Logger.debug("显示带参数执行方法的配置页面：" + i_XJavaObjectID + "." + i_CallMethod + ":" + i_CallParams);
        
        String []   v_Param1 = new String[] {"java.lang.Void" ,""};
        String []   v_Param2 = new String[] {"java.lang.Void" ,""};
        String []   v_Param3 = new String[] {"java.lang.Void" ,""};
        String []   v_Param4 = new String[] {"java.lang.Void" ,""};
        String []   v_Param5 = new String[] {"java.lang.Void" ,""};
        String [][] v_Params = new String[][] {v_Param1 ,v_Param2 ,v_Param3 ,v_Param4 ,v_Param5};
        String      v_ShowNo = "5";
        
        if ( !Help.isNull(i_CallParams) )
        {
            String [] v_CallParamArr = i_CallParams.split("%%%");
            
            for (int i=0; i<v_CallParamArr.length; i++)
            {
                String [] v_ParamTV    = v_CallParamArr[i].split("@@@");
                String    v_ParamType  = v_ParamTV.length >= 1 ? v_ParamTV[0] : "";
                String    v_ParamValue = v_ParamTV.length >= 2 ? v_ParamTV[1] : "";
                
                v_Params[i][0] = v_ParamType;
                v_Params[i][1] = v_ParamValue;
            }
        }
        
        for (int i=0; i<v_Params.length; i++)
        {
            if ( "java.lang.Void".equalsIgnoreCase(v_Params[i][0]) )
            {
                v_ShowNo = (i + 1) + "";
                break;
            }
        }
        
        return StringHelp.replaceAll(this.getTemplateShowExecuteMethod()
                                    ,new String[]{":HttpBasePath" 
                                                 ,":xid"                    
                                                 ,":call"                 
                                                 ,":PType1"
                                                 ,":PType2"
                                                 ,":PType3"
                                                 ,":PType4"
                                                 ,":PType5"
                                                 ,":PValue1"
                                                 ,":PValue2"
                                                 ,":PValue3"
                                                 ,":PValue4"
                                                 ,":PValue5"
                                                 ,":ShowDefaultParamNo"
                                                 }
                                    ,new String[]{i_BasePath      
                                                 ,Help.NVL(i_XJavaObjectID) 
                                                 ,Help.NVL(i_CallMethod)
                                                 ,v_Param1[0]
                                                 ,v_Param2[0]
                                                 ,v_Param3[0]
                                                 ,v_Param4[0]
                                                 ,v_Param5[0]
                                                 ,v_Param1[1]
                                                 ,v_Param2[1]
                                                 ,v_Param3[1]
                                                 ,v_Param4[1]
                                                 ,v_Param5[1]
                                                 ,v_ShowNo
                                                 });
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
        $Logger.debug("查看对象列表：" + i_XIDPrefix );
        
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
        $Logger.debug("查看或热加载XJava配置列表：" + i_XFile + " is cluster " +  (i_Cluster ? "Yes" : "No"));
        
        Map<String ,Object> v_XFileNames = (Map<String ,Object>)XJava.getObject(AppInitConfig.$XFileNames_XID);
        Map<String ,Date>   v_XFileTime  = (Map<String ,Date>)  XJava.getObject(AppInitConfig.$XFileNames_XID_Time);
        StringBuilder       v_Buffer     = new StringBuilder();
        int                 v_Index      = 0;
        String              v_Content    = this.getTemplateShowXFilesContent();
        
        if ( Help.isNull(i_XFile) )
        {
            for (String v_XFile : v_XFileNames.keySet())
            {
                if ( !Help.isNull(v_XFile) )
                {
                    v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                         ,new String[]{":No" 
                                                                      ,":Name" 
                                                                      ,":OperTime"
                                                                      ,":OperateURL1" 
                                                                      ,":OperateTitle1"
                                                                      ,":OperateURL2" 
                                                                      ,":OperateTitle2"} 
                                                         ,new String[]{String.valueOf(++v_Index)
                                                                      ,v_XFile
                                                                      ,v_XFileTime.get(v_XFile).getFull()
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
        $Logger.debug("热加载XJava配置：" + i_XFile + " is cluster " +  (i_Cluster ? "Yes" : "No"));
        
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
     *              v2.0  2020-06-21  添加：定时刷新页面的功能
     *
     * @param  i_BasePath       服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ReLoadPath     重新加载的URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @param  i_IsShowSysTime  是否显示服务的当前系统时间
     * @param  i_Timer          定时刷新的时长（单位：毫秒）
     * @return
     */
    public String analyseCluster(String i_BasePath ,String i_ReLoadPath ,boolean i_IsShowSysTime ,String i_Timer)
    {
        $Logger.debug("查看集群服务列表");
        
        List<ClientSocket>   v_Servers      = Cluster.getClusters();
        StringBuilder        v_Buffer       = new StringBuilder();
        int                  v_Index        = 0;
        String               v_Content      = this.getTemplateShowClusterContent();
        List<ClusterReport>  v_Clusters     = new ArrayList<ClusterReport>();
        ClusterReport        v_Total        = new ClusterReport();
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseCluster_Info" ,true ,"监测服务");
            
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
            v_Clusters.get(0).setServerStatus("正常");
        }
        
        Help.toSort(v_Clusters ,"startTime Desc" ,"hostName");
        
        for (ClusterReport v_CReport : v_Clusters)
        {
            Map<String ,String> v_RKey   = new HashMap<String ,String>();
            String              v_OSCPU  = "";
            String              v_OSMem  = "";
            String              v_OSDisk = "";
            String              v_TM     = StringHelp.getComputeUnit(v_CReport.getTotalMemory());
            
            if ( v_CReport.getOsCPURate() >= 90 )
            {
                v_OSCPU = "<font color='red'>" + v_CReport.getOsCPURate() + "</font>";
            }
            else
            {
                v_OSCPU = "" + v_CReport.getOsCPURate();
            }
            
            double v_MemoryRate = 0;
            if ( v_CReport.getLinuxMemoryRate() >= 0 )
            {
                // 当能获取到Linux真实内存使用率时，采用此值
                v_MemoryRate = v_CReport.getLinuxMemoryRate();
            }
            else
            {
                v_MemoryRate = v_CReport.getOsMemoryRate();
            }
            if ( v_MemoryRate >= 90 )
            {
                v_OSMem = "<font color='red'>" + v_MemoryRate + "</font>";
            }
            else
            {
                v_OSMem = "" + v_MemoryRate;
            }
            
            if ( 0.05 >= Help.division(v_CReport.getMaxMemory() - v_CReport.getTotalMemory() ,v_CReport.getMaxMemory()) )
            {
                // 当余量小于5%时，用红提示
                v_TM = "<font color='red'>" + v_TM + "</font>";
            }
            
            if ( v_CReport.getLinuxDiskMaxRate() >= 0 )
            {
                if ( v_CReport.getLinuxDiskMaxRate() >= 90 )
                {
                    v_OSDisk = "<font color='red'>" + v_CReport.getLinuxDiskMaxRate() + "</font>";
                }
                else
                {
                    v_OSDisk = "" + v_CReport.getLinuxDiskMaxRate();
                }
            }
            else
            {
                v_OSDisk = "-";
            }
            
            v_RKey.put(":No"            ,String.valueOf(++v_Index));
            v_RKey.put(":ServerName"    ,v_CReport.getHostName());
            v_RKey.put(":OsCPURate"     ,v_OSCPU);
            v_RKey.put(":OsMemoryRate"  ,v_OSMem);
            v_RKey.put(":OsDiskMaxRate" ,v_OSDisk);
            v_RKey.put(":MaxMemory"     ,StringHelp.getComputeUnit(v_CReport.getMaxMemory()));
            v_RKey.put(":TotalMemory"   ,v_TM);
            v_RKey.put(":FreeMemory"    ,StringHelp.getComputeUnit(v_CReport.getFreeMemory()));
            v_RKey.put(":ThreadCount"   ,v_CReport.getThreadCount() + "");
            v_RKey.put(":QueueCount"    ,v_CReport.getQueueCount()  + "");
            v_RKey.put(":ServerStatus"  ,v_CReport.getServerStatus());
            v_RKey.put(":JavaVersion"   ,v_CReport.getJavaVersion());
            if ( "正常".equals(v_CReport.getServerStatus()) )
            {
                if ( i_IsShowSysTime )
                {
                    if ( Help.isNull(v_CReport.getSystemTime()) )
                    {
                        v_RKey.put(":StartTime" ,"-");
                    }
                    else
                    {
                        v_RKey.put(":StartTime" ,(new Date(v_CReport.getSystemTime())).getFull());
                    }
                }
                else
                {
                    v_RKey.put(":StartTime" ,(new Date(v_CReport.getStartTime())).getYMDHM());
                }
            }
            else
            {
                v_RKey.put(":StartTime" ,"-");
            }
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
            
            v_Total.setMaxMemory(  v_Total.getMaxMemory()   + v_CReport.getMaxMemory());
            v_Total.setTotalMemory(v_Total.getTotalMemory() + v_CReport.getTotalMemory());
            v_Total.setFreeMemory( v_Total.getFreeMemory()  + v_CReport.getFreeMemory());
            v_Total.setThreadCount(v_Total.getThreadCount() + v_CReport.getThreadCount());
            v_Total.setQueueCount( v_Total.getQueueCount()  + v_CReport.getQueueCount());
            v_Total.setServerStatus("正常".equals(v_CReport.getServerStatus()) ? v_Total.getServerStatus() : v_CReport.getServerStatus());
        }
        
        Map<String ,String> v_RKey = new HashMap<String ,String>();
        
        v_RKey.put(":No"            ,String.valueOf(++v_Index));
        v_RKey.put(":ServerName"    ,"合计");
        v_RKey.put(":OsCPURate"     ,"-");
        v_RKey.put(":OsMemoryRate"  ,"-");
        v_RKey.put(":OsDiskMaxRate" ,"-");
        v_RKey.put(":MaxMemory"     ,StringHelp.getComputeUnit(v_Total.getMaxMemory()));
        v_RKey.put(":TotalMemory"   ,StringHelp.getComputeUnit(v_Total.getTotalMemory()));
        v_RKey.put(":FreeMemory"    ,StringHelp.getComputeUnit(v_Total.getFreeMemory()));
        v_RKey.put(":ThreadCount"   ,v_Total.getThreadCount() + "");
        v_RKey.put(":QueueCount"    ,v_Total.getQueueCount()  + "");
        v_RKey.put(":StartTime"     ,"-");
        v_RKey.put(":ServerStatus"  ,Help.NVL(v_Total.getServerStatus() ,"正常"));
        v_RKey.put(":JavaVersion"   ,"-");
        
        v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        
        v_RKey.clear();
        v_RKey = null;
        
        v_Clusters.clear();
        v_Clusters = null;
        
        String v_StartTimeTitle = null;
        if ( i_IsShowSysTime )
        {
            v_StartTimeTitle = "<a href='analyseObject?cluster=Y'           style='color:#AA66CC'>系统时间</a>";
        }
        else
        {
            v_StartTimeTitle = "<a href='analyseObject?cluster=Y&SysTime=Y' style='color:#AA66CC'>启动时间</a>";
        }
        
        return StringHelp.replaceAll(this.getTemplateShowCluster()
                                    ,new String[]{":Title"     ,":Column01Title" ,":StartTimeTitle" ,":HttpBasePath" ,":Timer" ,":Content"}
                                    ,new String[]{"集群服务列表" ,"集群服务"        ,v_StartTimeTitle  ,i_BasePath  ,i_Timer  ,v_Buffer.toString()});
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
        $Logger.debug("查看线程池运行情况：" + " is cluster " +  (i_Cluster ? "Yes" : "No"));
        
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
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseThreadPool_Total" ,true ,"线程池");
                
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
        
        v_Total.getReports().clear();
        v_Total.setReports(null);
        
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
     *              v2.0  2020-06-21  添加：定时刷新页面的功能
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?Job=Y
     * @param  i_Cluster         是否为集群
     * @param  i_Timer           定时刷新的时长（单位：毫秒）
     * @return
     */
    public String analyseJob(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster ,String i_Timer)
    {
        $Logger.debug("查看线程池运行情况：" + " is cluster " +  (i_Cluster ? "Yes" : "No"));
        
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
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseJob_Total" ,true ,"定时任务");
                
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
        
        Help.toSort(v_Total.getReports() ,"nextTime" ,"lastTime" ,"intervalType" ,"intervalLen NUMASC" ,"jobID");
        
        for (JobReport v_JReport : v_Total.getReports())
        {
            Map<String ,String> v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"           ,String.valueOf(++v_Index));
            v_RKey.put(":JobID"        ,v_JReport.getJobID());
            v_RKey.put(":IntervalType" ,v_JReport.getIntervalType());
            v_RKey.put(":IntervalLen"  ,v_JReport.getIntervalLen());
            v_RKey.put(":RunCount"     ,v_JReport.getRunCount() + "");
            v_RKey.put(":LastTime"     ,v_JReport.getLastTime());
            v_RKey.put(":NextTime"     ,v_JReport.getNextTime());
            v_RKey.put(":JobDesc"      ,v_JReport.getJobDesc());
            v_RKey.put(":CloudServer"  ,Help.NVL(v_JReport.getCloudServer() ,"本机"));
            
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
        Jobs   v_Jobs      = (Jobs)XJava.getObject(Jobs.class);
        if ( v_Jobs != null )
        {
            if ( v_Jobs.isDisasterRecovery() )
            {
                v_GotoTitle += StringHelp.lpad("" ,4 ,"&nbsp;") + "<font color='green'>灾备中的" + (v_Jobs.isMaster() ? "Master" : "Slave") + "</font>";
            }
            else
            {
                v_GotoTitle += StringHelp.lpad("" ,4 ,"&nbsp;") + "<font color='green'>灾备机制未开启</font>";
            }
        }
        
        v_Total.getReports().clear();
        v_Total.setReports(null);
        
        return StringHelp.replaceAll(this.getTemplateShowJob()
                                    ,new String[]{":GotoTitle" ,":Title"         ,":HttpBasePath" ,":Timer" ,":Content"}
                                    ,new String[]{v_GotoTitle  ,"定时任务运行情况" ,i_BasePath   ,i_Timer  ,v_Buffer.toString()});
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
     * 查看定时任务的灾备多活集群服务列表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-02-26
     * @version     v1.0
     *
     * @param  i_BasePath       服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ReLoadPath     重新加载的URL。如：http://127.0.0.1:80/hy/../analyseObject
     * @return
     */
    public String analyseJobDisasterRecoverys(String i_BasePath ,String i_ReLoadPath)
    {
        $Logger.debug("查看定时任务的灾备多活集群");
        
        StringBuilder v_Buffer  = new StringBuilder();
        int           v_Index   = 0;
        String        v_Content = this.getTemplateShowJobDisasterRecoverysContent();
        Jobs          v_Jobs    = (Jobs)XJava.getObject(Jobs.class);
        
        if ( v_Jobs != null )
        {
            if ( Help.isNull(v_Jobs.getDisasterRecoverys()) )
            {
                v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                    ,new String[]{":No" 
                                                                 ,":JobServer" 
                                                                 ,":StartTime"
                                                                 ,":GetMasterTime"
                                                                 ,":Master" 
                                                                 ,":Slave"} 
                                                    ,new String[]{String.valueOf(++v_Index)
                                                                 ,"<font color='green'>正常</font>"
                                                                + StringHelp.lpad("" ,4 ,"&nbsp;") 
                                                                + "127.0.0.1"
                                                                 ,Help.isNull(v_Jobs.getStartTime())  ? "未启动" : v_Jobs.getStartTime() .getFull()
                                                                 ,Help.isNull(v_Jobs.getMasterTime()) ? "-"      : v_Jobs.getMasterTime().getFull()
                                                                 ,"<font color='green'><b>Master</b></font>"
                                                                 ,"-"
                                                                 })
                                );
            }
            else
            {
                List<JobDisasterRecoveryReport> v_Reports = v_Jobs.disasterRecoveryChecks();
                for (JobDisasterRecoveryReport v_Report : v_Reports)
                {
                    v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                         ,new String[]{":No" 
                                                                      ,":JobServer" 
                                                                      ,":StartTime"
                                                                      ,":GetMasterTime"
                                                                      ,":Master" 
                                                                      ,":Slave"} 
                                                         ,new String[]{String.valueOf(++v_Index)
                                                                      ,(v_Report.isOK() ? "<font color='green'>正常</font>" : "<font color='red'>异常</font>") 
                                                                     + StringHelp.lpad("" ,4 ,"&nbsp;") 
                                                                     + v_Report.getHostName() + ":" + v_Report.getPort()
                                                                      ,Help.isNull(v_Report.getStartTime())  ? "未启动" : v_Report.getStartTime() .getFull()
                                                                      ,Help.isNull(v_Report.getMasterTime()) ? "-"      : v_Report.getMasterTime().getFull()
                                                                      ,v_Report.isMaster() ? "<font color='green'><b>Master</b></font>" : "-"
                                                                      ,v_Report.isMaster() ? "-"                                        : "Slave"
                                                                      })
                                   );
                }
            }
        }
        else
        {
            v_Buffer.append(StringHelp.replaceAll(v_Content 
                                                ,new String[]{":No" 
                                                             ,":JobServer" 
                                                             ,":StartTime"
                                                             ,":GetMasterTime"
                                                             ,":Master" 
                                                             ,":Slave"} 
                                                ,new String[]{String.valueOf(++v_Index)
                                                             ,"-"
                                                             ,"-"
                                                             ,"-"
                                                             ,"-"
                                                             ,"-"
                                                             })
                        );
        }
        
        return StringHelp.replaceAll(this.getTemplateShowJobDisasterRecoverys()
                                    ,new String[]{":Title"                       ,":HttpBasePath" ,":Content"}
                                    ,new String[]{"定时任务的灾备多活集群服务列表" ,i_BasePath      ,v_Buffer.toString()});
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
        $Logger.debug("数据库连接池：" + " is cluster " +  (i_Cluster ? "Yes" : "No"));
        
        StringBuilder   v_Buffer  = new StringBuilder();
        int             v_Index   = 0;
        String          v_Content = this.getTemplateShowDSGContent();
        AnalyseDSGTotal v_Total   = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseDataSourceGroup_Total();
            
            for (DataSourceGroupReport v_Report : v_Total.getReports().values())
            {
                if ( "异常".equals(v_Report.getDsgStatus()) )
                {
                    v_Report.setDsgStatus("<font color='red'>异常</font>");
                }
            }
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseDSGTotal("合计");
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseDataSourceGroup_Total" ,true ,"数据库连接池");
                
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
                                        v_TR.setDataSourcesSize((v_TR.getDataSourcesSize() + v_Report.getDataSourcesSize()));        // 平均值
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
            v_RKey.put(":DBURLs"          ,StringHelp.toString(v_Report.getDbURLs() ,"" ,"<br>"));
            v_RKey.put(":DataSourcesSize" ,v_Report.getDataSourcesSize() + "");
            v_RKey.put(":ConnActiveCount" ,v_Report.getConnActiveCount() + "");
            v_RKey.put(":ConnMaxUseCount" ,v_Report.getConnMaxUseCount() + "");
            v_RKey.put(":ConnLastTime"    ,v_Report.getConnLastTime());
            v_RKey.put(":DSGStatus"       ,Help.NVL(v_Report.getDsgStatus() ,"正常"));
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        }
        
        String v_GotoTitle = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_GotoTitle += "<a href='analyseObject?DSG=Y' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_GotoTitle += "<a href='analyseObject?DSG=Y&cluster=Y' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Total.getReports().clear();
        v_Total.setReports(null);
        
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
    
    
    
    /**
     * 功能1：本机日志引擎的监控信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-13
     * @version     v1.0
     *
     * @param  i_BasePath         服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ReLoadPath       重新加载的URL。如：http://127.0.0.1:80/hy/../analyseObject?logger=Y
     * @param  i_Cluster          是否为集群
     * @param  i_TotalType        统计类型(class、method、lineNumber)
     * @param  i_SortType         排序类型(requectCount、lastTime、name、errorCount、execSumTime、execAvgTime)
     * @param  i_FilterClassName  名称的模糊过滤条件
     * @param  i_Timer            定时刷新的时长（单位：毫秒）
     * @return
     */
    public String analyseLogger(String  i_BasePath 
                               ,String  i_ReLoadPath 
                               ,boolean i_Cluster 
                               ,String  i_TotalType 
                               ,String  i_SortType 
                               ,String  i_FilterClassName
                               ,String  i_Timer)
    {
        $Logger.debug("日志引擎的监控：" + " is cluster " +  (i_Cluster ? "Yes" : "No"));
        
        StringBuilder                     v_Buffer  = new StringBuilder();
        int                               v_Index   = 0;
        String                            v_Content = this.getTemplateShowLoggerContent();
        AnalyseLoggerTotal                v_Total   = null;
        TablePartitionRID<String ,Object> v_Errors  = new TablePartitionRID<String ,Object>();
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analyseLogger_Total(i_TotalType);
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_Total = new AnalyseLoggerTotal();
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseLogger_Total" ,new Object[] {i_TotalType} ,true ,"日志引擎");
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof AnalyseLoggerTotal )
                        {
                            AnalyseLoggerTotal v_TempTotal = (AnalyseLoggerTotal)v_ResponseData.getData();
                            
                            if ( !Help.isNull(v_TempTotal.getReports()) )
                            {
                                for (LoggerReport v_Report : v_TempTotal.getReports().values())
                                {
                                    LoggerReport v_TR = v_Total.getReports().get(v_Report.getId());
                                    
                                    if ( v_TR == null )
                                    {
                                        v_Total.getReports().put(v_Report.getId() ,v_Report);
                                    }
                                    else
                                    {
                                        v_TR.setCount(       Math.max(v_TR.getCount()            ,v_Report.getCount()));
                                        v_TR.setCountNoError(Math.max(v_TR.getCountNoError()     ,v_Report.getCountNoError()));
                                        v_TR.setRequestCount(         v_TR.getRequestCount()    + v_Report.getRequestCount());
                                        v_TR.setErrorFatalCount(      v_TR.getErrorFatalCount() + v_Report.getErrorFatalCount());
                                        v_TR.setLastTime(    Math.max(v_TR.getLastTime()         ,v_Report.getLastTime()));
                                        v_TR.setExecSumTime(          v_TR.getExecSumTime()     + v_Report.getExecSumTime());
                                        v_TR.setExecAvgTime(AnalyseLoggerTotal.calcExecAvgTime(v_TR));
                                    }
                                    
                                    if ( v_Report.getErrorFatalCount() > 0L )
                                    {
                                        v_Errors.putRow(v_Report.getId() ,v_Item.getKey().getHostName() ,1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        List<LoggerReport> v_TotalList = Help.toList(v_Total.getReports());
        
        // 类名称的模糊过滤条件（不区分大小写）
        if ( !Help.isNull(i_FilterClassName) )
        {
            String v_FilterClassName = i_FilterClassName.trim().toLowerCase();
            for (int i=v_TotalList.size() - 1; i>=0; i--)
            {
                if ( !StringHelp.isContains(v_TotalList.get(i).getClassName().toLowerCase() ,v_FilterClassName) )
                {
                    v_TotalList.remove(i);
                }
            }
        }
        
        // 排序类型(最后记录时间)
        if ( Help.isNull(i_SortType) || "lastTime".equalsIgnoreCase(i_SortType) )
        {
            Help.toSort(v_TotalList ,"lastTime DESC" ,"requestCount DESC" ,"className" ,"methodName");
        }
        // 排序类型(日志执行量)
        else if ( "requectCount".equalsIgnoreCase(i_SortType) )
        {
            Help.toSort(v_TotalList ,"requestCount DESC" ,"lastTime DESC" ,"className" ,"methodName");
        }
        // 排序类型(类名、方法名)
        else if ( "name".equalsIgnoreCase(i_SortType) )
        {
            Help.toSort(v_TotalList ,"className" ,"methodName" ,"requestCount DESC" ,"lastTime DESC");
        }
        // 排序类型(Error级日志量)
        else if ( "errorCount".equalsIgnoreCase(i_SortType) )
        {
            Help.toSort(v_TotalList ,"ErrorFatalCount DESC" ,"className" ,"methodName" ,"requestCount DESC" ,"lastTime DESC");
        }
        // 排序类型(业务累计用时)
        else if ( "execSumTime".equalsIgnoreCase(i_SortType) )
        {
            Help.toSort(v_TotalList ,"execSumTime DESC" ,"execAvgTime DESC" ,"requestCount DESC" ,"className" ,"methodName");
        }
        // 排序类型(业务平均用时)
        else
        {
            Help.toSort(v_TotalList ,"execAvgTime DESC" ,"execSumTime DESC" ,"requestCount DESC" ,"className" ,"methodName");
        }
        
        long v_NowTime            = new Date().getMinutes(-2).getTime();
        long v_SumTotalCount      = 0L;
        long v_SumRequestCount    = 0L;
        long v_SumErrorFatalCount = 0L;
        long v_SumExecSumTime     = 0L;
        for (LoggerReport v_Report : v_TotalList)
        {
            Map<String ,String> v_RKey      = new HashMap<String ,String>();
            boolean             v_IsRunning = v_Report.getCountNoError() >= 2 
                                           && v_Report.getRequestCount() >= 1 
                                           && (v_Report.getRequestCount() - v_Report.getErrorFatalCount()) % v_Report.getCountNoError() > 0;
            
            v_RKey.put(":No"              ,String.valueOf(++v_Index));
            v_RKey.put(":ClassName"       ,v_Report.getClassName());
            v_RKey.put(":MethodName"      ,Help.NVL(v_Report.getMethodName() ,"-"));
            v_RKey.put(":LineNumber"      ,Help.NVL(v_Report.getLineNumber() ,"-"));
            v_RKey.put(":LogLevel"        ,Help.NVL(v_Report.getLevelName()  ,"-"));
            v_RKey.put(":TotalCount"      ,"<span style='color:" + (v_Report.getCount()           >  0 ? "green;font-weight:bold" : "gray") + ";'>" + v_Report.getCount()           + "</span>");
            v_RKey.put(":RequestCount"    ,"<span style='color:" + (v_Report.getRequestCount()    >  0 ? "green;font-weight:bold" : "gray") + ";'>" + v_Report.getRequestCount()    + "</span>");
            v_RKey.put(":ErrorFatalCount" ,"<span style='color:" + (v_Report.getErrorFatalCount() >  0 ? "red;font-weight:bold"   : "gray") + ";'>" + v_Report.getErrorFatalCount() + "</span>");
            v_RKey.put(":IsRunning"       ,v_IsRunning ? "运行中" : "-");
            v_RKey.put(":ExecSumTime"     ,"<span style='color:" + (v_Report.getExecSumTime()     >= 0 ? "green;font-weight:bold" : "gray") + ";'>" + (v_Report.getExecSumTime() >= 0 ? Date.toTimeLen(v_Report.getExecSumTime()) : "-") + "</span>");
            v_RKey.put(":ExecAvgTime"     ,"<span style='color:" + (v_Report.getExecAvgTime()     >= 0 ? "green;font-weight:bold" : "gray") + ";'>" + (v_Report.getExecAvgTime() >= 0 ? Help.round(v_Report.getExecAvgTime() ,2) : "-") + "</span>");
            v_RKey.put(":LastTime"        ,v_Report.getLastTime() <= 0L ? "" : (v_Report.getLastTime() >= v_NowTime ? new Date(v_Report.getLastTime()).getFull() : "<span style='color:gray;'>" + new Date(v_Report.getLastTime()).getFull() + "</span>"));
            
            Map<String ,Object> v_MMErrors = v_Errors.get(v_Report.getId());
            if ( !Help.isNull(v_MMErrors) )
            {
                v_RKey.put(":ErrorFatalIP" ,StringHelp.toStringKeys(v_MMErrors ,"" ,"\n"));
            }
            else
            {
                v_RKey.put(":ErrorFatalIP" ,"本机");
            }
            
            v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
            
            v_SumTotalCount      += v_Report.getCount();
            v_SumRequestCount    += v_Report.getRequestCount();
            v_SumErrorFatalCount += v_Report.getErrorFatalCount();
            v_SumExecSumTime     += v_Report.getExecSumTime();
        }
        
        // 合计
        Map<String ,String> v_RKey = new HashMap<String ,String>();
        
        v_RKey.put(":No"              ,String.valueOf(++v_Index));
        v_RKey.put(":ClassName"       ,"合计");
        v_RKey.put(":MethodName"      ,"-");
        v_RKey.put(":LineNumber"      ,"-");
        v_RKey.put(":LogLevel"        ,"-");
        v_RKey.put(":TotalCount"      ,"<span style='color:" + (v_SumTotalCount      >  0 ? "green;font-weight:bold" : "gray") + ";'>" + v_SumTotalCount      + "</span>");
        v_RKey.put(":RequestCount"    ,"<span style='color:" + (v_SumRequestCount    >  0 ? "green;font-weight:bold" : "gray") + ";'>" + v_SumRequestCount    + "</span>");
        v_RKey.put(":ErrorFatalCount" ,"<span style='color:" + (v_SumErrorFatalCount >  0 ? "red;font-weight:bold"   : "gray") + ";'>" + v_SumErrorFatalCount + "</span>");
        v_RKey.put(":IsRunning"       ,"-");
        v_RKey.put(":ExecSumTime"     ,"<span style='color:" + (v_SumExecSumTime     >= 0 ? "green;font-weight:bold" : "gray") + ";'>" + (v_SumExecSumTime >= 0 ? Date.toTimeLen(v_SumExecSumTime) : "-") + "</span>");
        v_RKey.put(":ExecAvgTime"     ,"<span style='color:" + (v_SumExecSumTime     >= 0 ? "green;font-weight:bold" : "gray") + ";'>" + (v_SumExecSumTime >= 0 ? Help.round(Help.division(v_SumExecSumTime ,Help.division(v_SumRequestCount , v_SumTotalCount)) ,2) : "-") + "</span>");
        v_RKey.put(":LastTime"        ,"-");
        
        v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        
        
        StringBuilder v_GotoTitle = new StringBuilder();
        v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
        v_GotoTitle.append("(");
        
        // 按“方法”分组统计。(默认分组方式)
        if ( Help.isNull(i_TotalType) || "method".equalsIgnoreCase(i_TotalType) )
        {
            v_GotoTitle.append("<a href='#' id='ByClassNameTotal'  style='color:#AA66CC'>类分组</a>");
            v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
            v_GotoTitle.append("<a href='#' id='ByMethodTotal'     style='color:gray'   >方法分组</a>");
            v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
            v_GotoTitle.append("<a href='#' id='ByLineNumberTotal' style='color:#AA66CC'>日志明细</a>");
        }
        // 按“类”分组统计
        else if ( "class".equalsIgnoreCase(i_TotalType) )
        {
            v_GotoTitle.append("<a href='#' id='ByClassNameTotal'  style='color:gray'   >类分组</a>");
            v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
            v_GotoTitle.append("<a href='#' id='ByMethodTotal'     style='color:#AA66CC'>方法分组</a>");
            v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
            v_GotoTitle.append("<a href='#' id='ByLineNumberTotal' style='color:#AA66CC'>日志明细</a>");
        }
        // 按“日志输出代码行”统计
        else
        {
            v_GotoTitle.append("<a href='#' id='ByClassNameTotal'  style='color:#AA66CC'>类分组</a>");
            v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
            v_GotoTitle.append("<a href='#' id='ByMethodTotal'     style='color:#AA66CC'>方法分组</a>");
            v_GotoTitle.append(StringHelp.lpad("" ,4 ,"&nbsp;"));
            v_GotoTitle.append("<a href='#' id='ByLineNumberTotal' style='color:gray'   >日志明细</a>");
        }
        
        v_GotoTitle.append(")").append(StringHelp.lpad("" ,4 ,"&nbsp;"));
        
        String v_GotoTitle02 = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_GotoTitle.append("<a href='#' id='Title_Local_Remote' style='color:#AA66CC'>查看本机</a>");
            v_GotoTitle02   += "<a href='analyseObject?xid=AnalyseBase&call=analyseLogger_RestTotal&cluster=Y&sameTime=Y' style='color:#AA66CC'>集群重置</a>";
        }
        else
        {
            v_GotoTitle.append("<a href='#' id='Title_Local_Remote' style='color:#AA66CC'>查看集群</a>");
            v_GotoTitle02   += "<a href='analyseObject?xid=AnalyseBase&call=analyseLogger_RestTotal' style='color:#AA66CC'>重置统计</a>";
        }
        
        v_Total.getReports().clear();
        v_Total = null;
        v_TotalList.clear();
        v_TotalList = null;
        v_Errors.clear();
        v_Errors = null;
        
        return StringHelp.replaceAll(this.getTemplateShowLogger()
                                    ,new String[]{":GotoTitle"           ,":Goto_02_Title" ,":Title"        ,":HttpBasePath" ,":Sort"   ,":TotalType" ,":Cluster"              ,":Timer" ,":FilterClassName"         ,":Content"}
                                    ,new String[]{v_GotoTitle.toString() ,v_GotoTitle02    ,"日志引擎分析" ,i_BasePath      ,i_SortType ,i_TotalType ,(i_Cluster ? "Y" : "N") ,i_Timer  ,Help.NVL(i_FilterClassName) ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 获取日志引擎的监控信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-17
     * @version     v1.0
     *
     * @param  i_TotalType      统计类型(class、method、lineNumber)
     * @return
     */
    public AnalyseLoggerTotal analyseLogger_Total(String i_TotalType)
    {
        return new AnalyseLoggerTotal(i_TotalType);
    }
    
    
    
    /**
     * 重置日志引擎的监控的统计数据 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-06-20
     * @version     v1.0
     *
     * @return
     */
    public void analyseLogger_RestTotal()
    {
        $Logger.debug("重置日志引擎的监控");
        
        Logger.resets();
    }
    
    
    
    private String getTemplateLogon()
    {
        return this.getTemplateContent("template.login.html");
    }
    
    
    
    private String getTemplateLogonSSO()
    {
        return this.getTemplateContent("template.loginSSO.html");
    }
    
    
    
    private String getTemplateShowXSQLGroup()
    {
        return this.getTemplateContent("template.showXSQLGroup.html");
    }
    
    
    
    private String getTemplateShowXSQLGroupContent()
    {
        return this.getTemplateContent("template.showXSQLGroupContent.html");
    }
    
    
    
    private String getTemplateShowXSQL()
    {
        return this.getTemplateContent("template.showXSQL.html");
    }
    
    
    
    private String getTemplateShowXSQLContent()
    {
        return this.getTemplateContent("template.showXSQLContent.html");
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
    
    
    
    private String getTemplateShowObjectsContent2URL()
    {
        return this.getTemplateContent("template.showObjectsContent2URL.html");
    }
    
    
    
    private String getTemplateShowWindows()
    {
        return this.getTemplateContent("windows.html" ,"org.hy.common.xml.plugins.analyse.windows");
    }
    
    
    
    private String getTemplateShowMultiWindows()
    {
        return this.getTemplateContent("multiWindows.html" ,"org.hy.common.xml.plugins.analyse.windows");
    }
    
    
    
    private String getTemplateShowWorkRest()
    {
        return this.getTemplateContent("workRest.html" ,"org.hy.common.xml.plugins.analyse.windows");
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
    
    
    
    private String getTemplateShowJobDisasterRecoverys()
    {
        return this.getTemplateContent("template.showJobDisasterRecoverys.html");
    }
    
    
    
    private String getTemplateShowJobDisasterRecoverysContent()
    {
        return this.getTemplateContent("template.showJobDisasterRecoverysContent.html");
    }
    
    
    
    private String getTemplateShowDSG()
    {
        return this.getTemplateContent("template.showDSG.html");
    }
    
    
    
    private String getTemplateShowDSGContent()
    {
        return this.getTemplateContent("template.showDSGContent.html");
    }
    
    
    
    private String getTemplateShowXSQLGroupFlow()
    {
        return this.getTemplateContent("template.showXSQLGroupFlow.html");
    }
    
    
    
    private String getTemplateShowXSQLRefTable()
    {
        return this.getTemplateContent("template.showXSQLRefTable.html");
    }
    
    
    
    private String getTemplateShowXSQLTablesRef()
    {
        return this.getTemplateContent("template.showXSQLTablesRef.html");
    }
    
    
    
    private String getTemplateShowExecuteMethod()
    {
        return this.getTemplateContent("template.execute.html");
    }
    
    
    
    private String getTemplateShowLogger()
    {
        return this.getTemplateContent("template.showLogger.html");
    }
    
    
    
    private String getTemplateShowLoggerContent()
    {
        return this.getTemplateContent("template.showLoggerContent.html");
    }
    
}
