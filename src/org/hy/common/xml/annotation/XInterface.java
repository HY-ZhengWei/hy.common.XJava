package org.hy.common.xml.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * Web接口的注解。
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
 * @author      ZhengWei(HY)
 * @createDate  2017-12-04
 * @version     v1.0
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XInterface
{
    
    /**
     * 接口惟一标识
     */
    public String value() default "";  
    
}
