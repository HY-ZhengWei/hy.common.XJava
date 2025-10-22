package org.hy.common.xml.junit;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Date;





/**
 * 光谱成份
 *
 * @author      ZhengWei(HY)
 * @createDate  2025-10-22
 * @version     v1.0
 */
public class SpectrumVO
{
    
    /** 主键信息 */
    private String                         id;
    
    /** 批次号 */
    private String                         batchNo;
    
    /** 牌号标准 */
    private String                         grade;
    
    /** 测温时间 */
    private Date                           dateTime;
    
    /** 光谱数据项 */
    private Map<String ,SpectrumElementVO> elements;
    
    
    
    public SpectrumVO()
    {
        this.elements = new HashMap<String ,SpectrumElementVO>();
    }
    
    
    /**
     * 获取：主键信息
     */
    public String getId()
    {
        return id;
    }

    
    /**
     * 设置：主键信息
     * 
     * @param i_Id 主键信息
     */
    public void setId(String i_Id)
    {
        this.id = i_Id;
    }


    /**
     * 获取：批次号
     */
    public String getBatchNo()
    {
        return batchNo;
    }

    
    /**
     * 设置：批次号
     * 
     * @param i_BatchNo 批次号
     */
    public void setBatchNo(String i_BatchNo)
    {
        this.batchNo = i_BatchNo;
    }

    
    /**
     * 获取：牌号标准
     */
    public String getGrade()
    {
        return grade;
    }

    
    /**
     * 设置：牌号标准
     * 
     * @param i_Grade 牌号标准
     */
    public void setGrade(String i_Grade)
    {
        this.grade = i_Grade;
    }


    /**
     * 获取：测温时间
     */
    public Date getDateTime()
    {
        return dateTime;
    }

    
    /**
     * 设置：测温时间
     * 
     * @param i_DateTime 测温时间
     */
    public void setDateTime(Date i_DateTime)
    {
        this.dateTime = i_DateTime;
    }
    
    
    /**
     * 获取：测温时间
     */
    public Date getValueTime()
    {
        return dateTime;
    }

    
    /**
     * 设置：测温时间
     * 
     * @param i_ValueTime 测温时间
     */
    public void setValueTime(Date i_ValueTime)
    {
        this.dateTime = i_ValueTime;
    }

    
    /**
     * 获取：光谱数据项
     */
    public Map<String ,SpectrumElementVO> getElements()
    {
        return elements;
    }

    
    /**
     * 设置：光谱数据项
     * 
     * @param i_Elements 光谱数据项
     */
    public void setElements(Map<String ,SpectrumElementVO> i_Elements)
    {
        this.elements = i_Elements;
    }
    
}
