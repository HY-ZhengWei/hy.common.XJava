package org.hy.common.xml.junit.template;

import java.util.Map;

import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.app.Param;
import org.hy.common.xml.SerializableDef;
import org.hy.common.xml.XJava;





/**
 * 字段信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-05-11
 * @version     v1.0
 */
public class ColumnInfo extends SerializableDef
{
    private static final long serialVersionUID = -4724247321457107633L;

    
    /** 字段名称 */
    private String name;
    
    /** 字段的数据类型 */
    private String typeDB;
    
    /** 字段的Java类型 */
    private String typeJava;
    
    /** 字段描述 */
    private String description;

    
    
    /**
     * 获取：字段名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：字段名称
     * 
     * @param i_Name 
     */
    public void setName(String i_Name)
    {
        if ( Help.isNull(i_Name) )
        {
            this.name = i_Name;
        }
        else
        {
            this.name = StringHelp.toLowerCaseByFirst(i_Name);
        }
    }

    
    /**
     * 获取：字段的数据类型
     */
    public String getTypeDB()
    {
        return typeDB;
    }

    
    /**
     * 设置：字段的数据类型
     * 
     * @param typeDB 
     */
    public void setTypeDB(String typeDB)
    {
        this.typeDB   = typeDB;
        this.typeJava = this.toJavaType(typeDB);
    }
    
    
    /**
     * 数据库字段类型转为Java类型
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-05-12
     * @version     v1.0
     *
     * @param i_ColDBType
     * @return
     */
    @SuppressWarnings("unchecked")
    public String toJavaType(String i_DbType)
    {
        Map<String ,Param> v_DBTypeTransforms = (Map<String ,Param>)XJava.getObject("DBTypeTransforms");
        Param              v_ColType          = v_DBTypeTransforms.get(i_DbType);
        
        if ( v_ColType == null )
        {
            return "";
        }
        else
        {
            return Help.NVL(v_ColType.getValue());
        }
    }

    
    
    /**
     * 获取：字段的Java类型
     */
    public String getTypeJava()
    {
        return typeJava;
    }

    
    /**
     * 设置：字段的Java类型
     * 
     * @param typeJava 
     */
    public void setTypeJava(String typeJava)
    {
        this.typeJava = typeJava;
    }

    
    /**
     * 获取：字段描述
     */
    public String getDescription()
    {
        return description;
    }

    
    /**
     * 设置：字段描述
     * 
     * @param description 
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
}
