package org.hy.common.xml.event;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.xml.XSQLResultFillEvent;





/**
 * 行级对象填充到表级对象时，在填充之前触发的事件接口
 * 
 *   1. 一对多关系时，识别出属于同一对象的多个关系信息，并保存在一起。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-03-02
 * @version     v1.0
 */
public class DefaultXSQLResultFillEvent implements XSQLResultFillEvent
{
    
    /** 识别一对多关系的Getter方法。如组合主键时，获取每个主键的Getter方法 */
    private List<Method> relationKeyMethods;
    
    /** 一对多关系时，从 "一对象" 中获取 "多对象" 的Getter方法。这个Getter方法返回值是一个集合，如: List、Set */
    private List<Method> relationValueMethods;
    
    
    
    public DefaultXSQLResultFillEvent(List<Method> i_RelationKeyMethods ,List<Method> i_RelationValueMethods)
    {
        this.relationKeyMethods   = i_RelationKeyMethods;
        this.relationValueMethods = i_RelationValueMethods;
    }
    
    
    
    /**
     * 在整体开始填充之前触发，并且只触发一次。
     * 
     * 方便在整体开始填充前统一的初始化等操作。
     * 
     * 在before()方法前触发调用。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-06-01
     * @version     v1.0
     *
     * @param i_Table
     */
    public void start(final Object i_Table)
    {
        // Nothing.
    }
    
    
    
    /**
     * 填充之前触发的事件方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-25
     * @version     v1.0
     *
     * @param i_Table        表级对象
     * @param i_Row          行级对象
     * @param i_RowNo        行号（如果实现本接口的外界用户，通过本方法额外填充了一行或多行数据到表级对象中，此i_RowNo行号不随外界用户的添加而增加）
     * @param i_PreviousRow  前一行的行级对象（被成功填充的，未成功填充的不算）
     * @return               当返回false时，不将i_Row行级对象填充到i_Table表级对象中。
     *                       当返回true时，才将行级对象填充到表级对象中。
     */
    @SuppressWarnings("unchecked")
    public boolean before(final Object i_Table ,final Object i_Row ,final long i_RowNo ,final Object i_PreviousRow)
    {
        if ( i_PreviousRow == null )
        {
            return true;
        }
        
        try
        {
            boolean v_IsEquals = true;
            
            // 判定是否为一对多关系
            for (Method v_Method : this.relationKeyMethods)
            {
                Object v_PreviousKey = v_Method.invoke(i_PreviousRow);
                Object v_Key         = v_Method.invoke(i_Row);
                
                if ( v_Key == v_PreviousKey )
                {
                    // Nothing.
                }
                else if ( null == v_Key || null == v_PreviousKey )
                {
                    v_IsEquals = false;
                    break;
                }
                else if ( !v_Key.equals(v_PreviousKey) )
                {
                    v_IsEquals = false;
                    break;
                }
            }
            
            
            if ( !v_IsEquals )
            {
                // 不相同的一行记录
                return true;
            }
            
            
            for (Method v_Method : this.relationValueMethods)
            {
                Collection<Object> v_PreviousValue = ((Collection<Object>)v_Method.invoke(i_PreviousRow));
                Collection<Object> v_Value         = ((Collection<Object>)v_Method.invoke(i_Row));
                
                if ( !Help.isNull(v_PreviousValue) && !Help.isNull(v_Value) )
                {
                    // 向前一行记录的一对多关系的 "多对象" 中添加信息
                    v_PreviousValue.addAll(v_Value);
                }
                else if ( Help.isNull(v_PreviousValue) && !Help.isNull(v_Value) )
                {
                    // 当前一行记录的一对多关系的 "多对象" 为空时，通过Getter方法反射出Setter方法，并用当前行的 "多对象" 填充
                    Method v_SetMethod = MethodReflect.getSetMethod(i_PreviousRow.getClass() ,v_Method.getName() ,false);
                    
                    if ( v_SetMethod != null )
                    {
                        v_SetMethod.invoke(i_PreviousRow ,v_Value);
                    }
                }
            }
            return false;
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return false;
        }
    }
    
}
