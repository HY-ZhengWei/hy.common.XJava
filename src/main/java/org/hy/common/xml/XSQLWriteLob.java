package org.hy.common.xml;

import org.hy.common.app.Param;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;





/**
 * 数据库大数据类型(如CLob)的写入功能的模板。
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-18
 * @version     v1.0
 */
@Xjava(XType.XML) 
public final class XSQLWriteLob
{
    
    private static boolean $IsInit = false;
    
    
    
    private synchronized static void init()
    {
        if ( !$IsInit )
        {
            $IsInit = true;
            try
            {
                XJava.parserAnnotation(XSQLWriteLob.class.getName());
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
    }
    
    
    
    /**
     * 获取数据库大数据类型(如CLob)的写入功能的模板。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @param i_DBType
     * @return
     */
    public static Param getLobTempalte(String i_DBType)
    {
        init();
        return (Param)XJava.getObject("XSQL_WriteLob_" + i_DBType);
    }
    
}
