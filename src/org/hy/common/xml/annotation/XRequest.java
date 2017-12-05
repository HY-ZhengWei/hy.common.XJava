package org.hy.common.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * Web请求接口的注解。
 * 
 *   可取代 sys.AppInterfaces.xml 配置文件。如下配置信息
 *   
     <import name="appInterface" class="org.hy.common.xml.plugins.AppInterface" />
     <interfaces id="AppInterfaces" key="name">
        <appInterface>
            <name>I001V001</name>
            <className>org...model.Version</className>
            <emName>VersionWeb.version</emName>
        </appInterface>
     </interfaces>
 *
 * 
 *  服务端接口的定义如下：

    @Xjava
    public class 类名
    {
        @XRequest("接口标识")
        public AppMessage<Object> 方法名称(AppMessage<实际传参的对象类型> i_AppMsg)
        {
            ...
        }
    }
 * 
 * 
 *  客户端接口的调用如下：
 
    Retrun<响应的对象类型> v_Ret = BaseMessage的实现类.sendMsg("XHttp对象的ID" 
                                                           ,"@XRequest接口标识" 
                                                           ,版本号 
                                                           ,实际传参的对象实例);
 * 
 * 
 * @author      ZhengWei(HY)
 * @createDate  2017-12-04
 * @version     v1.0
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XRequest
{
    
    /**
     * 接口惟一标识
     */
    public String value() default "";  
    
}
