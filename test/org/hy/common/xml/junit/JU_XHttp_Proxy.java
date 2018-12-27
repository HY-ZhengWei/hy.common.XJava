package org.hy.common.xml.junit;

import org.hy.common.Return;
import org.hy.common.xml.XHttp;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：通过代理发送请求
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-27
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_XHttp_Proxy
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_XHttp_Proxy() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_Proxy()
    {
        XHttp     v_XHttp = (XHttp)XJava.getObject("XHTTP_Proxy");
        Return<?> v_Ret   = v_XHttp.request();
        
        if ( v_Ret.booleanValue() )
        {
            System.out.println("请求成功，返回如下内容：");
            System.out.println(v_Ret.paramStr);
        }
        else
        {
            System.err.println("请求异常，异常信息如下：");
            System.err.println(v_Ret.exception.getMessage());
        }
    }
    
}
