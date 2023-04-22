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
 *              v2.0  2018-01-27  添加：batch()属性，支持预解析方式的批量执行。
 *              v3.0  2018-07-21  添加：paging()属性，支持分页查询。建议人：李浩
 *              v4.0  2018-08-08  添加：execute()属性，支持多种类不同的SQL在同一XSQL中执行。
 *              v5.0  2022-05-27  添加：getID()属性，支持自增长ID值的获取
 *              v6.0  2023-04-21  添加：firstValue()属性，针对Select操作，查询返回第一行第一列上的数值。
 *                                优化：batch()属性，支持一行数据的预解释执行
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
     * 4. names()[x] 为空字符串时，表示跳过第x个方法入参，x位置的入参将不作为XSQL、XSQLGroup的执行入参传递。
     * 5. names()[x] 值为"ToMap"时，表示将方法入参转为Map集合后再putAll()整合后的大Map集合中。
     * 
     * 更多丰富的参数配置，建议使用：@Xparam -- org.hy.common.xml.annotation.Xparam
     */
    public String [] names() default {};
    
    
    
    /**
     * 缓存ID。将查询结果集作为内存级的高速缓存使用，此处定义缓存在XJava大对象池中的对象ID。
     * 
     *   当ID对应的对象为null时，才执行XSQL、XSQLGroup。
     *   当ID对应的对象有值时，直接返回，不再执行数据库查询等操作。
     * 
     * 可选项（与updateCacheID属性成对出现，并且成对的两个cacheID与updateCacheID值相同）。
     * 
     * 只用于方法。
     * 只对查询XSQL，及XSQLGroup生效。
     * 
     * 注：
     *   1. 第二次从缓存中取数据时，方法入参无效，方法入参只对数据库操作的XSQL、XSQLGroup生效。
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
     * 只对查询XSQL，及XSQLGroup生效。
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
     * 只用于查询SQL，并且结果集的类型为List集合。
     * 当用于XSQL组时，须要与 returnID() 属性配合使用，可以获取XSQL组中有 returnID 标记的首行记录
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
    
    
    
    /**
     * 执行数据库操作时的日志，只在执行成功时输出。
     */
    public String log() default "";
    
    
    
    /**
     * 针对Insert、Update、Delete操作，是否按批量的预解析方式(prepareStatement)执行SQL。
     * 
     * 当方法入参类型为List集合
     *    1. batch = false 时，按普通批量方式执行，统一提交及回滚。如果XSQL设置了batchCommit属性，可再分批提交。
     *    2. batch = true  时，按预解析的方式执行，统一提交及回滚。如果XSQL设置了batchCommit属性，可再分批提交。
     * 
     * 当方法入参类型非List集合
     *    1. batch = false 时，按一行数据的普通方式执行
     *    2. batch = true  时，按一行数据的预解析方式执行
     * 
     * 上面两种方式，在写SQL模板时，略有不同。
     */
    public boolean batch() default false;
    
    
    
    /**
     * 针对DDL、DML等各类操作，当为真时，将按 execute() 方法执行。
     * 
     * 并支持以 XSQL.$Executes_Split 分割的多段SQL的顺次执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @return
     */
    public boolean execute() default false;
    
    
    
    /**
     * 针对Select操作，是否开启分页模式。
     * 
     * 当开启分页模式后，将采用 org.hy.common.xml.XSQL.queryPaging() 获取的分页模板SQL进行查询。
     * 同时，请符合 org.hy.common.xml.XSQLPaging.xml 中的分页要求。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-21
     * @version     v1.0
     *
     * @return
     */
    public boolean paging() default false;
    
    
    
    /**
     * 针对Select操作，查询返回第一行第一列上的数值。
     * 
     * 如，用于查询记录总数：  SELECT COUNT(1) FROM Dual
     * 如，用于查询某一配置项：SELECT SYSDATE  FROM Dual
     * 
     * 需要被注解的方法返回类型是String、Integer、Double、Float、BigDecimal、Date的，则按第一行第一列数据返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-04-20
     * @version     v1.0
     *
     * @return
     */
    public boolean firstValue() default true;
    
    
    
    /**
     * 针对Insert操作，是否返回数据库表上绑定的自增长ID的值。
     * 
     * 两类定义冲突时的解决方案：
     *   1. 当 Xsql.getID() = true  时，实例对象 XSQL.isGetID() = false 时，
     *      因为注解是写在Java中的，并且编释后其值固定不变，所以本类权限优先级是最高的，按本类getID尝试获取自增长ID的值。
     * 
     *   2. 当 Xsql.getID() = false 时，实例对象 XSQL.isGetID() = true  时，
     *      因为本类getID默认值为false，所以按实例对象的 XSQL.isGetID() 参数为准，控制尝试获取自增长ID的值
     * 
     * 显式定义
     *      使用本属性定义，显式定义尝试获取自增长ID的值。如：
     *      @Xsql(id="xxx" ,getID=true)
     *      public int yyy();             // 返回类型为 int 时，主要用于单记录添加时返回自增长ID
     * 
     * 隐式定义
     *      接口方法的返回值类型为 XSQLData 或 List<Integer> 时，并且为Insert语句时，也将尝试获取自增长ID的值。如：
     *      @xsql("xxx")
     *      public XSQLData yyy();        // 返回类型为 XSQLData 时，主要用于批量添加时返回所有自增长ID集合，并返回影响的记录总行数
     * 
     *      或
     * 
     *      @xsql("xxx")
     *      public List<Integer> yyy();   // 返回类型为 List<Integer> 时，主要用于批量添加时返回自增长ID集合
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-05-27
     * @version     v1.0
     *
     * @return
     */
    public boolean getID() default false;
    
}
