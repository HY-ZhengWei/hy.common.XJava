package org.hy.common.xml.junit.app.services;

import org.hy.common.Execute;
import org.hy.common.Return;
import org.hy.common.xml.junit.app.bean.BarcodeGun;
import org.hy.common.xml.junit.app.common.BaseService;
import org.hy.common.xml.junit.app.interfaces.IBarcodeGunMSG;
import org.hy.common.xml.junit.app.interfaces.IBarcodeGunService;





/**
 * 扫码枪消息操作业务
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-19
 */
public class BarcodeGunService extends BaseService implements IBarcodeGunService
{

    private IBarcodeGunMSG barcodeGunMSG;



    public void execute_Send(BarcodeGun i_BarcodeGun)
    {
        new Execute(this ,"sendBarcode" ,new Object[]{i_BarcodeGun}).start();
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
    public void sendBarcode(BarcodeGun i_BarcodeGun)
    {
        Return<?>  v_Ret        = null;
        BarcodeGun v_BarcodeGun = null;

        v_Ret = this.barcodeGunMSG.send(i_BarcodeGun);

        if ( v_Ret.booleanValue() )
        {
            v_BarcodeGun = (BarcodeGun)v_Ret.paramObj;
            
            if ( v_BarcodeGun != null )
            {
                System.out.println("成功");
            }
            else
            {
                System.out.println("未成功");
            }
        }
        else
        {
            System.out.println("未成功");
        }
    }



    public void setBarcodeGunMSG(IBarcodeGunMSG i_BarcodeGunMSG)
    {
        this.barcodeGunMSG = i_BarcodeGunMSG;
    }



    public IBarcodeGunMSG getBarcodeGunMSG()
    {
        return this.barcodeGunMSG;
    }

}
