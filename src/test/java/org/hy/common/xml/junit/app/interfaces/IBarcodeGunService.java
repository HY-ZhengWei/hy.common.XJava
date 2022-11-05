package org.hy.common.xml.junit.app.interfaces;

import org.hy.common.xml.junit.app.bean.BarcodeGun;





/**
 * 扫码枪消息操作业务的接口
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-19
 */
public interface IBarcodeGunService
{

    public void execute_Send(BarcodeGun i_BarcodeGun);

}
