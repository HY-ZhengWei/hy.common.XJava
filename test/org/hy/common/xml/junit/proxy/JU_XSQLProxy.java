package org.hy.common.xml.junit.proxy;


import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.Test;





/**
 * 测试单元：XSQL代理
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-14
 * @version     v1.0
 */
@Xjava(XType.XML)
public class JU_XSQLProxy
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XSQLProxy() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
            XJava.parserAnnotation(DAOInterface.class.getName());
        }
    }
    
    
    
    @Test
    public void test_001()
    {
        ((DAOInterface)XJava.getObject("DAOIF")).query();
    }
    
}
