package org.hy.common.xml.plugins;

import org.hy.common.file.FileHelp;
import org.springframework.web.servlet.DispatcherServlet;





/**
 * XJava与Spring MVC融合。
 * 
 * 在web.xml配置Spring MVC时，只要将Servlet的类名改成本类即可。
 * 
 * 可实现使Spring MVC可以通过 @Autowired 或 @Resource 注解注入XJava对象池中的对象。
 * 
 * 举例web.xml中的配置如下
 * 
    <servlet>
        <servlet-name>mvcDispatcherForXJava</servlet-name>
        <servlet-class>org.hy.common.xml.plugins.XJavaSpringMVCDispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:xml/spring/dispatcher-servlet.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>mvcDispatcherForXJava</servlet-name>
        <url-pattern>*.page</url-pattern>
    </servlet-mapping>
 *
 *  
 * 只需要注意一点：XJava的对象池在在Spring的对象池初始前构造。即XJava优先于Spring。
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-09
 * @version     v1.0
 */
public class XJavaSpringMVCDispatcherServlet extends DispatcherServlet
{
    
    private static final long serialVersionUID = 7286355961045343129L;
    
    
    public XJavaSpringMVCDispatcherServlet()
    {
        super();
        
        this.setContextClass(XJavaSpringMVCAnnotationConfigServletWebServerApplicationContext.class);
        
        try
        {
            FileHelp v_FileHelp = new FileHelp();
            String v_SpringMVC_XJava = v_FileHelp.getContent(this.getClass().getResourceAsStream("SpringMVC-XJava.txt") ,"UTF-8" ,true);
            System.out.println(v_SpringMVC_XJava);
        }
        catch (Exception exce)
        {
            // Nothing.
        }
    }
    
}
