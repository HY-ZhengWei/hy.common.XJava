package org.hy.common.xml.event;

import java.util.List;
import java.util.Map;

import org.hy.common.xml.XSQLResultFillEvent;





/**
 * 行级对象（Map结构）填充到表级对象时，在填充之前触发的事件接口
 * 
 *   1. 一对多关系时，识别出属于同一对象的多个关系信息，并保存在一起。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2024-01-09
 * @version     v1.0
 */
public class DefaultXSQLResultMapFillEvent implements XSQLResultFillEvent
{
    
    /** 识别一对多关系的Getter方法。如组合主键时，获取每个主键的Getter方法 */
    private List<RelationKeyMethod> relationKeyMethods;
    
    
    
    public DefaultXSQLResultMapFillEvent(List<RelationKeyMethod> i_RelationKeyMethods)
    {
        this.relationKeyMethods = i_RelationKeyMethods;
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
    @Override
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
    @Override
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
            for (RelationKeyMethod v_Method : this.relationKeyMethods)
            {
                Object v_PreviousKey = v_Method.getMethod().invoke(i_PreviousRow ,v_Method.getMapKey());
                Object v_Key         = v_Method.getMethod().invoke(i_Row         ,v_Method.getMapKey());
                
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
            
            
            Map<Object ,Object> v_PreviousRowMap = (Map<Object ,Object>) i_PreviousRow;
            Map<Object ,Object> v_RowMap         = (Map<Object ,Object>) i_Row;
            v_PreviousRowMap.putAll(v_RowMap);
            v_RowMap.clear();
            return false;
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return false;
        }
    }
    
}
