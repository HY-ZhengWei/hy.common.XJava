package org.hy.common.xml.plugins;

import java.lang.reflect.Method;
import java.util.Map;

import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;

import org.hy.common.Help;





/**
 * App接口工厂(通用)
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0
 * @createDate  2013-11-17
 */
public final class AppInterfaces
{
    private static AppInterfaces             $AppInterfaces;
    
    private static XJSON                     $XJson;
    
    private static Map<String ,AppInterface> $Interfaces;
    
    
    
    /**
     * 将消息分发给接收者去执行(只用于服务端：如Web服务)
     * 
     * @param i_Obj       消息的执行者(消息的接收者) 
     * @param i_Message   消息本身
     * @return
     */
    public static AppMessage<?> executeMessage(Object i_Obj ,String i_Message)
    {
        if ( Help.isNull(i_Message) )
        {
            return null;
        }
        
        return executeMessage(i_Obj ,getInstace().getAppMsg(i_Message));
    }
    
    
    
    /**
     * 将消息分发给接收者去执行(只用于服务端：如Web服务)
     * 
     * @param i_Obj         消息的执行者(消息的接收者) 
     * @param i_AppMessage  消息本身
     * @return
     */
    public static AppMessage<?> executeMessage(Object i_Obj ,AppMessage<?> i_AppMessage)
    {
        AppMessage<?> v_Ret = null;
        
        if ( i_AppMessage == null )
        {
            return null;
        }
        else if ( !AppMessage.$Succeed.equals(i_AppMessage.getRc()) )
        {
            return i_AppMessage;
        }
        
        try
        {
            Method v_Method = i_Obj.getClass().getDeclaredMethod(getInstace().getEMN(i_AppMessage) ,AppMessage.class);
            
            v_Ret = (AppMessage<?>)v_Method.invoke(i_Obj ,i_AppMessage);
        }
        catch (Exception exce)
        {
            v_Ret = (AppMessage<?>)i_AppMessage.clone();
            v_Ret.setRc("-1");
            v_Ret.setRi(exce.getMessage());
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 发送消息，而获取服务端返回的消息(只用于客户端：如手机)
     * 
     * @param i_Message
     * @return
     */
    public static AppMessage<?> getAppMessage(String i_Message)
    {
        return getInstace().getAppMsg(i_Message);
    }
    
    
    
    /**
     * 发送消息，而获取服务端返回的消息(只用于客户端：如手机)
     * 
     * 手工指定SID是什么，而不是通过Message消息解释出来的
     * 
     * @param i_SID
     * @param i_Message
     * @return
     */
    public static AppMessage<?> getAppMessage(String i_SID ,String i_Message)
    {
        return getInstace().getAppMsg(i_SID ,i_Message);
    }
    
    
    
    /**
     * 获取接口的消息被加密过的信息
     * 
     * @param i_SID   接口编号
     * @param i_Info  需要被加密的信息
     * @param i_SysID 系统编号
     * @return
     */
    public static String getEncrypt(String i_SID ,String i_Info ,String i_SysID)
    {
        return getInstace().getEncryptInfo(i_SID ,i_Info ,i_SysID);
    }
    
    
    
    /**
     * 获取接口的消息被加密过的信息
     * 
     * @param i_Message  消息对象 
     * @return
     */
    public static String getEncrypt(AppMessage<?> i_Msg)
    {
        return getEncrypt(i_Msg.getSid() ,i_Msg.bodytoString() ,i_Msg.getSysid());
    }
    
    
    
    public synchronized static XJSON getXJson()
    {
        if ( $XJson == null )
        {
            $XJson = new XJSON();
        }
        return $XJson;
    }
    
    
    
    @SuppressWarnings("unchecked")
    private synchronized static AppInterfaces getInstace()
    {
        if ( $AppInterfaces == null)
        {
            $AppInterfaces = new AppInterfaces();
            $Interfaces    = (Map<String ,AppInterface>)XJava.getObject("AppInterfaces");
            getXJson();
        }
        
        return $AppInterfaces;
    }
    
    
    
    private AppInterfaces()
    {
        super();
    }
    
    
    
    private AppMessage<?> getAppMsg(String i_Message)
    {
        try
        {
            Map<? ,?> v_Mes = (Map<? ,?>)$XJson.parser(i_Message);
            String    v_SID = v_Mes.get("sid").toString();
            
            if ( $Interfaces.containsKey(v_SID) )
            {
                AppInterface v_AppInterface = $Interfaces.get(v_SID); 
                
                return v_AppInterface.getAppInfo(i_Message);
            }
            else
            {
                return null;
            }
        }
        catch (Exception exce)
        {
            // Nothing.
            // 为了安全，不报错，直接返回空
        }
        
        return null;
    }
    
    
    
    /**
     * 手工指定SID是什么，而不是通过Message消息解释出来的
     * 
     * @param i_SID
     * @param i_Message
     * @return
     */
    private AppMessage<?> getAppMsg(String i_SID ,String i_Message)
    {
        try
        {
            if ( !Help.isNull(i_SID) && $Interfaces.containsKey(i_SID) )
            {
                AppInterface v_AppInterface = $Interfaces.get(i_SID); 
                
                return v_AppInterface.getAppInfo(i_SID ,i_Message);
            }
            else
            {
                return null;
            }
        }
        catch (Exception exce)
        {
            // Nothing.
            // 为了安全，不报错，直接返回空
        }
        
        return null;
    }
    
    
    
    private String getEMN(AppMessage<?> i_AppMessage)
    {
        if ( $Interfaces.containsKey(i_AppMessage.getSid()) )
        {
            AppInterface v_AppInterface = $Interfaces.get(i_AppMessage.getSid()); 
            
            return v_AppInterface.getEmName();
        }
        else
        {
            return null;
        }
    }
    
    
    
    private String getEncryptInfo(String i_SID ,String i_Info ,String i_SysID)
    {
        if ( !Help.isNull(i_SID) && $Interfaces.containsKey(i_SID) )
        {
            AppInterface v_AppInterface = $Interfaces.get(i_SID); 
            
            if ( !Help.isNull(v_AppInterface.getMsgKey(i_SID ,i_SysID)) )
            {
                return v_AppInterface.encrypt(i_Info ,i_SID ,i_SysID);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
}
