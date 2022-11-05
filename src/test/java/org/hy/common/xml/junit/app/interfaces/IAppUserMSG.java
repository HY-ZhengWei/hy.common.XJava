package org.hy.common.xml.junit.app.interfaces;



import org.hy.common.Return;
import org.hy.common.xml.junit.app.bean.AppLoginUser;

/**
 * 用户的网络消息的接口
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-23
 */
public interface IAppUserMSG
{

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
    public Return<?> login(AppLoginUser i_AppUser);

}
