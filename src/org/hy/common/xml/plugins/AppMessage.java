package org.hy.common.xml.plugins;

import java.io.Serializable;
import java.util.List;

import org.hy.common.xml.XJSON;
import org.hy.common.xml.log.Logger;
import org.hy.common.Date;
import org.hy.common.Help;





/**
 * App消息(通用)
 * 
 * @author  ZhengWei(HY)
 * @version v1.0  2013-11-17
 *          v1.1  2014-09-19  添加三个参数 encry、format、session
 *          v1.2  2014-09-26  添加一个参数 result
 *                            重写 toString(XJSON) 方法
 *          v1.3  2014-12-03  添加二个参数 serialNo、createTime
 *          v1.4  2016-02-24  将token改为tokenSec。
 */
public class AppMessage<O> implements Cloneable ,Serializable
{
    private static final Logger $Logger = Logger.getLogger(AppMessage.class ,true);
    
    private static final long serialVersionUID = -7150988524339365341L;


    /** 成功标识 */
    public static final String $Succeed = "0";
    
    
    /** 系统编号 */
    private String     sysid;
    
    /** 接口编号 */
    private String     sid;
    
    /** 接口版本 */
    private String     sidv;
    
    /** 数字签名 */
    private String     sign;
    
    /** 令牌，MD5(SYSID@PWD)。Sec是安全的意思 */
    private String     tokenSec;
    
    /** 参数的加密类型。如MD5、DES等 */
    private String     encry;
    
    /** 指定响应格式。默认JSON */
    private String     format;
    
    /** 分配给用户的SessionKey，通过登陆授权获取。区分大小写 */
    private String     session;
    
    /** 
     * 返回代码。成功/失败标识。
     * 
     * 此属性是后期添加上的(2014-09-26)，所以程序内部并没有对其有业务逻辑，只当做普通属性。
     * 
     * 但与其鲜明对比的是 rc 属性是有内部业务逻辑判断的。 
     */
    private Boolean    result;
    
    /** 返回代码。默认值为成功 */
    private String     rc;
    
    /** 返回信息 */
    private String     ri;
    
    /** 消息体对象 */
    private O          body;
    
    /** 消息流水号。每次访问消息唯一的标识。方便后期问题回溯 */
    private String     serialNo;
    
    /** 原始消息信息 */
    private String     msg;
    
    /** 消息创建的时间 */
    private Date       createTime;
    
    
    
    public AppMessage()
    {
        this.rc = $Succeed;
    }

    
    public String getSysid()
    {
        return sysid;
    }


    public void setSysid(String sysid)
    {
        this.sysid = sysid;
    }

    
    public String getSid()
    {
        return sid;
    }

    
    public void setSid(String sid)
    {
        this.sid = sid;
    }

    
    public String getSidv()
    {
        return sidv;
    }

    
    public void setSidv(String sidv)
    {
        this.sidv = sidv;
    }

    
    public String getSign()
    {
        return sign;
    }

    
    public void setSign(String sign)
    {
        this.sign = sign;
    }

    
    public String getTokenSec()
    {
        return tokenSec;
    }

    
    public void setTokenSec(String tokenSec)
    {
        this.tokenSec = tokenSec;
    }

    
    public String getEncry()
    {
        return encry;
    }

    
    public void setEncry(String encry)
    {
        this.encry = encry;
    }

    
    public String getFormat()
    {
        return format;
    }

    
    public void setFormat(String format)
    {
        this.format = format;
    }

    
    public String getSession()
    {
        return session;
    }

    
    public void setSession(String session)
    {
        this.session = session;
    }

    
    public Boolean getResult()
    {
        return result;
    }

    
    public void setResult(Boolean result)
    {
        this.result = result;
    }


    public String getRc()
    {
        return rc;
    }

    
    public void setRc(String rc)
    {
        this.rc = rc;
    }
    
    
    public String getRi()
    {
        return ri;
    }

    
    public void setRi(String ri)
    {
        this.ri = ri;
    }


    public O getBody()
    {
        return body;
    }

    
    public void setBody(O body)
    {
        this.body = body;
    }
    
    
    public String getSerialNo()
    {
        return serialNo;
    }


    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }


    /**
     * 有意不使用 getter，就是不为外界访问
     * 
     * @return
     */
    public String gatMsg()
    {
        return msg;
    }


    /**
     * 有意不使用 setter，就是不为外界访问
     * 
     * @param msg
     */
    public void satMsg(String msg)
    {
        this.msg = msg;
    }


    /**
     * 有意不使用 getter，就是不为外界访问
     * 
     * @return
     */
    public Date gatCreateTime()
    {
        return createTime;
    }


    /**
     * 有意不使用 setter，就是不为外界访问
     * 
     * @return
     */
    public void satCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }


    public String bodytoString()
    {
        StringBuilder v_Buffer = new StringBuilder();
        
        try
        {
            if ( this.body instanceof List )
            {
                List<?> v_List = (List<?>)this.body;
                for (int i=0; i<v_List.size(); i++)
                {
                    v_Buffer.append(v_List.get(i).toString());
                }
            }
            else
            {
                if ( this.body != null )
                {
                    v_Buffer.append(this.body.toString());
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Buffer.toString();
    }
    
    
    /**
     * 不克隆 Body 属性
     */
    public final AppMessage<Object> clone()
    {
        try
        {
            super.clone();
        }
        catch (CloneNotSupportedException exce)
        {
            $Logger.error(exce);
        }
        
        AppMessage<Object> v_Clone = new AppMessage<Object>();
        
        v_Clone.setSysid(     this.getSysid());
        v_Clone.setSid(       this.getSid());
        v_Clone.setSidv(      this.getSidv());
        v_Clone.setSign(      this.getSign());
        v_Clone.setTokenSec(  this.getTokenSec());
        v_Clone.setEncry(     this.getEncry());
        v_Clone.setFormat(    this.getFormat());
        v_Clone.setSession(   this.getSession());
        v_Clone.setResult(    this.getResult());
        v_Clone.setRc(        this.getRc());
        v_Clone.setRi(        this.getRi());
        v_Clone.setSerialNo(  this.getSerialNo());
        v_Clone.satMsg(       this.gatMsg());
        v_Clone.satCreateTime(this.gatCreateTime());
        
        return v_Clone;
    }
    
    
    public AppMessage<Object> cloneAll()
    {
        AppMessage<Object> v_Clone = this.clone();
        
        v_Clone.setBody(this.getBody());
        
        return v_Clone;
    }
    

    public String toString()
    {
        return this.toString(new XJSON());
    }
    
    
    public String toString(final XJSON i_XJSON)
    {
        try
        {
            this.sign = Help.NVL(AppInterfaces.getEncrypt(this) ,this.sign);
            return i_XJSON.parser(this).toJSONString();
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }

        return "";
    }
    
}
