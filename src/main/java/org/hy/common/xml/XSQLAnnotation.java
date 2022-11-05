package org.hy.common.xml;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.annotation.Xparam;
import org.hy.common.xml.annotation.Xsql;





/**
 * @Xsql 注解反射信息的操作类
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-12-15
 * @version     v1.0
 */
public class XSQLAnnotation implements Serializable
{
    
    private static final long serialVersionUID = 6644097435172438039L;
    

    private final Xsql             xsql;
    
    /** 方法入参信息。this.xsql.names() 将被整合在此属性中 */
    private List<XParamAnnotation> xparams;
    
    /** XJava对象池中的id */
    private String                 xid;
    
    
    
    public XSQLAnnotation(Method i_Method ,Xsql i_Xsql ,List<Xparam> i_Xparams) throws InstantiationException
    {
        this.xsql = i_Xsql;
        
        if ( Help.isNull(i_Xparams) )
        {
            if ( Help.isNull(this.xsql.names()) )
            {
                this.xparams = null;
            }
            else
            {
                this.xparams = new ArrayList<XParamAnnotation>(this.xsql.names().length);
                
                for (String v_Name : this.xsql.names())
                {
                    this.xparams.add(new XParamAnnotation(v_Name));
                }
            }
        }
        else
        {
            this.xparams = new ArrayList<XParamAnnotation>(Math.max(i_Xparams.size() ,this.xsql.names().length));
            
            for (int v_XPIndex=0; v_XPIndex<i_Xparams.size(); v_XPIndex++)
            {
                this.xparams.add(new XParamAnnotation(i_Method.getParameterTypes()[v_XPIndex] ,i_Xparams.get(v_XPIndex)));
            }
            
            if ( !Help.isNull(this.xsql.names()) )
            {
                for (int i=0; i<this.xsql.names().length; i++)
                {
                    String v_Name = this.xsql.names()[i];
                    
                    if ( i < this.xparams.size() )
                    {
                        if ( Help.isNull(this.xparams.get(i).getName()) )
                        {
                            this.xparams.get(i).setName(v_Name);
                        }
                        else if ( !Help.isNull(v_Name) )
                        {
                            if ( !v_Name.equals(this.xparams.get(i).getName()) )
                            {
                                // @Xsql(names) 与 @Xparam 两注解定义的名称不一致，有冲突。 
                                throw new InstantiationException("@Xsql(names[" + i + "]) = '" + v_Name + "' has a conflict with '" + this.xparams.get(i).getName() + "'.");
                            }
                        }
                    }
                    else
                    {
                        this.xparams.add(new XParamAnnotation(v_Name));
                    }
                }
            }
        }
    }
    
    
    
    /**
     * 获取：@Xsql 注解对象
     */
    public Xsql getXsql()
    {
        return xsql;
    }

    
    
    /**
     * 获取：方法入参信息
     */
    public List<XParamAnnotation> getXparams()
    {
        return xparams;
    }
    

    
    /**
     * 获取：XJava对象池中的id
     */
    public String getXid()
    {
        return xid;
    }
    

    
    /**
     * 设置：XJava对象池中的id
     * 
     * @param xid 
     */
    public void setXid(String xid)
    {
        this.xid = xid;
    }
    
}
