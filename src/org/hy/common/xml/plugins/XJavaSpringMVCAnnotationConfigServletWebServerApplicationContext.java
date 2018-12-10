package org.hy.common.xml.plugins;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;





/**
 * XJava对接Spring Boot的第3步（共4步），使Spring可以通过 @Autowired 或 @Resource 注解注入XJava对象池中的对象。
 * 
 * 第1步见：XJavaSpringObjectFactotry类。有详见的注入方式说明。
 * 第2步见：XJavaFactoryBeanDefinition。主要实现 @Resource 注解的注入时对象的构造。
 * 第4步见：@SpringBootApplication 注解的Spring Boot工程启动类，由最终用户编写（最终用户只用实现此即可），如下：
 * 
    @SpringBootApplication
    public class ProjectStart 
    {
        public static void main(String[] i_Args) 
        {
            SpringApplication v_SpringApp = new SpringApplication(ProjectStart.class);
            v_SpringApp.setApplicationContextClass(XJavaSpringAnnotationConfigServletWebServerApplicationContext.class);
            ConfigurableApplicationContext v_CAC = v_SpringApp.run(i_Args);
        }
    }
 * 
 * 自定义 ApplicationContext 主要实现：构建改造后的Spring对象工厂DefaultListableBeanFactory
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-12-09
 * @version     v1.0  
 */
public class XJavaSpringMVCAnnotationConfigServletWebServerApplicationContext extends XmlWebApplicationContext
{
    
    public XJavaSpringMVCAnnotationConfigServletWebServerApplicationContext() 
    {
        super();
    }
    
    
    protected DefaultListableBeanFactory createBeanFactory() 
    {
        return new XJavaSpringObjectFactotry(getInternalParentBeanFactory());
    }
    
}
