package org.hy.common.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * XJava的注解接口
 * 
 * 元注解是指注解的注解。包括以下四种。
 * 
 * 1. Target: 定义注解的作用目标
 *      Target(ElementType.TYPE)            -- 接口、类、枚举、注解
 *      Target(ElementType.FIELD)           -- 字段、枚举的常量
 *      Target(ElementType.METHOD)          -- 方法
 *      Target(ElementType.PARAMETER)       -- 方法参数
 *      Target(ElementType.CONSTRUCTOR)     -- 构造函数
 *      Target(ElementType.LOCAL_VARIABLE)  -- 局部变量
 *      Target(ElementType.ANNOTATION_TYPE) -- 注解
 *      Target(ElementType.PACKAGE)         -- 包   
 * 
 * 
 * 2. Retention: 定义注解的保留策略
 *      Retention(RetentionPolicy.SOURCE)   -- 注解仅存在于源码中，在class字节码文件中不包含
 *      Retention(RetentionPolicy.CLASS)    -- 默认的保留策略，注解会在class字节码文件中存在，但运行时无法获得，
 *      Retention(RetentionPolicy.RUNTIME)  -- 注解会在class字节码文件中存在，在运行时可以通过反射获取到
 *      
 * 
 * 3. Document：说明该注解将被包含在javadoc中
 * 
 * 
 * 4. Inherited：说明子类可以继承父类中的该注解
 * 
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2014-04-16
 */
@Documented
@Target({ElementType.TYPE ,ElementType.METHOD ,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Xjava
{
    
    /**
     * 对应XJava中的 id 关键字的功能 
     * 
     * 使用形式有两种：
     *   1. 命名注解,  如：Xjava(id="xxx")
     *   2. 无命名注解，如：Xjava
     *      使用这种方式，隐含id为Java的类名称 
     * 
     * 只用于类，即Target(ElementType.TYPE）
     */
    public String id() default "";
    
    
    
    /**
     * 对应XJava中的 ref 关键字的功能 
     * 
     * 此 ref 注解依赖于："id 注解" 或 "XJava配置文件中id关键字实例化的对象"。
     * 即，要么 id 注解必须同时存在，并有效。
     *    要么 XJava id 关键字必须存在，并有效。
     *    
     * 与id类似，也有两种使用形式：
     *   1. 命名注解,  如：Xjava(ref="xxx")
     *   2. 无命名注解，如：Xjava
     *      使用这种方式，隐含ref为注解方法入参数的Java元类名称 
     * 
     * 1.用于setter方法，即Target(ElementType.METHOD）
     * 2.用于属性，      即Target(ElementType.FIELD)
     */
    public String ref() default "";
    
    
    
    /**
     * 对应XJava中的 new 关键字的功能 
     * 
     * 只用于类，即Target(ElementType.TYPE）
     */
    public boolean isNew() default false;
    
    
    
    /**
     * 对应XJava中的实现 BaseInterface 接口的功能
     * 
     * 只用于类，即Target(ElementType.TYPE）
     */
    public XType value() default XType.NULL;
    
}
