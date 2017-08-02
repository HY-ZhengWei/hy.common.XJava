package org.hy.common.xml.plugins.analyse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        <url-pattern>/analyses</url-pattern>
    </servlet-mapping>
 *
 *  提议做此页面的人为：邹德福
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-08-01
 * @version     v1.0
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
        
        i_Response.getWriter().println(this.analyse.showCatalogue(v_BasePath ,v_RequestURL));
    }
    
    
    
    public void doPost(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        this.doGet(i_Request ,i_Response);
    }

}
