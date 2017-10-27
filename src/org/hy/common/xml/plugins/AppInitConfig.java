package org.hy.common.xml.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.xml.XJava;
import org.hy.common.Help;
import org.hy.common.app.Param;
import org.hy.common.file.FileHelp;





/**
 * 初始化信息
 * 
 * Web应用及桌面应用程序的通用初始化加载XJava相关配置文件的初始化类
 * 
 * @author      ZhengWei(HY)
 * @createDate  2014-08-07
 * @version     v1.0  
 *              v2.0  2016-01-04  将所有初始化过的XJava配置文件都保存在XJava对象池中。
 *                                保存类型为LinkedHashMap，map.key为配置文件的名称，map.value为AppInitConfig的实例对象
 *              v3.0  2016-12-26  支持对目录（包含子目录）下的所有配置文件都遍历加载的功能。
 *              v3.1  2017-10-27  修复：classpath:解释时的入参为空的问题。
 */
public class AppInitConfig
{
    
    /** 保存所有初始化过的XJava配置文件的XID标记 */
    public static final String $XFileNames_XID = "XFILENAMES";
    
    
    /** 日志类型 */
    public enum LogType
    {
        /** 加载日志 */
        $Loading
        
        /** 错误日志 */
       ,$Error
       
        /** 完成日志 */
       ,$Finish
    }
    
    
    /** 文件编码 */
    private String  enCode;
    
    /** 
     * XML文件的类路径 
     * 
     * 格式为：/com/xxx/yyy/
     */
    private String  xmlClassPath;
    
    /**
     * XJava功能中的xmlClassPath属性，表示解译xml文件的URL的路径（父目录路径） 
     * 
     * 格式为：com.xxx.yyy.
     */
    private String  xjavaXmlClassPath;
    
    /** 是否打印默认日志 */
    private boolean isLog;
    
    
    
    public AppInitConfig()
    {
        this(true);
    }
    
    
    
    public AppInitConfig(String i_EnCode)
    {
        this(true ,i_EnCode);
    }
    
    
    
    public AppInitConfig(boolean i_IsLog)
    {
        this(i_IsLog ,"UTF-8");
    }
    
    
    
    public AppInitConfig(boolean i_IsLog ,String i_EnCode)
    {
        this.enCode = i_EnCode;
        this.isLog  = i_IsLog;
        this.setXmlClassPath(this.getClass());
    }
    
    
    
    public synchronized void setInit(List<Param> i_Params)
    {
        this.init(i_Params);
    }
    
    
    
    public synchronized void setInit(String i_PackageName)
    {
        this.init(i_PackageName);
    }
    
    
    
    public synchronized void init(List<Param> i_Params)
    {
        this.init(null ,i_Params ,null);
    }
    
    
    
    /**
     * 初始配置。主要用于配置文件与*.class一同存放的情况
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-05-10
     * @version     v1.0
     *
     * @param i_Params  Param.getValue() 为配置文件名称。支持相对路径。如 db/db.Config.xml
     * @param i_XmlRootPath
     */
    public synchronized void init(List<Param> i_Params ,String i_XmlRootPath)
    {
        this.init(null ,i_Params ,i_XmlRootPath); 
    }
    
    
    
    public synchronized void init(String i_XDName ,List<Param> i_Params)
    {
        this.init(null ,i_Params ,null);
    }
    
    
    
