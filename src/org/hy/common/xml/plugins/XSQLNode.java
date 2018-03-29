package org.hy.common.xml.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.hy.common.CycleNextList;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.StringHelp;
import org.hy.common.net.ClientSocket;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;





/**
 * XSQL的组合的节点。与 org.hy.common.xml.plugins.XSQLGroup 配合使用。
 * 
 *   1. 对条件占位符命名无大小写要求
 *   2. 当占位符对应的值为null时，设置在FelContext.set()中的值为"NULL"
 *   3. XSQL组执行的Java方法的定义模板 @see org.hy.common.xml.plugins.XSQLGroupExecuteJava
 * 
 * @author      ZhengWei(HY)
 * @createDate  2016-01-20
 * @version     v1.0
 *              v2.0  2016-03-03  1.添加：操作后提交：执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作
 *                                2.添加：整体操作后的收尾操作：在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
 *                                3.添加：组嵌套组的执行：实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
 *                                    3.1 每个XSQLGroup组，除了执行参数、返回结果集、累计总行数、异常是相互传递的外，还有十分重要的数据库连接池控制，也是统一的、相互传递的。
 *                                    3.2 相互传递统一的数据库连接池控制(提交、回滚、关闭)。即，后一个XSQLGroup组的回滚、异常是会影响到前一个XSQLGroup组的数据库操作的。
 *              v3.0  2016-03-14  1.添加：返回查询结果集：returnID标记的查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
 *                                    1.1 此类查询XSQL节点，不再控制其后节点的执行次数。
 *              v4.0  2016-03-22  1.添加：查询结果当作其后节点的SQL入参的同时，还返回查询结果：queryReturnID标记的查询XSQL节点，在循环遍历的同时，还返回所有行数据结果。
 *                                    1.1 将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
 *                                    1.2 当 this.returnID 有值时，this.queryReturnID 将失效（不起作用）。
 *                                    1.3 并且只有每次循环中的所有节点都执行成功后，才 PartitionMap.putRows(Map<String ,Object>) 一行查询记录。
 *              v5.0  2016-05-17  1.添加节点类型：执行Java代码。即可以对查询结果集进行二次加工处理等Java操作。
 *              v6.0  2016-07-30  1.添加节点类型：XSQLGroup.execute()方法的入参中的集合，转换角色为数据库查询结果。
 *              v7.0  2016-08-02  1.添加节点类型：XSQLGroup.execute()方法的入参中的集合，整体批量的写入的数据库中。
 *              v8.0  2017-05-05  1.添加：this.oneConnection 查询SQL数据库连接的占用模式。
 *              v9.0  2017-05-17  1.添加：this.returnQuery，针对 this.returnID 属性，定义返回查询结果集是 "返回结果集"？还是 "查询并返回"。
 *                                  提供一种好理解的数据结构(与this.queryReturnID属性返回的数据结构相比)。
 *                                  此建议来自于：向以前同学
 *                                2.准备放弃this.queryReturnID属性，只少是不再建议使用此属性。
 *              v10.0 2017-05-23  1.添加：节点的执行条件this.condition中的占位符支持xx.yy.ww的面向对象的形式。此建议来自于：向以前同学
 *              v11.0 2017-12-22  1.添加：XSQL组执行的Java方法的入参参数中增加控制中心XSQLGroupControl，实现事务统一提交、回滚。
 *                                2.添加：XSQLNode.isNoUpdateRollbacks()方法，当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作。
 *              v11.1 2018-01-24  1.优化：isPass()方法中调用Fel计算时异常，显示更详细的异常日志(输出Fel表达式)
 *                                2.添加：多线程等待threadWait属性，可自由定义在哪个节点上等待所有线程均执行完成。
 *                                       同时，根节点XSQL组及所有子节点XSQL组均共享一个多线程任务组，且只有一个多线程任务组。
 *                                       配合递归功能，不再创建多个多线程任务组，
 *                                       递归时重复创建多个多线程任务组，会造成线程资源使用殆尽，出现死锁。
 *                                3.添加：自主、自由的数据库连接freeConnection属性。
 *                                       节点使用的数据库连接不再由XSQLGroup控制及管理。
 *                                       由节点自行打开一个独立的数据库连接，并自行控制提交、回滚。
 *                                       主要用于多线程的并发写操作。
 *                                4.添加：多线程监控完成情况的时间间隔threadWaitInterval属性。
 *                                       由原先的固定值改为可由用户自行调整的属性。
 *              v12.0 2018-01-30  1.添加：支持多台服务器并行计算。
 *              v12.1 2018-02-22  1.修复：云计算时，某台服务器异常后，修复"云等待"死等的问题。
 *                                2.添加：云计算异常时，尝试交给其它云服务计算。当重试多次(this.cloudRetryCount)云计算仍然异常时，放弃计算。
 *              v13.0 2018-03-05  1.添加：执行异常时重试XSQLNode.retryCount功能。
 *              v13.1 2018-03-08  1.添加：执行异常时重试等待的时间间隔XSQLNode.retryInterval功能。
 *              v13.2 2018-03-29  1.添加：针对具体XSQL节点的Java断言调试功能。方面问题的定位。
 */
public class XSQLNode
{
    /** 
     * 节点类型：查询。
     * 
     * 即查询数据，并将查询结果做为其它节点(XSQLNode)的查询参数 
     */
    public static final String  $Type_Query                      = "Query";
    
    /** 
     * 节点类型： 执行（DML）。如Insert、Update、Delete等
     * 
     *  即执行数据库操作，不获取操作结果。执行参数由外界或其它查询类型的节点(XSQLNode)提供
     */
    public static final String  $Type_ExecuteUpdate              = "DMLExecute";
    
    /** 
     * 节点类型： 执行（包含DML、DDL、DCL、TCL）
     * 
     *  即执行数据库操作，不获取操作结果。执行参数由外界或其它查询类型的节点(XSQLNode)提供。
     *  
     *  注意：此类型的数据库连接与freeConnection同义，不再由XSQL组控制及管理。每次执行均使用独立的数据库连接。
     */
    public static final String  $Type_Execute                    = "Execute";
    
    /**
     * 节点类型：执行Java代码
     *  
     *   即执行Java代码，主要用于对查询结果集的二次加工处理等操作。
     *   相关针对性属性有：
     *     1. xjavaID
     *     2. methodName
     *     3. cloudServers      云计算
     *     4. cloudServersList  云计算
     *     
     * 方法的定义模板 @see org.hy.common.xml.plugins.XSQLGroupExecuteJava
     */
    public static final String  $Type_ExecuteJava                = "ExecuteJava";
    
    /**
     * 节点类型：XSQLGroup.execute()方法的入参中的Java集合对象，转换角色为数据库查询结果
     * 
     *  1. 不查询数据库，直接将参数当查询结果来用。
     *  2. 与 $Type_Query 一样，同样控制其后XSQL节点的循环次数
     *  3. 参数类型必须为：List、Set、Map(会通过Hetp.toList()转为List)
     *  4. 也可是入参的某一个集合属性，当做查询结果来用。
     *  
     *  配合属性为：this.collectionID
     *  
     *  相关失效的属性有：
     *    1. this.lastOnce
     *    2. this.returnID
     *    3. this.returnAppend
     *    4. this.queryReturnID
     *    5. this.xjavaID
     *    6. this.sqlGroup
     *    7. this.sql
     */
    public static final String  $Type_CollectionToQuery          = "CollectionToQuery";
    
