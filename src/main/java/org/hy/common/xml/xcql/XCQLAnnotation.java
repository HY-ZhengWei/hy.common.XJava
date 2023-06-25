package org.hy.common.xml.xcql;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.XParamAnnotation;
import org.hy.common.xml.annotation.Xcql;
import org.hy.common.xml.annotation.Xparam;





/**
 * @Xcql 注解反射信息的操作类
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-06-24
 * @version     v1.0
 */
public class XCQLAnnotation implements Serializable
{

    private static final long serialVersionUID = 2734812841040994747L;

    private final Xcql             xcql;
    
    /** 方法入参信息。this.xcql.names() 将被整合在此属性中 */
    private List<XParamAnnotation> xparams;
    
    /** XJava对象池中的id */
    private String                 xid;
    
    
    
    public XCQLAnnotation(Method i_Method ,Xcql i_Xcql ,List<Xparam> i_Xparams) throws InstantiationException
    {
        this.xcql = i_Xcql;
        
        if ( Help.isNull(i_Xparams) )
        {
            if ( Help.isNull(this.xcql.names()) )
            {
                this.xparams = null;
            }
            else
            {
                this.xparams = new ArrayList<XParamAnnotation>(this.xcql.names().length);
                
                for (String v_Name : this.xcql.names())
                {
                    this.xparams.add(new XParamAnnotation(v_Name));
                }
            }
        }
        else
        {
            this.xparams = new ArrayList<XParamAnnotation>(Math.max(i_Xparams.size() ,this.xcql.names().length));
            
            for (int v_XPIndex=0; v_XPIndex<i_Xparams.size(); v_XPIndex++)
            {
                this.xparams.add(new XParamAnnotation(i_Method.getParameterTypes()[v_XPIndex] ,i_Xparams.get(v_XPIndex)));
            }
            
            if ( !Help.isNull(this.xcql.names()) )
            {
                for (int i=0; i<this.xcql.names().length; i++)
                {
                    String v_Name = this.xcql.names()[i];
                    
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
                                // @Xcql(names) 与 @Xparam 两注解定义的名称不一致，有冲突。
                                throw new InstantiationException("@Xcql(names[" + i + "]) = '" + v_Name + "' has a conflict with '" + this.xparams.get(i).getName() + "'.");
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
     * 获取：@Xcql 注解对象
     */
    public Xcql getXcql()
    {
        return xcql;
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
