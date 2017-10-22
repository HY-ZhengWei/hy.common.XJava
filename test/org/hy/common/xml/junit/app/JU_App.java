package org.hy.common.xml.junit.app;

import org.hy.common.xml.XJava;
import org.hy.common.xml.junit.app.bean.BarcodeGun;
import org.hy.common.xml.junit.app.config.InitConfig;
import org.hy.common.xml.junit.app.interfaces.IBarcodeGunService;
import org.junit.Test;





public class JU_App
{
    
    @Test
    public void test_BarcodeGun() throws InterruptedException
    {
        new InitConfig();
        
        BarcodeGun v_Data = new BarcodeGun();

        v_Data.setDeviceNo("867246023785125");
        v_Data.setData("ZhengWei(HY)");
        v_Data.setDataType("1D");
        
        ((IBarcodeGunService) XJava.getObject("BarcodeGunService")).execute_Send(v_Data);
        
        Thread.sleep(1000 * 60);
    }
    
}
