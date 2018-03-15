package org.hy.common.xml.plugins.analyse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.xml.XJava;





/**
 * 查看对象信息(Servlet实现方式)
 * 
 * 有登录验证功能。默认密码为xjava，但可以配置web.xml方式修改。如下代码
    <servlet>
        <servlet-name>AnalyseObjectServlet</servlet-name>
        <servlet-class>org.hy.common.xml.plugins.analyse.AnalyseObjectServlet</servlet-class>
        <init-param>
            <param-name>password</param-name>
            <param-value>1EFC6E30FDAFA34EE39A8E20CCED595F</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>AnalyseObjectServlet</servlet-name>
        <url-pattern>/analyses/analyseObject</url-pattern>
    </servlet-mapping>
 * 
 * 功能1：查看前缀匹配的对象列表         http://IP:Port/WebService/../analyseObject?xid=XJavaIDPrefix*
 * 功能2：查看对象信息                         http://IP:Port/WebService/../analyseObject?xid=XJavaID         
 * 功能3：执行对象方法                          http://IP:Port/WebService/../analyseObject?xid=XJavaID&call=方法全名称
 * 功能4：集群顺次执行对象方法            http://IP:Port/WebService/../analyseObject?xid=XJavaID&call=方法全名称&cluster=Y
 * 功能5：集群同时执行对象方法            http://IP:Port/WebService/../analyseObject?xid=XJavaID&call=方法全名称&cluster=Y&sameTime=Y
 * 
 * 功能6：查看XJava配置文件列表         http://IP:Port/WebService/../analyseObject
 * 功能7：重新加载XJava配置文件         http://IP:Port/WebService/../analyseObject?xfile=xxx
 * 功能8：集群重新加载XJava配置文件  http://IP:Port/WebService/../analyseObject?xfile=xxx&cluster=Y
 * 
 * 功能9：查看集群服务列表                   http://IP:Port/WebService/../analyseObject?cluster=Y
 * 
 * 功能10：删除并重建数据库对象          http://IP:Port/WebService/../analyseObject?XSQLCreate=Y
 * 
 * 功能11：本机线程池运行情况             http://IP:Port/WebService/../analyseObject?ThreadPool=Y
 * 功能12：集群线程池运行情况             http://IP:Port/WebService/../analyseObject?ThreadPool=Y&cluster=Y
 * 
 * 功能13：本机定时任务运行情况          http://IP:Port/WebService/../analyseObject?Job=Y
 * 
 * 功能15：本机线程池运行情况             http://IP:Port/WebService/../analyseObject?DSG=Y
 * 功能16：集群线程池运行情况             http://IP:Port/WebService/../analyseObject?DSG=Y&cluster=Y
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-12-16
 * @version     v1.0
 *              v2.0  2017-01-17  添加：集群重新加载XJava配置文件
 *                                添加：集群顺次执行对象方法
 *                                添加：集群同时执行对象方法（并发）
 *                                添加：查看集群服务列表
 *              v3.0  2018-02-11  添加：删除并重建数据库对象
 *              v4.0  2018-02-27  添加：本机线程池运行情况
 *                                添加：集群线程池运行情况 
 *              v5.0  2018-02-28  添加：本机定时任务运行情况。之前合并在 "查看前缀匹配的对象列表" 任务中
 *              
 */
public class AnalyseObjectServlet extends HttpServlet
{

    private static final long   serialVersionUID = -6165884390221056380L;
    
    /** 登陆的Session会话ID标识，标识着是否登陆成功 */
    public  static final String $SessionID       = "$XJAVA$";
    
    private AnalyseBase         analyse;
    
    private AnalyseFS           analyseFS;
    
    
    
