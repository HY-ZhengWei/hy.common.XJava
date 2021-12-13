package org.hy.common.xml.junit.xjavacloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.net.ClientSocketCluster;
import org.hy.common.net.common.ClientCluster;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.net.data.LoginRequest;
import org.hy.common.net.netty.rpc.ClientRPC;
import org.hy.common.xml.log.Logger;





/**
 * 测试单元：XJavaCloud的客户端
 * 
 * @author      ZhengWei(HY)
 * @createDate  2021-09-29
 * @version     v1.0
 */
public class JU_XJavaCloudClient
{
    private static final Logger $Logger = new Logger(JU_XJavaCloudClient.class ,true);
    
    
    
    public static void main(String [] args)
    {
        ClientRPC           v_Client  = new ClientRPC().setPort(3021).setHost("127.0.0.1");
        List<ClientCluster> v_Servers = new ArrayList<ClientCluster>();
        
        v_Servers.add(v_Client);
        
        ClientSocketCluster.startServer(v_Servers);
        ClientSocketCluster.login(v_Servers ,new LoginRequest("XJava" ,"").setSystemName("Analyses"));

        
        CommunicationResponse v_CResp = v_Client.operation().sendCommand("AnalyseFS" ,"getSystemTime");
        $Logger.info("获取服务端的时间");
        System.out.println(v_CResp);
        
        v_CResp = v_Client.operation().sendCommand("AnalyseBase" ,"analyseDB_Total");
        $Logger.info("获取服务端的的XSQL分析结果");
        System.out.println(v_CResp);
        
        
        
        
        Map<ClientCluster ,CommunicationResponse> v_ResponseDatas = null;
        
        
        v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,60 * 1000 ,"AnalyseFS"   ,"getSystemTime"   ,false ,"服务器的系统时间");
        $Logger.info("获取集群的时间");
        System.out.println(v_ResponseDatas);
        
        v_ResponseDatas = ClientSocketCluster.sendCommands(v_Servers ,60 * 1000 ,"AnalyseBase" ,"analyseDB_Total" ,false ,"XSQ分析");
        $Logger.info("获取集群的XSQL分析结果");
        System.out.println(v_ResponseDatas);
        
        ClientSocketCluster.shutdownServer(v_Servers);
    }
    
}
