package org.hy.common.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.xml.annotation.Xparam;





/**
 * @Xparam 注解反射信息的操作类
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-18
 * @version     v1.0
 */
public class XParamAnnotation implements Serializable
{
    
    private static final long serialVersionUID = -7531474798603852935L;
    
    

    /** 对应 @Xparam.name */
    private String              name;
    
    /** 对应 @Xparam.notNull */
    private boolean             notNull;
    
    /** 对应 @Xparam.notNulls */
    private List<MethodReflect> notNulls;

    
    
    public XParamAnnotation(String i_Name)
    {
        this.name     = i_Name;
        this.notNull  = false;
        this.notNulls = null;
    }
    
    
    
    public XParamAnnotation(Class<?> i_ParamClass ,Xparam i_Xparam) throws InstantiationException
    {
        this((String)null);
        
        if ( i_Xparam != null )
        {
            this.name     = Help.NVL(i_Xparam.value() ,Help.NVL(i_Xparam.name() ,i_Xparam.id()));
            this.notNull  = i_Xparam.notNull();
            
            if ( i_Xparam.notNulls().length >= 1 )
            {
                this.notNull  = true;
                this.notNulls = new ArrayList<MethodReflect>(i_Xparam.notNulls().length);
                
                for (String v_Name : i_Xparam.notNulls())
                {
                    if ( Help.isNull(v_Name) )
                    {
                        continue;
                    }
                    
                    MethodReflect v_MethodReflect = null;
                    
                    try
                    {
                        v_MethodReflect = new MethodReflect(i_ParamClass ,v_Name ,true ,MethodReflect.$NormType_Getter);
                    }
                    catch (Exception exce)
                    {
                        throw new InstantiationException("Xparam(notnulls={'" + v_Name + "'} is not exists (" + i_ParamClass.getName() + ").");
                    }
                    
                    this.notNulls.add(v_MethodReflect);
                }
            }
        }
    }
    
    
    
    /**
     * 获取：对应 @Xparam.name
     */
    public String getName()
    {
        return name;
    }
    

    
    /**
     * 获取：对应 @Xparam.notNull
     */
    public boolean isNotNull()
    {
        return notNull;
    }
    

    
    /**
     * 获取：对应 @Xparam.notNulls
     */
    public List<MethodReflect> getNotNulls()
    {
        return notNulls;
    }
    

    
    /**
     * 设置：对应 @Xparam.name
     * 
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }
    

    
    /**
     * 设置：对应 @Xparam.notNull
     * 
     * @param notNull 
     */
    public void setNotNull(boolean notNull)
    {
        this.notNull = notNull;
    }
    

    
    /**
     * 设置：对应 @Xparam.notNulls
     * 
     * @param notNulls 
     */
    public void setNotNulls(List<MethodReflect> i_NotNulls)
    {
        this.notNulls = i_NotNulls;
    }
    
}
