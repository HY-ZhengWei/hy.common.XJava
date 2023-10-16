package org.hy.common.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Execute;
import org.hy.common.Help;
import org.hy.common.XJavaID;
import org.hy.common.db.DataSourceGroup;





/**
 * XSQL的触发器。
 * 
 *   类似于数据库的After触发器，但也有区别：
 *     1. 对Insert、Update、Delete语句有效外，还对 SELECT语句、存储过程、函数及其它DDL、DML、DCL、TCL均均生效，均可触发。
 *     2. 一个XSQL可以触发多个触发器，并且可以递归触发（即触发器的触发器）。
 *     3. XSQL触发源的执行入参，会传递给所有XSQL触发器，并作为其执行入参。
 *     4. 因为每个XSQL触发器均一个XSQL对象，每个XSQL对象可以有自己的数据库，所以触发源与触发器间、触发器与触发器间均可实现跨数据库的触发器功能。
 *     5. 触发器执行的时长，是不统计在触发源XSQL的执行时长中的。
 *     6. XSQL触发器分为“同步模式”和“异步模式”。
 *        6.1 在同步模式的情况下，所有XSQL触发器依次顺序执行，前一个执行完成，后下一个才执行。
 *        6.2 在异步模式的情况下，每个XSQL触发器均是一个独立的线程，所有XSQL触发器几乎是同时执行的。
 *     7. 触发器执行异常后，是不会回滚先前触发源XSQL的操作的（即每个触发器每个操作都是一个独立的事务）。
 *     8. XSQL触发源执行异常时，可以通过XSQLTrigger.errorCode属性控制XSQL触发器是否执行。
 *        默认XSQLTrigger.errorCode为True，即触发源异常时，XSQL触发器也被触发执行。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2017-01-06
 * @version     v1.0
 *              v2.0  2019-02-18  添加：可自行定制的XSQL异常处理机制
 *              v3.0  2019-08-13  添加：设定触发器中XSQL的执行方式（$Execute、$ExecuteUpdate）
 *              v3.1  2021-02-27  修改：syncMode默认值改为true。原因是：防止触发源是多线程执行的情况，并且触发源数据量大，
 *                                      当触发器再是异步时，容易引发内存溢出的问题。
 */
public class XSQLTrigger implements Comparable<XSQLTrigger> ,XJavaID
{
    /** 执行方式之一：XSQL.execute(...) */
    public static final           int $Execute       = 0;
    
    /** 执行方式之一：XSQL.executeUpdate(...) */
    public static final           int $ExecuteUpdate = 1;
    
    
    
    /**
     * 触发器执行操作的集合
     * 
     * 1. 在同步模式(单线程)下，执行按List顺序有序执行。零下标的元素第一个执行。
     * 2. 在异步模式(多线程)下，线程的发起按List有顺序发起。但不一定是有顺序的执行。
     */
    private List<XSQLTriggerInfo> xsqls;
    
    /** 同步模式。默认为：true，即同步模式 */
    private boolean               syncMode;
    
    /**
     * 异常模式。
     * 默认为：true，主XSQL异常时，触发器也被触发执行。
     * 当为： false时，主XSQL执行成功后，触发器才被触发执行。
     */
    private boolean               errorMode;
    
    /** 可自行定制的XSQL异常处理机制。当触发的XSQL未设置异常处理机制（XSQL.getError()==null）时，才生效 */
    private XSQLError             error;
    
    /** 是否初始化 createBackup() 添加的XSQL。只针对 createBackup() 功能的初始化 */
    private boolean               isInit;
    
    /** XJava池中对象的ID标识 */
    private String                xjavaID;
    
    /** 注释。可用于日志的输出等帮助性的信息 */
    private String                comment;
    
    
    
    public XSQLTrigger()
    {
        this.xsqls     = new ArrayList<XSQLTriggerInfo>();
        this.syncMode  = true;
        this.errorMode = true;
        this.error     = null;
        this.isInit    = false;
    }
    
    
    
