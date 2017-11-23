package org.hy.common.xml.junit;

import java.lang.reflect.Field;

import org.junit.Test;





/**
 * 测试单元：私有属性的访问 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-11-21
 * @version     v1.0
 */
@SuppressWarnings("unused")
public class JU_PrivateField
{
    
    private long    longValue;
    
    private byte    byteValue;
    
    private int     intValue;
    
    private boolean booleanValue;
    
    private String  stringValue;
    
    
    
    @Test
    public void test_Field() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException
    {
        JU_PrivateField v_Instance = JU_PrivateField.class.newInstance();
        String []       v_Names    = {"longValue" ,"byteValue" ,"intValue" ,"booleanValue" ,"stringValue"};
        Object []       v_Values   = {1L          ,(byte)1     ,2          ,true           ,"ZhengWei"};
        
        for (int i=0; i<v_Names.length; i++)
        {
            Field v_Filed = JU_PrivateField.class.getDeclaredField(v_Names[i]);
            
            v_Filed.setAccessible(true);
            v_Filed.set(v_Instance ,v_Values[i]);
            
            System.out.println(v_Filed.get(v_Instance));
        }
        
        System.out.println(v_Instance);
    }
    
}
