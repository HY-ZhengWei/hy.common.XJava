package org.hy.common.xml.junit.overWriteXml;

import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.Test;





/**
 * 测试单元：XSQL云计算
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-02-22
 * @version     v1.0
 */
@Xjava(value=XType.XML)
public class JU_OverWriteXml
{
    private static boolean $isInit = false;
    
    
    
    public JU_OverWriteXml() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_OverWriteXml()
    {
        System.out.println("........" + XJava.getObject("DS_OA_EMP"));
    }
    
}
