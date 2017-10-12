package org.hy.common.xml.junit;

import org.hy.common.xml.XHttp;
import org.hy.common.xml.XJava;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.app.Param;





/**
 * 测试Http访问。Post方式的访问
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-11-19
 */
public class JU_XHttp_Post
{
    private String i;
    
    
    
    public JU_XHttp_Post(String i_Info)
    {
        this.i = i_Info;
    }
    
    
    
    public String getI()
    {
        return i;
    }
    

    
    public void setI(String i)
    {
        this.i = i;
    }



    public static void main(String [] args) throws Exception
    {
        XJava.parserXml(JU_XHttp_Post.class.getResource("XHttp-Post-config.xml") ,"XHttp");
        XHttp  v_XHttp = (XHttp)XJava.getObject("XHTTP_User_Logon");
        String v_Info  = ((Param)XJava.getObject("SendInfo")).getValue();
        
        Return<?> v_Response = v_XHttp.request(new JU_XHttp_Post(v_Info));
        
        System.out.println("请求报文: " + v_XHttp.getRequestInfo(new JU_XHttp_Post(v_Info)));
        System.out.println("响应报文: " + v_Response.paramStr);
        System.out.println("响应报文: " + StringHelp.unescape(v_Response.paramStr));
    }
    
}
