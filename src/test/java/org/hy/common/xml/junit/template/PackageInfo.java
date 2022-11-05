package org.hy.common.xml.junit.template;

import org.hy.common.xml.SerializableDef;


/**
 * TODO(请详细描述类型的作用。描述后请删除todo标签) 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-05-12
 * @version     v1.0
 */
public class PackageInfo extends SerializableDef
{
    
    private static final long serialVersionUID = 6117159950746889193L;

    
    /** 包名称 */
    private String    packageName;
    
    /** 类信息 */
    private TableInfo table;

    
    
    /**
     * 获取：包名称
     */
    public String getPackageName()
    {
        return packageName;
    }

    
    /**
     * 设置：包名称
     * 
     * @param packageName 
     */
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    
    /**
     * 获取：类信息
     */
    public TableInfo getTable()
    {
        return table;
    }

    
    /**
     * 设置：类信息
     * 
     * @param table 
     */
    public void setTable(TableInfo table)
    {
        this.table = table;
    }
    
}
