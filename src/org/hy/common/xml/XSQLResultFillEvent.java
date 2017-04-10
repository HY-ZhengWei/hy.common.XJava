package org.hy.common.xml;





/**
 * 行级对象填充到表级对象时，在填充之前触发的事件接口
 * 
 *   1. 支持一行转多行功能
 *   2. 支持一行转多列功能
 *   3. 支持过滤行记录功能
 *   4. 支持一对多关系功能。见 org.hy.common.xml.event.DefaultXSQLResultFillEvent
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-01-25
 * @version     v1.0
 *              v2.0  2017-03-02  添加：before()方法添加i_PreviousRow参数：前一行的行级对象（被成功填充的，未成功填充的不算）。
 */
public interface XSQLResultFillEvent
{
    
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
    public boolean before(final Object i_Table ,final Object i_Row ,final long i_RowNo ,final Object i_PreviousRow);
    
}
