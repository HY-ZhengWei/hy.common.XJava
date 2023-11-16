package org.hy.common.xml.plugins;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;
import org.hy.common.xml.event.XRequestListener;
import org.hy.common.xml.log.Logger;





/**
 * App接口工厂(通用)
 * 
 * @author      ZhengWei(HY)
 * @createDate  2013-11-17
 * @version     v1.0
 *              v2.0  2017-10-25  emn支持两种模式
 *                                   1：对象.方法 的模式。对象为XJava对象，可以配置文件中自定义。
 *                                   2：方法 的模式。这是之前的模式，为具体的实例类this.方法的调用。
 *                                以模式1为优先级别。
 *              v3.0  2023-08-16  添加：事件监听器的功能
 */
public final class AppInterfaces
{
    private static final Logger              $Logger = new Logger(AppInterfaces.class);
    
    private static AppInterfaces             $AppInterfaces;
    
    private static XJSON                     $XJson;
    
    private static Map<String ,AppInterface> $Interfaces;
    
    /** 事件监听器 */
    private static XRequestListener          $XRequestListener;
    
    
    
    /**
     * 获取：事件监听器
     * @return
     */
    public static XRequestListener getListener()
    {
		return $XRequestListener;
	}


    
    /**
     * 设置：事件监听器
     * 
     * @param i_Listener
     */
	public static void setListener(XRequestListener i_Listener)
	{
		$XRequestListener = i_Listener;
	}
	
	
	
	/**
     * 将消息分发给接收者去执行(只用于服务端：如Web服务)
     * 
     * @param i_Obj       消息的执行者(消息的接收者)
     * @param i_Message   消息本身
     * @return
     * 
     * 2023-08-16 废弃。建议使用其他的两个方法，方便配合事件监听器的功能
     */
    @Deprecated
    public static AppMessage<?> executeMessage(Object i_Obj ,String i_Message)
    {
    	return executeMessage(null ,null ,i_Obj ,i_Message);
    }
    
    
    
    /**
     * 将消息分发给接收者去执行(只用于服务端：如Web服务)
     * 
     * @param i_Obj         消息的执行者(消息的接收者)
     * @param i_AppMessage  消息本身
     * @return
     * 
     * 2023-08-16 废弃。建议使用其他的两个方法，方便配合事件监听器的功能
     */
    @Deprecated
    public static AppMessage<?> executeMessage(Object i_Obj ,AppMessage<?> i_AppMessage)
    {
    	return executeMessage(null ,null ,i_Obj ,i_AppMessage ,"");
    }



	/**
     * 将消息分发给接收者去执行(只用于服务端：如Web服务)
     * 
     * @param i_Request   请求对象
     * @param i_Response  响应对象
     * @param i_Obj       消息的执行者(消息的接收者)
     * @param i_Message   消息本身
     * @return
     */
    public static AppMessage<?> executeMessage(HttpServletRequest  i_Request
    		                                  ,HttpServletResponse i_Response
    		                                  ,Object              i_Obj
    		                                  ,String              i_Message)
    {
        if ( Help.isNull(i_Message) )
        {
            return null;
        }
        
        return executeMessage(i_Request ,i_Response ,i_Obj ,getInstace().getAppMsg(i_Message) ,i_Message);
    }
    
    
    
    /**
     * 将消息分发给接收者去执行(只用于服务端：如Web服务)
     * 
     * @param i_Request     请求对象
     * @param i_Response    响应对象
     * @param i_Obj         消息的执行者(消息的接收者)
     * @param i_AppMessage  消息本身
     * @return
     */
    public static AppMessage<?> executeMessage(HttpServletRequest  i_Request
                                              ,HttpServletResponse i_Response
                                              ,Object              i_Obj
                                              ,AppMessage<?>       i_AppMessage
                                              ,String              i_Message)
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
        
        Return<Object> v_BeforeRet = null;
        try
        {
            String [] v_EMN     = getInstace().getEMN(i_AppMessage).replace("." ,"@").split("@");
            Object    v_Instace = null;
            Method    v_Method  = null;
            
            if ( v_EMN.length >= 2 )
            {
                v_Instace = XJava.getObject(v_EMN[0].trim());
                v_Method  = v_Instace.getClass().getDeclaredMethod(v_EMN[1].trim() ,AppMessage.class);
            }
            else
            {
                v_Instace = i_Obj;
                v_Method  = v_Instace.getClass().getDeclaredMethod(v_EMN[0].trim() ,AppMessage.class);
            }
            
            if ( $XRequestListener != null )
            {
                v_BeforeRet = $XRequestListener.before(i_Request ,i_Response ,i_AppMessage ,i_Message);
                if ( v_BeforeRet != null && v_BeforeRet.booleanValue() )
                {
                    v_Ret = (AppMessage<?>)v_Method.invoke(v_Instace ,i_AppMessage);
                    $XRequestListener.succeed(i_AppMessage, v_Ret ,v_BeforeRet.getParamObj());
                }
                else
                {
                    v_Ret = i_AppMessage.clone();
                    v_Ret.setRc("-998");
                    v_Ret.setResult(false);
                    v_Ret.setRi(Help.NVL(v_BeforeRet.getParamStr() ,"监听器中断执行"));
                }
            }
            else
            {
                v_Ret = (AppMessage<?>)v_Method.invoke(v_Instace ,i_AppMessage);
            }
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            if ( v_BeforeRet != null )
            {
                $XRequestListener.fail(i_AppMessage, exce ,v_BeforeRet.getParamObj());
            }
            
            v_Ret = i_AppMessage.clone();
            v_Ret.setRc("-1");
            v_Ret.setResult(false);
            if ( exce.getCause() != null  )
            {
                v_Ret.setRi(exce.getCause().toString() + "   " + Help.NVL(exce.getMessage()));
            }
            else
            {
                v_Ret.setRi(exce.getMessage());
            }
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
        AppMessage<?> v_ErrorMsg = null;
        
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
                v_ErrorMsg = new AppMessage<Object>();
                v_ErrorMsg.setSid("Not containsKey[" + v_SID + "]");
            }
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
        }
        
        return v_ErrorMsg;
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
                return "";
            }
        }
        else
        {
            return "";
        }
    }
    
}
