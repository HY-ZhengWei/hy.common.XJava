package org.hy.common.xml;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.XJavaID;





/**
 * XJava.$XML_OBJECTS集合中的元素对象
 * 
 * 有了此类后，就可以更加丰富XJava构造对象的多样化。
 * 
 * 1. 可从原先所有构造出的对象都是单例，变成：可单例、可多实例
 * 2. 深克隆：当构造一个新的实例时，如果有clone()方法，则调用克隆对象
 * 3. 浅克隆：当构造一个新的实例时，如果没有clone()方法，则通过无参数的构造器new一个实例，再依次newObject.setter(oldObject.getter())
 * 
 * @author      ZhengWei(HY)
 * @createDate  2013-08-10
 * @version     v1.0
 *              v2.0  2025-08-14  添加：创建时间   
 *                                删除：所有Setter方法     
 *                                独立：从原先的内部类，独立为正常类      
 */
public class XJavaObject
{
    /** XJava构造出的对象实例 */
    private Object object;
    
    /**
     * 是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     * 
     * XJava默认构造出的对象为"单例"
     */
    private boolean isNew;
    
    /** 创建时间 */
    private Date    createTime;
    
    
    
    public XJavaObject()
    {
        this(null ,null ,false);
    }
    
    
    
    public XJavaObject(String i_XID ,Object i_Object)
    {
        this(i_XID ,i_Object ,false);
    }
    
    
    
    public XJavaObject(String i_XID ,Object i_Object ,boolean i_IsNew)
    {
        this.object     = i_Object;
        this.isNew      = i_IsNew;
        this.createTime = new Date();
        
        // 将配置在XML配置文件中的ID值，自动赋值给Java实例对象。  ZhengWei(HY) Add 2018-03-09
        if ( this.object != null )
        {
            if ( this.object instanceof XJavaID )
            {
                XJavaID v_XJavaID = (XJavaID)this.object;
                if ( Help.isNull(v_XJavaID.getXJavaID()) )
                {
                    // 不重复赋值
                    v_XJavaID.setXJavaID(i_XID);
                }
            }
        }
    }
    
    
    /**
     * 尝试获取保存在对象内的XID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2025-08-14
     * @version     v1.0
     *
     * @return
     */
    public String getXID()
    {
        if ( this.object != null )
        {
            if ( this.object instanceof XJavaID )
            {
                XJavaID v_XJavaID = (XJavaID)this.object;
                return v_XJavaID.getXJavaID();
            }
        }
        
        return null;
    }
    
    
    /**
     * 是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     * 
     * XJava默认构造出的对象为"单例"
     */
    public boolean isNew()
    {
        return isNew;
    }

    
    public Object getObject() throws NoSuchMethodException
    {
        return this.getObject(this.isNew);
    }
    
    
    public Object getObject(boolean i_IsNew) throws NoSuchMethodException
    {
        if ( i_IsNew )
        {
            return XJava.clone(this.object);
        }
        else
        {
            return this.object;
        }
    }
    
    
    /**
     * 不克隆的获取对象实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2025-08-14
     * @version     v1.0
     *
     * @return
     */
    public Object getObjectNoClone()
    {
        return this.object;
    }
    
    
    /**
     * 获取：创建时间
     */
    public Date getCreateTime()
    {
        return createTime;
    }
    
}