package org.hy.common.xml.junit.template;

import org.hy.common.xml.SerializableDef;





public class AttributeInfo extends SerializableDef
{
    
    private static final long serialVersionUID = 8584632341378032843L;
    

    /** 属性说明 */
    private String attributeDesc;
    
    /** 属性名称 */
    private String attributeName;
    
    /** 属性的大写名称 */
    private String attributeNameUpper;
    
    /** 属性类型 */
    private String attributeType;

    
    
    /**
     * 获取：属性说明
     */
    public String getAttributeDesc()
    {
        return attributeDesc;
    }

    
    /**
     * 设置：属性说明
     * 
     * @param attributeDesc 
     */
    public void setAttributeDesc(String attributeDesc)
    {
        this.attributeDesc = attributeDesc;
    }

    
    /**
     * 获取：属性名称
     */
    public String getAttributeName()
    {
        return attributeName;
    }

    
    /**
     * 设置：属性名称
     * 
     * @param attributeName 
     */
    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    
    /**
     * 获取：属性的大写名称
     */
    public String getAttributeNameUpper()
    {
        return attributeNameUpper;
    }

    
    /**
     * 设置：属性的大写名称
     * 
     * @param attributeNameUpper 
     */
    public void setAttributeNameUpper(String attributeNameUpper)
    {
        this.attributeNameUpper = attributeNameUpper;
    }

    
    /**
     * 获取：属性类型
     */
    public String getAttributeType()
    {
        return attributeType;
    }


    /**
     * 设置：属性类型
     * 
     * @param attributeType 
     */
    public void setAttributeType(String attributeType)
    {
        this.attributeType = attributeType;
    }
    
}
