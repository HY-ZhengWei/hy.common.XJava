package org.hy.common.xml.plugins.analyse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.Date;
import org.hy.common.Help;
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
 * 功能1：查看前缀匹配的对象列表                http://IP:Port/WebService/../analyseObject?xid=XJavaIDPrefix*
 * 功能2：查看对象信息                          http://IP:Port/WebService/../analyseObject?xid=XJavaID         
 * 功能3：执行对象方法                          http://IP:Port/WebService/../analyseObject?xid=XJavaID&call=方法全名称
 * 功能4：执行对象方法的配置页面（带方法参数） http://IP:Port/WebService/../analyseObject?execute=Y
 * 功能5：集群顺次执行对象方法                  http://IP:Port/WebService/../analyseObject?xid=XJavaID&call=方法全名称&cluster=Y
 * 功能6：集群同时执行对象方法                  http://IP:Port/WebService/../analyseObject?xid=XJavaID&call=方法全名称&cluster=Y&sameTime=Y
 * 
 * 功能7：查看XJava配置文件列表                 http://IP:Port/WebService/../analyseObject
 * 功能8：重新加载XJava配置文件                 http://IP:Port/WebService/../analyseObject?xfile=xxx
 * 功能9：集群重新加载XJava配置文件             http://IP:Port/WebService/../analyseObject?xfile=xxx&cluster=Y
 * 
 * 功能10：查看集群服务列表                     http://IP:Port/WebService/../analyseObject?cluster=Y
 * 
 * 功能11：删除并重建数据库对象                 http://IP:Port/WebService/../analyseObject?XSQLCreate=Y
 * 功能12：查看创建数据库对象列表               http://IP:Port/WebService/../analyseObject?XSQLCreateList=Y
 * 
 * 功能13：本机线程池运行情况                   http://IP:Port/WebService/../analyseObject?ThreadPool=Y
 * 功能14：集群线程池运行情况                   http://IP:Port/WebService/../analyseObject?ThreadPool=Y&cluster=Y
 *
 * 功能15：定时灾备多活集群情况                 http://IP:Port/WebService/../analyseObject?JobDisasterRecoverys=Y
 * 功能16：本机定时任务运行情况                 http://IP:Port/WebService/../analyseObject?Job=Y
 * 
 * 功能17：本机数据库连接池信息                 http://IP:Port/WebService/../analyseObject?DSG=Y
 * 功能18：集群数据库连接池信息                 http://IP:Port/WebService/../analyseObject?DSG=Y&cluster=Y
 * 
 * 功能19：Web文件资源管理器                    http://IP:Port/WebService/../analyseObject?FS=Y
 * 
 * 功能20：查看XSQL组流程图                     http://IP:Port/WebService/../analyseObject?XSGFlow=Y&xid=xxx
 * 
 * 功能21：查看XSQL与表的关系图                 http://IP:Port/WebService/../analyseObject?dsgid=*
 * 功能22：查看表的关系图                       http://IP:Port/WebService/../analyseObject?&tableRef=Ydsgid=*
 * 
 * 功能23：查看日志引擎分析                     http://IP:Port/WebService/../analyseObject?logger=Y
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
 *              v6.0  2018-03-11  添加：Web文件资源管理器（支持集群）
 *              v7.0  2018-07-26  添加：查看创建数据库对象列表
 *              v8.0  2018-09-10  添加：查看XSQL组流程图
 *              v9.0  2019-02-26  添加：定时灾备多活集群情况
 *              v10.0 2019-06-11  添加：查看XSQL与表的关系图
 *              v11.0 2019-06-14  添加：查看表的关系图
 *              v12.0 2020-01-21  添加：执行对象方法的配置页面（带方法参数）
 *              v13.0 2020-06-15  添加：查看日志引擎分析（按类名、按方法、按日志代码行）
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
        
        String v_SessionData = (String)i_Request.getSession().getAttribute($SessionID);
        String v_BasePath    = i_Request.getScheme() + "://" + i_Request.getServerName() + ":" + i_Request.getServerPort() + i_Request.getContextPath();
        String v_RequestURL  = i_Request.getRequestURL().toString();
        
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
                v_SessionData = i_Request.getSession().getId() + "@" + Date.getNowTime().getTime();
                i_Request.getSession().setAttribute($SessionID ,v_SessionData);
            }
        }
        
        String v_XID        = i_Request.getParameter("xid");
        String v_Call       = i_Request.getParameter("call");
        String v_CallParams = i_Request.getParameter("callParams");
        String v_Execute    = i_Request.getParameter("execute");
        String v_XFile      = i_Request.getParameter("xfile");
        String v_Cluster    = i_Request.getParameter("cluster");
        String v_SameTime   = i_Request.getParameter("sameTime");
        String v_Create     = i_Request.getParameter("XSQLCreate");
        String v_CreateList = i_Request.getParameter("XSQLCreateList");
        String v_ThreadPool = i_Request.getParameter("ThreadPool");
        String v_Job        = i_Request.getParameter("Job");
        String v_JobDRs     = i_Request.getParameter("JobDisasterRecoverys");
        String v_DSG        = i_Request.getParameter("DSG");
        String v_DSGID      = i_Request.getParameter("dsgid");
        String v_FS         = i_Request.getParameter("FS");
        String v_XSGFlow    = i_Request.getParameter("XSGFlow");
        String v_Logger     = i_Request.getParameter("logger");
        
        if ( !Help.isNull(v_Execute) )
        {
            i_Response.getWriter().println(this.analyse.showExecuteMethod(v_BasePath ,i_Request.getRequestURL().toString() ,v_XID ,v_Call ,v_CallParams));
        }
        else if ( Help.isNull(v_XID) )
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
                    String v_LIP = Help.NVL(i_Request.getParameter("LIP"));
                    
                    if ( "RELOAD".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.reload(v_FN ,"Y".equalsIgnoreCase(v_HIP)));
                    }
                    else if ( "CLONE-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.cloneFile(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "CLONE-DL".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.cloneFileDownload(v_FPath ,v_FN ,v_HIP ,v_LIP));
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
                    else if ( "CALC-SIZE".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.calcFileSize(v_FPath ,v_FN));
                    }
                    else if ( "CALC-SIZE-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.calcFileSizeCluster(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "DIFF".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.diffFile(v_BasePath ,v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "EXECUTE-COMMAND".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.executeCommand(v_FPath ,v_FN));
                    }
                    else if ( "EXECUTE-COMMAND-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.executeCommandCluster(v_FPath ,v_FN ,v_HIP));
                    }
                    else if ( "SYSTEM-TIME-C".equalsIgnoreCase(v_Action) )
                    {
                        i_Response.getWriter().println(this.analyseFS.getSystemTimeCluster());
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
            else if ( !Help.isNull(v_DSGID) )
            {
                String v_TableRef = Help.NVL(i_Request.getParameter("tableRef"));
                String v_Sort     = Help.NVL(i_Request.getParameter("S"));
                
                if ( Help.isNull(v_TableRef) )
                {
                    i_Response.getWriter().println(this.analyse.showXSQLRefTable (v_BasePath ,i_Request.getRequestURL().toString() ,v_DSGID ,Help.NVL(v_Sort ,"2")));
                }
                else
                {
                    i_Response.getWriter().println(this.analyse.showXSQLTablesRef(v_BasePath ,i_Request.getRequestURL().toString() ,v_DSGID ,Help.NVL(v_Sort ,"2")));
                }
            }
            else if ( !Help.isNull(v_Job) )
            {
                String v_Timer = Help.NVL(i_Request.getParameter("Timer"));
                i_Response.getWriter().println(this.analyse.analyseJob(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster) ,v_Timer));
            }
            else if ( !Help.isNull(v_JobDRs) )
            {
                i_Response.getWriter().println(this.analyse.analyseJobDisasterRecoverys(v_BasePath ,i_Request.getRequestURL().toString()));
            }
            else if ( !Help.isNull(v_ThreadPool) )
            {
                i_Response.getWriter().println(this.analyse.analyseThreadPool(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster)));
            }
            else if ( !Help.isNull(v_CreateList) )
            {
                i_Response.getWriter().println(this.analyse.showXSQLCreateList(v_BasePath ,i_Request.getRequestURL().toString()));
            }
            else if ( !Help.isNull(v_Create) )
            {
                i_Response.getWriter().println(this.analyse.analyseDBCreate(v_BasePath ,i_Request.getRequestURL().toString()));
            }
            else if ( !Help.isNull(v_Logger) )
            {
                String v_TotalType       = Help.NVL(i_Request.getParameter("TT"));
                String v_Sort            = Help.NVL(i_Request.getParameter("S"));
                String v_FilterClassName = Help.NVL(i_Request.getParameter("FCN"));
                String v_Timer           = Help.NVL(i_Request.getParameter("Timer"));
                String v_ShowEveryOne    = Help.NVL(i_Request.getParameter("ShowEveryOne"));
                
                i_Response.getWriter().println(this.analyse.analyseLogger(v_BasePath 
                                                                         ,i_Request.getRequestURL().toString() 
                                                                         ,"Y".equalsIgnoreCase(v_Cluster) 
                                                                         ,"Y".equalsIgnoreCase(v_ShowEveryOne) 
                                                                         ,v_TotalType 
                                                                         ,v_Sort
                                                                         ,v_FilterClassName 
                                                                         ,v_Timer));
            }
            else if ( Help.isNull(v_XFile) && "Y".equalsIgnoreCase(v_Cluster) )
            {
                String v_SysTime = i_Request.getParameter("SysTime");
                String v_Timer   = Help.NVL(i_Request.getParameter("Timer"));
                
                i_Response.getWriter().println(this.analyse.analyseCluster(v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_SysTime) ,v_Timer));
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
        else if ( !Help.isNull(v_XSGFlow) && "Y".equalsIgnoreCase(v_XSGFlow) )
        {
            i_Response.getWriter().println(this.analyse.showXSQLGroupFlow(v_BasePath ,i_Request.getRequestURL().toString() ,v_XID));
        }
        else
        {
            i_Response.getWriter().println(this.analyse.analyseObject(v_BasePath ,i_Request.getRequestURL().toString() ,v_XID ,v_Call ,v_CallParams ,"Y".equalsIgnoreCase(v_Cluster) ,"Y".equalsIgnoreCase(v_SameTime)));
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
