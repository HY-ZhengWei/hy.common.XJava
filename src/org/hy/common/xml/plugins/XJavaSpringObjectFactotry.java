package org.hy.common.xml.plugins;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.hy.common.Help;
import org.hy.common.xml.XJava;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodParameter;





/**
 * XJava对接Spring Boot的第1步（共4步），使Spring可以通过 @Autowired 或 @Resource 注解注入XJava对象池中的对象。
 * 
 * 第2步见：XJavaFactoryBeanDefinition。主要实现 @Resource 注解的注入时对象的构造。
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
 * 自定义 BeanFactory 主要实现：Spring构造非Spring自身对象失败时，交给XJava来尝试构造。
 * 
 * XJava可通过两类4种方式来实现Spring的注入(支持属性及方法的自动化注入)。
 *     @Autowired有三种方式，其优先级为：方式1 > 方式2 > 方式3。
 *     方式1有排他性，即使用方式1构造未成功时，方式2、方式3也不再尝试构造。
 *     
 *     @Resource有一种方式：方式4
 * 
 * 
 *     方式1：@Autowired + @Qualifier("XJava对象池中对象的XID")，如下
 *           
 *           @Autowired
 *           @Qualifier("XSQL_Query")
 *           private XSQL xsql;                  // 通过XJava.getObject("XSQL_Query")注入
 *           
 *           @Autowired
 *           public void setXSQL(@Qualifier("XSQL_Query")XSQL i_XSQL)
 *           {
 *                                               // 通过XJava.getObject("XSQL_Query")注入
 *           }
 *           
 *     方式2：@Autowired + 属性名称（或方法参数名称）： 属性名称（或方法参数名称）及为XJava对象池中对象的XID，如下
 *           
 *           @Autowired
 *           private UserDAO userDAO;            // 通过XJava.getObject("userDAO")注入。
 *           
 *           @Autowired
 *           public void setDAO(UserDAO userDAO)
 *           {
 *                                               // 通过XJava.getObject("userDAO")注入。
 *           }
 *           
 *     方式3：@Autowired + 属性类型（或方法参数类型）： 用属性类型（或方法参数类型）在XJava对象池中匹配，如下
 *     
 *           @Autowired
 *           private UserDAO dao;                // 通过XJava.getObject(UserDAO.class)注入。
 *           
 *           @Autowired
 *           public void setDAO(UserDAO dao)
 *           {
 *              // 通过XJava.getObject(UserDAO.class)注入。
 *           }
 *           
 *     方式4：@Resource(name="XJava对象池中对象的XID") ，如下
 *     
 *           @Resource(name="XSQL_Query")
 *           private DataSourceGroup xsql;       // 通过XJava.getObject("XSQL_Query")注入
 *           
 *           @Resource(name="XSQL_Query")        // 通过XJava.getObject("XSQL_Query")注入
 *           public void setXSQL(XSQL i_XSQL);
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-11-08
 * @version     v1.0  
 */
public class XJavaSpringObjectFactotry extends DefaultListableBeanFactory
{
    
    /** 用于性能的提高 */
    private Map<String ,BeanDefinition> beanDefinitionCache = new Hashtable<String ,BeanDefinition>();
    
    
    
