package org.hy.common.xml.plugins.analyse;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.app.Param;
import org.hy.common.net.common.ClientCluster;
import org.hy.common.net.netty.rpc.ClientRPC;
import org.hy.common.xml.XJava;





/**
 * 集群操作
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-01-25
 * @version     v1.0
 */
public class Cluster
{
    
    private Cluster()
    {
        
    }
    
    
    
    /**
     * 集群并发通讯的超时时长。默认为：30000毫秒
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-25
     * @version     v1.0
     *
     * @return
     */
    public static long getClusterTimeout()
    {
        return Long.parseLong(Help.NVL(Help.NVL(XJava.getParam("ClusterTimeout") ,new Param()).getValue() ,"60000"));
    }
    
    
    
    /**
     * 获取集群配置信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-17
     * @version     v1.0
     *
     * @return
     */
    public static List<ClientCluster> getClusters()
    {
        String []           v_ClusterServers = Help.NVL(Help.NVL(XJava.getParam("ClusterServers") ,new Param()).getValue()).split(",");
        List<ClientCluster> v_Clusters       = new ArrayList<ClientCluster>();
        
        if ( !Help.isNull(v_ClusterServers) )
        {
            for (String v_Server : v_ClusterServers)
            {
                if ( !Help.isNull(v_Server) )
                {
                    String [] v_HostPort = (v_Server.trim() + ":1721").split(":");
                    
                    ClientRPC v_Clent = new ClientRPC().setHost(v_HostPort[0]).setPort(Integer.parseInt(v_HostPort[1]));
                    
                    v_Clusters.add(v_Clent);
                }
            }
        }
        
        return v_Clusters;
    }
    
}
