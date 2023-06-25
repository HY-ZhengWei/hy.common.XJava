package org.hy.common.xml.annotation;





/**
 * XJava加载配置文件的类型
 * 
 * @author  ZhengWei(HY)
 * @version 2014-04-17
 *          2017-12-15  添加：XSQL代理
 *          2023-06-24  添加：XCQL代理
 */
public enum XType
{
    
    /** 默认值。空类型 */
    NULL,

    /** 使用XML文件配置XJava */
    XML,

    /** 使用XD文件配置XJava */
    XD,
    
    /** 使用XSQL代理配置XJava。只能用于接口Interface */
    XSQL,
    
    /** 使用XCQL代理配置XJava。只能用于接口Interface */
    XCQL
}
