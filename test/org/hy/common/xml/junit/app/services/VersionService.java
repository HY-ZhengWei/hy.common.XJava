package org.hy.common.xml.junit.app.services;

import org.hy.common.Execute;
import org.hy.common.Return;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.junit.app.bean.AppLoginUser;
import org.hy.common.xml.junit.app.bean.Version2;
import org.hy.common.xml.junit.app.common.BaseService;
import org.hy.common.xml.junit.app.interfaces.IVersionMSG;
import org.hy.common.xml.junit.app.interfaces.IVersionService;

/**
 * 用户枪消息操作业务
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-23
 */
@Xjava
public class VersionService extends BaseService implements IVersionService
{

    @Xjava
    private IVersionMSG versionMSG;



    public void execute_Version(Version2 i_Version)
    {
        new Execute(this ,"version" ,new Object[]{i_Version}).start();
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
    public void version(Version2 i_Version)
    {
        Return<?> v_Ret     = null;
        Version2  v_Version = null;

        v_Ret = this.versionMSG.version(i_Version);

        if ( v_Ret.booleanValue() )
        {
            v_Version = (Version2)v_Ret.paramObj;
            
            if ( v_Version != null )
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

}
