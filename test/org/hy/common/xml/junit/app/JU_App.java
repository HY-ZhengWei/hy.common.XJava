package org.hy.common.xml.junit.app;

import org.hy.common.Date;
import org.hy.common.xml.XJava;
import org.hy.common.xml.junit.app.bean.AppLoginUser;
import org.hy.common.xml.junit.app.bean.BarcodeGun;
import org.hy.common.xml.junit.app.bean.Version2;
import org.hy.common.xml.junit.app.config.InitConfig;
import org.hy.common.xml.junit.app.interfaces.IAppUserService;
import org.hy.common.xml.junit.app.interfaces.IBarcodeGunService;
import org.hy.common.xml.junit.app.interfaces.IVersionService;
import org.junit.Test;





public class JU_App
{
    
    public JU_App()
    {
        new InitConfig();
    }
    
    
    
    public void test_BarcodeGun() throws InterruptedException
    {
        BarcodeGun v_Data = new BarcodeGun();

        v_Data.setDeviceNo("867246023785125");
        v_Data.setData("ZhengWei(HY)");
        v_Data.setDataType("1D");
        
        ((IBarcodeGunService) XJava.getObject("BarcodeGunService")).execute_Send(v_Data);
        
        Thread.sleep(1000 * 60);
    }
    
    
    
    public void test_AppLogin() throws InterruptedException
    {
        AppLoginUser v_Data = new AppLoginUser();

        v_Data.setDeviceNo("867246023785125");
        v_Data.setLoginName("admin");
        v_Data.setLoginType("mes");
        v_Data.setLoginPassword("");
        v_Data.setLoginTime(new Date());
        
        ((IAppUserService) XJava.getObject("AppUserService")).execute_Login(v_Data);
        
        Thread.sleep(1000 * 60);
    }
    
    
    
    @Test
    public void test_Version() throws InterruptedException
    {
        Version2 v_Data = new Version2();

        v_Data.setDeviceNo("867246023785125");
        
        ((IVersionService) XJava.getObject("VersionService")).execute_Version(v_Data);
        
        Thread.sleep(1000 * 60);
    }
    
}
