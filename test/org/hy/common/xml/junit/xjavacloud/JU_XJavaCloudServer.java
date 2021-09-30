package org.hy.common.xml.junit.xjavacloud;

import org.hy.common.Date;
import org.hy.common.net.netty.rpc.ServerRPC;
import org.hy.common.xml.XJava;





/**
 * 测试单元：XJavaCloud的服务端
 * 
 * @author      ZhengWei(HY)
 * @createDate  2021-09-29
 * @version     v1.0
 */
public class JU_XJavaCloudServer
{
    
    public static void main(String [] args)
    {
        XJava.putObject("TEST-Date-1" ,new Date());
        XJava.putObject("TEST-Date-2" ,new java.util.Date());
        
        
        
        ServerRPC v_Server = new ServerRPC().setPort(3021);
        v_Server.start();
    }
    
}
