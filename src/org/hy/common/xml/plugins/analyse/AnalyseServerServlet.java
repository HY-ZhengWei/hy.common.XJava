package org.hy.common.xml.plugins.analyse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.Help;





/**
 * 获取数据库访问量的概要统计数据(Servlet实现方式)
 * 
 * 功能1：获取数据库组合SQL访问量的概要统计数据      http://IP:Port/WebService/../analyseDB?type=Group
 * 功能2：获取集群数据库组合SQL访问量的概要统计数据   http://IP:Port/WebService/../analyseDB?type=Group&cluster=Y
 * 
 * 功能3：获取数据库访问量的概要统计数据             http://IP:Port/WebService/../analyseDB
 * 功能4：获取集群数据库访问量的概要统计数据         http://IP:Port/WebService/../analyseDB?cluster=Y
 * 
 * 功能5：查看XSQL对象执行错误的SQL语句            http://IP:Port/WebService/../analyseDB?xsqloid=xxxxx&xsqlxid=xxxxx
 *
 *  配置web.xml如下代码
    <servlet>
        <servlet-name>AnalyseServerServlet</servlet-name>
        <servlet-class>org.hy.common.xml.plugins.analyse.AnalyseServerServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>AnalyseServerServlet</servlet-name>
        <url-pattern>/analyses/analyseDB</url-pattern>
    </servlet-mapping>
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-12-15
 * @version     v1.0
 *              v2.0  2017-01-04  添加：查看XSQL对象执行错误的SQL语句
 *              v3.0  2017-01-22  添加：查看集群数据库访问量的概要统计数据
 *                                添加：查看集群数据库组合SQL访问量的概要统计数据 
 *                                添加：查看集群查看XSQL对象执行错误的SQL语句
 *              4.0  2018-02-11   添加： 删除并重新创建数据库对象
 */
public class AnalyseServerServlet extends HttpServlet
{

    private static final long serialVersionUID = -6165884390221056380L;
    
    private AnalyseBase analyse;
    
    
    
    public AnalyseServerServlet()
    {
        super();
        this.analyse = new AnalyseBase();
    }
    
    
    
    public void doGet(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        String v_BasePath = i_Request.getScheme()+"://" + i_Request.getServerName() + ":" + i_Request.getServerPort() + i_Request.getContextPath();
        
        i_Response.setContentType("text/html;charset=UTF-8");
        
        String v_Type    = i_Request.getParameter("type");
        String v_XSQLOID = i_Request.getParameter("xsqloid");
        String v_XSQLXID = i_Request.getParameter("xsqlxid");
        String v_Cluster = i_Request.getParameter("cluster");
        
        if ( !Help.isNull(v_XSQLOID) && !Help.isNull(v_XSQLXID) )
        {
            i_Response.getWriter().println(this.analyse.analyseDBError (v_BasePath ,i_Request.getRequestURL().toString() ,v_XSQLOID ,v_XSQLXID ,"Y".equalsIgnoreCase(v_Cluster)));
        }
        else if ( "GROUP".equalsIgnoreCase(v_Type) )
        {
            i_Response.getWriter().println(this.analyse.analyseDBGroup (v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster)));
        }
        else if ( "CREATE".equalsIgnoreCase(v_Type) )
        {
            i_Response.getWriter().println(this.analyse.analyseDBCreate(v_BasePath ,i_Request.getRequestURL().toString()));
        }
        else
        {
            i_Response.getWriter().println(this.analyse.analyseDB      (v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster)));
        }
    }
    
    
    
    public void doPost(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        this.doGet(i_Request ,i_Response);
    }
    
}
