package org.hy.common.xml.junit.xjavaBuilder;

import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：Builder创建对象实例
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-07-13
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_XJavaBuilder
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XJavaBuilder() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_XJavaBuilder.class.getName());
        }
    }
    
    
    
    /**
     * Builder创建对象实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-13
     * @version     v1.0
     *
     */
    @Test
    public void test_Builder()
    {
        DataBuilder v_DataBuilder = (DataBuilder) XJava.getObject("XJavaBuilder");
        Assert.assertNotNull(v_DataBuilder);
    }
    
}
