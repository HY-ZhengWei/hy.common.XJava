package org.hy.common.xml;

import java.io.Serializable;

import org.hy.common.Date;





/**
 * XSQL 执行日志
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-07-05
 * @version     v1.0
 *              v2.0  2017-01-04  添加：XSQL 的唯一标识ID，这通过此属性对比出是哪一个XSQL对象的错误。
 *                                     并能统计、罗列出此XSQL的所有错误。
 *                                     不直接将XSQL对象作为本类的属性的原因：是为了安全考虑。
 */
public class XSQLLog implements Serializable
{
    
    private static final long serialVersionUID = -6948406993065598422L;
    
    
    
    /** XSQL 的唯一标识ID */
    private String oid;
    
    /** 执行SQL语句 */
    private String sql;
    
    /** 执行时间。一般执行完成时的时间，或出现异常时的时间 */
    private String time;
    
    /** 执行异常信息 */
    private String e;
    
    
    
    public XSQLLog(String i_SQL)
    {
        this.time  = Date.getNowTime().getFullMilli();
        this.sql   = i_SQL;
        this.e     = "";
    }
    
    
    
    public XSQLLog(String i_SQL ,Exception i_Exce)
    {
        this(i_SQL ,i_Exce ,"");
    }
    
    
    
    public XSQLLog(String i_SQL ,Exception i_Exce ,String i_XSQLObjectID)
    {
        this.time = Date.getNowTime().getFullMilli();
        this.sql  = i_SQL;
        this.e    = i_Exce.getMessage();
        this.oid  = i_XSQLObjectID;
    }
    
    
    
    /**
     * 获取：执行SQL语句
     */
    public String getSql()
    {
        return sql;
    }

    
    /**
     * 设置：执行SQL语句
     * 
     * @param sql 
     */
    public void setSql(String sql)
    {
        this.sql = sql;
    }

    
    /**
     * 获取：执行时间
     */
    public String getTime()
    {
        return time;
    }

    
    /**
     * 设置：执行时间
     * 
     * @param time 
     */
    public void setTime(String time)
    {
        this.time = time;
    }

    
    /**
     * 获取：执行异常信息
     */
    public String getE()
    {
        return e;
    }

    
    /**
     * 设置：执行异常信息
     * 
     * @param error 
     */
    public void setE(String e)
    {
        this.e = e;
    }

    
    /**
     * 获取：XSQL 的唯一标识ID
     */
    public String getOid()
    {
        return oid;
    }

    
    /**
     * 设置：XSQL 的唯一标识ID
     * 
     * @param oid 
     */
    public void setOid(String oid)
    {
        this.oid = oid;
    }
    
}
