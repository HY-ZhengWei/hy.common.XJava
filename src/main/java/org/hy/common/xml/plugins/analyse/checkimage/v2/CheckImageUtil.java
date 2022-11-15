package org.hy.common.xml.plugins.analyse.checkimage.v2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.ListMap;
import org.hy.common.TablePartitionRID;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.log.Logger;





/**
 * 图形验证码：推理点图
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-11-12
 * @version     v1.0
 */
public class CheckImageUtil
{
    
    private static final Logger                              $Logger     = Logger.getLogger(CheckImageUtil.class);
    
    /** 验证图片组。Map.分区为图片组，Map.key为图片编码 */
    private static TablePartitionRID<String ,CheckImageData> $Images     = new TablePartitionRID<String ,CheckImageData>();
    
    /** 验证图片的缓存 */
    private static Map<String ,BufferedImage>                $ImageBuffs = new HashMap<String ,BufferedImage>();
    
    
    
    /** 推理点图的正向图片的数量 */
    private int size;
    
    /** 推理点图中每张图片的宽度 */
    private int imageWidth;
    
    /** 推理点图中每张图片的高度 */
    private int imageHeight;
    
    
    
    public CheckImageUtil()
    {
        this.size        = 3;
        this.imageWidth  = 320;
        this.imageHeight = 380;
    }
    
    
    
    /**
     * 填充图片资源
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-11-13
     * @version     v1.0
     *
     * @param i_CImage
     * @return
     */
    public synchronized boolean putImage(CheckImageInfo i_CImage)
    {
        if ( i_CImage == null )
        {
            return false;
        }
        
        if ( Help.isNull(i_CImage.getGroupCode()) )
        {
            return false;
        }
        
        if ( Help.isNull(i_CImage.getImageID()) )
        {
            return false;
        }
        
        if ( Help.isNull(i_CImage.getImageCode()) )
        {
            return false;
        }
        
        if ( Help.isNull(i_CImage.getImageUrl()) )
        {
            return false;
        }
        
        CheckImageData v_CImageData = $Images.getRow(i_CImage.getGroupCode() ,i_CImage.getImageCode());
        if ( v_CImageData == null )
        {
            v_CImageData = new CheckImageData();
            v_CImageData.setGroupCode(i_CImage.getGroupCode());
            v_CImageData.setImageCode(i_CImage.getImageCode());
            
            $Images.putRow(v_CImageData.getGroupCode() ,v_CImageData.getImageCode() ,v_CImageData);
        }
        
        if ( !Help.isNull(i_CImage.getImageName()) )
        {
            v_CImageData.putName(i_CImage.getImageName());
        }
        
        v_CImageData.putImageURL(i_CImage.getImageID() ,i_CImage.getImageUrl() ,i_CImage.isGood());
        
        return true;
    }
    
    
    
