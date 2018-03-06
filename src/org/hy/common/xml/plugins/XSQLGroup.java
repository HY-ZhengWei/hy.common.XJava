package org.hy.common.xml.plugins;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Counter;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.TablePartition;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.thread.Task;
import org.hy.common.thread.TaskGroup;
import org.hy.common.thread.ThreadPool;
import org.hy.common.xml.XSQLBigData;





/**
 * 组合一组XSQL，有顺序，有参数关联的执行。
 * 
 *   特点01：跨数据库位置及数据库种类。每个XSQL节点，可以执行（或查询）不同数据库的SQL语言。
 *   特点02：先后执行顺序的执行，并且前一个查询SQL的结果，将是下一个（乃至其后）XSQL节点的执行参数（或查询参数）。
 *   特点03：前一个查询XSQL节点的查询结果行数，决定着下一个XSQL节点的执行（或查询）次数，并且依次类推（除了 lastOnce=true 的节点）。
 *   特点04：当节点 lastOnce=true 时，表示本节点为收尾操作之一。在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
 *   特点05：每个XSQL节点，都一个检查点，只有当检查通过时，才能执行本XSQL节点。
 *   特点06：当XSQL节点检查不通过时，通过noPassContinue属性决定是否继续执行其后的XSQL节点。默认检查只影响自己的节点，不影响其它及其后XSQL节点的执行。
 *   特点07：任何一个XSQL节点执行异常，都将全部终止执行（或查询）。
 *   特点08：可打印出执行轨迹，显示每一步执行的SQL语句。
 *   特点09：记录每个XSQL节点的上执行SQL（Insert、Update、Delete）影响的行数，可作为其后所有节点的执行参数（查询参数）。如变量EXECOUNT2，表示第二个步骤执行后影响的行数。但，不记录查询SQL的行数。
 *   特点10：支持统一提交、统一回滚的事务功能。不做特别设置的时，默认情况下，在所有操作成功后，统一提交。或在出现异常后，统一回滚。
 *   特点11：有三种节点提交类型：
 *           1. beforeCommit   操作节点前提交；
 *           2. afterCommit    操作节点后提交；
 *           3. perAfterCommit 每获取结果集一条记录前提交。
 *   特点12： 当未更新任何数据（操作影响的数据量为0条）时，可控制是否执行事务统一回滚操作XSQLNode.isNoUpdateRollbacks()
 *   特点13： 由外界决定是否提交、是否回滚的功能。
 *   特点14：支持简单的统计功能（请求整体执行的次数、成功次数、成功累计用时时长）。
 *   特点15：支持组嵌套组的执行：实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
 *   特点16：支持递归的执行。自己指向自己递归执行（参类可分为局部变量和全局变量）。
 *   特点17：支持返回多个查询结果集。returnID标记的查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
 *   特点18：支持查询结果当作其后节点的SQL入参的同时，还返回查询结果。将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
 *   特点19：支持执行Java代码，对查询结果集进行二次加工处理等Java操作。
 *   特点20：查询SQL语句有两种使用数据库连接的方式
 *           1. 读写分离：每一个查询SQL均占用一个新的连接，所有的更新修改SQL共用一个连接。this.oneConnection = false，默认值。
 *           2. 读写同事务：查询SQL与更新修改SQL共用一个连接，做到读、写在同一个事务中进行。
 *   
 *   注意01：查询类型XSQL节点的查询结果使用List<Object>结构保存。List元素也可以是Map类型的。如果元素是JavaBean，是会被内部自动转为Map的。
 *   注意02：XSQL中占位符的命名，无大小写要求。但那怕是为了一点点儿的性能，都写成大写的要好些（或前后关系中写法一致）。
 *   注意03：节点检查条件的占位符命名无大小写要求。
 *   注意04：如果前一个节点做了更新操作(Update、Delete等)，但没有提交，后一个节点对同一张表的查询，是会死锁的（一直等待上个更新节点的提交）。
 *          但设置 XSQLNode.oneConnection=true 后，同一张表的查询是OK的。
 *   注意05：嵌套查询类型的XSQL节点，其子XSQL节点不能更改（能读取）父XSQL节点的参数信息，等同时for循环中的局部变量。
 *          举例：控制循环次数的查询XSQL节点有A、B、C三个嵌套，相当于Java中嵌套循环for(A){ for(B){ for(C){} } }，
 *               节点C可以读取到A、B两节点的所有参数信息（执行入参，A、B节点当前行的查询结果），
 *               但节点C不能修改A、B两节点的所有参数（原先是可以的，但只对Java基本类型有效）。
 *          这样做的好处是：当节点C退出循环时，B节点的参数信息不会受到影响，同时，每个节点均可以有相同名称的变量。
 *                        从此将支持递归嵌套循环。
 *          
 *   
 *   XSQL组执行的Java方法的定义模板 @see org.hy.common.xml.plugins.XSQLGroupExecuteJava
 *   
 *   
 * 此类的原始构想来源于：2013-12-16开发的名为DBTask（数据库任务执行程序）的程序。
 * 本类比起DBTask而言，功能单一，但更加专注，去掉了Mail、FTP、File、系统命令等相关复杂操作。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2016-01-20
 * @version     v1.0
 *              v2.0  2016-02-15  1.记录每个XSQL节点的上执行SQL（Insert、Update、Delete）影响的行数，可作为其后所有节点的执行参数（查询参数）。
 *                                2.支持简单的统计功能（请求整体执行的次数、成功次数、成功累计用时时长）
 *              v3.0  2016-03-03  1.添加：操作后提交：执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作
 *                                2.添加：整体操作后的收尾操作：在整个组合XSQLGroup的最后执行，并只执行一次。
 *                                3.添加：组嵌套组的执行：实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
 *                                    3.1 每个XSQLGroup组，除了执行参数、返回结果集、累计总行数、异常是相互传递的外，还有十分重要的数据库连接池控制，也是统一的、相互传递的。
 *                                    3.2 相互传递统一的数据库连接池控制(提交、回滚、关闭)。即，后一个XSQLGroup组的回滚、异常是会影响到前一个XSQLGroup组的数据库操作的。
 *              v4.0  2016-03-14  1.添加：返回查询结果集：returnID标记的查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
 *                                    1.1 此类查询XSQL节点，不再控制其后节点的执行次数。
 *              v5.0  2016-03-22  1.添加：查询结果当作其后节点的SQL入参的同时，还返回查询结果：queryReturnID标记的查询XSQL节点，在循环遍历的同时，还返回所有行数据结果。
 *                                    1.1 将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
 *                                    1.2 当 this.returnID 有值时，this.queryReturnID 将失效（不起作用）。
 *                                    1.3 并且只有每次循环中的所有节点都执行成功后，才 PartitionMap.putRows(Map<String ,Object>) 一行查询记录。
 *              v6.0  2016-05-17  1.添加节点类型：执行Java代码。即可以对查询结果集进行二次加工处理等Java操作。
 *              v7.0  2016-07-21  1.添加 结果集索引下标$Param_RowIndex、结果集大小$Param_RowSize、上一条数据$Param_RowPrevious、下一条数据$Param_RowNext 四个辅助数据处理的数据对象。
 *              v7.1  2016-07-30  1.修正 当为子级的嵌套SQL组时，不关闭数据库连接，而是交给父组SQL组来统一关闭。
 *              v8.0  2016-07-30  1.添加节点类型：XSQLGroup.execute()方法的入参，转换角色为数据库查询结果。
 *              v9.0  2016-08-02  1.添加节点类型：XSQLGroup.execute()方法的入参中的集合，整体批量的写入的数据库中。
 *              v10.0 2016-08-16  1.添加：最后执行时间点的记录。
 *              v11.0 2017-03-08  1.添加：描述属性。方便在http://IP:Port/WebName/analyses/analyseObject?xid=GXSQL*页面中显示描述信息。
 *              v12.0 2017-05-05  1.添加：XSQLNode.oneConnection 查询SQL数据库连接的占用模式。
 *              v13.0 2017-05-17  1.添加：this.returnQuery，针对 this.returnID 属性，定义返回查询结果集是 "返回结果集"？还是 "查询并返回"。
 *                                  提供一种好理解的数据结构(与this.queryReturnID属性返回的数据结构相比)。
 *                                  建议来自于：向以前同学
 *                                2.准备放弃this.queryReturnID属性，只少是不再建议使用此属性。
 *              v14.0 2017-06-22  1.添加：XSQLGroup也同样支持在执行前的条件检查，只有检查通过时才允许执行。
 *                                2.添加：XSQLGroup也同样支持在执行前的提交BeforeCommit。
 *                                3.添加：XSQLGroup也同样支持在执行前的提交AfterCommit。
 *                                  建议来自于：谈闻同学
 *              v14.1 2017-07-06  1.修正：当预处理 XSQLNode.$Type_CollectionToExecuteUpdate 执行异常时，输出的SQL日志不正确的问题。
 *                                  发现人：向以前同学
 *              v14.2 2017-10-31  1.修正：getConnection()未添加同步锁，造成XSQL组在发起多线程执行时，会出现挂死的问题。
 *                                  发现人：邹德福
 *              v15.0 2017-11-03  1.添加：由外界决定是否提交、是否回滚的功能。
 *                                       通过 this.executes(...) 执行结果 XSQLGroupResult 来手工提交、回滚。
 *                                  建议来自于：向以前同学
 *              v15.1 2017-11-20  1.优化：提升getCollectionToDB()方法的执行性能。
 *              v16.0 2017-12-22  1.添加：XSQL组执行的Java方法的入参参数中增加控制中心XSQLGroupControl，实现事务统一提交、回滚。
 *                                2.添加：XSQLNode.isNoUpdateRollbacks()方法，当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作。
 *              v17.0 2018-01-21  1.添加：支持多个平行、平等的数据库的负载均衡（简单级的），详见XSQL
 *              v18.0 2018-01-24  1.更改：控制循环次数的查询XSQL节点，其查询入参(io_Params)由全局变量更为局部变量。
 *                                       保留原子节点继承父查询XSQL节点的入参信息。从此将支持递归嵌套循环
 *                                       举例：控制循环次数的查询XSQL节点有A、B、C三个嵌套，相当于Java的三层嵌套循环for(A){ for(B){ for(C){} } }，
 *                                            节点C可以读取到A、B两节点的所有参数信息（执行入参，A、B节点当前行的查询结果），
 *                                            但节点C不能修改A、B两节点的所有参数（原先是可以的，但只对Java基本类型有效）。
 *                                2.添加：多线程等待XSQLNode.threadWait属性，可自由定义在哪个节点上等待所有线程均执行完成。
 *                                       同时，根节点XSQL组及所有子节点XSQL组均共享一个线程任务组。
 *                                       配合递归功能，不再重复创建多线程任务组，
 *                                       递归时重复创建多个多线程任务组，会造成线程资源使用殆尽，出现死锁
 *                                3.添加：自主、自由的数据库连接freeConnection属性。
 *                                       节点使用的数据库连接不再由XSQLGroup控制及管理。
 *                                       由节点自行打开一个独立的数据库连接，并自行控制提交、回滚。
 *                                       主要用于多线程的并发写操作。
 *              v19.0 2018-02-22  1.修复：云计算时，某台服务器异常后，修复"云等待"死等的问题。
 *                                2.添加：等待哪个节点上的云服务计算完成。与XSQLNode.cloudWait同义。
 *                                       但，此属性表示XSQL组整体完成前的最后等待哪个节点上的云服务计算。
 *                                       在所有lastOnce标记的XSQL节点执行之前执行此等待操作。
 *              v20.0 2018-03-05  1.添加：重置统计数据的功能。
 *                                2.添加：执行异常时重试XSQLNode.retryCount功能。
 */
