package org.hy.common.xml;

import java.util.List;





/**
 * 查询数据库返回的结果。
 * 
 * 主要是通过 XSQLResullt.getDatas() 方法返回的结果。
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-03-19
 * @version     v1.0
 *              v2.0  2022-05-23  添加：Insert语句影响生成的自增长ID的集合
 */
public class XSQLData
{
    
    /** 查询返回的结果 */
    private Object        datas;
    
    /** 将数据库结果集转化为Java实例对象的行数(已读取的行数) */
    private long          rowCount;
    
    /** 将数据库结果集转化为Java实例对象的列数(有效列数) */
    private int           colCount;
    
    /** 将数据库结果集转化为Java实例对象的用时时长(单位：毫秒) */
    private long          timeLen;
    
    
    
    public XSQLData(Object i_Datas ,long i_RowCount ,int i_ColCount ,long i_TimeLen)
    {
        this.datas     = i_Datas;
        this.rowCount  = i_RowCount;
        this.colCount  = i_ColCount;
        this.timeLen   = i_TimeLen;
    }

    
    
    /**
     * 获取：查询返回的结果
     */
    public Object getDatas()
    {
        return datas;
    }


    
    /**
     * 获取：将数据库结果集转化为Java实例对象的行数(已读取的行数)
     */
    public long getRowCount()
    {
        return rowCount;
    }


    
    /**
     * 获取：将数据库结果集转化为Java实例对象的列数(有效列数)
     */
    public int getColCount()
    {
        return colCount;
    }


    
    /**
     * 获取：将数据库结果集转化为Java实例对象的用时时长(单位：毫秒)
     */
    public long getTimeLen()
    {
        return timeLen;
    }


    
    /**
     * 获取：Insert语句影响生成的自增长ID的集合
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getIdentitys()
    {
        return (List<Integer>)datas;
    }
    
}
