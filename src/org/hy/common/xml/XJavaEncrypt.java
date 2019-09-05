package org.hy.common.xml;





/**
 * 加密XML配置文件中的对象属性
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-09-04
 * @version     v1.0
 */
public class XJavaEncrypt
{
    
    /** 节点名称  */
    private String nodeName;
    
    /** 父节点的ID */
    private String superID;
    
    /** 加密属性的值 */
    private String encrypt;
    
    /** 节点的值 */
    private String value;
    
    /** 节点加密的值 */
    private String valueEncrypt;

    
    
    /**
     * 获取：节点名称
     */
    public String getNodeName()
    {
        return nodeName;
    }

    
    /**
     * 获取：父节点的ID
     */
    public String getSuperID()
    {
        return superID;
    }

    
    /**
     * 获取：加密属性的值
     */
    public String getEncrypt()
    {
        return encrypt;
    }

    
    /**
     * 获取：节点的值
     */
    public String getValue()
    {
        return value;
    }

    
    /**
     * 获取：节点加密的值
     */
    public String getValueEncrypt()
    {
        return valueEncrypt;
    }

    
    /**
     * 设置：节点名称
     * 
     * @param nodeName 
     */
    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    
    /**
     * 设置：父节点的ID
     * 
     * @param superID 
     */
    public void setSuperID(String superID)
    {
        this.superID = superID;
    }

    
    /**
     * 设置：加密属性的值
     * 
     * @param encrypt 
     */
    public void setEncrypt(String encrypt)
    {
        this.encrypt = encrypt;
    }

    
    /**
     * 设置：节点的值
     * 
     * @param value 
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    
    /**
     * 设置：节点加密的值
     * 
     * @param valueEncrypt 
     */
    public void setValueEncrypt(String valueEncrypt)
    {
        this.valueEncrypt = valueEncrypt;
    }
    
}
