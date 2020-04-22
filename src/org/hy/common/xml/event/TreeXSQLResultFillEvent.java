package org.hy.common.xml.event;

import org.hy.common.xml.XSQLResultFillEvent;





public class TreeXSQLResultFillEvent implements XSQLResultFillEvent
{
    
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
    public boolean before(final Object i_Table ,final Object i_Row ,final long i_RowNo ,final Object i_PreviousRow)
    {
//        Product p;
//        
//        if ( ((ProductList)i_Table).getChildMap().containsKey(p.getSuperID()) )
//        {
//            Product v_Super = ((ProductList)i_Table).getChildMap().get(p.getSuperID());
//            
//            v_Super.getChilds().add(p);
//            
//            
//            ((ProductList)i_Table).getChildMap().put(p.getId() ,p);
//        }
//        else
//        {
//            ((ProductList)i_Table).getChildMap().put(p.getId() ,p);
//        }
        
        return false;
    }
    
    
    
    
    
    
    
}
