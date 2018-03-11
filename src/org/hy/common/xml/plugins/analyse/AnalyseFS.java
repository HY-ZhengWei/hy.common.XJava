package org.hy.common.xml.plugins.analyse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.net.ClientSocket;
import org.hy.common.net.ClientSocketCluster;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.thread.JobReport;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.analyse.data.AnalyseJobTotal;





/**
 * 网页版本的文件管理系统（支持集群）
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
     * @createDate  2018-02-28
     * @version     v1.0
     *
     * @param  i_BasePath        服务请求根路径。如：http://127.0.0.1:80/hy
     * @param  i_ObjectValuePath 对象值的详情URL。如：http://127.0.0.1:80/hy/../analyseObject?FS=Y&FPath=xxx
     * @param  i_Cluster         是否为集群
     * @return
     */
    public String analysePath(String i_BasePath ,String i_ObjectValuePath ,boolean i_Cluster ,String i_FPath)
    {
        StringBuilder   v_Buffer  = new StringBuilder();
        int             v_Index   = 0;
        String          v_Content = this.getTemplateShowFilesContent();
        AnalyseJobTotal v_Total   = null;
        
        File v_FPath = new File(i_FPath);
        
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
        
        Help.toSort(v_Total.getReports() ,"nextTime" ,"lastTime" ,"intervalType" ,"intervalLen NUMASC" ,"jobID");
        
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
        
        return StringHelp.replaceAll(this.getTemplateShowFiles()
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
    
    
    
    private String getTemplateShowFiles()
    {
        return this.getTemplateContent("template.showFiles.html");
    }
    
    
    
    private String getTemplateShowFilesContent()
    {
        return this.getTemplateContent("template.showFilesContent.html");
    }
    
}
