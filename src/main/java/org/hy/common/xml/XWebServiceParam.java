package org.hy.common.xml;

import java.util.Map;





/**
 * WebService服务的客户端调用(访问)类中的请求参数接口。
 * 
 * 将Java实例对象转为XWebService可以理解的参数形式。
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2013-07-08
 */
public interface XWebServiceParam
{
    
    public Map<String ,Object> getXWSParam();
    
}
