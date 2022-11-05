package org.hy.common.xml.plugins;





/**
 * 网络消息(通用)
 * 
 * 主要用于客户端(如：手机)
 * 
 * @author  ZhengWei(HY)
 * @version 2013-11-17
 */
public class MsgInfo
{
        
    private String i;

    
    
    public MsgInfo()
    {
        this.i = "";
    }
    
    
    public MsgInfo(String i_I)
    {
        this.i = i_I;
    }
    
    
    public String getI()
    {
        return i;
    }

    
    public void setI(String i)
    {
        this.i = i;
    }
    
}
