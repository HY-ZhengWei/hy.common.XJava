package org.hy.common.xml.plugins.analyse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.file.FileDataPacket;
import org.hy.common.file.FileHelp;
import org.hy.common.file.event.FileReadEvent;
import org.hy.common.file.event.FileReadListener;
import org.hy.common.net.ClientSocket;
import org.hy.common.net.ClientSocketCluster;
import org.hy.common.net.ServerSocket;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.AppInitConfig;
import org.hy.common.xml.plugins.analyse.data.FileReport;





/**
 * Web文件资源管理器（支持集群）
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-11
 * @version     v1.0
 *              v2.0  2018-04-10  添加：定向克隆功能。
 *                                添加：定向删除功能。
 *                                添加：XJava配置xml文件热加载功能。
 *              v3.0  2018-04-12  添加：文件内容查看，及与其它服务器对比的功能。
 *              v4.0  2018-04-16  添加：定向下载功能。
 *              v5.0  2018-09-28  添加：集群计算文件或目录的大小。
 *              v6.0  2018-10-08  添加：集群或本机执行命令文件的功能。
 *              v7.0  2018-12-20  添加：集群查看操作系统的当前时间
 *              v8.0  2019-08-26  添加：判定哪些服务没有部署文件
 *                                添加：判定集群同名文件的大小是否均相同
 *                                添加：返回失败服务IP的同时，也返回成功克隆文件的服务IP。
 *                                添加：“全体计算”功能，包括对集群目录大小的计算。
 */
@Xjava
public class AnalyseFS extends Analyse
{
    
    /** 虚拟的Web服务的主目录。有了它，就支持集群中各个服务可以在不同目录中的功能 */
    public static final String    $WebHome   = "$WebHome";
    
    /** 集群操作时，防止对本服务重复无效的操作时的文件锁 */
    public static final String    $CloudLock = ".cloudlock";
    
    /** 允许对比查看文件内容的文件类型 */
    public static final String [] $DiffTypes = new String[]{".xml" ,".txt" ,".json"    ,".ini"  ,".inf" ,".properties"
                                                           ,".log" ,".out" ,".mf"      ,".md"
                                                           ,".js"  ,".jsp" ,".css"     ,".htm" ,".html" ,".ftl" ,".svg" ,".map"
                                                           ,".sh"  ,".bat" ,".profile" ,".policy"};
    
    
    
