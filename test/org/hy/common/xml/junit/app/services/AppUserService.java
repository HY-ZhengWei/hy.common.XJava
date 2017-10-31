package org.hy.common.xml.junit.app.services;

import org.hy.common.Execute;
import org.hy.common.Return;
import org.hy.common.xml.junit.app.bean.AppLoginUser;
import org.hy.common.xml.junit.app.common.BaseService;
import org.hy.common.xml.junit.app.interfaces.IAppUserMSG;
import org.hy.common.xml.junit.app.interfaces.IAppUserService;

/**
 * 用户枪消息操作业务
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-23
 */
public class AppUserService extends BaseService implements IAppUserService
{

    private IAppUserMSG appUserMSG;



    public void execute_Login(AppLoginUser i_AppUser)
    {
        new Execute(this ,"login" ,new Object[]{i_AppUser}).start();
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
    public void login(AppLoginUser i_AppUser)
    {
        Return<?>    v_Ret     = null;
        AppLoginUser v_AppUser = null;

        v_Ret = this.appUserMSG.login(i_AppUser);

        if ( v_Ret.booleanValue() )
        {
            v_AppUser = (AppLoginUser)v_Ret.paramObj;
            
            if ( v_AppUser != null )
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



    public void setAppUserMSG(IAppUserMSG i_AppUserMSG)
    {
        this.appUserMSG = i_AppUserMSG;
    }



    public IAppUserMSG getAppUserMSG()
    {
        return this.appUserMSG;
    }

}