    @SuppressWarnings("unchecked")
    public synchronized void init(String i_XDName ,List<Param> i_Params ,String i_XmlRootPath)
    {
        int                 v_Count      = 0;
        int                 v_OK         = 0;
        Param               v_Param      = null;
        String              v_FFullName  = null;
        File                v_FObject    = null;
        Map<String ,Object> v_XFileNames = (Map<String ,Object>)XJava.getObject($XFileNames_XID);
        
        if ( v_XFileNames == null )
        {
            v_XFileNames = new LinkedHashMap<String ,Object>();
            XJava.putObject($XFileNames_XID ,v_XFileNames);
        }
        
        try
        {
            FileHelp v_FileHelp = new FileHelp();
            
            if ( !Help.isNull(i_Params) )
            {
                v_Count = i_Params.size();
                
                if ( Help.isNull(i_XDName) )
                {
                    for (int i=0; i<i_Params.size(); i++)
                    {
                        v_Param     = i_Params.get(i);
                        v_FFullName = (!Help.isNull(i_XmlRootPath) ? i_XmlRootPath : Help.NVL(this.xmlClassPath)) + v_Param.getValue().trim();
                        v_FObject   = new File(v_FFullName);
                        
                        // 遍历目录（包含子目录）下的所有配置文件 2016-12-26
                        if ( v_FObject.isDirectory() ) 
                        {
                            List<Param> v_ChildParams = new ArrayList<Param>();
                            for (File v_ChildFile : v_FObject.listFiles())
                            {
                                if (  v_ChildFile.isHidden()
                                  || (v_ChildFile.isFile() && !v_ChildFile.getName().toLowerCase().endsWith(".xml")) )
                                {
                                    continue;
                                }
                                
                                Param v_ChildParam = new Param();
                                
                                if ( v_Param.getValue().trim().endsWith(Help.getSysPathSeparator()) )
                                {
                                    v_ChildParam.setValue(v_Param.getValue().trim()                              + v_ChildFile.getName());  
                                }
                                else
                                {
                                    v_ChildParam.setValue(v_Param.getValue().trim() + Help.getSysPathSeparator() + v_ChildFile.getName());  
                                }
                                
                                v_ChildParam.setName(v_ChildParam.getValue());
                                
                                v_ChildParams.add(v_ChildParam);
                            }
                            
                            if ( !Help.isNull(v_ChildParams) )
                            {
                                this.init(null ,v_ChildParams ,i_XmlRootPath);
                                v_OK++;
                            }
                        }
                        else
                        {
                            if ( !Help.isNull(i_XmlRootPath) )
                            {
                                // ZhengWei(HY) Edit 2017-10-27 i_XmlRootPath处原先的传值为：""
                                XJava.parserXml(v_FileHelp.getContent(v_FFullName                                      ,this.enCode) ,i_XmlRootPath          ,v_Param.getValue());
                            }
                            else
                            {
                                XJava.parserXml(v_FileHelp.getContent(this.getClass().getResourceAsStream(v_FFullName) ,this.enCode) ,this.xjavaXmlClassPath ,v_Param.getValue());
                            }
                            
                            v_OK++;
                            v_XFileNames.put(v_Param.getValue() ,this);
                            
                            if ( this.isLog )
                            {
                                log(v_Param ,LogType.$Loading);
                            }
                        }
                    }
                }
                else
                {
                    for (int i=0; i<i_Params.size(); i++)
                    {
                        v_Param = i_Params.get(i);
                        if ( !Help.isNull(i_XmlRootPath) )
                        {
                            // ZhengWei(HY) Edit 2017-10-27 i_XmlRootPath处原先的传值为：""
                            XJava.parserXml(v_FileHelp.getXD(i_XmlRootPath + i_XDName ,v_Param.getValue() ,this.enCode)                                                    ,i_XmlRootPath          ,v_Param.getValue());
                        }
                        else
                        {
                            XJava.parserXml(v_FileHelp.getXD(this.getClass().getResourceAsStream(Help.NVL(this.xmlClassPath) + i_XDName) ,v_Param.getValue() ,this.enCode) ,this.xjavaXmlClassPath ,v_Param.getValue());
                        }
                        v_OK++;
                        v_XFileNames.put(v_Param.getValue() ,this);
                        
                        if ( this.isLog )
                        {
                            log(v_Param ,LogType.$Loading);
                        }
                    }
                }
            }
        }
        catch (Exception exce)
        {
            System.out.println(exce.getMessage());
            exce.printStackTrace();
        }
        
        if ( this.isLog )
        {
            if ( v_OK < v_Count )
            {
                log(v_Param ,LogType.$Error);
            }
            else if ( v_OK == v_Count )
            {
                log(v_Param ,LogType.$Finish);
            }
        }
    }
    
    
    
