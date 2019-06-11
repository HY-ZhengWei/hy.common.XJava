package org.hy.common.xml.plugins.analyse.data;

import java.util.List;

import org.hy.common.Help;
import org.hy.common.StringHelp;





/**
 * 查看XSQL与表的关系图
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-06-11
 * @version     v1.0
 */
public class XSQLRetTable
{
    
    /** 表名称 */
    private String tableName;
    
    /** 表对应的XSQL */
    private List<String> xsqls;
    
    /** XSQL的ID */
    private String xsql;
    
    /** XSQL的类型，对应DBSQL.getSQLType() */
    private Integer type;
    
    /** XSQL文本 */
    private String sqlText;
    
    /** 引用次数 */
    private Integer refCount;
    
    
    
    /**
     * 表对应的XSQL的个数，主要用于排序
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-06-11
     * @version     v1.0
     *
     * @return
     */
    public Integer getXsqlCount()
    {
        if ( this.xsqls == null )
        {
            return null;
        }
        
        return this.xsqls.size();
    }
    
    
    /**
     * 用于排序，按XSQL名称排序
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-06-11
     * @version     v1.0
     *
     * @return
     */
    public String getOrderTableName()
    {
        if ( Help.isNull(this.xsqls) )
        {
            return this.tableName;
        }
        else
        {
            return StringHelp.toString(this.xsqls);
        }
    }
    
    
    /**
     * 获取：表名称
     */
    public String getTableName()
    {
        return tableName;
    }

    
    /**
     * 获取：表对应的XSQL
     */
    public List<String> getXsqls()
    {
        return xsqls;
    }

    
    /**
     * 获取：XSQL的ID
     */
    public String getXsql()
    {
        return xsql;
    }

    
    /**
     * 获取：XSQL的类型，对应DBSQL.getSQLType()
     */
    public Integer getType()
    {
        return type;
    }

    
    /**
     * 设置：表名称
     * 
     * @param tableName 
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    
    /**
     * 设置：表对应的XSQL
     * 
     * @param xsqls 
     */
    public void setXsqls(List<String> xsqls)
    {
        this.xsqls = xsqls;
    }

    
    /**
     * 设置：XSQL的ID
     * 
     * @param xsql 
     */
    public void setXsql(String xsql)
    {
        this.xsql = xsql;
    }

    
    /**
     * 设置：XSQL的类型，对应DBSQL.getSQLType()
     * 
     * @param type 
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    
    /**
     * 获取：XSQL文本
     */
    public String getSqlText()
    {
        return sqlText;
    }

    
    /**
     * 设置：XSQL文本
     * 
     * @param sqlText 
     */
    public void setSqlText(String sqlText)
    {
        this.sqlText = sqlText;
    }

    
    /**
     * 获取：引用次数
     */
    public Integer getRefCount()
    {
        return refCount;
    }

    
    /**
     * 设置：引用次数
     * 
     * @param refCount 
     */
    public void setRefCount(Integer refCount)
    {
        this.refCount = refCount;
    }
    
}
