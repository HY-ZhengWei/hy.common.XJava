package org.hy.common.xml.plugins.analyse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hy.common.Date;
import org.hy.common.ExpireMap;
import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;
import org.hy.common.xml.log.Logger;





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
        <url-pattern>/analyses/analyseObject/*</url-pattern>
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
 *              v14.0 2021-05-29  添加：图形码验证登录的功能
 *                                合并：analyseDB数据库分析功能
 * 
 */
public class AnalyseObjectServlet extends HttpServlet
{

    private static final long   serialVersionUID = -6165884390221056380L;
    
    
    
    private static final Logger               $Logger       = Logger.getLogger(AnalyseBase.class);
    
    /** 登陆的Session会话ID标识，标识着是否登陆成功 */
    public  static final String               $SessionID    = "$XJAVA$";
    
    /** 登录验证码的超时时长（单位：秒） */
    private static final int                  $LoginTimeout       = 30;
    
    /** 登录失败，锁会话的时长（单位：分钟） */
    private static final int                  $LockSessionTimeLen = 10;
    
    /** 登录失败，锁全局的时长（单位：分钟） */
    private static final int                  $LockGlobalTimeLen  = 30;
    
    /** 登录失败次数Map.key 为Session会话ID，Map.value为登录失败次数 */
    private static ExpireMap<String ,Integer> $LoginCounts  = null;
    
    /** 登录验证码Map.key 为Session会话ID，Map.value为验证码 */
    private static ExpireMap<String ,String>  $PasswdCheck  = null;
    
    
    
    
    private CheckImageUtil checkImageUtil;
    
    private AnalyseBase    analyse;
    
