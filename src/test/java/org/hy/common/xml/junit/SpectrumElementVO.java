package org.hy.common.xml.junit;

import java.util.Map;

/**
 * 光谱数据项的信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2025-10-22
 * @version     v1.0
 */
public class SpectrumElementVO
{

    /** 主键ID */
    private String id;
    
    /** 归属ID */
    private String ownerID;
    
    /** 数据项名称 */
    private String name;
    
    /** 数据项符号 */
    private String symbol;
    
    /** 数据项值 */
    private Double value;
    
    private Map<String ,SpectrumElementChildVO> childs;

    
    
    /**
     * 获取：主键ID
     */
    public String getId()
    {
        return id;
    }

    
    /**
     * 设置：主键ID
     * 
     * @param i_Id 主键ID
     */
    public void setId(String i_Id)
    {
        this.id = i_Id;
    }

    
    /**
     * 获取：归属ID
     */
    public String getOwnerID()
    {
        return ownerID;
    }

    
    /**
     * 设置：归属ID
     * 
     * @param i_OwnerID 归属ID
     */
    public void setOwnerID(String i_OwnerID)
    {
        this.ownerID = i_OwnerID;
    }


    /**
     * 获取：数据项名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：数据项名称
     * 
     * @param i_Name 数据项名称
     */
    public void setName(String i_Name)
    {
        this.name = i_Name;
    }

    
    /**
     * 获取：数据项符号
     */
    public String getSymbol()
    {
        return symbol;
    }

    
    /**
     * 设置：数据项符号
     * 
     * @param i_Symbol 数据项符号
     */
    public void setSymbol(String i_Symbol)
    {
        symbol = i_Symbol;
    }

    
    /**
     * 获取：数据项值
     */
    public Double getValue()
    {
        return value;
    }

    
    /**
     * 设置：数据项值
     * 
     * @param i_Value 数据项值
     */
    public void setValue(Double i_Value)
    {
        this.value = i_Value;
    }

    
    /**
     * 获取：三级子对象
     */
    public Map<String ,SpectrumElementChildVO> getChilds()
    {
        return childs;
    }

    
    /**
     * 设置：三级子对象
     * 
     * @param i_Childs 
     */
    public void setChilds(Map<String ,SpectrumElementChildVO> i_Childs)
    {
        this.childs = i_Childs;
    }
    
}
