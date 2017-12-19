package org.hy.common.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * 方法的参数注解。
 * 
 * 比 @Xsql(names{}) 属性更为丰富的配置。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-18
 * @version     v1.0
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Xparam
{
    
    /**
     * 定义参数的映射名称
     */
    public String value() default "";
    
    
    
    /**
     * 与 value() 同义。只为为了在注释时意义更明确。
     */
    public String id() default "";
    
    
    
    /**
     * 与 value() 同义。只为为了在注释时意义更明确。
     */
    public String name() default "";
    
    
    
    /**
     * 验证参数是否为null。
     * 
     *   1. 当参数是String时，还将验证对象的哪些属性是否为null(及空字符串)。
     *   2. 当参数是Set、List、Map时，还将验证集合元素是否小于等于0。
     */
    public boolean notNull() default false;
    
    
    
    /**
     * 除了验证参数自身是否为null外，还将验证对象的多个属性。
     * 
     *  1. 当参数对象的属性是String时，还将验证对象的哪些属性是否为null(及空字符串)。
     *  2. 当参数对象的属性是Set、List、Map时，还将验证集合元素是否小于等于0。
     */
    public String [] notNulls() default {};
    
}