    private AnalyseFS      analyseFS;
    
    
    
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
        this.checkImageUtil = new CheckImageUtil();
    }
    
    
    
    /**
     * 获取登录失败次数对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-05-25
     * @version     v1.0
     * 
     * @return
     */
    private synchronized ExpireMap<String ,Integer> getLoginCounts()
    {
        if ( $LoginCounts == null )
        {
            $LoginCounts = new ExpireMap<String ,Integer>();
        }
        
        return $LoginCounts;
    }
    
    
    
    /**
     * 登录验证码Map.key 为Session会话ID，Map.value为验证码
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-05-26
     * @version     v1.0
     * 
     * @return
     */
    private synchronized ExpireMap<String ,String> getPasswdCheck()
    {
        if ( $PasswdCheck == null )
        {
            $PasswdCheck = new ExpireMap<String ,String>();
        }
        
        return $PasswdCheck;
    }
    
    
    
    /**
     * 生成图片验证信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-05-20
     * @version     v1.0
     * 
     * @param i_Request
     * @return           返回 Return 对象的 Json字符串
     */
    private String getCheckImage(HttpServletRequest i_Request)
    {
        String         v_SessionID = i_Request.getSession().getId();
        Return<String> v_Ret       = new Return<String>(true);
        
        try
        {
            int v_X = Help.random(70 ,385 - 70);
            int v_Y = Help.random(70 ,378 - 70);
            String [] v_ImageDatas = checkImageUtil.makeCheckImage(v_X ,v_Y);
            
            v_Ret.setParamStr(v_ImageDatas[0]);
            v_Ret.setParamObj(v_ImageDatas[1]);
            v_Ret.setParamInt(v_Y);
            
            getPasswdCheck().put(v_SessionID ,v_Y + "" + v_X ,$LoginTimeout);
        }
        catch (Exception exce)
        {
            $Logger.error("图片验证码" ,exce);
            v_Ret.set(false);
        }
        
        String v_RetJson = "{}";
        try
        {
            XJSON v_XJson = new XJSON();
            
            v_RetJson = v_XJson.toJson(v_Ret).toJSONString();
        }
        catch (Exception exce)
        {
            $Logger.error("图片验证码" ,exce);
        }
        
        return v_RetJson;
    }
    
    
    
    @Override
    public void doGet(HttpServletRequest i_Request, HttpServletResponse i_Response) throws ServletException, IOException
    {
        String v_BasePath    = i_Request.getScheme() + "://" + i_Request.getServerName() + ":" + i_Request.getServerPort() + i_Request.getContextPath();
        String v_RequestPage = i_Request.getRequestURL().toString();
        String v_RequestURL  = v_RequestPage;
        
        if ( v_RequestURL.indexOf("/windows/") >= 0 )
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
                i_Response.setContentType("image/*");
                i_Response.getOutputStream().write(this.analyse.getTemplateContentBytes(v_RequestURL.split("/windows/")[1] ,"org.hy.common.xml.plugins.analyse.windows"));
                return;
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
            
            i_Response.getWriter().println(this.analyse.getTemplateContent(v_RequestURL.split("/windows/")[1] ,"org.hy.common.xml.plugins.analyse.windows"));
            return;
        }
        
        
        // 上面是图片、js、css等资源处理
        // 下面是页面内容处理
        
        
        i_Response.setContentType("text/html;charset=UTF-8");
        String v_SessionData = (String)i_Request.getSession().getAttribute($SessionID);
        
        if ( !Help.isNull(i_Request.getQueryString()) )
        {
            v_RequestURL += "?" + i_Request.getQueryString();
        }
        
        // 验证登录
        if ( null == v_SessionData )
        {
            i_Response.setHeader("Cache-Control"      ,"no-cache, no-store, must-revalidate");
            i_Response.setHeader("This-Header-Is-Set" ,"no-cache, no-store, must-revalidate");
            i_Response.setHeader("Expires"            ,"0");
            
            
            // 获取图片验证码
            if ( v_RequestURL.indexOf("/getCheckImage.page") > 0 )
            {
                i_Response.setContentType("application/json;charset=UTF-8");
                i_Response.getWriter().println(getCheckImage(i_Request));
                return;
            }
            
            Integer v_AllLCount = getLoginCounts().get($SessionID);
            if ( v_AllLCount != null && v_AllLCount >= 3 )
            {
                // 全域被锁定中的提示
                i_Response.getWriter().println(this.analyse.login(v_RequestURL ,v_BasePath ,v_RequestPage ,"ERR92"));
                return;
            }
            
            String  v_SID    = i_Request.getSession().getId();
            Integer v_LCount = getLoginCounts().get(v_SID);
            if ( v_LCount != null && v_LCount >= 3 )
            {
                // 账户被锁定中的提示
                i_Response.getWriter().println(this.analyse.login(v_RequestURL ,v_BasePath ,v_RequestPage ,"ERR02"));
                return;
            }
            
            String v_Password = i_Request.getParameter("password");
            String v_CheckPwd = i_Request.getParameter("checkPWD");
            String v_CachePwd = getPasswdCheck().remove(v_SID);     // 密码只用一次尝试的机会。用完就立刻删除
            if ( Help.isNull(v_Password) || !Help.isNumber(v_CheckPwd) || !Help.isNumber(v_CachePwd) )
            {
                i_Response.getWriter().println(this.analyse.login(v_RequestURL ,v_BasePath ,v_RequestPage ,""));
                return;
            }
            
            // 验证图片验证码和密码
            long v_CheckPwdValue = Long.parseLong(v_CheckPwd);
            long v_CachePwdValue = Long.parseLong(v_CachePwd);
            if ( Math.abs(v_CheckPwdValue - v_CachePwdValue) > 5
              || !this.getPassword().equals(StringHelp.md5(v_Password ,StringHelp.$MD5_Type_Hex)) )
            {
                if ( v_LCount == null || v_LCount <= 0 )
                {
                    v_LCount = 1;
                    getLoginCounts().put(v_SID ,v_LCount ,60 * $LockSessionTimeLen);
                }
                else if ( v_LCount < 3 )
                {
                    getLoginCounts().put(v_SID ,++v_LCount ,60 * $LockSessionTimeLen);
                }
                
                if ( v_LCount >= 3 )
                {
                    if ( v_AllLCount == null ||v_AllLCount <= 0 )
                    {
                        v_AllLCount = 1;
                        getLoginCounts().put($SessionID ,v_AllLCount ,60 * $LockGlobalTimeLen);
                    }
                    else if ( v_AllLCount < 3 )
                    {
                        getLoginCounts().put($SessionID ,++v_AllLCount ,60 * $LockGlobalTimeLen);
                    }
                    
                    if ( v_AllLCount >= 3 )
                    {
                        // 全域锁定的提示
                        i_Response.getWriter().println(this.analyse.login(v_RequestURL ,v_BasePath ,v_RequestPage ,"ERR91"));
                        $Logger.info("分析中心被多次尝试登录，全域已锁定");
                    }
                    else
                    {
                        // 账户锁定的提示
                        i_Response.getWriter().println(this.analyse.login(v_RequestURL ,v_BasePath ,v_RequestPage ,"ERR01"));
                        $Logger.info("分析中心被多次尝试登录，账户已锁定");
                    }
                }
                else
                {
                    i_Response.getWriter().println(this.analyse.login(v_RequestURL ,v_BasePath ,v_RequestPage ,""));
                }
                return;
            }
            // 登陆成功
            else
            {
                v_SessionData = i_Request.getSession().getId() + "@" + Date.getNowTime().getTime();
                i_Request.getSession().setAttribute($SessionID ,v_SessionData);
            }
        }
        
        
        
        // 2021-05-29  合并analyseDB的数据分析功能
        if ( v_RequestURL.indexOf("/analyseDB") > 0 )
        {
            String v_Type    = i_Request.getParameter("type");
            String v_XSQLXID = i_Request.getParameter("xsqlxid");
            String v_Cluster = i_Request.getParameter("cluster");
            String v_Sort    = Help.NVL(i_Request.getParameter("S"));
            String v_Scope   = Help.NVL(i_Request.getParameter("scope"));
            String v_Timer   = Help.NVL(i_Request.getParameter("Timer"));
            
            if ( !Help.isNull(v_XSQLXID) )
            {
                i_Response.getWriter().println(this.analyse.analyseDBError (v_BasePath ,i_Request.getRequestURL().toString() ,v_XSQLXID ,"Y".equalsIgnoreCase(v_Cluster)));
            }
            else if ( "GROUP".equalsIgnoreCase(v_Type) )
            {
                i_Response.getWriter().println(this.analyse.analyseDBGroup (v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster) ,v_Sort ,"Y".equalsIgnoreCase(v_Scope) ,v_Timer));
            }
            else
            {
                i_Response.getWriter().println(this.analyse.analyseDB      (v_BasePath ,i_Request.getRequestURL().toString() ,"Y".equalsIgnoreCase(v_Cluster) ,v_Sort ,"Y".equalsIgnoreCase(v_Scope) ,v_Timer));
            }
            
            return;
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
                String v_ClassName = Help.NVL(i_Request.getParameter("cn"));
                if ( Help.isNull(v_ClassName) )
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
                else
                {
                    String v_MethodName = Help.NVL(i_Request.getParameter("mn"));
                    String v_LineNumber = Help.NVL(i_Request.getParameter("ln"));
                    String v_LogLevel   = Help.NVL(i_Request.getParameter("level"));
                    
                    i_Response.getWriter().println(this.analyse.analyseLoggerException(v_BasePath
                                                                                      ,i_Request.getRequestURL().toString()
                                                                                      ,"Y".equalsIgnoreCase(v_Cluster)
                                                                                      ,v_ClassName
                                                                                      ,v_MethodName
                                                                                      ,v_LineNumber
                                                                                      ,v_LogLevel));
                }
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
    
    
    
    @Override
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
