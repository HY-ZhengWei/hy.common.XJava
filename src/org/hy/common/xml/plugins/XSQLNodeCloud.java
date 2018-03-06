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
 *                                2.添加：云计算异常时，尝试交给其它云服务计算。当重试多次(this.cloudRetryCount)云计算仍然异常时，放弃计算。
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
    
    
    
    public XSQLNodeCloud(String i_HostName ,int i_Port)
    {
        this(new ClientSocket(i_HostName ,i_Port));
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
     * @param i_Control
     * @param io_Params
     * @param io_Returns
     * @param i_ExecuteCount  执行次数，用于云计算异常时重新计算的执行次数。表示当前执行次数，下标从1开始。
     */
    public synchronized void executeCloud(XSQLNode i_XSQLNode ,XSQLGroupControl i_Control ,Map<String ,Object> io_Params ,Map<String ,Object> io_Returns ,int i_ExecuteCount)
    {
        i_XSQLNode.cloudBusy();
        this.isIdle = false;
        
        System.out.println("\n" + Date.getNowTime().getFullMilli() + "  Cloud computing " + this.client.getHostName() + ":" + this.client.getPort() + " Starting ...");
        Help.print(io_Params);
        System.out.println();
        
        Execute v_Execute = new Execute(client ,"sendCommand" ,new Object[]{i_XSQLNode.getXjavaID().trim() ,i_XSQLNode.getMethodName().trim() ,new Object[]{io_Params}});
        v_Execute.addListener(new XSQLNodeCloudExecuteListener(i_XSQLNode ,this ,i_Control ,io_Params ,io_Returns ,i_ExecuteCount));
        v_Execute.start();
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
        
        private XSQLNode            xsqlNode;
        
        private XSQLNodeCloud       cloud;
        
        private XSQLGroupControl    control;
        
        private Map<String ,Object> params;
        
        private Map<String ,Object> returns;
        
        private int                 executeCount;
        
        
        
        public XSQLNodeCloudExecuteListener(XSQLNode i_XSQLNode ,XSQLNodeCloud i_XSQLNodeCloud ,XSQLGroupControl i_Control ,Map<String ,Object> io_Params ,Map<String ,Object> io_Returns ,int i_ExecuteCount)
        {
            this.xsqlNode     = i_XSQLNode;
            this.cloud        = i_XSQLNodeCloud;
            
            // 以下参数是为了：当异常时交给其它云服务重新计算的必要参数
            this.control      = i_Control;
            this.params       = io_Params;
            this.returns      = io_Returns;
            this.executeCount = i_ExecuteCount;
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
                
                // 2018-02-23  1.添加：云计算异常时，尝试交给其它云服务计算。当重试多次云计算仍然异常时，放弃计算。
                try
                {
                    if ( this.executeCount < this.xsqlNode.getCloudRetryCount() )
                    {
                        this.xsqlNode.executeJava(this.control ,this.params ,this.returns ,this.executeCount + 1);
                    }
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
        }
        
    }
    
}
