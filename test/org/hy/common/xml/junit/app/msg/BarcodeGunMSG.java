package org.hy.common.xml.junit.app.msg;


import org.hy.common.Return;
import org.hy.common.xml.junit.app.bean.BarcodeGun;
import org.hy.common.xml.junit.app.common.BaseMSG;
import org.hy.common.xml.junit.app.interfaces.IBarcodeGunMSG;





/**
 * 扫码枪的网络消息
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-19
 */
public class BarcodeGunMSG extends BaseMSG implements IBarcodeGunMSG
{

    public BarcodeGunMSG()
    {
        super();
    }



    /**
     * 接口名称：扫码枪接口
     * 接口编号：I005B001
     * 接口版本：1
     *
     * @param i_BarcodeGun
     * @return 返回错误信息
     *            -1: 系统错误，附加错误信息或返回报文
     *            21: 服务端返回错误信息，附加返回的错误信息
     *            22: 非法接口调用
     *         返回成功信息
     *             paramObj  为扫码枪对象
     */
    public Return<?> send(BarcodeGun i_BarcodeGun)
    {
        return this.sendMsg("XHTTP_BarcodeGun" ,$SID_BarcodeGun ,1 ,i_BarcodeGun);
    }

}
