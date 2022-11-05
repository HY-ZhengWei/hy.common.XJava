package org.hy.common.xml.junit.app.interfaces;


import org.hy.common.Return;
import org.hy.common.xml.junit.app.bean.BarcodeGun;





/**
 * 扫码枪的网络消息的接口
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-19
 */
public interface IBarcodeGunMSG
{

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
    public Return<?> send(BarcodeGun i_BarcodeGun);

}
