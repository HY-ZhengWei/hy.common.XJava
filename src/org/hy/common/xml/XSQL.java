package org.hy.common.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hy.common.app.Param;
import org.hy.common.db.DBSQL;
import org.hy.common.db.DataSourceGroup;
import org.hy.common.xml.event.BLobListener;
import org.hy.common.xml.event.DefaultBLobEvent;

import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.hy.common.Busway;
import org.hy.common.ByteHelp;
import org.hy.common.CycleNextList;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.Return;
import org.hy.common.StaticReflect;
import org.hy.common.StringHelp;
import org.hy.common.XJavaID;
import org.hy.common.xml.event.BLobEvent;





/**
 * 《XSQL开发说明》见：https://github.com/HY-ZhengWei/XJava/blob/master/doc/XSQL%E5%BC%80%E5%8F%91%E8%AF%B4%E6%98%8E.docx
 * 
 * 解释Xml文件，执行占位符SQL，再分析数据库结果集转化为Java实例对象。
 * 
 * 1. 必须是数据库连接池的。但微型应用(如手机App)，可用无连接池概念的 org.hy.common.DataSourceNoPool 代替。
 * 
 * 2. 有占位符SQL分析功能。
 *    A. 可按对象填充占位符SQL; 同时支持动态SQL，动态标识 <[ ... ]>
 *    B. 可按集合填充占位符SQL。同时支持动态SQL，动态标识 <[ ... ]>
 *    C. SQL语句生成时，对于占位符，可实现xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释。如，':shool.BeginTime'
 * 
 * 3. 对结果集输出字段转为Java实例，有过滤功能。
 *    A. 可按字段名称过滤;
 *    B. 可按字段位置过滤。
 *    
 * 4. 针对超大结果集做特殊的转换优化，类似于PL/SQL的分页功能。同样也有过滤功能。
 *    A. 可按字段名称过滤;
 *    B. 可按字段位置过滤。
 *    
 * 5. 有数据库连接池组功能
 * 
 * 6. Oracle数据库中BLob大字段的操作(写入、读取)
 *    并有简单加密(转码)功能。
 *    
 * 7. 支持存储过程及数据库函数的调用
 * 
 * 8. 支持批量执行DML操作
 * 
 * 9. 支持XSQL触发器
 * 
 * @author      ZhengWei(HY)
 * @createDate  2012-11-12
 * @version     v1.0  
 *              v2.0  2016-02-19  添加：游标的分页查询功能（可通用于所有数据库）。
 *              v3.0  2016-07-05  添加：SQL执行日志。默认只保留1000条执行过的SQL语句。
 *              v4.0  2016-08-16  添加：最后执行时间点的记录。
 *              v5.0  2017-01-04  添加：SQL执行异常的日志。默认只保留100条执行异常的SQL语句。
 *              v6.0  2017-01-06  添加：XSQL触发器功能，详见org.hy.common.xml.XSQLTrigger。
 *                                     但，对于原生态的SQL文本及BLob类型的没有建立触发器功能。
 *                                     不建立的原因为：
 *                                       1. 原生态的SQL文本是动态不确定的SQL，可比喻为临时写的SQL语言，是不用建立触发器的。
 *                                       2. BLob类型的不太常用，业务逻辑也较为单一，暂时就不建立触发器了。
 *              v6.1  2017-07-06  修正：当预处理 this.executeUpdatesPrepared_Inner() 执行异常时，未记录异常SQL的问题。
 *              v7.0  2017-09-18  添加：数据库连接的域。实现相同数据库结构下的，多个数据库间的分域功能。
 *                                     多个数据库间的相同SQL语句，不用重复写多次，只须通过"分域"动态改变数据库连接池组即可。
 *              v7.1  2017-11-06  修正：当预处理 this.executeUpdatesPrepared_Inner() 执行的同时 batchCommit >= 1时，可能出现未"执行executeBatch"情况。
 *                                       发现人：向以前同学
 *              v8.0  2017-12-19  添加：将参数效验检查抛出的异常，也包括在try{}cacth{}内，自己抛出自己捕获，并记录在统计数据中。
 *                                       记录完成后再向外抛出。
 *                                       方便异常定位页面统计数据：http://IP:Port/服务名/analyses/analyseDB
 *              v9.0  2018-01-12  添加：setCreate()实现服务启动时检查并创建数据库对象(如数据库表)，已存在不创建。
 *                                添加：execute()方法支持多条SQL语句的执行。
 *              v10.0 2018-01-17  添加：queryBigData()系列关于大数据操作的方法
 *                                添加：setComment()注释。可用于日志的输出等帮助性的信息
 *              v11.0 2018-01-21  添加：executeUpdates(Map<XSQL ,?> ...)系列方法支持不同类型的多个不同数据库的操作。之前，只支持同一数据库的操作。
 *                                添加：实现多个平行、平等的数据库的负载均衡（简单级的）。
 *                                       目前建议只用在查询SQL上，当多个相同数据的数据库（如主备数据库），
 *                                       在高并发的情况下，提高整体查询速度，查询锁、查询阻塞等问题均能得到一定的解决。
 *                                       在高并发的情况下，突破数据库可分配的连接数量，会话数量将翻数倍（与数据库个数有正相关的关系）。
 *              v11.1 2018-03-05  添加：重置统计数据的功能。
 *              v11.2 2018-05-08  添加：支持枚举toString()的匹配
 *              v11.3 2018-05-11  修正：预解析处理时间时，getSQLDate()的精度只到天，未到时分秒，所以换成getSQLTimestamp()方法。
 *              v11.4 2018-05-15  添加：数据库java.sql.Timestamp时间的转换
 *              v12.0 2018-06-24  添加：allowExecutesSplit属性，是否允许或支持execute()方法中执行多条SQL语句，
 *                                       即$Executes_Split = ";/"分割符是否生效。
 *              v13.0 2018-07-01  添加：实现Oracle数据库Clob的写入方法executeUpdateCLob()。
 *                                       对于Clob的读取已在hy.common.db包中实现，对于开发者来说，查询SQL无须任何特殊处理，就当Clob是普通字段。
 *              v13.1 2018-07-18  添加：实现普通Insert、Update语句就能写入Clob字段的能力。
 *                                       将两次对数据库的操作，封装在一个普通SQL中，再通过程序自动化拆分两个具体的数据库操作。
 *                                       大大简化开发的工作量。
 *              v13.2 2018-07-25  添加：支持多个长文本信息CLob的写入。
 */
/*
 * 游标类型的说明
 * TYPE_FORWARD_ONLY         默认的cursor类型，仅仅支持向前forward，不支持backforward，random，last，first操作，类似单向链表。
 *                           类型通常是效率最高最快的cursor类型，也是最常用的选择。
 * TYPE_SCROLL_INSENSITIVE   支持backforward，random，last，first操作，对其它数据session对选择数据做出的更改是不敏感，不可见的。
 *                           需要在jvm中cache所有fetch到的记录实体，在大量记录集返回时慎用。
 * TYPE_SCROLL_SENSITIVE     支持backforward，random，last，first操作，对其它数据session对选择数据做出的更改是敏感，可见的。但是这种可见性仅限于update操作
 *                           在jvm中cache所有fetch到的记录rowid，需要进行二次查询，效率最低，开销最大
 */
public final class XSQL implements Comparable<XSQL> ,XJavaID
{
    /** SQL类型。N: 增、删、改、查的普通SQL语句  (默认值) */
    public  static final String            $Type_NormalSQL = "N";
    
    /** SQL类型。P: 存储过程 */
    public  static final String            $Type_Procedure = "P";
    
    /** SQL类型。F: 函数 */
    public  static final String            $Type_Function  = "F";
    
    /** SQL类型。C：DDL、DCL、TCL创建表，创建对象等 */
    public  static final String            $Type_Create    = "C";
    
    /** execute()方法中执行多条SQL语句的分割符 */
    public  static final String            $Executes_Split = ";/";
    
    /** 大数据字段类型(如,CLob)的占位符名称，多个占位符名称间用逗号，分隔 */
    public  static final String            $LobName_Split  = ",";
    
    /** SQL执行日志。默认只保留1000条执行过的SQL语句 */
    public  static final Busway<XSQLLog>   $SQLBusway      = new Busway<XSQLLog>(1000);
    
    /** SQL执行异常的日志。默认只保留1000条执行异常的SQL语句 */
    public  static final Busway<XSQLLog>   $SQLBuswayError = new Busway<XSQLLog>(1000);
    
    
    
    static
    {
        XJava.putObject("$SQLBusway"      ,$SQLBusway);
        XJava.putObject("$SQLBuswayError" ,$SQLBuswayError);
    }
    
    
    
    /** 
     * 通用分区XSQL标示记录（确保只操作一次，而不是重复执行替换操作）
     * Map.key   为数据库类型 + "_" + XSQL.getObjectID()
     * Map.value 为 XSQL
     */
    private static final Map<String ,XSQL> $PagingMap      = new HashMap<String ,XSQL>();

	/** 缓存大小 */
	private static final int               $BufferSize     = 4 * 1024;
	
	
	
	/** XJava池中对象的ID标识 */
    private String                         xjavaID;
	
	/** 多个平行、平等的数据库的负载数据库集合 */
	private CycleNextList<DataSourceGroup> dataSourceGroups;
	
	/** 
	 * 数据库连接的域。
	 * 
	 * 它可与 this.dataSourceGroup 同时存在值，但 this.domain 的优先级高。
	 * 当"域"存在时，使用域的数据库连接池组。其它情况，使用默认的数据库连接池组。
	 */
	private XSQLDomain                     domain;
	
	/** 数据库占位符SQL的信息 */
	private DBSQL                          content;
	
	/** 解释Xml文件，分析数据库结果集转化为Java实例对象 */
	private XSQLResult                     result;
	
	/** XSQL的触发器 */
	private XSQLTrigger                    trigger;
	
	/** 数据安全性。如果为真，将对上传的文件进行数据加密 */
	private boolean                        blobSafe;
	
	/** 自定义事件的监听器集合--文件拷贝 */
	private Collection<BLobListener>       blobListeners;
    
    /** 
     * SQL类型。
     * 
     * N: 增、删、改、查的普通SQL语句  (默认值)
     * P: 存储过程
     * F: 函数
     * C: DML创建表，创建对象等
     */
    private String                         type;
    
    /** 
     * 创建对象的名称。如表名称。
     * 
     * 此属性为动作方法，即this.setCreate(...)时，将尝试创建对象(当对象不存在时)。
     * 也因为是动作方法，所以在设置本属性前dataSourceGroup、content它两属性应当已设置OK。
     * 
     * 实现服务启动时检查并创建数据库对象(如数据库表)，已存在不创建。
     */
    private String                         create;
    
    /** 大数据字段类型(如,CLob)的字段名称，多个字段名称间用逗号，分隔。只用于Insert、Update语句 */
    private String                         lobName;
    
    /** 
     * 写入大数据字段类型(如,CLob)的所在行的查询条件SQL片段(不包含WHERE关键字)。只用于Insert、Update语句。
     */
    private String                         lobWheres;
    
    /** 内部属性。标记创建出来的用于写入大数据库类型的XSQL的XJava标记 */
    private String                         lobXSQLID;
    
    /** 当调用存储过程或函数时的参数对象 */
    private List<XSQLCallParam>            callParams;
    
    /** 当调用存储过程或函数时的输入参数的个数 */
    private int                            callParamInCount;
    
    /** 当调用存储过程或函数时的输出参数的个数 */
    private int                            callParamOutCount;
    
    /** 
     * 批量执行 Insert、Update、Delete 时，达到提交的提交点
     * 
     * 当>=1时，才有效，即分次提交
     * 当<=0时，在一个事务中执行所有的操作(默认状态)
     */
    private int                            batchCommit;
    
    /**
     * 是否允许或支持execute()方法中执行多条SQL语句，即$Executes_Split = ";/"分割符是否生效。
     * 默认情况下，通过XSQL模板自动判定$Executes_Split分割符是否生效的。
     * 
     * 但特殊情况下，允许外界通过本属性启用或关闭execute()方法中执行多条SQL语句的功能。
     * 如，SQL语句中就包含;/文本字符的情况，不是分割符是意思。
     */
    private boolean                        allowExecutesSplit;
    
    /** 唯一标示，主用于对比等操作 */
    private String                         uuid;
    
    /** 请求数据库的次数 */
    private long                           requestCount;
    
    /** 请求成功，并成功返回次数 */
    private long                           successCount;
    
    /** 
     * 请求成功，并成功返回的累计用时时长。
     * 用的是Double，而不是long，因为在批量执行时。为了精度，会出现小数 
     */
    private double                         successTimeLen;
    
    /**
     * 最后执行时间点。
     *   1. 在开始执行时，此时间点会记录一次。
     *   2. 在执行结束后，此时间点会记录一次。
     *   3. 当出现异常时，此时间点保持最近一次，不变。
     *   4. 当多个线程同时操作时，记录最新的时间点。
     *   5. 未执行时，此属性为NULL
     */
    private Date                           executeTime;
    
    /** 注释。可用于日志的输出等帮助性的信息 */
    private String                         comment; 
	
	
	
	public XSQL()
	{
		this.dataSourceGroups   = new CycleNextList<DataSourceGroup>(1);
		this.domain             = null;
		this.content            = new DBSQL();
		this.result             = new XSQLResult();
		this.trigger            = null;
		this.blobSafe           = false;
        this.type               = $Type_NormalSQL;
        this.create             = null;
        this.lobName            = null;
        this.lobWheres          = null;
        this.lobXSQLID          = "";
        this.callParamInCount   = 0;
        this.callParamOutCount  = 0;
        this.batchCommit        = 0;
        this.allowExecutesSplit = false;
        this.uuid               = StringHelp.getUUID();
        this.requestCount       = 0;
        this.successCount       = 0;
        this.successTimeLen     = 0D;
        this.executeTime        = null;
        this.comment            = null;
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
	    
	    if ( this.isTriggers() )
	    {
	        for (XSQL v_Trigger : this.trigger.getXsqls())
	        {
	            v_Trigger.reset();
	        }
	    }
	}
	
	
	
	private synchronized Date request()
	{
	    ++this.requestCount;
	    this.executeTime = new Date();
	    return this.executeTime;
	}
	
	
	
	private synchronized void success(Date i_ExecuteTime ,double i_TimeLen)
    {
        ++this.successCount;
        this.successTimeLen += i_TimeLen;
        this.executeTime     = i_ExecuteTime;
    }
	
	
	
