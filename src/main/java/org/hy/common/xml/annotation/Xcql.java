package org.hy.common.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * XCQL的注解。用于 xml配置 + Java接口类(无须实现类)的组合实现持久层的功能。
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-06-24
 * @version     v1.0
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Xcql
{
    
    /**
     * Java方法对应的XML配置文件中定义的XCQL对象的ID。即XJava对象池中的XID。
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
     * 定义方法入参与XCQL占位符的对应关系。
     * 
     * 说明方法多个入参依次转为Map集合时的Map.key值。
     * 转为Map集合后，将Map集合为XCQL的执行入参传递。
     * 
     * 可选项。
     * 
     * 只用于方法。
     * 1. 如果设定此属性，应小于等于方法的入参个数，否则报异常。
     * 2. 方法入参个数小于等于1时，可不设定此属性。
     * 3. 方法入参个数大于1时，必须设定此属性，否则报异常。
     * 4. names()[x] 为空字符串时，表示跳过第x个方法入参，x位置的入参将不作为XCQL的执行入参传递。
     * 5. names()[x] 值为"ToMap"时，表示将方法入参转为Map集合后再putAll()整合后的大Map集合中。
     * 
     * 更多丰富的参数配置，建议使用：@Xparam -- org.hy.common.xml.annotation.Xparam
     */
    public String [] names() default {};
    
    
    
    /**
     * 缓存ID。将查询结果集作为内存级的高速缓存使用，此处定义缓存在XJava大对象池中的对象ID。
     * 
     *   当ID对应的对象为null时，才执行XCQL。
     *   当ID对应的对象有值时，直接返回，不再执行数据库查询等操作。
     * 
     * 可选项（与updateCacheID属性成对出现，并且成对的两个cacheID与updateCacheID值相同）。
     * 
     * 只用于方法。
     * 只对查询XCQL生效。
     * 
     * 注：
     *   1. 第二次从缓存中取数据时，方法入参无效，方法入参只对数据库操作的XCQL生效。
     *   2. 标记有缓存ID的方法，其实现类的实现方法，自动带同步锁 synchronized 。
     */
    public String cacheID() default "";
    
    
    
    /**
     * 更新缓存ID。将查询结果集作为内存级的高速缓存使用，此处定义缓存在XJava大对象池中的对象ID。
     * 
     *   无论ID对应的对象是否存在，是否为null，都将执行数据库查询等操作。
     * 
     * 可选项（与cacheID属性成对出现，并且成对的两个cacheID与updateCacheID值相同）。
     * 
     * 只用于方法。
     * 只对查询XCQL生效。
     * 
     * 注：
     *   1. 标记有更新缓存ID的方法，其实现类的实现方法，自动带同步锁 synchronized 。
     */
    public String updateCacheID() default "";
    
    
    
    /**
     * 表示只取查询结果集中的首行记录。即只返回一个对象。
     * 
     * 可选项。
     * 
     * 只用于方法。
     * 只用于查询CQL，并且结果集的类型为List集合。
     * 当用于XCQL组时，须要与 returnID() 属性配合使用，可以获取XCQL组中有 returnID 标记的首行记录
     */
    public boolean returnOne() default false;
    
    
    
    /**
     * 执行数据库操作时的日志，只在执行成功时输出。
     */
    public String log() default "";
    
    
    
    /**
     * 针对DDL、DML等各类操作，当为真时，将按 execute() 方法执行。
     * 
     * 并支持以 XCQL.$Executes_Split 分割的多段CQL的顺次执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @return
     */
    public boolean execute() default false;
    
    
    
    /**
     * 针对MATCH操作，是否开启分页模式。
     * 
     * 当开启分页模式后，将采用 org.hy.common.xcql.XCQL.queryPaging() 获取的分页模板CQL进行查询。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-21
     * @version     v1.0
     *
     * @return
     */
    public boolean paging() default false;
    
    
    
    /**
     * 针对MATCH操作，查询返回第一行第一列上的数值。
     * 
     * 如，用于查询记录总数：  MATCH (n) RETURN COUNT(n)
     * 如，用于查询某一配置项：MATCH (n) RETURN id(n)
     * 
     * 需要被注解的方法返回类型是String、Integer、Long、Double、Float、BigDecimal、Date的，则按第一行第一列数据返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v1.0
     *
     * @return
     */
    public boolean firstValue() default true;
    
}
