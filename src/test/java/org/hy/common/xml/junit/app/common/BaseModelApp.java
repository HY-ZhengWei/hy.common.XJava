package org.hy.common.xml.junit.app.common;

import org.hy.common.xml.SerializableDef;





/**
 * App基础信息类
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-09-04
 * @version     v1.0
 */
public class BaseModelApp extends SerializableDef
{
    
    private static final long serialVersionUID = 6980139745121612124L;

    /** 设备号 */
    private String deviceNo;

    /** 设备类型 */
    private String deviceType = "android";

    
    
    /**
     * 获取：设备号
     */
    public String getDeviceNo()
    {
        return deviceNo;
    }
    

    
    /**
     * 设置：设备号
     * 
     * @param deviceNo 
     */
    public void setDeviceNo(String deviceNo)
    {
        this.deviceNo = deviceNo;
    }
    

    
    /**
     * 获取：设备类型
     */
    public String getDeviceType()
    {
        return deviceType;
    }
    

    
    /**
     * 设置：设备类型
     * 
     * @param deviceType 
     */
    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }
    
}