    /**
     * 主要用于配置文件存在AppInitConfig继承类的同级目录下的情况。
     * 
     * 入参支持三个情况：
     *   1. 支持Java包路径，如 com.xxx.yyy
     *   2. 支持Java类名称，如 java.lang.Integer
     *   3. 支持XML文件名称，如 abc.xml
     *   4. 支持目录名称，将对其下所有（包含子目录）配置文件进行加载
     * 
     * @param i_PackageName
     */
    public synchronized void init(String i_PackageName)
    {
        if ( Help.isNull(i_PackageName) )
        {
            try
            {
                XJava.parserAnnotation();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return;
        }
        
        if ( i_PackageName.trim().toLowerCase().endsWith(".xml") )
        {
            List<Param> v_Params = new ArrayList<Param>();
            Param       v_Param  = new Param();
            
            v_Param.setValue(i_PackageName.trim());
            v_Param.setName(v_Param.getValue());
            
            v_Params.add(v_Param);
            this.init(v_Params);
            return;
        }
        
        try
        {
            XJava.parserAnnotation(i_PackageName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * Web服务的初始。主要用于配置文件存在 C:/Tomcat/webapps/项目名称/WEB-INF/ 目录下的情况。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     * 
     * @param i_Params  Param.getValue() 为配置文件名称。支持相对路径。如 db/db.Config.xml
     */
    public synchronized void initW(List<Param> i_Params)
    {
        this.init(null ,i_Params ,Help.getWebINFPath());
    }
    
    
    
    /**
     * Web服务的初始。主要用于配置文件存在 C:/Tomcat/webapps/项目名称/WEB-INF/ 目录下的情况。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-22
     * @version     v1.0
     * 
     * @param i_Params  Param.getValue() 为配置文件名称。支持相对路径。如 db/db.Config.xml
     */
    public synchronized void initW(List<Param> i_Params ,String i_XmlRootPath)
    {
        this.init(null ,i_Params ,i_XmlRootPath);
    }
    
    
    
    /**
     * Web服务的初始。主要用于配置文件存在 C:/Tomcat/webapps/项目名称/WEB-INF/ 目录下的情况。
     * 
     * 入参支持三个情况：
     *   1. 支持Java包路径，如 com.xxx.yyy
     *   2. 支持Java类名称，如 java.lang.Integer
     *   3. 支持XML文件名称，如 abc.xml
     *   4. 支持目录名称，将对其下所有（包含子目录）配置文件进行加载
     *   
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     * 
     * @param i_PackageName
     */
    public synchronized void initW(String i_PackageName)
    {
        this.initW(i_PackageName ,Help.getWebINFPath());
    }
    
    
    
    /**
     * Web服务的初始。主要用于配置文件存在 C:/Tomcat/webapps/项目名称/WEB-INF/ 目录下的情况。
     * 
     * 解释所有包中的"注解 Annotation"，转为Java对象实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-16
     * @version     v1.0
     *
     */
    public synchronized void initW()
    {
        try
        {
            XJava.parserAnnotation();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * Web服务的初始。主要用于配置文件存在 C:/Tomcat/webapps/项目名称/WEB-INF/ 目录下的情况。
     * 
     * 入参支持三个情况：
     *   1. 支持Java包路径，如 com.xxx.yyy
     *   2. 支持Java类名称，如 java.lang.Integer
     *   3. 支持XML文件名称，如 abc.xml
     *   4. 支持目录名称，将对其下所有（包含子目录）配置文件进行加载
     *   
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     * 
     * @param i_PackageName
     * @param i_XmlRootPath  Xml配置文件的根目录。如 C:/Tomcat/webapps/项目名称/WEB-INF/ 。（最后有一个/符号）
     */
    public synchronized void initW(String i_PackageName ,String i_XmlRootPath)
    {
        if ( Help.isNull(i_PackageName) )
        {
            try
            {
                XJava.parserAnnotation();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return;
        }
        
        if ( i_PackageName.trim().toLowerCase().endsWith(".xml") )
        {
            List<Param> v_Params = new ArrayList<Param>();
            Param       v_Param  = new Param();
            
            v_Param.setValue(i_PackageName.trim());
            v_Param.setName(v_Param.getValue());
            
            v_Params.add(v_Param);
            this.init("" ,v_Params ,i_XmlRootPath);
            return;
        }
        
        try
        {
            XJava.parserAnnotation(i_PackageName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * 只支持XD文件名称，如 abc.xd
     * 
     * @param i_XDName       格式可以为 abc.xd。或者为 abc，内部会自己加上 .xd
     * @param i_PackageName
     */
    public synchronized void init(String i_XDName ,String i_PackageName)
    {
        if ( Help.isNull(i_XDName) || Help.isNull(i_PackageName) )
        {
            return;
        }
        
        String v_XDName = i_XDName;
        if ( !v_XDName.trim().toLowerCase().endsWith(".xd") )
        {
            v_XDName += ".xd";
        }
        
        List<Param> v_Params = new ArrayList<Param>();
        Param       v_Param  = new Param();
        
        v_Param.setValue(i_PackageName.trim());
        v_Param.setName(v_Param.getValue());
        
        v_Params.add(v_Param);
        this.init(v_XDName ,v_Params);
    }
    
    
    
    /**
     * 打印日志，重写后有更多定制精彩
     * 
     * @param i_Param
     * @param i_LogType
     */
    public void log(Param i_Param ,LogType i_LogType)
    {
        if ( LogType.$Loading == i_LogType )
        {
            System.out.println("Loading " + i_Param.getValue());
        }
        else if ( LogType.$Finish == i_LogType )
        {
            System.out.println("load finish.");
        }
        else
        {
            System.out.println("Loading " + i_Param.getValue() + " error.");
        }
    }
    

    
    public boolean isLog()
    {
        return isLog;
    }


    
    public void setLog(boolean isLog)
    {
        this.isLog = isLog;
    }


    
    public String getXmlClassPath()
    {
        return xmlClassPath;
    }


    
    /**
     * 入参格式为：/com/xxx/yyy/。注意最后有一个符号'/' 
     * 
     * @param xmlClassPath
     */
    public void setXmlClassPath(String xmlClassPath)
    {
        this.xmlClassPath = xmlClassPath;
    }
    
    
    
    public void setXmlClassPath(Class<?> i_XmlClass)
    {
        this.xmlClassPath      = "/" + i_XmlClass.getName().replaceAll("\\." ,"\\/").replaceAll(i_XmlClass.getSimpleName() ,"");
        this.xjavaXmlClassPath = i_XmlClass.getName().replaceAll(i_XmlClass.getSimpleName() ,"");
    }


    
    public String getEnCode()
    {
        return enCode;
    }


    
    public void setEnCode(String enCode)
    {
        this.enCode = enCode;
    }
    
}
