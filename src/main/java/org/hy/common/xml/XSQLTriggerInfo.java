package org.hy.common.xml;





/**
 * 触发器的元素对象
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-08-13
 * @version     v1.0
 */
public class XSQLTriggerInfo
{
    /** XSQL对象 */
    private XSQL xsql;
   
    /** 执行类型（0按execute方法执行，1按executeUpdate方法执行） */
    private int  executeType;
   
   
   
    public XSQLTriggerInfo(XSQL i_XSQL ,int i_ExecuteType)
    {
        this.xsql        = i_XSQL;
        this.executeType = i_ExecuteType;
    }

    
    /**
     * 获取：XSQL对象
     */
    public XSQL getXsql()
    {
        return xsql;
    }
    
    
    /**
     * 设置：XSQL对象
     * 
     * @param i_Xsql XSQL对象
     */
    public void setXsql(XSQL i_Xsql)
    {
        this.xsql = i_Xsql;
    }


    /**
     * 获取：执行类型（0按execute方法执行，1按executeUpdate方法执行）
     */
    public int getExecuteType()
    {
        return executeType;
    }

    
    /**
     * 设置：执行类型（0按execute方法执行，1按executeUpdate方法执行）
     * 
     * @param executeType
     */
    public void setExecuteType(int executeType)
    {
        this.executeType = executeType;
    }
    
}