    @Override
    public BeanDefinition getBeanDefinition(String i_BeanName) throws NoSuchBeanDefinitionException 
    {
        BeanDefinition v_Ret = null;
        
        try
        {
            v_Ret = super.getBeanDefinition(i_BeanName);
        }
        catch (NoSuchBeanDefinitionException exec)
        {
            Object v_XObject = XJava.getObject(i_BeanName ,false);
            if ( v_XObject == null )
            {
                throw exec;
            }
            
            v_Ret = objectFactotryByXJava(i_BeanName ,v_XObject);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * Spring构造非Spring自身对象失败时，交给XJava来尝试构造。
     * 
     * 主要实现：@Resource 注解方式的注入。
     * 
     * 注：此方法可以不加同步锁，大不了重复覆盖创建多次相同的BeanDefinition
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-11-08
     * @version     v1.0  
     * 
     * @param i_XID
     * @param i_XObject
     * @return
     */
    private BeanDefinition objectFactotryByXJava(String i_XID ,Object i_XObject)
    {
        AnnotatedGenericBeanDefinition v_Ret = (AnnotatedGenericBeanDefinition)beanDefinitionCache.get(i_XID);
        
        if ( v_Ret == null )
        {
            ConstructorArgumentValues v_ConstructorArgs = new ConstructorArgumentValues();
            v_ConstructorArgs.addGenericArgumentValue(i_XID);
            
            v_Ret = new AnnotatedGenericBeanDefinition(i_XObject.getClass());
            v_Ret.setFactoryBeanName("XJavaFactoryBeanDefinition");
            v_Ret.setFactoryMethodName("getObject");
            v_Ret.setConstructorArgumentValues(v_ConstructorArgs);
            
            beanDefinitionCache.put(i_XID ,v_Ret);
        }
        
        return v_Ret;
    }



    @Override
    public Object doResolveDependency(DependencyDescriptor i_Descriptor ,String i_BeanName ,Set<String> i_AutowiredBeanNames ,TypeConverter i_TypeConverter) throws BeansException
    {
        Object v_Ret = null;
        try
        {
            v_Ret = super.doResolveDependency(i_Descriptor ,i_BeanName ,i_AutowiredBeanNames ,i_TypeConverter);
        }
        catch (BeansException exce)
        {
            v_Ret = objectFactotryByXJava(i_Descriptor);
            
            if ( v_Ret == null )
            {
                exce.printStackTrace();
                throw exce;
            }
        }
        
        return v_Ret; 
    }
    
    
    
    /**
     * Spring构造非Spring自身对象失败时，交给XJava来尝试构造。
     * 
     * 主要实现：@Autowired 注解方式的注入
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-11-08
     * @version     v1.0  
     * 
     * @param i_Descriptor
     * @return
     */
    private Object objectFactotryByXJava(DependencyDescriptor i_Descriptor)
    {
        if ( i_Descriptor == null )
        {
            return null;
        }
        
        Object    v_Ret       = null;
        Qualifier v_Qualifier = null;
        
        if ( !Help.isNull(i_Descriptor.getAnnotations()) )
        {
            for (Annotation v_Annotation : i_Descriptor.getAnnotations())
            {
                if ( v_Annotation instanceof Qualifier )
                {
                    v_Qualifier = (Qualifier)v_Annotation;
                }
            }
        }
        
        Field           v_Field  = i_Descriptor.getField();
        MethodParameter v_MParam = i_Descriptor.getMethodParameter();
        
        if ( v_Qualifier != null && !Help.isNull(v_Qualifier.value()) )
        {
            // 方式1：@Autowired + @Qualifier ：@Qualifier("XJava对象池中对象的XID")
            v_Ret = XJava.getObject(v_Qualifier.value());
        }
        else if ( v_Field != null )
        {
            // 方式2：@Autowired + 属性名称： 属性名称及为XJava对象池中对象的XID
            v_Ret = XJava.getObject(v_Field.getName());
            
            if ( v_Ret == null )
            {
                // 方式3：@Autowired + 属性类型： 用属性类型在XJava对象池中匹配
                v_Ret = XJava.getObject(v_Field.getType());
            }
        }
        else if ( v_MParam != null )
        {
            // 方式2：@Autowired + 方法参数名称： 方法参数名称及为XJava对象池中对象的XID
            v_Ret = XJava.getObject(v_MParam.getParameterName());
            
            if ( v_Ret == null )
            {
                // 方式3：@Autowired + 方法参数类型： 用方法参数类型在XJava对象池中匹配
                v_Ret = XJava.getObject(v_MParam.getParameterType());
            }
        }
        
        return v_Ret;
    }
    
}
