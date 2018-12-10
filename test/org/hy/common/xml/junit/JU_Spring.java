package org.hy.common.xml.junit;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class JU_Spring
{
    
    @Test
    public void test() throws NoSuchMethodException, SecurityException
    {
        Method v_Method = DefaultListableBeanFactory.class.getMethod("doResolveDependency");
        
        
        System.out.println(v_Method);
    }
    
}