    /**
     * 节点类型：XSQLGroup.execute()方法的入参中的集合，整体批量的写入的数据库中。
     * 
     *   1. 此节点为Insert、Update、Delete更新数据库操作。
     *   2. 与 $Type_ExecuteUpdate 类似，是它的批量操作。
     *   3. 参数类型必须为：List、Set、Map(会通过Hetp.toList()转为List)
     *   4. 也可是入参的某一个集合属性，当做查询结果来用。
     *   
     *   配合属性为：this.collectionID
     */
    public static final String  $Type_CollectionToExecuteUpdate  = "CollectionToExecuteUpdate";
    
    
    
    /** 节点类型。默认为：执行类型（包含DML、DDL、DCL、TCL） */
    private String                       type;
    
    /** 操作SQL对象 */
    private XSQL                         sql;
    
    /** 
     * 执行前的前提条件，只有当满足这个条件后，XSQL才会被执行
     * 
     * 形式为带占位符的Fel条件，如：:c01=='1' && :c02=='2'
     * 
     * 为空时，表示任何情况下都允许执行
     */
    private String                       condition;
    
    /** 
     * 解释出来的Fel条件。与this.condition的区别是：它是没有占位符
     * 
     * 如：c01=='1' && c02=='2'
     */
    private String                       conditionFel;
    
    /**
     * 占位符信息的集合
     * 
     * Map.key    为占位符。前缀为:符号
     * Map.Value  为占位符原文本信息
     */
    private Map<String ,Object>          placeholders;
    
    /**
     * 当检查不通过时，是否允许其后的XSQL节点执行。默认为：true
     * 
     * 当为 true 时，检查只影响自己的节点，不影响其它及其后XSQL节点的执行。
     */
    private boolean                      noPassContinue;
    
    /** 执行本节点前，对之前的所有XSQL节点进行统一提交操作。默认为：false */
    private boolean                      beforeCommit;
    
    /**
     * 专用于执行类型的XSQL节点。
     * 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作。默认为：false 
     */
    private boolean                      afterCommit;
    
    /** 
     * 专用于查询类型的XSQL节点。
     * 除获取查询结果集的首条记录外，每获取结果集一条记录前，对上一条记录产生的一系列数据库操作，做一次统一提交操作。 
     * 默认为：false
     */
    private boolean                      perAfterCommit;
    
    /**
     * 专用于更新类型的XSQL节点($Type_ExecuteUpdate、$Type_CollectionToExecuteUpdate)。
     * 
     * 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作。
     * 1. noUpdateRollbacks=true时，操作影响的数据量为0条时，回滚。
     * 2. 事务统一回滚后，XSQL组将按整体执行失败处理。
     * 
     * 默认为：false
     */
    private boolean                      noUpdateRollbacks;
    
    /** 
     * 在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
     * 即，标记为lastOnce=true的节点，不在查询类型XSQL节点的循环之中执行。
     * 默认为：false
     */
    private boolean                      lastOnce;
    
    /** 
     * 返回查询结果集
     * 
     * 专用于查询类型的XSQL节点。
     * 查询结果集的标示ID。区分大小写。
     *    1. this.returnAppend=false时，多个相同标示ID，会相互覆盖。
     *    2. this.returnAppend=true时， 多个相同标示ID，会向首个有值的结果集对象中追加其它结果集对象。
     *    查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
     *    将结果集对象保存在类似的形式中： Map.key = this.returnID ,Map.value = 结果集对象。
     * 
     * 此类查询XSQL节点，不再控制其后节点的执行次数。
     * 默认为：null
     * 
     * 建议与 this.lastOnce 属性配合使用。如果在控制类循环的查询SQL中，会被执行多次 Map.put(this.returnID ,结果集)。
     */
    private String                       returnID;
    
    /**
     * 针对 this.returnID 属性，定义返回查询结果集是 "追加方式"？还是 "覆盖方式"。
     *   1. 在追加方式下，相同标示returnID对应查询结果集的Java类型应当是一致的。
     *   2. 目前只支持对查询结果集是 Map、List、Set 三种接口实现类的追加方式。
     *   3. 其它查询结果集还是采用覆盖方式。
     * 默认为：false（覆盖方式）
     */
    private boolean                      returnAppend;
    
    /**
     * 针对 this.returnID 属性，定义返回查询结果集是 "返回结果集"？还是 "查询并返回"。
     *   1. 返回结果集：只返回结果集，不再控制其后XSQL节点的执行次数。
     *   2. 查询并返回：返回结果集，控制其后节点执行：返回结果集的同时，还将控制其后XSQL节点的执行次数。
     * 默认为：false（返回结果集）
     * 
     * 与 queryReturnID功能是一样。还添加此功能的原因是：queryReturnID返回的结果集的数据结构不好被理解。
     * 
     * ZhengWei(HY) Add 2017-05-17
     */
    private boolean                      returnQuery;
    
    /**
     * 查询结果当作其后节点的SQL入参的同时，还返回查询结果
     * 
     * 专用于查询类型的XSQL节点。
     * 查询结果集的标示ID。区分大小写。多个相同标示ID，会相互覆盖。
     *   当 this.returnID 有值时，this.queryReturnID 将失效（不起作用）。
     *   
     * 此类查询XSQL节点为控制其后节点执行次数的节点，与 this.returnID 刚好相反。
     * 默认为：null
     * 
     * 将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
     *   PartitionMap.分区为：  数据库表的字段名称 
     *   PartitionMap.行记录为：数据库表字段对应的数值
     *   
     * 可应用于：在循环中多次生成数个主键ID，并将所有生成的主键ID统一返回的功能。
     * 
     * 2017-05-17 准备放弃this.queryReturnID属性，只少是不再建议使用此属性。
     *            用 this.returnQuery 属性代替。原因是，this.returnQuery 返回的数据结构更好被理解。
     */
    private String                       queryReturnID;
    
    /** 
     * 实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
     * 
     * 如果将 XSQLGroup组，比喻为Java方法，那嵌套另一个XSQLGroup组，就相当于Java方法中调用另一个方法。
     * 
     * 当 sqlGroup 生效(不为空)时，只有 condition、noPassContinue、lastOnce 三个属性依然有作用。
     * 其它属性全部都将失效，包括 this.sql 也同时失效。
     */
    private XSQLGroup                    sqlGroup;
    
    /** 注解说明。当开启日志模式(XSQLGroup.isLog)时，此注解说明也会被同时输出。 */
    private String                       comment;
    
    /**
     * 异常重试次数。执行SQL语句，因争抢锁等原因异常时，尝试重新执行。当重试多次仍然异常时，放弃执行。
     * 
     * 等于0时，表示异常时，不尝试重新执行。
     * 
     * 默认为：0次
     */
    private int                          retryCount;
    
    /**
     * 异常重试的时间间隔(单位：毫秒)。
     * 
     * 第1次异常时，等待时长为：5秒时，
     * 第2次异常时，等待时长为：10秒，是上次等待时长的翻倍。
     * 第3次异常时，等待时长为：20秒，是上次等待时长的翻倍。
     * 
     * 与 this.异常重试次数 属性配合使用。
     * 
     * 默认为5 * 1000 = 5秒
     */
    private long                         retryInterval;
    
