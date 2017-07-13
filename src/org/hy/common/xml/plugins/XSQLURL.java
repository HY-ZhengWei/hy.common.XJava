package org.hy.common.xml.plugins;

import java.util.ArrayList;
import java.util.List;





/**
 * XSQL与访问URL的执行关系
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-13
 * @version     v1.0
 */
public class XSQLURL
{
    
    /** 访问的URL */
    private String       url;
    
    /** 访问的URL对应执行的SQL */
    private List<String> sqls;

    
    
    public XSQLURL()
    {
        
    }
    
    
    
    /**
     * 获取：访问的URL
     */
    public String getUrl()
    {
        return url;
    }
    

    
    /**
     * 设置：访问的URL
     * 
     * @param url 
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
    

    
    /**
     * 获取：访问的URL对应执行的SQL
     */
    public List<String> getSqls()
    {
        return sqls;
    }
    

    
    /**
     * 设置：访问的URL对应执行的SQL
     * 
     * @param sqls 
     */
    public void setSqls(List<String> sqls)
    {
        this.sqls = sqls;
    }
    
}