    /**
     * 触发执行所有的操作
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     */
    public void executes()
    {
        if ( this.syncMode )
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                if ( v_XSQLTrigger.getExecuteType() == $ExecuteUpdate )
                {
                    v_XSQLTrigger.getXsql().executeUpdate();
                }
                else
                {
                    v_XSQLTrigger.getXsql().execute();
                }
            }
        }
        else
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                if ( v_XSQLTrigger.getExecuteType() == $ExecuteUpdate )
                {
                    (new Execute(v_XSQLTrigger.getXsql() ,"executeUpdate")).start();
                }
                else
                {
                    (new Execute(v_XSQLTrigger.getXsql() ,"execute")).start();
                }
            }
        }
    }
    
    
    
    /**
     * 触发执行所有的操作
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     *
     * @param i_Values  主XSQL的入参数
     */
    public void executes(Map<String ,?> i_Values)
    {
        if ( this.syncMode )
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                if ( v_XSQLTrigger.getExecuteType() == $ExecuteUpdate )
                {
                    v_XSQLTrigger.getXsql().executeUpdate(i_Values);
                }
                else
                {
                    v_XSQLTrigger.getXsql().execute(i_Values);
                }
            }
        }
        else
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                if ( v_XSQLTrigger.getExecuteType() == $ExecuteUpdate )
                {
                    (new Execute(v_XSQLTrigger.getXsql() ,"executeUpdate" ,i_Values)).start();
                }
                else
                {
                    (new Execute(v_XSQLTrigger.getXsql() ,"execute" ,i_Values)).start();
                }
            }
        }
    }
    
    
    
    /**
     * 触发执行所有的操作
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     *
     * @param i_Obj  主XSQL的入参数
     */
    public void executes(Object i_Obj)
    {
        if ( this.syncMode )
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                if ( v_XSQLTrigger.getExecuteType() == $ExecuteUpdate )
                {
                    v_XSQLTrigger.getXsql().executeUpdate(i_Obj);
                }
                else
                {
                    v_XSQLTrigger.getXsql().execute(i_Obj);
                }
            }
        }
        else
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                if ( v_XSQLTrigger.getExecuteType() == $ExecuteUpdate )
                {
                    (new Execute(v_XSQLTrigger.getXsql() ,"executeUpdate" ,i_Obj)).start();
                }
                else
                {
                    (new Execute(v_XSQLTrigger.getXsql() ,"execute" ,i_Obj)).start();
                }
            }
        }
    }
    
    
    
    /**
     * 触发执行所有的操作（针对批量执行）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     *
     * @param i_Obj  主XSQL的入参数
     */
    public void executeUpdates(List<?> i_ObjList)
    {
        if ( this.syncMode )
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                v_XSQLTrigger.getXsql().executeUpdates(i_ObjList);
            }
        }
        else
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                (new Execute(v_XSQLTrigger.getXsql() ,"executeUpdates" ,i_ObjList)).start();
            }
        }
    }
    
    
    
    /**
     * 触发执行所有的操作（针对批量执行）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     *
     * @param i_Obj  主XSQL的入参数
     */
    public void executeUpdatesPrepared(List<?> i_ObjList)
    {
        if ( this.syncMode )
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                v_XSQLTrigger.getXsql().executeUpdatesPrepared(i_ObjList);
            }
        }
        else
        {
            for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
            {
                (new Execute(v_XSQLTrigger.getXsql() ,"executeUpdatesPrepared" ,i_ObjList)).start();
            }
        }
    }
    
    
    
    /**
     * 创建一个备份(数据冗余)的触发器
     * 
     * 这里只需备份数据库的连接池组，其它属性信息均与主数据库一样（属性赋值在其后的操作中设置 XSQL.initTriggers() ）。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     *
     * @param i_DSG  备份数据库的连接池组对象
     */
    public void setCreateBackup(DataSourceGroup i_DSG)
    {
        XSQL v_Trigger = new XSQL();
        
        v_Trigger.setDataSourceGroup(i_DSG);
        v_Trigger.setError(this.error);
        
        this.xsqls.add(new XSQLTriggerInfo(v_Trigger ,$ExecuteUpdate));
        
        this.isInit = true;
    }
    
    
    
    /**
     * 创建一个DML触发器
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-08-13
     * @version     v1.0
     *
     * @param i_XSQL
     */
    public void setCreateUpdate(XSQL i_XSQL)
    {
        if ( i_XSQL.getError() == null )
        {
            i_XSQL.setError(this.error);
        }
        this.xsqls.add(new XSQLTriggerInfo(i_XSQL ,$ExecuteUpdate));
    }
    
    
    
    /**
     * 创建一个触发器
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-05
     * @version     v1.0
     *
     * @param i_XSQL
     */
    public void setCreate(XSQL i_XSQL)
    {
        if ( i_XSQL.getError() == null )
        {
            i_XSQL.setError(this.error);
        }
        this.xsqls.add(new XSQLTriggerInfo(i_XSQL ,$Execute));
    }
    
    
    /**
     * 请求数据库的次数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-06
     * @version     v1.0
     *
     * @return
     */
    public long getRequestCount()
    {
        long v_Ret = 0;
        
        if ( Help.isNull(this.xsqls) )
        {
            return v_Ret;
        }
        
        for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
        {
            v_Ret += v_XSQLTrigger.getXsql().getRequestCount();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 请求成功，并成功返回次数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-06
     * @version     v1.0
     *
     * @return
     */
    public long getSuccessCount()
    {
        long v_Ret = 0;
        
        if ( Help.isNull(this.xsqls) )
        {
            return v_Ret;
        }
        
        for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
        {
            v_Ret += v_XSQLTrigger.getXsql().getSuccessCount();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 请求成功，并成功返回的累计用时时长。
     * 用的是Double，而不是long，因为在批量执行时。为了精度，会出现小数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-06
     * @version     v1.0
     *
     * @return
     */
    public double getSuccessTimeLen()
    {
        double v_Ret = 0;
        
        if ( Help.isNull(this.xsqls) )
        {
            return v_Ret;
        }
        
        for (XSQLTriggerInfo v_XSQLTrigger : this.xsqls)
        {
            v_Ret += v_XSQLTrigger.getXsql().getSuccessTimeLen();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 获取：触发器执行操作的集合
     * 
     * 1. 在同步模式(单线程)下，执行按List顺序有序执行。零下标的元素第一个执行。
     * 2. 在异步模式(多线程)下，线程的发起按List有顺序发起。但不一定是有顺序的执行。
     */
    public List<XSQLTriggerInfo> getXsqls()
    {
        return xsqls;
    }


    
    /**
     * 设置：触发器执行操作的集合
     * 
     * 1. 在同步模式(单线程)下，执行按List顺序有序执行。零下标的元素第一个执行。
     * 2. 在异步模式(多线程)下，线程的发起按List有顺序发起。但不一定是有顺序的执行。
     * 
     * @param xsqls
     */
    public void setXsqls(List<XSQLTriggerInfo> xsqls)
    {
        this.xsqls = xsqls;
    }


    
    /**
     * 获取：同步模式。默认为：true，即同步模式
     */
    public boolean isSyncMode()
    {
        return syncMode;
    }


    
    /**
     * 设置：同步模式。默认为：true，即同步模式
     * 
     * @param syncMode
     */
    public void setSyncMode(boolean syncMode)
    {
        this.syncMode = syncMode;
    }


    
    /**
     * 获取：异常模式。
     * 默认为：false，主XSQL执行成功后，触发器才被触发执行
     * 当为：true时， 主XSQL异常时，触发器也被触发执行
     */
    public boolean isErrorMode()
    {
        return errorMode;
    }


    
    /**
     * 设置：异常模式。
     * 默认为：false，主XSQL执行成功后，触发器才被触发执行
     * 当为：true时， 主XSQL异常时，触发器也被触发执行
     * 
     * @param errorMode
     */
    public void setErrorMode(boolean errorMode)
    {
        this.errorMode = errorMode;
    }



    /**
     * 获取：可自行定制的XSQL异常处理机制。当触发的XSQL未设置异常处理机制（XSQL.getError()==null）时，才生效
     */
    public XSQLError getError()
    {
        return error;
    }


    
    /**
     * 设置：可自行定制的XSQL异常处理机制。当触发的XSQL未设置异常处理机制（XSQL.getError()==null）时，才生效
     * 
     * @param error
     */
    public void setError(XSQLError error)
    {
        this.error = error;
    }


    
    /**
     * 获取：是否初始化过所有的XSQL。只针对 createBackup() 功能的初始化
     */
    public boolean isInit()
    {
        return isInit;
    }


    
    /**
     * 设置：是否初始化过所有的XSQL。只针对 createBackup() 功能的初始化
     * 
     * @param isInit
     */
    public void setInit(boolean isInit)
    {
        this.isInit = isInit;
    }


    
    /**
     * 获取：XJava池中对象的ID标识
     */
    @Override
    public String getXJavaID()
    {
        return xjavaID;
    }


    
    /**
     * 设置：XJava池中对象的ID标识
     * 
     * @param i_XjavaID XJava池中对象的ID标识
     */
    @Override
    public void setXJavaID(String i_XjavaID)
    {
        this.xjavaID = i_XjavaID;
    }


    
    /**
     * 获取：注释。可用于日志的输出等帮助性的信息
     */
    @Override
    public String getComment()
    {
        return comment;
    }


    
    /**
     * 设置：注释。可用于日志的输出等帮助性的信息
     * 
     * @param i_Comment 注释。可用于日志的输出等帮助性的信息
     */
    @Override
    public void setComment(String i_Comment)
    {
        this.comment = i_Comment;
    }



    @Override
    public int compareTo(XSQLTrigger i_XSQLTrigger)
    {
        if ( i_XSQLTrigger == null )
        {
            return 1;
        }
        else if ( this == i_XSQLTrigger )
        {
            return 0;
        }
        else if ( Help.isNull(this.getXJavaID()) )
        {
            return -1;
        }
        else if ( Help.isNull(i_XSQLTrigger.getXJavaID()) )
        {
            return 1;
        }
        else
        {
            return this.getXJavaID().compareTo(i_XSQLTrigger.getXJavaID());
        }
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
        else if ( i_Other instanceof XSQLTrigger )
        {
            XSQLTrigger v_Other = (XSQLTrigger) i_Other;
            
            if ( Help.isNull(this.getXJavaID()) )
            {
                return false;
            }
            else if ( Help.isNull(v_Other.getXJavaID()) )
            {
                return false;
            }
            else
            {
                return this.getXJavaID().equals(v_Other.getXJavaID());
            }
        }
        else
        {
            return false;
        }
    }
    
}
