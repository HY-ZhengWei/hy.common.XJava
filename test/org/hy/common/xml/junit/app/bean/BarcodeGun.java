package org.hy.common.xml.junit.app.bean;

import org.hy.common.Date;
import org.hy.common.xml.junit.app.common.BaseBean;





/**
 * 扫码枪信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-10-19
 * @version     v1.0
 */
public class BarcodeGun extends BaseBean
{

    private static final long serialVersionUID = 3192286494348848621L;
    
    
    /** 数据信息 */
    private String data;
    
    /** 数据类型 */
    private String dataType;
    
    /** 数据时间 */
    private Date   dataTime;
    
    
    
    public BarcodeGun()
    {
        this.dataTime = new Date();
    }
    
    
    /**
     * 获取：数据信息
     */
    public String getData()
    {
        return data;
    }
    

    
    /**
     * 设置：数据信息
     * 
     * @param data 
     */
    public void setData(String data)
    {
        this.data = data;
    }
    

    
    /**
     * 获取：数据类型
     */
    public String getDataType()
    {
        return dataType;
    }
    

    
    /**
     * 设置：数据类型
     * 
     * @param dataType 
     */
    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }


    
    /**
     * 获取：数据时间
     */
    public Date getDataTime()
    {
        return dataTime;
    }

    
    
    /**
     * 设置：数据时间
     * 
     * @param dataTime 
     */
    public void setDataTime(Date dataTime)
    {
        this.dataTime = dataTime;
    }
    
}
