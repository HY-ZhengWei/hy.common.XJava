package org.hy.common.xml.junit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;






public class JU_Spring extends JU_SpringSuper
{
    
    @Test
    public void test() throws NoSuchMethodException, SecurityException
    {
        Method v_Method = DefaultListableBeanFactory.class.getMethod("doResolveDependency");
        
        System.out.println(v_Method);
    }
    
    
    
    @Test
    public void testSuper() throws Throwable
    {
        JU_SpringSuper v_JU_Spring = new JU_Spring();
        
        Method v_Method = JU_SpringSuper.class.getMethod("toString");
        
        
        MethodType v_MethodType = MethodType.methodType(void.class);
        MethodType v_StringType = MethodType.methodType(String.class);
        MethodHandle inithandle = MethodHandles.lookup().findConstructor(JU_SpringSuper.class ,v_MethodType);
        
        //获取祖父类实例对象
        Object o = inithandle.invokeWithArguments(new Object[] {});
        
        //找到祖父类里被覆写的方法并把该方法绑定到祖父类实例上
        MethodHandle handle = MethodHandles.lookup()
                .findVirtual(JU_SpringSuper.class, "toString", v_StringType).bindTo(v_JU_Spring);
      //调用祖父类里被父类覆写的方法
        System.out.println(handle.invokeWithArguments(new Object[] {}));
    }
    
    
    
    public String toString()
    {
        return "I am Child.";
    }
    
}
