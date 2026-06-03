package org.hy.common.xml.junit.xjavaFor;

import java.util.Map;

import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：For循环批量创建对象的信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-06-03
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_XJavaFor
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XJavaFor() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_XJavaFor.class.getName());
        }
    }
    
    
    
    /**
     * For循环批量创建对象的信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-06-03
     * @version     v1.0
     *
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void test_For()
    {
        Param                 v_Param1   = XJava.getParam("XJavaFor_1");
        Param                 v_Param2   = XJava.getParam("XJavaFor_2");
        DataInfo              v_Data1    = (DataInfo) XJava.getObject("XJavaFor_Data_ID1");
        DataInfo              v_Data2    = (DataInfo) XJava.getObject("XJavaFor_Data_ID2");
        DataInfo              v_Data3    = (DataInfo) XJava.getObject("XJavaFor_Data_ID3");
        Map<String ,DataInfo> v_DataMap1 = (Map<String ,DataInfo>) XJava.getObject("DataMap01");
        Map<String ,DataInfo> v_DataMap2 = (Map<String ,DataInfo>) XJava.getObject("DataMap01");
        Assert.assertNotNull(v_Param1);
        Assert.assertNotNull(v_Param2);
        Assert.assertNotNull(v_Data1);
        Assert.assertNotNull(v_Data2);
        Assert.assertNotNull(v_Data3);
        Assert.assertNotNull(v_DataMap1);
        Assert.assertNotNull(v_DataMap2);
    }
    
}
