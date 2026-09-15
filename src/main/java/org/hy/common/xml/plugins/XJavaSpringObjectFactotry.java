package org.hy.common.xml.plugins;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.hy.common.Help;
import org.hy.common.xml.XJava;
import org.hy.common.xml.log.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;





/**
 * XJava与Spring Boot融合。
 *   下面描述了4个步骤，但最终用户在使用时，也只须要实现第4步，即可。
 *   即，只须Spring Boot启动时，引用一下XJavaSpringAnnotationConfigServletWebServerApplicationContext就成了。
 * 
 * XJava对接Spring Boot的第1步（共4步），使Spring可以通过 @Autowired 或 @Resource 注解注入XJava对象池中的对象。
 * 
 * 第2步见：XJavaFactoryBeanDefinition。主要实现 @Resource 注解的注入时对象的构造。
 * 第3步见：XJavaSpringAnnotationConfigServletWebServerApplicationContext类。主要用于构造Spring对象工厂。
 * 第4步见：@SpringBootApplication 注解的Spring Boot工程启动类，由最终用户编写（最终用户只用实现此即可），如下：
 * 
    @SpringBootApplication
    public class ProjectStart extends SpringBootServletInitializer
    {
        public static void main(String[] i_Args)
        {
            SpringApplication v_SpringApp = new SpringApplication(ProjectStart.class);
            v_SpringApp.setApplicationContextClass(XJavaSpringAnnotationConfigServletWebServerApplicationContext.class);
            v_SpringApp.setApplicationContextFactory(ApplicationContextFactory.ofContextClass(XJavaSpringAnnotationConfigServletWebServerApplicationContext.class));  // 2.4.0 之后的新版本
            ConfigurableApplicationContext v_CAC = v_SpringApp.run(i_Args);
        }
        
        // Tomcat方式部署及启动Spring Boot（可选的）
        // 1. 继承 SpringBootServletInitializer
        // 2. 重写 run(...) 方法
        // 3. 配置 web.xml 等。此步为常规方法，网上有很多资料，具体配置内容不在赘述
        @Overrid
        protected WebApplicationContext run(SpringApplication i_Application)
        {
            i_Application.setApplicationContextClass(XJavaSpringAnnotationConfigServletWebServerApplicationContext.class);
            
            return (WebApplicationContext) i_Application.run();
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
 * 
 * 注意两点：
 *    1. XJava的对象池在在Spring的对象池初始前构造。即XJava优先于Spring。
 *    2. （已失效，同时支持两种版本）Spring 5.x 的版本与 Spring 3.x 的版本中 doResolveDependency() 方法的入参个数不同，
 *       而Spring并没有做兼容性处理，所以在两个不版本下，要重新引用不同版本的SpringBean.jar包到工程，
 *       并且重新编译XJava.jar。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-11-08
 * @version     v1.0
 *              v2.0  2019-06-26  添加：不再专门编译支持Spring3.x和Spring5.x等两个版本的jar包了。
 *                                      可以只编译一次，就能全动态反射的支持两个版本。
 *                                      使用者引用哪个版本的Spring，XJava就支持那个版本。
 *              v3.0  2024-02-22  添加：适配JDK 17
 */
public class XJavaSpringObjectFactotry extends DefaultListableBeanFactory
{
    
    private static final Logger $Logger = new Logger(XJavaSpringObjectFactotry.class);
    
    
    
    /** 用于性能的提高 */
    private Map<String ,BeanDefinition> beanDefinitionCache = new Hashtable<String ,BeanDefinition>();
    
    /** 适用于多个Spring版本的doResolveDependency()方法 */
    private MethodHandle                doResolveDependency = null;
    
    /** 配对的Spring版本 */
    private String                      version             = "";
    
    
    
    public XJavaSpringObjectFactotry()
    {
        super();
        this.findDoResolveDependency();
    }
    
    
    
    public XJavaSpringObjectFactotry(@Nullable BeanFactory parentBeanFactory)
    {
        super(parentBeanFactory);
        this.findDoResolveDependency();
    }
    
    
    
