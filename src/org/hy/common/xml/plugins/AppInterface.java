package org.hy.common.xml.plugins;

import java.util.Map;
import java.util.Random;

import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;
import org.hy.common.Counter;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.TablePartitionRID;





/**
 * App接口(通用)
 * 
 * @author  ZhengWei(HY)
 * @version v1.0  2013-11-17
 * @version v1.1  2014-09-19  当消息加密码的密钥为空时，不进行判断加密
 * @version v1.2  2014-12-15  添加请求次数、请求成功数、累计用时时长
 * @version v1.3  2016-02-23  删除msgKey属性。只保留getMsgkey()方法。但方法还添加了两个入参。
 *                            这做的目地是：可以保证msgKey可以通过外界动态的生成，适应"每个系统的每个接口"的消息密钥都可以不同（可独立设置）。
 *                            外界可有两种选择的确定消息密钥的适应权限粒度
 *                               1. 接口级：i_SID + i_SysID 组合才能确定的消息密钥。       适应"每个系统的每个接口"的消息密钥都可以不同。
 *                               2. 系统级：i_SysID         系统编号一项就能确定的消息密钥。适应"每个系统"的消息密钥都可以不同，此系统调用的所有接口都是同一个消息密钥。
 *          v2.0  2017-10-25  emName支持两种模式
 *                                   1：对象.方法 的模式。对象为XJava对象，可以配置文件中自定义。
 *                                   2：方法 的模式。这是之前的模式，为具体的实例类this.方法的调用。
 *                                以模式1为优先级别。
 *          v3.0  2018-01-20  添加：当系统级密钥有效时，当接口级密钥为空字符串时，支持接口无密钥的访问。
 */
public class AppInterface
{
    private static final int  $RandomMax = 99999;
    
    private static final int  $RandomLen = ("" + $RandomMax).length();
    
    
    
    /** 接口名称或编码 */
    private String          name;
    
    /** 接口消息解释出的对象的元类型 */
    private String          className;
    
    /** 
     * 执行方法的方法名称(只用于服务端：如Web服务)
     *  
     * emName支持两种模式
     *   1：对象.方法 的模式。对象为XJava对象，可以配置文件中自定义。
     *   2：方法 的模式。这是之前的模式，为具体的实例类this.方法的调用。
     * 以模式1为优先级别。
     */
    private String          emName;
    
    private XJSON           xjson;
    
    /** 请求次数 */
    private Counter<String> requestCount;
    
    /** 请求成功，并成功返回次数 */
    private Counter<String> successCount;
    
    /** 请求成功，并成功返回的累计用时时长 */
    private Counter<String> successTimeLen;
    
    
    
    public AppInterface()
    {
        this("");
    }
    
    
    
    public AppInterface(Class<?> i_Class)
    {
        this(i_Class.getName());
    }
    
    
    
    public AppInterface(String i_ClassName)
    {
        this.className      = i_ClassName;
        this.xjson          = new XJSON();
        this.requestCount   = new Counter<String>();
        this.successCount   = new Counter<String>();
        this.successTimeLen = new Counter<String>();
    }
    
    
    
    /**
     * 
     * 
     * @param i_Info
     * @return  返回 null 表示系统配置错误。或非法调用
     */
    public AppMessage<?> getAppInfo(String i_Info)
    {
        return this.getAppInfo(null ,i_Info);
    }
    
    
    
    /**
     * 部署Web服务所在主机的标示
     * 
     * @return
     */
    private String getAppWebID()
    {
        Object v_Ret = XJava.getObject("APPWEBID");
        
        if ( v_Ret == null )
        {
            return "";
        }
        else
        {
            return v_Ret.toString();
        }
    }
    
    
    
