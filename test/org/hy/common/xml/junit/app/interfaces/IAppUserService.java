package org.hy.common.xml.junit.app.interfaces;

import org.hy.common.xml.junit.app.bean.AppLoginUser;




/**
 * 用户消息操作业务的接口
 *
 * @author  ZhengWei(HY)
 * @version 2017-10-23
 */
public interface IAppUserService
{

    public void execute_Login(AppLoginUser i_AppUser);

}
