package org.hy.common.xml.event;

import java.lang.reflect.Method;





/**
 * 一对多关系的引用关键字的方法信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2024-01-09
 * @version     v1.0
 */
public class RelationKeyMethod
{
    
    /** 识别一对多关系的Getter方法。如组合主键时，获取每个主键的Getter方法 */
    private Method method;
    
    /** 固定的关键字数值。仅当 method 为 Map.get() 方法时生效 */
    private String mapKey;

    
    
    public RelationKeyMethod(Method i_Method ,String i_MapKey)
    {
        this.method = i_Method;
        this.mapKey = i_MapKey;
    }
    
    
    /**
     * 获取：识别一对多关系的Getter方法。如组合主键时，获取每个主键的Getter方法
     */
    public Method getMethod()
    {
        return method;
    }

    
    /**
     * 获取：固定的关键字数值。仅当 method 为 Map.get() 方法时生效
     */
    public String getMapKey()
    {
        return mapKey;
    }
    
}
