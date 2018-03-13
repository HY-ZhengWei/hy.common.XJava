package org.hy.common.xml.plugins.analyse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.file.FileHelp;
import org.hy.common.net.ClientSocket;
import org.hy.common.net.ClientSocketCluster;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.analyse.data.FileReport;





/**
 * Web文件资源管理器（支持集群）
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-11
 * @version     v1.0
 */
@Xjava
public class AnalyseFS extends Analyse
{
    
    public static final String $WebHome = "$WebHome";
    
    
    
    /**
     * 显示指定目录下的所有文件及文件夹（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-11
     * @version     v1.0
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
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseFS" ,"analysePath_Total" ,new Object[]{v_FPath});
                
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
                                    
                                    if ( v_FReport != null )
                                    {
                                        // 最后修改时间为：集群中的最后修改时间，才能保证多次刷新页面时，修改时间不会随机游走
                                        if ( v_FR.getValue().getLastTime().compareTo(v_FReport.getLastTime()) >= 1 )
                                        {
                                            v_FReport.setLastTime(v_FR.getValue().getLastTime());
                                        }
                                        v_FReport.getClusterHave().add(v_Item.getKey().getHostName());
                                    }
                                    else
                                    {
                                        v_FR.getValue().getClusterHave().add(v_Item.getKey().getHostName());
                                        v_Total.put(v_FR.getKey() ,v_FR.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        
        // 生成 .. 的跳转上一级目录
        Map<String ,String> v_RKey  = new HashMap<String ,String>();
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
        v_RKey.put(":FileSize"          ,"");
        v_RKey.put(":PromptClusterHave" ,"");
        v_RKey.put(":ClusterHave"       ,"-");
        v_RKey.put(":HIP"               ,"");
        v_RKey.put(":Operate"           ,"");
        v_Buffer.append(StringHelp.replaceAll(v_Content ,v_RKey));
        
        
        List<FileReport> v_FReports = Help.toList(v_Total);
        if ( "1".equalsIgnoreCase(i_SortType) )
        {
            // 按修改时间排序
            Help.toSort(v_FReports ,"directory Desc" ,"lastTime Desc" ,"fileName");
        }
        else if ( "2".equalsIgnoreCase(i_SortType) )
        {
            // 按类型
            Help.toSort(v_FReports ,"directory Desc" ,"fileType" ,"fileName");
        }
        else if ( "3".equalsIgnoreCase(i_SortType) )
        {
            // 按大小排序
            Help.toSort(v_FReports ,"directory Desc" ,"fileSize NumDesc" ,"fileName");
        }
        else
        {
            // 默认的：按名称排序
            Help.toSort(v_FReports ,"directory Desc" ,"fileName");
        }
        
        for (FileReport v_FReport : v_FReports)
        {
            v_RKey = new HashMap<String ,String>();
            
            v_RKey.put(":No"       ,String.valueOf(++v_Index));
            v_RKey.put(":LastTime" ,v_FReport.getLastTime());
            v_RKey.put(":FileType" ,v_FReport.getFileType());
            
            if ( v_FReport.isDirectory() )
            {
                v_RKey.put(":FileName" ,"<a href='" + v_AUrl + "&FP=" + v_FReport.getFullName() + "'>" + v_FReport.getFileName() + "</a>");
                v_RKey.put(":FileSize" ,"");
                v_RKey.put(":Operate"  ,StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>集群克隆</a>"  
                                      + StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>压缩</a>"
                                      + StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#' onclick='delFile(\"" + v_Index + ":" + v_FReport.getFileName() + "\")'>删除</a>");
            }
            else
            {
                v_RKey.put(":FileName" ,v_FReport.getFileName()); 
                v_RKey.put(":FileSize" ,StringHelp.getComputeUnit(v_FReport.getFileSize()));
                v_RKey.put(":Operate"  ,StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>集群克隆</a>" 
                                      + StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#' onclick='delFile(\"" + v_Index + ":" + v_FReport.getFileName() + "\")'>删除</a>");
            }
            
            if ( !i_Cluster )
            {
                v_RKey.put(":PromptClusterHave" ,"");
                v_RKey.put(":ClusterHave"       ,"-");
                v_RKey.put(":HIP"               ,"");
            }
            else if ( v_FReport.getClusterHave().size() == v_SCount )
            {
                v_RKey.put(":PromptClusterHave" ,"");
                v_RKey.put(":ClusterHave"       ,"全有");
                v_RKey.put(":HIP"               ,"");
            }
            else
            {
                File v_File = new File(v_FReport.getFullName());
                if ( v_File.exists() )
                {
                    v_RKey.put(":ClusterHave" ,"<font color='red'>本机有</font>");
                }
                else
                {
                    v_RKey.put(":ClusterHave" ,"<font color='red'>他机有</font>");
                }
                
                Help.toSort(v_FReport.getClusterHave());
                v_RKey.put(":PromptClusterHave" ,"资源存在的服务：\n\n" + StringHelp.toString(v_FReport.getClusterHave() ,"" ,"\n"));
                v_RKey.put(":HIP"               ,StringHelp.toString(v_FReport.getClusterHave() ,""));
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
        
        return StringHelp.replaceAll(this.getTemplateShowFiles()
                                    ,new String[]{":GotoTitle" ,":Title"          ,":HttpBasePath" ,":FPath" ,":Sort"    ,":cluster"             ,":Content"}
                                    ,new String[]{v_Goto       ,"Web文件资源管理器" ,i_BasePath      ,v_FPath  ,i_SortType ,(i_Cluster ? "Y" : "") ,v_Buffer.toString()});
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
     * 删除本地文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-13
     * @version     v1.0
     *
     * @param i_FilePath  路径
     * @param i_FileName  名称
     * @return
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
    
    
    
    private String getTemplateShowFiles()
    {
        return this.getTemplateContent("template.showFiles.html");
    }
    
    
    
    private String getTemplateShowFilesContent()
    {
        return this.getTemplateContent("template.showFilesContent.html");
    }
    
}
