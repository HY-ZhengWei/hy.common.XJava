package org.hy.common.xml.plugins.analyse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.app.Param;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.XJava;





/**
 * 分析服务的基础类
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-12-15
 * @version     v1.0
 *              v2.0  2019-07-05  添加：按包路径获取文件的方法
 */
public class Analyse
{
    
    /** 模板信息的缓存 */
    private final static Map<String ,String> $TemplateCaches  = new Hashtable<String ,String>();
    
    
    
    /**
     * 获取模板内容（有缓存机制）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_TemplateName  模板名称
     * @return
     */
    public String getTemplateContent(String i_TemplateName)
    {
        return getTemplateContent(i_TemplateName ,null);
    }
    
    
    
    /**
     * 获取模板内容（有缓存机制）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_Package       包名称
     * @param i_TemplateName  模板名称
     * @return
     */
    public String getTemplateContent(String i_TemplateName ,String i_Package)
    {
        if ( $TemplateCaches.containsKey(i_TemplateName) )
        {
            return $TemplateCaches.get(i_TemplateName);
        }
        
        String v_Content = "";
        
        try
        {
            if ( Help.isNull(i_Package) )
            {
                v_Content = this.getFileTemplate(i_TemplateName);
            }
            else
            {
                v_Content = this.getFileTemplate(i_TemplateName ,i_Package);
            }
            $TemplateCaches.put(i_TemplateName ,v_Content);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Content;
    }
    
    
    
    /**
     * 获取模板内容（无缓存机制）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-05-26
     * @version     v1.0
     *
     * @param i_Package       包名称
     * @param i_TemplateName  模板名称
     * @return
     */
    public byte [] getTemplateContentBytes(String i_TemplateName ,String i_Package)
    {
        byte [] v_Content = null;
        
        try
        {
            v_Content = this.getFileTemplateByte(i_TemplateName ,i_Package);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return v_Content;
    }
    
    
    
    /**
     * 获取文件内容
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_FileName  文件名称(无须文件路径)。此文件应在同级目录中保存
     * @return
     * @throws Exception
     */
    protected String getFileTemplate(String i_FileName) throws Exception
    {
        return getFileTemplate(i_FileName ,"org.hy.common.xml.plugins.analyse.templates");
    }
    
    
    
    /**
     * 获取文件内容
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-07-05
     * @version     v1.0
     *
     * @param i_FileName  文件名称(无须文件路径)。此文件应在同级目录中保存
     * @param i_Package   包路径
     * @return
     * @throws Exception
     */
    protected String getFileTemplate(String i_FileName ,String i_Package) throws Exception
    {
        FileHelp    v_FileHelp    = new FileHelp();
        String      v_PackageName = i_Package.replaceAll("\\." ,"/");
        InputStream v_InputStream = this.getClass().getResourceAsStream("/" + v_PackageName + "/" + i_FileName);
        
        try
        {
            return v_FileHelp.getContent(v_InputStream ,"UTF-8");
        }
        finally
        {
            if ( v_InputStream != null )
            {
                try
                {
                    v_InputStream.close();
                }
                catch (IOException exce)
                {
                    // Nothing.
                }
                
                v_InputStream = null;
            }
        }
    }
    
    
    
    /**
     * 获取文件内容
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-05-26
     * @version     v1.0
     *
     * @param i_FileName  文件名称(无须文件路径)。此文件应在同级目录中保存
     * @param i_Package   包路径
     * @return
     * @throws Exception
     */
    protected byte [] getFileTemplateByte(String i_FileName ,String i_Package) throws Exception
    {
        FileHelp    v_FileHelp    = new FileHelp();
        String      v_PackageName = i_Package.replaceAll("\\." ,"/");
        InputStream v_InputStream = this.getClass().getResourceAsStream("/" + v_PackageName + "/" + i_FileName);
        
        try
        {
            return v_FileHelp.getContentByte(v_InputStream);
        }
        finally
        {
            if ( v_InputStream != null )
            {
                try
                {
                    v_InputStream.close();
                }
                catch (IOException exce)
                {
                    // Nothing.
                }
                
                v_InputStream = null;
            }
        }
    }
    
    
    
    /**
     * 获取参数Param对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-12-15
     * @version     v1.0
     *
     * @param i_XJavaID
     * @return
     */
    protected Param getParam(String i_XJavaID)
    {
        return (Param)XJava.getObject(i_XJavaID);
    }
    
}
