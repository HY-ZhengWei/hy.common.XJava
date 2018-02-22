package org.hy.common.xml.junit.xsqlcloud;

import org.hy.common.Date;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;






/**
 * 测试单元：XSQL云计算。请用独立的进程开启，用以模拟服务器02
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-02-22
 * @version     v1.0
 */
@Xjava(value=XType.XML)
public class Server02
{
    private static boolean $isInit = false;
    
    
    
    public Server02() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
            new JU_XSQLCloud();
        }
    }
    
    
    
    public static void main(String [] i_Agrs) throws Exception
    {
        new Server02();
        System.out.println("-- " + Date.getNowTime().getFullMilli() + " 服务02开启");
        
        try
        {
            Thread.sleep(1000 * 60 * 60);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
}