public final class XSQLGroup
{
    
    /** 执行SQL(Insert、Update、Delete)影响行数的变量名前缀 */
    public  static final String      $Param_ExecCount   = "ExecCount_";
    
    /** 用于执行Java节点：执行查询SQL的结果集索引下标的变量名称（变量值下标从零开始） */
    public  static final String      $Param_RowIndex    = "RowIndex_";
    
    /** 用于执行Java节点：执行查询SQL的结果集大小的变量名称 */
    public  static final String      $Param_RowSize     = "RowSize_";
    
    /** 
     * 用于执行Java节点：执行查询SQL的结果集当前数据的上一条数据的变量名称
     *   1. 此变量名称对应的数据有可能为空，因为第0条前没有上一条数据
     *   2. 此变量名称对应的数据对象类型为：Map<String ,Object> ，都被统一转换了。
     *   3. 此变量名称对应的数据对象为修改后，是会影响其后执行的执行参数的。即，不只能处理当前数据，还能处理上一条数据
     */
    public  static final String      $Param_RowPrevious = "RowPrevious_";
    
    /** 
     * 用于执行Java节点：执行查询SQL的结果集当前数据的下一条数据的变量名称
     *   1. 此变量名称对应的数据有可能为空，因为最后一条后没有下一条数据
     *   2. 此变量名称对应的数据对象类型为：SQL结果XSQLResult中定义的类型 
     *   3. 此变量名称对应的数据对象为修改后，是会影响其后执行的执行参数的。即，不只能处理当前数据，还能处理下一条数据
     */
    public  static final String      $Param_RowNext     = "RowNext_";
    
    /** 任务序列号 */
    private static       int         $SerialNo          = 0;
    
    
    
    /** 父级SQL组对象。当用值时，表示本级为子的嵌套SQL组对象 */
    private XSQLGroup                superGroup;
    
    /** 执行SQL节点的集合 */
    private List<XSQLNode>           xsqlNodes;
    
    /**
     * 等待哪个节点上的云服务计算完成。与XSQLNode.cloudWait同义
     * 但，此属性表示XSQL组整体完成前的最后等待哪个节点上的云服务计算。
     * 
     * 在所有lastOnce标记的XSQL节点执行之前执行此等待操作。
     */
    private XSQLNode                 cloudWait;
    
    /** 是否打印执行轨迹日志。默认为：false */
    private boolean                  isLog;
    
    /** 
     * 是否自动提交。默认为： true
     * 
     * 当为false时，须外界手工执行提交或回滚，及关闭数据库连接。
     * 
     * 注：此属性对以下情况无效，以下情况相当于手工提交。
     *           1. beforeCommit   操作节点前提交；
     *           2. afterCommit    操作节点后提交；
     *           3. perAfterCommit 每获取结果集一条记录前提交。
     */
    private boolean                  isAutoCommit;
    
    /** 请求整体执行的次数 */
    private long                     requestCount;
    
    /** 请求成功，并成功返回次数 */
    private long                     successCount;
    
    /** 请求成功，并成功返回的累计用时时长 */
    private double                   successTimeLen;
    
    /**
     * 最后执行时间点。
     *   1. 在开始执行时，此时间点会记录一次。
     *   2. 在执行结束后，此时间点会记录一次。
     *   3. 当出现异常时，此时间点保持最近一次，不变。
     *   4. 当多个线程同时操作时，记录最新的时间点。
     *   5. 未执行时，此属性为NULL
     */
    private Date                     executeTime;
    
    /** 注释。可用于日志的输出等帮助性的信息 */
    private String                   comment;
    
    
    
    public XSQLGroup()
    {
        this.superGroup     = null;
        this.xsqlNodes      = new ArrayList<XSQLNode>();
        this.cloudWait      = null;
        this.isLog          = false;
        this.isAutoCommit   = true;
        this.requestCount   = 0;
        this.successCount   = 0;
        this.successTimeLen = 0D;
        this.executeTime    = null;
        this.comment        = null;
    }
    
    
    
    /**
     * 重置统计数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-05
     * @version     v1.0
     *
     */
    public void reset()
    {
        this.requestCount   = 0;
        this.successCount   = 0;
        this.successTimeLen = 0D;
        this.executeTime    = null;
    }
    
    
    
    private synchronized Date request()
    {
        ++this.requestCount;
        this.executeTime = new Date();
        return this.executeTime;
    }
    
    
    
    private synchronized void success(double i_TimeLen)
    {
        ++this.successCount;
        this.successTimeLen += i_TimeLen;
        this.executeTime = new Date();
    }
    
    
    
    public long getRequestCount()
    {
        return requestCount;
    }


    
    public long getSuccessCount()
    {
        return successCount;
    }


    
    public double getSuccessTimeLen()
    {
        return successTimeLen;
    }
    
    
    
    /**
     * 最后执行时间点。
     *   1. 在开始执行时，此时间点会记录一次。
     *   2. 在执行结束后，此时间点会记录一次。
     *   3. 当出现异常时，此时间点保持最近一次，不变。
     *   4. 当多个线程同时操作时，记录最新的时间点。
     *   5. 未执行时，此属性为NULL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-16
     * @version     v1.0
     *
     * @return
     */
    public Date getExecuteTime()
    {
        return this.executeTime;
    }
    
    
    
    /**
     * 占位符SQL的执行。-- 无填充值的
     * 
     * @return
     */
    public XSQLGroupResult executes()
    {
        if ( Help.isNull(this.xsqlNodes) )
        {
            throw new NullPointerException("XSQLNodes is null of XSQLGroup.");
        }
        
        return this.executeGroup(null);
    }
    
    
    
    /**
     * 占位符SQL的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Values          占位符SQL的填充集合。
     * @return                  
     */
    public XSQLGroupResult executes(Map<String ,Object> i_Values)
    {
        if ( Help.isNull(this.xsqlNodes) )
        {
            throw new NullPointerException("XSQLNodes is null of XSQLGroup.");
        }
        
        return this.executeGroup(i_Values);
    }
    
    
    