    /**
     * 查找具体运行时环境下的Spring版本对应的doResolveDependency()方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-10
     * @version     v1.0
     *              v2.0  2024-02-22  添加：JDK 17、11、8等各个版本均适配的方法
     */
    private void findDoResolveDependency()
    {
        try
        {
            Method    v_DoResolveDependencyMethod = null;
            Method [] v_Methods                   = DefaultListableBeanFactory.class.getDeclaredMethods();
            for (Method v_Method : v_Methods)
            {
                if ( v_Method.getName().equals("doResolveDependency") )
                {
                    v_DoResolveDependencyMethod = v_Method;
                    break;
                }
            }
            if ( v_DoResolveDependencyMethod == null )
            {
                $Logger.error("DefaultListableBeanFactory is not find doResolveDependency.");
                throw new RuntimeException("DefaultListableBeanFactory is not find doResolveDependency.");
            }
            
            v_DoResolveDependencyMethod.setAccessible(true);
            
            MethodHandles.Lookup v_Lookup = MethodHandles.lookup();
            
            if ( v_DoResolveDependencyMethod.getParameterCount() == 4 )
            {
                this.version             = "5.x";
                this.doResolveDependency = v_Lookup.findSpecial(DefaultListableBeanFactory.class
                                                               ,"doResolveDependency"
                                                               ,MethodType.methodType(Object.class ,new Class[]{DependencyDescriptor.class ,String.class ,Set.class ,TypeConverter.class})
                                                               ,XJavaSpringObjectFactotry.class);
            }
            else if ( v_DoResolveDependencyMethod.getParameterCount() == 5 )
            {
                this.version             = "3.x";
                this.doResolveDependency = v_Lookup.findSpecial(DefaultListableBeanFactory.class
                                                               ,"doResolveDependency"
                                                               ,MethodType.methodType(Object.class ,new Class[]{DependencyDescriptor.class ,Class.class ,String.class ,Set.class ,TypeConverter.class})
                                                               ,XJavaSpringObjectFactotry.class);
            }
            else
            {
                throw new RuntimeException("DefaultListableBeanFactory.doResolveDependency is unknown Spring version.");
            }
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            throw new RuntimeException("DefaultListableBeanFactory.doResolveDependency is unknown Spring version.");
        }
    }
    
    
    
    /**
     * 查找具体运行时环境下的Spring版本对应的doResolveDependency()方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-10
     * @version     v1.0
     *              v2.0  2024-02-22  丢用：JDK 17 不再允许 allowedModes 属性的访问，因而丢用本方法
     */
    @Deprecated
    @SuppressWarnings("unused")
    private void findDoResolveDependency_JDK11_JDK8()
    {
        try
        {
            MethodHandles.Lookup v_Lookup = MethodHandles.lookup();
            
            Field allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            allowedModes.setAccessible(true);
            allowedModes.set(v_Lookup, -1);
            
            try
            {
                this.doResolveDependency = v_Lookup.findSpecial(DefaultListableBeanFactory.class
                                                               ,"doResolveDependency"
                                                               ,MethodType.methodType(Object.class ,new Class[]{DependencyDescriptor.class ,String.class ,Set.class ,TypeConverter.class})
                                                               ,XJavaSpringObjectFactotry.class);
                
                this.version = "5.x";
            }
            catch (Throwable exce)
            {
                this.doResolveDependency = v_Lookup.findSpecial(DefaultListableBeanFactory.class
                                                               ,"doResolveDependency"
                                                               ,MethodType.methodType(Object.class ,new Class[]{DependencyDescriptor.class ,Class.class ,String.class ,Set.class ,TypeConverter.class})
                                                               ,XJavaSpringObjectFactotry.class);

                this.version = "3.x";
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            throw new RuntimeException("DefaultListableBeanFactory.doResolveDependency is unknown Spring version.");
        }
    }
    
    
    
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



    /**
     * 适用于Spring 5.x版本的
     *
     * @author      ZhengWei(HY)
     * @createDate  2018-11-09
     * @version     v1.0
     *
     * @param i_Descriptor
     * @param i_BeanName
     * @param i_AutowiredBeanNames
     * @param i_TypeConverter
     * @return
     * @throws BeansException
     *
     * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
     */
    @Override
    public Object doResolveDependency(DependencyDescriptor i_Descriptor
                                     ,String               i_BeanName
                                     ,Set<String>          i_AutowiredBeanNames
                                     ,TypeConverter        i_TypeConverter) throws BeansException
    {
        Object v_Ret = null;
        try
        {
            // System.err.println("请重新引Spring 5.x对应版本的Bean包，并重新编译XJava，好支持Spring 5.x版本");
            // v_Ret = super.doResolveDependency(i_Descriptor ,i_BeanName ,i_AutowiredBeanNames ,i_TypeConverter);
            v_Ret = this.doResolveDependency.invoke(this ,i_Descriptor ,i_BeanName ,i_AutowiredBeanNames ,i_TypeConverter);
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
        catch (Throwable exce)
        {
            exce.printStackTrace();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 适用于Spring 3.x版本的
     *
     * @author      ZhengWei(HY)
     * @createDate  2018-12-10
     * @version     v1.0
     *
     * @param i_Descriptor
     * @param i_Type
     * @param i_BeanName
     * @param i_AutowiredBeanNames
     * @param i_TypeConverter
     * @return
     * @throws BeansException
     *
     * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
     */
    protected Object doResolveDependency(DependencyDescriptor i_Descriptor
                                        ,Class<?>             i_Type
                                        ,String               i_BeanName
                                        ,Set<String>          i_AutowiredBeanNames
                                        ,TypeConverter        i_TypeConverter) throws BeansException
    {
        Object v_Ret = null;
        try
        {
            // System.err.println("请重新引Spring 3.x对应版本的Bean包，并重新编译XJava，好支持Spring 3.x版本");
            v_Ret = this.doResolveDependency.invoke(this ,i_Descriptor ,i_Type ,i_BeanName ,i_AutowiredBeanNames ,i_TypeConverter);
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
        catch (Throwable exce)
        {
            exce.printStackTrace();
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


    
    /**
     * 获取：配对的Spring版本
     */
    public String getVersion()
    {
        return version;
    }
    
}
