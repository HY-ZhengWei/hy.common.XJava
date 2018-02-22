package org.hy.common.xml.plugins;

import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Execute;
import org.hy.common.ExecuteEvent;
import org.hy.common.ExecuteListener;
import org.hy.common.Help;
import org.hy.common.net.ClientSocket;
import org.hy.common.net.data.CommunicationResponse;





/**
 * SQL节点 + 云服务计算
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-01-30
 * @version     v1.0
 *              v2.0  2018-02-22  1.修复：云计算时，某台服务器异常后，修复"云等待"死等的问题。
 */
public class XSQLNodeCloud
{
    
    /** 云服务器 */
    private ClientSocket client;
    
    /** 是否空闲 */
    private boolean      isIdle;
    
    
    
    public XSQLNodeCloud()
    {
        this(null);
    }
    
    
    
    public XSQLNodeCloud(ClientSocket i_Client)
    {
        this.client = i_Client;
        this.isIdle = true;
    }

    
    
    /**
     * 获取：云服务器
     */
    public ClientSocket getClient()
    {
        return client;
    }
    

    
    /**
     * 获取：是否空闲
     */
    public synchronized boolean isIdle()
    {
        return isIdle;
    }
    
    
    
    /**
     * 设置：是否空闲
     * 
     * @param isIdle 
     */
    public synchronized void setIdle(boolean isIdle)
    {
        this.isIdle = isIdle;
    }
    


    /**
     * 云服务计算
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-30
     * @version     v1.0
     *
     * @param i_XSQLNode
     * @param i_Params
     */
    public synchronized void executeCloud(XSQLNode i_XSQLNode ,Map<String ,Object> i_Params)
    {
        i_XSQLNode.cloudBusy();
        this.isIdle = false;
        
        System.out.println("\n" + Date.getNowTime().getFullMilli() + "  Cloud computing " + this.client.getHostName() + ":" + this.client.getPort() + " Starting ...");
        Help.print(i_Params);
        System.out.println();
        
        try
        {
            Execute v_Execute = new Execute(client ,"sendCommand" ,new Object[]{i_XSQLNode.getXjavaID().trim() ,i_XSQLNode.getMethodName().trim() ,new Object[]{i_Params}});
            
            v_Execute.addListener(new XSQLNodeCloudExecuteListener(i_XSQLNode ,this));
            
            v_Execute.start();
        }
        catch (Exception exce)
        {
            i_XSQLNode.cloudError();
            exce.printStackTrace();
        }
    }
    
    
    
    
    
    /**
     * 异步执行等待结果的监听器
     *
     * @author      ZhengWei(HY)
     * @createDate  2018-01-30
     * @version     v1.0
     */
    class XSQLNodeCloudExecuteListener implements ExecuteListener
    {
        
        private XSQLNode      xsqlNode;
        
        private XSQLNodeCloud cloud;
        
        
        
        public XSQLNodeCloudExecuteListener(XSQLNode i_XSQLNode ,XSQLNodeCloud i_XSQLNodeCloud)
        {
            this.xsqlNode = i_XSQLNode;
            this.cloud    = i_XSQLNodeCloud;
        }
        
        
        
        /**
         * 执行结果
         * 
         * @author      ZhengWei(HY)
         * @createDate  2018-01-30
         * @version     v1.0
         *
         * @param i_Event
         */
        public void result(ExecuteEvent i_Event)
        {
            if (  i_Event != null 
              && !i_Event.isError()
              &&  i_Event.getResult() != null
              &&  ((CommunicationResponse)i_Event.getResult()).getResult() == 0 )
            {
                this.cloud.isIdle = true;
                this.xsqlNode.cloudIdle();
            }
            else
            {
                // 2018-02-22  1.修复：云计算时，某台服务器异常后，修复"云等待"死等的问题。
                this.xsqlNode.cloudError();
            }
        }
        
    }
    
}
