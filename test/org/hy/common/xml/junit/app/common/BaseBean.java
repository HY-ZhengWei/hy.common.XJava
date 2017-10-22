package org.hy.common.xml.junit.app.common;


import org.hy.common.xml.SerializableDef;





/**
 * 基础数据对象
 * 
 * @author  ZhengWei(HY)
 * @version 2013-11-15
 */
public class BaseBean extends SerializableDef
{
    private static final long serialVersionUID = 8345081941751162817L;

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
        return this.deviceType;
    }



    /**
     * 设置：设备类型
     *
     * @param i_DeviceType
     */
    public void setDeviceType(String i_DeviceType)
    {
        this.deviceType = i_DeviceType;
    }

}
