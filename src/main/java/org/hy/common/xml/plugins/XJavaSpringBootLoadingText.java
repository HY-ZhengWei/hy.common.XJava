package org.hy.common.xml.plugins;

import org.hy.common.Help;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;





/**
 * 加载SpringBoot&XJava文字
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-04-18
 * @version     v1.0
 */
@Xjava(XType.XML) 
public final class XJavaSpringBootLoadingText
{
    
    private static boolean $IsInit = false;
    
    
    
    private synchronized static void init()
    {
        if ( !$IsInit )
        {
            $IsInit = true;
            try
            {
                XJava.parserAnnotation(XJavaSpringBootLoadingText.class.getName());
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
    }
    
    
    
    /**
     * 从运行时中判定并获取SpringBoot版本号
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-04-18
     * @version     v1.0
     *
     * @return
     */
    public static int getVersion()
    {
        Class<?> v_Class = null;
        
        try
        {
            v_Class = Help.forName("org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext");
        }
        catch (ClassNotFoundException exce)
        {
            v_Class = null;
        }
        if ( v_Class != null )
        {
            return 3;
        }
        
        try
        {
            v_Class = Help.forName("org.springframework.boot.web.server.servlet.context.AnnotationConfigServletWebServerApplicationContext");
        }
        catch (ClassNotFoundException exce)
        {
            v_Class = null;
        }
        if ( v_Class != null )
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }
    
    
    
    /**
     * 获取对应SpringBoot&XJava文字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-04-18
     * @version     v1.0
     *
     * @param i_SpringBootVersion  版本号。支持：3和4
     * @return
     */
    public static String getText(int i_SpringBootVersion)
    {
        init();
        int v_Version = i_SpringBootVersion >= 4 ? 4 : 3; 
        return XJava.getParam("XJavaSpringBootLoadingVersion_" + v_Version + "x").getValue();
    }
    
    
    
    /**
     * 获取对应SpringBoot&XJava代码
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-04-18
     * @version     v1.0
     *
     * @param i_SpringBootVersion  版本号。支持：3和4
     * @return
     */
    public static String getJava(int i_SpringBootVersion)
    {
        init();
        int v_Version = i_SpringBootVersion >= 4 ? 4 : 3; 
        return XJava.getParam("XJavaSpringBootLoadingJava_" + v_Version + "x").getValue();
    }
    
}
