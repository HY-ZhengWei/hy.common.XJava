package org.hy.common.xml.plugins;

import java.util.Hashtable;
import java.util.Map;

import org.hy.common.xml.XHttp;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJSONObject;
import org.hy.common.xml.XJava;
import org.hy.common.xml.log.Logger;
import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.StringHelp;





/**
 * 基础访问网络的消息
 * 
 * 主要用于客户端(如：手机)
 * 
 * @author  ZhengWei(HY)
 * @version 2013-11-14
 */
public abstract class BaseMessage
{   
    private static final Logger $Logger = Logger.getLogger(BaseMessage.class ,true);
    
    private XJSON xjson;
    
    
    
    /**
     * 获取系统编码
     * 
     * @return
     */
    protected abstract String getSysID();
    
    
    
    public BaseMessage()
    {
        this.xjson = new XJSON();
    }
    
    
    
    /**
     * 发送请求消息
     * 
     * @param i_XHttpID       XHttp对象的ID
     * @param i_SID           接口编号
     * @param i_SIDV          接口版本(整型，下标从1开始)
     * @param i_RequestParam  消息体
     * @return 返回错误信息 
     *            -1: 系统错误，附加错误信息或返回报文
     *            21: 服务端返回错误信息，附加返回的错误信息
     *            22: 非法接口调用
     *         返回成功信息
     *             paramObj 为用户信息对象
     */
    public Return<?> sendMsg(String i_XHttpID ,String i_SID ,int i_SIDV ,Object i_RequestParam)
    {
        Return<Object> v_Ret = new Return<Object>(false);
        
        try
        {
            XHttp     v_XHttp    = this.getXHttp(i_XHttpID);
            MsgInfo   v_Msg      = this.getRequestMsg(i_SID ,i_SIDV ,i_RequestParam);
            Return<?> v_Response = v_XHttp.request(v_Msg);
            
            if ( this.isDebug() )
            {
                $Logger.info("请求报文: " + v_XHttp.getRequestInfo(v_Msg));
                // System.out.println("响应报文: " + v_XHttp.getResponseInfo());
                if ( v_Response.get() )
                {
                    $Logger.info("响应报文: " + StringHelp.unescape_toUnicode(v_Response.paramStr));
                }
                else
                {
                    $Logger.info("响应报文: " + StringHelp.unescape_toUnicode(v_Response.exception.getMessage()));
                }
            }
            
            AppMessage<?> v_AppMsg = AppInterfaces.getAppMessage(StringHelp.unescape_toUnicode(v_Response.paramStr));
            if ( v_AppMsg == null )
            {
                v_Ret.paramStr = "";
                return v_Ret.paramInt(22);
            }
            else if ( !i_SID.equals(v_AppMsg.getSid()) )
            {
                v_Ret.paramStr = v_AppMsg.getSid();
                return v_Ret.paramInt(22);
            }
            else
            {
                // 成功标记
                if ( "0".equals(v_AppMsg.getRc()) )
                {
                    if ( v_AppMsg.getBody() == null )
                    {
                        v_Ret.paramStr = v_AppMsg.getRc() + "=" + v_AppMsg.getRi();
                        return v_Ret.paramInt(21);
                    }
                    else
                    {
                        v_Ret.set(true);
                        return v_Ret.paramObj(v_AppMsg.getBody());
                    }
                }
                else 
                {
                    v_Ret.paramStr = v_AppMsg.getRc() + "=" + v_AppMsg.getRi();
                    return v_Ret.paramInt(Integer.valueOf(v_AppMsg.getRc()));
                }
            }
        }
        catch (Exception exce)
        {
            if ( Help.isNull(v_Ret.paramStr) )
            {
                v_Ret.paramStr = exce.getMessage();
            }
        }
        
        return v_Ret.paramInt(-1);
    }
    
    
    
    /**
     * 生成请求消息
     * 
     * @param i_SID           接口编号
     * @param i_SIDV          接口版本(整型，下标从1开始)
     * @param i_RequestParam  消息体
     * @return
     * @throws Exception
     */
    protected synchronized MsgInfo getRequestMsg(String i_SID ,int i_SIDV ,Object i_RequestParam) throws Exception
    {
        XJSONObject v_XJSONObject = this.xjson.parser(this.getRequestMap(i_SID ,i_SIDV ,i_RequestParam));
        
        return new MsgInfo(StringHelp.escape_toUnicode(v_XJSONObject.toJSONString()));
    }
    
    
    
    /**
     * 生成请求数据
     * 
     * @param i_SID           接口编号
     * @param i_SIDV          接口版本(整型，下标从1开始)
     * @param i_RequestParam  消息体
     * @return
     * @throws Exception
     */
    protected Map<String ,Object> getRequestMap(String i_SID ,int i_SIDV ,Object i_RequestParam) throws Exception
    {
        if ( Help.isNull(this.getSysID()) )
        {
            throw new NullPointerException("SysID is null.");
        }
        
        Map<String ,Object> v_RequestMap = new Hashtable<String ,Object>();
        
        v_RequestMap.put("sysid"    ,this.getSysID());                                                               // 系统编号
        v_RequestMap.put("sid"      ,i_SID);                                                                         // 接口编号
        v_RequestMap.put("sidv"     ,String.valueOf(i_SIDV));                                                        // 接口版本
        v_RequestMap.put("sign"     ,AppInterfaces.getEncrypt(i_SID ,i_RequestParam.toString() ,this.getSysID()));   // 数字签名，MD5(消息体@PWD)
        v_RequestMap.put("tokenSec" ,AppInterfaces.getEncrypt(i_SID ,this.getSysID()           ,this.getSysID()));   // 令牌，   MD5(SYSID@PWD)
        v_RequestMap.put("body"     ,i_RequestParam);                                                                // 消息体
        
        return v_RequestMap;
    }
    
    
    
    protected Object getXObject(String i_XID)
    {
        return XJava.getObject(i_XID);
    }
    
    
    
    protected XHttp getXHttp(String i_XID)
    {
        return (XHttp)this.getXObject(i_XID);
    }
    
    
    /**
     * 是否测试及打印日志
     * 
     * @return
     */
    protected boolean isDebug()
    {
        return false;
    }
    
}
