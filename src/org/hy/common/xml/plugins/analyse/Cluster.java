package org.hy.common.xml.plugins.analyse;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.net.common.ClientCluster;
import org.hy.common.net.netty.rpc.ClientRPC;
import org.hy.common.xml.XJava;





/**
 * 集群操作
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-01-25
 * @version     v1.0
 *              v2.0  2021-12-15  优化：使用新版本net 3.0.0。
 */
public class Cluster
{
    
    private Cluster()
    {
        
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
    @SuppressWarnings("unchecked")
    public static List<ClientCluster> getClusters()
    {
        List<ClientCluster> v_Clusters = (List<ClientCluster>)XJava.getObject("ClusterServers");
        
        if ( v_Clusters == null )
        {
            v_Clusters = new ArrayList<ClientCluster>();
        }
        
        List<ClientCluster> v_ClusterServers = new ArrayList<ClientCluster>();
        
        for (ClientCluster v_Client : v_Clusters)
        {
            // 因为 ClusterServers 是短连接，在使用完成后，就关闭的，并且超时时长也不同，所以要每次创建个新的哈
            ClientRPC v_NewClient = new ClientRPC();
            
            v_NewClient.setHost(v_Client.getHost());
            v_NewClient.setPort(v_Client.getPort());
            
            if ( v_Client instanceof ClientRPC )
            {
                v_NewClient.setComment(((ClientRPC)v_Client).getComment());
                v_NewClient.setTimeout(((ClientRPC)v_Client).getTimeout());
            }
            
            v_ClusterServers.add(v_NewClient);
        }
        
        return v_ClusterServers;
    }
    
}
