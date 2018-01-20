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
 *  1. 与 AppBaseServlet.java 配合使用。
 * 
 *  2. 服务端接口的定义如下：

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
 *  3. 客户端接口的调用如下：
 
        Retrun<响应的对象类型> v_Ret = BaseMessage的实现类.sendMsg("XHttp对象的ID" 
                                                               ,"@XRequest接口标识" 
                                                               ,版本号 
                                                               ,实际传参的对象实例);
 * 
 * 所有注册的接口可通过它查看： http://IP:Port/服务名/analyses/analyseObject?xid=AppInterfaces
 * 
 * @author      ZhengWei(HY)
 * @createDate  2017-12-04
 * @version     v1.0
 *              v2.0  2018-01-20  添加：用注解定义各个系统的接口级消息密钥。
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
    
    
    
    /**
     * 接口惟一标识。与 this.value() 同义
     */
    public String id() default "";
    
    
    
    /**
     * 各个系统的接口级消息密钥。
     * 
     * 数组中每个元素表示访问接口的访问系统的密钥，即每个调用方的密钥可以各不相同。
     * 
     * 格式如下
     *    1. 用:分号分割系统编号和密钥
     *       如 this.secret = {["sysID系统编号01:密钥01"] ,["sysID系统编号02:密钥02"]}
     *       
     *    2. 用=等号分割系统编号和密钥
     *       如 this.secret = {["sysID系统编号01=密钥01"] ,["sysID系统编号02=密钥02"]}
     * 
     * 可设置各别调用方，无密钥访问。如下格式
     *       如 this.secret = {["sysID系统编号01="] ,["sysID系统编号02"]}
     *       上面两调用方法均能无密钥访问
     *       
     * 当此属性无值时，表示所有系统均能无密钥访问（前提是：系统级消息密钥也没有定义）。
     */
    public String [] secrets();
    
}
