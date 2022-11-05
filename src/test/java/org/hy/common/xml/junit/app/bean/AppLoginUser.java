package org.hy.common.xml.junit.app.bean;

import org.hy.common.Date;
import org.hy.common.xml.junit.app.common.BaseModelApp;





/**
 * 登录用户
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-10-23
 * @version     v1.0
 */
public class AppLoginUser extends BaseModelApp
{
    
    private static final long serialVersionUID = -4171657725810209923L;

    
    
    /** 登录类型 */
    private String loginType;
    
    /** 登录账号 */
    private String loginName;
    
    /** 登录密码 */
    private String loginPassword;
    
    /** 登录时间 */
    private Date   loginTime;

    
    
    /**
     * 获取：登录类型
     */
    public String getLoginType()
    {
        return loginType;
    }
    

    
    /**
     * 设置：登录类型
     * 
     * @param loginType 
     */
    public void setLoginType(String loginType)
    {
        this.loginType = loginType;
    }
    

    
    /**
     * 获取：登录账号
     */
    public String getLoginName()
    {
        return loginName;
    }
    

    
    /**
     * 设置：登录账号
     * 
     * @param loginName 
     */
    public void setLoginName(String loginName)
    {
        this.loginName = loginName;
    }
    

    
    /**
     * 获取：登录密码
     */
    public String getLoginPassword()
    {
        return loginPassword;
    }
    

    
    /**
     * 设置：登录密码
     * 
     * @param loginPassword 
     */
    public void setLoginPassword(String loginPassword)
    {
        this.loginPassword = loginPassword;
    }
    

    
    /**
     * 获取：登录时间
     */
    public Date getLoginTime()
    {
        return loginTime;
    }
    

    
    /**
     * 设置：登录时间
     * 
     * @param loginTime 
     */
    public void setLoginTime(Date loginTime)
    {
        this.loginTime = loginTime;
    }
    
}
