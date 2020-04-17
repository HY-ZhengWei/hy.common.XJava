package org.hy.common.xml.plugins.analyse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.StringHelp;
import org.hy.common.xml.XJava;





/**
 * 分析中心
 * 
 *  配置web.xml如下代码
    <servlet>
        <servlet-name>AnalysesServlet</servlet-name>
        <servlet-class>org.hy.common.xml.plugins.analyse.AnalysesServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>AnalysesServlet</servlet-name>
        <url-pattern>/analyses/*</url-pattern>
    </servlet-mapping>
 *
 *  提议做此页面的人为：邹德福
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-08-01
 * @version     v1.0
 *              v2.0  2019-07-05  添加：从列表改为云桌面的样式
 */
public class AnalysesServlet extends HttpServlet
{

    private static final long serialVersionUID = -1594698544076309942L;

    private AnalyseBase       analyse;
    
    
    
    public AnalysesServlet()
    {
        super();
        
        this.analyse = (AnalyseBase)XJava.getObject("AnalyseBase");
        if ( this.analyse == null )
        {
            this.analyse = new AnalyseBase();
        }
    }
    
    
    
    public void doGet(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        i_Response.setContentType("text/html;charset=UTF-8");
        
        String v_BasePath   = i_Request.getScheme() + "://" + i_Request.getServerName() + ":" + i_Request.getServerPort() + i_Request.getContextPath();
        String v_RequestURL = i_Request.getRequestURL().toString();
        
        // System.out.println("-- getScheme()        =" + i_Request.getScheme());
        // System.out.println("-- X-Forwarded-Scheme =" + i_Request.getHeader("X-Forwarded-Scheme"));
        
        if ( v_RequestURL.indexOf("analyses/windows/") >= 0 )
        {
            if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".css") )
            {
                i_Response.setContentType("text/css;charset=UTF-8");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".js") )
            {
                i_Response.setContentType("application/x-javascript;charset=UTF-8");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".png" ,".jpg" ,".png" ,".gif" ,".bmp") )
            {
                i_Response.setContentType("image/png");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".svg") )
            {
                i_Response.setContentType("text/xml;charset=UTF-8");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".eot") )
            {
                i_Response.setContentType("application/vnd.ms-fontobject");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".ttf") )
            {
                i_Response.setContentType("application/x-font-ttf");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".woff2") )
            {
                i_Response.setContentType("application/x-font-woff2");
            }
            else if ( StringHelp.isContains(v_RequestURL.toLowerCase() ,".woff") )
            {
                i_Response.setContentType("application/x-font-woff");
            }
            
            i_Response.getWriter().println(this.analyse.getTemplateContent(v_RequestURL.split("analyses/windows/")[1] ,"org.hy.common.xml.plugins.analyse.windows"));
        }
        else
        {
            i_Response.getWriter().println(this.analyse.showWindows(v_BasePath ,v_RequestURL));
        }
    }
    
    
    
    public void doPost(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        this.doGet(i_Request ,i_Response);
    }

}
