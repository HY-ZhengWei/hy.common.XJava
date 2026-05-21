package org.hy.common.xml.junit;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Return;
import org.hy.common.xml.XHttp;
import org.junit.Test;





/**
 * 测试单元：网盘PUT方式
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-05-21
 * @version     v1.0
 */
public class JU_XHttp_Put
{
    
    @Test
    public void test_NextCloudCreateFile()
    {
        XHttp v_XHttp = new XHttp();
        
        v_XHttp.setProtocol("https");
        v_XHttp.setIp("127.0.0.1");
        v_XHttp.setUrl("/nextcloud/remote.php/dav/files/zhengwei/Share/演示文件.txt");
        v_XHttp.setContentType("text/plain; charset=utf-8");
        v_XHttp.setRequestType(XHttp.$Request_Type_Put);
        v_XHttp.setHaveQuestionMark(false);
        
        Map<String ,String> v_Head = new HashMap<String ,String>();
        v_Head.put("Authorization" ,"Basic 1234567890");
        
        Return<?> v_Ret = v_XHttp.request(new HashMap<String ,String>() ,"Hello world" ,v_Head);
        System.out.println(v_Ret);
    }
    
}
