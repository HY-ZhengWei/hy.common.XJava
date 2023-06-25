package org.hy.common.xml.junit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.Test;





/**
 * 测试单元：私有属性的赋值
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-11-21
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@SuppressWarnings("unused")
public class JU_PrivateField
{
    
    private static boolean $isInit = false;
    
    private long    longValue;
    
    private Long    longObject;
    
    private byte    byteValue;
    
    private int     intValue;
    
    private boolean booleanValue;
    
    private String  stringValue;
    
    
    
    public JU_PrivateField() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_Field() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        JU_PrivateField v_Instance = JU_PrivateField.class.getDeclaredConstructor().newInstance();
        String []       v_Names    = {"longValue" ,"longObject" ,"byteValue" ,"intValue" ,"booleanValue" ,"stringValue" ,"stringValue"};
        Object []       v_Values   = {1L          ,null         ,(byte)1     ,2          ,true           ,"ZhengWei"    ,null};
        
        for (int i=0; i<v_Names.length; i++)
        {
            Field v_Filed = JU_PrivateField.class.getDeclaredField(v_Names[i]);
            
            v_Filed.setAccessible(true);
            v_Filed.set(v_Instance ,v_Values[i]);
            
            System.out.println(v_Filed.get(v_Instance));
        }
        
        System.out.println(v_Instance);
    }
    
    
    
    @Test
    public void test_XJava()
    {
        System.out.println(XJava.getObject("PrivateField01"));
    }
    
}
