package org.hy.common.xml.junit;

import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：属性加密
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-09-04
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_Encrypt
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_Encrypt() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_Encrypt()
    {
        System.out.println("我的秘密01" + "\t" + XJava.getParam("我的秘密01").getValue());
        System.out.println("我的秘密02" + "\t" + XJava.getParam("我的秘密02").getValue());
    }
    
}