    /**
     * 显示指定目录下的所有文件及文件夹（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-11
     * @version     v1.0
     *              v2.0  2019-08-26 添加：判定哪些服务没有部署文件
     *                               添加：判定集群同名文件的大小是否均相同
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?FS=Y&FP=xxx
     * @param  i_Cluster         是否为集群
     * @param  i_FPath           显示的目录路径
     * @param  i_SortType        排序类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public String analysePath(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster ,String i_FPath ,String i_SortType)
    {
        StringBuilder           v_Buffer  = new StringBuilder();
        int                     v_Index   = 0;
        String                  v_Content = this.getTemplateShowFilesContent();
        String                  v_FPath   = toWebHome(i_FPath);
        String                  v_AUrl    = "analyseObject?FS=Y" + (i_Cluster ? "&cluster=Y" : "") + "&S=" + i_SortType;
        int                     v_SCount  = 1;
        Map<String ,FileReport> v_Total   = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analysePath_Total(v_FPath);
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_SCount = v_Servers.size();
            v_Total  = new HashMap<String ,FileReport>();
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"analysePath_Total" ,new Object[]{v_FPath} ,true ,"访问目录" + i_FPath);
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null && v_ResponseData.getData() instanceof Map )
                        {
                            Map<String ,FileReport> v_TempTotal = (Map<String ,FileReport>)v_ResponseData.getData();
                            
                            if ( !Help.isNull(v_TempTotal) )
                            {
                                for (Map.Entry<String ,FileReport> v_FR : v_TempTotal.entrySet())
                                {
                                    FileReport v_FReport = v_Total.get(v_FR.getKey());
                                    FileReport v_FRTemp  = v_FR.getValue();
                                    
                                    if ( v_FReport != null )
                                    {
                                        // 最后修改时间为：集群中的最后修改时间，才能保证多次刷新页面时，修改时间不会随机游走
                                        if ( v_FRTemp.getLastTime().compareTo(v_FReport.getLastTime()) >= 1 )
                                        {
                                            v_FReport.setLastTime(v_FRTemp.getLastTime());
                                        }
                                        
                                        // 判定文件大小是否均相同
                                        if ( v_FRTemp.getFileSize() != v_FReport.getFileSize() )
                                        {
                                            v_FReport.setClusterSameSize(false);
                                        }
                                        
                                        v_FReport.getClusterHave().add(v_Item.getKey().getHostName());
                                    }
                                    else
                                    {
                                        v_FRTemp.getClusterHave().add(v_Item.getKey().getHostName());
                                        v_FRTemp.setClusterSameSize(true);
                                        v_Total.put(v_FR.getKey() ,v_FRTemp);
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 判定哪些服务没有部署文件 2019-08-24
                for (FileReport v_FR : v_Total.values())
                {
                    if ( !Help.isNull(v_FR.getClusterHave()) )
                    {
                        Map<String ,?> v_Haves = Help.toMap(v_FR.getClusterHave() ,(Map<String ,?>)null);
                        if ( Help.isNull(v_FR.getClusterNoHave()) )
                        {
                            v_FR.setClusterNoHave(new ArrayList<String>());
                        }
                        
                        for (ClientSocket v_Server : v_Servers)
                        {
                            if ( !v_Haves.containsKey(v_Server.getHostName()) )
                            {
                                v_FR.getClusterNoHave().add(v_Server.getHostName());
                            }
                        }
                        
                        Help.toSort(v_FR.getClusterNoHave());
                        v_Haves.clear();
                        v_Haves = null;
                    }
                }
            }
        }
        
        
        // 生成 .. 的跳转上一级目录
        Map<String ,String> v_RKey = new HashMap<String ,String>();
        if ( !i_Cluster )
        {
            File v_File = new File(toTruePath(v_FPath));
            v_File = v_File.getParentFile();
            
            if ( v_File != null )
            {
                v_RKey.put(":FileName"  ,"<a href='" + v_AUrl + "&FP=" + toWebHome(v_File.getPath()) + "'>上一级目录</a>");
            }
            else
            {
                v_RKey.put(":FileName"  ,"<a href='" + v_AUrl + "&FP=" + v_FPath + "'>上一级目录</a>");
            }
        }
        else
        {
            String [] v_FPArr = v_FPath.split("/");
            if ( v_FPArr.length >= 2 && !v_FPath.endsWith("/.."))
            {
                v_RKey.put(":FileName"  ,"<a href='" + v_AUrl + "&FP=" + v_FPath.substring(0 ,v_FPath.length() - v_FPArr[v_FPArr.length-1].length() - 1) + "'>上一级目录</a>");
            }
            else
            {
                v_RKey.put(":FileName"  ,"<a href='" + v_AUrl + "&FP=" + v_FPath + "/..'>上一级目录</a>");
            }
        }
        v_RKey.put(":No"                ,String.valueOf(++v_Index));
        v_RKey.put(":LastTime"          ,"-");
        v_RKey.put(":FileType"          ,"文件夹");
        v_RKey.put(":FileSize"          ,"<a href='#' onclick='calcAllFileSize()'>全体计算</a>");
        v_RKey.put(":PromptClusterHave" ,"");
        v_RKey.put(":ClusterHave"       ,"-");
        v_RKey.put(":HIP"               ,"");
        v_RKey.put(":Operate"           ,"");
        v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        
        
        List<FileReport> v_FReports = Help.toList(v_Total);
        if ( "1".equalsIgnoreCase(i_SortType) )
        {
            // 按修改时间排序
            Help.toSort(v_FReports ,"directory Desc" ,"lastTime Desc" ,"fileNameToUpper");
        }
        else if ( "2".equalsIgnoreCase(i_SortType) )
        {
            // 按类型
            Help.toSort(v_FReports ,"directory Desc" ,"fileType" ,"fileNameToUpper" ,"lastTime Desc");
        }
        else if ( "3".equalsIgnoreCase(i_SortType) )
        {
            // 按大小排序
            Help.toSort(v_FReports ,"directory Desc" ,"fileSize NumDesc" ,"fileNameToUpper");
        }
        else
        {
            // 默认的：按名称排序
            Help.toSort(v_FReports ,"directory Desc" ,"fileNameToUpper" ,"lastTime Desc");
        }
        
        List<String> v_FileNoNames = new ArrayList<String>();
        for (FileReport v_FReport : v_FReports)
        {
            v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"       ,String.valueOf(++v_Index));
            v_RKey.put(":LastTime" ,v_FReport.getLastTime());
            v_RKey.put(":FileType" ,v_FReport.getFileType());
            
            StringBuilder v_Operate    = new StringBuilder();
            String        v_FileNoName = v_Index + ":" + v_FReport.getFileName();
            
            v_FileNoNames.add(v_FileNoName);
            
            if ( v_FReport.isDirectory() )
            {
                v_RKey.put(":FileName" ,"<a href='" + v_AUrl + "&FP=" + v_FReport.getFullName() + "'>" + v_FReport.getFileName() + "</a>");
                v_RKey.put(":FileSize" ,"<a href='#' onclick='calcFileSize(\"" + v_FileNoName + "\" ,true)'>计算</a>");
                
                v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='cloneFile(\"").append(v_FileNoName).append("\")'>集群克隆</a>");
                v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='zipFile(\"").append(v_FileNoName).append("\")'>压缩</a>");
                v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='delFile(\"").append(v_FileNoName).append("\")'>删除</a>");
            }
            else
            {
                v_RKey.put(":FileSize" ,"<a href='#' onclick='calcFileSizeCluster(\"" + v_FileNoName + "\" ,true)'>" + StringHelp.getComputeUnit(v_FReport.getFileSize()) + "</a>");
                
                v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='cloneFile(\"").append(v_FileNoName).append("\")'>集群克隆</a>");
                
                String v_FType = v_FReport.getFileType().toLowerCase();
                if ( StringHelp.isContains(v_FType ,".zip" ,".tar" ,"gz") )
                {
                    v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='unZipFile(\"").append(v_FileNoName).append("\")'>解压</a>");
                }
                else if ( StringHelp.isContains(v_FType ,".rar") )
                {
                    // Nothing. 没有解压能力的
                }
                else
                {
                    v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='zipFile(\"").append(v_FileNoName).append("\")'>压缩</a>");
                }
                
                v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='delFile(\"").append(v_FileNoName).append("\")'>删除</a>");
                
                // 热加载xml配置文件
                if ( StringHelp.isContains(v_FType ,".xml") )
                {
                    String v_XmlFile = getReloadName(v_FReport);
                    if ( v_XmlFile != null )
                    {
                        v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='reload(\"").append(v_XmlFile).append("\")'>热加载</a>");
                    }
                }
                
                // 执行操作系统命令
                if ( StringHelp.isContains(v_FType ,".sh" ,".bat") )
                {
                    v_Operate.append(StringHelp.lpad("" ,4 ,"&nbsp;")).append("<a href='#' onclick='executeCommand(\"").append(v_FileNoName).append("\")'>执行</a>");
                }
                
                if ( StringHelp.isContains(v_FType ,$DiffTypes) )
                {
                    v_RKey.put(":FileName" ,"<a target='_blank' style='color:#AA66CC' href='" + v_AUrl + "&Action=DIFF&FP=" + v_FReport.getFilePath() + "&FN=" + v_FReport.getFileName() + "'>" + v_FReport.getFileName() + "</a>");
                }
                else
                {
                    v_RKey.put(":FileName" ,v_FReport.getFileName()); 
                }
            }
            v_RKey.put(":Operate" ,v_Operate.toString());
            
            if ( !i_Cluster )
            {
                v_RKey.put(":PromptClusterHave" ,"");
                v_RKey.put(":ClusterHave"       ,"-");
                v_RKey.put(":HIP"               ,"");
            }
            else if ( v_FReport.getClusterHave().size() == v_SCount && (v_FReport.isDirectory() || v_FReport.isClusterSameSize()))
            {
                v_RKey.put(":PromptClusterHave" ,"");
                v_RKey.put(":ClusterHave"       ,"全有");
                v_RKey.put(":HIP"               ,"");
            }
            else
            {
                if ( v_FReport.getClusterHave().size() > 0 )
                {
                    v_RKey.put(":ClusterHave" ,"<font color='red'>有差异</font>");
                }
                else if ( v_FReport.getClusterNoHave().size() > 0 && v_FReport.getClusterNoHave().size() < v_SCount )
                {
                    v_RKey.put(":ClusterHave" ,"<font color='red'>有差异</font>");
                }
                else
                {
                    File v_File = new File(v_FReport.getFullName());
                    if ( v_File.exists() )
                    {
                        v_RKey.put(":ClusterHave" ,"<font color='red'>本服务有</font>");
                    }
                    else
                    {
                        v_RKey.put(":ClusterHave" ,"<font color='red'>他服务有</font>");
                    }
                }
                
                Help.toSort(v_FReport.getClusterHave());
                v_RKey.put(":PromptClusterHave" ,"资源存在的服务 ("   + v_FReport.getClusterHave().size()   + "台)：\n" + StringHelp.toString(v_FReport.getClusterHave()   ,"" ,"\n")
                                               + "\n\n不存在的服务 (" + v_FReport.getClusterNoHave().size() + "台):\n"  + StringHelp.toString(v_FReport.getClusterNoHave() ,"" ,"\n"));
                v_RKey.put(":HIP"  ,StringHelp.toString(v_FReport.getClusterHave() ,""));
                v_RKey.put(":NoIP" ,StringHelp.toString(v_FReport.getClusterNoHave() ,""));
            }
            
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
        
        String v_Goto = StringHelp.lpad("" ,4 ,"&nbsp;");
        if ( i_Cluster )
        {
            v_Goto += "<a href='analyseObject?FS=Y&S=" + i_SortType +"&FP=" + v_FPath + "' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?FS=Y&S=" + i_SortType +"&cluster=Y&FP=" + v_FPath + "' style='color:#AA66CC'>查看集群</a>";
        }
        
        v_Goto += StringHelp.lpad("" ,4 ,"&nbsp;");
        v_Goto += "<a href='#' onclick='getSystemTimeCluster()' style='color:#AA66CC'>集群时间</a>";
        
        return StringHelp.replaceAll(this.getTemplateShowFiles()
                                    ,new String[]{":GotoTitle" ,":Title"            ,":HttpBasePath" ,":FPath" ,":Sort"    ,":cluster"             ,":SelectHIP"        ,":SelectLocalIP"         ,":AllFileNoNames"                      ,":Content"}
                                    ,new String[]{v_Goto       ,"Web文件资源管理器" ,i_BasePath      ,v_FPath  ,i_SortType ,(i_Cluster ? "Y" : "") ,makeSelectHIP(null) ,makeSelectLocalIP(null) ,StringHelp.toString(v_FileNoNames ,"") ,v_Buffer.toString()});
    }
    
    
    
    /**
     * 对比文件内容
     * 
     * 注：当本服务的文件不存在时，文件内容为空。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-12
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_FPath           文件目录路径
     * @param  i_FName           文件名称
     * @param  i_HIP             对比云服务器IP（前缀加有一个叹号!），如 !127.0.0.1。
     *                           当为空时，只显示本地文件的内容。
     */
    public String diffFile(String i_BasePath ,String i_FPath ,String i_FName ,String i_HIP)
    {
        String   v_FPath         = toWebHome(i_FPath);
        File     v_File          = new File(toTruePath(i_FPath) + Help.getSysPathSeparator() + i_FName);
        FileHelp v_FileHelp      = new FileHelp();
        String   v_HIP           = "";
        String   v_TextContent01 = "";
        String   v_TextContent02 = "";
        try
        {
            if ( v_File.exists() && v_File.isFile() )
            {
                v_TextContent01 = v_FileHelp.getContent(v_File ,"UTF-8" ,true);
            }
            
            if ( !Help.isNull(i_HIP) )
            {
                List<ClientSocket> v_Servers = Cluster.getClusters();
                
                removeHIP(v_Servers ,i_HIP ,false);
                
                if ( !Help.isNull(v_Servers) )
                {
                    CommunicationResponse v_ResponseData = v_Servers.get(0).sendCommand("AnalyseFS" ,"getFileContent" ,new Object[]{v_FPath ,i_FName});
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null )
                        {
                            v_TextContent02 = v_ResponseData.getData().toString();
                        }
                    }
                    
                    v_HIP = v_Servers.get(0).getHostName();
                }
            }
            else
            {
                v_TextContent02 = v_TextContent01;
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return StringHelp.replaceAll(this.getTemplateDiff()
                ,new String[]{":HttpBasePath" ,":FPath" ,":FName" ,":HIP"          ,":TextContent01" ,":TextContent02" ,":SelectHIP"}
                ,new String[]{i_BasePath      ,i_FPath  ,i_FName  ,Help.NVL(v_HIP) ,v_TextContent01  ,v_TextContent02  ,makeSelectHIP("SelectHIP_OnChange")});
    }
    
    
    