    public AnalyseObjectServlet()
    {
        super();
        
        this.analyse   = (AnalyseBase)XJava.getObject("AnalyseBase");
        this.analyseFS = (AnalyseFS)  XJava.getObject("AnalyseFS");
        if ( this.analyse == null )
        {
            this.analyse = new AnalyseBase();
        }
        if ( this.analyseFS == null )
        {
            this.analyseFS = new AnalyseFS();
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        i_Response.setContentType("text/html;charset=UTF-8");
        
        Return<Date> v_SessionData = (Return<Date>)i_Request.getSession().getAttribute($SessionID);
        String       v_BasePath    = i_Request.getScheme() + "://" + i_Request.getServerName() + ":" + i_Request.getServerPort() + i_Request.getContextPath();
        String       v_RequestURL  = i_Request.getRequestURL().toString();
        
        if ( !Help.isNull(i_Request.getQueryString()) )
        {
            v_RequestURL += "?" + i_Request.getQueryString();
        }
        
        // 验证登录
        if ( null == v_SessionData )
        {
            String v_Password = i_Request.getParameter("password");
            
            if ( Help.isNull(v_Password) || !this.getPassword().equals(StringHelp.md5(v_Password ,StringHelp.$MD5_Type_Hex)) )
            {
                i_Response.getWriter().println(this.analyse.login(v_RequestURL));
                return;
            }
            // 登陆成功
            else
            {
                v_SessionData = new Return<Date>();
                v_SessionData.paramObj = new Date();
                v_SessionData.paramStr = i_Request.getSession().getId();
                
                i_Request.getSession().setAttribute($SessionID ,v_SessionData);
            }
        }
        
        String v_XID        = i_Request.getParameter("xid");
        String v_Call       = i_Request.getParameter("call");
        String v_XFile      = i_Request.getParameter("xfile");
        String v_Cluster    = i_Request.getParameter("cluster");
        String v_SameTime   = i_Request.getParameter("sameTime");
        String v_Create     = i_Request.getParameter("XSQLCreate");
        String v_ThreadPool = i_Request.getParameter("ThreadPool");
        String v_Job        = i_Request.getParameter("Job");
        String v_DSG        = i_Request.getParameter("DSG");
        String v_FS         = i_Request.getParameter("FS");
        
        if ( Help.isNull(v_XID) )
        {
            if ( !Help.isNull(v_FS) )
            {
                String v_Sort   = Help.NVL(i_Request.getParameter("S"));
                String v_FPath  = StringHelp.replaceAll(Help.NVL(i_Request.getParameter("FP") ,AnalyseFS.$WebHome) ,"\\" ,"/");
                String v_Action = Help.NVL(i_Request.getParameter("Action"));
                
                if ( !Help.isNull(v_Action) )
                {
                    String v_FN  = Help.NVL(i_Request.getParameter("FN"));
                    String v_HIP = Help.NVL(i_Request.getParameter("HIP"));
                    
                    if ( "CLONE-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.cloneFile(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "DEL".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.delFile(v_FPath ,v_FN));
                    }
                    else if ( "DEL-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.delFileByCluster(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "ZIP".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.zipFile(v_FPath ,v_FN ,Date.getNowTime().getFullMilli_ID()));
                    }
                    else if ( "ZIP-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.zipFileByCluster(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "UNZIP".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.unZipFile(v_FPath ,v_FN));
                    }
                    else if ( "UNZIP-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.unZipFileByCluster(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "CALC-SIZE".equals(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.calcFileSize(v_FPath ,v_FN));
                    }
                }
                else
                {
                    i_Response.getWriter().println(this.analyseFS.analysePath(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster) ,v_FPath ,v_Sort));
                }
            }
            else if ( !Help.isNull(v_DSG) )
            {
                i_Response.getWriter().println(this.analyse.analyseDataSourceGroup(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster)));
            }
            else if ( !Help.isNull(v_Job) )
            {
                i_Response.getWriter().println(this.analyse.analyseJob(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster)));
            }
            else if ( !Help.isNull(v_ThreadPool) )
            {
                i_Response.getWriter().println(this.analyse.analyseThreadPool(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster)));
            }
            else if ( !Help.isNull(v_Create) )
            {
                i_Response.getWriter().println(this.analyse.analyseDBCreate(v_BasePath ,i_Request.getRequestURL().toString()));
            }
            else if ( Help.isNull(v_XFile) && "Y".equalsIgnoreCase(v_Cluster) )
            {
                i_Response.getWriter().println(this.analyse.analyseCluster(v_BasePath ,i_Request.getRequestURL().toString()));
            }
            else
            {
                i_Response.getWriter().println(this.analyse.analyseXFile(v_BasePath ,i_Request.getRequestURL().toString() ,v_XFile ,"Y".equalsIgnoreCase(v_Cluster)));
            }
        }
        else if ( v_XID.endsWith("*") )
        {
            v_XID = StringHelp.replaceAll(v_XID ,"*" ,"");
            i_Response.getWriter().println(this.analyse.analyseObjects(v_BasePath ,i_Request.getRequestURL().toString() ,v_XID));
        }
        else
        {
            i_Response.getWriter().println(this.analyse.analyseObject(v_BasePath ,i_Request.getRequestURL().toString() ,v_XID ,v_Call ,"Y".equalsIgnoreCase(v_Cluster) ,"Y".equalsIgnoreCase(v_SameTime)));
        }
    }
    
    
    
    public void doPost(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException 
    {
        this.doGet(i_Request ,i_Response);
    }


    
    /**
     * 获取：访问密码
     */
    public String getPassword()
    {
        // 默认密码为xjava
        return Help.NVL(this.getInitParameter("password") ,"1EFC6E30FDAFA34EE39A8E20CCED595F");
    }
    
}
