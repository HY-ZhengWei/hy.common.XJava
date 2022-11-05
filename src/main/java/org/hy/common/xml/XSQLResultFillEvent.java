package org.hy.common.xml;





/**
 * 行级对象填充到表级对象时，在填充之前触发的事件接口
 * 
 *   1. 支持一行转多行功能（如，列转行）
 *   2. 支持一列转多列功能（如，行转列）
 *   3. 支持多行转一行功能（如，多行字符串合并功能）
 *   4. 支持过滤行记录功能
 *   5. 支持一对多关系功能。见 org.hy.common.xml.event.DefaultXSQLResultFillEvent
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-01-25
 * @version     v1.0
 *              v2.0  2017-03-02  添加：before()方法添加i_PreviousRow参数：前一行的行级对象（被成功填充的，未成功填充的不算）。
 *              v3.0  2018-06-01  添加：start()方法，在整体开始填充之前触发，并且只触发一次。
 */
public interface XSQLResultFillEvent
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
    public void start(final Object i_Table);
    
    
    
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
