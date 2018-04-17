package org.hy.common.xml.junit.template;

import org.hy.common.xml.SerializableDef;





public class JavaInfo extends SerializableDef
{

    private static final long serialVersionUID = 4253762421469507188L;
    
    
    /** 包名称 */
    private String packageName;
    
    /** 类名称 */
    private String className;
    
    /** 类注释 */
    private String classDesc;
    
    /** 作者 */
    private String author;
    
    /** 创建时间 */
    private String createDate;
    
    /** 类的所有属性 */
    private String attributes;
    
    /** 类的所有方法 */
    private String methods;

    
    
    /**
     * 获取：包名称
     */
    public String getPackageName()
    {
        return packageName;
    }

    
    /**
     * 设置：包名称
     * 
     * @param packageName 
     */
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    
    /**
     * 获取：类名称
     */
    public String getClassName()
    {
        return className;
    }

    
    /**
     * 设置：类名称
     * 
     * @param className 
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    
    /**
     * 获取：类注释
     */
    public String getClassDesc()
    {
        return classDesc;
    }

    
    /**
     * 设置：类注释
     * 
     * @param classDesc 
     */
    public void setClassDesc(String classDesc)
    {
        this.classDesc = classDesc;
    }

    
    /**
     * 获取：作者
     */
    public String getAuthor()
    {
        return author;
    }

    
    /**
     * 设置：作者
     * 
     * @param author 
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    
    /**
     * 获取：创建时间
     */
    public String getCreateDate()
    {
        return createDate;
    }

    
    /**
     * 设置：创建时间
     * 
     * @param createDate 
     */
    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }

    
    /**
     * 获取：类的所有属性
     */
    public String getAttributes()
    {
        return attributes;
    }

    
    /**
     * 设置：类的所有属性
     * 
     * @param attributes 
     */
    public void setAttributes(String attributes)
    {
        this.attributes = attributes;
    }

    
    /**
     * 获取：类的所有方法
     */
    public String getMethods()
    {
        return methods;
    }

    
    /**
     * 设置：类的所有方法
     * 
     * @param methods 
     */
    public void setMethods(String methods)
    {
        this.methods = methods;
    }
    
}
