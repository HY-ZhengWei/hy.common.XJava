package org.hy.common.xml.plugins.analyse;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import org.hy.common.app.Param;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.XJava;





/**
 * 分析服务的基础类
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-12-15
 * @version     v1.0
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
    protected String getTemplateContent(String i_TemplateName)
    {
        if ( $TemplateCaches.containsKey(i_TemplateName) )
        {
            return $TemplateCaches.get(i_TemplateName);
        }
        
        String v_Content = "";
        
        try
        {
            v_Content = this.getFileContent(i_TemplateName);
            $TemplateCaches.put(i_TemplateName ,v_Content);
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
    protected String getFileContent(String i_FileName) throws Exception
    {
        FileHelp    v_FileHelp    = new FileHelp();
        String      v_PackageName = "org.hy.common.xml.plugins.analyse".replaceAll("\\." ,"/");
        InputStream v_InputStream = this.getClass().getResourceAsStream("/" + v_PackageName + "/" + i_FileName);
        
        return v_FileHelp.getContent(v_InputStream ,"UTF-8");
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
