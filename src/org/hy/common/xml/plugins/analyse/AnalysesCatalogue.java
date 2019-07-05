package org.hy.common.xml.plugins.analyse;

import java.util.List;

import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.analyse.data.WindowsApp;





/**
 * 加载大纲目录
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-08-01
 * @version     v1.0
 */
@Xjava(value=XType.XML)
public class AnalysesCatalogue
{
    
    private static boolean $isInit = false;
    
    
    
    public AnalysesCatalogue() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(AnalysesCatalogue.class.getName());
        }
    }
    
    
    
    /**
     * 获取大纲目录
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-08-01
     * @version     v1.0
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized static List<WindowsApp> getCatalogue()
    {
        if ( !$isInit )
        {
            try
            {
                new AnalysesCatalogue();
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return (List<WindowsApp>)XJava.getObject("AnalysesCatalogue");
    }
    
}
