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
    
}
