package org.hy.common.xml.event;

import org.hy.common.MethodReflect;
import org.hy.common.SumObjectMap;
import org.hy.common.SumStringMap;
import org.hy.common.xml.XSQLResultFillEvent;





/**
 * 多行字符串合并功能。
 * 
 * 要求：XSQL.result.table(<table>...</table>)必须是如下两种类型结构之一
 *      1. org.hy.common.SumObjectMap
 *      2. org.hy.common.SumStringMap 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-06-01
 * @version     v1.0
 */
public class SumStringXSQLResultFillEvent implements XSQLResultFillEvent
{
    
    /** 连接符、对象属性名称的分隔符。默认是逗号 */
    private String split;
    
    /** 连接符。默认是空字符串。多个属性间用this.split指定字符分隔 */
    private String connectors;
    
    /** 合并或拼接对象的哪个属性。支持面向对象，可实现xxx.yyy.www全路径的解释。多个属性间用this.split指定字符分隔 */
    private String methodURLs;
    
    
    
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
        if ( MethodReflect.isExtendImplement(i_Table ,SumObjectMap.class) )
        {
            SumObjectMap<? ,?> v_SumMap = (SumObjectMap<? ,?>)i_Table;
            
            v_SumMap.setSplit(     this.split);
            v_SumMap.setConnectors(this.connectors);
            v_SumMap.setMethodURLs(this.methodURLs);
        }
        else if ( MethodReflect.isExtendImplement(i_Table ,SumStringMap.class) )
        {
            SumStringMap<?> v_SumMap = (SumStringMap<?>)i_Table;
            
            v_SumMap.setConnector(this.connectors);
        }
        else
        {
            throw new java.lang.ClassCastException("XSQL.result.table(<table>...</table>) is not SumObjectMap or SumStringMap.");
        }
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
        // 核心均是使用SumObjectMap集合实现的，所以这里无须任何代码
        return true;
    }


    
    /**
     * 获取：连接符。默认是空字符串。多个属性间用this.split指定字符分隔。多个属性间用this.split指定字符分隔
     */
    public String getConnectors()
    {
        return connectors;
    }
    

    
    /**
     * 获取：合并或拼接对象的哪个属性。支持面向对象，可实现xxx.yyy.www全路径的解释。多个属性间用this.split指定字符分隔
     */
    public String getMethodURLs()
    {
        return methodURLs;
    }
    

    
    /**
     * 设置：连接符。默认是空字符串。多个属性间用this.split指定字符分隔
     * 
     * @param connector 
     */
    public void setConnectors(String i_Connectors)
    {
        this.connectors = i_Connectors;
    }
    
    
    
    /**
     * 设置：合并或拼接对象的哪个属性。支持面向对象，可实现xxx.yyy.www全路径的解释。多个属性间用this.split指定字符分隔
     * 
     * @param methodURLs 
     */
    public void setMethodURL(String i_MethodURLs)
    {
        this.methodURLs = i_MethodURLs;
    }
    
    
    
    /**
     * 获取：连接符、对象属性名称的分隔符。默认是逗号。多个属性间用this.split指定字符分隔
     */
    public String getSplit()
    {
        return split;
    }
    
    
    
    /**
     * 设置：连接符、对象属性名称的分隔符。默认是逗号。多个属性间用this.split指定字符分隔
     * 
     * @param split 
     */
    public void setSplit(String split)
    {
        this.split = split;
    }
    
}
