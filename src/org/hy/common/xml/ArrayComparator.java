package org.hy.common.xml;

import java.util.Comparator;

import org.hy.common.Help;





/**
 * "数组"的排序比较器
 * 
 * 只对数组的第一个元素进行比较
 * 
 * @author      ZhengWei(HY)
 * @createDate  2014-11-30
 * @version     v1.0  
 */
public class ArrayComparator implements Comparator<Object []>
{
    private static ArrayComparator $MY;
    
    
    
    public static synchronized ArrayComparator getInstance()
    {
        if ( $MY == null )
        {
            $MY = new ArrayComparator();
        }
        
        return $MY;
    }
    

    
    public int compare(Object [] i_Array1 ,Object [] i_Array2)
    {
        if ( i_Array1 == i_Array2 )
        {
            return 0;
        }
        else if ( Help.isNull(i_Array1) )
        {
            return -1;
        }
        else if ( Help.isNull(i_Array2) )
        {
            return 1;
        }
        else
        {
            return i_Array1[0].toString().compareTo(i_Array2[0].toString());
        }
    }
    
}
