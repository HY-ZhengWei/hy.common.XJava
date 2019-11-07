package org.hy.common.xml.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;


/** 
 * 
 * Table 
 * */
public class ProductList extends ArrayList
{

    private static final long serialVersionUID = 5585043737441297490L;
    
    
    
    /** 快速定位。存放着所有父节点信息 */
    private Map<String ,Object> childMap;



    
    public ProductList()
    {
        this.childMap = new Hashtable<String ,Object>();
    }
    
    
    
    /**
     * 获取：快速定位
     */
    public Map<String ,Object> getChildMap()
    {
        return childMap;
    }



    
    /**
     * 设置：快速定位
     * 
     * @param childMap 
     */
    public void setChildMap(Map<String ,Object> childMap)
    {
        this.childMap = childMap;
    }
    
    
    
}
