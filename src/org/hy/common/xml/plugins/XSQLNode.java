package org.hy.common.xml.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.StringHelp;
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
     * 节点类型： 执行（包含DML、DDL）
     * 
     *  即执行数据库操作，不获取操作结果。执行参数由外界或其它查询类型的节点(XSQLNode)提供
     */
    public static final String  $Type_Execute                    = "Execute";
    
    /**
     * 节点类型：执行Java代码
     *  
     *   即执行Java代码，主要用于对查询结果集的二次加工处理等操作。
     *   相关针对性属性有：
     *     1. xjavaID
     *     2. methodName
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
    
    
    
    /** 节点类型。默认为：执行类型（包含DML、DDL） */
    private String              type;
    
    /** 操作SQL对象 */
    private XSQL                sql;
    
    /** 
     * 执行前的前提条件，只有当满足这个条件后，XSQL才会被执行
     * 
     * 形式为带占位符的Fel条件，如：:c01=='1' && :c02=='2'
     * 
     * 为空时，表示任何情况下都允许执行
     */
    private String              condition;
    
    /** 
     * 解释出来的Fel条件。与this.condition的区别是：它是没有占位符
     * 
     * 如：c01=='1' && c02=='2'
     */
    private String              conditionFel;
    
    /**
     * 占位符信息的集合
     * 
     * Map.key    为占位符。前缀为:符号
     * Map.Value  为占位符原文本信息
     */
    private Map<String ,Object> placeholders;
    
    /**
     * 当检查不通过时，是否允许其后的XSQL节点执行。默认为：true
     * 
     * 当为 true 时，检查只影响自己的节点，不影响其它及其后XSQL节点的执行。
     */
    private boolean             noPassContinue;
    
    /** 执行本节点前，对之前的所有XSQL节点进行统一提交操作。默认为：false */
    private boolean             beforeCommit;
    
    /**
     * 专用于执行类型的XSQL节点。
     * 执行本节点后，对之前(及本节点)的所有XSQL节点进行统一提交操作。默认为：false 
     */
    private boolean             afterCommit;
    
    /** 
     * 专用于查询类型的XSQL节点。
     * 除获取查询结果集的首条记录外，每获取结果集一条记录前，对上一条记录产生的一系列数据库操作，做一次统一提交操作。 
     * 默认为：false
     */
    private boolean             perAfterCommit;
    
    /** 
     * 在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
     * 即，标记为lastOnce=true的节点，不在查询类型XSQL节点的循环之中执行。
     * 默认为：false
     */
    private boolean             lastOnce;
    
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
    private String              returnID;
    
    /**
     * 针对 this.returnID 属性，定义返回查询结果集是 "追加方式"？还是 "覆盖方式"。
     *   1. 在追加方式下，相同标示returnID对应查询结果集的Java类型应当是一致的。
     *   2. 目前只支持对查询结果集是 Map、List、Set 三种接口实现类的追加方式。
     *   3. 其它查询结果集还是采用覆盖方式。
     * 默认为：false（覆盖方式）
     */
    private boolean             returnAppend;
    
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
     */
    private String              queryReturnID;
    
    /** 
     * 实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
     * 
     * 如果将 XSQLGroup组，比喻为Java方法，那嵌套另一个XSQLGroup组，就相当于Java方法中调用另一个方法。
     * 
     * 当 sqlGroup 生效(不为空)时，只有 condition、noPassContinue、lastOnce 三个属性依然有作用。
     * 其它属性全部都将失效，包括 this.sql 也同时失效。
     */
    private XSQLGroup           sqlGroup;
    
    /** 注解说明。当开启日志模式(XSQLGroup.isLog)时，此注解说明也会被同时输出。 */
    private String              comment;
    
    /** 
     * XJava对象标识
     *    构建XSQLNode对象实例时，xjavaID标记的对象实例，可以是不存在的（或尚未构建的）。
     *    只要在执行时存在就OK了 
     */
    private String              xjavaID;
    
    /** 
     * XJava对象执行的方法名
     *      方法的定义形式为：public boolean xxx(Map<String ,Object> io_Params ,Map<String ,Object> io_Returns) { }
     *        1. io_Params    执行或查询参数。同XSQLGroup.executeGroup()方法的入参参数io_Params同义。
     *        2. io_Returns   通过returnID标记的，返回出去的多个查询结果集。同XSQLGroupResult.returns属性同义。
     *        3. 返回值，表示是否执行成功
     */
    private String              methodName;
    
    /** 解释好的XJava对象实例。为了性能而存在，在反复循环中，只解释一次就好 */
    private Object             xjavaIntance;
    
    /** 解释好的XJava对象方法。为了性能而存在，在反复循环中，只解释一次就好 */
    private Method             xjavaMethod;
    
    /**
     * 集合ID
     * 
     * 只对 $Type_CollectionToQuery、$Type_CollectionToDB 两个类型有效。标识要转换角色的集合ID。
     * 当为空时，表示将整个 XSQLGroup.execute() 方法的入参当为查询结果来用（前提是：入参类型必须为：List、Set、Map(会通过Hetp.toList()转为List)）。
     * 
     * 支持xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释
     */
    private String             collectionID;
    
    /**
     * 是否为多线程并且发执行。默认值：false。
     * 
     * 只对 $Type_Query、$Type_CollectionToQuery 两个类型有效。
     */
    private boolean            thread;
    
    
    
    public XSQLNode()
    {
        this.noPassContinue = true;
        this.beforeCommit   = false;
        this.afterCommit    = false;
        this.perAfterCommit = false;
        this.lastOnce       = false;
        this.returnID       = null;
        this.returnAppend   = false;
        this.queryReturnID  = null;
        this.sqlGroup       = null;
        this.xjavaID        = null;
        this.methodName     = null;
        this.xjavaIntance   = null;
        this.xjavaMethod    = null;
        this.collectionID   = null;
        this.thread         = false;
    }
    
    
    
    /**
     * 获取：节点类型。默认为：执行类型（包含DML、DDL）
     */
    public String getType()
    {
        return Help.NVL(this.type ,$Type_Execute);
    }

    
    
    /**
     * 设置：节点类型。默认为：执行类型（包含DML、DDL）
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
                this.conditionFel = StringHelp.replaceAll(this.conditionFel ,":" + v_Key ,v_Key);
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
    public boolean getReturnAppend()
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
     *      方法的定义形式为：public boolean xxx(Map<String ,Object> io_Params ,Map<String ,Object> io_Returns) { }
     *        1. io_Params    执行或查询参数。同XSQLGroup.executeGroup()方法的入参参数io_Params同义。
     *        2. io_Returns   通过returnID标记的，返回出去的多个查询结果集。同XSQLGroupResult.returns属性同义。
     *        3. 返回值，表示是否执行成功
     */
    public String getMethodName()
    {
        return methodName;
    }


    
    /**
     * 设置：XJava对象执行的方法名
     *      方法的定义形式为：public boolean xxx(Map<String ,Object> io_Params ,Map<String ,Object> io_Returns) { }
     *        1. io_Params    执行或查询参数。同XSQLGroup.executeGroup()方法的入参参数io_Params同义。
     *        2. io_Returns   通过returnID标记的，返回出去的多个查询结果集。同XSQLGroupResult.returns属性同义。
     *        3. 返回值，表示是否执行成功
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
     * @author      ZhengWei(HY)
     * @createDate  2016-05-17
     * @version     v1.0
     *
     * @param io_Params    执行或查询参数。同XSQLGroup.executeGroup()方法的入参参数io_Params同义。
     * @param io_Returns   通过returnID标记的，返回出去的多个查询结果集。同XSQLGroupResult.returns属性同义。
     * @return             表示是否执行成功
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public boolean executeJava(Map<String ,Object> io_Params ,Map<String ,Object> io_Returns) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
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
        
        Object v_Ret = this.xjavaMethod.invoke(this.xjavaIntance ,io_Params ,io_Returns);
        return (Boolean)v_Ret;
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
        
        Method [] v_Methods = this.xjavaIntance.getClass().getDeclaredMethods();
        
        for (int i=0; i<v_Methods.length; i++)
        {
            if ( v_Methods[i].getName().equals(this.methodName) )
            {
                if ( v_Methods[i].getParameterTypes().length == 2 )
                {
                    if ( Map.class.equals(v_Methods[i].getParameterTypes()[0]) 
                      && Map.class.equals(v_Methods[i].getParameterTypes()[1]) )
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
        
        FelEngine  v_Fel        = new FelEngineImpl();
        FelContext v_FelContext = v_Fel.getContext();
        
        for (String v_Key : this.placeholders.keySet())
        {
            Object v_Value = Help.getValueIgnoreCase(i_ConditionValues ,v_Key);
            
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
     * @param i_Thread
     */
    public void setThread(boolean i_Thread)
    {
        this.thread = i_Thread;
    }
    
}
