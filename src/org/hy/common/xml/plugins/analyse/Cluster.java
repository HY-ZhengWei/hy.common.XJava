package org.hy.common.xml.plugins.analyse;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.net.common.ClientCluster;
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
        
        return v_Clusters;
    }
    
}
