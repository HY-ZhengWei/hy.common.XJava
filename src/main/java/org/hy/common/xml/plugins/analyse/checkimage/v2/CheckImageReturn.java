package org.hy.common.xml.plugins.analyse.checkimage.v2;

import java.io.Serializable;





/**
 * 验证码图片生成方法返回的数据对象
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-11-14
 * @version     v1.0
 */
public class CheckImageReturn implements Serializable
{

    private static final long serialVersionUID = -5657975621102295543L;
    
    
    
    /** 验证码图片的Base64数据 */
    private String    checkImageBase64;
    
    /** 验证码图片的推理文字名称 */
    private String [] checkImageName;
    
    /** 验证码图片的推理正确的点击序号。全部图片阵列为两行（每行为 checkImageIndex.length / 2 列），序号从零开始，依次为从左到右，从上到下编号 */
    private int    [] checkImageIndex;

    
    
    /**
     * 获取：验证码图片的Base64数据
     */
    public String getCheckImageBase64()
    {
        return checkImageBase64;
    }

    
    /**
     * 设置：验证码图片的Base64数据
     * 
     * @param i_CheckImageBase64 验证码图片的Base64数据
     */
    public void setCheckImageBase64(String i_CheckImageBase64)
    {
        this.checkImageBase64 = i_CheckImageBase64;
    }

    
    /**
     * 获取：验证码图片的推理文字名称
     */
    public String [] getCheckImageName()
    {
        return checkImageName;
    }

    
    /**
     * 设置：验证码图片的推理文字名称
     * 
     * @param i_CheckImageName 验证码图片的推理文字名称
     */
    public void setCheckImageName(String [] i_CheckImageName)
    {
        this.checkImageName = i_CheckImageName;
    }

    
    /**
     * 获取：验证码图片的推理正确的点击序号。全部图片阵列为两行（每行为 checkImageIndex.length / 2 列），序号从零开始，依次为从左到右，从上到下编号
     */
    public int [] getCheckImageIndex()
    {
        return checkImageIndex;
    }

    
    /**
     * 设置：验证码图片的推理正确的点击序号。全部图片阵列为两行（每行为 checkImageIndex.length / 2 列），序号从零开始，依次为从左到右，从上到下编号
     * 
     * @param i_CheckImageIndex 验证码图片的推理正确的点击序号。全部图片阵列为两行（每行为 checkImageIndex.length / 2 列），序号从零开始，依次为从左到右，从上到下编号
     */
    public void setCheckImageIndex(int [] i_CheckImageIndex)
    {
        this.checkImageIndex = i_CheckImageIndex;
    }
    
}