    /**
     * 获取图片缓存对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-11-13
     * @version     v1.0
     *
     * @param i_ImageID   图片ID
     * @param i_ImageURL  图片资源路径
     * @return
     */
    private synchronized BufferedImage getImageBuff(String i_ImageID ,String i_ImageURL)
    {
        BufferedImage v_Ret = $ImageBuffs.get(i_ImageID);
        
        if ( v_Ret != null )
        {
            return v_Ret;
        }
        
        try
        {
            v_Ret = FileHelp.getContentImage(i_ImageURL);
            v_Ret = new FileHelp().resizeImage(v_Ret ,this.imageWidth ,this.imageHeight);
            if ( v_Ret != null )
            {
                $ImageBuffs.put(i_ImageID ,v_Ret);
            }
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            return null;
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 生成图片验证的图片
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-11-13
     * @version     v1.0
     *
     * @param i_GroupCode  图片验证码的组编码
     * @return
     */
    public CheckImageReturn makeCheckImage(String i_GroupCode)
    {
        Map<String ,CheckImageData> v_ImageCodeMap = $Images.get(i_GroupCode);
        if ( Help.isNull(v_ImageCodeMap) )
        {
            return null;
        }
        
        if ( v_ImageCodeMap.size() < this.size )
        {
            return null;
        }
        
        List<CheckImageData>        v_ImageCodeList  = Help.toList(v_ImageCodeMap);
        Map<String ,CheckImageData> v_CImageSelected = new LinkedHashMap<String ,CheckImageData>();
        int                         v_Count          = 0;
        
        // 第一步：随机选出三个不重复的图片
        while ( v_CImageSelected.size() < this.size && v_Count <= 100 )
        {
            CheckImageData v_CImageData = v_ImageCodeList.get(Help.random(0 ,v_ImageCodeList.size() - 1));
            
            if ( !Help.isNull(v_CImageData.getImageNames())
              && !Help.isNull(v_CImageData.getImageGoodUrls())
              && !Help.isNull(v_CImageData.getImageBadUrls()) )
            {
                v_CImageSelected.put(v_CImageData.getImageCode() ,v_CImageData);
            }
            
            v_Count++;
        }
        
        if ( v_CImageSelected.size() < this.size )
        {
            return null;
        }
        
        // 第二步：从每张图片中随机选出：名称、正向图片、反向图片，并确定逻辑推理名称的顺序
        String [] v_RetNames  = new String[this.size];
        String [] v_ImageIDs  = new String[this.size * 2];
        String [] v_ImageURLs = new String[this.size * 2];
        
        v_ImageCodeList = Help.toList(v_CImageSelected);
        for (int x=0; x<this.size; x++)
        {
            CheckImageData v_CImageData = v_ImageCodeList.get(x);
            
            v_RetNames [x]         = v_CImageData.getImageNames()   .get(   Help.random(0 ,v_CImageData.getImageNames()   .size() - 1));
            v_ImageIDs [x * 2]     = v_CImageData.getImageGoodUrls().getKey(Help.random(0 ,v_CImageData.getImageGoodUrls().size() - 1));
            v_ImageIDs [x * 2 + 1] = v_CImageData.getImageBadUrls() .getKey(Help.random(0 ,v_CImageData.getImageBadUrls() .size() - 1));
            v_ImageURLs[x * 2]     = v_CImageData.getImageGoodUrls().get(v_ImageIDs[x * 2]);
            v_ImageURLs[x * 2 + 1] = v_CImageData.getImageBadUrls() .get(v_ImageIDs[x * 2 + 1]);
        }
        
        // 第三步：随机排序第二步中的图片位置
        ListMap<String ,Integer> v_OrderBys = new ListMap<String ,Integer>();     // Map.key为图片ID，Map.value为图片在第二步生成顺序索引号
        while ( v_OrderBys.size() < v_ImageIDs.length )
        {
            int    v_ImageIndex = Help.random(0 ,v_ImageIDs.length - 1);
            String v_ImageID    = v_ImageIDs[v_ImageIndex];
            
            if ( !v_OrderBys.containsKey(v_ImageID) )
            {
                v_OrderBys.put(v_ImageID ,v_ImageIndex);
            }
        }
        
        // 第四步：确定逻辑推理的鼠标点击顺序，同时拼接所有图片为一张大图
        int []        v_RetImageOrderBy = new int[this.size];
        int           v_SpacePXSize     = 3;                                                           // 图片间的间隔空隙
        int           v_BigImageWidth   = this.imageWidth  * this.size + v_SpacePXSize * (this.size - 1);
        int           v_BigImageHeight  = this.imageHeight * 2 + v_SpacePXSize;
        BufferedImage v_RetBigImage     = new BufferedImage(v_BigImageWidth ,v_BigImageHeight ,BufferedImage.TYPE_INT_ARGB);
        Graphics2D    v_BigGraphics     = v_RetBigImage.createGraphics();
        
        v_BigGraphics.setColor(Color.WHITE);
        v_BigGraphics.fillRect(0 ,0 ,v_BigImageWidth ,v_BigImageHeight);
        
        for (int x=0 ,v_RowIndex=0 ,v_ColIndex=0; x<v_OrderBys.size(); x++)
        {
            int v_ImageIndex = v_OrderBys.get(x);
            if ( v_ImageIndex % 2 == 0 )
            {
                // 只有正向图片的索引
                v_RetImageOrderBy[v_ImageIndex / 2] = x;
            }
            
            String        v_ImageID   = v_ImageIDs [v_ImageIndex];
            String        v_ImageURL  = v_ImageURLs[v_ImageIndex];
            BufferedImage v_ImageBuff = this.getImageBuff(v_ImageID ,v_ImageURL);
            
            FileHelp.overAlphaImage(v_RetBigImage
                                   ,v_ImageBuff
                                   ,v_RowIndex * this.imageWidth  + v_RowIndex * v_SpacePXSize
                                   ,v_ColIndex * this.imageHeight + v_ColIndex * v_SpacePXSize
                                   ,1.0f);
            if ( (++v_RowIndex) >= this.size )
            {
                v_RowIndex = 0;
                v_ColIndex++;
            }
        }
        
        // 第五步：返回验证码图片信息
        try
        {
            CheckImageReturn v_Ret = new CheckImageReturn();
            
            v_Ret.setCheckImageBase64(FileHelp.getContentImageBase64(v_RetBigImage));
            v_Ret.setCheckImageName(v_RetNames);
            v_Ret.setCheckImageIndex(v_RetImageOrderBy);
            
            return v_Ret;
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
        }
        
        return null;
    }


    
    /**
     * 获取：推理点图的正向图片的数量
     */
    public int getSize()
    {
        return size;
    }


    
    /**
     * 设置：推理点图的正向图片的数量
     * 
     * @param i_Size 推理点图的正向图片的数量
     */
    public void setSize(int i_Size)
    {
        this.size = i_Size;
    }


    
    /**
     * 获取：推理点图中每张图片的宽度
     */
    public int getImageWidth()
    {
        return imageWidth;
    }



    /**
     * 设置：推理点图中每张图片的宽度
     * 
     * @param i_ImageWidth 推理点图中每张图片的宽度
     */
    public void setImageWidth(int i_ImageWidth)
    {
        this.imageWidth = i_ImageWidth;
    }



    /**
     * 获取：推理点图中每张图片的高度
     */
    public int getImageHeight()
    {
        return imageHeight;
    }


    
    /**
     * 设置：推理点图中每张图片的高度
     * 
     * @param i_ImageHeight 推理点图中每张图片的高度
     */
    public void setImageHeight(int i_ImageHeight)
    {
        this.imageHeight = i_ImageHeight;
    }
    
}
