package org.hy.common.xml.plugins.analyse.checkimage.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.ListMap;





/**
 * 验证码图片信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-11-13
 * @version     v1.0
 */
public class CheckImageData implements Serializable
{
    
    private static final long serialVersionUID = 1676409423138845200L;
    
    
    
    /** 图片所属组 */
    private String                   groupCode;

    /** 图片编码 */
    private String                   imageCode;
    
    /** 图片可用名称的集合 */
    private List<String>             imageNames;
    
    /** 正向图片资源路径的集合。Map.key为图片ID，Map.value为图片URL */
    private ListMap<String ,String>  imageGoodUrls;
    
    /** 反向图片资源路径的集合。Map.key为图片ID，Map.value为图片URL */
    private ListMap<String ,String>  imageBadUrls;
    
    
    
    public CheckImageData()
    {
        this.imageNames    = new ArrayList<String>();
        this.imageGoodUrls = new ListMap<String ,String>();
        this.imageBadUrls  = new ListMap<String ,String>();
    }
    
    
    
    /**
     * 添加图片可用名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-11-13
     * @version     v1.0
     *
     * @param i_ImageName  图片可用名称
     * @return
     */
    public synchronized boolean putName(String i_ImageName)
    {
        if ( Help.isNull(i_ImageName) )
        {
            return false;
        }
        
        if ( this.imageNames == null )
        {
            this.imageNames = new ArrayList<String>();
        }
        
        this.imageNames.add(i_ImageName);
        return true;
    }
    
    
    
    /**
     * 添加图片资源地址
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-11-13
     * @version     v1.0
     *
     * @param i_ImageID   图片ID
     * @param i_ImageUrl  图片资源地址
     * @param i_IsGood    正向图片或反向图片
     * @return
     */
    public synchronized boolean putImageURL(String i_ImageID ,String i_ImageUrl ,boolean i_IsGood)
    {
        if ( Help.isNull(i_ImageID) || Help.isNull(i_ImageUrl) )
        {
            return false;
        }
        
        if ( i_IsGood )
        {
            if ( this.imageGoodUrls == null )
            {
                this.imageGoodUrls = new ListMap<String ,String>();
            }
            
            this.imageGoodUrls.put(i_ImageID ,i_ImageUrl);
        }
        else
        {
            if ( this.imageBadUrls == null )
            {
                this.imageBadUrls = new ListMap<String ,String>();
            }
            
            this.imageBadUrls.put(i_ImageID ,i_ImageUrl);
        }
        
        return true;
    }

    
    
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
     * @param i_GroupCode
     */
    public void setGroupCode(String i_GroupCode)
    {
        this.groupCode = i_GroupCode;
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
     * @param i_ImageCode
     */
    public void setImageCode(String i_ImageCode)
    {
        this.imageCode = i_ImageCode;
    }

    
    /**
     * 获取：图片可用名称的集合
     */
    public List<String> getImageNames()
    {
        return imageNames;
    }

    
    /**
     * 设置：图片可用名称的集合
     * 
     * @param i_ImageNames
     */
    public void setImageNames(List<String> i_ImageNames)
    {
        this.imageNames = i_ImageNames;
    }

    
    /**
     * 获取：正向图片资源路径的集合。Map.key为图片ID，Map.value为图片URL
     */
    public ListMap<String ,String> getImageGoodUrls()
    {
        return imageGoodUrls;
    }

    
    /**
     * 设置：正向图片资源路径的集合。Map.key为图片ID，Map.value为图片URL
     * 
     * @param i_ImageGoodUrls
     */
    public void setImageGoodUrls(ListMap<String ,String> i_ImageGoodUrls)
    {
        this.imageGoodUrls = i_ImageGoodUrls;
    }

    
    /**
     * 获取：反向图片资源路径的集合。Map.key为图片ID，Map.value为图片URL
     */
    public ListMap<String ,String> getImageBadUrls()
    {
        return imageBadUrls;
    }

    
    /**
     * 设置：反向图片资源路径的集合。Map.key为图片ID，Map.value为图片URL
     * 
     * @param i_ImageBadUrls
     */
    public void setImageBadUrls(ListMap<String ,String> i_ImageBadUrls)
    {
        this.imageBadUrls = i_ImageBadUrls;
    }
    
}