	private synchronized void success(Date i_ExecuteTime ,double i_TimeLen ,int i_SCount)
    {
        this.requestCount   += i_SCount - 1;
        this.successCount   += i_SCount;
        this.successTimeLen += i_TimeLen;
        this.executeTime     = i_ExecuteTime;
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
     * 执行SQL异常时的统一处理方法
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-04
     * @version     v1.0
     *
     * @param i_SQL
     * @param i_Exce
     */
    private static void erroring(String i_SQL ,Exception i_Exce ,XSQL i_XSQL)
    {
        $SQLBusway     .put(new XSQLLog(i_SQL ,i_Exce));
        $SQLBuswayError.put(new XSQLLog(i_SQL ,i_Exce ,i_XSQL == null ? "" : i_XSQL.getObjectID()));
        
        String v_XJavaID = "";
        
        if ( i_XSQL != null && i_XSQL.getDataSourceGroup() != null )
        {
            i_XSQL.getDataSourceGroup().setException(true);
            v_XJavaID = Help.NVL(i_XSQL.getXJavaID());
        }
        
        System.err.println("-- Error time：  " + Date.getNowTime().getFull() 
                       + "\n-- Error XSQL ID：  " + v_XJavaID
                       + "\n-- Error SQL：  " + i_SQL);
    }
    
    
    
    /**
     * 检查数据库占位符SQL的对象是否为null。同时统计异常数据。
     * 
     * 此方法从各个数据库操作方法中提炼而来。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-19
     * @version     v1.0
     *
     */
    private void checkContent()
    {
        if ( this.content == null )
        {
            NullPointerException v_Exce = new NullPointerException("Content is null of XSQL.");
            
            this.request();
            erroring("" ,v_Exce ,this);
            throw v_Exce;
        }
    }
    
    
    
    /**
     * 是否执行触发器
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-06
     * @version     v1.0
     *
     * @param i_IsError  主XSQL在执行时是否异常？
     * @return
     */
    private boolean isTriggers(boolean i_IsError)
    {
        if ( null != this.trigger && !Help.isNull(this.trigger.getXsqls()) )
        {
            if ( !i_IsError || this.trigger.isErrorMode() )
            {
                this.initTriggers();
                return true;
            }
        }
        
        return false;
    }
    
    
    
    /**
     * 是否有触发器
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-06
     * @version     v1.0
     *
     * @return
     */
    public boolean isTriggers()
    {
        return null != this.trigger && !Help.isNull(this.trigger.getXsqls());
    }
    
    
    
    /**
     * 对只有数据库连接组的触发器XSQL对象赋值
     * 
     *   这种情况一般是由：setCreateBackup(...) 方法创建的触发器
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-06
     * @version     v1.0
     *
     */
    private synchronized void initTriggers()
    {
        for (XSQL v_XSQL : this.trigger.getXsqls())
        {
            if ( Help.isNull(v_XSQL.getContentDB().getSqlText()) )
            {
                v_XSQL.setContentDB(this.getContentDB());
                v_XSQL.setResult(   this.getResult());
                v_XSQL.setType(     this.getType());
            }
        }
    }
    
    
    
    /**
     * 通用(限定常用的数据库)分页查询。-- i_XSQL中的普通SQL，将通过模板变成一个分页SQL
     * 
     * 本方法并不真的执行查询，而是获取一个分页查询的XSQL对象。
     * 
     * 与游标分页查询相比，其性能是很高的。
     * 
     * SQL语句中的占位符 :StartIndex 下标从0开始
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-22
     * @version     v1.0
     *
     * @param i_XSQL
     * @return
     */
    public static XSQL queryPaging(String i_XSQLID)
    {
        return XSQL.queryPaging((XSQL)XJava.getObject(i_XSQLID) ,false);
    }
    
    
    
    /**
     * 通用(限定常用的数据库)分页查询。-- i_XSQL中的普通SQL，将通过模板变成一个分页SQL
     * 
     * 本方法并不真的执行查询，而是获取一个分页查询的XSQL对象。
     * 
     * 与游标分页查询相比，其性能是很高的。
     * 
     * SQL语句中的占位符 :StartIndex 下标从0开始
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-22
     * @version     v1.0
     *
     * @param i_XSQL
     * @return
     */
    public static XSQL queryPaging(XSQL i_XSQL)
    {
        return XSQL.queryPaging(i_XSQL ,false);
    }
    
    
    
    /**
     * 通用(限定常用的数据库)分页查询。
     * 
     * 本方法并不真的执行查询，而是获取一个分页查询的XSQL对象。
     * 
     * 与游标分页查询相比，其性能是很高的。
     * 
     * SQL语句中的占位符 :StartIndex 下标从0开始
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-22
     * @version     v1.0
     *
     * @param i_XSQL
     * @param i_IsClone  标示参数对象i_XSQL，是否会被改变。
     *                   1. 当为true时，用通用模板、具体i_XSQL生成一个全新的XSQL。
     *                   2. 当为false时，i_XSQL中的普通SQL，将通过模板变成一个分页SQL
     * @return
     */
    public static XSQL queryPaging(String i_XSQLID ,boolean i_IsClone)
    {
        return XSQL.queryPaging((XSQL)XJava.getObject(i_XSQLID) ,i_IsClone);
    }
    
    
    
    /**
     * 通用(限定常用的数据库)分页查询。
     * 
     * 本方法并不真的执行查询，而是获取一个分页查询的XSQL对象。
     * 
     * 与游标分页查询相比，其性能是很高的。
     * 
     * SQL语句中的占位符 :StartIndex 下标从0开始
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-22
     * @version     v1.0
     *
     * @param i_XSQL
     * @param i_IsClone  标示参数对象i_XSQL，是否会被改变。
     *                   1. 当为true时，用通用模板、具体i_XSQL生成一个全新的XSQL。
     *                   2. 当为false时，i_XSQL中的普通SQL，将通过模板变成一个分页SQL
     * @return
     */
    public synchronized static XSQL queryPaging(XSQL i_XSQL ,boolean i_IsClone)
    {
        if ( null == i_XSQL )
        {
            return null;
        }
        
        String v_DBType = i_XSQL.getDataSourceGroup().getDbProductType();
        String v_PMKey  = v_DBType + "_" + i_XSQL.getObjectID();
        
        if ( $PagingMap.containsKey(v_PMKey) )
        {
            return $PagingMap.get(v_PMKey);
        }
        
        String v_PagingTemplate = null;
        if ( !Help.isNull(v_DBType) )
        {
            Param v_Template = XSQLPaging.getPagingTempalte(v_DBType);
            
            if ( null != v_Template )
            {
                v_PagingTemplate = v_Template.getValue();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
        
        String v_PaginSQLText = StringHelp.replaceAll(v_PagingTemplate ,":SQLPaging" ,i_XSQL.getContent().getSqlText());
        
        if ( i_IsClone )
        {
            // 用通用模板、具体i_XSQL生成一个全新的XSQL。
            // 优势：具体i_XSQL可零活使用，因为它本身没有变化，还可以用于非分页的查询。
            // 缺点：新XSQL与具体i_XSQL统计量不统一。
            XSQL v_NewXSQL = new XSQL();
            
            v_NewXSQL.setDataSourceGroup(i_XSQL.getDataSourceGroup());
            v_NewXSQL.setResult(         i_XSQL.getResult());
            v_NewXSQL.getContent().setSqlText(v_PaginSQLText);
            
            // 注意：这里是Key是i_XSQL，而不是v_NewXSQL的uuid
            $PagingMap.put(v_PMKey ,v_NewXSQL);
            XJava.putObject("XPaging_" + v_PMKey ,v_NewXSQL);
            return v_NewXSQL;
        }
        else
        {
            // 用通用模板替换具体i_XSQL中的内容。
            // 优势：统计功能统一。
            // 缺点：具体i_XSQL就变为专用于分页查询的SQL。
            i_XSQL.getContent().setSqlText(v_PaginSQLText);
            
            $PagingMap.put(v_PMKey ,i_XSQL);
            return i_XSQL;
        }
    }



    /**
	 * 占位符SQL的查询。 -- 无填充值的
	 * 
	 * @return
	 */
	public Object query()
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.query(this.content.getSQL());
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes();
		    }
		}
	}
	
	
	
