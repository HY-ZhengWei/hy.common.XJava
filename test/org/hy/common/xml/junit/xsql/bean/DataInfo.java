package org.hy.common.xml.junit.xsql.bean;





/**
 * 数据库的值对象
 *
 * @author      ZhengWei(HY)
 * @createDate  2020-10-27
 * @version     v1.0
 */
public class DataInfo
{
    
    /** 字节数组 */
    private byte [] bytes;

    
    
    /**
     * 获取：字节数组
     */
    public byte [] getBytes()
    {
        return bytes;
    }

    
    /**
     * 设置：字节数组
     * 
     * @param bytes 
     */
    public void setBytes(byte [] bytes)
    {
        this.bytes = bytes;
    }
    
}
