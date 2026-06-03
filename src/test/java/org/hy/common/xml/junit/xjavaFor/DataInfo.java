package org.hy.common.xml.junit.xjavaFor;

import org.hy.common.XJavaID;





/**
 * 数据信息 
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-06-03
 * @version     v1.0
 */
public class DataInfo implements XJavaID
{
    
    /** ID */
    private Integer id;
    
    /** 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的 */
    private String  xid;
    
    /** 名称 */
    private String  name;
    
    /** 数据 */
    private String  data;
    
    /** 注释。可用于日志的输出等帮助性的信息 */
    private String  comment;
    
    
    public DataInfo()
    {
        
    }
    
    
    public DataInfo(Integer i_Value)
    {
        this.id = i_Value;
    }
    
    
    /**
     * 获取：ID
     */
    public Integer getId()
    {
        return id;
    }

    
    /**
     * 设置：ID
     * 
     * @param i_Id ID
     */
    public void setId(Integer i_Id)
    {
        this.id = i_Id;
    }


    /**
     * 获取：设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的
     */
    public String getXid()
    {
        return xid;
    }

    
    /**
     * 设置：设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的
     * 
     * @param i_Xid 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的
     */
    public void setXid(String i_Xid)
    {
        this.xid = i_Xid;
    }


    /**
     * 获取：名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：名称
     * 
     * @param i_Name 名称
     */
    public void setName(String i_Name)
    {
        this.name = i_Name;
    }
    
    
    /**
     * 获取：数据
     */
    public String getData()
    {
        return data;
    }

    
    /**
     * 设置：数据
     * 
     * @param i_Data 数据
     */
    public void setData(String i_Data)
    {
        this.data = i_Data;
    }


    /**
     * 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的。
     * 
     * @param i_XJavaID
     */
    public void setXJavaID(String i_XJavaID)
    {
        this.xid = i_XJavaID;
    }
    
    
    /**
     * 获取XJava池中对象的ID标识。
     * 
     * @return
     */
    public String getXJavaID()
    {
        return this.xid;
    }
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     * 
     * @param i_Comment
     */
    public void setComment(String i_Comment)
    {
        this.comment = i_Comment;
    }
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     *
     * @return
     */
    public String getComment()
    {
        return this.comment;
    }
    
}
