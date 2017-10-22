package org.hy.common.xml.junit.app.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.AppInitConfig;





/**
 * 初始化信息
 * 
 * @author      ZhengWei(HY)
 * @createDate  2016-07-08
 * @version     v1.0  
 */
public final class InitConfig extends AppInitConfig
{
    
    private static boolean $Init = false;
    
    
    
    public InitConfig()
    {
        this(true);
    }
    
    
    
    public InitConfig(boolean i_IsStartJobs)
    {
        init(i_IsStartJobs);
    }
    
    
    
    @SuppressWarnings("unchecked")
    private synchronized void init(boolean i_IsStartJobs)
    {
        if ( !$Init )
        {
            $Init = true;
            
            try
            {
                this.init("sys.Config.xml");
                this.init("startup.Config.xml");
                this.init((List<Param>)XJava.getObject("StartupConfig"));
            }
            catch (Exception exce)
            {
                System.out.println(exce.getMessage());
                exce.printStackTrace();
            }
            
            Map<String ,String> v_AppMsgKeySysID = new HashMap<String ,String>();
            v_AppMsgKeySysID.put(XJava.getParam("SYSID").getValue() ,XJava.getParam("MsgPWD").getValue());
            XJava.putObject("AppMsgKeySysID" ,v_AppMsgKeySysID);
        }
    }
    
}
