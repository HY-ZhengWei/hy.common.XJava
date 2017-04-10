package org.hy.common.xml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;





/**
 * 为了保证对象解释成的方法顺序是一致的
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2013-11-17
 * 
 * 注：此类已被 MethodComparator 代替
 */
@Deprecated
public class MethodOrder implements Comparable<MethodOrder>
{
    private Method method;
    
    
    
    public static List<MethodOrder> toMethodOrders(List<Method> i_Methods)
    {
        List<MethodOrder> v_Ret = new ArrayList<MethodOrder>();
        
        for (int i=0; i<i_Methods.size(); i++)
        {
            v_Ret.add(new MethodOrder(i_Methods.get(i)));
        }
        
        Collections.sort(v_Ret);
        
        return v_Ret;
    }
    
    
    
    public static List<Method> toMethods(List<MethodOrder> i_JSONMethods)
    {
        List<Method> v_Ret = new ArrayList<Method>();
        
        for (int i=0; i<i_JSONMethods.size(); i++)
        {
            v_Ret.add(i_JSONMethods.get(i).getMethod());
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 按方法名称排序
     * 
     * @param io_Methods
     */
    public static void sort(List<Method> io_Methods)
    {
        List<MethodOrder> v_Caches = new ArrayList<MethodOrder>();
        
        for (int i=0; i<io_Methods.size(); i++)
        {
            v_Caches.add(new MethodOrder(io_Methods.get(i)));
        }
        
        Collections.sort(v_Caches);
        
        io_Methods.clear();
        
        for (int i=0; i<v_Caches.size(); i++)
        {
            io_Methods.add(v_Caches.get(i).getMethod());
        }
    }
    
    
    
    public MethodOrder()
    {
        
    }
    
    
    
    public MethodOrder(Method i_Method)
    {
        this.method = i_Method;
    }
    

    
    public int compareTo(MethodOrder i_Other)
    {
        if ( i_Other == null )
        {
            return 1;
        }
        else if ( this == i_Other )
        {
            return 0;
        }
        else
        {
            return this.method.getName().compareTo(i_Other.method.getName());
        }
    }


    
    public Method getMethod()
    {
        return method;
    }

    
    
    public void setMethod(Method method)
    {
        this.method = method;
    }
    
}
