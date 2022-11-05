package org.hy.common.xml.plugins.analyse.data;

import java.io.Serializable;
import java.util.List;





/**
 * XSQLGroup组对外显示输出的树结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-09-10
 * @version     v1.0
 */
public class XSQLGroupTree implements Serializable
{

    private static final long serialVersionUID = -8975069739843201141L;
    
    
    /** XJava的对象ID。可为空 */
    private String              xid;
    
    /** XSQLNode节点执行的对象ID。有可能是XSQL的XID，也可能是Java类的XID及方法名称 */
    private String              executeXID;
    
    /** XSQLNode节点执行的数据组连接池组的名称 */
    private String              dbgName;
    
    /** XSQLNode节点执行返回的对象ID */
    private String              returnID;
    
    /** XSQLNode节点执行 */
    private String              condition;
    
    /** XSQLNode节点多线程执行的类型 */
    private String              threadType;
    
    /** XSQLNode节点多线程等待的节点 */
    private String              threadWait;
    
    /** XSQLNode节点云等待的节点 */
    private String              cloudWait;
    
    /** XSQLNode节点云计算的服务器数量 */
    private String              cloudServers;
    
    /** 节点名称，对应XSQLNode中的注释XSQLNode.comment。可为空 */
    private String              name;
    
    /** 节点注释 */
    private String              comment;
    
    /** 节点类型。见XSQLNode.type。只对叶子节点有效 */
    private String              nodeType;
    
    /** 子节点 */
    private List<XSQLGroupTree> children;

    
    
    /**
     * 获取：XJava的对象ID。可为空
     */
    public String getXid()
    {
        return xid;
    }
    

    
    /**
     * 获取：XSQLNode节点执行的对象ID。有可能是XSQL的XID，也可能是Java类的XID及方法名称
     */
    public String getExecuteXID()
    {
        return executeXID;
    }
    

    
    /**
     * 获取：节点名称，对应XSQLNode中的注释XSQLNode.comment。可为空
     */
    public String getName()
    {
        return name;
    }
    

    
    /**
     * 获取：节点类型。见XSQLNode.type。只对叶子节点有效
     */
    public String getNodeType()
    {
        return nodeType;
    }
    

    
    /**
     * 获取：子节点
     */
    public List<XSQLGroupTree> getChildren()
    {
        return children;
    }
    

    
    /**
     * 设置：XJava的对象ID。可为空
     * 
     * @param xid 
     */
    public void setXid(String xid)
    {
        this.xid = xid;
    }
    

    
    /**
     * 设置：XSQLNode节点执行的对象ID。有可能是XSQL的XID，也可能是Java类的XID及方法名称
     * 
     * @param executeXID 
     */
    public void setExecuteXID(String executeXID)
    {
        this.executeXID = executeXID;
    }
    

    
    /**
     * 设置：节点名称，对应XSQLNode中的注释XSQLNode.comment。可为空
     * 
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }
    

    
    /**
     * 设置：节点类型。见XSQLNode.type。只对叶子节点有效
     * 
     * @param nodeType 
     */
    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }
    

    
    /**
     * 设置：子节点
     * 
     * @param children 
     */
    public void setChildren(List<XSQLGroupTree> children)
    {
        this.children = children;
    }


    
    /**
     * 获取：XSQLNode节点执行返回的对象ID
     */
    public String getReturnID()
    {
        return returnID;
    }
    

    
    /**
     * 设置：XSQLNode节点执行返回的对象ID
     * 
     * @param returnID 
     */
    public void setReturnID(String returnID)
    {
        this.returnID = returnID;
    }


    
    /**
     * 获取：节点注释
     */
    public String getComment()
    {
        return comment;
    }
    

    
    /**
     * 设置：节点注释
     * 
     * @param comment 
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }



    /**
     * 获取：XSQLNode节点执行
     */
    public String getCondition()
    {
        return condition;
    }
    

    
    /**
     * 设置：XSQLNode节点执行
     * 
     * @param condition 
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }


    
    /**
     * 获取：XSQLNode节点多线程执行的类型
     */
    public String getThreadType()
    {
        return threadType;
    }


    
    /**
     * 设置：XSQLNode节点多线程执行的类型
     * 
     * @param threadType 
     */
    public void setThreadType(String threadType)
    {
        this.threadType = threadType;
    }


    
    /**
     * 获取：XSQLNode节点云计算的服务器数量
     */
    public String getCloudServers()
    {
        return cloudServers;
    }
    

    
    /**
     * 设置：XSQLNode节点云计算的服务器数量
     * 
     * @param cloudServers 
     */
    public void setCloudServers(String cloudServers)
    {
        this.cloudServers = cloudServers;
    }


    
    /**
     * 获取：XSQLNode节点执行的数据组连接池组的名称
     */
    public String getDbgName()
    {
        return dbgName;
    }
    


    /**
     * 设置：XSQLNode节点执行的数据组连接池组的名称
     * 
     * @param dbgName 
     */
    public void setDbgName(String dbgName)
    {
        this.dbgName = dbgName;
    }


    
    /**
     * 获取：XSQLNode节点多线程等待的节点
     */
    public String getThreadWait()
    {
        return threadWait;
    }
    

    
    /**
     * 设置：XSQLNode节点多线程等待的节点
     * 
     * @param threadWait 
     */
    public void setThreadWait(String threadWait)
    {
        this.threadWait = threadWait;
    }


    
    /**
     * 获取：XSQLNode节点云等待的节点
     */
    public String getCloudWait()
    {
        return cloudWait;
    }
    


    /**
     * 设置：XSQLNode节点云等待的节点
     * 
     * @param cloudWait 
     */
    public void setCloudWait(String cloudWait)
    {
        this.cloudWait = cloudWait;
    }
    
}