    /**
     * 云计算服务器的列表。用英文逗号,分隔（可以有空格、回车符、制表符）。如下形式：IP1:Port1 ,IP2:Port2 ,IP3:Port3。
     * 
     * 与 $Type_ExecuteJava、xjavaID、methodName配合使用。
     * 
     * 云计算服务器的列表，可以包含自己。
     * 
     * 注1：所有通讯数据对象及关联对象，均须实际序列化接口java.io.Serializable。 
     * 注2：methodName方法入参形式与本地执行Java方法不同。方法形式如下：
     *     public boolean 方法名称(Map<String ,Object> i_Params) {}
     *        
     *     入参i_Params与XSQLGroupExecuteJava.executeJava_XSQLNode()方法中的io_Params入参相同，但只是只读的。
     */
    private String                       cloudServers;
    
    /**
     * 与 cloudServers 同义，并且 cloudServers 最终将转成 cloudServersList。
     * 
     * 但功能上与cloudServers略有差异：
     *    当多个XSQL组共用一组云计算服务列表时，云计算服务器可在多个XSQL组间保持一定负载均衡的功能。
     */
    private CycleNextList<XSQLNodeCloud> cloudServersList;
    
    /**
     * 云计算异常重试次数。云计算异常时，尝试交给其它云服务重新计算。当重试多次云计算仍然异常时，放弃计算。
     * 
     * 等于0时，表示云计算异常时，不尝试重新计算。
     * 
     * 默认为：3次
     */
    private int                          cloudRetryCount;
    
    /**
     * 云服务计算异常的服务器数量。当异常时，其服务器仍然标记为"繁忙"
     */
    private int                          cloudErrorCount;
    
    /**
     * 云服务正在运算（或繁忙）的服务器数量
     */
    private int                          cloudBusyCount;
    
    /**
     * 等待哪个节点上的云服务计算完成
     */
    private XSQLNode                     cloudWait;
    
    /**
     * 监控云服务计算完成情况的时间间隔(单位：毫秒)。
     * 
     * 与 this.cloudWait 属性配合使用。
     * 
     * 默认为5 * 1000 = 5秒
     */
    private long                         cloudWaitInterval;
    
    /**
     * 云服务并行发起计算动作时，间隔发起计算的时间间隔(单位：毫秒)。
     * 
     * 默认为 66毫秒
     */
    private long                         cloudExecInterval;
    
    /** 
     * XJava对象标识
     *    构建XSQLNode对象实例时，xjavaID标记的对象实例，可以是不存在的（或尚未构建的）。
     *    只要在执行时存在就OK了 
     */
    private String                       xjavaID;
    
    /** 
     * XJava对象执行的方法名
     *      方法的定义形式为：详见org.hy.common.xml.plugins.XSQLGroupExecuteJava
     *      
     * 默认名称为：executes。方便云计算时，执行云端的XSQL组，只须用户配置xjavaID即可。
     */
    private String                       methodName;
    
    /** 解释好的XJava对象实例。为了性能而存在，在反复循环中，只解释一次就好 */
    private Object                       xjavaIntance;
    
    /** 解释好的XJava对象方法。为了性能而存在，在反复循环中，只解释一次就好 */
    private Method                       xjavaMethod;
    
    /**
     * 集合ID
     * 
     * 只对 $Type_CollectionToQuery、$Type_CollectionToDB 两个类型有效。标识要转换角色的集合ID。
     * 当为空时，表示将整个 XSQLGroup.execute() 方法的入参当为查询结果来用（前提是：入参类型必须为：List、Set、Map(会通过Hetp.toList()转为List)）。
     * 
     * 支持xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释
     */
    private String                       collectionID;
    
    /**
     * 是否为多线程并且发执行。默认值：false。
     * 
     * 只对 $Type_Query、$Type_CollectionToQuery 两个类型有效。
     * 
     * 注：当thread设置为ture时，threadWait默认也设置为true，
     *    表示同一节点发起的多线程任务的同时，也在同一节点等待所有线程执行完成。
     */
    private boolean                      thread;
    
    /**
     * 当发起多线程时，标记哪个节点等待所有线程均执行完成。与 this.thread 配合使用。默认值：false。
     * 
     * 当某一节点设置为等待(this.threadWait=true)时，将在此节点的SQL语句执行完成后执行等待。
     */
    private boolean                      threadWait;
    
    /**
     * 监控所有线程完成情况的时间间隔(单位：毫秒)。
     * 
     * 与 this.threadWait 属性配合使用。
     * 
     * 默认为0值，表示取时间间隔为：ThreadPool.getIntervalTime() * 3
     */
    private long                         threadWaitInterval;
    
    /**
     * 自主、自由的数据库连接。
     * 
     * 当为true时，本节点的数据库连接不再由XSQLGroup控制及管理。
     * 由节点自行打开一个独立的数据库连接，并自行控制提交、回滚。
     * 
     * 主要用于多线程的并发写操作。
     * 
     * 只对 $Type_ExecuteUpdate、$Type_Execute、$Type_CollectionToExecuteUpdate 三个类型有效。
     * 
     * 默认为：false，即：由XSQLroup控制及管理数据库连接 
     */
    private boolean                      freeConnection;
    
    /**
     * 对于查询语句有两种使用数据库连接的方式
     *   1. 读写分离：每一个查询SQL均占用一个新的连接，所有的更新修改SQL共用一个连接。this.oneConnection = false，默认值。
     *   2. 读写同事务：查询SQL与更新修改SQL共用一个连接，做到读、写在同一个事务中进行。
     *   
     * 只对 $Type_Query 类型有效。
     */
    private boolean                      oneConnection;
    
    /** 是否采用大数据模式控制循环遍历 */
    private boolean                      bigData;
    
    /** 是否断言(assert)调试 */
    private boolean                      debug;
    
    
    
    public XSQLNode()
    {
        this.noPassContinue     = true;
        this.beforeCommit       = false;
        this.afterCommit        = false;
        this.perAfterCommit     = false;
        this.noUpdateRollbacks  = false;
        this.lastOnce           = false;
        this.returnID           = null;
        this.returnAppend       = false;
        this.returnQuery        = false;
        this.queryReturnID      = null;
        this.sqlGroup           = null;
        this.retryCount         = 0;
        this.retryInterval      = 5 * 1000;
        this.cloudServers       = null;
        this.cloudServersList   = null;
        this.cloudRetryCount    = 3;
        this.cloudErrorCount    = 0;
        this.cloudBusyCount     = 0;
        this.cloudWait          = null;
        this.cloudWaitInterval  = 5 * 1000;
        this.cloudExecInterval  = 66;
        this.xjavaID            = null;
        this.methodName         = "executes";
        this.xjavaIntance       = null;
        this.xjavaMethod        = null;
        this.collectionID       = null;
        this.thread             = false;
        this.threadWait         = false;
        this.threadWaitInterval = 0;
        this.freeConnection     = false;
        this.oneConnection      = false;
        this.bigData            = false;
        this.debug              = false;
    }
    
    
    
    /**
     * 获取：节点类型。默认为：执行类型（包含DML、DDL、DCL、TCL）
     */
    public String getType()
    {
        return Help.NVL(this.type ,$Type_Execute);
    }

    
    
