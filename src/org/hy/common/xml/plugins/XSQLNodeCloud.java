package org.hy.common.xml.plugins;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Execute;
import org.hy.common.ExecuteEvent;
import org.hy.common.ExecuteListener;
import org.hy.common.Help;
import org.hy.common.net.common.ClientCluster;
import org.hy.common.net.data.CommunicationResponse;
import org.hy.common.net.data.LoginRequest;
import org.hy.common.net.netty.rpc.ClientRPC;
import org.hy.common.xml.log.Logger;





/**
 * SQL节点 + 云服务计算
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-01-30
 * @version     v1.0
 *              v2.0  2018-02-22  1.修复：云计算时，某台服务器异常后，修复"云等待"死等的问题。
 *                                2.添加：云计算异常时，尝试交给其它云服务计算。当重试多次(this.cloudRetryCount)云计算仍然异常时，放弃计算。
 *              v3.0  2021-12-18  1.升级：使用Net 3.0.0 版本
 */
public class XSQLNodeCloud
{
    private static Logger $Logger = Logger.getLogger(XSQLNodeCloud.class ,true);
    
    
    
    /** 云服务器 */
    private ClientCluster client;
    
    /** 是否空闲 */
    private boolean       isIdle;
    
    
    
    public XSQLNodeCloud()
    {
        this(null);
    }
    
    
    
    /**
     * 2021-12-18 建议不再使用本方法创建通讯，因没法定义每台服务的个性化超时时长
     *            建议使用：XSQLNodeCloud(ClientCluster i_Client) 方法
     * 
     * @param i_HostName
     * @param i_Port
     */
    @Deprecated
    public XSQLNodeCloud(String i_HostName ,int i_Port)
    {
        this(new ClientRPC().setHost(i_HostName).setPort(i_Port));
    }
    
    
    
    public XSQLNodeCloud(ClientCluster i_Client)
    {
        this.client = i_Client;
        this.isIdle = true;
    }

    
    
    /**
     * 获取：云服务器
     */
    public ClientCluster getClient()
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
        if ( this.client == null )
        {
            return;
        }
        
        i_XSQLNode.cloudBusy();
        this.isIdle = false;
        
        System.out.println("\n" + Date.getNowTime().getFullMilli() + "  Cloud computing " + this.client.getHost() + ":" + this.client.getPort() + " Starting ...");
        Help.print(io_Params);
        System.out.println();
        
        // 在控制循环的SQL节点中，当向下循环时会清理上一循环的参数。所以要在此复制一份。 ZhengWei(HY) Add 2018-08-08
        // 这与上次 "2018-07-26 优化：及时释放资源，自动的GC太慢了" 有关。
        Map<String ,Object> v_Params = new HashMap<String ,Object>(io_Params);
        
        try
        {
            if ( !this.client.operation().isStartServer() )
            {
                this.client.operation().startServer();
            }
            
            if ( !this.client.operation().isLogin() )
            {
                this.client.operation().login(new LoginRequest("XSQL" ,"").setSystemName("XSQLCloud"));
            }
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
        }
        
        Execute v_Execute = new Execute(this.client.operation() ,"sendCommand" ,new Object[]{-1 ,i_XSQLNode.getXid().trim() ,i_XSQLNode.getMethodName().trim() ,new Object[]{v_Params}});
        v_Execute.addListener(new XSQLNodeCloudExecuteListener(i_XSQLNode ,this ,i_Control ,v_Params ,io_Returns ,i_ExecuteCount));
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
        @Override
        public void result(ExecuteEvent i_Event)
        {
            if (  i_Event != null
              && !i_Event.isError()
              &&  i_Event.getResult() != null
              &&  ((CommunicationResponse)i_Event.getResult()).getResult() == 0 )
            {
                System.out.println("\n"
                                 + Date.getNowTime().getFullMilli()
                                 + "  Cloud computing "
                                 + this.cloud.getClient().getHost()
                                 + ":"
                                 + this.cloud.getClient().getPort()
                                 + " Finish.");
                
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
