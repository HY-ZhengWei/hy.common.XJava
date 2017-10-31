package org.hy.common.xml.junit.app.msg;

import org.hy.common.Return;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.junit.app.bean.AppLoginUser;
import org.hy.common.xml.junit.app.common.BaseMSG;
import org.hy.common.xml.junit.app.interfaces.IAppUserMSG;

/**
 * 用户的网络消息
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-23
 */
@Xjava
public class AppUserMSG extends BaseMSG implements IAppUserMSG
{

    public AppUserMSG()
    {
        super();
    }



    /**
     * 接口名称：用户登录接口
     * 接口编号：I006U001
     * 接口版本：1
     *
     * @param i_AppUser
     * @return 返回错误信息
     *            -1: 系统错误，附加错误信息或返回报文
     *            21: 服务端返回错误信息，附加返回的错误信息
     *            22: 非法接口调用
     *         返回成功信息
     *             paramObj  为用户对象
     */
    public Return login(AppLoginUser i_AppUser)
    {
        return this.sendMsg("XHTTP_BarcodeGun" ,$SID_Login ,1 ,i_AppUser);
    }

}
