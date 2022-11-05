package org.hy.common.xml.junit;

import org.hy.common.StringHelp;
import org.hy.common.xml.XHttp;
import org.junit.Test;





public class JU_XHttp_Get
{
    
    @Test
    public void test_requestGet()
    {
        System.out.println(XHttp.requestGet("http://192.168.1.117:8180/avplan/wzyb/attendance_parameters.jsp?leavehs=0&leaveds=2&leaveStart=2017/11/13%2009:00&leaveEnd=2017/11/14%2009:00&createTime=2017/11/13%2009:00&reason=11&orgcode=4fc1e0132fe7df56c027c2d849aaa1b6&orgname=财务部&userno=1234567890&username=" + StringHelp.bytesToHex("小明".getBytes())));
    }
    
    
    
    @Test
    public void test_Request()
    {
        for (int i=0; i<100; i++)
        {
            this.request();
        }
    }
    
    
    
    public void request()
    {
        String v_URL = "http://10.1.50.234:8080/avplan/wzyb/AprroveFlow.jsp?userNO=52258&domainRef=25&agreeFlag=NO&postScript=1234567890&objIID=a523601c07204889ac4f3040b96d3906&module=purchaseCountersign&mainObjType=purchaseCountersign&objectName=11C0610G02-001";
        
        v_URL = "https://www.baidu.com";
        
        System.out.println(XHttp.requestGet(v_URL));
    }
    
}