	/**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @param i_Conn
     * @return
     */
    public Object query(Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.query(this.content.getSQL() ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
	
	
	
	/**
	 * 占位符SQL的查询。
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @return
	 */
	public Object query(Map<String ,?> i_Values)
	{
	    checkContent();
		
		boolean v_IsError = false;
		
		try
		{
		    return this.query(this.content.getSQL(i_Values));
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
		finally
		{
		    if ( this.isTriggers(v_IsError) )
	        {
		        this.trigger.executes(i_Values);
	        }
		}
	}
	
	
	
	/**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn  
     * @return
     */
    public Object query(Map<String ,?> i_Values ,Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;
        
        try
        {
            return this.query(this.content.getSQL(i_Values) ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段名称过滤)
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @param i_FilterColNames   按输出字段名称过滤。
	 * @return
	 */
	public Object query(Map<String ,?> i_Values ,List<String> i_FilterColNames)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.query(this.content.getSQL(i_Values) ,i_FilterColNames);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段位置过滤)
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @param i_FilterColNoArr   按输出字段位置过滤。
	 * @return
	 */
	public Object query(Map<String ,?> i_Values ,int [] i_FilterColNoArr)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.query(this.content.getSQL(i_Values) ,i_FilterColNoArr);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @return
	 */
	public Object query(Object i_Obj)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.query(this.content.getSQL(i_Obj));
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_Conn
     * @return
     */
    public Object query(Object i_Obj ,Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.query(this.content.getSQL(i_Obj) ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段名称过滤)
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @param i_FilterColNames   按输出字段名称过滤。
	 * @return
	 */
	public Object query(Object i_Obj ,List<String> i_FilterColNames)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.query(this.content.getSQL(i_Obj) ,i_FilterColNames);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段位置过滤)
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @param i_FilterColNoArr   按输出字段位置过滤。
	 * @return
	 */
	public Object query(Object i_Obj ,int [] i_FilterColNoArr)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.query(this.content.getSQL(i_Obj) ,i_FilterColNoArr);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 常规SQL的查询。（内部不再关闭数据库连接）
	 * 
	 * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_SQL              常规SQL语句
	 * @return
	 */
	public Object query(String i_SQL ,Connection i_Conn)
	{
		Statement  v_Statement = null;
	    ResultSet  v_Resultset = null;
	    long       v_BeginTime = this.request().getTime();
		
		try
		{
		    if ( this.result == null )
	        {
	            throw new NullPointerException("Result is null of XSQL.");
	        }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
	        
	        if ( null == i_Conn)
	        {
	            throw new NullPointerException("Connection is null of XSQL.");
	        }
		    
			v_Statement = i_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_Resultset = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			Object v_Ret = this.result.getDatas(v_Resultset);
			Date v_EndTime = Date.getNowTime();
			this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Ret;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
			throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			this.closeDB(v_Resultset ,v_Statement ,null);
		}
	}
	
	
	
	/**
     * 常规SQL的查询。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_SQL              常规SQL语句
     * @return
     */
    public Object query(String i_SQL)
    {
        DataSourceGroup v_DSG       = null;
        Connection      v_Conn      = null;
        Statement       v_Statement = null;
        ResultSet       v_Resultset = null;
        long            v_BeginTime = this.request().getTime();
        
        try
        {
            if ( this.result == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = this.getConnection(v_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            
            Object v_Ret = this.result.getDatas(v_Resultset);
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
    
    
    
	/**
     * 占位符SQL的查询。游标的分页查询（可通用于所有数据库）。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     * 
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public Object query(int i_StartRow ,int i_PagePerSize)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.query(this.content.getSQL() ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public Object query(Map<String ,?> i_Values ,int i_StartRow ,int i_PagePerSize)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.query(this.content.getSQL(i_Values) ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_StartRow         开始读取的行号。下标从0开始。
     * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
     * @return
     */
    public Object query(Object i_Obj ,int i_StartRow ,int i_PagePerSize)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.query(this.content.getSQL(i_Obj) ,i_StartRow ,i_PagePerSize);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
	
	
	
	/**
     * 常规SQL的查询。游标的分页查询（可通用于所有数据库）。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2016-02-19
	 * @version     v1.0
	 *
     * @param i_SQL              常规SQL语句
	 * @param i_StartRow         开始读取的行号。下标从0开始。
	 * @param i_PagePerSize      每页显示多少条数据。只有大于0时，游标分页功能才生效。
	 * @return
	 */
    public Object query(String i_SQL ,int i_StartRow ,int i_PagePerSize)
    {
        DataSourceGroup v_DSG       = null;
        Connection      v_Conn      = null;
        Statement       v_Statement = null;
        ResultSet       v_Resultset = null;
        long            v_BeginTime = this.request().getTime();
        
        try
        {
            if ( this.result == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = this.getConnection(v_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            
            Object v_Ret = this.result.getDatas(v_Resultset ,i_StartRow ,i_PagePerSize);
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
	
	
	
	/**
	 * 常规SQL的查询。(按输出字段名称过滤)
	 * 
	 * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_SQL              常规SQL语句
	 * @param i_FilterColNames   按输出字段名称过滤。
	 * @return
	 */
	public Object query(String i_SQL ,List<String> i_FilterColNames)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
	    ResultSet       v_Resultset = null;
	    long            v_BeginTime = this.request().getTime();
		
		try
		{
		    if ( this.result == null )
	        {
	            throw new NullPointerException("Result is null of XSQL.");
	        }
	        
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_Resultset = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			Object v_Ret = this.result.getDatas(v_Resultset ,i_FilterColNames);
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Ret;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			this.closeDB(v_Resultset ,v_Statement ,v_Conn);
		}
	}
	
	
	
	/**
	 * 常规SQL的查询。(按输出字段位置过滤)
	 * 
	 * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_SQL              常规SQL语句
	 * @param i_FilterColNoArr   按输出字段位置过滤。
	 * @return
	 */
	public Object query(String i_SQL ,int [] i_FilterColNoArr)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
	    ResultSet       v_Resultset = null;
	    long            v_BeginTime = this.request().getTime();
		
		try
		{
		    if ( this.result == null )
	        {
	            throw new NullPointerException("Result is null of XSQL.");
	        }
	        
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_Resultset = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			Object v_Ret = this.result.getDatas(v_Resultset ,i_FilterColNoArr);
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Ret;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			this.closeDB(v_Resultset ,v_Statement ,v_Conn);
		}
	}
	
	
	
	/**
     * 占位符SQL的查询。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Values       占位符SQL的填充集合。
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(Map<String ,?> i_Values ,XSQLBigData i_XSQLBigData)
    {
        checkContent();
        
        boolean v_IsError = false;
        
        try
        {
            return this.queryBigData(this.content.getSQL(i_Values) ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Values       占位符SQL的填充集合。
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(Map<String ,?> i_Values ,Connection i_Conn ,XSQLBigData i_XSQLBigData)
    {
        checkContent();
        
        boolean v_IsError = false;
        
        try
        {
            return this.queryBigData(this.content.getSQL(i_Values) ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
	
	
	
	/**
     * 占位符SQL的查询。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Obj          占位符SQL的填充对象。
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(Object i_Obj ,XSQLBigData i_XSQLBigData)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.queryBigData(this.content.getSQL(i_Obj) ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Obj          占位符SQL的填充对象。
     * @param i_Conn         数据库连接池
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(Object i_Obj ,Connection i_Conn ,XSQLBigData i_XSQLBigData)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.queryBigData(this.content.getSQL(i_Obj) ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
	
	
	
	/**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_XSQLBigData  大数据处理接口
     * 
     * @return
     */
    public Object queryBigData(XSQLBigData i_XSQLBigData)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.queryBigData(this.content.getSQL() ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的查询。 -- 无填充值的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(Connection i_Conn ,XSQLBigData i_XSQLBigData)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.queryBigData(this.content.getSQL() ,i_Conn ,i_XSQLBigData);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
    
    
    
    /**
     * 常规SQL的查询。（内部不再关闭数据库连接）
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_SQL          常规SQL语句
     * @param i_Conn         数据库连接
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(String i_SQL ,Connection i_Conn ,XSQLBigData i_XSQLBigData)
    {
        Statement  v_Statement = null;
        ResultSet  v_Resultset = null;
        long       v_BeginTime = this.request().getTime();
        
        try
        {
            if ( this.result == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn)
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            
            Object v_Ret = this.result.getBigDatas(v_Resultset ,i_XSQLBigData);
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(v_Resultset ,v_Statement ,null);
        }
    }
    
    
    
    /**
     * 常规SQL的查询。
     * 
     * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-17
     * @version     v1.0
     * 
     * @param i_SQL          常规SQL语句
     * @param i_XSQLBigData  大数据处理接口
     * @return
     */
    public Object queryBigData(String i_SQL ,XSQLBigData i_XSQLBigData)
    {
        DataSourceGroup v_DSG       = null;
        Connection      v_Conn      = null;
        Statement       v_Statement = null;
        ResultSet       v_Resultset = null;
        long            v_BeginTime = this.request().getTime();
        
        try
        {
            if ( this.result == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            v_Conn      = this.getConnection(v_DSG);
            v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
            v_Resultset = v_Statement.executeQuery(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            
            Object v_Ret = this.result.getBigDatas(v_Resultset ,i_XSQLBigData);
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(v_Resultset ,v_Statement ,v_Conn);
        }
    }
	
	
	
	/**
	 * 占位符SQL的查询。-- 超级大结果集的SQL查询
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 */
	public XSQLBigger queryBigger(Map<String ,?> i_Values)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.queryBigger(this.content.getSQL(i_Values));
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @param i_FilterColNames   按输出字段名称过滤。
	 */
	public XSQLBigger queryBigger(Map<String ,?> i_Values ,List<String> i_FilterColNames)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.queryBigger(this.content.getSQL(i_Values) ,i_FilterColNames);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @param i_FilterColNoArr   按输出字段位置过滤。
	 */
	public XSQLBigger queryBigger(Map<String ,?> i_Values ,int [] i_FilterColNoArr)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.queryBigger(this.content.getSQL(i_Values) ,i_FilterColNoArr);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。 -- 超级大结果集的SQL查询
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 */
	public XSQLBigger queryBigger(Object i_Obj)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.queryBigger(this.content.getSQL(i_Obj));
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @param i_FilterColNames   按输出字段名称过滤。
	 */
	public XSQLBigger queryBigger(Object i_Obj ,List<String> i_FilterColNames)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.queryBigger(this.content.getSQL(i_Obj) ,i_FilterColNames);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @param i_FilterColNoArr   按输出字段位置过滤。
	 */
	public XSQLBigger queryBigger(Object i_Obj ,int [] i_FilterColNoArr)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.queryBigger(this.content.getSQL(i_Obj) ,i_FilterColNoArr);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 常规SQL的查询。 -- 超级大结果集的SQL查询
	 * 
	 * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
	 * 
	 * 2. 通过 getBiggerNextData() 方法获取下一页数据
	 * 
	 * @param i_SQL              常规SQL语句
	 */
	public XSQLBigger queryBigger(String i_SQL)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
	    ResultSet       v_Resultset = null;
	    long            v_RowSize   = 0;
	    long            v_BeginTime = this.request().getTime();
		
		try
		{
		    if ( this.result == null )
	        {
	            throw new NullPointerException("Result is null of XSQL.");
	        }
	        
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_RowSize   = this.getSQLCount("SELECT COUNT(1) FROM ( " + i_SQL + " ) HY");
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_Resultset = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			this.result.initTotalInfo();
			
			XSQLBigger v_Bigger = new XSQLBigger(this ,v_Resultset ,v_RowSize ,(new Date()).getTime() - v_BeginTime);
			
			this.result.getDatas(v_Bigger);
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Bigger;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			// this.closeDB(v_Resultset ,v_Statement ,v_Conn);  超大结果集，不自动关闭数据库连接
		}
	}
	
	
	
	/**
	 * 常规SQL的查询。(按输出字段名称过滤) -- 超级大结果集的SQL查询
	 * 
	 * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
	 * 
	 * 2. 通过 getBiggerNextData() 方法获取下一页数据
	 * 
	 * @param i_SQL              常规SQL语句
	 * @param i_FilterColNames   按输出字段名称过滤。
	 */
	public XSQLBigger queryBigger(String i_SQL ,List<String> i_FilterColNames)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
	    ResultSet       v_Resultset = null;
	    long            v_RowSize   = 0;
	    long            v_BeginTime = this.request().getTime();
		
		try
		{
		    if ( this.result == null )
	        {
	            throw new NullPointerException("Result is null of XSQL.");
	        }
	        
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_RowSize   = this.getSQLCount("SELECT COUNT(1) FROM ( " + i_SQL + " ) HY");
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_Resultset = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			this.result.initTotalInfo();
			
			XSQLBigger v_Bigger = new XSQLBigger(this ,v_Resultset ,v_RowSize ,(new Date()).getTime() - v_BeginTime);
			
			this.result.getDatas(v_Bigger ,i_FilterColNames);
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Bigger;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			// this.closeDB(v_Resultset ,v_Statement ,v_Conn);  超大结果集，不自动关闭数据库连接
		}
	}
	
	
	
	/**
	 * 常规SQL的查询。(按输出字段位置过滤) -- 超级大结果集的SQL查询
	 * 
	 * 1. 提交数据库执行 i_SQL ，将数据库结果集转化为Java实例对象
	 * 
	 * 2. 通过 getBiggerNextData() 方法获取下一页数据
	 * 
	 * @param i_SQL              常规SQL语句
	 * @param i_FilterColNoArr   按输出字段位置过滤。
	 */
	public XSQLBigger queryBigger(String i_SQL ,int [] i_FilterColNoArr)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
	    ResultSet       v_Resultset = null;
	    long            v_RowSize   = 0;
	    long            v_BeginTime = this.request().getTime();
		
		try
		{
		    if ( this.result == null )
	        {
	            throw new NullPointerException("Result is null of XSQL.");
	        }
	        
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_RowSize   = this.getSQLCount("SELECT COUNT(1) FROM ( " + i_SQL + " ) HY");
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_Resultset = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			this.result.initTotalInfo();
			
			XSQLBigger v_Bigger = new XSQLBigger(this ,v_Resultset ,v_RowSize ,(new Date()).getTime() - v_BeginTime);
			
			this.result.getDatas(v_Bigger ,i_FilterColNoArr);
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Bigger;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			// this.closeDB(v_Resultset ,v_Statement ,v_Conn);  超大结果集，不自动关闭数据库连接
		}
	}
	
	
	
	/**
     * 统计记录数据：占位符SQL的查询。
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @return
     * @throws Exception 
     */
    public long getSQLCount(Map<String ,?> i_Values)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.getSQLCount(this.content.getSQL(i_Values));
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 统计记录数据：占位符SQL的查询。
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. 并提交数据库执行SQL，将数据库结果集转化为Java实例对象返回
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @return
     * @throws Exception 
     */
    public long getSQLCount(Object i_Obj)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.getSQLCount(this.content.getSQL(i_Obj));
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 查询记录总数
     * 
     * 模块SQL的形式如：SELECT COUNT(1) FROM ...
     * 
     * @return
     * @throws Exception
     */
    public long getSQLCount()
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.getSQLCount(this.content.getSQL());
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
	
	
	
	/**
	 * 查询记录总数
	 * 
	 * 模块SQL的形式如：SELECT COUNT(1) FROM ...
	 * 
	 * @param i_SQL
	 * @return
	 * @throws Exception
	 */
	public long getSQLCount(String i_SQL)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
	    ResultSet       v_Resultset = null;
	    long            v_SQLCount  = 0;
	    long            v_BeginTime = this.request().getTime();

	    
	    try
	    {
	        v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
	        
	    	v_Conn      = this.getConnection(v_DSG);
	    	v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
	    	v_Resultset = v_Statement.executeQuery(i_SQL);
	    	$SQLBusway.put(new XSQLLog(i_SQL));
	    	
	    	if ( v_Resultset.next() ) 
	    	{
	    		v_SQLCount = v_Resultset.getLong(1);
	    	}
	    	
	    	Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
	    }
	    catch (Exception exce)
	    {
	        erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
	    }
	    finally
	    {
	    	this.closeDB(v_Resultset ,v_Statement ,v_Conn);
	    }
    	
    	
    	return v_SQLCount;
	}
	
	
	
	/**
     * 根据写入大数据的SQL语法(如下)，创建一个XSQL对象。
     * 
     *    SELECT  clobColumn 
     *      FROM  tablename 
     *     WHERE  id = 1 
     *       FOR  UPDATE
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @return  返回创建XSQL对象实例，对应的XJava标记 
     */
    private String createLobXSQL()
    {
        String v_DBType   = this.getDataSourceGroup().getDbProductType();
        Param  v_Template = XSQLWriteLob.getLobTempalte(v_DBType);
        
        if ( v_Template == null || Help.isNull(v_Template.getValue()) )
        {
            return null;
        }
        
        String v_SQL = StringHelp.replaceAll(v_Template.getValue() 
                                            ,new String[]{":TableName"                        ,":LobName"   ,":IdWheres"}
                                            ,new String[]{this.getContent().getSqlTableName() ,this.lobName ,this.lobWheres});
        
        XSQL v_LobXSQL = new XSQL();
        
        v_LobXSQL.setDataSourceGroup(this.getDataSourceGroup());
        v_LobXSQL.setContent(v_SQL);
        v_LobXSQL.setXJavaID(Help.NVL(this.getXJavaID() ,"XSQL_WriteLob") + "_" + this.lobName + "_" + Date.getNowTime().getFull_ID());
        
        XJava.putObject(v_LobXSQL.getXJavaID() ,v_LobXSQL);
        
        return v_LobXSQL.getXJavaID();
    }
    
    
    
    /**
     * 插入或更新一行数据之后再写入大数据字段（如,CLob）。
     * 
     * 在executeUpdate()或execute()方法内部被自动执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *              v2.0  2018-07-25  添加：支持多个大数据字段（如，CLob）的写入。
     *
     * @param i_Values     占位符SQL的填充集合。
     * @param i_DataCount  插入或更新语句影响的记录数
     * @return
     */
    private int executeUpdate_AfterWriteLob(Map<String ,?> i_Values ,int i_DataCount)
    {
        if ( i_DataCount <= 0 )
        {
            return i_DataCount;
        }
        
        if ( !Help.isNull(this.lobName) && !Help.isNull(this.lobWheres) )
        {
            String [] v_LobNames  = this.lobName.split($LobName_Split);
            String [] v_LobValues = new String[v_LobNames.length];
            for (int i=0; i<v_LobNames.length; i++)
            {
                Object v_LobValue = MethodReflect.getMapValue(i_Values ,v_LobNames[i].trim());
                if ( v_LobValue != null )
                {
                    v_LobValues[i] = v_LobValue.toString();
                }
                else
                {
                    v_LobValues[i] = "";
                }
            }
            
            if ( !Help.isNull(v_LobValues) )
            {
                return XJava.getXSQL(getLobXSQLID()).executeUpdateCLob(i_Values ,v_LobValues);
            }
        }
        
        return i_DataCount;
    }
    
    
    
    /**
     * 获取或创建处理Lob类型的XSQL的标记XJavaID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @return
     */
    private synchronized String getLobXSQLID()
    {
        if ( Help.isNull(this.lobXSQLID) )
        {
            this.lobXSQLID = this.createLobXSQL();
        }
        
        return this.lobXSQLID;
    }
    
    
    
    /**
     * 插入或更新一行数据之后再写入大数据字段（如,CLob）。
     * 
     * 在executeUpdate()或execute()方法内部被自动执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @param i_Values     占位符SQL的填充集合。
     * @param i_DataCount  插入或更新语句影响的记录数
     * @return
     */
    private int executeUpdate_AfterWriteLob(Object i_Obj ,int i_DataCount)
    {
        if ( i_DataCount <= 0 )
        {
            return i_DataCount;
        }
        
        if ( !Help.isNull(this.lobName) && !Help.isNull(this.lobWheres) )
        {
            String [] v_LobNames  = this.lobName.split($LobName_Split);
            String [] v_LobValues = new String[v_LobNames.length];
            for (int i=0; i<v_LobNames.length; i++)
            {
                try
                {
                    MethodReflect v_MethodReflect = new MethodReflect(i_Obj ,v_LobNames[i].trim() ,true ,MethodReflect.$NormType_Getter);
                    
                    if ( v_MethodReflect != null )
                    {
                        Object v_LobValue = v_MethodReflect.invoke();
                        if ( v_LobValue != null )
                        {
                            v_LobValues[i] = v_LobValue.toString();
                        }
                        else
                        {
                            v_LobValues[i] = "";
                        }
                    }
                }
                catch (Exception exce)
                {
                    // 有些:xx占位符可能找不到对应Java的Getter方法，所以忽略
                    // Nothing.
                    return i_DataCount;
                }
            }
            
            if ( !Help.isNull(v_LobValues) )
            {
                return XJava.getXSQL(getLobXSQLID()).executeUpdateCLob(i_Obj ,v_LobValues);
            }
        }
        
        return i_DataCount;
    }
	
	
	
	/**
	 * 占位符SQL的Insert语句与Update语句的执行。 -- 无填充值的
	 * 
	 * @return                   返回语句影响的记录数。
	 */
	public int executeUpdate()
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.executeUpdate(this.content.getSQL());
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes();
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的Insert语句与Update语句的执行。
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 
	 * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @return                   返回语句影响的记录数。
	 */
	public int executeUpdate(Map<String ,?> i_Values)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    int v_Ret = this.executeUpdate(this.content.getSQL(i_Values));
		    
		    return executeUpdate_AfterWriteLob(i_Values ,v_Ret);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的Insert语句与Update语句的执行。
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 
	 * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @return                   返回语句影响的记录数。
	 */
	public int executeUpdate(Object i_Obj)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    int v_Ret = this.executeUpdate(this.content.getSQL(i_Obj));
		    return executeUpdate_AfterWriteLob(i_Obj ,v_Ret);
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 常规Insert语句与Update语句的执行。
	 * 
	 * @param i_SQL              常规SQL语句
	 * @return                   返回语句影响的记录数。
	 */
	public int executeUpdate(String i_SQL)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
		long            v_BeginTime = this.request().getTime();
		
		try
		{
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement();
			int v_Count = v_Statement.executeUpdate(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return v_Count;
		}
		catch (Exception exce)
		{
		    erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			this.closeDB(null ,v_Statement ,v_Conn);
		}
	}
	
	
	
	/**
     * 占位符SQL的Insert语句与Update语句的执行。 -- 无填充值的（内部不再关闭数据库连接）
     * 
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdate(Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.executeUpdate(this.content.getSQL() ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdate(Map<String ,?> i_Values ,Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.executeUpdate(this.content.getSQL(i_Values) ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
    
    
    
    /**
     * 占位符SQL的Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdate(Object i_Obj ,Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.executeUpdate(this.content.getSQL(i_Obj) ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
    
    
    
    /**
     * 常规Insert语句与Update语句的执行。（内部不再关闭数据库连接）
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdate(String i_SQL ,Connection i_Conn)
    {
        Statement v_Statement = null;
        long      v_BeginTime = this.request().getTime();
        
        try
        {
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn)
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement();
            int v_Count = v_Statement.executeUpdate(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
            
            return v_Count;
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(null ,v_Statement ,null);
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdates(List<?> i_ObjList)
    {
        boolean v_IsError = false;

        try
        {
            return this.executeUpdates_Inner(i_ObjList ,null);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executeUpdates(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdates(List<?> i_ObjList ,Connection i_Conn)
    {
        boolean v_IsError = false;

        try
        {
            return this.executeUpdates_Inner(i_ObjList ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executeUpdates(i_ObjList);
            }
        }
    }
	
	
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    private int executeUpdates_Inner(List<?> i_ObjList ,Connection i_Conn)
    {
        DataSourceGroup v_DSG        = null;
        Connection      v_Conn       = null;
        Statement       v_Statement  = null;
        boolean         v_AutoCommit = false;
        int             v_Ret        = 0;
        long            v_BeginTime  = this.request().getTime();
        String          v_SQL        = null;
        
        try
        {
            if ( this.content == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_ObjList) )
            {
                throw new NullPointerException("Batch execute update List<Object> is null.");
            }
            
            v_Conn       = i_Conn == null ? this.getConnection(v_DSG) : i_Conn;
            v_AutoCommit = v_Conn.getAutoCommit();  
            v_Conn.setAutoCommit(false);
            v_Statement  = v_Conn.createStatement();
            
            if ( this.batchCommit <= 0 )
            {
                for (int i=0; i<i_ObjList.size(); i++)
                {
                    if ( i_ObjList.get(i) != null )
                    {
                        v_SQL = this.content.getSQL(i_ObjList.get(i));
                        v_Ret += v_Statement.executeUpdate(v_SQL);
                        $SQLBusway.put(new XSQLLog(v_SQL));
                    }
                }
                
                if ( i_Conn == null )
                {
                    v_Conn.commit();  // 它与i_Conn.commit();同作用
                }
            }
            else
            {
                boolean v_IsCommit = true;
                
                for (int i=0 ,v_EC=0; i<i_ObjList.size(); i++)
                {
                    if ( i_ObjList.get(i) != null )
                    {
                        v_SQL = this.content.getSQL(i_ObjList.get(i));
                        v_Ret += v_Statement.executeUpdate(v_SQL);
                        $SQLBusway.put(new XSQLLog(v_SQL));
                        v_EC++;
                        
                        if ( v_EC % this.batchCommit == 0 )
                        {
                            v_Conn.commit();
                            v_IsCommit = true;
                        }
                        else
                        {
                            v_IsCommit = false;
                        }
                    }
                }
                
                if ( !v_IsCommit )
                {
                    v_Conn.commit();
                }
            }
            
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,v_Ret);
            return v_Ret;
        }
        catch (Exception exce)
        {
            erroring(v_SQL ,exce ,this);
            
            try
            {
                if ( i_Conn == null && v_Conn != null )
                {
                    v_Conn.rollback();
                }
            }
            catch (Exception e)
            {
                // Nothing.
            }
            
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( i_Conn == null )
            {
                try
                {
                    if ( v_Conn != null )
                    {
                        v_Conn.setAutoCommit(v_AutoCommit);
                    }
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                this.closeDB(null ,v_Statement ,v_Conn);
            }
            else
            {
                this.closeDB(null ,v_Statement ,null);
            }
        }
    }
    
    
    
    /**
     * 预解释SQL的方式填充数值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param io_PStatement  预解释SQL的对象
     * @param i_ParamIndex   填充数值的位置。下标从1开始
     * @param i_Value        填充的数值
     * @throws SQLException
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    private void preparedStatementSetValue(PreparedStatement io_PStatement ,int i_ParamIndex ,Object i_Value) throws SQLException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        if ( i_Value == null )
        {
            io_PStatement.setString(i_ParamIndex ,"");
            return;
        }
        
        Class<?> v_Class = i_Value.getClass();
        
        if ( v_Class == String.class )
        {
            io_PStatement.setString(i_ParamIndex ,(String)i_Value);
        }
        else if ( v_Class == Integer.class || v_Class == int.class)
        {
            io_PStatement.setInt(i_ParamIndex ,((Integer)i_Value).intValue());
        }
        else if ( v_Class == Double.class || v_Class == double.class )
        {
            io_PStatement.setDouble(i_ParamIndex ,((Double)i_Value).doubleValue());
        }
        else if ( v_Class == Float.class || v_Class == float.class )
        {
            io_PStatement.setFloat(i_ParamIndex ,((Float)i_Value).floatValue());
        }
        else if ( v_Class == Boolean.class || v_Class == boolean.class )
        {
            io_PStatement.setBoolean(i_ParamIndex ,((Boolean)i_Value).booleanValue());
        }
        else if ( v_Class == Long.class || v_Class == long.class )
        {
            io_PStatement.setLong(i_ParamIndex ,((Long)i_Value).longValue());
        }
        else if ( v_Class == Date.class )
        {
            // getSQLDate()的精度只到天，未到时分秒，所以换成getSQLTimestamp()方法  ZhengWei(HY) Edit 2018-05-11
            io_PStatement.setTimestamp(i_ParamIndex ,((Date)i_Value).getSQLTimestamp());
        }
        else if ( v_Class == java.util.Date.class )
        {
            // getSQLDate()的精度只到天，未到时分秒，所以换成getSQLTimestamp()方法  ZhengWei(HY) Edit 2018-05-11
            io_PStatement.setTimestamp(i_ParamIndex ,(new Date((java.util.Date)i_Value)).getSQLTimestamp());
        }
        // 添加对数据库时间的转换 Add ZhengWei(HY) 2018-05-15 
        else if ( v_Class == Timestamp.class )
        {
            io_PStatement.setTimestamp(i_ParamIndex ,(Timestamp)i_Value);
        }
        else if ( v_Class == BigDecimal.class )
        {
            io_PStatement.setBigDecimal(i_ParamIndex ,(BigDecimal)i_Value);
        }
        else if ( v_Class == MethodReflect.class )
        {
            this.preparedStatementSetValue(io_PStatement ,i_ParamIndex ,((MethodReflect)i_Value).invoke());
        }
        else if ( MethodReflect.isExtendImplement(v_Class ,Enum.class) )
        {
            @SuppressWarnings("unchecked")
            Enum<?> [] v_EnumValues = StaticReflect.getEnums((Class<? extends Enum<?>>) v_Class);
            String     v_Value      = i_Value.toString();
            boolean    v_Continue   = true;
            
            // ZhengWei(HY) Add 2018-05-08  支持枚举toString()的匹配 
            for (Enum<?> v_Enum : v_EnumValues)
            {
                if ( v_Value.equalsIgnoreCase(v_Enum.toString()) )
                {
                    io_PStatement.setInt(i_ParamIndex ,v_Enum.ordinal());
                    v_Continue = false;
                }
            }
            
            if ( v_Continue )
            {
                // ZhengWei(HY) Add 2018-05-08  支持枚举名称的匹配 
                for (Enum<?> v_Enum : v_EnumValues)
                {
                    if ( v_Value.equalsIgnoreCase(v_Enum.name()) )
                    {
                        io_PStatement.setInt(i_ParamIndex ,v_Enum.ordinal());
                        v_Continue = false;
                    }
                }
            }
            
            if ( v_Continue )
            {
                // 尝试用枚举值匹配 
                if ( Help.isNumber(v_Value) )
                {
                    int v_IntValue = Integer.parseInt(v_Value.trim());
                    if ( 0 <= v_IntValue && v_IntValue < v_EnumValues.length )
                    {
                        io_PStatement.setInt(i_ParamIndex ,v_EnumValues[v_IntValue].ordinal());
                        v_Continue = false;
                    }
                }
            }
            
            if ( v_Continue )
            {
                throw new java.lang.IndexOutOfBoundsException("Enum [" + v_Class.getName() + "] is not find Value[" + i_Value.toString() + "].");
            }
        }
        else if ( v_Class == Short.class || v_Class == short.class)
        {
            io_PStatement.setShort(i_ParamIndex ,((Short)i_Value).shortValue());
        }
        else if ( v_Class == Byte.class || v_Class == byte.class )
        {
            io_PStatement.setShort(i_ParamIndex ,((Byte)i_Value).byteValue());
        }
        else if ( v_Class == Character.class || v_Class == char.class )
        {
            io_PStatement.setString(i_ParamIndex ,i_Value.toString());
        }
        else
        {
            io_PStatement.setString(i_ParamIndex ,i_Value.toString());
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdatesPrepared(List<?> i_ObjList)
    {
        boolean v_IsError = false;

        try
        {
            return executeUpdatesPrepared_Inner(i_ObjList ,null);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executeUpdatesPrepared(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    public int executeUpdatesPrepared(List<?> i_ObjList ,Connection i_Conn)
    {
        boolean v_IsError = false;
        
        try
        {
            return executeUpdatesPrepared_Inner(i_ObjList ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executeUpdatesPrepared(i_ObjList);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     *   注意：不支持Delete语句
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注：只支持单一SQL语句的执行
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     * 
     * @param i_ObjList          占位符SQL的填充对象的集合。
     *                           1. 集合元素可以是Object
     *                           2. 集合元素可以是Map<String ,?>
     *                           3. 更可以是上面两者的混合元素组成的集合
     * @param i_Conn             数据库连接。
     *                           1. 当为空时，内部自动获取一个新的数据库连接。
     *                           2. 当有值时，内部将不关闭数据库连接，而是交给外部调用者来关闭。
     *                           3. 当有值时，内部也不执行"提交"操作（但分批提交this.batchCommit大于0时除外），而是交给外部调用者来执行"提交"。
     *                           4. 当有值时，出现异常时，内部也不执行"回滚"操作，而是交给外部调用者来执行"回滚"。
     * @return                   返回语句影响的记录数。
     */
    @SuppressWarnings("unchecked")
    private int executeUpdatesPrepared_Inner(List<?> i_ObjList ,Connection i_Conn)
    {
        DataSourceGroup   v_DSG        = null;
        Connection        v_Conn       = null;
        PreparedStatement v_PStatement = null;
        boolean           v_AutoCommit = false;
        int               v_Ret        = 0;
        long              v_BeginTime  = this.request().getTime();
        String            v_SQL        = null;
        
        try
        {
            if ( this.content == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_ObjList) )
            {
                throw new NullPointerException("Batch execute update List<Object> is null.");
            }
            
            v_Conn       = i_Conn == null ? this.getConnection(v_DSG) : i_Conn;
            v_AutoCommit = v_Conn.getAutoCommit();  
            v_Conn.setAutoCommit(false);
            v_SQL        = this.content.getPreparedSQL().getSQL();
            v_PStatement = v_Conn.prepareStatement(v_SQL);
            
            if ( this.batchCommit <= 0 )
            {
                for (int i=0; i<i_ObjList.size(); i++)
                {
                    Object v_Object = i_ObjList.get(i);
                    if ( v_Object != null )
                    {
                        if ( MethodReflect.isExtendImplement(v_Object ,Map.class) )
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : this.content.getPreparedSQL().getPlaceholders())
                            {
                                Object v_Value = MethodReflect.getMapValue((Map<String ,?>)v_Object ,v_PlaceHolder);
                                
                                this.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_Value);
                            }
                        }
                        else
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : this.content.getPreparedSQL().getPlaceholders())
                            {
                                MethodReflect v_MethodReflect = new MethodReflect(v_Object ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                                
                                this.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_MethodReflect.invoke());
                            }
                        }
                        
                        v_PStatement.addBatch(); 
                    }
                }
                
                int [] v_CountArr = v_PStatement.executeBatch();
                
                for (int v_Count : v_CountArr)
                {
                    v_Ret += v_Count;
                }
                
                if ( i_Conn == null )
                {
                    v_Conn.commit();  // 它与i_Conn.commit();同作用
                }
            }
            else
            {
                boolean v_IsCommit = true;  // 2017-11-06  修正：当预处理 this.executeUpdatesPrepared_Inner() 执行的同时 batchCommit >= 1时，可能出现未"执行executeBatch"情况
                
                for (int i=0 ,v_EC=0; i<i_ObjList.size(); i++)
                {
                    Object v_Object = i_ObjList.get(i);
                    if ( v_Object != null )
                    {
                        if ( MethodReflect.isExtendImplement(v_Object ,Map.class) )
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : this.content.getPreparedSQL().getPlaceholders())
                            {
                                Object v_Value = MethodReflect.getMapValue((Map<String ,?>)v_Object ,v_PlaceHolder);
                                
                                this.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_Value);
                            }
                        }
                        else
                        {
                            int v_ParamIndex = 0;
                            for (String v_PlaceHolder : this.content.getPreparedSQL().getPlaceholders())
                            {
                                MethodReflect v_MethodReflect = new MethodReflect(v_Object ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                                
                                this.preparedStatementSetValue(v_PStatement ,++v_ParamIndex ,v_MethodReflect.invoke());
                            }
                        }
                        
                        v_PStatement.addBatch();
                        v_EC++;
                        
                        if ( v_EC % this.batchCommit == 0 )
                        {
                            int [] v_CountArr = v_PStatement.executeBatch();
                            for (int v_Count : v_CountArr)
                            {
                                v_Ret += v_Count;
                            }
                            
                            v_Conn.commit();
                            
                            v_PStatement.clearBatch();
                            v_IsCommit = true;
                        }
                        else
                        {
                            v_IsCommit = false;
                        }
                    }
                }
                
                if ( !v_IsCommit )
                {
                    int [] v_CountArr = v_PStatement.executeBatch();
                    for (int v_Count : v_CountArr)
                    {
                        v_Ret += v_Count;
                    }
                    
                    v_Conn.commit();
                }
            }
            
            $SQLBusway.put(new XSQLLog(v_SQL));
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime ,v_Ret);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            erroring(v_SQL ,exce ,this);
            
            try
            {
                if ( i_Conn == null && v_Conn != null )
                {
                    v_Conn.rollback();
                }
            }
            catch (Exception e)
            {
                // Nothing.
            }
            
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( i_Conn == null )
            {
                try
                {
                    if ( v_Conn != null )
                    {
                        v_Conn.setAutoCommit(v_AutoCommit);
                    }
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                this.closeDB(null ,v_PStatement ,v_Conn);
            }
            else
            {
                this.closeDB(null ,v_PStatement ,null);
            }
        }
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注: 1. 支持多种不同SQL语句的执行
     *     2. 支持不同类型的多个不同数据库的操作
     *     3. 如果要有顺序的执行，请java.util.LinkedHashMap
     *     
     * 重点注意：2014-12-04
     *         建议入参使用 TablePartition。为什么呢？
     *         原因是，Hashtable.put() 同样的key多次，只保存一份value。
     *         而 TablePartition.putRows() 会将同样key的多份不同的value整合在一起。
     *         特别适应于同一份Insert语句的SQL，执行多批数据的插入的情况
     * 
     * @param i_XSQLs            XSQL及占位符SQL的填充对象的集合。
     *                           1. List<?>集合元素可以是Object
     *                           2. List<?>集合元素可以是Map<String ,?>
     *                           3. List<?>更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    @SuppressWarnings({"rawtypes" ,"unchecked"})
    public static int executeUpdates(PartitionMap<XSQL ,?> i_XSQLs)
    {
        return executeUpdates((Map)i_XSQLs ,0);
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注: 1. 支持多种不同SQL语句的执行
     *     2. 支持不同类型的多个不同数据库的操作
     *     3. 如果要有顺序的执行，请java.util.LinkedHashMap
     *     
     * 重点注意：2014-12-04
     *         建议入参使用 TablePartition。为什么呢？
     *         原因是，Hashtable.put() 同样的key多次，只保存一份value。
     *         而 TablePartition.putRows() 会将同样key的多份不同的value整合在一起。
     *         特别适应于同一份Insert语句的SQL，执行多批数据的插入的情况
     * 
     * @param i_XSQLs            XSQL及占位符SQL的填充对象的集合。
     *                           1. List<?>集合元素可以是Object
     *                           2. List<?>集合元素可以是Map<String ,?>
     *                           3. List<?>更可以是上面两者的混合元素组成的集合
     * @return                   返回语句影响的记录数。
     */
    public static int executeUpdates(Map<XSQL ,List<?>> i_XSQLs)
    {
        return executeUpdates(i_XSQLs ,0);
    }
    
    
    
    /**
     * 批量执行：占位符SQL的Insert语句与Update语句的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * 注: 1. 支持多种不同SQL语句的执行
     *     2. 支持不同类型的多个不同数据库的操作
     *     3. 如果要有顺序的执行，请java.util.LinkedHashMap
     *
     * 重点注意：2014-12-04
     *         建议入参使用 TablePartition<XSQL ,?>，（注意不是 TablePartition<XSQL ,List<?>>）
     *         为什么呢？
     *         原因是，Hashtable.put() 同样的key多次，只保存一份value。
     *         而 TablePartition.putRows() 会将同样key的多份不同的value整合在一起。
     *         特别适应于同一份Insert语句的SQL，执行多批数据的插入的情况
     * 
     * @param i_XSQLs            XSQL及占位符SQL的填充对象的集合。
     *                           1. List<?>集合元素可以是Object
     *                           2. List<?>集合元素可以是Map<String ,?>
     *                           3. List<?>更可以是上面两者的混合元素组成的集合
     * @param i_BatchCommit      批量执行 Insert、Update、Delete 时，达到提交的提交点
     * @return                   返回语句影响的记录数。
     */
    public static <R> int executeUpdates(Map<XSQL ,List<?>> i_XSQLs ,int i_BatchCommit)
    {
        Map<XSQL ,DataSourceGroup> v_DSGMap         = new HashMap<XSQL ,DataSourceGroup>();
        DataSourceGroup            v_DSG            = null;
        List<Connection>           v_Conns          = new ArrayList<Connection>();
        XSQL                       v_XSQL           = null;
        XSQL                       v_XSQLError      = null;
        Object                     v_ParamObj       = null;
        int                        v_Ret            = 0;
        long                       v_TimeLenSum     = 0;                               // 每段SQL用时时长的累计值。此值一般情况下小于 v_TimeLenTotal
        long                       v_TimeLenTotal   = 0;                               // 总体用时时长
        long                       v_BeginTimeTotal = Date.getNowTime().getTime();     // 总体开始时间
        long                       v_BeginTime      = 0;
        List<Return<XSQL>>         v_Totals         = null;
        Return<XSQL>               v_TotalCache     = null;
        
        try
        {
            if ( Help.isNull(i_XSQLs) )
            {
                throw new NullPointerException("XSQLs is null.");
            }
            
            for (XSQL v_XSQLTemp : i_XSQLs.keySet())
            {
                if ( v_XSQLTemp.content == null )
                {
                    throw new NullPointerException("Content is null of XSQL.");
                }
                
                if ( Help.isNull(i_XSQLs.get(v_XSQLTemp)) )
                {
                    throw new NullPointerException("Batch execute update List<Object> is null.");
                }
                
                v_DSG = v_XSQLTemp.getDataSourceGroup();
                if ( !v_DSG.isValid() )
                {
                    throw new RuntimeException("DataSourceGroup is not valid.");
                }
                v_DSGMap.put(v_XSQLTemp ,v_DSG);
            }
            
            v_Totals = new ArrayList<Return<XSQL>>(i_XSQLs.size());
            v_XSQL   = i_XSQLs.keySet().iterator().next();
            
            if ( i_BatchCommit <= 0 )
            {
                for (XSQL v_XSQLTemp : i_XSQLs.keySet())
                {
                    Connection v_Conn = v_XSQL.getConnection(v_DSG);
                    v_Conn.setAutoCommit(false);
                    v_Conns.add(v_Conn);
                    
                    v_XSQLError = v_XSQLTemp;
                    List<?> v_ObjList = i_XSQLs.get(v_XSQLTemp);
                    
                    for (int i=0; i<v_ObjList.size(); i++)
                    {
                        v_ParamObj = v_ObjList.get(i);
                        
                        if ( v_ParamObj != null )
                        {
                            v_BeginTime = Date.getNowTime().getTime();
                            v_Ret += v_XSQLTemp.executeUpdate(v_ParamObj ,v_Conn);
                            
                            v_TotalCache = new Return<XSQL>();
                            v_TotalCache.paramInt((int)(Date.getNowTime().getTime() - v_BeginTime));
                            v_Totals.add(v_TotalCache.paramObj(v_XSQLTemp));
                            
                            v_TimeLenSum += v_TotalCache.paramInt;
                        }
                    }
                }
                
                commits(1 ,v_Conns);
            }
            else
            {
                boolean v_IsCommit = true;
                
                for (XSQL v_XSQLTemp : i_XSQLs.keySet())
                {
                    Connection v_Conn = v_XSQL.getConnection(v_DSG);
                    v_Conn.setAutoCommit(false);
                    v_Conns.add(v_Conn);
                    
                    v_XSQLError = v_XSQLTemp;
                    List<?> v_ObjList = i_XSQLs.get(v_XSQLTemp);
                    
                    for (int i=0; i<v_ObjList.size(); i++)
                    {
                        v_ParamObj = v_ObjList.get(i);
                        
                        if ( v_ParamObj != null )
                        {
                            v_BeginTime = Date.getNowTime().getTime();
                            v_Ret += v_XSQLTemp.executeUpdate(v_ParamObj ,v_Conn);
                            
                            v_TotalCache = new Return<XSQL>();
                            v_TotalCache.paramInt((int)(Date.getNowTime().getTime() - v_BeginTime));
                            v_Totals.add(v_TotalCache.paramObj(v_XSQLTemp));
                            
                            v_TimeLenSum += v_TotalCache.paramInt;
                            
                            if ( v_Totals.size() % i_BatchCommit == 0 )
                            {
                                v_Conn.commit();
                                v_IsCommit = true;
                            }
                            else
                            {
                                v_IsCommit = false;
                            }
                        }
                    }
                    
                    if ( !v_IsCommit )
                    {
                        v_Conn.commit();
                        v_IsCommit = true;
                    }
                }
            }
            
            // 计算出总用时与每段SQL累计用时之差，后平摊到每段SQL的用时时长上。
            v_TimeLenTotal = Date.getNowTime().getTime() - v_BeginTimeTotal;
            double v_Per   = Help.division(v_TimeLenTotal - v_TimeLenSum ,v_Totals.size());
            
            // 当操作数据库成功后，统一记录统计信息
            for (Return<XSQL> v_Total : v_Totals)
            {
                v_Total.paramObj.successTimeLen += v_Per;
            }
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            String v_SQLError = "";
            if ( v_XSQLError != null )
            {
                v_SQLError = v_XSQLError.content.getSQL(v_ParamObj);
            }
            erroring(v_SQLError ,exce ,v_XSQLError);
            
            if ( !Help.isNull(v_Conns) )
            {
                rollbacks(v_Conns);
            }
            
            // 计算出总用时与每段SQL累计用时之差，后平摊到每段SQL的用时时长上。
            v_TimeLenTotal = Date.getNowTime().getTime() - v_BeginTimeTotal;
            double v_Per   = Help.division(v_TimeLenTotal - v_TimeLenSum ,v_Totals.size());
            
            // 当操作数据库异常后，记录之前成功的XQL的统计信息
            for (int i=0; i<v_Totals.size() ; i++)
            {
                Return<XSQL> v_Total = v_Totals.get(i);
                v_Total.paramObj.successTimeLen += v_Per;
            }
            
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            setAutoCommits(v_Conns ,true);
            closeDB(v_Conns);
        }
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行 -- 无填充值的
     * 
     * 1. CLob类型必须在SELECT语句的第一个输出字段的位置
     * 2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public int executeUpdateCLob(String ... i_ClobTexts)
    {
        checkContent();
        
        return this.executeUpdateCLobSQL(this.content.getSQL() ,i_ClobTexts);
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行。
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 2. CLob类型必须在SELECT语句的第一个输出字段的位置
     * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public int executeUpdateCLob(Map<String ,?> i_Values ,String ... i_ClobTexts)
    {
        checkContent();
        
        return this.executeUpdateCLobSQL(this.content.getSQL(i_Values) ,i_ClobTexts);
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作
     * 
     * 占位符SQL的的执行。
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 2. CLob类型必须在SELECT语句的第一个输出字段的位置
     * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    public int executeUpdateCLob(Object i_Obj ,String ... i_ClobTexts)
    {
        checkContent();
        
        return this.executeUpdateCLobSQL(this.content.getSQL(i_Obj) ,i_ClobTexts);
    }
    
    
    
    /**
     * 针对数据库的CLob类型的填充数据的操作。
     * 可以简单理解为Update语句的操作 
     * 
     * 写入Clob方法1（本类实现的方法）：
     *    Oracle中Clob数据类型是不能够直接插入的，但是可以通过流的形式对Clob类型数据写入或者读取。
     *    INSERT INTO tablename
     *               (
     *                id 
     *               ,clobColumn
     *               ) 
     *        VALUES (
     *                1
     *               ,EMPTY_CLOB()
     *               );
     *    
     *    SELECT  clobColumn 
     *      FROM  tablename 
     *     WHERE  id = 1 
     *       FOR  UPDATE
     *    
     * 写入Clob方法2：
     *    通过TO_CLOB将字符转为clob类型，每个转换的参数不能超过2000个字符，多个部分通过连接符 || 连接
     *    INSERT INTO tablename
     *               (
     *                varcharColumn 
     *               ,clobColumn
     *               ) 
     *        VALUES (
     *                'string part'
     *               ,TO_CLOB('clob chars part1 ') || TO_CLOB('clob chars part2')
     *               );
     *               
     * @author      ZhengWei(HY)
     * @createDate  2018-07-01
     * @version     v1.0
     *              v2.0  2018-07-25  支持多个长文本信息的写入
     * 
     * @param i_SQL              带有Clob字段的查询SQL语句 
     *                           1. CLob类型必须在SELECT语句的第一个输出字段的位置
     *                           2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
     *                           3. 只操作首条数据记录
     * @param i_ClobTexts        多个长文本信息
     * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
     */
    @SuppressWarnings("deprecation")
    public int executeUpdateCLobSQL(String i_SQL ,String ... i_ClobTexts)
    {
        DataSourceGroup     v_DSG            = null;
        Connection          v_Conn           = null;
        Statement           v_Statement      = null;
        ResultSet           v_ResultSet      = null;
        boolean             v_Old_AutoCommit = false; // 保存原始状态，使用完后，再恢复原状
        int                 v_ExecResult     = -1;    // 执行结果。0:没有执行  1:需要Commit  -1:需要RollBack
        Writer              v_Output         = null;
        long                v_BeginTime      = this.request().getTime();
        
        try
        {
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( Help.isNull(i_ClobTexts) )
            {
                throw new NullPointerException("ClobTexts is null of XSQL.");
            }
            
            
            v_Conn           = this.getConnection(v_DSG);
            v_Old_AutoCommit = v_Conn.getAutoCommit();
            v_Conn.setAutoCommit(false);
            v_Statement      = v_Conn.createStatement();
            v_ResultSet      = v_Statement.executeQuery(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            
            if ( v_ResultSet.next() )
            {
                int v_ColCount  = v_ResultSet.getMetaData().getColumnCount();
                int v_CLobIndex = 0;
                for (int v_ColIndex=1; v_ColIndex<=v_ColCount; v_ColIndex++)
                {
                    int v_ColType = v_ResultSet.getMetaData().getColumnType(v_ColIndex);
                    if ( Types.CLOB  == v_ColType 
                      || Types.NCLOB == v_ColType )
                    {
                        // 获取数据流
                        CLOB v_CLob = (CLOB)v_ResultSet.getClob(v_ColCount);
                        v_Output = v_CLob.getCharacterOutputStream();
                    }
                    else
                    {
                        continue;
                    }
                    
                    v_Output.write(Help.NVL(i_ClobTexts[v_CLobIndex++]));
                    
                    try
                    {
                        v_Output.flush();
                        v_Output.close();
                    }
                    catch (Exception exce)
                    {
                        exce.printStackTrace();
                    }
                    
                    v_Output = null;
                }
                
                Date v_EndTime = Date.getNowTime();
                this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                v_ExecResult = 1;
            }
            else
            {
                v_ExecResult = 0;
            }
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            if ( v_Conn != null )
            {
                try
                {
                    if ( v_ExecResult == 1 )
                    {
                        v_Conn.commit();
                    }
                    else if ( v_ExecResult == -1 )
                    {
                        v_Conn.rollback();
                    }
                    
                    v_Conn.setAutoCommit(v_Old_AutoCommit);
                }
                catch (Exception exce)
                {
                    exce.printStackTrace();
                }
            }
            
            this.closeDB(v_ResultSet ,v_Statement ,v_Conn);
        }
        
        return v_ExecResult;
    }
    
    
	
	/**
	 * 针对数据库的BLob类型的填充数据的操作。
	 * 可以简单理解为Update语句的操作
	 * 
	 * 占位符SQL的的执行 -- 无填充值的
	 * 
	 * 1. BLob类型必须在SELECT语句的第一个输出字段的位置
	 * 2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
	 * 
	 * @param i_File             文件对象
	 * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
	 */
	public int executeUpdateBLob(File i_File)
	{
	    checkContent();
		
		return this.executeUpdateBLob(this.content.getSQL() ,i_File);
	}
	
	
	
	/**
	 * 针对数据库的BLob类型的填充数据的操作。
	 * 可以简单理解为Update语句的操作
	 * 
	 * 占位符SQL的的执行。
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 2. BLob类型必须在SELECT语句的第一个输出字段的位置
	 * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @param i_File             文件对象
	 * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
	 */
	public int executeUpdateBLob(Map<String ,?> i_Values ,File i_File)
	{
	    checkContent();
		
		return this.executeUpdateBLob(this.content.getSQL(i_Values) ,i_File);
	}
	
	
	
	/**
	 * 针对数据库的BLob类型的填充数据的操作。
	 * 可以简单理解为Update语句的操作
	 * 
	 * 占位符SQL的的执行。
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 2. BLob类型必须在SELECT语句的第一个输出字段的位置
	 * 3. 对于Oracle数据库，必须有 FOR UPDATE 关键字
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @param i_File             文件对象
	 * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
	 */
	public int executeUpdateBLob(Object i_Obj ,File i_File)
	{
	    checkContent();
		
		return this.executeUpdateBLob(this.content.getSQL(i_Obj) ,i_File);
	}
	
	
	
	/**
	 * 针对数据库的BLob类型的填充数据的操作。
	 * 可以简单理解为Update语句的操作 
	 * 
     * 写入Blob方法：
     *    Oracle中Blob数据类型是不能够直接插入的，但是可以通过流的形式对Blob类型数据写入或者读取。
     *    INSERT INTO tablename
     *               (
     *                id 
     *               ,blobColumn
     *               ) 
     *        VALUES (
     *                1
     *               ,EMPTY_BLOB()
     *               );
     *    
     *    SELECT  blobColumn 
     *      FROM  tablename 
     *     WHERE  id = 1 
     *       FOR  UPDATE
	 * 
	 * @param i_SQL              带有Blob字段的查询SQL语句 
	 *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
	 *                           2. 对于Oracle数据库，必须有 FOR UPDATE 关键字
	 *                           3. 只操作首条数据记录
	 * @param i_File             文件对象
	 * @return                   返回语句影响的记录数。正常情况下，只操作首条数据记录，即返回 1。
	 */
	@SuppressWarnings("deprecation")
    public int executeUpdateBLob(String i_SQL ,File i_File)
	{
	    DataSourceGroup     v_DSG            = null;
		Connection          v_Conn           = null;
		Statement           v_Statement      = null;
		ResultSet           v_ResultSet      = null;
		boolean             v_Old_AutoCommit = false; // 保存原始状态，使用完后，再恢复原状
		int                 v_ExecResult     = -1;    // 执行结果。0:没有执行  1:需要Commit  -1:需要RollBack
		PrintStream         v_Output         = null;
		BufferedInputStream v_Input          = null;
		byte []             v_ByteBuffer     = new byte[$BufferSize];
		int                 v_DataLen        = 0;
		DefaultBLobEvent    v_Event          = null;
		long                v_BLobingSize    = 0;
		boolean             v_IsContinue     = true;
		long                v_BeginTime      = this.request().getTime();
		
		try
		{
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
	        
	        if ( i_File == null )
	        {
	            throw new NullPointerException("File is null of XSQL.");
	        }
	        
	        v_Event = new DefaultBLobEvent(this ,i_File.length());
	        v_Event.setActionType(1);
		    
			v_Conn           = this.getConnection(v_DSG);
			v_Old_AutoCommit = v_Conn.getAutoCommit();
			v_Conn.setAutoCommit(false);
			v_Statement      = v_Conn.createStatement();
			v_ResultSet      = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			
			v_IsContinue = this.fireBLobBeforeListener(v_Event);
			
			
			if ( v_IsContinue && v_ResultSet.next() )
			{
				// 获取数据流
				BLOB v_BLob = (BLOB)v_ResultSet.getBlob(1);
				v_Output = new PrintStream(v_BLob.getBinaryOutputStream());
				v_Input  = new BufferedInputStream(new FileInputStream(i_File));
				
				
				if ( this.blobSafe )
				{
					while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 ) 
					{   
						v_Output.write(ByteHelp.xorMV(v_ByteBuffer ,0 ,v_DataLen) ,0 ,v_DataLen);
						
						v_BLobingSize += v_DataLen;
						
						v_Event.setCompleteSize(v_BLobingSize);
						v_IsContinue = this.fireBLobingListener(v_Event);
					}
				}
				else
				{
					while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 ) 
					{   
						v_Output.write(v_ByteBuffer ,0 ,v_DataLen);
						
						v_BLobingSize += v_DataLen;
						
						v_Event.setCompleteSize(v_BLobingSize);
						v_IsContinue = this.fireBLobingListener(v_Event);
					}
				}
				
				Date v_EndTime = Date.getNowTime();
	            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
				v_ExecResult = 1;
				v_Event.setSucceedFinish();
			}
			else
			{
				v_ExecResult = 0;
			}
		}
		catch (Exception exce)
		{
		    if ( v_Event == null )
		    {
		        v_Event = new DefaultBLobEvent(this ,0);
		    }
			v_Event.setEndTime();
			erroring(i_SQL ,exce ,this);
			throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			if ( v_Input != null )
			{
				try
				{
					v_Input.close();
				}
				catch (Exception exce)
				{
					exce.printStackTrace();
				}
				
				v_Input = null;
			}
			
			if ( v_Output != null )
			{
				try
				{
					v_Output.flush();
					v_Output.close();
				}
				catch (Exception exce)
				{
					exce.printStackTrace();
				}
				
				v_Output = null;
			}
			
			if ( v_Conn != null )
			{
				try
				{
					if ( v_ExecResult == 1 )
					{
						v_Conn.commit();
					}
					else if ( v_ExecResult == -1 )
					{
						v_Conn.rollback();
					}
					
					v_Conn.setAutoCommit(v_Old_AutoCommit);
				}
				catch (Exception exce)
				{
					exce.printStackTrace();
				}
			}
			
			this.closeDB(v_ResultSet ,v_Statement ,v_Conn);
			
			this.fireBLobAfterListener(v_Event);
		}
		
		return v_ExecResult;
	}
	
	
	
	/**
	 * 针对数据库的BLob类型转换成文件并保存
	 * 
	 * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
	 *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
	 *                           2. 只操作首条数据记录
	 * @return
	 */
	public boolean executeGetBLob(File io_SaveFile)
	{
	    checkContent();
		
		return this.executeGetBLob(this.content.getSQL() ,io_SaveFile);
	}
	
	
	
	/**
	 * 针对数据库的BLob类型转换成文件并保存
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
	 *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
	 *                           2. 只操作首条数据记录
	 * @return
	 */
	public boolean executeGetBLob(Map<String ,?> i_Values ,File io_SaveFile)
	{
	    checkContent();
		
		return this.executeGetBLob(this.content.getSQL(i_Values) ,io_SaveFile);
	}
	
	
	
	/**
	 * 针对数据库的BLob类型转换成文件并保存
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
	 *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
	 *                           2. 只操作首条数据记录
	 * @return
	 */
	public boolean executeGetBLob(Object i_Obj ,File io_SaveFile)
	{
	    checkContent();
		
		return this.executeGetBLob(this.content.getSQL(i_Obj) ,io_SaveFile);
	}
	
	
	
	/**
	 * 针对数据库的BLob类型转换成文件并保存
	 * 
	 * @param i_SQL              常规SQL语句 
	 * @param io_SaveFile        保存的文件对象（如果文件已存，会被覆盖保存）
	 *                           1. BLob类型必须在SELECT语句的第一个输出字段的位置
	 *                           2. 只操作首条数据记录
	 * @return
	 */
	public boolean executeGetBLob(String i_SQL ,File io_SaveFile)
	{
	    DataSourceGroup     v_DSG         = null;
		Connection          v_Conn        = null;
		Statement           v_Statement   = null;
	    ResultSet           v_ResultSet   = null;
	    BufferedInputStream v_Input       = null;
	    PrintStream         v_Output      = null;
	    byte []             v_ByteBuffer  = new byte[$BufferSize];
		int                 v_DataLen     = 0;
		boolean             v_ExecResult  = false;
		DefaultBLobEvent    v_Event       = new DefaultBLobEvent(this);
		long                v_BLobingSize = 0;
		boolean             v_IsContinue  = true;
		long                v_BeginTime   = this.request().getTime();
		
		
		try
		{
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(i_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
	        
	        if ( io_SaveFile == null )
	        {
	            throw new NullPointerException("SaveFile is null of XSQL.");
	        }
		    
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
			v_ResultSet = v_Statement.executeQuery(i_SQL);
			$SQLBusway.put(new XSQLLog(i_SQL));
			
			if ( v_ResultSet.next() )
			{
				// 获取数据流
				BLOB v_BLob = (BLOB)v_ResultSet.getBlob(1);
				
				v_Event.setActionType(2);
				v_Event.setSize(v_BLob.length());
				
				v_IsContinue = this.fireBLobBeforeListener(v_Event);
				
				if ( v_IsContinue )
				{
					v_Input  = new BufferedInputStream(v_BLob.getBinaryStream());
					v_Output = new PrintStream(io_SaveFile);
					
					if ( this.blobSafe )
					{
						while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 ) 
						{   
							v_Output.write(ByteHelp.xorMV(v_ByteBuffer ,0 ,v_DataLen) ,0 ,v_DataLen);
							
							v_BLobingSize += v_DataLen;
							
							v_Event.setCompleteSize(v_BLobingSize);
							v_IsContinue = this.fireBLobingListener(v_Event);
						}
					}
					else
					{
						while ( (v_DataLen = v_Input.read(v_ByteBuffer)) != -1 ) 
						{   
							v_Output.write(v_ByteBuffer ,0 ,v_DataLen);
							
							v_BLobingSize += v_DataLen;
							
							v_Event.setCompleteSize(v_BLobingSize);
							v_IsContinue = this.fireBLobingListener(v_Event);
						}
					}
					
					Date v_EndTime = Date.getNowTime();
		            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
					v_ExecResult = true;
					v_Event.setSucceedFinish();
				}
			}
		}
		catch (Exception exce)
		{
			v_Event.setEndTime();
			erroring(i_SQL ,exce ,this);
			throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			if ( v_Input != null )
			{
				try
				{
					v_Input.close();
				}
				catch (Exception exce)
				{
					exce.printStackTrace();
				}
				
				v_Input = null;
			}
			
			if ( v_Output != null )
			{
				try
				{
					v_Output.flush();
					v_Output.close();
				}
				catch (Exception exce)
				{
					exce.printStackTrace();
				}
				
				v_Output = null;
			}
			
			this.closeDB(v_ResultSet ,v_Statement ,v_Conn);
			
			this.fireBLobAfterListener(v_Event);
		}
		
		return v_ExecResult;
	}
	
	
	
	/**
	 * 占位符SQL的执行。-- 无填充值的
	 * 
	 * @return                   是否执行成功。
	 */
	public boolean execute()
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    return this.execute(this.content.getSQL());
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes();
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的执行。
	 * 
	 * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
	 * 
	 * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
	 * 
	 * @param i_Values           占位符SQL的填充集合。
	 * @return                   是否执行成功。
	 */
	public boolean execute(Map<String ,?> i_Values)
	{
	    checkContent();
		
		boolean v_IsError = false;

		try
		{
		    boolean v_Ret = this.execute(this.content.getSQL(i_Values));
		    return this.executeUpdate_AfterWriteLob(i_Values ,v_Ret ? 1 : 0) >= 1;
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Values);
		    }
		}
	}
	
	
	
	/**
	 * 占位符SQL的执行。
	 * 
	 * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
	 * 
	 * V2.0  2018-07-18  1.添加：支持CLob字段类型的简单Insert、Update语法的写入操作。
	 * 
	 * @param i_Obj              占位符SQL的填充对象。
	 * @return                   是否执行成功。
	 */
	public boolean execute(Object i_Obj)
	{
	    checkContent();
		
		boolean v_IsError = false;
		
		try
		{
		    boolean v_Ret = this.execute(this.content.getSQL(i_Obj));
            return this.executeUpdate_AfterWriteLob(i_Obj ,v_Ret ? 1 : 0) >= 1;
		}
		catch (NullPointerException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		catch (RuntimeException exce)
		{
		    v_IsError = true;
		    throw exce;
		}
		finally
		{
		    if ( this.isTriggers(v_IsError) )
		    {
		        this.trigger.executes(i_Obj);
		    }
		}
	}
	
	
	
	/**
	 * 常规SQL的执行。
	 * 
	 * @param i_SQL              常规SQL语句
	 * @return                   是否执行成功。
	 */
	public boolean execute(String i_SQL)
	{
	    DataSourceGroup v_DSG       = null;
		Connection      v_Conn      = null;
		Statement       v_Statement = null;
		long            v_BeginTime = this.request().getTime();
		String          v_SQL       = i_SQL;
		
		try
		{
		    v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
	        
	        if ( Help.isNull(v_SQL) )
	        {
	            throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
	        }
		    
			v_Conn      = this.getConnection(v_DSG);
			v_Statement = v_Conn.createStatement();
			
			if ( this.allowExecutesSplit )
			{
    			String [] v_SQLs = v_SQL.split($Executes_Split);
    			for (int i=0; i<v_SQLs.length; i++)
    			{
    			    v_SQL = v_SQLs[i].trim();
    			    v_Statement.execute(v_SQL);
    			    $SQLBusway.put(new XSQLLog(v_SQL));
    			}
			}
			else
			{
			    v_Statement.execute(v_SQL);
                $SQLBusway.put(new XSQLLog(v_SQL));
			}
			
			Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
			
			return true;
		}
		catch (Exception exce)
		{
		    erroring(v_SQL ,exce ,this);
			throw new RuntimeException(exce.getMessage());
		}
		finally
		{
			this.closeDB(null ,v_Statement ,v_Conn);
		}
	}
	
	
	
	/**
     * 占位符SQL的执行。-- 无填充值的（内部不再关闭数据库连接）
     * 
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public boolean execute(Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.execute(this.content.getSQL() ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
	
	
	
	/**
     * 占位符SQL的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按集合 Map<String ,Object> 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Values           占位符SQL的填充集合。
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public boolean execute(Map<String ,?> i_Values ,Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.execute(this.content.getSQL(i_Values) ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Values);
            }
        }
    }
	
	
	
	/**
     * 占位符SQL的执行。（内部不再关闭数据库连接）
     * 
     * 1. 按对象 i_Obj 填充占位符SQL，生成可执行的SQL语句；
     * 
     * @param i_Obj              占位符SQL的填充对象。
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public boolean execute(Object i_Obj ,Connection i_Conn)
    {
        checkContent();
        
        boolean v_IsError = false;

        try
        {
            return this.execute(this.content.getSQL(i_Obj) ,i_Conn);
        }
        catch (NullPointerException exce)
        {
            v_IsError = true;
            throw exce;
        }
        catch (RuntimeException exce)
        {
            v_IsError = true;
            throw exce;
        }
        finally
        {
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_Obj);
            }
        }
    }
	
	
	
	/**
     * 常规SQL的执行。（内部不再关闭数据库连接）
     * 
     * @param i_SQL              常规SQL语句
     * @param i_Conn             数据库连接
     * @return                   是否执行成功。
     */
    public boolean execute(String i_SQL ,Connection i_Conn)
    {
        Statement v_Statement = null;
        long      v_BeginTime = this.request().getTime();
        
        try
        {
            if ( Help.isNull(i_SQL) )
            {
                throw new NullPointerException("SQL or SQL-Params is null of XSQL.");
            }
            
            if ( null == i_Conn )
            {
                throw new NullPointerException("Connection is null of XSQL.");
            }
            
            v_Statement = i_Conn.createStatement();
            
            v_Statement.execute(i_SQL);
            $SQLBusway.put(new XSQLLog(i_SQL));
            Date v_EndTime = Date.getNowTime();
            this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
            
            return true;
        }
        catch (Exception exce)
        {
            erroring(i_SQL ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(null ,v_Statement ,null);
        }
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 简单型
     * 
     * 1. 调用对象无输入参数
     * 2. 调用对象无输出参数(函数自身返回除外)
     * 3. 存储过程返回Boolean类型，表示是否执行成功
     * 4. 函数返回返回值类型，但返回值类型必须是Integer、Varchar中的一种，其它类型出错。或请您使用高级的call方法。
     * 
     * @param i_SQLCallName      存储过程或函数的名称
     */
    public Object call(String i_SQLCallName)
    {
        DataSourceGroup   v_DSG       = null;
        Connection        v_Conn      = null;
        CallableStatement v_Statement = null;
        long              v_BeginTime = this.request().getTime();
        boolean           v_IsError   = false;
        
        try
        {
            if ( !$Type_Procedure.equals(this.type) && !$Type_Function.equals(this.type) )
            {
                throw new IllegalArgumentException("Type is not 'P' or 'F' of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(i_SQLCallName) )
            {
                throw new NullPointerException("SQLCallName is null of XSQL.");
            }
            
            v_Conn = this.getConnection(v_DSG);
            if ( $Type_Procedure.equals(this.type) )
            {
                v_Statement = v_Conn.prepareCall("{call " + i_SQLCallName + "()}");
                
                v_Statement.execute();
                $SQLBusway.put(new XSQLLog("{call " + i_SQLCallName + "()}"));
                Date v_EndTime = Date.getNowTime();
                this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                
                return true;
            }
            else
            {
                int v_RetType = -1;
                v_Statement = v_Conn.prepareCall("{? = call " + i_SQLCallName + "()}");
                
                try
                {
                    v_Statement.registerOutParameter(1 ,java.sql.Types.INTEGER);
                    v_Statement.execute();
                    v_RetType = 1;
                }
                catch (Exception exce)
                {
                    v_Statement.registerOutParameter(1 ,java.sql.Types.VARCHAR);
                    v_Statement.execute();
                    v_RetType = 2;
                }
                
                $SQLBusway.put(new XSQLLog("{call " + i_SQLCallName + "()}"));
                Date v_EndTime = Date.getNowTime();
                this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                
                if ( v_RetType == 1 )
                {
                    return v_Statement.getInt(1);
                }
                else if ( v_RetType == 2 )
                {
                    return v_Statement.getString(1);
                }
                else
                {
                    return null;
                }
            }
        }
        catch (Exception exce)
        {
            v_IsError = true;
            erroring("{call " + i_SQLCallName + "()}" ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            this.closeDB(null ,v_Statement ,v_Conn);
            
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes();
            }
        }
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 无输入参数型
     * 
     * 1. 支持游标输出
     * 2. 支持多个输出参数
     * 
     * @return  1. 当调用对象为存储过程时，当无输出参数时，返回是否执行成功(true、false)
     *          2. 当调用对象为存储过程时，当有一个输出类型时，返回输出值。
     *                                如果输出类型为游标，则可以按XJava的概念直接转为实例对象。
     *          3. 当调用对象为存储过程时，当有多个输出类型时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                                如果输出类型为游标，则每个游标都按XJava的概念直接转为实例对象。
     *          4. 当调用函数时，当无输出参数时，返回函数的返回值。
     *          5. 当调用函数时，除自身返回值外还有一个或多个输出参数时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                        List集合的首个元素为函数自身的返回值。
     */
    public Object call()
    {
        return this.call((Object)null);
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 输入参数为XJava对象
     * 
     * 1. 支持游标输出
     * 2. 支持多个输出参数
     * 
     * @param i_SQLCallName      存储过程或函数的名称
     * @param i_ParamObj         入参参数对象
     * 
     * @return  1. 当调用对象为存储过程时，当无输出参数时，返回是否执行成功(true、false)
     *          2. 当调用对象为存储过程时，当有一个输出类型时，返回输出值。
     *                                如果输出类型为游标，则可以按XJava的概念直接转为实例对象。
     *          3. 当调用对象为存储过程时，当有多个输出类型时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                                如果输出类型为游标，则每个游标都按XJava的概念直接转为实例对象。
     *          4. 当调用函数时，当无输出参数时，返回函数的返回值。
     *          5. 当调用函数时，除自身返回值外还有一个或多个输出参数时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                        List集合的首个元素为函数自身的返回值。  
     */
    @SuppressWarnings("resource")
    public Object call(Object i_ParamObj)
    {
        DataSourceGroup   v_DSG            = null;
        Connection        v_Conn           = null;
        CallableStatement v_Statement      = null;
        ResultSet         v_Resultset      = null;
        List<Integer>     v_OutParamIndexs = new ArrayList<Integer>();
        long              v_BeginTime      = this.request().getTime();
        boolean           v_IsError        = false;
        StringBuilder     v_Buffer         = new StringBuilder();
        
        try
        {
            if ( !$Type_Procedure.equals(this.type) && !$Type_Function.equals(this.type) )
            {
                throw new IllegalArgumentException("Type is not 'P' or 'F' of XSQL.");
            }
            
            if ( this.callParams == null )
            {
                this.callParams = new ArrayList<XSQLCallParam>();
            }
            
            if ( this.content == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            if ( this.result == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(this.getContent().getSQL()) )
            {
                throw new NullPointerException("SQLCallName is null of XSQL.");
            }
            
            // 存储过程或函数有入参时，i_ParamObj不能为空。否则，可则为空
            if ( this.callParamInCount >= 1 && i_ParamObj == null )
            {
                throw new NullPointerException("Call [" + this.getContent().getSQL() + "] parameters values is null.");
            }
            
            // 数据函数调用时
            if ( $Type_Function.equals(this.type) )
            {
                // 必须有返回值
                if ( this.callParamOutCount <= 0 )
                {
                    throw new NullPointerException("Call [" + this.getContent().getSQL() + "] Return parameter is invalid.");
                }
                
                // 返回值的配置必须为第一个<addParam>标记
                if ( !this.callParams.get(0).isOutType() )
                {
                    throw new NullPointerException("Call [" + this.getContent().getSQL() + "] Return parameter is not first '<addParam>'.");
                }
            }
            
            
            // 生成 {call xxx(? ,? ,? ...)}
            int v_StartIndex = 0;
            if ( $Type_Procedure.equals(this.type) )
            {
                v_Buffer.append("{call ");
                v_StartIndex = 0;
            }
            else
            {
                v_Buffer.append("{? = call ");
                v_StartIndex = 1;
            }
            v_Buffer.append(this.getContent().getSQL().trim()).append("(");
            for (int i=v_StartIndex; i<this.callParams.size(); i++)
            {
                if ( i > v_StartIndex )
                {
                    v_Buffer.append(" ,");
                }
                v_Buffer.append("?");
            }
            v_Buffer.append(")}");
            
            
            v_Conn      = this.getConnection(v_DSG);
            v_Statement = v_Conn.prepareCall(v_Buffer.toString());
            
            
            for (int v_ParamIndex=1; v_ParamIndex<=this.callParams.size(); v_ParamIndex++)
            {
                XSQLCallParam v_CallParam = this.callParams.get(v_ParamIndex - 1);
                
                // 输入类型的参数
                if ( v_CallParam.isInType() )
                {
                    Object v_ParamValue = MethodReflect.getGetMethod(i_ParamObj.getClass() ,v_CallParam.getName() ,true).invoke(i_ParamObj);
                    
                    if ( v_ParamValue == null )
                    {
                        throw new NullPointerException("Call " + this.getContent().getSQL() + "(" + v_CallParam.getName() + ") parameter is null.");
                    }
                    
                    v_CallParam.setValue(v_Statement ,v_ParamIndex ,v_ParamValue);
                }
                
                // 输出类型的参数
                if ( v_CallParam.isOutType() )
                {
                    v_Statement.registerOutParameter(v_ParamIndex ,v_CallParam.getJdbcTypeID());
                    v_OutParamIndexs.add(Integer.valueOf(v_ParamIndex));
                }
            }
            
            if ( v_OutParamIndexs.size() <= 0 )
            {
                v_Statement.execute();
                $SQLBusway.put(new XSQLLog(v_Buffer.toString()));
                Date v_EndTime = Date.getNowTime();
                this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                return true;
            }
            else
            {
                v_Statement.execute();
                $SQLBusway.put(new XSQLLog(v_Buffer.toString()));
                
                if ( v_OutParamIndexs.size() == 1 )
                {
                    XSQLCallParam v_CallParam = this.callParams.get(v_OutParamIndexs.get(0).intValue() - 1);
                    Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(0).intValue());
                    
                    if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                    {
                        v_Resultset = (ResultSet)v_OutValue;
                        Object v_Ret = this.result.getDatas(v_Resultset);
                        Date v_EndTime = Date.getNowTime();
                        this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                        return v_Ret;
                    }
                    else
                    {
                        Date v_EndTime = Date.getNowTime();
                        this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                        return v_OutValue;
                    }
                }
                else
                {
                    List<Object> v_Rets = new ArrayList<Object>();
                    
                    for (int i=0; i<v_OutParamIndexs.size(); i++)
                    {
                        XSQLCallParam v_CallParam = this.callParams.get(v_OutParamIndexs.get(i).intValue() - 1);
                        Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(i).intValue());
                        
                        if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                        {
                            v_Resultset = (ResultSet)v_OutValue;
                            v_Rets.add(this.result.getDatas(v_Resultset));
                        }
                        else
                        {
                            v_Rets.add(v_OutValue);
                        }
                    }
                    
                    Date v_EndTime = Date.getNowTime();
                    this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                    return v_Rets;
                }
            }
        }
        catch (Exception exce)
        {
            v_IsError = true;
            erroring(v_Buffer.toString() ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            v_OutParamIndexs.clear();
            v_OutParamIndexs = null;
            this.closeDB(v_Resultset ,v_Statement ,v_Conn);
            
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_ParamObj);
            }
        }
    }
    
    
    
    /**
     * 调用存储过程或函数 -- 输入参数为Map对象
     * 
     * Map.key    应于XSQLCallParam.name相互匹配。
     * Map.value  为输入参数的值
     * 
     * 1. 支持游标输出
     * 2. 支持多个输出参数
     * 
     * @param i_SQLCallName      存储过程或函数的名称
     * @param i_ParamObj         入参参数值集合
     * 
     * @return  1. 当调用对象为存储过程时，当无输出参数时，返回是否执行成功(true、false)
     *          2. 当调用对象为存储过程时，当有一个输出类型时，返回输出值。
     *                                如果输出类型为游标，则可以按XJava的概念直接转为实例对象。
     *          3. 当调用对象为存储过程时，当有多个输出类型时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                                如果输出类型为游标，则每个游标都按XJava的概念直接转为实例对象。
     *          4. 当调用函数时，当无输出参数时，返回函数的返回值。
     *          5. 当调用函数时，除自身返回值外还有一个或多个输出参数时，将每个输出值依次添加到List集合中，再将List集合返回。
     *                        List集合的首个元素为函数自身的返回值。
     */
    @SuppressWarnings("resource")
    public Object call(Map<String ,?> i_ParamValues)
    {
        DataSourceGroup   v_DSG            = null;
        Connection        v_Conn           = null;
        CallableStatement v_Statement      = null;
        ResultSet         v_Resultset      = null;
        List<Integer>     v_OutParamIndexs = new ArrayList<Integer>();
        long              v_BeginTime      = this.request().getTime();
        boolean           v_IsError        = false;
        StringBuilder     v_Buffer         = new StringBuilder();
        
        try
        {
            if ( !$Type_Procedure.equals(this.type) && !$Type_Function.equals(this.type) )
            {
                throw new IllegalArgumentException("Type is not 'P' or 'F' of XSQL.");
            }
            
            if ( this.content == null )
            {
                throw new NullPointerException("Content is null of XSQL.");
            }
            
            if ( this.result == null )
            {
                throw new NullPointerException("Result is null of XSQL.");
            }
            
            v_DSG = this.getDataSourceGroup();
            if ( !v_DSG.isValid() )
            {
                throw new RuntimeException("DataSourceGroup is not valid.");
            }
            
            if ( Help.isNull(this.getContent().getSQL()) )
            {
                throw new NullPointerException("SQLCallName is null of XSQL.");
            }
            
            // 存储过程或函数有入参时，i_ParamObj不能为空。否则，可则为空
            if ( this.callParamInCount >= 1 && Help.isNull(i_ParamValues) )
            {
                throw new NullPointerException("Call [" + this.getContent().getSQL() + "] parameters values is null.");
            }
            
            // 数据函数调用时
            if ( $Type_Function.equals(this.type) )
            {
                // 必须有返回值
                if ( this.callParamOutCount <= 0 )
                {
                    throw new NullPointerException("Call [" + this.getContent().getSQL() + "] Return parameter is invalid.");
                }
                
                // 返回值的配置必须为第一个<addParam>标记
                if ( !this.callParams.get(0).isOutType() )
                {
                    throw new NullPointerException("Call [" + this.getContent().getSQL() + "] Return parameter is not first '<addParam>'.");
                }
            }
            
            
            // 生成 {call xxx(? ,? ,? ...)}
            int v_StartIndex = 0;
            if ( $Type_Procedure.equals(this.type) )
            {
                v_Buffer.append("{call ");
                v_StartIndex = 0;
            }
            else
            {
                v_Buffer.append("{? = call ");
                v_StartIndex = 1;
            }
            v_Buffer.append(this.getContent().getSQL().trim()).append("(");
            for (int i=v_StartIndex; i<this.callParams.size(); i++)
            {
                if ( i > v_StartIndex )
                {
                    v_Buffer.append(" ,");
                }
                v_Buffer.append("?");
            }
            v_Buffer.append(")}");
            
            
            v_Conn      = this.getConnection(v_DSG);
            v_Statement = v_Conn.prepareCall(v_Buffer.toString());
            
            
            for (int v_ParamIndex=1; v_ParamIndex<=this.callParams.size(); v_ParamIndex++)
            {
                XSQLCallParam v_CallParam = this.callParams.get(v_ParamIndex - 1);
                
                // 输入类型的参数
                if ( v_CallParam.isInType() )
                {
                    if ( !i_ParamValues.containsKey(v_CallParam.getName().trim()) )
                    {
                        throw new NullPointerException("Call " + this.getContent().getSQL() + "(" + v_CallParam.getName() + ") parameter is not exist.");
                    }
                    
                    Object v_ParamValue = i_ParamValues.get(v_CallParam.getName().trim());
                    
                    if ( v_ParamValue == null )
                    {
                        throw new NullPointerException("Call " + this.getContent().getSQL() + "(" + v_CallParam.getName() + ") parameter is null.");
                    }
                    
                    v_CallParam.setValue(v_Statement ,v_ParamIndex ,v_ParamValue);
                }
                
                // 输出类型的参数
                if ( v_CallParam.isOutType() )
                {
                    v_Statement.registerOutParameter(v_ParamIndex ,v_CallParam.getJdbcTypeID());
                    v_OutParamIndexs.add(Integer.valueOf(v_ParamIndex));
                }
            }
            
            if ( v_OutParamIndexs.size() <= 0 )
            {
                v_Statement.execute();
                $SQLBusway.put(new XSQLLog(v_Buffer.toString()));
                Date v_EndTime = Date.getNowTime();
                this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                return true;
            }
            else
            {
                v_Statement.execute();
                $SQLBusway.put(new XSQLLog(v_Buffer.toString()));
                
                if ( v_OutParamIndexs.size() == 1 )
                {
                    XSQLCallParam v_CallParam = this.callParams.get(v_OutParamIndexs.get(0).intValue() - 1);
                    Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(0).intValue());
                    
                    if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                    {
                        v_Resultset = (ResultSet)v_OutValue;
                        Object v_Ret = this.result.getDatas(v_Resultset);
                        Date v_EndTime = Date.getNowTime();
                        this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                        return v_Ret;
                    }
                    else
                    {
                        Date v_EndTime = Date.getNowTime();
                        this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                        return v_OutValue;
                    }
                }
                else
                {
                    List<Object> v_Rets = new ArrayList<Object>();
                    
                    for (int i=0; i<v_OutParamIndexs.size(); i++)
                    {
                        XSQLCallParam v_CallParam = this.callParams.get(v_OutParamIndexs.get(i).intValue() - 1);
                        Object        v_OutValue  = v_CallParam.getValue(v_Statement ,v_OutParamIndexs.get(i).intValue());
                        
                        if ( oracle.jdbc.OracleTypes.CURSOR == v_CallParam.getJdbcTypeID() && v_OutValue != null )
                        {
                            v_Resultset = (ResultSet)v_OutValue;
                            v_Rets.add(this.result.getDatas(v_Resultset));
                        }
                        else
                        {
                            v_Rets.add(v_OutValue);
                        }
                    }
                    
                    Date v_EndTime = Date.getNowTime();
                    this.success(v_EndTime ,v_EndTime.getTime() - v_BeginTime);
                    return v_Rets;
                }
            }
        }
        catch (Exception exce)
        {
            v_IsError = true;
            erroring(v_Buffer.toString() ,exce ,this);
            throw new RuntimeException(exce.getMessage());
        }
        finally
        {
            v_OutParamIndexs.clear();
            v_OutParamIndexs = null;
            this.closeDB(v_Resultset ,v_Statement ,v_Conn);
            
            if ( this.isTriggers(v_IsError) )
            {
                this.trigger.executes(i_ParamValues);
            }
        }
    }
	
	
	
	/**
	 * 关闭外部所有与数据有关的连接
	 * 
	 * @param i_Resultset
	 * @param i_Statement
	 * @param i_Conn
	 */
	public void closeDB(ResultSet i_Resultset ,Statement i_Statement ,Connection i_Conn)
	{
    	try
    	{
	    	if ( i_Resultset != null )
		    {
	    		i_Resultset.close();
	    		i_Resultset = null;
		    }
    	}
    	catch (Exception exce)
    	{
    		throw new RuntimeException(exce.getMessage());
    	}
    	finally
    	{
    		i_Resultset = null;
    	}
    	
    	
    	try
    	{
	    	if ( i_Statement != null )
		    {
		    	i_Statement.close();
		    	i_Statement = null;
		    }
    	}
    	catch (Exception exce)
    	{
    		throw new RuntimeException(exce.getMessage());
    	}
    	finally
    	{
    		i_Statement = null;
    	}
    	
    	
    	try
    	{
	    	if ( i_Conn != null )
		    {
	    		i_Conn.close();
	    		i_Conn = null;
		    }
    	}
    	catch (Exception exce)
    	{
    		throw new RuntimeException(exce.getMessage());
    	}
    	finally
    	{
    		i_Conn = null;
    	}
	}
	
	
	
	/**
	 * 注册BLob文件事件
	 * 
	 * @param e
	 */
	public void addBLobListener(BLobListener e)
	{
		if ( this.blobListeners == null )
		{
			this.blobListeners = new HashSet<BLobListener>();
		}
		
		this.blobListeners.add(e);
	}
	
	
	
	/**
	 * 移除BLob文件事件
	 * 
	 * @param e
	 */
	public void removeBLobListener(BLobListener e)
	{
		if ( this.blobListeners == null )
		{
			return;
		}
		
		this.blobListeners.remove(e);
	}
	
	
	
	/**
	 * 触发BLob传送文件之前的事件
	 * 
	 * @param i_Event
	 * @return   返回值表示是否继续
	 */
	protected boolean fireBLobBeforeListener(BLobEvent i_Event)
	{
		if ( this.blobListeners == null )
		{
			return true;
		}
		
		return notifyBLobBeforeListeners(i_Event);
	}
	
	
	
	/**
	 * 触发BLob传送文件事件
	 * 
	 * @param i_Event
	 * @return   返回值表示是否继续
	 */
	protected boolean fireBLobingListener(BLobEvent i_Event)
	{
		if ( this.blobListeners == null )
		{
			return true;
		}
		
		return notifyBLobingListeners(i_Event);
	}
	
	
	
	/**
	 * 触发BLob传送文件完成之后的事件
	 * 
	 * @param i_Event
	 */
	protected void fireBLobAfterListener(BLobEvent i_Event)
	{
		if ( this.blobListeners == null )
		{
			return;
		}
		
		notifyBLobAfterListeners(i_Event);
	}

	
	
	/**
	 * 通知所有注册BLob传送文件之前的事件监听的对象
	 * 
	 * @param i_Event
	 * @return   返回值表示是否继续
	 */
	private boolean notifyBLobBeforeListeners(BLobEvent i_Event)
	{
		Iterator<BLobListener> v_Iter       = this.blobListeners.iterator();
		boolean                v_IsContinue = true;

		while ( v_IsContinue && v_Iter.hasNext() ) 
		{
			v_IsContinue = v_Iter.next().blobBefore(i_Event);
		}
		
		return v_IsContinue;
	}
	
	
	
	/**
	 * 通知所有注册BLob传送文件事件监听的对象
	 * 
	 * @param i_Event
	 */
	private boolean notifyBLobingListeners(BLobEvent i_Event)
	{
		Iterator<BLobListener> v_Iter       = this.blobListeners.iterator();
		boolean                v_IsContinue = true; 

		while ( v_IsContinue && v_Iter.hasNext() ) 
		{
			v_IsContinue = v_Iter.next().blobProcess(i_Event);
		}
		
		return v_IsContinue;
	}

	
	
	/**
	 * 通知所有注册FTP传送完成之后的事件监听的对象
	 * 
	 * @param i_Event
	 */
	private void notifyBLobAfterListeners(BLobEvent i_Event)
	{
		Iterator<BLobListener> v_Iter = this.blobListeners.iterator();

		while ( v_Iter.hasNext() ) 
		{
			v_Iter.next().blobAfter(i_Event);
		}
	}
	
	
	
    /**
     * 添加当调用存储过程或函数时的参数对象
     * 
     * @param i_CallParam
     */
    public synchronized void setAddParam(XSQLCallParam i_CallParam)
    {
        if ( this.callParams == null )
        {
            this.callParams = new ArrayList<XSQLCallParam>();
        }
        
        this.callParams.add(i_CallParam);
        
        if ( i_CallParam.isInType() )
        {
            this.callParamInCount++;
        }
        
        if ( i_CallParam.isOutType() )
        {
            this.callParamOutCount++;
        }
    }
	
	
	
	public XSQLResult getResult() 
	{
		return result;
	}
	
	
	
	public void setResult(XSQLResult i_Result) 
	{
		this.result = i_Result;
	}
	
	
	
	public DBSQL getContent() 
	{
		return this.content;
	}
	
	
	
	public void setContent(String i_SQLText) 
	{
		this.content.setSqlText(i_SQLText);
		this.isAllowExecutesSplit(i_SQLText);
	}
	
	
	
	public DBSQL getContentDB() 
    {
        return this.content;
    }
	
	
	
	public void setContentDB(DBSQL i_DBSQL) 
    {
        this.content = i_DBSQL;
        
        if ( this.content != null )
        {
            this.isAllowExecutesSplit(this.content.getSqlText());
        }
    }
	
	
	
	/**
	 * 默认情况下，通过XSQL模板自动判定$Executes_Split分割符是否生效的。
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2018-06-24
	 * @version     v1.0
	 *
	 * @param i_SQLText
	 */
	private void isAllowExecutesSplit(String i_SQLText)
	{
	    if ( !Help.isNull(i_SQLText) )
        {
            if ( i_SQLText.split($Executes_Split).length >= 2 )
            {
                this.allowExecutesSplit = true;
            }
            else
            {
                this.allowExecutesSplit = false;
            }
        }
        else
        {
            this.allowExecutesSplit = false;
        }
	}
	
	
	
	/**
     * 是否允许或支持execute()方法中执行多条SQL语句，即$Executes_Split = ";/"分割符是否生效。
     * 默认情况下，通过XSQL模板自动判定$Executes_Split分割符是否生效的。
     * 
     * 但特殊情况下，允许外界通过本属性启用或关闭execute()方法中执行多条SQL语句的功能。
     * 如，SQL语句中就包含;/文本字符的情况，不是分割符是意思。
     */
    public boolean isAllowExecutesSplit()
    {
        return allowExecutesSplit;
    }

    

    /**
     * 是否允许或支持execute()方法中执行多条SQL语句，即$Executes_Split = ";/"分割符是否生效。
     * 默认情况下，通过XSQL模板自动判定$Executes_Split分割符是否生效的。
     * 
     * 但特殊情况下，允许外界通过本属性启用或关闭execute()方法中执行多条SQL语句的功能。
     * 如，SQL语句中就包含;/文本字符的情况，不是分割符是意思。
     */
    public void setAllowExecutesSplit(boolean allowExecutesSplit)
    {
        this.allowExecutesSplit = allowExecutesSplit;
    }
    
    

    /**
	 * 多个数据库连接批量提交
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2018-01-21
	 * @version     v1.0
	 *
	 * @param i_Strategy  策略类型。
	 *                       策略1：当出现异常时，其后的连接均继续：提交。
	 *                       策略2：当出现异常时，其后的连接均执行：回滚。
	 * @param i_Conns     数据库连接的集合
	 * @return
	 */
	public static boolean commits(int i_Strategy ,List<Connection> i_Conns)
	{
	    boolean v_IsOK = true;
	    
	    if ( Help.isNull(i_Conns) )
	    {
	        return v_IsOK;
	    }

	    // 策略1：当出现异常时，其后的连接均继续：提交。
        if ( i_Strategy == 1 )
        {
            for (Connection v_Conn : i_Conns)
            {
                try
                {
                    v_Conn.commit();
                }
                catch (Exception exce)
                {
                    v_IsOK = false;
                    exce.printStackTrace();
                }
            }
        }
	    // 策略2：当出现异常时，其后的连接均执行：回滚。
        else if ( i_Strategy == 2 )
	    {
    	    for (Connection v_Conn : i_Conns)
    	    {
    	        try
    	        {
    	            if ( v_IsOK )
    	            {
    	                v_Conn.commit();
    	            }
    	            else
    	            {
    	                v_Conn.rollback();
    	            }
    	        }
    	        catch (Exception exce)
    	        {
    	            v_IsOK = false;
    	            exce.printStackTrace();
    	        }
    	    }
	    }
	    
	    return v_IsOK;
	}
	
	
	
	/**
     * 多个数据库连接批量回滚
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-21
     * @version     v1.0
     *
     * @param i_Conns     数据库连接的集合
     * @return
     */
    public static boolean rollbacks(List<Connection> i_Conns)
    {
        boolean v_IsOK = true;

        if ( !Help.isNull(i_Conns) )
        {
            for (Connection v_Conn : i_Conns)
            {
                try
                {
                    v_Conn.rollback();
                }
                catch (Exception exce)
                {
                    // 异常用不抛出
                    v_IsOK = false;
                }
            }
        }
        
        return v_IsOK;
    }
    
    
    
    /**
     * 多个数据库连接批量设置是否自动提交
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-21
     * @version     v1.0
     *
     * @param i_Conns       数据库连接的集合
     * @param i_AutoCommit  是否自动提交
     * @return
     */
    public static boolean setAutoCommits(List<Connection> i_Conns ,boolean i_AutoCommit)
    {
        boolean v_IsOK = true;
        
        if ( !Help.isNull(i_Conns) )
        {
            for (Connection v_Conn : i_Conns)
            {
                try
                {
                    v_Conn.setAutoCommit(i_AutoCommit);
                }
                catch (Exception exce)
                {
                    // 异常用不抛出
                    v_IsOK = false;
                }
            }
        }
        
        return v_IsOK;
    }
    
    
    
    /**
     * 多个数据库连接批量关闭
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-21
     * @version     v1.0
     *
     * @param i_Conns     数据库连接的集合
     * @return
     */
    public static boolean closeDB(List<Connection> i_Conns)
    {
        boolean v_IsOK = true;

        if ( !Help.isNull(i_Conns) )
        {
            for (Connection v_Conn : i_Conns)
            {
                try
                {
                    v_Conn.close();
                }
                catch (Exception exce)
                {
                    // 异常用不抛出
                    v_IsOK = false;
                }
            }
        }
        
        return v_IsOK;
    }
	
	
	
	/**
     * 获取数据库连接。
     * 
     * @return
     */
    public Connection getConnection(DataSourceGroup i_DataSourceGroup)
    {
        return i_DataSourceGroup.getConnection();
    }

	
    
    /**
     * 获取：数据库连接池组
     * 
     * 当"域"存在时，使用域的数据库连接池组。其它情况，使用默认的数据库连接池组。
     */
    public DataSourceGroup getDataSourceGroup()
    {
        if ( this.domain != null )
        {
            try
            {
                DataSourceGroup v_DomainDBG = this.domain.getDataSourceGroup();
                
                if ( v_DomainDBG != null )
                {
                    return v_DomainDBG;
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return dataSourceGroups.next();
    }

    
    
    /**
     * 设置：将数据库连接池组将添加到的负载数据库集合中
     * 
     * @param i_DataSourceGroup 
     */
    public void setDataSourceGroup(DataSourceGroup i_DataSourceGroup)
    {
        if ( i_DataSourceGroup == null ) return;
        this.dataSourceGroups.add(i_DataSourceGroup);
    }
    
    
    
    /**
     * 获取：创建对象的名称。如表名称。
     * 
     * 此属性为动作方法，即this.setCreate(...)时，将尝试创建对象(当对象不存在时)。
     * 也因为是动作方法，所以在设置本属性前dataSourceGroup、content它两属性应当已设置OK。
     */
    public String getCreateObjectName()
    {
        return create;
    }
    
    
    
    /**
     * 创建对象。如创建表。
     * 
     * 此属性为动作方法，即this.setCreate(...)时，将尝试创建对象(当对象不存在时)。
     * 也因为是动作方法，所以在设置本属性前dataSourceGroup、content它两属性应当已设置OK。
     * 
     * 实现服务启动时检查并创建数据库对象(如数据库表)，已存在不创建。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-12
     * @version     v1.0
     *
     * @param i_CreateObjectName
     */
    public void setCreate(String i_CreateObjectName)
    {
        this.create = i_CreateObjectName.trim();
        this.type   = $Type_Create;
        
        
        createObject();
    }
    
    
    
    /**
     * 创建对象。如创建表。
     * 
     * 此属性为动作方法，即this.setCreate(...)时，将尝试创建对象(当对象不存在时)。
     * 也因为是动作方法，所以在设置本属性前dataSourceGroup、content它两属性应当已设置OK。
     * 
     * 实现服务启动时检查并创建数据库对象(如数据库表)，已存在不创建。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-01-12
     * @version     v1.0
     */
    public synchronized boolean createObject()
    {
        if ( null == this.getDataSourceGroup() )
        {
            throw new NullPointerException("DataSourceGroup is null.");
        }
        else if ( null == this.content )
        {
            throw new NullPointerException("SQL content(DBSQL) is null.");
        }
        else if ( Help.isNull(this.create) )
        {
            throw new NullPointerException("CreateObjectName is null.");
        }
        else if ( Help.isNull(this.content.getSqlText()) )
        {
            return true;
        }
        else if ( !this.getContent().getSqlText().toUpperCase().contains(this.create.toUpperCase()) )
        {
            // 简单的检查创建的对象名称，是否在执行SQL语句中存在
            throw new RuntimeException("CreateObjectName[" + this.create + "] is invalid.");
        }
        
        try
        {
            XSQLDBMetadata v_XSQLDBMetadata = new XSQLDBMetadata();
            boolean        v_IsExists       = v_XSQLDBMetadata.isExists(this);
            
            if ( !v_IsExists )
            {
                boolean v_Ret = this.execute();
                
                if ( v_Ret )
                {
                    System.out.println("Create object[" + this.create + "] OK. " + Help.NVL(this.comment));
                }
                else
                {
                    System.err.println("Create object[" + this.create + "] Error. " + Help.NVL(this.comment));
                }
                
                return v_Ret;
            }
        }
        catch (Exception exce)
        {
            System.err.println("Create object[" + this.create + "] Error. " + Help.NVL(this.comment));
            exce.printStackTrace();
        }
        
        return false;
    } 
    

    
    /**
     * 获取：大数据字段类型(如,CLob)的字段名称，多个字段名称间用逗号，分隔。只用于Insert、Update语句
     */
    public String getLobName()
    {
        return lobName;
    }
    

    
    /**
     * 设置：大数据字段类型(如,CLob)的字段名称，多个字段名称间用逗号，分隔。只用于Insert、Update语句
     * 
     * @param lobName 
     */
    public void setLobName(String lobName)
    {
        this.lobName = lobName;
    }
    

    
    /**
     * 获取：写入大数据字段类型(如,CLob)的所在行的查询条件SQL片段(不包含WHERE关键字)。只用于Insert、Update语句。
     */
    public String getLobWheres()
    {
        return lobWheres;
    }


    
    /**
     * 设置：写入大数据字段类型(如,CLob)的所在行的查询条件SQL片段(不包含WHERE关键字)。只用于Insert、Update语句。
     * 
     * @param lobWheres 
     */
    public void setLobWheres(String lobWheres)
    {
        this.lobWheres = lobWheres;
    }
    


    /**
     * 获取：数据库连接的域
     * 
     * 它可与 this.dataSourceGroup 同时存在值，但 this.domain 的优先级高。
     * 当"域"存在时，使用域的数据库连接池组。其它情况，使用默认的数据库连接池组。
     */
    public XSQLDomain getDomain()
    {
        return domain;
    }

    
    
    /**
     * 设置：数据库连接的域
     * 
     * 它可与 this.dataSourceGroup 同时存在值，但 this.domain 的优先级高。
     * 当"域"存在时，使用域的数据库连接池组。其它情况，使用默认的数据库连接池组。
     * 
     * @param domain 
     */
    public void setDomain(XSQLDomain domain)
    {
        this.domain = domain;
    }
    
    
    
    /**
     * 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的。
     * 
     * @param i_XJavaID
     */
    public void setXJavaID(String i_XJavaID)
    {
        this.xjavaID = i_XJavaID;
    }
    
    
    
    /**
     * 获取XJava池中对象的ID标识。
     * 
     * @return
     */
    public String getXJavaID()
    {
        return this.xjavaID;
    }
    


    /**
     * 获取：XSQL的触发器
     */
    public XSQLTrigger getTrigger()
    {
        return trigger;
    }

    
    
    /**
     * 设置：XSQL的触发器
     * 
     * @param trigger 
     */
    public void setTrigger(XSQLTrigger trigger)
    {
        this.trigger = trigger;
    }



    public boolean isBlobSafe() 
	{
		return blobSafe;
	}
	
	
	
	public void setBlobSafe(boolean blobSafe) 
	{
		this.blobSafe = blobSafe;
	}
	
	
	
	/** 
     * SQL类型。
     * 
     * N: 增、删、改、查的普通SQL语句  (默认值)
     * P: 存储过程
     * F: 函数
     * C: DML创建表，创建对象等
     */
    public String getType()
    {
        return Help.NVL(this.type ,$Type_NormalSQL);
    }


    
    /** 
     * SQL类型。
     * 
     * N: 增、删、改、查的普通SQL语句  (默认值)
     * P: 存储过程
     * F: 函数
     * C: DML创建表，创建对象等
     */
    public void setType(String i_Type)
    {
        if ( Help.isNull(i_Type) )
        {
            throw new NullPointerException("Type is null.");
        }
        
        if ( !$Type_NormalSQL.equals(i_Type)
          && !$Type_Procedure.equals(i_Type)
          && !$Type_Function .equals(i_Type)
          && !$Type_Create   .equals(i_Type) )
        {
            throw new IllegalArgumentException("Type is not 'N' or 'P' or 'F' or 'C' of XSQL.");
        }
        
        this.type = i_Type;
    }


    
    /**
     * 批量执行 Insert、Update、Delete 时，达到提交的提交点
     * 
     * 当>=1时，才有效，即分次提交
     * 当<=0时，在一个事务中执行所有的操作(默认状态)
     *
     * @return
     */
    public int getBatchCommit()
    {
        return batchCommit;
    }


    
    /**
     * 批量执行 Insert、Update、Delete 时，达到提交的提交点
     * 
     * 当>=1时，才有效，即分次提交
     * 当<=0时，在一个事务中执行所有的操作(默认状态)
     *
     * @param i_BatchCommit
     */
    public void setBatchCommit(int i_BatchCommit)
    {
        this.batchCommit = i_BatchCommit;
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
    


    public String getObjectID()
    {
        return this.uuid;
    }
    
    
    
    @Override
    public int hashCode()
    {
        return this.getObjectID().hashCode();
    }
    
    
    
    @Override
    public boolean equals(Object i_Other)
    {
        if ( i_Other == null )
        {
            return false;
        }
        else if ( this == i_Other )
        {
            return true;
        }
        else if ( i_Other instanceof XSQL )
        {
            return this.getObjectID().equals(((XSQL)i_Other).getObjectID());
        }
        else
        {
            return false;
        }
    }
    
    
    
    public int compareTo(XSQL i_XSQL)
    {
        if ( i_XSQL == null )
        {
            return 1;
        }
        else if ( this == i_XSQL )
        {
            return 0;
        }
        else
        {
            return this.getObjectID().compareTo(i_XSQL.getObjectID());
        }
    }



    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
	protected void finalize() throws Throwable 
	{
		this.dataSourceGroup = null;
		this.content         = null;
		this.result          = null;
        
        if ( this.callParams != null )
        {
            this.callParams.clear();
            this.callParams = null;
        }

		super.finalize();
	}
	*/
	
}
