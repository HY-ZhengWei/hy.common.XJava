package org.hy.common.xml.event;

import java.util.List;

public class Product
{
    
    private String superID;
    
    private String id;
    
    private List<Object> childs;

    
    /**
     * 获取：
     */
    public String getSuperID()
    {
        return superID;
    }

    
    /**
     * 获取：
     */
    public String getId()
    {
        return id;
    }

    
    /**
     * 获取：
     */
    public List<Object> getChilds()
    {
        return childs;
    }

    
    /**
     * 设置：
     * 
     * @param superID 
     */
    public void setSuperID(String superID)
    {
        this.superID = superID;
    }

    
    /**
     * 设置：
     * 
     * @param id 
     */
    public void setId(String id)
    {
        this.id = id;
    }

    
    /**
     * 设置：
     * 
     * @param childs 
     */
    public void setChilds(List<Object> childs)
    {
        this.childs = childs;
    }
    
}
