package org.hy.common.xml.plugins.analyse;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.hy.common.Return;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.log.Logger;





/**
 * 图形验证码
 *
 * @author      ZhengWei(HY)
 * @createDate  2021-05-18
 * @version     v1.0
 */
public class CheckImageUtil
{
    private static final Logger  $Logger         = Logger.getLogger(CheckImageUtil.class);
    
    /** 剪切轮廓 */
    private static Return<?>[][] $CutOutlineData = null;
    
    /** 贴图数据 */
    private static BufferedImage $CssOutLine     = null;
    
    /** 验证的图片 */
    private static BufferedImage $BgImage        = null;
    
    
    
    /**
     * 生成图片验证的图片
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-08-02
     * @version     v1.0
     * 
     * @param i_X  生成验证图片的坐标X
     * @param i_Y  生成验证图片的坐标X
     * @return     [0]: 验证图片的背景图
     *             [1]: 验证图片的前景图
     */
    public String[] makeCheckImage(int i_X ,int i_Y)
    {
        try
        {
            synchronized ( this )
            {
                if ( $BgImage == null )
                {
                    Color  v_TransparentColor = new Color(255 ,255 ,255);
                    String v_Package          = "/org/hy/common/xml/plugins/analyse/windows/image/";
                    
                    $CutOutlineData = FileHelp.getImageOutline(ImageIO.read(getClass().getResource(v_Package + "CheckImageTemplateA.png")) ,v_TransparentColor.getRGB());
                    $CssOutLine     = ImageIO.read(getClass().getResource(v_Package + "CheckImageTemplateB.png"));
                    $BgImage        = ImageIO.read(getClass().getResource(v_Package + "CheckImage.png"));
                }
            }
            
            BufferedImage v_NewBig   = new BufferedImage($BgImage.getWidth()    ,$BgImage.getHeight()      ,BufferedImage.TYPE_INT_ARGB);
            BufferedImage v_NewSmall = new BufferedImage($CutOutlineData.length ,$CutOutlineData[0].length ,BufferedImage.TYPE_INT_ARGB);
            
            FileHelp.copyImage($BgImage ,v_NewBig);
            FileHelp.cutImage( $BgImage ,v_NewSmall ,$CutOutlineData ,i_X ,i_Y);
            FileHelp.overAlphaImage(     v_NewBig   ,$CssOutLine     ,i_X ,i_Y ,0.5F);
            
            return new String[] {FileHelp.getContentImageBase64(v_NewBig) ,FileHelp.getContentImageBase64(v_NewSmall)};
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
        }
        
        return null;
    }
    
}
