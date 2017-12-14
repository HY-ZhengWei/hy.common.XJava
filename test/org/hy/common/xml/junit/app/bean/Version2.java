package org.hy.common.xml.junit.app.bean;

import org.hy.common.xml.junit.app.common.BaseModelApp;





/**
 * 版本信息
 * 
 * @author  ZhengWei(HY)
 * @version 2014-02-20
 */
public class Version2 extends BaseModelApp
{
    private static final long serialVersionUID = 1484114639979807596L;
    
    
    /** 版本号 */
    private String version;
    
    /** 版本信息 */
    private String info;
    
    /** 版本发布日期 */
    private String publicDate;

    
    
    public String getVersion()
    {
        return version;
    }

    
    public void setVersion(String version)
    {
        this.version = version;
    }

    
    public String getInfo()
    {
        return info;
    }

    
    public void setInfo(String info)
    {
        this.info = info;
    }

    
    public String getPublicDate()
    {
        return publicDate;
    }

    
    public void setPublicDate(String publicDate)
    {
        this.publicDate = publicDate;
    }
    
}