    /**
     * 手工指定SID是什么，而不是通过Message消息解释出来的
     * 
     * @param i_Info
     * @return  返回 null 表示系统配置错误。或非法调用
     */
    @SuppressWarnings("unchecked")
    public AppMessage<?> getAppInfo(String i_SID ,String i_Info)
    {
        AppMessage<Object> v_Msg = null;
        
        try
        {
            this.xjson.setObjectClass(AppMessage.class);
            v_Msg = (AppMessage<Object>)this.xjson.parser(i_Info);
            v_Msg.setSid(Help.NVL(i_SID ,v_Msg.getSid()));
            v_Msg.satMsg(i_Info);
            v_Msg.satCreateTime(new Date());
            if ( Help.isNull(v_Msg.getSerialNo()) )
            {
                Random v_Random = new Random();
                v_Msg.setSerialNo(v_Msg.gatCreateTime().getFullMilli_ID() + StringHelp.lpad(v_Random.nextInt($RandomMax) ,$RandomLen ,"0") + getAppWebID());
            }
            
            if ( AppMessage.$Succeed.equals(v_Msg.getRc()) )
            {
                this.xjson.setObjectClassName(this.className);
                try
                {
                    v_Msg.setBody(this.xjson.parser(i_Info ,"body"));
                }
                catch (Exception exce)
                {
                    v_Msg.setRc(exce.getMessage());
                    v_Msg.setBody(null);
                    v_Msg.setSid("ERROR-01：" + exce.getMessage());
                    return v_Msg;
                }
                
                // 当消息加密码的密钥为空时，不进行判断加密
                if ( Help.isNull(this.getMsgKey(i_SID ,v_Msg.getSysid())) )
                {
                    return v_Msg;
                }
                else
                {
                    if ( encrypt(v_Msg.getSysid() ,v_Msg.getSid() ,v_Msg.getSysid()).equals(v_Msg.getTokenSec()) )
                    {
                        if ( encrypt(v_Msg.bodytoString() ,v_Msg.getSid() ,v_Msg.getSysid()).equals(v_Msg.getSign()) )
                        {
                            return v_Msg;
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
    
    
    
    public String encrypt(String i_Info ,String i_SID ,String i_SysID) 
    {
        return StringHelp.md5(i_Info + this.getMsgKey(i_SID ,i_SysID));
    }


    
    public void setClassName(String className)
    {
        this.className = className;
    }


    
    public String getClassName()
    {
        return className;
    }
    
    
    
    public String getName()
    {
        return name;
    }


    
    public void setName(String name)
    {
        this.name = name;
    }


    
    /**
     * 执行方法的方法名称(只用于服务端：如Web服务)
     *  
     * emName支持两种模式
     *   1：对象.方法 的模式。对象为XJava对象，可以配置文件中自定义。
     *   2：方法 的模式。这是之前的模式，为具体的实例类this.方法的调用。
     * 以模式1为优先级别。
     * 
     * @return
     */
    public String getEmName()
    {
        return emName;
    }


    
    /**
     * 执行方法的方法名称(只用于服务端：如Web服务)
     *  
     * emName支持两种模式
     *   1：对象.方法 的模式。对象为XJava对象，可以配置文件中自定义。
     *   2：方法 的模式。这是之前的模式，为具体的实例类this.方法的调用。
     * 以模式1为优先级别。
     * 
     * @param emName
     */
    public void setEmName(String emName)
    {
        this.emName = emName;
    }
    
    
    
    /**
     * 可以保证msgKey可以通过外界动态的生成，适应"每个系统的每个接口"的消息密钥都可以不同（可独立设置）。
     * 外界可有两种选择的确定消息密钥的适应权限粒度
     *   1. 接口级：i_SID + i_SysID 组合才能确定的消息密钥。       适应"每个系统的每个接口"的消息密钥都可以不同。
     *   2. 系统级：i_SysID         系统编号一项就能确定的消息密钥。适应"每个系统"的消息密钥都可以不同，此系统调用的所有接口都是同一个消息密钥。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-23
     * @version     v1.0
     *
     * @param i_SID    接口编号
     * @param i_SysID  系统编号
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getMsgKey(String i_SID ,String i_SysID)
    {
        TablePartitionRID<String ,String> v_SysSIDKeys = (TablePartitionRID<String ,String>)XJava.getObject("AppMsgKeySSID");
        String                            v_Key        = null;
        
        if ( !Help.isNull(v_SysSIDKeys) )
        {
            v_Key = v_SysSIDKeys.getRow(i_SysID ,i_SID);
        }
        
        // 接口级消息级密钥可为空字符串，表示无密钥访问  ZhengWei(HY) Edit 2018-01-20
        if ( v_Key == null ) 
        {
            Map<String ,String> v_SysIDKeys = (Map<String ,String>)XJava.getObject("AppMsgKeySysID");
            
            if ( !Help.isNull(v_SysIDKeys) )
            {
                v_Key = v_SysIDKeys.get(i_SysID);
            }
        }
        
        return Help.NVL(v_Key);
    }
    

    
    public Counter<String> getRequestCount()
    {
        return requestCount;
    }


    
    public void setRequestCount(Counter<String> requestCount)
    {
        this.requestCount = requestCount;
    }
    
    
    
    /**
     * 请求次数++
     * 
     * @param i_SysID     系统ID
     * @return
     */
    public long request(String i_SysID)
    {
        return this.requestCount.put(Help.NVL(i_SysID));
    }

    
    
    public Counter<String> getSuccessCount()
    {
        return successCount;
    }

    
    public void setSuccessCount(Counter<String> successCount)
    {
        this.successCount = successCount;
    }
    
    
    
    /**
     * 请求成功，并成功返回次数++
     * 
     * @param i_SysID     系统ID
     * @return
     */
    public long success(String i_SysID)
    {
        return this.successCount.put(Help.NVL(i_SysID));
    }
    
    
    
    /**
     * 1. 请求成功，并成功返回次数++
     * 2. 请求成功，并成功返回的累计用时时长++
     * 
     * @param i_SysID     系统ID
     * @param i_TimeLen
     * @return
     */
    public synchronized long success(String i_SysID ,long i_TimeLen)
    {
        String v_SysID = Help.NVL(i_SysID);
        this.successTimeLen.put(v_SysID ,i_TimeLen);
        return this.successCount.put(Help.NVL(v_SysID));
    }

    
    
    public Counter<String> getSuccessTimeLen()
    {
        return successTimeLen;
    }

    
    
    public void setSuccessTimeLen(Counter<String> successTimeLen)
    {
        this.successTimeLen = successTimeLen;
    }
    
}
