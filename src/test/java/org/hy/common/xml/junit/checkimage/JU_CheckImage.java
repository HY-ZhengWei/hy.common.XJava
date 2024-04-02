package org.hy.common.xml.junit.checkimage;

import java.util.List;

import org.hy.common.StringHelp;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.analyse.checkimage.v2.CheckImageInfo;
import org.hy.common.xml.plugins.analyse.checkimage.v2.CheckImageReturn;
import org.hy.common.xml.plugins.analyse.checkimage.v2.CheckImageUtil;
import org.junit.Test;





/**
 * 测试单元：图片验证码
 *
 * @author      ZhengWei(HY)
 * @createDate  2022-11-14
 * @version     v1.0
 */
@Xjava(value=XType.XML)
public class JU_CheckImage
{
    
    private static final Logger $Logger = new Logger(JU_CheckImage.class ,true);
    
    private static boolean      $isInit = false;
    
    
    
    public JU_CheckImage() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
 
    
    
    /**
     * 测试推理点图
     * 
     * @author      ZhengWei(HY)
     * @createDate  2022-11-14
     * @version     v1.0
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_V2()
    {
        List<CheckImageInfo> v_CImageList     = (List<CheckImageInfo>) XJava.getObject("CheckImages");
        CheckImageUtil       v_CheckImageUtil = new CheckImageUtil();
        
        for (CheckImageInfo v_CImage : v_CImageList)
        {
            v_CheckImageUtil.putImage(v_CImage);
        }
        
        CheckImageReturn v_CImageRet = v_CheckImageUtil.makeCheckImage("wwwww_people");
        
        $Logger.info(                    v_CImageRet.getCheckImageBase64());
        $Logger.info(StringHelp.toString(v_CImageRet.getCheckImageName()));
        $Logger.info(StringHelp.toString(v_CImageRet.getCheckImageIndex()));
    }
    
}
