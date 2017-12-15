package org.hy.common.xml;

import org.hy.common.xml.annotation.Xsql;





/**
 * @Xsql 注解反射信息的操作类
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-15
 * @version     v1.0
 */
public class XSQLAnnotation
{
    
    private final Xsql xsql;
    
    /** 是否曾经检查通过过。为了提高执行性能，第二次执行时，有些重复检查就没必要了 */
    private boolean    checkOK;
    
    /** XJava对象池中的id */
    private String     xid;
    
    
    
    public XSQLAnnotation(Xsql i_Xsql)
    {
        this.xsql    = i_Xsql;
        this.checkOK = false;
    }


    
    /**
     * 获取：@Xsql 注解对象
     */
    public Xsql getXsql()
    {
        return xsql;
    }
    

    
    /**
     * 获取：是否曾经检查通过过。为了提高执行性能，第二次执行时，有些重复检查就没必要了
     */
    public boolean isCheckOK()
    {
        return checkOK;
    }
    

    
    /**
     * 设置：是否曾经检查通过过。为了提高执行性能，第二次执行时，有些重复检查就没必要了
     * 
     * @param checkOK 
     */
    public void setCheckOK(boolean checkOK)
    {
        this.checkOK = checkOK;
    }


    
    /**
     * 获取：XJava对象池中的id
     */
    public String getXid()
    {
        return xid;
    }
    

    
    /**
     * 设置：XJava对象池中的id
     * 
     * @param xid 
     */
    public void setXid(String xid)
    {
        this.xid = xid;
    }
    
}
