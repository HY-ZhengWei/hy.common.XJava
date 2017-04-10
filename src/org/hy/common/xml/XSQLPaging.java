package org.hy.common.xml;

import org.hy.common.app.Param;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;





/**
 * 通用(限定常用的数据库)分页查询的模板。
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-02-22
 * @version     v1.0
 */
@Xjava(XType.XML) 
public final class XSQLPaging
{
    
    private static boolean $IsInit = false;
    
    
    
    private synchronized static void init()
    {
        if ( !$IsInit )
        {
            $IsInit = true;
            try
            {
                XJava.parserAnnotation(XSQLPaging.class.getName());
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
    }
    
    
    
    /**
     * 获取对应数据库的分页SQL模板
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-22
     * @version     v1.0
     *
     * @param i_DBType
     * @return
     */
    public static Param getPagingTempalte(String i_DBType)
    {
        init();
        return (Param)XJava.getObject("XSQLPaging_" + i_DBType);
    }
    
}
