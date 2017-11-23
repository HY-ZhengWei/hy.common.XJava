package org.hy.common.xml.junit;

import java.lang.reflect.Field;

import org.hy.common.app.Param;
import org.junit.Test;





/**
 * 测试单元：私有属性的访问 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-11-21
 * @version     v1.0
 */
public class JU_PrivateField
{
    
    @Test
    public void test_Field() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException
    {
        Param v_Instance = Param.class.newInstance();
        
        Field v_Filed = Param.class.getDeclaredField("name");
        
        v_Filed.setAccessible(true);
        v_Filed.set(v_Instance ,"ZhengWei");
        
        
        System.out.println(v_Instance.getName());
    }
    
}
