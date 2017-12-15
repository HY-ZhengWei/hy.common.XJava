package org.hy.common.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * XSQL、XSQLGroup的注解。用于 xml配置 + Java接口类(无须实现类)的组合实现持久层的功能。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-14
 * @version     v1.0
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Xsql
{
    
    /**
     * Java方法对应的XML配置文件中定义的XSQL、XSQLGroup对象的ID。即XJava对象池中的XID。
     * 
     * 可选项。当为空时，用方法名称作XID在XJava对象池中匹配。
     * 
     * 只用于方法
     */
    public String value() default "";
    
    
    
    /**
     * 与 value() 同义。只为为了在注释时意义更明确。
     */
    public String id() default "";
    
    
    
    /**
     * 定义方法入参与XSQL占位符的对应关系。
     * 
     * 说明方法多个入参依次转为Map集合时的Map.key值。
     * 转为Map集合后，将Map集合为XSQL、XSQLGroup的执行入参传递。
     * 
     * 可选项。
     * 
     * 只用于方法。
     * 1. 如果设定此属性，应小于等于方法的入参个数，否则报异常。
     * 2. 方法入参个数小于等于1时，可不设定此属性。
     * 3. 方法入参个数大于1时，必须设定此属性，否则报异常。
     * 4. paramNames()[x] 为空字符串时，表示跳过第x个方法入参，x位置的入参将不作为XSQL、XSQLGroup的执行入参传递。
     */
    public String [] names() default {};
    
    
    
    /**
     * 表示只取查询结果集中的首行记录。即只返回一个对象。
     * 
     * 可选项。
     * 
     * 只用于方法。
     * 只用于查询SQL，并且结果集的类型为List集合。
     */
    public boolean returnOne() default false;
    
    
    
    /**
     * 与 XSQLNode.returnID 同义。
     * 
     * 说明代理方法执行结果的返回值是哪个。
     * 
     * 可选项。
     * 
     * 只用于方法；
     * 只用于XSQLGroup
     */
    public String returnID() default "";
    
}
