package org.hy.common.xml.junit.xjavacloud;

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
        ClientRPC v_Client = new ClientRPC().setPort(3021).setHost("127.0.0.1");
        v_Client.start();
        
        // 登录
        LoginRequest v_LoginRequest = new LoginRequest();
        v_LoginRequest.setUserName  ("用户1");
        v_LoginRequest.setSystemName("系统1");
        v_Client.operation().login(v_LoginRequest);
        
        // 通讯
        $Logger.info("获取服务端的对象：时间1：" + v_Client.operation().getObject("TEST-Date-1"));
        $Logger.info("获取服务端的对象：时间2：" + v_Client.operation().getObject("TEST-Date-2"));
        
        $Logger.info("获取服务端的对象：多个对象：" + v_Client.operation().getObjects("TEST-Date"));
    }
    
}
