package org.hy.common.xml.plugins;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;





/**
 * 在Spring Boot 2.4.0 的版本之后，将使用 ApplicationContextFactory 代替ApplicationContext的创建。
 * 
 *   2.4.0 之前的老版本
 *   v_SpringApp.setApplicationContextClass(XJavaSpringAnnotationConfigServletWebServerApplicationContext.class);
 *   
 *   2.4.0 之后的新版本
 *   v_SpringApp.setApplicationContextFactory(XJavaSpringApplicationContextFactory.DEFAULT);
 *   
 * @author      ZhengWei(HY)
 * @createDate  2021-02-19
 * @version     v1.0
 */
public interface XJavaSpringApplicationContextFactory 
{
    
    ApplicationContextFactory DEFAULT = (webApplicationType) -> 
    {
        try 
        {
            switch (webApplicationType) 
            {
                case SERVLET:
                    return new XJavaSpringMVCAnnotationConfigServletWebServerApplicationContext();
                case REACTIVE:
                    return new AnnotationConfigReactiveWebServerApplicationContext();
                default:
                    return new AnnotationConfigApplicationContext();
            }
        }
        catch (Exception ex) 
        {
            throw new IllegalStateException("Unable create a default ApplicationContext instance, " + "you may need a custom ApplicationContextFactory", ex);
        }
    };
    
}