    /**
     * 获取文件内容
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-12
     * @version     v1.0
     *
     * @param i_FPath
     * @param i_FName
     * @return
     */
    public String getFileContent(String i_FPath ,String i_FName)
    {
        File     v_File     = new File(toTruePath(i_FPath) + Help.getSysPathSeparator() + i_FName);
        FileHelp v_FileHelp = new FileHelp();
        
        if ( !v_File.exists() || !v_File.isFile() )
        {
            return "[" + v_File.toString() + "]文件不存在或不是一个文件！";
        }
        
        String v_Content = "";
        try
        {
            v_Content = v_FileHelp.getContent(v_File ,"UTF-8" ,true);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Content;
    }
    
    
    
    /**
     * 获取有效的支持热加载的xml配置文件Key
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-10
     * @version     v1.0
     *
     * @param i_FileReport
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getReloadName(FileReport i_FileReport)
    {
        Map<String ,Object> v_XFileNames = (Map<String ,Object>)XJava.getObject(AppInitConfig.$XFileNames_XID);
        
        for (String v_XFileName : v_XFileNames.keySet())
        {
            if ( v_XFileName.equals(i_FileReport.getFileName()) )
            {
                return v_XFileName;
            }
            else if ( i_FileReport.getFullName().endsWith(v_XFileName) )
            {
                return v_XFileName;
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 生成定向操作的Html选择列表框
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-10
     * @version     v1.0
     * 
     * @param  i_OnChangeFunName  下拉列表框选择改变时的触动发JS函数名称
     *
     * @return
     */
    private String makeSelectHIP(String i_OnChangeFunName)
    {
        StringBuilder v_SelectHIP = new StringBuilder();
        List<ClientSocket> v_Servers = Cluster.getClusters();
        if ( !Help.isNull(v_Servers) )
        {
            v_SelectHIP.append("<select id='SelectHIP' name='SelectHIP'");
            if ( !Help.isNull(i_OnChangeFunName) )
            {
                v_SelectHIP.append(" onchange='").append(i_OnChangeFunName).append("()'");
            }
            v_SelectHIP.append(">");
            v_SelectHIP.append("<option disabled selected>请选择定向操作的服务器</option>");
            
            for (ClientSocket v_Server : v_Servers)
            {
                v_SelectHIP.append("<option>").append(v_Server.getHostName()).append("</option>");
            }
            
            v_SelectHIP.append("</select>");
        }
        
        return v_SelectHIP.toString();
    }
    
    
    
    /**
     * 生成定向操作本地IP地址的Html选择列表框
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-16
     * @version     v1.0
     * 
     * @param  i_OnChangeFunName  下拉列表框选择改变时的触动发JS函数名称
     *
     * @return
     */
    private String makeSelectLocalIP(String i_OnChangeFunName)
    {
        StringBuilder v_SelectLocalIP = new StringBuilder();
        String []     v_IPs           = Help.getIPs().split(" ");
        ServerSocket  v_LocalServer   = (ServerSocket)XJava.getObject(ServerSocket.class);
        
        if ( v_LocalServer != null && !Help.isNull(v_IPs) )
        {
            v_SelectLocalIP.append("<select id='SelectLocalIP' name='SelectLocalIP'");
            if ( !Help.isNull(i_OnChangeFunName) )
            {
                v_SelectLocalIP.append(" onchange='").append(i_OnChangeFunName).append("()'");
            }
            v_SelectLocalIP.append(">");
            v_SelectLocalIP.append("<option disabled selected>请选择本服务器的通讯IP</option>");
            
            for (String v_IP : v_IPs)
            {
                String [] v_IPHostName = v_IP.split("=");
                if ( v_IPHostName.length >= 2 && v_IP.indexOf("127.0.0.1") < 0 )
                {
                    v_SelectLocalIP.append("<option>").append(v_IPHostName[1].split(";")[0]).append(":").append(v_LocalServer.getPort()).append("</option>");
                }
            }
            
            v_SelectLocalIP.append("</select>");
        }
        
        return v_SelectLocalIP.toString();
    }
    
    
    
    /**
     * 本机显示指定目录下的所有文件及文件夹
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-11
     * @version     v1.0
     *
     * @return
     */
    public Map<String ,FileReport> analysePath_Total(String i_FPath)
    {
        Map<String ,FileReport> v_Ret   = new HashMap<String ,FileReport>();
        File                    v_FPath = new File(toTruePath(i_FPath));
        
        if ( v_FPath.isDirectory() )
        {
            File [] v_Files = v_FPath.listFiles();
            if ( !Help.isNull(v_Files) )
            {
                for (File v_File : v_Files)
                {
                    v_Ret.put(v_File.getName() ,new FileReport(i_FPath ,v_File));
                }
            }
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 下载文件。定向克隆文件到本服务
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-16
     * @version     v1.0
     *
     * @param i_FilePath     路径
     * @param i_FileName     名称
     * @param i_HIP          定向从哪台云服务器IP克隆文件（前缀加有一个叹号!），如 !127.0.0.1
     * @param i_LocalIPPort  本服务IP和端口Port
     * @return               返回值：0.成功
     *                           1.云服务在克隆时，文件异常
     *                           2.云服务在克隆时，文件不存在
     *                           3.本服务与云服务通讯异常
     *                           4.云服务HIP地址不存在
     */
    public String cloneFileDownload(String i_FilePath ,String i_FileName ,String i_HIP ,String i_LocalIP)
    {
        List<ClientSocket> v_Servers = Cluster.getClusters();
        
        removeHIP(v_Servers ,i_HIP ,false);
        
        if ( !Help.isNull(v_Servers) )
        {
            CommunicationResponse v_ResponseData = v_Servers.get(0).sendCommand("AnalyseFS" ,"cloneFile" ,new Object[]{toWebHome(i_FilePath) ,i_FileName ,i_LocalIP});
            
            if ( v_ResponseData.getResult() == 0 )
            {
                if ( v_ResponseData.getData() != null )
                {
                    return v_ResponseData.getData().toString();
                }
            }
            
            return StringHelp.replaceAll("{'retCode':'3'}" ,"'" ,"\"");
        } 
        else
        {
            return StringHelp.replaceAll("{'retCode':'4'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 克隆文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-14
     * @version     v1.0
     *              v2.0  2019-08-26  添加：返回失败服务IP的同时，也返回成功克隆文件的服务IP。
     *
     * @param i_FilePath  路径
     * @param i_FileName  名称
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    public String cloneFile(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        File     v_File      = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        File     v_CloudLock = null;
        FileHelp v_FileHelp  = new FileHelp();
        
        v_FileHelp.setBufferSize(1024 * 1024);
        v_FileHelp.setReturnContent(false);  // 不获取返回，可用于超大文件的读取
        
        if ( v_File.exists() )
        {
            try
            {
                v_CloudLock = new File(v_File.toString() + $CloudLock); 
                v_FileHelp.create(v_CloudLock.toString() ,Date.getNowTime().getFullMilli() ,"UTF-8");
                List<String> v_FailIP = null;
                List<String> v_SuccIP = null;
                
                if ( v_File.isDirectory() )
                {
                    String v_SaveFileName = i_FileName + "_" + Date.getNowTime().getFullMilli_ID() + ".zip";
                    String v_SaveFileFull = toTruePath(i_FilePath) + Help.getSysPathSeparator() + v_SaveFileName;
                    File   v_SaveFile     = new File(v_SaveFileFull);
                    
                    v_FileHelp.createZip4j(v_SaveFileFull ,v_File.toString());
                    
                    CloneListener v_CloneListener = new CloneListener(i_FilePath ,v_SaveFile ,v_FileHelp.getBufferSize() ,i_HIP);
                    v_FileHelp.addReadListener(v_CloneListener);
                    v_FileHelp.getContentByte(v_SaveFile);
                    
                    v_FailIP = v_CloneListener.getFailIP();
                    if ( Help.isNull(v_FailIP) )
                    {
                        // 删除临时的打包文件
                        v_SaveFile.delete();
                        
                        // 集群解压
                        String v_UnZipRet = this.unZipFileByCluster(i_FilePath ,v_SaveFileName ,i_HIP); 
                        v_UnZipRet = StringHelp.replaceAll(v_UnZipRet ,"\"" ,"'");
                        if ( StringHelp.isContains(v_UnZipRet ,"'retCode':'0'") )
                        {
                            // 集群删除
                            return this.delFileByCluster(i_FilePath ,v_SaveFileName ,i_HIP); 
                        }
                        else
                        {
                            // 集群删除
                            this.delFileByCluster(i_FilePath ,v_SaveFileName ,i_HIP); 
                            return StringHelp.replaceAll(v_UnZipRet ,"'" ,"\"");
                        }
                    }
                    else
                    {
                        v_SuccIP = v_CloneListener.getSucceedfulIP();
                        Help.toSort(v_FailIP);
                        Help.toSort(v_SuccIP);
                        
                        return StringHelp.replaceAll("{'retCode':'1','retHIP':'"              + StringHelp.toString(v_FailIP ,"") 
                                                                + "','retHIPSize':'"          + v_FailIP.size() 
                                                                + "','retSucceedfulIP':'"     + StringHelp.toString(v_SuccIP ,"") 
                                                                + "','retSucceedfulIPSize':'" + v_SuccIP.size() 
                                                                + "'}" 
                                                    ,"'" ,"\"");
                    }
                }
                else
                {
                    CloneListener v_CloneListener = new CloneListener(i_FilePath ,v_File ,v_FileHelp.getBufferSize() ,i_HIP);
                    v_FileHelp.addReadListener(v_CloneListener);
                    v_FileHelp.getContentByte(v_File);
                    
                    v_FailIP = v_CloneListener.getFailIP();
                    if ( !Help.isNull(v_FailIP) )
                    {
                        v_SuccIP = v_CloneListener.getSucceedfulIP();
                        Help.toSort(v_FailIP);
                        Help.toSort(v_SuccIP);
                        
                        return StringHelp.replaceAll("{'retCode':'1','retHIP':'"              + StringHelp.toString(v_FailIP ,"") 
                                                                + "','retHIPSize':'"          + v_FailIP.size() 
                                                                + "','retSucceedfulIP':'"     + StringHelp.toString(v_SuccIP ,"") 
                                                                + "','retSucceedfulIPSize':'" + v_SuccIP.size() 
                                                                + "'}" 
                                                    ,"'" ,"\"");
                    }
                }
                
                return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            finally
            {
                if ( v_CloudLock != null && v_CloudLock.exists() && v_CloudLock.isFile() )
                {
                    v_CloudLock.delete();
                }
            }
            
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'','retHIPSize':'','retSucceedfulIP':'','retSucceedfulIPSize':''}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2','retHIP':'','retSucceedfulIP':''}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 集群克隆文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-14
     * @version     v1.0
     *
     * @param i_Dir
     * @param i_DataPacket
     * @return
     */
    public int cloneFileUpload(String i_Dir ,FileDataPacket i_DataPacket)
    {
        String v_Dir     = toTruePath(i_Dir);
        File   v_DirFile = new File(v_Dir);
        if ( !v_DirFile.exists() || !v_DirFile.isDirectory() )
        {
            // 目录不存在时，就创建目录
            v_DirFile.mkdirs();
        }
        
        File     v_CloudLock = new File(v_Dir + Help.getSysPathSeparator() + i_DataPacket.getName() + $CloudLock);
        FileHelp v_FileHelp  = new FileHelp();
        
        try
        {
            if ( !v_CloudLock.exists() )
            {
                if ( i_DataPacket.getDataNo().intValue() == 1 )
                {
                    File v_Old = new File(v_Dir + Help.getSysPathSeparator() + i_DataPacket.getName());
                    if ( v_Old.exists() && v_Old.isFile() )
                    {
                        v_Old.delete();
                        
                        // 有删除不掉的情况，如系统正在读取的
                        if ( v_Old.exists() )
                        {
                            FileOutputStream v_SaveOutput = new FileOutputStream(v_Old ,false);

                            try
                            {
                                v_SaveOutput.write(new byte[0]);
                                v_SaveOutput.flush();
                            }
                            catch (Exception exce)
                            {
                                exce.printStackTrace();
                            }
                            finally
                            {
                                try
                                {
                                    v_SaveOutput.close();
                                }
                                catch (Exception exce)
                                {
                                    // Nothing.
                                }
                                
                                v_SaveOutput = null;
                            }
                        }
                    }
                }
                
                return v_FileHelp.uploadServer(v_Dir ,i_DataPacket);
            }
            else
            {
                // 克隆的原文件，不再二次克隆
                return FileHelp.$Upload_Finish;
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return FileHelp.$Upload_Error;
    }
    
    
    
    /**
     * 集群执行命令文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-10-08
     * @version     v1.0
     *
     * @param i_FilePath  路径
     * @param i_FileName  名称
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    public String executeCommandCluster(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        String             v_HIP     = "";
        int                v_ExecRet = 0;
        List<ClientSocket> v_Servers = Cluster.getClusters();
        
        removeHIP(v_Servers ,i_HIP ,false);
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"executeCommand" ,new Object[]{i_FilePath ,i_FileName} ,true ,"执行命令文件" + i_FileName);
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null )
                    {
                        String v_RetValue = v_ResponseData.getData().toString();
                        v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                        
                        if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                        {
                            v_ExecRet++;
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'1'") )
                        {
                            if ( !Help.isNull(v_HIP) )
                            {
                                v_HIP += ",";
                            }
                            v_HIP += v_Item.getKey().getHostName();
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'2'") )
                        {
                            // 文件不存在也按成功算
                            v_ExecRet++;
                        }
                    }
                }
                else
                {
                    if ( !Help.isNull(v_HIP) )
                    {
                        v_HIP += ",";
                    }
                    v_HIP += v_Item.getKey().getHostName();
                }
            }
        }
        
        if ( v_ExecRet == v_Servers.size() )
        {
            return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
        }
        else
        {
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + v_HIP + "'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 执行命令文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-10-08
     * @version     v1.0
     *
     * @param i_FilePath  路径
     * @param i_FileName  名称
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    public String executeCommand(String i_FilePath ,String i_FileName)
    {
        File v_File = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        
        if ( v_File.exists() && v_File.isFile() )
        {
            try
            {
                String       v_FileType = i_FileName.toLowerCase();
                List<String> v_Ret      = null;
                if ( StringHelp.isContains(v_FileType ,".sh") )
                {
                    v_Ret = Help.executeCommand("UTF-8" ,false ,true ,v_File.toString());
                }
                else if ( StringHelp.isContains(v_FileType ,".bat") )
                {
                    String v_Device = v_File.toString().substring(0 ,2);
                    v_Ret = Help.executeCommand("GBK"   ,false ,true ,"cmd.exe /c " 
                                                                      + v_Device + " && " 
                                                                      + " cd " + v_File.getParent() + " && \""
                                                                      + v_File.toString() + "\"");
                }
                
                System.out.println("-- " + Date.getNowTime().getFullMilli() + " 执行[" + v_File.toString() + "]命令文件");
                Help.print(v_Ret);
                
                return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 删除本地文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-13
     * @version     v1.0
     *
     * @param i_FilePath  路径
     * @param i_FileName  名称
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    public String delFile(String i_FilePath ,String i_FileName)
    {
        File v_File = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        
        if ( v_File.exists() )
        {
            try
            {
                if ( v_File.isDirectory() )
                {
                    FileHelp v_FileHelp = new FileHelp();
                    v_FileHelp.delFiles(v_File ,Date.getNowTime() ,true);
                    v_File.delete();
                }
                else
                {
                    v_File.delete();
                }
                
                return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 集群删除文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-15
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常，同时返回失效服务器的IP。
     */
    public String delFileByCluster(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        int                v_ExecRet = 0;
        List<ClientSocket> v_Servers = Cluster.getClusters();
        List<String>       v_FailIP  = new ArrayList<String>();
        
        removeHIP(v_Servers ,i_HIP ,false);
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"delFile" ,new Object[]{i_FilePath ,i_FileName} ,true ,"删除文件" + i_FileName);
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null )
                    {
                        String v_RetValue = v_ResponseData.getData().toString();
                        v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                        
                        if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                        {
                            v_ExecRet++;
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'1'") )
                        {
                            v_FailIP.add(v_Item.getKey().getHostName());
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'2'") )
                        {
                            v_ExecRet++;
                        }
                    }
                }
                else
                {
                    v_FailIP.add(v_Item.getKey().getHostName());
                }
            }
        }
        
        if ( v_ExecRet == v_Servers.size() )
        {
            return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
        }
        else
        {
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + StringHelp.toString(v_FailIP ,"") + "','retHIPSize':'" + v_FailIP.size() + "','retSucceedfulIP':'','retSucceedfulIPSize':'0'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 压缩文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-13
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @param i_TimeID    时间ID
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     *                           3.压缩目录中无任何文件，即空目录
     */
    public String zipFile(String i_FilePath ,String i_FileName ,String i_TimeID)
    {
        File     v_File     = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        FileHelp v_FileHelp = new FileHelp();
        
        if ( v_File.exists() )
        {
            try
            {
                String v_SaveFile = toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName + "_" + i_TimeID + ".zip";
                
                v_FileHelp.createZip4j(v_SaveFile ,v_File.toString());
                
//                List<File> v_Files = new ArrayList<File>();
//                if ( v_File.isDirectory() )
//                {
//                    v_Files.add(v_File);
//                    v_Files.addAll(v_FileHelp.getFiles(v_File ,false));
//                    if ( !Help.isNull(v_Files) )
//                    {
//                        v_FileHelp.createZip(v_SaveFile ,v_File.getParent() ,v_Files ,true);
//                    }
//                    else
//                    {
//                        return StringHelp.replaceAll("{'retCode':'3'}" ,"'" ,"\"");
//                    }
//                }
//                else
//                {
//                    v_Files.add(v_File);
//                    v_FileHelp.createZip(v_SaveFile ,null ,v_Files ,true);
//                }
                
                return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 集群压缩文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-15
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常，同时返回失效服务器的IP。
     */
    public String zipFileByCluster(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        String             v_HIP     = "";
        int                v_ExecRet = 0;
        List<ClientSocket> v_Servers = Cluster.getClusters();
        
        removeHIP(v_Servers ,i_HIP ,false);
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"zipFile" ,new Object[]{i_FilePath ,i_FileName ,Date.getNowTime().getFullMilli_ID()} ,true ,"压缩文件" + i_FileName);
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null )
                    {
                        String v_RetValue = v_ResponseData.getData().toString();
                        v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                        
                        if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                        {
                            v_ExecRet++;
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'1'") )
                        {
                            if ( !Help.isNull(v_HIP) )
                            {
                                v_HIP += ",";
                            }
                            v_HIP += v_Item.getKey().getHostName();
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'2'") )
                        {
                            v_ExecRet++;
                        }
                    }
                }
                else
                {
                    if ( !Help.isNull(v_HIP) )
                    {
                        v_HIP += ",";
                    }
                    v_HIP += v_Item.getKey().getHostName();
                }
            }
        }
        
        if ( v_ExecRet == v_Servers.size() )
        {
            return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
        }
        else
        {
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + v_HIP + "'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 解压文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-13
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    public String unZipFile(String i_FilePath ,String i_FileName)
    {
        File       v_File     = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        FileHelp   v_FileHelp = new FileHelp();
        
        if ( v_File.exists() && v_File.isFile() )
        {
            try
            {
//                v_FileHelp.setOverWrite(true);
//                v_FileHelp.UnCompressZip(v_File.toString() ,v_File.getParent() ,true);
                v_FileHelp.UnCompressZip4j(v_File.toString() ,v_File.getParent());
                
                return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 集群解压文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-15
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常，同时返回失效服务器的IP。
     */
    public String unZipFileByCluster(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        int                v_ExecRet = 0;
        List<ClientSocket> v_Servers = Cluster.getClusters();
        List<String>       v_FailIP  = new ArrayList<String>();
        
        removeHIP(v_Servers ,i_HIP ,false);
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"unZipFile" ,new Object[]{i_FilePath ,i_FileName} ,true ,"解压文件" + i_FileName);
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null )
                    {
                        String v_RetValue = v_ResponseData.getData().toString();
                        v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                        
                        if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                        {
                            v_ExecRet++;
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'1'") )
                        {
                            v_FailIP.add(v_Item.getKey().getHostName());
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'2'") )
                        {
                            // 不存在需解压的压缩包时，也认为是成功的
                            v_ExecRet++;
                        }
                    }
                }
                else
                {
                    v_FailIP.add(v_Item.getKey().getHostName());
                }
            }
        }
        
        if ( v_ExecRet == v_Servers.size() )
        {
            return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
        }
        else
        {
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + StringHelp.toString(v_FailIP ,"") + "','retHIPSize':'" + v_FailIP.size() + "','retSucceedfulIP':'','retSucceedfulIPSize':'0'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 创建目录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-16
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.目录已存在
     */
    public String mkdir(String i_FilePath ,String i_FileName)
    {
        File v_File = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        
        if ( !v_File.exists() || !v_File.isDirectory() )
        {
            try
            {
                v_File.mkdirs();
                
                return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 集群创建目录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-16
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常，同时返回失效服务器的IP。
     */
    public String mkdirByCluster(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        String             v_HIP     = "";
        int                v_ExecRet = 0;
        List<ClientSocket> v_Servers = Cluster.getClusters();
        
        removeHIP(v_Servers ,i_HIP ,true);
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"mkdir" ,new Object[]{i_FilePath ,i_FileName} ,true ,"创建目录" + i_FileName);
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null )
                    {
                        String v_RetValue = v_ResponseData.getData().toString();
                        v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                        
                        if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                        {
                            v_ExecRet++;
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'1'") )
                        {
                            if ( !Help.isNull(v_HIP) )
                            {
                                v_HIP += ",";
                            }
                            v_HIP += v_Item.getKey().getHostName();
                        }
                        else if ( StringHelp.isContains(v_RetValue ,"'retCode':'2'") )
                        {
                            // 目录存在时，也认为是成功的
                            v_ExecRet++;
                        }
                    }
                }
                else
                {
                    if ( !Help.isNull(v_HIP) )
                    {
                        v_HIP += ",";
                    }
                    v_HIP += v_Item.getKey().getHostName();
                }
            }
        }
        
        if ( v_ExecRet == v_Servers.size() )
        {
            return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
        }
        else
        {
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + v_HIP + "'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 计算目录或文件的大小
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-14
     * @version     v1.0
     *
     * @param i_FilePath  路径
     * @param i_FileName  名称
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    public String calcFileSize(String i_FilePath ,String i_FileName)
    {
        File v_File = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
        
        if ( v_File.exists() )
        {
            try
            {
                long v_Size = 0;
                
                if ( v_File.isDirectory() )
                {
                    FileHelp v_FileHelp = new FileHelp();
                    v_Size = v_FileHelp.calcSize(v_File);
                }
                else 
                {
                    v_Size = v_File.length();
                }
                
                return StringHelp.replaceAll("{'retCode':'0','fileSize':'" + StringHelp.getComputeUnit(v_Size ,2) 
                                           + "','fileByteSize':'"          + v_Size
                                           + "','lastTime':'"              + new Date(v_File.lastModified()).getFull() + "'}" ,"'" ,"\"");
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 集群计算目录或文件的大小
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-09-28
     * @version     v1.0
     *
     * @param i_FilePath
     * @param i_FileName
     * @param i_HIP       详细说明参见方法：removeHIP()
     * @return            返回值：0.成功
     *                           1.异常，同时返回失效服务器的IP。
     */
    public String calcFileSizeCluster(String i_FilePath ,String i_FileName ,String i_HIP)
    {
        String              v_HIP     = "";
        int                 v_ExecRet = 0;
        int                 v_Error   = 0;
        List<ClientSocket>  v_Servers = Cluster.getClusters();
        int                 v_SCount  = v_Servers.size();
        Map<String ,String> v_Sizes   = new HashMap<String ,String>(); 
        String              v_FSize   = null;
        String              v_FBSize  = null;
        boolean             v_IsSame  = true;
        
        try
        {
            removeHIP(v_Servers ,i_HIP ,false);
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"calcFileSize" ,new Object[]{i_FilePath ,i_FileName} ,true ,"计算大小" + i_FileName);
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null )
                        {
                            String v_RetValue = v_ResponseData.getData().toString();
                            v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                            
                            if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                            {
                                v_ExecRet++;
                                String v_FileSize  = StringHelp.getString(v_RetValue ,"'fileSize':'" ,"'");
                                String v_ByteSize  = StringHelp.getString(v_RetValue ,"'fileByteSize':'" ,"'");
                                String v_LastTime  = StringHelp.getString(v_RetValue ,"'lastTime':'" ,"'");
                                
                                if ( !Help.isNull(v_ByteSize) )
                                {
                                    if ( v_FBSize == null )
                                    {
                                        v_FBSize = v_ByteSize;
                                    }
                                    else if ( !v_FBSize.equals(v_ByteSize) )
                                    {
                                        v_IsSame = false;
                                    }
                                }
                                else
                                {
                                    if ( v_FSize == null )
                                    {
                                        v_FSize = v_FileSize;
                                    }
                                    else if ( !v_FSize.equals(v_FileSize) )
                                    {
                                        v_IsSame = false;
                                    }
                                }
                                
                                if ( !Help.isNull(v_ByteSize) )
                                {
                                    v_ByteSize = StringHelp.getComputeUnit(Long.parseLong(v_ByteSize) ,4);
                                }
                                v_Sizes.put(v_Item.getKey().getHostName() ,Help.NVL(v_ByteSize ,v_FileSize) + "," + v_LastTime);
                            }
                            else if ( StringHelp.isContains(v_RetValue ,"'retCode':'1'") )
                            {
                                v_Error++;
                                v_Sizes.put(v_Item.getKey().getHostName() ,"异常");
                                
                                if ( !Help.isNull(v_HIP) )
                                {
                                    v_HIP += ",";
                                }
                                v_HIP += v_Item.getKey().getHostName();
                            }
                            else if ( StringHelp.isContains(v_RetValue ,"'retCode':'2'") )
                            {
                                v_Sizes.put(v_Item.getKey().getHostName() ,"不存在");
                                
                                if ( !Help.isNull(v_HIP) )
                                {
                                    v_HIP += ",";
                                }
                                v_HIP += v_Item.getKey().getHostName();
                            }
                        }
                    }
                    else
                    {
                        v_Sizes.put(v_Item.getKey().getHostName() ,"异常");
                        
                        if ( !Help.isNull(v_HIP) )
                        {
                            v_HIP += ",";
                        }
                        v_HIP += v_Item.getKey().getHostName();
                    }
                }
            }
            
            v_Sizes = Help.toSort(v_Sizes);
            StringBuilder v_Buffer = new StringBuilder();
            
            v_Buffer.append("'datas':'<table>");
            for (Map.Entry<String ,String> v_Item : v_Sizes.entrySet())
            {
                String [] v_Values = (v_Item.getValue() + ", ").split(",");
                v_Buffer.append("<tr><td>")
                        .append(v_Item.getKey())
                        .append("</td><td>&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(v_Values[0])
                        .append("</td><td>&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(v_Values[1])
                        .append("</td></tr>");
            }
            v_Buffer.append("</table>");
            
            File   v_File        = new File(toTruePath(i_FilePath) + Help.getSysPathSeparator() + i_FileName);
            String v_ClusterInfo = "";
            if ( v_ExecRet >= 1 )
            {
                if ( v_ExecRet != v_SCount || !v_IsSame )
                {
                    v_ClusterInfo = "有差异";
                }
                else if ( !v_File.exists() )
                {
                    v_ClusterInfo = "集群相同";
                }
                else
                {
                    boolean v_IsLocalSame = false;  // 集群全有，并相同时，判定是否与本服务相同
                    long    v_MyFileSize  = 0;
                    
                    if ( v_File.isDirectory() )
                    {
                        FileHelp v_FileHelp = new FileHelp();
                        v_MyFileSize = v_FileHelp.calcSize(v_File);
                    }
                    else
                    {
                        v_MyFileSize = v_File.length();
                    }
                    
                    if ( Help.isNull(v_FBSize) )
                    {
                        v_IsLocalSame = v_FSize.equals(StringHelp.getComputeUnit(v_MyFileSize));
                    }
                    else
                    {
                        v_IsLocalSame = v_FBSize.equals("" + v_MyFileSize);
                    }
                    
                    v_ClusterInfo = (v_IsLocalSame ? "全部相同" : "集群相同");
                }
            }
            else if ( v_Error >= 1 )
            {
                v_ClusterInfo = "异常";
            }
            else
            {
                v_ClusterInfo = "仅本服务有";
            }
            
            return StringHelp.replaceAll("{'retCode':'0'," + v_Buffer.toString()
                                       + "','clusterInfo':'" + v_ClusterInfo
                                       + "','clusterSize':'" + v_Sizes.size()
                                       + "'}" ,"'" ,"\"");
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + v_HIP + "','clusterInfo':'异常'"
                                       + ",'clusterSize':'" + v_Sizes.size()
                                       + "'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 获取操作系统上的当前时间
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-20
     * @version     v1.0
     */
    public String getSystemTime()
    {
        return new Date().getFullMilli();
    }
    
    
    
    /**
     * 集群获取操作系统上的当前时间
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-20
     * @version     v1.0
     *
     * @return            返回值：0.成功
     *                           1.异常，同时返回失效服务器的IP。
     */
    public String getSystemTimeCluster()
    {
        String              v_HIP     = "";
        int                 v_ExecRet = 0;
        List<ClientSocket>  v_Servers = Cluster.getClusters();
        Map<String ,String> v_Times   = new HashMap<String ,String>();  
        
        if ( !Help.isNull(v_Servers) )
        {
            Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"getSystemTime" ,new Object[]{} ,true ,"系统时间");
            
            for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
            {
                CommunicationResponse v_ResponseData = v_Item.getValue();
                
                if ( v_ResponseData.getResult() == 0 )
                {
                    if ( v_ResponseData.getData() != null )
                    {
                        String v_RetValue = v_ResponseData.getData().toString();
                        v_Times.put(v_Item.getKey().getHostName() ,v_RetValue);
                        v_ExecRet++;
                    }
                }
                else
                {
                    v_Times.put(v_Item.getKey().getHostName() ,"异常");
                    
                    if ( !Help.isNull(v_HIP) )
                    {
                        v_HIP += ",";
                    }
                    v_HIP += v_Item.getKey().getHostName();
                }
            }
        }
        
        if ( v_ExecRet >= 1 )
        {
            v_Times = Help.toSort(v_Times);
            StringBuilder v_Buffer = new StringBuilder();
            
            v_Buffer.append("'datas':'<table>");
            for (Map.Entry<String ,String> v_Item : v_Times.entrySet())
            {
                String [] v_Values = (v_Item.getValue() + ", ").split(",");
                v_Buffer.append("<tr><td>")
                        .append(v_Item.getKey())
                        .append("</td><td>&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(v_Values[0])
                        .append("</td><td>&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(v_Values[1])
                        .append("</td></tr>");
            }
            v_Buffer.append("</table>'");
            
            return StringHelp.replaceAll("{'retCode':'0'," + v_Buffer.toString() + "}" ,"'" ,"\"");
        }
        else
        {
            return StringHelp.replaceAll("{'retCode':'1','retHIP':'" + v_HIP + "'}" ,"'" ,"\"");
        }
    }
    
    
    
    /**
     * 重新加载配置文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-04-11
     * @version     v1.0
     *
     * @param i_XFile     XJava配置文件名称
     * @param i_IsCluster 是否为集群
     * @return            返回值：0.成功
     *                           1.异常
     *                           2.文件不存在
     */
    @SuppressWarnings("unchecked")
    public String reload(String i_XFile ,boolean i_Cluster)
    {
        Map<String ,Object> v_XFileNames = (Map<String ,Object>)XJava.getObject(AppInitConfig.$XFileNames_XID);
        
        if ( v_XFileNames.containsKey(i_XFile) )
        {
            // 本机重新加载
            if ( !i_Cluster )
            {
                try
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
                    
                    return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
                }
                catch (Exception exce)
                {
                    return StringHelp.replaceAll("{'retCode':'1'}" ,"'" ,"\"");
                }
            }
            // 集群重新加载 
            else
            {
                int                v_ExecRet = 0 ;
                List<ClientSocket> v_Servers = Cluster.getClusters();
                
                if ( !Help.isNull(v_Servers) )
                {
                    Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"reload" ,new Object[]{i_XFile ,false} ,true ,"重新加载配置" + i_XFile);
                    StringBuilder                            v_ErrorInfo     = new StringBuilder();
                    
                    for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                    {
                        CommunicationResponse v_ResponseData = v_Item.getValue();
                        
                        if ( v_ResponseData.getResult() == 0 )
                        {
                            if ( v_ResponseData.getData() != null )
                            {
                                String v_RetValue = v_ResponseData.getData().toString();
                                v_RetValue = StringHelp.replaceAll(v_RetValue ,"\"" ,"'");
                                
                                if ( StringHelp.isContains(v_RetValue ,"'retCode':'0'") )
                                {
                                    v_ExecRet++;
                                }
                                else
                                {
                                    v_ErrorInfo.append(",").append(v_Item.getKey().getHostName());
                                }
                            }
                        }
                        else
                        {
                            v_ErrorInfo.append(",").append(v_Item.getKey().getHostName());
                        }
                    }
                    
                    if ( v_ExecRet == v_Servers.size() )
                    {
                        return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
                    }
                    else
                    {
                        return StringHelp.replaceAll("{'retCode':'1' ,'retHIP':'" + v_ErrorInfo.toString().substring(1) + "'}" ,"'" ,"\"");
                    }
                }
                else
                {
                    return StringHelp.replaceAll("{'retCode':'0'}" ,"'" ,"\"");
                }
            }
        }
        
        return StringHelp.replaceAll("{'retCode':'2'}" ,"'" ,"\"");
    }
    
    
    
    /**
     * 转为 $WebHome 字符的路径
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-12
     * @version     v1.0
     *
     * @param i_Path
     * @return
     */
    public static String toWebHome(String i_Path)
    {
        String v_WebHome = StringHelp.replaceAll(Help.getWebHomePath() ,"\\" ,"/");
        if ( v_WebHome.endsWith("/") )
        {
            v_WebHome = v_WebHome.substring(0 ,v_WebHome.length() - 1);
        }
        
        String v_Ret = StringHelp.replaceAll(i_Path ,"\\" ,"/");
        v_Ret = StringHelp.replaceAll(v_Ret ,v_WebHome ,$WebHome);
        
        if ( v_Ret.endsWith($WebHome) )
        {
            return $WebHome;
        }
        else
        {
            return v_Ret;
        }
    }
    
    
    
    /**
     * 将  $WebHome 字符的路径，转为真实的本地路径
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-12
     * @version     v1.0
     *
     * @param i_Path
     * @return
     */
    public static String toTruePath(String i_Path)
    {
        String v_WebHome = StringHelp.replaceAll(Help.getWebHomePath() ,"\\" ,"/");
        if ( v_WebHome.endsWith("/") )
        {
            v_WebHome = v_WebHome.substring(0 ,v_WebHome.length() - 1);
        }
        
        String v_Ret = StringHelp.replaceAll(i_Path ,"/" ,Help.getSysPathSeparator());
        v_Ret = StringHelp.replaceAll(v_Ret ,$WebHome ,v_WebHome);
        return v_Ret;
    }
    
    
    
    /**
     * 1. i_IsRemoveHave = true ， 删除已有资源的服务器信息
     * 2. i_IsRemoveHave = false， 删除没有资源的服务器信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-15
     * @version     v1.0
     *
     * @param io_Servers
     * @param i_HIP       云服务器IP地址。可能出现的形式如下
     *                      形式1 - 多服务同时操作，IP逗号分隔，如 127.0.0.1,127.0.0.2
     *                      形式2 - 定向服务指定操作（仅限一台服务，一个IP），如 !127.0.0.1
     *                      形式3 - 集群之外的临时服务的定向操作（仅限一台服务，一个IP），如下载 !127.0.0.1:1721
     * @param i_IsRemoveHave  有资源时删除服务器，还是无资源时删除服务器
     */
    public static void removeHIP(List<ClientSocket> io_Servers ,String i_HIP ,boolean i_IsRemoveHave)
    {
        if ( Help.isNull(io_Servers) )
        {
            return;
        }
        
        if ( !Help.isNull(i_HIP) )
        {
            String  v_HIP          = i_HIP + ",";
            boolean v_IsRemoveHave = i_IsRemoveHave;
            if ( v_HIP.startsWith("!") )
            {
                // 形式2 - 定向服务指定操作（仅限一台服务，一个IP），如 !127.0.0.1
                v_IsRemoveHave = false;
            }
            
            if ( v_IsRemoveHave )
            {
                for (int i=io_Servers.size()-1; i>=0; i--)
                {
                    if ( v_HIP.indexOf(io_Servers.get(i).getHostName() + ",") >= 0 )
                    {
                        // 删除有资源的服务器，对没有资源的服务进行操作
                        io_Servers.remove(i);
                    }
                }
            }
            else
            {
                for (int i=io_Servers.size()-1; i>=0; i--)
                {
                    if ( v_HIP.indexOf(io_Servers.get(i).getHostName() + ",") < 0 )
                    {
                        // 删除没有资源的服务器，对有资源的服务进行操作
                        io_Servers.remove(i);
                    }
                }
            }
            
            // 形式3 - 集群之外的临时服务的定向操作（仅限一台服务，一个IP），如下载 !127.0.0.1:1721
            if ( Help.isNull(io_Servers) && v_HIP.startsWith("!")  )
            {
                String [] v_IPPort = i_HIP.split(":");
                if ( v_IPPort.length == 2 && Help.isNumber(v_IPPort[1]) )
                {
                    ClientSocket v_NewServer = new ClientSocket(v_IPPort[0].substring(1) ,Integer.parseInt(v_IPPort[1]));
                    
                    io_Servers.add(v_NewServer);
                }
            }
        }
    }
    
    
    
    private String getTemplateShowFiles()
    {
        return this.getTemplateContent("template.showFiles.html");
    }
    
    
    
    private String getTemplateShowFilesContent()
    {
        return this.getTemplateContent("template.showFilesContent.html");
    }
    
    
    
    private String getTemplateDiff()
    {
        return this.getTemplateContent("template.diff.html");
    }
    
    
    
    
    
    /**
     * 克隆文件的监听器
     *
     * @author      ZhengWei(HY)
     * @createDate  2018-03-15
     * @version     v1.0
     */
    class CloneListener implements FileReadListener
    {
        
        private String              savePath;
        
        private FileDataPacket      dataPacket;
        
        private List<ClientSocket>  servers;
        
        /** 克隆成功的服务IP */
        private Map<String ,String> succeedfulIP;
        
        /** 克隆异常的服务IP */
        private Map<String ,String> failIP;
        
        private boolean             isClone;
        
        
        
        public CloneListener(String i_SavePath ,File i_File ,int i_BufferSize ,String i_HIP)
        {
            long v_Size       = i_File.length();
            this.succeedfulIP = new HashMap<String ,String>();
            this.failIP       = new HashMap<String ,String>();
            this.savePath     = i_SavePath;
            this.isClone      = false;
            this.dataPacket   = new FileDataPacket();
            this.dataPacket.setName(i_File.getName());
            this.dataPacket.setDataCount((int)Math.ceil(Help.division(v_Size , i_BufferSize)));
            this.dataPacket.setDataNo(0);
            this.dataPacket.setSize(v_Size);
            
            this.servers = Cluster.getClusters();
            removeHIP(this.servers ,i_HIP ,true);
        }
        
        
        
        /**
         * 获取：克隆成功的服务IP
         */
        public List<String> getSucceedfulIP()
        {
            return Help.toListKeys(succeedfulIP);
        }
        
        
        
        /**
         * 获取：克隆异常的服务IP
         */
        public List<String> getFailIP()
        {
            return Help.toListKeys(failIP);
        }
        


        /**
         * 读取文件内容之前
         * 
         * @param e
         * @return   返回值表示是否继续拷贝
         */
        public boolean readBefore(FileReadEvent i_Event)
        {
            return true;
        }
        
        

        /**
         * 读取文件内容的进度
         * 
         * @param e
         * @return   返回值表示是否继续拷贝
         */
        public boolean readProcess(FileReadEvent i_Event)
        {
            this.isClone = true;
            if ( Help.isNull(this.servers) )
            {
                return false;
            }
            
            this.dataPacket.setDataNo(this.dataPacket.getDataNo() + 1);
            this.dataPacket.setDataByte(i_Event.getDataByte());
            
            // 对已之前失败的服务，不再继续后面的克隆动作，这是为了性能。2019-08-26
            if ( !Help.isNull(this.failIP) )
            {
                for (String v_HostName : this.failIP.keySet())
                {
                    for (int i=this.servers.size() - 1; i>=0; i--)
                    {
                        if ( v_HostName.equals(this.servers.get(i).getHostName()) )
                        {
                            this.servers.remove(i);
                            break;
                        }
                    }
                }
            }
            
            int    v_ExecRet  = 0;
            String v_HostName = "";
            
            try
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(this.servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"cloneFileUpload" ,new Object[]{this.savePath ,this.dataPacket});
                
                for (Map.Entry<ClientSocket ,CommunicationResponse> v_Item : v_ResponseDatas.entrySet())
                {
                    v_HostName = v_Item.getKey().getHostName();
                    CommunicationResponse v_ResponseData = v_Item.getValue();
                    
                    if ( v_ResponseData.getResult() == 0 )
                    {
                        if ( v_ResponseData.getData() != null )
                        {
                            int v_UploadValue = (Integer)v_ResponseData.getData();
                            
                            if ( v_UploadValue == FileHelp.$Upload_Finish )
                            {
                                v_ExecRet++;
                                this.succeedfulIP.put(v_HostName ,v_HostName);
                            }
                            else if ( v_UploadValue == FileHelp.$Upload_GoOn )
                            {
                                v_ExecRet++;
                            }
                            else 
                            {
                                this.failIP.put(v_HostName ,v_HostName);
                            }
                        }
                    }
                    else
                    {
                        this.failIP.put(v_HostName ,v_HostName);
                    }
                }
            }
            catch (Exception exce)
            {
                this.failIP.put(v_HostName ,v_HostName);
                exce.printStackTrace();
            }
            
            // 当有任一服务成功克隆时，后续的克隆动作还是继续要做的。
            // 只有当所有服务均异常时，才停止后续的克隆动作。
            return v_ExecRet > 0;
        }
        
        
        
        /**
         * 读取文件内容完成之后
         * 
         * @param e
         */
        public void readAfter(FileReadEvent i_Event)
        {
            // 当文件大小为0时，readProcess(...)方法是不会被调用的，所以是此特殊处理一下。
            if ( !this.isClone )
            {
                readProcess(i_Event);
            }
        }
        
    }
    
}
