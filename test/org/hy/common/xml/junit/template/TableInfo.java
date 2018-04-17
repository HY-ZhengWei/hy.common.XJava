package org.hy.common.xml.junit.template;

import java.util.List;

import org.hy.common.xml.SerializableDef;





/**
 * 表信息 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-05-11
 * @version     v1.0
 */
public class TableInfo extends SerializableDef
{
    
    private static final long serialVersionUID = 3818570047406654367L;
    
    
    /** 作者 */
    private String           author;
    
    /** 描述信息 */
    private String           description;
    
    /** 表名称 */
    private String           nameDB;
    
    /** Java类名称 */
    private String           nameJava;
    
    /** 字段信息 */
    private List<ColumnInfo> tableColumns;
    
    
    
    /**
     * 获取：表名称
     */
    public String getNameDB()
    {
        return nameDB;
    }

    
    /**
     * 设置：表名称
     * 
     * @param nameDB 
     */
    public void setNameDB(String nameDB)
    {
        this.nameDB   = nameDB;
        this.nameJava = nameDB;
    }

    
    /**
     * 获取：Java类名称
     */
    public String getNameJava()
    {
        return nameJava;
    }

    
    /**
     * 设置：Java类名称
     * 
     * @param nameJava 
     */
    public void setNameJava(String nameJava)
    {
        this.nameJava = nameJava;
    }


    /**
     * 获取：作者
     */
    public String getAuthor()
    {
        return author;
    }

    
    /**
     * 设置：作者
     * 
     * @param author 
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }


    /**
     * 获取：描述信息
     */
    public String getDescription()
    {
        return description;
    }

    
    /**
     * 设置：描述信息
     * 
     * @param description 
     */
    public void setDescription(String description)
    {
        this.description = description;
    }



    
    /**
     * 获取：字段信息
     */
    public List<ColumnInfo> getTableColumns()
    {
        return tableColumns;
    }

    
    /**
     * 设置：字段信息
     * 
     * @param tableColumns 
     */
    public void setTableColumns(List<ColumnInfo> tableColumns)
    {
        this.tableColumns = tableColumns;
    }
    
}