    /**
     * 占位符SQL的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Obj             占位符SQL的填充对象。
     * @return                  异常时：Return.paramInt  为异常时执行XSQL的索引位置。下标从0开始。
     *                          异常时：Return.paramStr  为异常时执行的SQL
     *                          异常时：Return.exception 为异常对象
     *                          成功时：Return.paramInt  为最后一个有效执行XSQL的索引位置。下标从0开始。
     *                          成功时：Return.paramObj  为影响(Insert、Update、Delete)数据的累计行数。
     */
    @SuppressWarnings("unchecked")
    public XSQLGroupResult executes(Object i_Obj)
    {
        if ( Help.isNull(this.xsqlNodes) )
        {
            throw new NullPointerException("XSQLNodes is null of XSQLGroup.");
        }
        
        if ( null == i_Obj )
        {
            throw new NullPointerException("Object is null of XSQLGroup.");
        }
        
        try
        {
            if ( MethodReflect.isExtendImplement(i_Obj ,Map.class) )
            {
                return this.executeGroup((Map<String ,Object>)i_Obj);
            }
            else
            {
                return this.executeGroup(Help.toMap(i_Obj));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return new XSQLGroupResult(false);
    }
    
    
    
    /**
     * 执行一组XSQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     * @param io_Params         执行或查询参数
     * @return
     */
    private XSQLGroupResult executeGroup(Map<String ,Object> io_Params)
    {
        return this.executeGroup(io_Params ,new Hashtable<DataSourceGroup ,XConnection>() ,new XSQLGroupResult());
    }
    
    
    
    /**
     * 执行一组XSQL（提供给组嵌套组的执行）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     * @param io_Params           执行或查询参数
     * @param io_DSGConns         数据库连接池控制(提交、回滚、关闭)集合
     * @param io_XSQLGroupResult  执行结果
     * @return                  
     */
    private XSQLGroupResult executeGroup(Map<String ,Object> io_Params ,Map<DataSourceGroup ,XConnection> io_DSGConns ,XSQLGroupResult io_XSQLGroupResult)
    {
        if ( Help.isNull(io_Params) )
        {
            io_Params = new HashMap<String ,Object>();
        }
        
        long                              v_BeginTime = this.request().getTime();
        Map<DataSourceGroup ,XConnection> v_DSGConns  = io_DSGConns;
        XSQLGroupResult                   v_Ret       = this.executeGroup(-1 ,io_Params ,io_XSQLGroupResult ,v_DSGConns);
        
        // v_Ret.getExecSumCount().putAll(i_ExecSumCount);
        
        if ( this.cloudWait != null )
        {
            v_Ret = waitClouds(this.cloudWait ,v_Ret);
        }
        
        // 在整个组合XSQLGroup的最后执行，并只执行一次。不在查询类型XSQL节点的循环之中执行
        v_Ret = this.executeGroup_LastOnce(io_Params ,v_DSGConns ,v_Ret);
        
        // 当为子级的嵌套SQL组时，不关闭数据库连接，而是交给父组SQL组来统一关闭。ZhengWei(HY) 2016-07-30
        if ( this.superGroup == null )
        {
            if ( this.isAutoCommit )
            {
                // 统一提交、统一回滚的事务功能
                if ( v_Ret.isSuccess() )
                {
                    this.commits(v_DSGConns ,v_Ret.getExecSumCount());
                    this.success(Date.getNowTime().getTime() - v_BeginTime);
                }
                else
                {
                    this.rollbacks(v_DSGConns);
                }
                
                // 统一关闭数据库连接
                this.closeConnections(v_DSGConns);
            }
            else
            {
                // 这里不能记录成功时间及次数信息，因为外界可能手工回滚。 ZhengWei(HY) Add 2017-11-03
                v_Ret.setXsqlGroup(this);
                v_Ret.setDsgConns(v_DSGConns);
            }
        }
        else
        {
            if ( v_Ret.isSuccess() )
            {
                this.success(Date.getNowTime().getTime() - v_BeginTime);
            }
        }
        
        if ( this.isLog )
        {
            return this.logReturn(v_Ret);
        }
        else
        {
            return v_Ret;
        }
    }
    
    
    
    /**
     * 组嵌套组的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-03-03
     * @version     v1.0
     *
     * @param i_SuperNodeIndex    父XSQL节点的索引。下标从0开始。
     * @param io_Params           执行或查询参数
     * @param io_DSGConns         数据库连接池控制(提交、回滚、关闭)集合
     * @param io_XSQLGroupResult  执行结果
     * @return
     */
    private XSQLGroupResult executeGroup_Nesting(int i_SuperNodeIndex ,Map<String ,Object> io_Params ,Map<DataSourceGroup ,XConnection> io_DSGConns ,XSQLGroupResult io_XSQLGroupResult)
    {
        XSQLGroupResult v_Ret       = io_XSQLGroupResult;
        int             v_NodeIndex = i_SuperNodeIndex + 1;
        XSQLNode        v_Node      = this.xsqlNodes.get(v_NodeIndex);
        
        // 检查条件是否通过
        if ( !v_Node.isPass(io_Params) )
        {
            if ( v_Node.isNoPassContinue() )
            {
                // 检查不通过，继续向后击鼓传花
                return this.executeGroup(v_NodeIndex ,io_Params ,v_Ret ,io_DSGConns);
            }
            else
            {
                return v_Ret.setSuccess(true).setExecLastNode(i_SuperNodeIndex);
            }
        }
        
        // 拷贝父级XSQLGroup组的属性
        v_Node.getSqlGroup().setLog(this.isLog());
        // 嵌套XSQLGroup组的执行
        v_Node.getSqlGroup().superGroup = this;
        v_Ret = v_Node.getSqlGroup().executeGroup(io_Params ,io_DSGConns ,v_Ret); 
        
        // 如果是多线程并有等待标识时，一直等待并且的执行结果  Add 2018-01-24

        v_Ret = waitThreads(v_Node ,v_Ret);
        if ( v_Ret.isSuccess() )
        {
            // 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作
            if ( v_Ret.isSuccess() && v_Node.isAfterCommit() )
            {
                this.commits(io_DSGConns ,v_Ret.getExecSumCount());
            }
            
            // 继续向后击鼓传花
            return this.executeGroup(v_NodeIndex ,io_Params ,v_Ret ,io_DSGConns);
        }
        else
        {
            return v_Ret;
        }
    }
    
    
    
    /**
     * 在整个组合XSQLGroup的最后执行，并只执行一次。不在查询类型XSQL节点的循环之中执行。
     * 
     * 执行 this.lastOnce = true 的节点
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-03-03
     * @version     v1.0
     *
     * @param io_Params         执行或查询参数
     * @param io_DSGConns       数据库连接池控制(提交、回滚、关闭)集合
     * @param i_Return          与返回值同义
     * @return                  异常时：Return.paramInt  为异常时执行XSQL的索引位置。下标从0开始。
     *                          异常时：Return.paramStr  为异常时执行的SQL
     *                          异常时：Return.exception 为异常对象
     *                          成功时：Return.paramInt  为最后一个有效执行XSQL的索引位置。下标从0开始。
     *                          成功时：Return.paramObj  为影响(Insert、Update、Delete)数据的累计行数
     */
    private XSQLGroupResult executeGroup_LastOnce(Map<String ,Object> io_Params ,Map<DataSourceGroup ,XConnection> io_DSGConns ,XSQLGroupResult i_Return)
    {
        XSQLGroupResult v_Ret = i_Return;
        
        // 在整个组合XSQLGroup的最后执行，并只执行一次。不在查询类型XSQL节点的循环之中执行
        for (int v_NodeIndex=0; v_Ret.isSuccess() && v_NodeIndex<this.xsqlNodes.size(); v_NodeIndex++)
        {
            XSQLNode v_Node = this.xsqlNodes.get(v_NodeIndex);
            
            if ( !v_Node.isLastOnce() 
              || ( XSQLNode.$Type_Query            .equals(v_Node.getType()) && Help.isNull(v_Node.getReturnID()) )
              ||   XSQLNode.$Type_CollectionToQuery.equals(v_Node.getType()) )
            {
                continue;
            }
            
            // 检查条件是否通过
            if ( !v_Node.isPass(io_Params) )
            {
                if ( v_Node.isNoPassContinue() )
                {
                    // 检查不通过，允许其后的节点继续执行
                    continue;
                }
                else
                {
                    // 整体退出
                    break;
                }
            }
            
            // 执行本节点前，对之前的所有XSQL节点进行统一提交操作
            if ( v_Node.isBeforeCommit() )
            {
                this.commits(io_DSGConns ,v_Ret.getExecSumCount());
            }
            
            // 嵌套的XSQLGroup组
            if ( null != v_Node.getSqlGroup() )
            {
                v_Ret = executeGroup_Nesting(v_NodeIndex - 1 ,io_Params ,io_DSGConns ,v_Ret);
                continue;
            }
            
            int v_RetryCount = v_Node.getRetryCount();
            do
            {
                try
                {
                    boolean v_ExecRet = false;
                    int     v_RCount  = 0;
                    
                    this.logExecuteBefore(v_Node ,io_Params ,v_NodeIndex);
                    
                    if ( XSQLNode.$Type_Query.equals(v_Node.getType()) )
                    {
                        Object v_QueryRet = null;
                        
                        if ( Help.isNull(io_Params) )
                        {
                            if ( v_Node.isOneConnection() )
                            {
                                v_QueryRet = v_Node.getSql().query(this.getConnection(v_Node ,io_DSGConns));
                            }
                            else
                            {
                                v_QueryRet = v_Node.getSql().query();
                            }
                        }
                        else
                        {
                            if ( v_Node.isOneConnection() )
                            {
                                v_QueryRet = v_Node.getSql().query(io_Params ,this.getConnection(v_Node ,io_DSGConns));
                            }
                            else
                            {
                                v_QueryRet = v_Node.getSql().query(io_Params);
                            }
                        }
                        
                        // put返回查询结果集
                        this.putReturnID(v_Ret ,v_Node ,v_QueryRet);
                        v_ExecRet = true;
                    }
                    else if ( XSQLNode.$Type_CollectionToExecuteUpdate.equals(v_Node.getType()) )
                    {
                        List<Object> v_CollectionParam = getCollectionToQueryOrDB(v_Node ,io_Params);
                        
                        if ( !Help.isNull(v_CollectionParam) )
                        {
                            v_RCount = v_Node.getSql().executeUpdatesPrepared(this.getCollectionToDB(v_CollectionParam ,io_Params) ,this.getConnection(v_Node ,io_DSGConns));
                            
                            io_Params.put(              $Param_ExecCount + v_NodeIndex ,v_RCount);
                            v_Ret.getExecSumCount().put($Param_ExecCount + v_NodeIndex ,v_RCount);
                        }
                        else
                        {
                            v_RCount = 1; // 防止被回滚 v_Node.isNoUpdateRollbacks();
                        }
                        
                        // 2017-12-22 Add 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作
                        if ( v_RCount <= 0 && v_Node.isNoUpdateRollbacks() )
                        {
                            v_ExecRet = false;
                            this.rollbacks(io_DSGConns);
                        }
                        else
                        {
                            v_ExecRet = true;
                        }
                    }
                    else if ( XSQLNode.$Type_ExecuteUpdate.equals(v_Node.getType()) )
                    {
                        if ( Help.isNull(io_Params) )
                        {
                            v_RCount = v_Node.getSql().executeUpdate(this.getConnection(v_Node ,io_DSGConns));
                        }
                        else
                        {
                            v_RCount = v_Node.getSql().executeUpdate(io_Params ,this.getConnection(v_Node ,io_DSGConns));
                        }
                        
                        io_Params.put(              $Param_ExecCount + v_NodeIndex ,v_RCount);
                        v_Ret.getExecSumCount().put($Param_ExecCount + v_NodeIndex ,v_RCount);
                        
                        // 2017-12-22 Add 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作
                        if ( v_RCount <= 0 && v_Node.isNoUpdateRollbacks() )
                        {
                            v_ExecRet = false;
                            this.rollbacks(io_DSGConns);
                        }
                        else
                        {
                            v_ExecRet = true;
                        }
                    }
                    else if ( XSQLNode.$Type_Execute.equals(v_Node.getType()) )
                    {
                        if ( Help.isNull(io_Params) )
                        {
                            v_ExecRet = v_Node.getSql().execute();
                        }
                        else
                        {
                            v_ExecRet = v_Node.getSql().execute(io_Params);
                        }
                    }
                    else 
                    {
                        v_ExecRet = v_Node.executeJava(new XSQLGroupControl(this ,io_DSGConns ,v_Ret.getExecSumCount()) ,io_Params ,v_Ret.getReturns());
                    }
                    
                    this.logExecuteAfter(v_Node ,io_Params ,v_NodeIndex);
                    
                    if ( v_ExecRet )
                    {
                        // 如果是多线程并有等待标识时，一直等待并且的执行结果  Add 2018-01-24
                        v_Ret = waitThreads(v_Node ,v_Ret);
                        if ( v_Ret.isSuccess() )
                        {
                            // 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作
                            if ( v_Node.isAfterCommit() )
                            {
                                this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                            }
                            
                            v_Ret.setSuccess(true).setExecLastNode(v_NodeIndex);
                        }
                    }
                    else
                    {
                        v_Ret.setExceptionNode(v_NodeIndex);
                        v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                        v_Ret.setSuccess(false);
                    }
                }
                catch (Exception exce)
                {
                    v_Ret.setExceptionNode(v_NodeIndex);
                    v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                    v_Ret.setException(    exce);
                    v_Ret.setSuccess(false);
                }
                
                v_RetryCount--;
            } while ( !v_Ret.isSuccess() && v_RetryCount > 0 );
            if ( !v_Ret.isSuccess() ) return v_Ret;
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 执行一组XSQL（最核心的方法，它会被递归调用）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     * @param i_SuperNodeIndex  父XSQL节点的索引。下标从0开始。
     * @param io_Params         io_Params  执行或查询参数
     * @param i_XSQLGroupResult 执行结果
     * @param io_DSGConns       数据库连接池控制(提交、回滚、关闭)集合
     * @return
     */
    @SuppressWarnings("unchecked")
    protected XSQLGroupResult executeGroup(int i_SuperNodeIndex ,Map<String ,Object> io_Params ,XSQLGroupResult i_XSQLGroupResult ,Map<DataSourceGroup ,XConnection> io_DSGConns)
    {
        XSQLGroupResult v_Ret = i_XSQLGroupResult;
        v_Ret.setSuccess(true); // 2018-01-25:false
        
        int v_NodeIndex = i_SuperNodeIndex + 1;
        if ( v_NodeIndex >= this.xsqlNodes.size() )
        {
            return v_Ret.setSuccess(true).setExecLastNode(i_SuperNodeIndex);
        }
        
        XSQLNode v_Node = this.xsqlNodes.get(v_NodeIndex);
        // 在整个组合XSQLGroup的最后执行，并只执行一次。不在查询类型XSQL节点的循环之中执行
        if ( v_Node.isLastOnce() )
        {
            // 不检查，继续向后击鼓传花
            return this.executeGroup(v_NodeIndex ,io_Params ,v_Ret ,io_DSGConns);
        }
        
        // 检查条件是否通过
        if ( !v_Node.isPass(io_Params) )
        {
            if ( v_Node.isNoPassContinue() )
            {
                // 检查不通过，继续向后击鼓传花
                return this.executeGroup(v_NodeIndex ,io_Params ,v_Ret ,io_DSGConns);
            }
            else
            {
                return v_Ret.setSuccess(true).setExecLastNode(i_SuperNodeIndex);
            }
        }
        
        // 执行本节点前，对之前的所有XSQL节点进行统一提交操作
        if ( v_Node.isBeforeCommit() )
        {
            this.commits(io_DSGConns ,v_Ret.getExecSumCount());
        }
        
        // 嵌套的XSQLGroup组
        if ( null != v_Node.getSqlGroup() )
        {
            v_Ret = executeGroup_Nesting(i_SuperNodeIndex ,io_Params ,io_DSGConns ,v_Ret);
            return v_Ret;
        }
        
        if ( XSQLNode.$Type_Query            .equals(v_Node.getType()) 
          || XSQLNode.$Type_CollectionToQuery.equals(v_Node.getType()) )
        {
            // 控制其后节点循环执行的查询
            if (    Help.isNull(v_Node.getReturnID())
               || (!Help.isNull(v_Node.getReturnID()) && v_Node.isReturnQuery()) 
               || XSQLNode.$Type_CollectionToQuery.equals(v_Node.getType()) )
            {
                List<Object> v_QueryRet = null;
                
                int v_RetryCount = v_Node.getRetryCount();
                do
                {
                    try
                    {
                        this.logExecuteBefore(v_Node ,io_Params ,v_NodeIndex);
                        
                        if ( XSQLNode.$Type_CollectionToQuery.equals(v_Node.getType()) )
                        {
                            v_QueryRet = getCollectionToQueryOrDB(v_Node ,io_Params);
                        }
                        else if ( v_Node.isBigData() )
                        {
                            XSQLGroupBigData v_XSQLBigData = new XSQLGroupBigData(this 
                                                                                 ,v_NodeIndex
                                                                                 ,v_Node
                                                                                 ,io_Params
                                                                                 ,v_Ret
                                                                                 ,io_DSGConns);
                            
                            if ( Help.isNull(io_Params) )
                            {
                                if ( v_Node.isOneConnection() )
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().queryBigData(this.getConnection(v_Node ,io_DSGConns) ,v_XSQLBigData);
                                }
                                else
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().queryBigData(v_XSQLBigData);
                                }
                            }
                            else
                            {
                                if ( v_Node.isOneConnection() )
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().queryBigData(io_Params ,this.getConnection(v_Node ,io_DSGConns) ,v_XSQLBigData);
                                }
                                else
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().queryBigData(io_Params ,v_XSQLBigData);
                                }
                            }
                        }
                        else
                        {
                            if ( Help.isNull(io_Params) )
                            {
                                if ( v_Node.isOneConnection() )
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().query(this.getConnection(v_Node ,io_DSGConns));
                                }
                                else
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().query();
                                }
                            }
                            else
                            {
                                if ( v_Node.isOneConnection() )
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().query(io_Params ,this.getConnection(v_Node ,io_DSGConns));
                                }
                                else
                                {
                                    v_QueryRet = (List<Object>)v_Node.getSql().query(io_Params);
                                }
                            }
                        }
                        
                        this.logExecuteAfter(v_Node ,io_Params ,v_NodeIndex);
                    }
                    catch (Exception exce)
                    {
                        v_Ret.setExceptionNode(v_NodeIndex);
                        v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                        v_Ret.setException(    exce);
                        v_Ret.setSuccess(false);
                    }
                    
                    v_RetryCount--;
                } while ( !v_Ret.isSuccess() && v_RetryCount > 0 );
                if ( !v_Ret.isSuccess() ) return v_Ret;
                
