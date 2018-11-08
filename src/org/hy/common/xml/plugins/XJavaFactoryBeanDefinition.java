package org.hy.common.xml.plugins;

import org.hy.common.xml.XJava;
import org.springframework.stereotype.Component;





/**
 * XJava对接Spring Boot的第2步（共4步），使Spring可以通过 @Autowired 或 @Resource 注解注入XJava对象池中的对象。
 * 
 * 第1步见：XJavaSpringObjectFactotry类。有详见的注入方式说明。
 * 第3步见：XJavaSpringAnnotationConfigServletWebServerApplicationContext类。主要用于构造Spring对象工厂。
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
 * 主要实现 @Resource 注解的注入时对象的构造。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-11-08
 * @version     v1.0  
 */
@Component
public class XJavaFactoryBeanDefinition
{
    
    public Object getObject(String i_XID)
    {
        return XJava.getObject(i_XID);
    }
    
}
