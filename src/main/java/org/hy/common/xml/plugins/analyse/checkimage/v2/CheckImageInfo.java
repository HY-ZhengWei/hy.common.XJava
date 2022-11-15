package org.hy.common.xml.plugins.analyse.checkimage.v2;

import java.io.Serializable;





/**
 * 验证码图片信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-11-13
 * @version     v1.0
 */
public class CheckImageInfo implements Serializable
{
    
    private static final long serialVersionUID = 1539918900784906957L;

    
    
    /** 图片所属组 */
    private String               groupCode;
    
    /** 图片ID */
    private String               imageID;

    /** 图片编码 */
    private String               imageCode;
    
    /** 图片可用名称的集合 */
    private String               imageName;
    
    /** 图片资源路径 */
    private String               imageUrl;
    
    /** 图片正向或反向身份 */
    private boolean              isGood;

    
    
    /**
     * 获取：图片所属组
     */
    public String getGroupCode()
    {
        return groupCode;
    }

    
    /**
     * 设置：图片所属组
     * 
     * @param i_GroupCode 图片所属组
     */
    public void setGroupCode(String i_GroupCode)
    {
        this.groupCode = i_GroupCode;
    }

    
    /**
     * 获取：图片ID
     */
    public String getImageID()
    {
        return imageID;
    }

    
    /**
     * 设置：图片ID
     * 
     * @param i_ImageID 图片ID
     */
    public void setImageID(String i_ImageID)
    {
        this.imageID = i_ImageID;
    }

    
    /**
     * 获取：图片编码
     */
    public String getImageCode()
    {
        return imageCode;
    }

    
    /**
     * 设置：图片编码
     * 
     * @param i_ImageCode 图片编码
     */
    public void setImageCode(String i_ImageCode)
    {
        this.imageCode = i_ImageCode;
    }

    
    /**
     * 获取：图片可用名称的集合
     */
    public String getImageName()
    {
        return imageName;
    }

    
    /**
     * 设置：图片可用名称的集合
     * 
     * @param i_ImageName 图片可用名称的集合
     */
    public void setImageName(String i_ImageName)
    {
        this.imageName = i_ImageName;
    }

    
    /**
     * 获取：图片资源路径
     */
    public String getImageUrl()
    {
        return imageUrl;
    }

    
    /**
     * 设置：图片资源路径
     * 
     * @param i_ImageUrl 图片资源路径
     */
    public void setImageUrl(String i_ImageUrl)
    {
        this.imageUrl = i_ImageUrl;
    }

    
    /**
     * 获取：图片正向或反向身份
     */
    public boolean isGood()
    {
        return isGood;
    }

    
    /**
     * 设置：图片正向或反向身份
     * 
     * @param i_IsGood 图片正向或反向身份
     */
    public void setGood(boolean i_IsGood)
    {
        this.isGood = i_IsGood;
    }
    
}