    /**
     * 设置：节点类型。默认为：执行类型（包含DML、DDL、DCL、TCL）
     * 
     * @param type 
     */
    public void setType(String type)
    {
        this.type = type;
    }


    
    /**
     * 获取：操作SQL对象
     */
    public XSQL getSql()
    {
        return sql;
    }


    
    /**
     * 设置：操作SQL对象
     * 
     * @param sql 
     */
    public void setSql(XSQL sql)
    {
        this.sql = sql;
    }


    
    /**
     * 获取：执行前的前提条件，只有当满足这个条件后，XSQL才会被执行
     * 
     * 形式为带占位符的Fel条件，如：:c01=='1' && :c02=='2'
     */
    public String getCondition()
    {
        return condition;
    }



    
    /**
     * 设置：执行前的前提条件，只有当满足这个条件后，XSQL才会被执行
     * 
     * 形式为带占位符的Fel条件，如：:c01=='1' && :c02=='2'
     * 
     * @param i_Condition 
     */
    public void setCondition(String i_Condition)
    {
        this.condition    = i_Condition;
        this.conditionFel = i_Condition;
        this.placeholders = null;
        
        if ( !Help.isNull(this.condition) )
        {
            this.placeholders = StringHelp.parsePlaceholders(this.condition);
            
            for (String v_Key : this.placeholders.keySet())
            {
                this.conditionFel = StringHelp.replaceAll(this.conditionFel ,":" + v_Key ,StringHelp.replaceAll(v_Key ,"." ,"_"));
            }
        }
    }
    
    
    
    /**
     * 获取：当检查不通过时，是否允许其后的XSQL节点执行。默认为：true
     * 
     * 当为 true 时，检查只影响自己这个节点，不影响其它及其后的节点。
     */
    public boolean isNoPassContinue()
    {
        return noPassContinue;
    }


    
    /**
     * 设置：当检查不通过时，是否允许其后的XSQL节点执行。默认为：true
     * 
     * 当为 true 时，检查只影响自己这个节点，不影响其它及其后的节点。
     * 
     * @param noPassContinue 
     */
    public void setNoPassContinue(boolean noPassContinue)
    {
        this.noPassContinue = noPassContinue;
    }
    
    
    
    /**
     * 获取：执行本节点前，对之前的所有XSQL节点进行统一提交操作。默认为：false
     */
    public boolean isBeforeCommit()
    {
        return beforeCommit;
    }

    
    
    /**
     * 设置：执行本节点前，对之前的所有XSQL节点进行统一提交操作。默认为：false
     * 
     * @param beforeCommit 
     */
    public void setBeforeCommit(boolean beforeCommit)
    {
        this.beforeCommit = beforeCommit;
    }

    
    
    /**
     * 获取：专用于执行类型的XSQL节点。
     * 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作。默认为：false
     */
    public boolean isAfterCommit()
    {
        return afterCommit;
    }



    /**
     * 设置：专用于执行类型的XSQL节点。
     * 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作。默认为：false
     * 
     * @param afterCommit 
     */
    public void setAfterCommit(boolean afterCommit)
    {
        this.afterCommit = afterCommit;
    }



    /**
     * 获取：专用于查询类型的XSQL节点。
     * 除获取查询结果集的首条记录外，每获取结果集一条记录前，对上一条记录产生的一系列数据库操作，做一次统一提交操作。 
     * 默认为：false
     */
    public boolean isPerAfterCommit()
    {
        return perAfterCommit;
    }


    
    /**
     * 设置：专用于查询类型的XSQL节点。
     * 除获取查询结果集的首条记录外，每获取结果集一条记录前，对上一条记录产生的一系列数据库操作，做一次统一提交操作。 
     * 默认为：false
     * 
     * @param perAfterCommit 
     */
    public void setPerAfterCommit(boolean perAfterCommit)
    {
        this.perAfterCommit = perAfterCommit;
    }
    
    
    
    /**
     * 获取：专用于更新类型的XSQL节点($Type_ExecuteUpdate、$Type_CollectionToExecuteUpdate)。
     * 
     * 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作。
     * 1. noUpdateRollbacks=true时，操作影响的数据量为0条时，回滚。
     * 2. 事务统一回滚后，XSQL组将按整体执行失败处理。
     * 
     * 默认为：false
     */
    public boolean isNoUpdateRollbacks()
    {
        return this.noUpdateRollbacks;
    }
    
    
    
    /**
     * 设置：专用于更新类型的XSQL节点($Type_ExecuteUpdate、$Type_CollectionToExecuteUpdate)。
     * 
     * 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作。
     * 1. noUpdateRollbacks=true时，操作影响的数据量为0条时，回滚。
     * 2. 事务统一回滚后，XSQL组将按整体执行失败处理。
     * 
     * 默认为：false
     * 
     * @param perAfterCommit 
     */
    public void setNoUpdateRollbacks(boolean i_NoUpdateRollbacks)
    {
        this.noUpdateRollbacks = i_NoUpdateRollbacks;
    }


    
    /**
     * 获取：在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
     * 即，标记为lastOnce=true的节点，不在查询类型XSQL节点的循环之中执行。
     * 默认为：false
     */
    public boolean isLastOnce()
    {
        return lastOnce;
    }


    
    /**
     * 设置：在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
     * 即，标记为lastOnce=true的节点，不在查询类型XSQL节点的循环之中执行。
     * 默认为：false
     * 
     * @param lastOnce 
     */
    public void setLastOnce(boolean lastOnce)
    {
        this.lastOnce = lastOnce;
    }

    
    
    /**
     * 获取：返回查询结果集
     * 
     * 专用于查询类型的XSQL节点。
     * 查询结果集的标示ID。区分大小写。
     *    1. this.returnAppend=false时，多个相同标示ID，会相互覆盖。
     *    2. this.returnAppend=true时， 多个相同标示ID，会向首个有值的结果集对象中追加其它结果集对象。
     *    查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
     *    将结果集对象保存在类似的形式中： Map.key = this.returnID ,Map.value = 结果集对象。
     * 
     * 此类查询XSQL节点，不再控制其后节点的执行次数。
     * 默认为：null
     * 
     * 建议与 this.lastOnce 属性配合使用。如果在控制类循环的查询SQL中，会被执行多次 Map.put(this.returnID ,结果集)。
     */
    public String getReturnID()
    {
        return returnID;
    }


    
    /**
     * 设置：返回查询结果集
     * 
     * 专用于查询类型的XSQL节点。
     * 查询结果集的标示ID。区分大小写。
     *    1. this.returnAppend=false时，多个相同标示ID，会相互覆盖。
     *    2. this.returnAppend=true时， 多个相同标示ID，会向首个有值的结果集对象中追加其它结果集对象。
     *    查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
     *    将结果集对象保存在类似的形式中： Map.key = this.returnID ,Map.value = 结果集对象。
     * 
     * 此类查询XSQL节点，不再控制其后节点的执行次数。
     * 默认为：null
     * 
     * 建议与 this.lastOnce 属性配合使用。如果在控制类循环的查询SQL中，会被执行多次 Map.put(this.returnID ,结果集)。
     * 
     * @param returnID 
     */
    public void setReturnID(String returnID)
    {
        this.returnID = returnID;
    }


    
    /**
     * 获取：针对 this.returnID 属性，定义返回查询结果集是 "追加方式"？还是 "覆盖方式"。
     *   1. 在追加方式下，相同标示returnID对应查询结果集的Java类型应当是一致的。
     *   2. 目前只支持对查询结果集是 Map、List、Set 三种接口实现类的追加方式。
     *   3. 其它查询结果集还是采用覆盖方式。
     * 默认为：false（覆盖方式）
     */
    public boolean isReturnAppend()
    {
        return returnAppend;
    }


    
    /**
     * 设置：针对 this.returnID 属性，定义返回查询结果集是 "追加方式"？还是 "覆盖方式"。
     *   1. 在追加方式下，相同标示returnID对应查询结果集的Java类型应当是一致的。
     *   2. 目前只支持对查询结果集是 Map、List、Set 三种接口实现类的追加方式。
     *   3. 其它查询结果集还是采用覆盖方式。
     * 默认为：false（覆盖方式）
     * 
     * @param isReturnAppend 
     */
    public void setReturnAppend(boolean returnAppend)
    {
        this.returnAppend = returnAppend;
    }


    
    /**
     * 获取：针对 this.returnID 属性，定义返回查询结果集是 "返回结果集"？还是 "查询并返回"。
     *   1. 返回结果集：只返回结果集，不再控制其后XSQL节点的执行次数。
     *   2. 查询并返回：返回结果集，控制其后节点执行：返回结果集的同时，还将控制其后XSQL节点的执行次数。
     * 默认为：false（返回结果集）
     * 
     * 与 queryReturnID功能是一样。还添加此功能的原因是：queryReturnID返回的结果集的数据结构不好被理解。
     * 
     * ZhengWei(HY) Add 2017-05-17
     */
    public boolean isReturnQuery()
    {
        return returnQuery;
    }


    
    /**
     * 设置：针对 this.returnID 属性，定义返回查询结果集是 "返回结果集"？还是 "查询并返回"。
     *   1. 返回结果集：只返回结果集，不再控制其后XSQL节点的执行次数。
     *   2. 查询并返回：返回结果集，控制其后节点执行：返回结果集的同时，还将控制其后XSQL节点的执行次数。
     * 默认为：false（返回结果集）
     * 
     * 与 queryReturnID功能是一样。还添加此功能的原因是：queryReturnID返回的结果集的数据结构不好被理解。
     * 
     * ZhengWei(HY) Add 2017-05-17
     * 
     * @param returnQuery 
     */
    public void setReturnQuery(boolean returnQuery)
    {
        this.returnQuery = returnQuery;
    }