                // 查询并返回：返回结果集，控制其后节点执行：返回结果集的同时，还将控制其后XSQL节点的执行次数。ZhengWei(HY) Add 2017-05-17
                if ( !Help.isNull(v_Node.getReturnID()) )
                {
                    this.putReturnID(v_Ret ,v_Node ,v_QueryRet);
                }
                
                
                // 这里只处理内存中的集合循环遍历，因为数据库中的已交于大数据处理接口来处理了  ZhengWei(HY) Add 2018-01-18
                if ( (XSQLNode.$Type_CollectionToQuery.equals(v_Node.getType()) || !v_Node.isBigData()) && !Help.isNull(v_QueryRet) )
                {
                    if ( v_Node.isThread() )
                    {
                        newTaskGroupByThreads(v_Node ,v_Ret ,io_Params);
                    }
                    
                    // 行级对象是：Map<String ,Object>
                    if ( v_QueryRet.get(0) instanceof Map || MethodReflect.isExtendImplement(v_QueryRet.get(0) ,Map.class) )
                    {
                        if ( !Help.isNull(v_Node.getQueryReturnID()) )
                        {
                            PartitionMap<String ,Object> v_QueryReturnPart = (PartitionMap<String ,Object>)v_Ret.getReturns().get(v_Node.getQueryReturnID());
                            if ( null == v_QueryReturnPart )
                            {
                                v_QueryReturnPart = new TablePartition<String ,Object>();
                                // put返回查询结果集
                                v_Ret.getReturns().put(v_Node.getQueryReturnID() ,v_QueryReturnPart);
                            }
                            
                            Map<String ,Object> v_RowPrevious = null;
                            for (int v_RowIndex=0; v_RowIndex<v_QueryRet.size(); v_RowIndex++)
                            {
                                Object              v_QRItem    = v_QueryRet.get(v_RowIndex);
                                Map<String ,Object> v_QRItemMap = (Map<String ,Object>)v_QRItem;
                                Map<String ,Object> v_Params    = new HashMap<String ,Object>(io_Params);
                                
                                v_Params.putAll(v_QRItemMap);
                                v_Params.put($Param_RowIndex ,v_RowIndex);
                                v_Params.put($Param_RowSize  ,v_QueryRet.size());
                                if ( v_RowIndex == v_QueryRet.size() - 1 )
                                {
                                    v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                    v_Params.put($Param_RowNext     ,null);
                                }
                                else
                                {
                                    v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                    v_Params.put($Param_RowNext     ,v_QueryRet.get(v_RowIndex + 1));
                                }
                                
                                if ( v_Node.isThread() )
                                {
                                    // 多线程并发执行  Add 2017-02-22
                                    XSQLGroupTask v_Task = new XSQLGroupTask(this ,v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                    v_Ret.taskGroup.addTaskAndStart(v_Task);
                                }
                                else
                                {
                                    v_Ret = this.executeGroup(v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                    if ( !v_Ret.isSuccess() )
                                    {
                                        // 循环执行时，只要有一个执行异常，就全部退出
                                        return v_Ret;
                                    }
                                    else if ( v_Node.isPerAfterCommit() )
                                    {
                                        this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                                    }
                                }
                                
                                v_QueryReturnPart.putRows(v_QRItemMap);  // 只有执行成功后才put返回查询结果集
                                v_RowPrevious = Help.setMapValues(v_QRItemMap ,v_Params);
                            }
                        }
                        else
                        {
                            Map<String ,Object> v_RowPrevious = null;
                            for (int v_RowIndex=0; v_RowIndex<v_QueryRet.size(); v_RowIndex++)
                            {
                                Object              v_QRItem    = v_QueryRet.get(v_RowIndex);
                                Map<String ,Object> v_QRItemMap = (Map<String ,Object>)v_QRItem;
                                Map<String ,Object> v_Params    = new HashMap<String ,Object>(io_Params);
                                
                                v_Params.putAll(v_QRItemMap);
                                v_Params.put($Param_RowIndex ,v_RowIndex);
                                v_Params.put($Param_RowSize  ,v_QueryRet.size());
                                if ( v_RowIndex == v_QueryRet.size() - 1 )
                                {
                                    v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                    v_Params.put($Param_RowNext     ,null);
                                }
                                else
                                {
                                    v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                    v_Params.put($Param_RowNext     ,v_QueryRet.get(v_RowIndex + 1));
                                }
                                
                                if ( v_Node.isThread() )
                                {
                                    // 多线程并发执行  Add 2017-02-22
                                    XSQLGroupTask v_Task = new XSQLGroupTask(this ,v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                    v_Ret.taskGroup.addTaskAndStart(v_Task);
                                }
                                else
                                {
                                    v_Ret = this.executeGroup(v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                    if ( !v_Ret.isSuccess() )
                                    {
                                        // 循环执行时，只要有一个执行异常，就全部退出
                                        return v_Ret;
                                    }
                                    else if ( v_Node.isPerAfterCommit() )
                                    {
                                        this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                                    }
                                }
                                
                                v_RowPrevious = Help.setMapValues(v_QRItemMap ,v_Params);
                            }
                        }
                    }
                    // 行级对象是：Java Bean。需要转成Map<String ,Object>
                    else
                    {
                        try
                        {
                            if ( !Help.isNull(v_Node.getQueryReturnID()) )
                            {
                                PartitionMap<String ,Object> v_QueryReturnPart = (PartitionMap<String ,Object>)v_Ret.getReturns().get(v_Node.getQueryReturnID());
                                if ( null == v_QueryReturnPart )
                                {
                                    v_QueryReturnPart = new TablePartition<String ,Object>();
                                    // put返回查询结果集
                                    v_Ret.getReturns().put(v_Node.getQueryReturnID() ,v_QueryReturnPart);
                                }
                                
                                Map<String ,Object> v_RowPrevious = null;
                                for (int v_RowIndex=0; v_RowIndex<v_QueryRet.size(); v_RowIndex++)
                                {
                                    Object              v_QRItem    = v_QueryRet.get(v_RowIndex);
                                    Map<String ,Object> v_QRItemMap = Help.toMap(v_QRItem ,null ,false);
                                    Map<String ,Object> v_Params    = new HashMap<String ,Object>(io_Params);
                                    
                                    v_Params.putAll(v_QRItemMap);
                                    v_Params.put($Param_RowIndex ,v_RowIndex);
                                    v_Params.put($Param_RowSize  ,v_QueryRet.size());
                                    if ( v_RowIndex == v_QueryRet.size() - 1 )
                                    {
                                        v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                        v_Params.put($Param_RowNext     ,null);
                                    }
                                    else
                                    {
                                        v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                        v_Params.put($Param_RowNext     ,v_QueryRet.get(v_RowIndex + 1));
                                    }
                                    
                                    if ( v_Node.isThread() )
                                    {
                                        // 多线程并发执行  Add 2017-02-22
                                        XSQLGroupTask v_Task = new XSQLGroupTask(this ,v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                        v_Ret.taskGroup.addTaskAndStart(v_Task);
                                    }
                                    else
                                    {
                                        v_Ret = this.executeGroup(v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                        if ( !v_Ret.isSuccess() )
                                        {
                                            // 循环执行时，只要有一个执行异常，就全部退出
                                            return v_Ret;
                                        }
                                        else if ( v_Node.isPerAfterCommit() )
                                        {
                                            this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                                        }
                                    }
                                    
                                    v_QueryReturnPart.putRows(v_QRItemMap);  // 只有执行成功后才put返回查询结果集
                                    v_RowPrevious = Help.setMapValues(v_QRItemMap ,v_Params);
                                }
                            }
                            else
                            {
                                Map<String ,Object> v_RowPrevious = null;
                                for (int v_RowIndex=0; v_RowIndex<v_QueryRet.size(); v_RowIndex++)
                                {
                                    Object              v_QRItem    = v_QueryRet.get(v_RowIndex);
                                    Map<String ,Object> v_QRItemMap = Help.toMap(v_QRItem ,null ,false);
                                    Map<String ,Object> v_Params    = new HashMap<String ,Object>(io_Params);
                                    
                                    v_Params.putAll(v_QRItemMap);
                                    v_Params.put($Param_RowIndex ,v_RowIndex);
                                    v_Params.put($Param_RowSize  ,v_QueryRet.size());
                                    if ( v_RowIndex == v_QueryRet.size() - 1 )
                                    {
                                        v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                        v_Params.put($Param_RowNext     ,null);
                                    }
                                    else
                                    {
                                        v_Params.put($Param_RowPrevious ,v_RowPrevious);
                                        v_Params.put($Param_RowNext     ,v_QueryRet.get(v_RowIndex + 1));
                                    }
                                    
                                    if ( v_Node.isThread() )
                                    {
                                        // 多线程并发执行  Add 2017-02-22
                                        XSQLGroupTask v_Task = new XSQLGroupTask(this ,v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                        v_Ret.taskGroup.addTaskAndStart(v_Task);
                                    }
                                    else
                                    {
                                        v_Ret = this.executeGroup(v_NodeIndex ,v_Params ,v_Ret ,io_DSGConns);
                                        if ( !v_Ret.isSuccess() )
                                        {
                                            // 循环执行时，只要有一个执行异常，就全部退出
                                            return v_Ret;
                                        }
                                        else if ( v_Node.isPerAfterCommit() )
                                        {
                                            this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                                        }
                                    }
                                    
                                    v_RowPrevious = Help.setMapValues(v_QRItemMap ,v_Params);
                                }
                            }
                        }
                        catch (Exception exce)
                        {
                            v_Ret.setExceptionNode(v_NodeIndex);
                            v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                            v_Ret.setException(    exce);
                            v_Ret.setSuccess(false);
                            return v_Ret;
                        }
                    }
                    
                    // 如果是多线程并有等待标识时，一直等待并且的执行结果  (原来的位置)
                    v_Ret = waitThreads(v_Node ,v_Ret);
                    if ( v_Ret.isSuccess() )
                    {
                        v_Ret.setSuccess(true).setExecLastNode(v_NodeIndex);
                    }
                }
                else
                {
                    // 如果是多线程并有等待标识时，一直等待并且的执行结果  Add 2018-01-24
                    v_Ret = waitThreads(v_Node ,v_Ret);
                    if ( v_Ret.isSuccess() )
                    {
                        v_Ret.setSuccess(true).setExecLastNode(v_NodeIndex);
                    }
                }
            }
            // 返回查询结果集的查询
            else
            {
                Object v_QueryRet = null;
                int v_RetryCount = v_Node.getRetryCount();
                do
                {
                    try
                    {
                        this.logExecuteBefore(v_Node ,io_Params ,v_NodeIndex);
                        
                        if ( Help.isNull(io_Params) )
                        {
                            v_QueryRet = v_Node.getSql().query();
                        }
                        else
                        {
                            v_QueryRet = v_Node.getSql().query(io_Params);
                        }
                        
                        this.logExecuteAfter(v_Node ,io_Params ,v_NodeIndex);
                    }
                    catch (Exception exce)
                    {
                        v_Ret.setExceptionNode(v_NodeIndex);
                        v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                        v_Ret.setException(    exce);
                        v_Ret.setSuccess(false);
                    }
                    
                    v_RetryCount--;
                } while ( !v_Ret.isSuccess() && v_RetryCount > 0 );
                if ( !v_Ret.isSuccess() ) return v_Ret;
                
                // 如果是多线程并有等待标识时，一直等待并且的执行结果  Add 2018-01-24
                v_Ret = waitThreads(v_Node ,v_Ret);
                if ( v_Ret.isSuccess() )
                {
                    // 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作
                    if ( v_Node.isAfterCommit() )
                    {
                        this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                    }
                    
                    v_Ret.setSuccess(true).setExecLastNode(v_NodeIndex);
                    
                    // put返回查询结果集
                    this.putReturnID(v_Ret ,v_Node ,v_QueryRet);
                    
                    // 继续向后击鼓传花
                    return this.executeGroup(v_NodeIndex ,io_Params ,v_Ret ,io_DSGConns);
                }
                else
                {
                    return v_Ret;
                }
            }
        }
        else
        {
            int v_RetryCount = v_Node.getRetryCount();
            do
            {
                try
                {
                    boolean v_ExecRet = false;
                    int     v_RCount  = 0;
                    
                    this.logExecuteBefore(v_Node ,io_Params ,v_NodeIndex);
                    
                    if ( XSQLNode.$Type_CollectionToExecuteUpdate.equals(v_Node.getType()) )
                    {
                        List<Object> v_CollectionParam = getCollectionToQueryOrDB(v_Node ,io_Params);
                        
                        if ( !Help.isNull(v_CollectionParam) )
                        {
                            if ( v_Node.isFreeConnection() )
                            {
                                v_RCount = v_Node.getSql().executeUpdatesPrepared(this.getCollectionToDB(v_CollectionParam ,io_Params));
                            }
                            else
                            {
                                v_RCount = v_Node.getSql().executeUpdatesPrepared(this.getCollectionToDB(v_CollectionParam ,io_Params) ,this.getConnection(v_Node ,io_DSGConns));
                            }
                            
                            io_Params.put(              $Param_ExecCount + v_NodeIndex ,v_RCount);
                            v_Ret.getExecSumCount().put($Param_ExecCount + v_NodeIndex ,v_RCount);
                        }
                        else
                        {
                            v_RCount = 1; // 防止被回滚 v_Node.isNoUpdateRollbacks();
                        }
                        
                        // 2017-12-22 Add 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作
                        if ( v_RCount <= 0 && v_Node.isNoUpdateRollbacks() )
                        {
                            v_ExecRet = false;
                            this.rollbacks(io_DSGConns);
                        }
                        else
                        {
                            v_ExecRet = true;
                        }
                    }
                    else if ( XSQLNode.$Type_ExecuteUpdate.equals(v_Node.getType()) )
                    {
                        if ( Help.isNull(io_Params) )
                        {
                            if ( v_Node.isFreeConnection() )
                            {
                                v_RCount = v_Node.getSql().executeUpdate();
                            }
                            else
                            {
                                v_RCount = v_Node.getSql().executeUpdate(this.getConnection(v_Node ,io_DSGConns));
                            }
                        }
                        else
                        {
                            if ( v_Node.isFreeConnection() )
                            {
                                v_RCount = v_Node.getSql().executeUpdate(io_Params);
                            }
                            else
                            {
                                v_RCount = v_Node.getSql().executeUpdate(io_Params ,this.getConnection(v_Node ,io_DSGConns));
                            }
                        }
                        
                        io_Params.put(              $Param_ExecCount + v_NodeIndex ,v_RCount);
                        v_Ret.getExecSumCount().put($Param_ExecCount + v_NodeIndex ,v_RCount);
                        
                        // 2017-12-22 Add 当未更新任何数据（操作影响的数据量为0条）时，是否执行事务统一回滚操作
                        if ( v_RCount <= 0 && v_Node.isNoUpdateRollbacks() )
                        {
                            v_ExecRet = false;
                            this.rollbacks(io_DSGConns);
                        }
                        else
                        {
                            v_ExecRet = true;
                        }
                    }
                    else if ( XSQLNode.$Type_Execute.equals(v_Node.getType()) )
                    {
                        if ( Help.isNull(io_Params) )
                        {
                            v_ExecRet = v_Node.getSql().execute();
                        }
                        else
                        {
                            v_ExecRet = v_Node.getSql().execute(io_Params);
                        }
                    }
                    else 
                    {
                        // $Type_ExecuteJava
                        v_ExecRet = v_Node.executeJava(new XSQLGroupControl(this ,io_DSGConns ,v_Ret.getExecSumCount()) ,io_Params ,v_Ret.getReturns());
                    }
                    
                    this.logExecuteAfter(v_Node ,io_Params ,v_NodeIndex);
                    
                    if ( v_ExecRet )
                    {
                        // 如果是多线程并有等待标识时，一直等待并且的执行结果  Add 2018-01-24
                        v_Ret = waitThreads(v_Node ,v_Ret);
                        if ( v_Ret.isSuccess() )
                        {
                            // 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作
                            if ( v_Node.isAfterCommit() )
                            {
                                this.commits(io_DSGConns ,v_Ret.getExecSumCount());
                            }
                            
                            v_Ret.setSuccess(true).setExecLastNode(v_NodeIndex);
                            
                            // 继续向后击鼓传花
                            return this.executeGroup(v_NodeIndex ,io_Params ,v_Ret ,io_DSGConns);
                        }
                    }
                    else
                    {
                        v_Ret.setExceptionNode(v_NodeIndex);
                        v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                        v_Ret.setSuccess(false);
                    }
                }
                catch (Exception exce)
                {
                    v_Ret.setExceptionNode(v_NodeIndex);
                    v_Ret.setExceptionSQL (this.getSQL(v_Node ,io_Params));
                    v_Ret.setException(    exce);
                    v_Ret.setSuccess(false);
                }
                
                v_RetryCount--;
            } while ( !v_Ret.isSuccess() && v_RetryCount > 0 );
            if ( !v_Ret.isSuccess() ) return v_Ret;
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 创建多线程任务组
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-25
     * @version     v1.0
     *
     * @param i_Node
     * @param i_XSQLGroupResult
     * @param io_Params
     */
    public synchronized void newTaskGroupByThreads(XSQLNode i_Node ,XSQLGroupResult i_XSQLGroupResult ,Map<String ,Object> io_Params)
    {
        if ( i_XSQLGroupResult.taskGroup == null )
        {
            i_XSQLGroupResult.taskGroup = new TaskGroup(getSQL(i_Node ,io_Params));
        }
    }
    
    
    
    /**
     * 等待所有线程均执行完成
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-24
     * @version     v1.0
     *
     */
    public XSQLGroupResult waitThreads(XSQLNode i_Node ,XSQLGroupResult i_XSQLGroupResult)
    {
        if ( i_Node.isThreadWait() && i_XSQLGroupResult.taskGroup != null )
        {
            long v_Interval = i_Node.getThreadWaitInterval();
            if ( v_Interval <= 0 )
            {
                v_Interval = ThreadPool.getIntervalTime() * 3;
            }
            
            while ( !i_XSQLGroupResult.taskGroup.isTasksFinish() )
            {
                // 一直等待并且的执行结果
                try
                {
                    Thread.sleep(v_Interval);
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
            }
            
            // 获取执行结果
            XSQLGroupTask v_Task = (XSQLGroupTask)i_XSQLGroupResult.taskGroup.getTask(0);
            i_XSQLGroupResult = v_Task.getXsqlGroupResult();
            for (int v_TaskIndex=0; v_TaskIndex<i_XSQLGroupResult.taskGroup.size(); v_TaskIndex++)
            {
                v_Task = (XSQLGroupTask)i_XSQLGroupResult.taskGroup.getTask(v_TaskIndex);
                
                if ( v_Task != null )
                {
                    if ( v_Task.getXsqlGroupResult() != null )
                    {
                        if ( !v_Task.getXsqlGroupResult().isSuccess() )
                        {
                            return v_Task.getXsqlGroupResult();
                        }
                    }
                }
            }
        }
        
        if ( i_Node.getCloudWait() != null )
        {
            return waitClouds(i_Node.getCloudWait() ,i_XSQLGroupResult);
        }
        else
        {
            return i_XSQLGroupResult;
        }
    }
    
    
    
    /**
     * 等待云服务的计算完成。
     * 
     * 先等待线程，再等待云服务。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-30
     * @version     v1.0
     *
     * @param i_CloudWaitNode   执行云计算的XSQL节点
     * @param i_XSQLGroupResult
     * @return
     */
    public XSQLGroupResult waitClouds(XSQLNode i_CloudWaitNode ,XSQLGroupResult i_XSQLGroupResult)
    {
        long v_Interval = Math.max(i_CloudWaitNode.getCloudWaitInterval() ,i_CloudWaitNode.getCloudExecInterval());
        while ( i_CloudWaitNode.getCloudBusyCount() - i_CloudWaitNode.getCloudErrorCount() >= 1 )
        {
            // 一直等待并且的执行结果
            try
            {
                Thread.sleep(v_Interval);
            }
            catch (Exception exce)
            {
                // Nothing.
            }
        }
        
        // 2018-02-22 还原各状态参数
        i_CloudWaitNode.setCloudBusyCount (0);
        i_CloudWaitNode.setCloudErrorCount(0);
        for (int i=i_CloudWaitNode.getCloudServersList().size(); i>=1; i--)
        {
            i_CloudWaitNode.getCloudServersList().next().setIdle(true);
        }
        
        return i_XSQLGroupResult;
    }
    
    
    
    /**
     * 填充返回查询结果集（两种模式：追加、覆盖）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-26
     * @version     v1.0
     *
     * @param io_Result     组合一组XSQL的执行结果
     * @param i_Node        当前节点对象
     * @param i_QueryDatas  查询出的结果集对象
     */
    @SuppressWarnings({"unchecked" ,"rawtypes"})
    private void putReturnID(XSQLGroupResult io_Result ,XSQLNode i_Node ,Object i_QueryDatas)
    {
        // "追加方式"：多个相同标示ID，会向首个有值的结果集对象中追加其它结果集对象。
        if ( i_Node.isReturnAppend() )
        {
            Object v_FirstDatas = io_Result.getReturns().get(i_Node.getReturnID());
            if ( v_FirstDatas == null )
            {
                io_Result.getReturns().put(i_Node.getReturnID() ,i_QueryDatas);
            }
            else if ( MethodReflect.isExtendImplement(v_FirstDatas ,Map.class) )
            {
                ((Map)v_FirstDatas).putAll((Map)i_QueryDatas);
            }
            else if ( MethodReflect.isExtendImplement(v_FirstDatas ,List.class) )
            {
                ((List)v_FirstDatas).addAll((List)i_QueryDatas);
            }
            else if (  MethodReflect.isExtendImplement(v_FirstDatas ,Set.class) )
            {
                ((Set)v_FirstDatas).addAll((Set)i_QueryDatas);
            }
            else
            {
                io_Result.getReturns().put(i_Node.getReturnID() ,i_QueryDatas);
            }
        }
        // "覆盖方式"：多个相同标示ID，会相互覆盖
        else
        {
            io_Result.getReturns().put(i_Node.getReturnID() ,i_QueryDatas);
        }
    }
    
    
    
    /**
     * 获取参数中的集合对象
     * 
     *   XSQLNode.getCollectionID()当为空时，表示将整个 XSQLGroup.execute() 方法的入参当为查询结果来用（前提是：入参类型必须为：List、Set、Map(会通过Hetp.toList()转为List)）。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-30
     * @version     v1.0
     *
     * @param i_Node            XSQL节点
     * @param io_Params         执行或查询参数
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Object> getCollectionToQueryOrDB(XSQLNode i_Node ,Map<String ,Object> io_Params)
    {
        List<Object> v_QueryRet = null;
        
        if ( Help.isNull(i_Node.getCollectionID()) )
        {
            v_QueryRet = Help.toList(io_Params);
        }
        else
        {
            Object v_MapValue = MethodReflect.getMapValue(io_Params ,i_Node.getCollectionID());
            
            if ( v_MapValue == null )
            {
                // Nothing.
            }
            else if ( MethodReflect.isExtendImplement(v_MapValue ,List.class) )
            {
                v_QueryRet = (List<Object>)v_MapValue;
            }
            else if ( MethodReflect.isExtendImplement(v_MapValue ,Set.class) )
            {
                v_QueryRet = Help.toList((Set<Object>)v_MapValue);
            }
            else if ( MethodReflect.isExtendImplement(v_MapValue ,Map.class) )
            {
                v_QueryRet = Help.toList((Map<? ,Object>)v_MapValue);
            }
            else
            {
                v_QueryRet = null;
            }
        }
        
        return v_QueryRet;
    }
    
    
    
    /**
     * 生成批量执行的参数集合。
     * 将 io_Params 填充到每一个 i_CollectionList 元素中。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-02
     * @version     v1.0
     *              v2.0  减少if语句的执行次数，提升执行性能。
     *
     * @param i_CollectionList
     * @param io_Params
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    private List<Map<String ,Object>> getCollectionToDB(List<Object> i_CollectionList ,Map<String ,Object> io_Params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        List<Map<String ,Object>> v_CollectionMap = new ArrayList<Map<String ,Object>>();
        Object                    v_OneObject     = i_CollectionList.get(0);
        
        if ( MethodReflect.isExtendImplement(v_OneObject ,Map.class) )
        {
            for (Object v_Item : i_CollectionList)
            {
                Map<String ,Object> v_ItemMap = new HashMap<String ,Object>();
                
                v_ItemMap.putAll(io_Params);
                v_ItemMap.putAll((Map<String ,Object>)v_Item);
                
                v_CollectionMap.add(v_ItemMap);
            }
        }
        else
        {
            for (Object v_Item : i_CollectionList)
            {
                Map<String ,Object> v_ItemMap = new HashMap<String ,Object>();
                
                v_ItemMap.putAll(io_Params);
                v_ItemMap.putAll(Help.toMap(v_Item));
                
                v_CollectionMap.add(v_ItemMap);
            }
        }
        
        return v_CollectionMap;
    }
    
    
    
    /**
     * 获取数据库连接。
     * 
     * 其数据库连接，统一由本类管理（包含打开及关闭）。实现统一提交、统一回滚的事务功能。
     * 
     * 对于Insert、Update、Delete操作，同一个数据库只使用一个数据库连接，不再另外打开新的数据库连接。
     * 但对于查询的操作，每次查询都是打开一个新的数据库连接。但，也可通过 XSQLNode.oneConnection 参数改变连接占用模型。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param i_Node
     * @param io_DSGConns
     * @return
     * @throws SQLException
     */
    private synchronized Connection getConnection(XSQLNode i_Node ,Map<DataSourceGroup ,XConnection> io_DSGConns) throws SQLException
    {
        XConnection v_XConn = io_DSGConns.get(i_Node.getSql().getDataSourceGroup());
        
        if ( null == v_XConn )
        {
            DataSourceGroup v_DSG = i_Node.getSql().getDataSourceGroup();
            v_XConn = new XConnection(i_Node.getSql().getConnection(v_DSG));
            v_XConn.getConn().setAutoCommit(false);                 // 不自动提交，而是之后统一提交（或回滚）
            
            io_DSGConns.put(v_DSG ,v_XConn);
        }
        
        v_XConn.setCommit(false);  // 内部标记为：没有 "提交" 过的连接
        return v_XConn.getConn();
    }
    
    
    
    /**
     * 统一提交的事务功能
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param io_DSGConns
     * @param i_ExecSumCount    累计总行数（可选参数，可为null）
     */
    protected synchronized void commits(Map<DataSourceGroup ,XConnection> io_DSGConns ,Counter<String> i_ExecSumCount)
    {
        if ( !Help.isNull(io_DSGConns) )
        {
            for (XConnection v_XConn : io_DSGConns.values())
            {
                try
                {
                    if ( !v_XConn.isCommit() )
                    {
                        if ( this.isLog )
                        {
                            System.out.print("-- " + Date.getNowTime().getFullMilli() + " Commit ...");
                        }
                        
                        v_XConn.setCommit(true);
                        v_XConn.getConn().commit();
                        
                        if ( this.isLog )
                        {
                            if ( i_ExecSumCount != null )
                            {
                                System.out.println("  " + i_ExecSumCount.getSumValue() + " rows affected.");
                            }
                            else
                            {
                                System.out.println("  OK.");
                            }
                        }
                    }
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
        }
    }
    
    
    
    /**
     * 统一回滚的事务功能
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param io_DSGConns
     */
    protected synchronized void rollbacks(Map<DataSourceGroup ,XConnection> io_DSGConns)
    {
        if ( !Help.isNull(io_DSGConns) )
        {
            for (XConnection v_XConn : io_DSGConns.values())
            {
                try
                {
                    if ( !v_XConn.isCommit() )
                    {
                        if ( this.isLog )
                        {
                            System.out.print("-- " + Date.getNowTime().getFullMilli() + " Rollback ...");
                        }
                        
                        v_XConn.setCommit(true);
                        v_XConn.getConn().rollback();
                        
                        if ( this.isLog )
                        {
                            System.out.println(" OK");
                        }
                    }
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
        }
    }
    
    
    
    /**
     * 统一关闭数据库连接
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param io_DSGConns
     */
    protected synchronized void closeConnections(Map<DataSourceGroup ,XConnection> io_DSGConns)
    {
        if ( !Help.isNull(io_DSGConns) )
        {
            for (XConnection v_XConn : io_DSGConns.values())
            {
                try
                {
                    // 在嵌套组时，内部嵌套的组会先关闭连接
                    if ( !v_XConn.getConn().isClosed() )
                    {
                        v_XConn.getConn().setAutoCommit(true);
                        v_XConn.getConn().close();
                    }
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
                finally
                {
                    v_XConn = null;
                }
            }
            
            io_DSGConns = new Hashtable<DataSourceGroup ,XConnection>();
        }
    }
    
    
    
    /**
     * 获取可执行SQL语句
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param i_Node
     * @param i_Params
     * @return
     */
    protected String getSQL(XSQLNode i_Node ,Map<String ,Object> i_Params)
    {
        if ( null != i_Node.getSql() )
        {
            if ( XSQLNode.$Type_CollectionToExecuteUpdate.equals(i_Node.getType()) )
            {
                return i_Node.getSql().getContent().getPreparedSQL().getSQL();
            }
            else
            {
                if ( Help.isNull(i_Params) )
                {
                    return i_Node.getSql().getContent().getSQL();
                }
                else
                {
                    return i_Node.getSql().getContent().getSQL(i_Params);
                }
            }
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 输出执行SQL之前的轨迹日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-03-03
     * @version     v1.0
     *
     * @param i_Node
     * @param i_Params
     * @param i_NodeIndex
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    private synchronized void logExecuteBefore(XSQLNode i_Node ,Map<String ,Object> i_Params ,int i_NodeIndex) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if ( this.isLog )
        {
            if ( XSQLNode.$Type_ExecuteJava.equals(i_Node.getType()) )
            {
                System.out.println("-- " + Date.getNowTime().getFullMilli() + " " + i_Node.getXjavaID() + "." + i_Node.getMethodName() + "(Map ,Map).");
            }
            else if ( XSQLNode.$Type_CollectionToQuery.equals(i_Node.getType()) )
            {
                System.out.println("-- " + Date.getNowTime().getFullMilli() + " CollectionToQuery = " + Help.NVL(i_Node.getCollectionID() ,"整个入参对象"));
            }
            else
            {
                if ( Help.isNull(i_Node.getComment()) )
                {
                    System.out.print("-- " + Date.getNowTime().getFullMilli() + " " + this.getSQL(i_Node ,i_Params) + " ... ... ");
                }
                else
                {
                    System.out.print("-- " + Date.getNowTime().getFullMilli() + " " + i_Node.getComment() + "：" + this.getSQL(i_Node ,i_Params) + " ... ... ");
                }
            }
        }
    }
    
    
    
    /**
     * 输出执行SQL之后的轨迹日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param i_Node
     * @param i_Params
     * @param i_NodeIndex
     */
    private synchronized void logExecuteAfter(XSQLNode i_Node ,Map<String ,Object> i_Params ,int i_NodeIndex)
    {
        if ( this.isLog )
        {
            if ( XSQLNode.$Type_ExecuteUpdate            .equals(i_Node.getType())
              || XSQLNode.$Type_CollectionToExecuteUpdate.equals(i_Node.getType()) )
            {
                String v_ExeCount = $Param_ExecCount + i_NodeIndex;
                System.out.println("  " + v_ExeCount + "=" + i_Params.get(v_ExeCount));
            }
            else if ( XSQLNode.$Type_ExecuteJava      .equals(i_Node.getType())
                   || XSQLNode.$Type_CollectionToQuery.equals(i_Node.getType()) )
            {
                // Nothing.
            }
            else
            {
                System.out.println(" OK.");
            }
        }
    }
    
    
    
    /**
     * 输出最终执行异常时的日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     *
     * @param i_Return
     */
    public synchronized XSQLGroupResult logReturn(XSQLGroupResult i_Return)
    {
        if ( null != i_Return && !i_Return.isSuccess() )
        {
            StringBuilder v_Buffer = new StringBuilder();
            
            v_Buffer.append("-- Error Index：").append(i_Return.getExceptionNode()).append("\n");
            v_Buffer.append("-- Error SQL：  ").append(i_Return.getExceptionSQL()).append("\n");
            
            if ( null != i_Return.getException() )
            {
                v_Buffer.append("-- Error MSG：  ").append(i_Return.getException().getMessage()).append("\n");
                System.out.print(v_Buffer.toString());
                
                i_Return.getException().printStackTrace();
            }
            else
            {
                System.out.print(v_Buffer.toString());
            }
            
            
        }
        
        return i_Return;
    }
    
    
    
    /**
     * 添加SQL节点
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     * @param i_XSQLNode
     */
    public void setSqlNode(XSQLNode i_XSQLNode)
    {
        if ( null != i_XSQLNode )
        {
            this.xsqlNodes.add(i_XSQLNode);
        }
    }


    
    /**
     * 获取：执行SQL节点的集合
     */
    public List<XSQLNode> getXsqlNodes()
    {
        return xsqlNodes;
    }


    
    /**
     * 设置：执行SQL节点的集合
     * 
     * @param xsqlNodes 
     */
    public void setXsqlNodes(List<XSQLNode> xsqlNodes)
    {
        this.xsqlNodes = xsqlNodes;
    }

    
    
    /**
     * 获取：等待哪个节点上的云服务计算完成。与XSQLNode.cloudWait同义
     *      但，此属性表示XSQL组整体完成前的最后等待哪个节点上的云服务计算。
     *      
     *      在所有lastOnce标记的XSQL节点执行之前执行此等待操作。
     */
    public XSQLNode getCloudWait()
    {
        return cloudWait;
    }
    

    
    /**
     * 设置：等待哪个节点上的云服务计算完成。与XSQLNode.cloudWait同义
     *      但，此属性表示XSQL组整体完成前的最后等待哪个节点上的云服务计算。
     *      
     *      在所有lastOnce标记的XSQL节点执行之前执行此等待操作。
     * 
     * @param cloudWait 
     */
    public void setCloudWait(XSQLNode cloudWait)
    {
        this.cloudWait = cloudWait;
    }
    
    
    
    /**
     * 获取：是否打印执行轨迹日志。默认为：false
     */
    public boolean isLog()
    {
        return isLog;
    }


    
    /**
     * 设置：是否打印执行轨迹日志。默认为：false
     * 
     * @param isLog 
     */
    public XSQLGroup setLog(boolean isLog)
    {
        this.isLog = isLog;
        return this;
    }
    
    
    
    /**
     * 输出执行日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-08-02
     * @version     v1.0
     *
     */
    public void openLog()
    {
        this.setLog(true);
    }
    
    
    
    /**
     * 不输出执行日志
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-08-02
     * @version     v1.0
     *
     */
    public void closeLog()
    {
        this.setLog(false);
    }
    
    
    
    /**
     * 获取：是否自动提交。默认为： true
     * 
     * 当为false时，须外界手工执行提交或回滚，及关闭数据库连接
     * 
     * 注：此属性对以下情况无效，以下情况相当于手工提交。
     *           1. beforeCommit   操作节点前提交；
     *           2. afterCommit    操作节点后提交；
     *           3. perAfterCommit 每获取结果集一条记录前提交。
     */
    public boolean isAutoCommit()
    {
        return isAutoCommit;
    }
    
    
    
    /**
     * 设置：否自动提交。默认为： true
     * 
     * 当为false时，须外界手工执行提交或回滚，及关闭数据库连接
     * 
     * 注：此属性对以下情况无效，以下情况相当于手工提交。
     *           1. beforeCommit   操作节点前提交；
     *           2. afterCommit    操作节点后提交；
     *           3. perAfterCommit 每获取结果集一条记录前提交。
     * 
     * @param isAutoCommit 
     */
    public void setAutoCommit(boolean isAutoCommit)
    {
        this.isAutoCommit = isAutoCommit;
    }
    
    
    
    /**
     * 获取：注释。可用于日志的输出等帮助性的信息
     */
    public String getComment()
    {
        return comment;
    }

    
    
    /**
     * 设置：注释。可用于日志的输出等帮助性的信息
     * 
     * @param comment 
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }



    /**
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-03-08
     * @version     v1.0
     *
     * @return
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return Help.NVL(this.comment);
    }





    /**
     * 防止重复提交
     *
     * @author      ZhengWei(HY)
     * @createDate  2016-01-22
     * @version     v1.0
     */
    class XConnection
    {
        private Connection conn;
        
        /** 是否提交过 */
        private boolean    isCommit;
        
        
        
        public XConnection(Connection i_Conn)
        {
            this.conn     = i_Conn;
            this.isCommit = false;
        }
        
        
        public Connection getConn()
        {
            return conn;
        }

        
        public boolean isCommit()
        {
            return isCommit;
        }

        
        public void setCommit(boolean isCommit)
        {
            this.isCommit = isCommit;
        }
        

        /*
        ZhengWei(HY) Del 2016-07-30
        不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
        它会在元素还有用，但集合对象本身没有用时，释放元素对象
        
        一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
        protected void finalize() throws Throwable
        {
            this.conn = null;
            super.finalize();
        }
        */
        
    }
    
    
    
    
    
    /**
     * 多线程并发执行SQLNode节点及其后的关联节点
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-02-21
     * @version     v1.0
     */
    class XSQLGroupTask extends Task<Object>
    {
        private XSQLGroup                         xsqlGroup;
        
        private int                               superNodeIndex;
        
        private Map<String ,Object>               params;
        
        private XSQLGroupResult                   xsqlGroupResult;
        
        private Map<DataSourceGroup ,XConnection> dsgConns;
        
        
        
        private synchronized int GetSerialNo()
        {
            return ++$SerialNo;
        }
        
        
        public XSQLGroupTask(XSQLGroup i_XSQLGroup ,int i_SuperNodeIndex ,Map<String ,Object> i_Params ,XSQLGroupResult i_XSQLGroupResult ,Map<DataSourceGroup ,XConnection> io_DSGConns) 
        {
            super("XSQLGroupTask");
            
            this.xsqlGroup       = i_XSQLGroup;
            this.superNodeIndex  = i_SuperNodeIndex;
            this.params          = new HashMap<String ,Object>(i_Params);  // 必须复制克隆所有数据
            this.xsqlGroupResult = new XSQLGroupResult(i_XSQLGroupResult); // 必须复制克隆所有数据
            this.dsgConns        = io_DSGConns;
        }
        
        
        @Override
        public void execute()
        {
            this.xsqlGroupResult = this.xsqlGroup.executeGroup(this.superNodeIndex 
                                                              ,this.params 
                                                              ,this.xsqlGroupResult 
                                                              ,this.dsgConns);
            if ( !this.xsqlGroupResult.isSuccess() )
            {
                // 多任务并发执行时，只要有一个任务异常，其它还在队列中等待执行的任务将就全部退出等待，将不再执行。
                this.getTaskGroup().stopTasksNoExecute();
            }
            else
            {
                XSQLNode v_Node = this.xsqlGroup.xsqlNodes.get(this.superNodeIndex);
                
                if ( v_Node.isPerAfterCommit() )
                {
                    this.xsqlGroup.commits(this.dsgConns ,this.xsqlGroupResult.getExecSumCount());
                }
            }
            
            this.finishTask();
        }
        

        @Override
        public String getTaskDesc()
        {
            return this.getTaskType();
        }


        @Override
        public int getSerialNo()
        {
            return GetSerialNo();
        }

        
        public XSQLGroupResult getXsqlGroupResult()
        {
            return xsqlGroupResult;
        }
        
    }
    
    
    
    
    
    /**
     * 大数据循环处理类 
     *
     * @author      ZhengWei(HY)
     * @createDate  2018-01-18
     * @version     v1.0
     */
    class XSQLGroupBigData implements XSQLBigData
    {
        private XSQLGroup                    xsqlGroup;
        
        private int                          xsqlNodeIndex;
        
        private XSQLNode                     xsqlNode;
        
        private Map<String ,Object>          xsqlParams;
        
        private XSQLGroupResult              xsqlRet;
        
        Map<DataSourceGroup ,XConnection>    xsqlDSGConns;
        
        private PartitionMap<String ,Object> queryReturnPart;
        
        private Map<String ,Object>          rowPrevious;
        
        
        
        public XSQLGroupBigData(XSQLGroup                         i_XSQLGroup
                               ,int                               i_XSQLNodeIndex
                               ,XSQLNode                          i_XSQLNode
                               ,Map<String ,Object>               i_XSQLParams
                               ,XSQLGroupResult                   i_XSQLRet
                               ,Map<DataSourceGroup ,XConnection> i_XSQLDSGConns)
        {
            this.xsqlGroup       = i_XSQLGroup;
            this.xsqlNodeIndex   = i_XSQLNodeIndex;
            this.xsqlNode        = i_XSQLNode;
            this.xsqlParams      = i_XSQLParams;
            this.xsqlRet         = i_XSQLRet;
            this.xsqlDSGConns    = i_XSQLDSGConns;
        }
        
        
        
        /**
         * 大数据开始处理前的操作。只执行一次
         * 
         * @author      ZhengWei(HY)
         * @createDate  2018-01-18
         * @version     v1.0
         *
         */
        @SuppressWarnings("unchecked")
        public void before()
        {
            PartitionMap<String ,Object> v_QueryReturnPart = (PartitionMap<String ,Object>)xsqlRet.getReturns().get(xsqlNode.getQueryReturnID());
            if ( null == v_QueryReturnPart )
            {
                queryReturnPart = new TablePartition<String ,Object>();
                // put返回查询结果集
                xsqlRet.getReturns().put(xsqlNode.getQueryReturnID() ,queryReturnPart);
            }
            
            if ( xsqlNode.isThread() )
            {
                xsqlGroup.newTaskGroupByThreads(xsqlNode ,xsqlRet ,xsqlParams);
            }
        }
        
        
        
        /**
         * 大数据循环遍历完成时触发。只执行一次
         * 
         * @author      ZhengWei(HY)
         * @createDate  2018-01-18
         * @version     v1.0
         *
         * @param i_IsSucceed  循环遍历是否成功。未成功即出现异常。
         */
        public void finish(boolean i_sSucceed)
        {
            // 如果是多线程并有等待标识时，一直等待并且的执行结果  Add 2018-01-24
            xsqlRet = this.xsqlGroup.waitThreads(xsqlNode ,xsqlRet);
        }
        
        
        
        /**
         * 大数据一行一处理方法 
         * 
         * @author      ZhengWei(HY)
         * @createDate  2018-01-17
         * @version     v1.0
         *
         * @param i_RowNo        行号。下标从0开始
         * @param i_Row          本行数据
         * @param i_RowPrevious  上一行数据
         * @param i_RowNext      下一行数据
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean row(long i_RowNo ,Object i_Row ,Object i_RowPrevious ,Object i_RowNext)
        {
            try
            {
                // 行级对象是：Map<String ,Object>
                if ( i_Row instanceof Map || MethodReflect.isExtendImplement(i_Row ,Map.class) )
                {
                    Map<String ,Object> v_QRItemMap = (Map<String ,Object>)i_Row;
                    
                    xsqlParams.putAll(v_QRItemMap);
                    xsqlParams.put(XSQLGroup.$Param_RowIndex    ,i_RowNo);
                    xsqlParams.put(XSQLGroup.$Param_RowPrevious ,rowPrevious);
                    xsqlParams.put(XSQLGroup.$Param_RowNext     ,i_RowNext);
                    
                    if ( xsqlNode.isThread() )
                    {
                        // 多线程并发执行  Add 2017-02-22
                        XSQLGroupTask v_Task = new XSQLGroupTask(xsqlGroup ,xsqlNodeIndex ,xsqlParams ,xsqlRet ,xsqlDSGConns);
                        xsqlRet.taskGroup.addTaskAndStart(v_Task);
                    }
                    else
                    {
                        xsqlRet = xsqlGroup.executeGroup(xsqlNodeIndex ,xsqlParams ,xsqlRet ,xsqlDSGConns);
                        if ( !xsqlRet.isSuccess() )
                        {
                            // 循环执行时，只要有一个执行异常，就全部退出
                            return false;
                        }
                        else if ( xsqlNode.isPerAfterCommit() )
                        {
                            xsqlGroup.commits(xsqlDSGConns ,xsqlRet.getExecSumCount());
                        }
                    }
                    
                    if ( !Help.isNull(xsqlNode.getQueryReturnID()) )
                    {
                        queryReturnPart.putRows(v_QRItemMap);  // 只有执行成功后才put返回查询结果集
                    }
                    rowPrevious = Help.setMapValues(v_QRItemMap ,xsqlParams);
                }
                // 行级对象是：Java Bean。需要转成Map<String ,Object>
                else
                {
                
                    Map<String ,Object> v_QRItemMap = Help.toMap(i_Row ,null ,false);
                    
                    xsqlParams.putAll(v_QRItemMap);
                    xsqlParams.put(XSQLGroup.$Param_RowIndex    ,i_RowNo);
                    xsqlParams.put(XSQLGroup.$Param_RowPrevious ,rowPrevious);
                    xsqlParams.put(XSQLGroup.$Param_RowNext     ,i_RowNext);
                    
                    if ( xsqlNode.isThread() )
                    {
                        // 多线程并发执行  Add 2017-02-22
                        XSQLGroupTask v_Task = new XSQLGroupTask(xsqlGroup ,xsqlNodeIndex ,xsqlParams ,xsqlRet ,xsqlDSGConns);
                        xsqlRet.taskGroup.addTaskAndStart(v_Task);
                    }
                    else
                    {
                        xsqlRet = xsqlGroup.executeGroup(xsqlNodeIndex ,xsqlParams ,xsqlRet ,xsqlDSGConns);
                        if ( !xsqlRet.isSuccess() )
                        {
                            // 循环执行时，只要有一个执行异常，就全部退出
                            return false;
                        }
                        else if ( xsqlNode.isPerAfterCommit() )
                        {
                            xsqlGroup.commits(xsqlDSGConns ,xsqlRet.getExecSumCount());
                        }
                    }
                    
                    if ( !Help.isNull(xsqlNode.getQueryReturnID()) )
                    {
                        queryReturnPart.putRows(v_QRItemMap);  // 只有执行成功后才put返回查询结果集
                    }
                    rowPrevious = Help.setMapValues(v_QRItemMap ,xsqlParams);
                }
            
                return true;
            }
            catch (Exception exce)
            {
                xsqlRet.setExceptionNode(xsqlNodeIndex);
                xsqlRet.setExceptionSQL (xsqlGroup.getSQL(xsqlNode ,xsqlParams));
                xsqlRet.setException(    exce);
                xsqlRet.setSuccess(false);
                return false;
            }
        }
    }
    
}
