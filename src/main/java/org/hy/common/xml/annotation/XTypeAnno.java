package org.hy.common.xml.annotation;

import org.hy.common.ClassInfo;
import org.hy.common.xml.plugins.BaseInterface;





/**
 * 配合注释功能value关键字的实现（对应XJava中的实现 BaseInterface 接口的功能）
 * 
 * @author  ZhengWei(HY)
 * @version 2014-04-23
 */
public class XTypeAnno extends BaseInterface
{
    
    private Class<?> classObj;
    
    
    
    public XTypeAnno(XType i_XType)
    {
        this(i_XType ,null);
    }
    
    
    
    public XTypeAnno(XType i_XType ,ClassInfo i_ClassInfo)
    {
        super();
        
        if ( XType.XML == i_XType )
        {
            this.setRtype(1);
        }
        else if ( XType.NULL == i_XType )
        {
            throw new NullPointerException("XType is null.");
        }
        
        this.classObj = i_ClassInfo.getClassObj();
    }
    
    
    
    public void setClassInfo(ClassInfo i_ClassInfo)
    {
        this.classObj = i_ClassInfo.getClassObj();
    }
    
    
    
    @Override
    public void init()
    {
        super.init();
    }
    
    
    
    @Override
    protected Class<?> getXClass()
    {
        return this.classObj;
    }
    
}