    /**
     * 获取：查询结果当作其后节点的SQL入参的同时，还返回查询结果
     * 
     * 专用于查询类型的XSQL节点。
     * 查询结果集的标示ID。区分大小写。多个相同标示ID，会相互覆盖。
     *   当 this.returnID 有值时，this.queryReturnID 将失效（不起作用）。
     *   
     * 此类查询XSQL节点为控制其后节点执行次数的节点，与 this.returnID 刚好相反。
     * 默认为：null
     * 
     * 将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
     *   PartitionMap.分区为：  数据库表的字段名称 
     *   PartitionMap.行记录为：数据库表字段对应的数值
     *   
     * 可应用于：在循环中多次生成数个主键ID，并将所有生成的主键ID统一返回的功能。
     */
    public String getQueryReturnID()
    {
        return queryReturnID;
    }


    
    /**
     * 设置：查询结果当作其后节点的SQL入参的同时，还返回查询结果
     * 
     * 专用于查询类型的XSQL节点。
     * 查询结果集的标示ID。区分大小写。多个相同标示ID，会相互覆盖。
     *   当 this.returnID 有值时，this.queryReturnID 将失效（不起作用）。
     *   
     * 此类查询XSQL节点为控制其后节点执行次数的节点，与 this.returnID 刚好相反。
     * 默认为：null
     * 
     * 将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
     *   PartitionMap.分区为：  数据库表的字段名称 
     *   PartitionMap.行记录为：数据库表字段对应的数值
     *   
     * 可应用于：在循环中多次生成数个主键ID，并将所有生成的主键ID统一返回的功能。
     * 
     * @param queryReturnID 
     */
    public void setQueryReturnID(String queryReturnID)
    {
        this.queryReturnID = queryReturnID;
    }



    /**
     * 获取：实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
     * 
     * 如果将 XSQLGroup组，比喻为Java方法，那嵌套另一个XSQLGroup组，就相当于Java方法中调用另一个方法。
     * 
     * 当 sqlGroup 生效(不为空)时，只有 this.condition、this.noPassContinue 两个属性依然有作用。
     * 其它属性全部都将失效，包括 this.sql 也同时失效。
     */
    public XSQLGroup getSqlGroup()
    {
        return sqlGroup;
    }


    
    /**
     * 设置：实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
     * 
     * 如果将 XSQLGroup组，比喻为Java方法，那嵌套另一个XSQLGroup组，就相当于Java方法中调用另一个方法。
     * 
     * 当 sqlGroup 生效(不为空)时，只有 this.condition、this.noPassContinue 两个属性依然有作用。
     * 其它属性全部都将失效，包括 this.sql 也同时失效。
     * 
     * @param sqlGroup 
     */
    public void setSqlGroup(XSQLGroup sqlGroup)
    {
        this.sqlGroup = sqlGroup;
    }


    
    /**
     * 获取：注解说明。当开启日志模式(XSQLGroup.isLog)时，此注解说明也会被同时输出。
     */
    public String getComment()
    {
        return comment;
    }


    
    /**
     * 设置：注解说明。当开启日志模式(XSQLGroup.isLog)时，此注解说明也会被同时输出。
     * 
     * @param comment 
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    
    
    /**
     * 云计算服务器的列表。用英文逗号,分隔（可以有空格、回车符、制表符）。如下形式：IP1:Port1 ,IP2:Port2 ,IP3:Port3。
     * 
     * 与 $Type_ExecuteJava、xjavaID、methodName配合使用。
     * 
     * 云计算服务器的列表，可以包含自己。
     * 
     * 注1：所有通讯数据对象及关联对象，均须实际序列化接口java.io.Serializable。 
     * 注2：methodName方法入参形式与本地执行Java方法不同。方法形式如下：
     *     public boolean 方法名称(Map<String ,Object> i_Params) {}
     *        
     *     入参i_Params与XSQLGroupExecuteJava.executeJava_XSQLNode()方法中的io_Params入参相同，但只是只读的。 
     */
    public String getCloudServers()
    {
        return cloudServers;
    }
    

    
    /**
     * 与 cloudServers 同义，并且 cloudServers 最终将转成 cloudServersList。
     * 
     * 但功能上与cloudServers略有差异：
     *    当多个XSQL组共用一组云计算服务列表时，云计算服务器可在多个XSQL组间保持一定负载均衡的功能。
     */
    public CycleNextList<XSQLNodeCloud> getCloudServersList()
    {
        return cloudServersList;
    }


    
    /**
     * 云计算服务器的列表。用英文逗号,分隔（可以有空格、回车符、制表符）。如下形式：IP1:Port1 ,IP2:Port2 ,IP3:Port3。
     * 
     * 与 $Type_ExecuteJava、xjavaID、methodName配合使用。
     * 
     * 云计算服务器的列表，可以包含自己。
     * 
     * 注1：所有通讯数据对象及关联对象，均须实际序列化接口java.io.Serializable。 
     * 注2：methodName方法入参形式与本地执行Java方法不同。方法形式如下：
     *     public boolean 方法名称(Map<String ,Object> i_Params) {}
     *        
     *     入参i_Params与XSQLGroupExecuteJava.executeJava_XSQLNode()方法中的io_Params入参相同，但只是只读的。
     */
    public void setCloudServers(String cloudServers)
    {
        this.cloudServers     = cloudServers;
        this.cloudServersList = new CycleNextList<XSQLNodeCloud>();
        
        String [] v_ClusterServers = StringHelp.replaceAll(this.cloudServers ,new String[]{"，" ," " ,"\t" ,"\r" ,"\n"} ,new String[]{"," ,""}).split(",");
        for (String v_Server : v_ClusterServers)
        {
            String [] v_HostPort = (v_Server.trim() + ":1721").split(":");
            
            this.cloudServersList.add(new XSQLNodeCloud(new ClientSocket(v_HostPort[0] ,Integer.parseInt(v_HostPort[1]))));
        }
    }
    

    
    /**
     * 与 cloudServers 同义，并且 cloudServers 最终将转成 cloudServersList。
     * 
     * 但功能上与cloudServers略有差异：
     *    当多个XSQL组共用一组云计算服务列表时，云计算服务器可在多个XSQL组间保持一定负载均衡的功能。
     */
    public void setCloudServersList(CycleNextList<XSQLNodeCloud> cloudServersList)
    {
        this.cloudServersList = cloudServersList;
    }
    

    
    
