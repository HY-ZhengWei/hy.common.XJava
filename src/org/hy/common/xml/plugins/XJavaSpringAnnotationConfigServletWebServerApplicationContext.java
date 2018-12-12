package org.hy.common.xml.plugins;

import org.hy.common.file.FileHelp;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;




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
 * @createDate  2018-11-08
 * @version     v1.0  
 */
public class XJavaSpringAnnotationConfigServletWebServerApplicationContext extends AnnotationConfigServletWebServerApplicationContext
{
    
    /**
     * Create a new {@link AnnotationConfigServletWebServerApplicationContext} that needs
     * to be populated through {@link #register} calls and then manually
     * {@linkplain #refresh refreshed}.
     */
    public XJavaSpringAnnotationConfigServletWebServerApplicationContext() 
    {
        super(new XJavaSpringObjectFactotry());
        
        try
        {
            FileHelp v_FileHelp = new FileHelp();
            String v_SpringBoot_XJava = v_FileHelp.getContent(this.getClass().getResourceAsStream("SpringBoot-XJava.txt") ,"UTF-8" ,true);
            System.out.println(v_SpringBoot_XJava);
        }
        catch (Exception exce)
        {
            // Nothing.
        }
    }
    
}
