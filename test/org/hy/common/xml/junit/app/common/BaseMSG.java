package org.hy.common.xml.junit.app.common;

import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.BaseMessage;





/**
 * 基础访问网络的消息
 * 
 * @author  ZhengWei(HY)
 * @version 2013-11-14
 */
public class BaseMSG extends BaseMessage
{


    /** 接口编码：获取最新版本信息 */
    protected static final String $SID_Version             = "I001V002";

    /** 接口编码：上传二维码 */
    protected static final String $SID_QRCode              = "I002Q001";

    /** 接口编码：上传拍照 */
    protected static final String $SID_Photo               = "I003P001";

    /** 接口编码：拍照选型 */
    protected static final String $SID_PhotoXX             = "I004P002";

    /** 接口编码：扫码枪 */
    protected static final String $SID_BarcodeGun          = "I005B001";
    


    public BaseMSG()
    {
        super();
    }



    @Override
    protected String getSysID()
    {
        return XJava.getParam("SYSID").getValue();
    }



    @Override
    protected boolean isDebug()
    {
        return true;
    }

}