    /**
     * 获取：异常重试的时间间隔(单位：毫秒)。
     * 
     * 第1次异常时，等待时长为：5秒时，
     * 第2次异常时，等待时长为：10秒，是上次等待时长的翻倍。
     * 第3次异常时，等待时长为：20秒，是上次等待时长的翻倍。
     * 
     * 与 this.异常重试次数 属性配合使用。
     * 
     * 默认为5 * 1000 = 5秒
     */
    public long getRetryInterval()
    {
        return retryInterval;
    }
    

    
    /**
     * 设置：异常重试的时间间隔(单位：毫秒)。
     * 
     * 第1次异常时，等待时长为：5秒时，
     * 第2次异常时，等待时长为：10秒，是上次等待时长的翻倍。
     * 第3次异常时，等待时长为：20秒，是上次等待时长的翻倍。
     * 
     * 与 this.异常重试次数 属性配合使用。
     * 
     * 默认为5 * 1000 = 5秒
     * 
     * @param retryInterval 
     */
    public void setRetryInterval(long retryInterval)
    {
        this.retryInterval = retryInterval;
    }
    


    /**
     * 获取：* 异常重试次数。执行SQL语句，因争抢锁等原因异常时，尝试重新执行。当重试多次仍然异常时，放弃执行。
     * 
     * 等于0时，表示异常时，不尝试重新执行。
     * 
     * 默认为：0次
     */
    public int getRetryCount()
    {
        return retryCount;
    }
    

    
    /**
     * 设置：* 异常重试次数。执行SQL语句，因争抢锁等原因异常时，尝试重新执行。当重试多次仍然异常时，放弃执行。
     * 
     * 等于0时，表示异常时，不尝试重新执行。
     * 
     * 默认为：0次
     * 
     * @param retryCount 
     */
    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }
    


    /**
     * 获取：异常重试次数。云计算异常时，尝试交给其它云服务重新计算。当重试多次云计算仍然异常时，放弃计算。
     * 
     * 等于0时，表示云计算异常时，不尝试重新计算。
     * 
     * 默认为：3次
     */
    public int getCloudRetryCount()
    {
        return cloudRetryCount;
    }
    

    
    /**
     * 设置：异常重试次数。云计算异常时，尝试交给其它云服务重新计算。当重试多次云计算仍然异常时，放弃计算。
     * 
     * 等于0时，表示云计算异常时，不尝试重新计算。
     * 
     * 默认为：3次
     * 
     * @param cloudRetryCount 
     */
    public void setCloudRetryCount(int cloudRetryCount)
    {
        this.cloudRetryCount = cloudRetryCount;
    }



    /**
     * 获取：云服务计算异常的服务器数量。当异常时，其服务器仍然标记为"繁忙"
     */
    public synchronized int getCloudErrorCount()
    {
        return cloudErrorCount;
    }


    
    /**
     * 设置：云服务计算异常的服务器数量。当异常时，其服务器仍然标记为"繁忙"
     * 
     * @param cloudErrorCount 
     */
    public synchronized void setCloudErrorCount(int cloudErrorCount)
    {
        this.cloudErrorCount = cloudErrorCount;
    }
    


    /**
     * 获取：云服务正在运算（或繁忙）的服务器数量
     */
    public synchronized int getCloudBusyCount()
    {
        return cloudBusyCount;
    }
    

    
    /**
     * 设置：云服务正在运算（或繁忙）的服务器数量
     * 
     * @param cloudBusyCount 
     */
    public synchronized void setCloudBusyCount(int cloudBusyCount)
    {
        this.cloudBusyCount = cloudBusyCount;
    }
    
    
    
    /**
     * 云服务的繁忙数+1
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-30
     * @version     v1.0
     *
     * @return  并返回当前的总繁忙数
     */
    public synchronized int cloudBusy()
    {
        return ++this.cloudBusyCount;
    }
    
    
    
    /**
     * 云服务的空闲数+1（即繁忙数-1）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-30
     * @version     v1.0
     *
     * @return  并返回当前的总繁忙数
     */
    public synchronized int cloudIdle()
    {
        return --this.cloudBusyCount;
    }
    
    
    
    /**
     * 云服务的异常数+1。当异常时，其服务器仍然标记为"繁忙"
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-22
     * @version     v1.0
     *
     * @return  并返回当前的总繁忙数
     */
    public synchronized int cloudError()
    {
        return ++this.cloudErrorCount;
    }
    

    
    /**
     * 获取：等待哪个节点上的云服务计算完成
     */
    public XSQLNode getCloudWait()
    {
        return cloudWait;
    }
    
    
    
    /**
     * 设置本节为等待的云服务计算完成的节点
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-02-12
     * @version     v1.0
     *
     * @param i_IsWait
     */
    public void setMyCloudWait(boolean i_IsWait)
    {
        if ( i_IsWait )
        {
            this.cloudWait = this;
        }
        else
        {
            this.cloudWait = null;
        }
    }
    

    
    /**
     * 设置：* 等待哪个节点上的云服务计算完成
     * 
     * @param cloudWait 
     */
    public void setCloudWait(XSQLNode cloudWait)
    {
        this.cloudWait = cloudWait;
    }
    

    
    /**
     * 获取：监控云服务计算完成情况的时间间隔(单位：毫秒)。
     * 
     * 与 this.cloudWait 属性配合使用。
     * 
     * 默认为5 * 1000 = 5秒
     */
    public long getCloudWaitInterval()
    {
        if ( cloudWaitInterval <= 0 )
        {
            cloudWaitInterval = 5 * 1000;
        }
        return cloudWaitInterval;
    }
    
    
    
    /**
     * 设置：* 监控云服务计算完成情况的时间间隔(单位：毫秒)。
     * 
     * 与 this.cloudWait 属性配合使用。
     * 
     * 默认为5 * 1000 = 5秒
     * 
     * @param cloudWaitInterval 
     */
    public void setCloudWaitInterval(long cloudWaitInterval)
    {
        this.cloudWaitInterval = cloudWaitInterval;
    }
    

    
    /**
     * 获取：云服务并行发起计算动作时，间隔发起计算的时间间隔(单位：毫秒)。
     * 
     * 默认为 66毫秒
     */
    public long getCloudExecInterval()
    {
        if ( cloudExecInterval <= 0 )
        {
            cloudExecInterval = 66;
        }
        return cloudExecInterval;
    }
    

    
    /**
     * 设置：云服务并行发起计算动作时，间隔发起计算的时间间隔(单位：毫秒)。
     * 
     * 默认为 66毫秒
     * 
     * @param cloudExecInterval 
     */
    public void setCloudExecInterval(long cloudExecInterval)
    {
        this.cloudExecInterval = cloudExecInterval;
    }
    


    /**
     * 获取：XJava对象标识
     *      构建XSQLNode对象实例时，xjavaID标记的对象实例，可以是不存在的（或尚未构建的）。
     *      只要在执行时存在就OK了
     */
    public String getXjavaID()
    {
        return xjavaID;
    }


    
    /**
     * 设置：XJava对象标识。
     *      构建XSQLNode对象实例时，xjavaID标记的对象实例，可以是不存在的（或尚未构建的）。
     *      只要在执行时存在就OK了
     * 
     * @param xjavaID 
     */
    public void setXjavaID(String xjavaID)
    {
        this.xjavaID      = xjavaID;
        this.xjavaIntance = null;
        this.xjavaMethod  = null;
    }


    
    /**
     * 获取：XJava对象执行的方法名
     *      方法的定义形式为：详见org.hy.common.xml.plugins.XSQLGroupExecuteJava
     *      
     * 默认名称为：executes。方便云计算时，执行云端的XSQL组，只须用户配置xjavaID即可。
     */
    public String getMethodName()
    {
        return methodName;
    }


    
    /**
     * 设置：XJava对象执行的方法名
     *      方法的定义形式为：详见org.hy.common.xml.plugins.XSQLGroupExecuteJava
     *        
     * 默认名称为：executes。方便云计算时，执行云端的XSQL组，只须用户配置xjavaID即可。
     *        
     * @param methodName 
     */
    public void setMethodName(String methodName)
    {
        this.methodName   = methodName;
        this.xjavaIntance = null;
        this.xjavaMethod  = null;
    }
    
    
    
    /**
     * 执行Java代码的执行方法。
     * 
     * @see org.hy.common.xml.plugins.XSQLGroupExecuteJava
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-05-17
     * @version     v1.0
     *              v2.0  2017-12-22  添加XSQLGroupControl参数
     *
     * @param i_Control    XSQL组的控制中心。如，统一事务提交、统一事务回滚。
     * @param io_Params    执行或查询参数。同XSQLGroup.executeGroup()方法的入参参数io_Params同义。
     * @param io_Returns   通过returnID标记的，返回出去的多个查询结果集。同XSQLGroupResult.returns属性同义。
     * @return             表示是否执行成功。当返回false时，其后的XSQLNode节点将不再执行。
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public boolean executeJava(XSQLGroupControl i_Control ,Map<String ,Object> io_Params ,Map<String ,Object> io_Returns) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        return this.executeJava(i_Control ,io_Params ,io_Returns ,1);
    }
    
    
    
    /**
     * 执行Java代码的执行方法。
     * 
     * @see org.hy.common.xml.plugins.XSQLGroupExecuteJava
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-05-17
     * @version     v1.0
     *              v2.0  2017-12-22  添加XSQLGroupControl参数
     *              v3.0  2018-02-23  添加执行次数，用于云计算异常时重新计算的执行次数。表示当前执行次数，下标从1开始。
     *
     * @param i_Control       XSQL组的控制中心。如，统一事务提交、统一事务回滚。
     * @param io_Params       执行或查询参数。同XSQLGroup.executeGroup()方法的入参参数io_Params同义。
     * @param io_Returns      通过returnID标记的，返回出去的多个查询结果集。同XSQLGroupResult.returns属性同义。
     * @param i_ExecuteCount  执行次数，用于云计算异常时重新计算的执行次数。表示当前执行次数，下标从1开始。
     * @return                表示是否执行成功。当返回false时，其后的XSQLNode节点将不再执行。
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public synchronized boolean executeJava(XSQLGroupControl i_Control ,Map<String ,Object> io_Params ,Map<String ,Object> io_Returns ,int i_ExecuteCount) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        // 本地计算
        if ( Help.isNull(this.cloudServersList) )
        {
            // 已经解释成功的，不在二次解释
            if ( this.xjavaMethod != null )
            {
                Object v_Object = XJava.getObject(this.xjavaID.trim());
                
                if ( this.xjavaIntance != v_Object )
                {
                    this.parserMethod();
                }
            }
            else
            {
                this.parserMethod();
            }
            
            Object v_Ret = this.xjavaMethod.invoke(this.xjavaIntance ,i_Control ,io_Params ,io_Returns);
            return (Boolean)v_Ret;
        }
        // 云计算 ZhengWei(HY) Add 2018-01-30
        else
        {
            if ( Help.isNull(this.xjavaID) )
            {
                throw new NullPointerException("XSQLNode.getXjavaID() is null.");
            }
            else if ( Help.isNull(this.methodName) )
            {
                throw new NullPointerException("XSQLNode.getMethodName() is null."); 
            }
            else
            {
                long          v_Interval = this.getCloudExecInterval();
                XSQLNodeCloud v_Cloud    = this.cloudServersList.next();
                while ( !v_Cloud.isIdle() )
                {
                    try
                    {
                        Thread.sleep(v_Interval);
                    }
                    catch (Exception exce)
                    {
                        // Nothing.
                    }
                    v_Cloud = this.cloudServersList.next();
                }
                
                v_Cloud.executeCloud(this ,i_Control ,io_Params ,io_Returns ,i_ExecuteCount);
                return true;
            }
        }
    }
    
    
    
    /**
     * 解释执行Java的方法对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-05-17
     * @version     v1.0
     *
     * @throws NoSuchMethodException
     */
    private void parserMethod() throws NoSuchMethodException
    {
        if ( Help.isNull(this.xjavaID) )
        {
            throw new NullPointerException("XSQLNode.getXjavaID() is null.");
        }
        
        if ( Help.isNull(this.methodName) )
        {
            throw new NullPointerException("XSQLNode.getMethodName() is null."); 
        }
        
        this.xjavaIntance = XJava.getObject(this.xjavaID.trim());
        this.xjavaMethod  = null;
        if ( this.xjavaIntance == null )
        {
            throw new NullPointerException("XSQLNode.getXjavaID() = " + this.xjavaID + " XJava.getObject(...) is null.");
        }
        
        Method [] v_Methods = this.xjavaIntance.getClass().getMethods();
        
        for (int i=0; i<v_Methods.length; i++)
        {
            if ( v_Methods[i].getName().equals(this.methodName) )
            {
                if ( v_Methods[i].getParameterTypes().length == 3 )
                {
                    if ( XSQLGroupControl.class.equals(v_Methods[i].getParameterTypes()[0])
                      && Map.class.equals(v_Methods[i].getParameterTypes()[1]) 
                      && Map.class.equals(v_Methods[i].getParameterTypes()[2]) )
                    {
                        if ( v_Methods[i].getReturnType() != null )
                        {
                            String v_RTName = v_Methods[i].getReturnType().getName();
                            
                            if ( "boolean".equals(v_RTName) || "java.lang.Boolean".equals(v_RTName) )
                            {
                                this.xjavaMethod = v_Methods[i];
                                break;
                            }
                        }
                        
                    }
                }
            }
        }
        
        if ( this.xjavaMethod == null )
        {
            throw new NoSuchMethodException("XSQLNode.getXjavaID() = " + this.xjavaID + " not find method[public boolean " + this.methodName + "(Map<String ,Object> i_Params ,Map<String ,Object> io_Returns){ ... }].");
        }
    }



    /**
     * 条件是否通过
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     * @param i_ConditionValues  条件值的集合
     * @return
     */
    public boolean isPass(Map<String ,?> i_ConditionValues)
    {
        if ( Help.isNull(this.condition) 
          || Help.isNull(this.conditionFel)
          || Help.isNull(this.placeholders) )
        {
            return true;
        }
        
        try
        {
            FelEngine  v_Fel        = new FelEngineImpl();
            FelContext v_FelContext = v_Fel.getContext();
            
            for (String v_Key : this.placeholders.keySet())
            {
                Object v_Value = MethodReflect.getMapValue(i_ConditionValues ,v_Key);
                
                v_Key = StringHelp.replaceAll(v_Key ,"." ,"_"); // 点原本就是Fel关键字，所以要替换 ZhengWei(HY) Add 2017-05-23
                
                if ( null == v_Value )
                {
                    v_FelContext.set(v_Key ,"NULL");
                }
                else
                {
                    v_FelContext.set(v_Key ,v_Value);
                }
            }
            
            return (Boolean) v_Fel.eval(this.conditionFel);
        }
        catch (Exception exce)
        {
            throw new RuntimeException("Fel[" + this.condition + "] is error." + exce.getMessage());
        }
    }
    
    
    
    /**
     * 集合ID
     * 
     * 只对 $Type_CollectionToQuery、$Type_CollectionToDB 两个类型有效。标识要转换角色的集合ID。
     * 当为空时，表示将整个 XSQLGroup.execute() 方法的入参当为查询结果来用（前提是：入参类型必须为：List、Set、Map(会通过Hetp.toList()转为List)）。
     * 
     * 支持xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释
     */
    public String getCollectionID()
    {
        return this.collectionID;
    }
    
    
    
    /**
     * 集合ID
     * 
     * 只对 $Type_CollectionToQuery、$Type_CollectionToDB 两个类型有效。标识要转换角色的集合ID。
     * 当为空时，表示将整个 XSQLGroup.execute() 方法的入参当为查询结果来用（前提是：入参类型必须为：List、Set、Map(会通过Hetp.toList()转为List)）。
     * 
     * 支持xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释
     */
    public void setCollectionID(String i_CollectionID)
    {
        this.collectionID = i_CollectionID;
    }
    
    
    
    /**
     * 是否为多线程并且发执行。默认值：false。
     * 
     * 只对 $Type_Query、$Type_CollectionToQuery 两个类型有效。
     * 
     * @return
     */
    public boolean isThread()
    {
        return this.thread;
    }
    
    
    
    /**
     * 是否为多线程并且发执行。默认值：false。
     * 
     * 只对 $Type_Query、$Type_CollectionToQuery 两个类型有效。
     * 
     * 注：当thread设置为ture时，threadWait默认也设置为true，
     *    表示同一节点发起的多线程任务的同时，也在同一节点等待所有线程执行完成。
     * 
     * @param i_Thread
     */
    public void setThread(boolean i_Thread)
    {
        this.thread     = i_Thread;
        this.threadWait = i_Thread;
    }

    
    
    /**
     * 获取：当发起多线程时，标记哪个节点等待所有线程均执行完成。与 this.thread 配合使用。默认值：false。
     * 
     * 当某一节点设置为等待(this.threadWait=true)时，将在此节点的SQL语句执行完成后执行等待。
     * 
     * 注：当thread设置为ture时，threadWait默认也设置为true，
     *    表示同一节点发起的多线程任务的同时，也在同一节点等待所有线程执行完成。
     */
    public boolean isThreadWait()
    {
        return threadWait;
    }
    

    
    /**
     * 设置：当发起多线程时，标记哪个节点等待所有线程均执行完成。与 this.thread 配合使用。默认值：false。
     * 
     * 当某一节点设置为等待(this.threadWait=true)时，将在此节点的SQL语句执行完成后执行等待。
     * 
     * @param threadWait 
     */
    public void setThreadWait(boolean threadWait)
    {
        this.threadWait = threadWait;
    }
    

    
    /**
     * 获取：监控所有线程完成情况的时间间隔(单位：毫秒)。
     * 
     * 与 this.threadWait 属性配合使用。
     * 
     * 默认为0值，表示取时间间隔为：ThreadPool.getIntervalTime() * 3
     */
    public long getThreadWaitInterval()
    {
        return threadWaitInterval;
    }
    

    
    /**
     * 设置：监控所有线程完成情况的时间间隔(单位：毫秒)。
     * 
     * 与 this.threadWait 属性配合使用。
     * 
     * 默认为0值，表示取时间间隔为：ThreadPool.getIntervalTime() * 3
     * 
     * @param threadWaitInterval 
     */
    public void setThreadWaitInterval(long threadWaitInterval)
    {
        this.threadWaitInterval = threadWaitInterval;
    }



    /**
     * 获取：自主、自由的数据库连接。
     * 
     * 当为true时，本节点的数据库连接不再由XSQLGroup控制及管理。
     * 由节点自行打开一个独立的数据库连接，并自行控制提交、回滚。
     * 
     * 主要用于多线程的并发写操作。
     * 
     * 只对 $Type_ExecuteUpdate、$Type_Execute、$Type_CollectionToExecuteUpdate 三个类型有效。
     * 
     * 默认为：false，即：由XSQLroup控制及管理数据库连接
     */
    public boolean isFreeConnection()
    {
        return freeConnection;
    }
    

    
    /**
     * 设置：自主、自由的数据库连接。
     * 
     * 当为true时，本节点的数据库连接不再由XSQLGroup控制及管理。
     * 由节点自行打开一个独立的数据库连接，并自行控制提交、回滚。
     * 
     * 主要用于多线程的并发写操作。
     * 
     * 默认为：false，即：由XSQLroup控制及管理数据库连接
     * 
     * 只对 $Type_ExecuteUpdate、$Type_Execute、$Type_CollectionToExecuteUpdate 三个类型有效。
     * 
     * @param freeConnection 
     */
    public void setFreeConnection(boolean freeConnection)
    {
        this.freeConnection = freeConnection;
    }
    


    /**
     * 获取：对于查询语句有两种使用数据库连接的方式
     *   1. 读写分离：每一个查询SQL均占用一个新的连接，所有的更新修改SQL共用一个连接。this.oneConnection = false，默认值。
     *   2. 读写同事务：查询SQL与更新修改SQL共用一个连接，做到读、写在同一个事务中进行。
     *   
     * 只对 $Type_Query 类型有效。
     */
    public boolean isOneConnection()
    {
        return oneConnection;
    }

    
    
    /**
     * 设置：对于查询语句有两种使用数据库连接的方式
     *   1. 读写分离：每一个查询SQL均占用一个新的连接，所有的更新修改SQL共用一个连接。this.oneConnection = false，默认值。
     *   2. 读写同事务：查询SQL与更新修改SQL共用一个连接，做到读、写在同一个事务中进行。
     * 
     * 只对 $Type_Query 类型有效。
     * 
     * @param oneConnection 
     */
    public void setOneConnection(boolean oneConnection)
    {
        this.oneConnection = oneConnection;
    }


    
    /**
     * 获取：是否采用大数据模式控制循环遍历
     */
    public boolean isBigData()
    {
        return bigData;
    }
    

    
    /**
     * 设置：是否采用大数据模式控制循环遍历
     * 
     * @param bigData 
     */
    public void setBigData(boolean bigData)
    {
        this.bigData = bigData;
    }


    
    /**
     * 获取：是否断言(assert)调试
     */
    public boolean isDebug()
    {
        return debug;
    }
    

    
    /**
     * 设置：是否断言(assert)调试
     * 
     * @param debug 
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }
    
}
