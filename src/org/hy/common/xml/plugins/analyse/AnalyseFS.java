package org.hy.common.xml.plugins.analyse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.StringHelp;
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
    
    /**
     * 显示指定目录下的所有文件及文件夹（支持集群）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-11
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?FS=Y&FPath=xxx
     * @param  i_Cluster         是否为集群
     * @param  i_FPath           显示的目录路径
     * @return
     */
    @SuppressWarnings("unchecked")
    public String analysePath(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster ,String i_FPath)
    {
        StringBuilder           v_Buffer  = new StringBuilder();
        int                     v_Index   = 0;
        String                  v_Content = this.getTemplateShowFilesContent();
        String                  v_AUrl    = "analyseObject?FS=Y" + (i_Cluster ? "&cluster=Y" : "");
        int                     v_SCount  = 1;
        Map<String ,FileReport> v_Total   = null;
        
        // 本机统计
        if ( !i_Cluster )
        {
            v_Total = this.analysePath_Total(i_FPath);
        }
        // 集群统计
        else
        {
            List<ClientSocket> v_Servers = Cluster.getClusters();
            v_SCount = v_Servers.size();
            v_Total  = new HashMap<String ,FileReport>();
            
            if ( !Help.isNull(v_Servers) )
            {
                Map<ClientSocket ,CommunicationResponse> v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,Cluster.getClusterTimeout() ,"AnalyseBase" ,"analyseJob_Total");
                
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
                                        v_FReport.setClusterHave(v_FReport.getClusterHave() + 1);
                                    }
                                    else
                                    {
                                        v_FR.getValue().setHostName(v_Item.getKey().getHostName());
                                        v_Total.put(v_FR.getKey() ,v_FR.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        List<FileReport> v_FReports = Help.toList(v_Total);
        Help.toSort(v_FReports ,"directory Desc" ,"fileName");
        
        for (FileReport v_FReport : v_FReports)
        {
            Map<String ,String> v_RKey = new HashMap<String ,String>();
            
            if ( v_FReport.isDirectory() )
            {
                v_RKey.put(":FileName" ,"<a href='" + v_AUrl + "&FPath=" + v_FReport.getFullName() + "'>" + v_FReport.getFileName() + "</a>");
                v_RKey.put(":Operate"  ,StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>集群克隆</a>"  
                                      + StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>压缩</a>"
                                      + StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>删除</a>");
            }
            else
            {
                v_RKey.put(":FileName" ,v_FReport.getFileName()); 
                v_RKey.put(":Operate"  ,StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>集群克隆</a>" 
                                      + StringHelp.lpad("" ,4 ,"&nbsp;") + "<a href='#'>删除</a>");
            }
            
            if ( v_FReport.getClusterHave() == v_SCount )
            {
                v_RKey.put(":ClusterHave" ,"是");
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
                    v_RKey.put(":ClusterHave" ,"<font color='red'>" + v_FReport.getHostName() + "</font>");
                }
            }
            
            v_RKey.put(":No"       ,String.valueOf(++v_Index));
            v_RKey.put(":LastTime" ,v_FReport.getLastTime());
            v_RKey.put(":FileSize" ,StringHelp.getComputeUnit(v_FReport.getFileSize()));
            
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
            v_Goto += "<a href='analyseObject?FS=Y&FPath=" + i_FPath + "' style='color:#AA66CC'>查看本机</a>";
        }
        else
        {
            v_Goto += "<a href='analyseObject?FS=Y&cluster=Y&FPath=" + i_FPath + "' style='color:#AA66CC'>查看集群</a>";
        }
        
        return StringHelp.replaceAll(this.getTemplateShowFiles()
                                    ,new String[]{":GotoTitle" ,":Title"          ,":HttpBasePath" ,":FPath" ,":Content"}
                                    ,new String[]{v_Goto       ,"Web文件资源管理器" ,i_BasePath      ,i_FPath  ,v_Buffer.toString()});
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
        File                    v_FPath = new File(StringHelp.replaceAll(i_FPath ,"/" ,Help.getSysPathSeparator()));
        
        if ( v_FPath.isDirectory() )
        {
            File [] v_Files = v_FPath.listFiles();
            if ( !Help.isNull(v_Files) )
            {
                for (File v_File : v_Files)
                {
                    v_Ret.put(v_File.getName() ,new FileReport(v_File));
                }
            }
        }
        
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
