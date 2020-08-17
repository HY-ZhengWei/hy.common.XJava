package org.hy.common.xml;

import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.hy.common.ClassInfo;
import org.hy.common.ClassReflect;
import org.hy.common.Date;
import org.hy.common.Des;
import org.hy.common.ExpireMap;
import org.hy.common.FieldReflect;
import org.hy.common.Help;
import org.hy.common.ListMap;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.Return;
import org.hy.common.StaticReflect;
import org.hy.common.StringHelp;
import org.hy.common.TablePartitionRID;
import org.hy.common.TreeMap;
import org.hy.common.TreeNode;
import org.hy.common.XJavaID;
import org.hy.common.app.Param;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.annotation.XRequest;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.XTypeAnno;
import org.hy.common.xml.annotation.Xjava;
import org.hy.common.xml.plugins.AppInterface;
import org.hy.common.xml.plugins.XRule;
import org.hy.common.xml.plugins.XSQLGroup;





/**
 * 解释Xml文件，并且可以直接将Xml文件内容转为Java对象实例。
 * 
 * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例。
 * 
 * 解释"注解 Annotation"，转为Java对象实例
 * 
 * 
 * 注：Xml配置文件类型与"注解 Annotation"可以同时运用在项目中。
 * 
 * @author      ZhengWei(HY) 
 * @createDate  2012-09-13
 * @version     v1.0
 *              v1.1  2015-10-27  添加：XJava给对象赋值时，入参是Java元类型(Class.class)的功能
 *              v1.2  2016-02-16  添加：填充Web服务根目录的标记 "webhome:"
 *              v1.3  2017-01-16  添加：$SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
 *              v1.4  2017-02-13  修正：对构造器的参数类型判定方法上，引用 MethodReflect.isExtendImplement(...) 方法提高识别的准确性。
 *              v1.5  2017-10-31  添加：支持枚举名称的匹配 
 *              v1.6  2017-11-23  添加：支持注解赋值时，对无Setter方法的成员属性赋值，即使是private属性也能赋值。
 *                                     当成员属性有Setter方法时，用Setter方法优先。
 *              v1.7  2017-11-24  添加：支持XML赋值时，对无Setter方法的成员属性赋值，即使是private属性也能赋值。
 *                                     当成员属性有Setter方法时，用Setter方法优先。
 *                                优化：getObject(Class)方法，在if语句判定时，不创建全新对象实例进行判定。
 *                                添加：扩展getObject(Class)方法的功能，尝试在实现类、子类中查找匹配的对象。
 *              v1.8  2017-12-05  添加：@XRequest注解，Web请求接口的解释功能。
 *              v1.9  2017-12-15  添加：@Xsql注解，XSQL、XSQLGroup的注解。用于 xml配置 + Java接口类(无须实现类)的组合实现持久层的功能。
 *              v1.10 2018-01-20  添加：@XRequest注解添加对secrets()属性的支持。用注解定义各个系统的接口级消息密钥。
 *              v1.11 2018-01-29  添加：对部分异常日志，添加更详细的说明。
 *              v1.12 2018-03-09  添加：将配置在XML配置文件中的ID值，自动赋值给Java实例对象。须实现接口 org.hy.common.XJavaID 才有效。
 *              v1.13 2018-05-04  添加：支持Setter方法重载情况下的XML解析赋值。
 *              v1.14 2019-09-04  添加：支持XML配置中特定的属性加密，并且当为明文时，程序启后将自动重写为密文保存在配置中。建议人：邹德福
 */
public final class XJava
{
	private final static String                    $XML_JAVA_DATATYPE_CHAR       = "char";
	
	private final static String                    $XML_JAVA_DATATYPE_BYTE       = "byte";
	
	private final static String                    $XML_JAVA_DATATYPE_SHORT      = "short";
	
	private final static String                    $XML_JAVA_DATATYPE_INT        = "int";
	
	private final static String                    $XML_JAVA_DATATYPE_LONG       = "long";
	
	private final static String                    $XML_JAVA_DATATYPE_BIGDECIMAL = "bigdecimal";
	
	private final static String                    $XML_JAVA_DATATYPE_DOUBLE     = "doulbe";
	
	private final static String                    $XML_JAVA_DATATYPE_FLOAT      = "float";
	
	private final static String                    $XML_JAVA_DATATYPE_BOOLEAN    = "boolean";
	
	private final static String                    $XML_JAVA_DATATYPE_STRING     = "string";
	
	private final static String                    $XML_JAVA_DATATYPE_DATE       = "date";
	
	private final static String                    $XML_JAVA_DATATYPE_OBJECT     = "object";
	
	private final static String                    $XML_JAVA_DATATYPE_CLASS      = "class";
	
	
	
	/** 导入功能的节点的标记 */
	private final static String                    $XML_IMPORT                   = "import";
	
	/** 导入功能的 xml 节点名称标记 */
	private final static String                    $XML_IMPORT_NAME              = "name";
	
	/** 导入功能的 name 对应的 Java 类 */
	private final static String                    $XML_IMPORT_CLASS             = "class";
	
	/** 节点为 Map 类型的集合时，统一标记获取元素哪个方法为Map.key值 */
	private final static String                    $XML_MAP_KEY                  = "key";
	
	/** 节点为 Map 类型的集合时，对于元素的添加方法名称 */
	private final static String                    $XML_MAP_DEF_SETTER           = "put";
	
	/** 节点为 List 类型的集合时，对于元素的添加方法名称 */
	private final static String                    $XML_LIST_DEF_SETTER          = "add";
	
	/** 构造器节点关键字。此关键字必须为构造节点的第一个子节点 */
	private final static String                    $XML_OBJECT_CONSTRUCTOR       = "constructor";
	
	/** 节点对应的 Java 类 */
	private final static String                    $XML_OBJECT_CLASS             = "class";
	
	/** 指定setter方法的节点标记 */
	private final static String                    $XML_OBJECT_SETTER            = "setter";
	
	/** 
	 * this关键字
	 * 
	 * 还有赋值功能。即 Bean v_Bean = v_Other 这样的功能
	 */
	private final static String                    $XML_OBJECT_THIS              = "this";
	
	/** 对象惟一属性的节点标记 */
	private final static String                    $XML_OBJECT_ID                = "id";
	
	/** 引用对象的节点标记 */
	private final static String                    $XML_OBJECT_REF               = "ref";
	
	/** 节点为调用方法Call节点标记 */
	private final static String                    $XML_OBJECT_CALL              = "call";
	
	/** Call节点调用的方法名称 */
	private final static String                    $XML_OBJECT_CALL_NAME         = "name";
	
	/** Call节点调用的方法后的返回结果的ID标记，此结果也将存在 $XML_OBJECTS 中 */
	private final static String                    $XML_OBJECT_CALL_RETURNID     = "returnid";
	
	/** 
	 * submit表示 TreeMap.TreeNode.nodeID 的值。
	 * 将对于树目录的子树目录的全部TreeNode.nodeID及TreeNode.info存在Map中，
	 * 再将Map传递(setter)给对象
	 */
	private final static String                    $XML_OBJECT_SUBMIT            = "submit";
    
    /**
     * 表示是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     */
    private final static String                    $XML_OBJECT_NEWOBJECT         = "new";
    
    /**
     * 加密关键字。对涉密属性进行加密。
     * 
     * 当配置文件为明文时，自动变成密文。
     * 当配置文件为官方时，解密后再赋值给对象的属性。
     */
    private final static String                    $XML_OBJECT_ENCRYPT           = "encrypt";
    
    /** 
     * 真值才解释XJava。
     * 
     * 与Ref关键字类似，但比其多一个功能，就是 if="xx" 时，xx不是引用对象时，当字符串处理，
     * 即可以写成 if="true"
     */
    private final static String                    $XML_OBJECT_IF                = "if";
    
    /** 
     * 假值才解释XJava 
     * 
     * 与Ref关键字类似，但比其多一个功能，就是 if="xx" 时，xx不是引用对象时，当字符串处理，
     * 即可以写成 if="true"
     */
    private final static String                    $XML_OBJECT_IFNOT             = "ifnot";
    
    /** if 关键字的对比关系"或"分割符 */
    private final static String                    $XML_OBJECT_IF_OR             = "||";
    
    /** if 关键字的对比关系"与"分割符 */
    private final static String                    $XML_OBJECT_IF_AND            = "&&";
    
    /** if 关键字的对比分割符 */
    private final static String                    $XML_OBJECT_IF_EQUALS         = "==";
    
    /** 
     * 此为节点文本内容的关键字标记。表示解译xml文件的URL的路径（父目录路径）。
     * 如果节点文本内容中出现 classpath: 将为自动替换为解译xml文件的URL的路径（父目录路径）。
     * 如：org.hy.common.xml
     */
    private final static String                    $XML_CLASSPATH                = "classpath:";
	
    /** 
     * 此为节点文本内容的关键字标记的集合。此集合中的key将自动替换为value
     *   1. classhome: 将为自动替换为classes的根目录。                   如：C:/xx/bin
     *   2. webhome: 将为自动替换为Web服务的根目录。                      如：C:/Tomcat/Webapps/Web项目名称/
     */
    private final static Map<String ,String>       $XML_Replace_Keys             = new LinkedHashMap<String ,String>();
	
	/** 标记有 id 的节点都将存入 $XML_OBJECTS 集合中的 TreeNode.info 中 */
	private final static TreeMap<XJavaObject>      $XML_OBJECTS                  = new TreeMap<XJavaObject>();
	
	/** 
	 * 专用于保存有限生命的对象实例。
	 * 与 $XML_OBJECTS 互补，共同结成整个大对象池。
	 * 即，XJava.getObject(...) 方法先从 $XML_OBJECTS 中获取，获取不到时，再从 $SessionMap 中获取。
	 * 
	 * $SessionMap 只负责运行过程中动态添加的对象，不对XML配置文件中的对象生效。
	 */
	private final static ExpireMap<String ,Object> $SessionMap                   = new ExpireMap<String ,Object>();
	
	/** 
	 * TreeMap.TreeNode.orderByID的最大长度。
	 * 此值决定着 XJava 能支持的树目录中同一层次中节点的数量 
     * 
     * 6 表示最大支持 999999 个对象实例
	 */
	private final static int                       $TREE_NODE_ORDERBYID_MAXLEN   = 6;
	
	
	
	/** Xml文件的路径 */
	private URL                        xmlURL;
	
    /** 表示解译xml文件的URL的路径（父目录路径） */
	private String                     xmlClassPath;
	
	/** 解释每一次Xml文件后，生成 TreeMap 中首个父节点的父节点名，即TreeNode.orderByID的值 */
	private String                     treeNodeRootKey;
	
	/** 包含对象的集合 */
	private Map<String ,String>        imports;
	
	/** 解释类型。1：XML文件解释； 2：XML字符串解释； 3：注解Annotation */
	private int                        parserType;
	
	/** XML字符串。只有当 parserType=2 时才生效。 */
	private String                     xmlString;
    
    /** 包名信息。只有当 parserType=3 时才生效。 */
    private List<String>               packageNames;
    
    /** 须替换的关键字。与$XML_Replace_Keys同义，但集合元素多一个$XML_CLASSPATH */
    private Map<String ,String>        replaces;
    
    /** 本次解析的加密信息。用完立即释放 */
    private List<XJavaEncrypt>         encrypts;
	
	
    
    static
    {
        $XML_Replace_Keys.put("classhome:" ,Help.getClassHomePath());
        $XML_Replace_Keys.put("webhome:"   ,Help.getWebHomePath());
    }
    
    
	
	/**
	 * 解释Xml文件，并且可以直接将Xml文件内容转为Java对象实例
	 * 
	 * @param i_ImportList
	 * @param i_XmlURL
	 * @param i_TreeNodeRootKey
	 * @return
	 * @throws Exception 
	 */
	public static Object parserXml(ListMap<String ,String> i_ImportList ,URL i_XmlURL ,String i_TreeNodeRootKey) throws Exception
	{
		XJava v_ParserXmlToJava = new XJava(i_XmlURL ,i_TreeNodeRootKey);
		
		return v_ParserXmlToJava.parserXml();
	}
	
	
	
	/**
	 * 解释Xml文件，并且可以直接将Xml文件内容转为Java对象实例
	 * 
	 * @param i_XmlURL
	 * @param i_TreeNodeRootKey
	 * @return
	 * @throws Exception 
	 */
	public static Object parserXml(URL i_XmlURL ,String i_TreeNodeRootKey) throws Exception
	{
		XJava v_ParserXmlToJava = new XJava(i_XmlURL ,i_TreeNodeRootKey);
		
		return v_ParserXmlToJava.parserXml();
	}
	
	
	
	/**
	 * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2019-09-04
	 * @version     v1.0
	 *
	 * @param i_XmlURL             XML配置文件的路径。可实现重写的相关功能
	 * @param i_XMLString
	 * @param i_ClassPath
	 * @param i_TreeNodeRootKey
	 * @return
	 * @throws Exception
	 */
    public static Object parserXml(URL i_XmlURL ,String i_XMLString ,String i_ClassPath ,String i_TreeNodeRootKey) throws Exception
    {
        XJava v_ParserXmlToJava = new XJava(i_XmlURL ,i_XMLString ,i_ClassPath ,i_TreeNodeRootKey);
        
        return v_ParserXmlToJava.parserXml();
    }
	
	
	
	/**
	 * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例
	 * 
	 * @param i_ImportList
	 * @param i_XMLString
	 * @param i_ClassPath
	 * @param i_TreeNodeRootKey
	 * @return
	 * @throws Exception 
	 */
	public static Object parserXml(ListMap<String ,String> i_ImportList ,String i_XMLString ,String i_ClassPath ,String i_TreeNodeRootKey) throws Exception
	{
		XJava v_ParserXmlToJava = new XJava(i_ImportList ,i_XMLString ,i_ClassPath ,i_TreeNodeRootKey);
		
		return v_ParserXmlToJava.parserXml();
	}
	
	
	
	/**
	 * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例
	 * 
	 * @param i_XMLString
	 * @param i_ClassPath
	 * @param i_TreeNodeRootKey
	 * @return
	 * @throws Exception 
	 */
	public static Object parserXml(String i_XMLString ,String i_ClassPath ,String i_TreeNodeRootKey) throws Exception
	{
		XJava v_ParserXmlToJava = new XJava(i_XMLString ,i_ClassPath ,i_TreeNodeRootKey);
		
		return v_ParserXmlToJava.parserXml();
	}
	
	
	
    /**
     * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例
     * 
     * @param i_XMLString
     * @param i_TreeNodeRootKey
     * @return
     * @throws Exception 
     */
    public static Object parserXml(String i_XMLString ,String i_TreeNodeRootKey) throws Exception
    {
        XJava v_ParserXmlToJava = new XJava(i_XMLString ,"" ,i_TreeNodeRootKey);
        
        return v_ParserXmlToJava.parserXml();
    }
    
    
    
    /**
     * 解释"注解 Annotation"，转为Java对象实例
     * 
     * @param i_PackageName  包名称
     * @return
     * @throws Exception 
     */
    public static void parserAnnotation() throws Exception
    {
        parserAnnotation(new ArrayList<String>());
    }
    
    
    
    /**
     * 解释"注解 Annotation"，转为Java对象实例
     * 
     * @param i_PackageName  包名称。也可为类的全路径
     * @return
     * @throws Exception 
     */
    public static void parserAnnotation(String i_PackageName) throws Exception
    {
        List<String> v_PackageNames = new ArrayList<String>();
        v_PackageNames.add(i_PackageName);
        v_PackageNames.add(XJava.class.getPackage().getName() + ".plugins.analyse");
        v_PackageNames.add("org.hy.common.net");  // 不强行链接，当运行环境没有引用hy.common.net.jar包时，此行也不会出错的。 ZhengWei(HY) Add 2017-01-25
        
        parserAnnotation(v_PackageNames);
    }
    
    
    
    /**
     * 解释"注解 Annotation"，转为Java对象实例
     * 
     * @param i_PackageNames  包名称集合。也可为类的全路径集合
     * @return
     * @throws Exception 
     */
    public static void parserAnnotation(List<String> i_PackageNames) throws Exception
    {
        XJava v_ParserAnnotationToJava = new XJava(i_PackageNames);
        
        v_ParserAnnotationToJava.parserAnnotations();
    }
    
    
    
    /**
     * 获取规则引擎。为了方便。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2020-05-25
     * @version     v1.0
     *
     * @param i_ID
     * @return
     */
    public static XRule getXRule(String i_ID)
    {
        return (XRule)getObject(i_ID);
    }
    
    
    
    /**
     * 为了方便
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-14
     * @version     v1.0
     *
     * @param i_ID
     * @return
     */
    public static XSQL getXSQL(String i_ID)
    {
        return (XSQL)getObject(i_ID);
    }
    
    
    
    /**
     * 为了方便
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-14
     * @version     v1.0
     *
     * @param i_ID
     * @param i_IsNew
     * @return
     */
    public static XSQL getXSQL(String i_ID ,boolean i_IsNew)
    {
        return (XSQL)getObject(i_ID ,i_IsNew);
    }
    
    
    
    /**
     * 为了方便
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-14
     * @version     v1.0
     *
     * @param i_ID
     * @return
     */
    public static XSQLGroup getXSQLGroup(String i_ID)
    {
        return (XSQLGroup)getObject(i_ID);
    }
    
    
    
    /**
     * 为了方便
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-14
     * @version     v1.0
     *
     * @param i_ID
     * @param i_IsNew
     * @return
     */
    public static XSQLGroup getXSQLGroup(String i_ID ,boolean i_IsNew)
    {
        return (XSQLGroup)getObject(i_ID ,i_IsNew);
    }
    
    
    
    /**
     * 为了方便
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-14
     * @version     v1.0
     *
     * @param i_ID
     * @return
     */
    public static Param getParam(String i_ID)
    {
        return (Param)getObject(i_ID);
    }
    
    
    
    /**
     * 为了方便
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-07-14
     * @version     v1.0
     *
     * @param i_ID
     * @param i_IsNew
     * @return
     */
    public static Param getParam(String i_ID ,boolean i_IsNew)
    {
        return (Param)getObject(i_ID ,i_IsNew);
    }
    
    
    
    /**
     * 获取专用于保存有限生命的对象实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-07
     * @version     v1.0
     *
     * @return
     */
    public static ExpireMap<String ,Object> getSessionMap()
    {
        return $SessionMap;
    }
	
	
	
	/**
	 * 按 id 值获取对象实例
	 * 
	 * 2017-01-16 Add $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
	 * 
	 * @param i_ID
	 * @return
	 */
	public static Object getObject(String i_ID)
	{
		if ( i_ID == null )
		{
			return null;
		}
		else if ( $XML_OBJECTS.containsNodeID(i_ID) )
		{
			try
            {
                return $XML_OBJECTS.getByNodeID(i_ID).getInfo().getObject();
            }
            catch (NoSuchMethodException e)
            {
                // 多数为Clone方法异常
                throw new NullPointerException("[" + i_ID + "] is " + e.getMessage());
            }
		}
		else if ( $SessionMap.containsKey(i_ID) )
		{
		    return $SessionMap.get(i_ID);
		}
		else
		{
			return null;
		}
	}
    
    
    
    /**
     * 按 id 值获取对象实例
     * 
     * 2017-01-16 Add $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 
     * @param i_ID
     * @param i_IsNew   是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     * @return
     */
    public static Object getObject(String i_ID ,boolean i_IsNew)
    {
        try
        {
            if ( i_ID == null )
            {
                return null;
            }
            else if ( $XML_OBJECTS.containsNodeID(i_ID) )
            {
                return $XML_OBJECTS.getByNodeID(i_ID).getInfo().getObject(i_IsNew);
            }
            else if ( $SessionMap.containsKey(i_ID) )
            {
                if ( i_IsNew )
                {
                    return XJava.clone($SessionMap.get(i_ID));
                }
                else
                {
                    return $SessionMap.get(i_ID);
                }
            }
            else
            {
                return null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // 多数为Clone方法异常
            throw new NullPointerException("[" + i_ID + "] is " + e.getMessage());
        }
    }
    
    
    
    /**
     * 按 Class 元类型获取对象实例的XID
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-04
     * @version     v1.0
     *
     * @param i_Class
     * @return
     */
    public static String getObjectID(Class<?> i_Class)
    {
        if ( i_Class == null )
        {
            return null;
        }
        else
        {
            Iterator<TreeNode<XJavaObject>>    v_Iterator = $XML_OBJECTS.valuesNodeID();
            TreeNode<XJavaObject>              v_TreeNode = null;
            Map<TreeNode<XJavaObject> ,Object> v_Maybe    = new HashMap<TreeNode<XJavaObject> ,Object>(1); // 有可能是的对象 ZhengWei(HY) Add 2017-11-24
            
            try
            {
                while ( v_Iterator.hasNext() )
                {
                    v_TreeNode = v_Iterator.next();
                    
                    if ( null != v_TreeNode.getInfo()
                      && null != v_TreeNode.getInfo().getObject(false) )
                    {
                        if ( i_Class == v_TreeNode.getInfo().getObject(false).getClass() )
                        {
                            return v_TreeNode.getNodeID();
                        }
                        else if ( i_Class != Object.class && i_Class.isInstance(v_TreeNode.getInfo().getObject(false)) )
                        {
                            // 尝试在实现类、子类中查找 ZhengWei(HY) Add 2017-11-24
                            v_Maybe.put(v_TreeNode ,null);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new NullPointerException(i_Class.getName() + "[" + v_TreeNode.getNodeID() + "] is " + e.getMessage() + ".");
            }
            
            
            for (Map.Entry<String ,Object> v_Item : $SessionMap.entrySet())
            {
                if ( null != v_Item && i_Class == v_Item.getValue().getClass() )
                {
                    return v_Item.getKey();
                }
            }
            
            if ( v_Maybe.size() == 1 )
            {
                return v_Maybe.keySet().iterator().next().getNodeID();
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 按 Class 元类型获取对象实例
     * 
     * 2017-01-16 Add   $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 2017-11-24 Edit  优化：在if语句判定时，不创建全新对象实例进行判定。
     * 2017-11-24 Add   尝试在实现类、子类中查找匹配的对象
     * 
     * @param i_ID
     * @return
     */
    public static Object getObject(Class<?> i_Class)
    {
        if ( i_Class == null )
        {
            return null;
        }
        else
        {
            Iterator<TreeNode<XJavaObject>> v_Iterator = $XML_OBJECTS.valuesNodeID();
            TreeNode<XJavaObject>           v_TreeNode = null;
            Map<XJavaObject ,Object>        v_Maybe    = new HashMap<XJavaObject ,Object>(1); // 有可能是的对象 ZhengWei(HY) Add 2017-11-24
            
            try
            {
                while ( v_Iterator.hasNext() )
                {
                    v_TreeNode = v_Iterator.next();
                    
                    if ( null != v_TreeNode.getInfo()
                      && null != v_TreeNode.getInfo().getObject(false) )
                    {
                        if ( i_Class == v_TreeNode.getInfo().getObject(false).getClass() )
                        {
                            return v_TreeNode.getInfo().getObject();
                        }
                        else if ( i_Class != Object.class && i_Class.isInstance(v_TreeNode.getInfo().getObject(false)) )
                        {
                            // 尝试在实现类、子类中查找 ZhengWei(HY) Add 2017-11-24
                            v_Maybe.put(v_TreeNode.getInfo() ,null);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new NullPointerException(i_Class.getName() + "[" + v_TreeNode.getNodeID() + "] is " + e.getMessage() + ".");
            }
            
            
            Iterator<Object> v_SessionIterator = $SessionMap.values().iterator();
            Object           v_Ret             = null;
            while ( v_SessionIterator.hasNext() )
            {
                v_Ret = v_SessionIterator.next();
                
                if ( null != v_Ret && i_Class == v_Ret.getClass() )
                {
                    return v_Ret;
                }
            }
            
            if ( v_Maybe.size() == 1 )
            {
                try
                {
                    return v_Maybe.keySet().iterator().next().getObject();
                }
                catch (Exception exce)
                {
                    throw new NullPointerException(i_Class.getName() + "[" + v_Maybe.keySet().iterator().next().object.getClass().getName() + "] is " + exce.getMessage() + ".\n" + exce.getMessage());
                }
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 按 Class 元类型获取对象实例
     * 
     * 2017-01-16 Add   $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 2017-11-24 Edit  优化：在if语句判定时，不创建全新对象实例进行判定。
     * 2017-11-24 Add   尝试在实现类、子类中查找匹配的对象
     * 
     * @param i_ID
     * @param i_IsNew   是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     * @return
     */
    public static Object getObject(Class<?> i_Class ,boolean i_IsNew)
    {
        if ( i_Class == null )
        {
            return null;
        }
        else
        {
            Iterator<TreeNode<XJavaObject>> v_Iterator = $XML_OBJECTS.valuesNodeID();
            TreeNode<XJavaObject>           v_TreeNode = null;
            Map<XJavaObject ,Object>        v_Maybe    = new HashMap<XJavaObject ,Object>(1); // 有可能是的对象 ZhengWei(HY) Add 2017-11-24
            
            try
            {
                while ( v_Iterator.hasNext() )
                {
                    v_TreeNode = v_Iterator.next();
                    
                    if ( null != v_TreeNode.getInfo()
                      && null != v_TreeNode.getInfo().getObject(false) )
                    {
                        if ( i_Class == v_TreeNode.getInfo().getObject(false).getClass() )
                        {
                            return v_TreeNode.getInfo().getObject(i_IsNew);
                        }
                        else if ( i_Class != Object.class && i_Class.isInstance(v_TreeNode.getInfo().getObject(false)) )
                        {
                            // 尝试在实现类、子类中查找 ZhengWei(HY) Add 2017-11-24
                            v_Maybe.put(v_TreeNode.getInfo() ,null);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new NullPointerException("[" + v_TreeNode.getNodeID() + "] is " + e.getMessage());
            }
            
            
            try
            {
                Iterator<Object> v_SessionIterator = $SessionMap.values().iterator();
                Object           v_Ret             = null;
                while ( v_SessionIterator.hasNext() )
                {
                    v_Ret = v_SessionIterator.next();
                    
                    if ( null != v_Ret && i_Class == v_Ret.getClass() )
                    {
                        if ( i_IsNew )
                        {
                            return XJava.clone(v_Ret);
                        }
                        else
                        {
                            return v_Ret;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new NullPointerException("[" + v_TreeNode.getNodeID() + "] is " + e.getMessage());
            }
            
            if ( v_Maybe.size() == 1 )
            {
                try
                {
                    return v_Maybe.keySet().iterator().next().getObject(i_IsNew);
                }
                catch (Exception e)
                {
                    throw new NullPointerException(i_Class.getName() + "[" + v_Maybe.keySet().iterator().next().object.getClass().getName() + "] is " + e.getMessage() + ".");
                }
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 按 ID 标识符的前缀 值获取类似的多个对象实例
     * 
     * 注意：本方法不能简单的调用 getObjects(String ,false) 来实现。
     *      这两种实现的区别在于 getObjects(String) 是按每种不同的自有属性来获取实例的。
     *      即，有的对象是新实例，有的不是新实例
     *      
     * 2017-01-16 Add $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 
     * @param i_IDPrefix  ID 标识符的前缀(区分大小写)
     * @return
     */
    public static Map<String ,Object> getObjects(String i_IDPrefix)
    {
        if ( i_IDPrefix == null )
        {
            return null;
        }
        
        Map<String ,Object> v_Objs     = new HashMap<String ,Object>();
        Iterator<String>    v_Iterator = $XML_OBJECTS.keySetNodeID();
        
        while ( v_Iterator.hasNext() )
        {
            String v_ID = v_Iterator.next();
            
            if ( v_ID.startsWith(i_IDPrefix) )
            {
                try
                {
                    v_Objs.put(v_ID ,$XML_OBJECTS.getByNodeID(v_ID).getInfo().getObject());
                }
                catch (NoSuchMethodException e)
                {
                    // 多数为Clone方法异常
                    throw new NullPointerException("[" + v_ID + "] is " + e.getMessage());
                }
            }
        }
        
        
        v_Iterator = $SessionMap.keySet().iterator();
        while ( v_Iterator.hasNext() )
        {
            String v_ID = v_Iterator.next();
            
            if ( v_ID.startsWith(i_IDPrefix) )
            {
                v_Objs.put(v_ID ,$SessionMap.get(v_ID));
            }
        }
        
        return v_Objs;
    }
    
    
    
    /**
     * 按 ID 标识符的前缀 值获取类似的多个对象实例
     * 
     * 2017-01-16 Add $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 
     * @param i_IDPrefix  ID 标识符的前缀(区分大小写)
     * @param i_IsNew     是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     * @return
     */
    public static Map<String ,Object> getObjects(String i_IDPrefix ,boolean i_IsNew)
    {
        if ( i_IDPrefix == null )
        {
            return null;
        }
        
        Map<String ,Object> v_Objs     = new HashMap<String ,Object>();
        Iterator<String>    v_Iterator = $XML_OBJECTS.keySetNodeID();
        while ( v_Iterator.hasNext() )
        {
            String v_ID = v_Iterator.next();
            
            if ( v_ID.startsWith(i_IDPrefix) )
            {
                try
                {
                    v_Objs.put(v_ID ,$XML_OBJECTS.getByNodeID(v_ID).getInfo().getObject(i_IsNew));
                }
                catch (NoSuchMethodException e)
                {
                    // 多数为Clone方法异常
                    throw new NullPointerException("[" + v_ID + "] is " + e.getMessage());
                }
            }
        }
        
        
        v_Iterator = $SessionMap.keySet().iterator();
        while ( v_Iterator.hasNext() )
        {
            String v_ID = v_Iterator.next();
            
            if ( v_ID.startsWith(i_IDPrefix) )
            {
                try
                {
                    if ( i_IsNew )
                    {
                        v_Objs.put(v_ID ,XJava.clone($SessionMap.get(v_ID)));
                    }
                    else
                    {
                        v_Objs.put(v_ID ,$SessionMap.get(v_ID));
                    }
                }
                catch (NoSuchMethodException e)
                {
                    // 多数为Clone方法异常
                    throw new NullPointerException("[" + v_ID + "] is " + e.getMessage());
                }
            }
        }
        
        return v_Objs;
    }
    
    
    
    /**
     * 按 Class 元类型获取对象实例
     * 
     * 2017-01-16 Add $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 
     * @param i_Class
     * @return
     */
    public static Map<String ,Object> getObjects(Class<?> i_Class)
    {
        Map<String ,Object> v_Objs = new HashMap<String ,Object>();
        
        if ( i_Class == null )
        {
            return v_Objs;
        }
        else
        {
            Iterator<String>      v_Iterator = $XML_OBJECTS.keySetNodeID();
            String                v_ID       = null;
            TreeNode<XJavaObject> v_TreeNode = null;
            
            try
            {
                while ( v_Iterator.hasNext() )
                {
                    v_ID       = v_Iterator.next();
                    v_TreeNode = $XML_OBJECTS.getByNodeID(v_ID);
                    
                    if ( null    != v_TreeNode.getInfo()
                      && null    != v_TreeNode.getInfo().getObject() 
                      && i_Class == v_TreeNode.getInfo().getObject().getClass() )
                    {
                        v_Objs.put(v_ID ,v_TreeNode.getInfo().getObject());
                    }
                }
            }
            catch (Exception e)
            {
                throw new NullPointerException(i_Class.getName() + "[" + v_TreeNode.getNodeID() + "] is " + e.getMessage() + ".");
            }
            
            
            Iterator<Object> v_SessionIterator = $SessionMap.values().iterator();
            while ( v_SessionIterator.hasNext() )
            {
                Object v_Object = v_SessionIterator.next();
                
                if ( null != v_Object && i_Class == v_Object.getClass() )
                {
                    v_Objs.put(v_ID ,v_Object);
                }
            }
        }
        
        return v_Objs;
    }
    
    
    
    /**
     * 按 Class 元类型获取对象实例
     * 
     * 2017-01-16 Add $SessionMap专用于保存有限生命的对象实例。与 $XML_OBJECTS 互补，共同结成整个大对象池。
     * 
     * @param i_ID
     * @param i_IsNew   是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
     * @return
     */
    public static Map<String ,Object> getObjects(Class<?> i_Class ,boolean i_IsNew)
    {
        Map<String ,Object> v_Objs = new HashMap<String ,Object>();
        
        if ( i_Class == null )
        {
            return v_Objs;
        }
        else
        {
            Iterator<String>      v_Iterator = $XML_OBJECTS.keySetNodeID();
            String                v_ID       = null;
            TreeNode<XJavaObject> v_TreeNode = null;
            
            try
            {
                while ( v_Iterator.hasNext() )
                {
                    v_ID       = v_Iterator.next();
                    v_TreeNode = $XML_OBJECTS.getByNodeID(v_ID);
                    
                    if ( null    != v_TreeNode.getInfo()
                      && null    != v_TreeNode.getInfo().getObject() 
                      && i_Class == v_TreeNode.getInfo().getObject().getClass() )
                    {
                        v_Objs.put(v_ID ,v_TreeNode.getInfo().getObject(i_IsNew));
                    }
                }
            }
            catch (Exception e)
            {
                throw new NullPointerException(i_Class.getName() + "[" + v_TreeNode.getNodeID() + "] is " + e.getMessage() + ".");
            }
            
            
            Iterator<Object> v_SessionIterator = $SessionMap.values().iterator();
            while ( v_SessionIterator.hasNext() )
            {
                Object v_Object = v_SessionIterator.next();
                
                if ( null != v_Object && i_Class == v_Object.getClass() )
                {
                    try
                    {
                        if ( i_IsNew )
                        {
                            v_Objs.put(v_ID ,XJava.clone(v_Object));
                        }
                        else
                        {
                            v_Objs.put(v_ID ,v_Object);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new NullPointerException(i_Class.getName() + "[" + v_TreeNode.getNodeID() + "] is " + e.getMessage() + ".");
                    }
                }
            }
        }
        
        return v_Objs;
    }
    
    
    
    /**
     * 由外界主动设置ID的值，并添加到XJava中
     * 
     * @param i_ID
     * @param i_Object
     */
    public static void putObject(String i_ID ,Object i_Object)
    {
        putObject(i_ID ,i_Object ,false);
    }
    
    
    
    /**
     * 由外界主动设置ID的值，并添加到XJava中
     * 
     * @param i_ID
     * @param i_Object
     * @param i_IsNew
     */
    public static void putObject(String i_ID ,Object i_Object ,boolean i_IsNew)
    {
        new XJava(i_ID ,i_Object ,i_IsNew);
    }
    
    
    
    /**
     * 由外界主动设置ID的值，并添加到XJava中
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-16
     * @version     v1.0
     *
     * @param i_ID       对象的ID
     * @param i_Object   对象
     * @param i_Second   过期时长(单位：秒)。指当前时刻过i_Second秒后过期失效。
     */
    public static void putObject(String i_ID ,Object i_Object ,long i_Second)
    {
        $SessionMap.put(i_ID ,i_Object ,i_Second);
    }
    
    
    
    /**
     * 将对象从对象池中移除
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-01-16
     * @version     v1.0
     *
     * @param i_ID  对象的ID
     */
    public static void remove(String i_ID)
    {
        $XML_OBJECTS.removeNodeID(i_ID);
        $SessionMap .remove      (i_ID);
    }
    
	
	
	/**
	 * 清空 XJava 解释过的所有实例化对象信息
	 */
	public static void clear()
	{
		$XML_OBJECTS.clear();
		$SessionMap .clear();
	}
	
	
	
	/**
	 * 按树目录结构，获取子树目录中 XJava 解释过的所有实例化对象信息
	 * 
	 * @param i_NodeID
	 * @return
	 */
	private static Map<String ,Object> getChildObjects(String i_NodeID)
	{
		Map<String ,Object>  v_Ret       = new Hashtable<String ,Object>();
		TreeMap<XJavaObject> v_ChildNode = null;
		
		if ( i_NodeID == null )
		{
			return v_Ret;
		}
		
		
		try
		{
			v_ChildNode = $XML_OBJECTS.getChildTreeByNodeID(i_NodeID);
		}
		catch (Exception exce)
		{
			return v_Ret;
		}
		
		
		if ( v_ChildNode.size() >= 1 )
		{
			Iterator<TreeNode<XJavaObject>> v_Iterator = v_ChildNode.valuesNodeID();
			
			try
			{
				for ( ; v_Iterator.hasNext(); )
				{
					TreeNode<XJavaObject> v_TreeNode = v_Iterator.next();
					
					if ( v_TreeNode != null && v_TreeNode.getNodeID() != null && v_TreeNode.getInfo() != null )
					{
						// 此处待解决的问题是：当 submit 关键字的对象在 i_NodeID 之中时，
						// sumbit 关键字所在对象也有 id 关键字，
						// 但，此时的 id 关键字还没有已存入 $XML_OBJECTS 集合中时，
						// v_TreeNode.getInfo() == null 的问题。
						v_Ret.put(v_TreeNode.getNodeID() ,v_TreeNode.getInfo().getObject());
					}
				}
			}
			catch (Exception exce)
			{
				throw new RuntimeException("Submit node [" + i_NodeID + "] is exception.");
			}
		}
		
		return v_Ret;
	}
	
	
	
	/**
	 * 由外界主动设置ID的值，并添加到XJava中
	 * 
	 * @param i_ID
	 * @param i_Object
	 * @param i_IsNew
	 */
	private XJava(String i_ID ,Object i_Object ,boolean i_IsNew)
	{
	    if ( Help.isNull(i_ID) )
	    {
	        throw new NullPointerException("ID is null.");
	    }
	    
	    if ( i_Object == null )
	    {
	        throw new NullPointerException("Object is null.");
	    }
	    
	    TreeNode<XJavaObject> v_TreeNode = new TreeNode<XJavaObject>(i_ID.trim() ,i_ID.trim());
        
        v_TreeNode.setInfo(new XJavaObject(i_ID.trim() ,i_Object ,i_IsNew));
        
        $XML_OBJECTS.put(v_TreeNode);
	}
	
	
	
	private XJava(URL i_XmlURL ,String i_TreeNodeRootKey)
	{
		this(new ListMap<String ,String>() ,i_XmlURL ,i_TreeNodeRootKey);
	}
	
	
	
	private XJava(ListMap<String ,String> i_ImportList ,URL i_XmlURL ,String i_TreeNodeRootKey)
	{
		this.imports         = i_ImportList;
		this.xmlURL          = i_XmlURL;
		this.treeNodeRootKey = i_TreeNodeRootKey;
		
		this.xmlClassPath    = this.xmlURL.toString().replaceFirst(StringHelp.getFileName(this.xmlURL.getPath()) ,"");
		this.parserType      = 1;
		this.xmlString       = null;
        this.packageNames    = null;
        
        this.encrypts        = new ArrayList<XJavaEncrypt>();
        this.replaces        = new LinkedHashMap<String ,String>($XML_Replace_Keys);
        this.replaces.put($XML_CLASSPATH ,this.xmlClassPath);
	}
	
	
	
	/**
	 * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例
	 *
	 * @author      ZhengWei(HY)
	 * @createDate  2019-09-04
	 * @version     v1.0
	 *
	 * @param i_XmlURL             XML配置文件的路径。可实现重写的相关功能
	 * @param i_XMLString
	 * @param i_ClassPath
	 * @param i_TreeNodeRootKey
	 */
    private XJava(URL i_XmlURL ,String i_XMLString ,String i_ClassPath ,String i_TreeNodeRootKey)
    {
        this(new ListMap<String ,String>() ,i_XMLString ,i_ClassPath ,i_TreeNodeRootKey);
        this.xmlURL = i_XmlURL;
    }
    
	
	
    /**
     * 解释Xml字符串，并且可以直接将Xml字符串转为Java对象实例
     * 
     * @param i_XMLString
     * @param i_ClassPath
     * @param i_TreeNodeRootKey
     * @return
     * @throws Exception 
     */
	private XJava(String i_XMLString ,String i_ClassPath ,String i_TreeNodeRootKey)
	{
		this(new ListMap<String ,String>() ,i_XMLString ,i_ClassPath ,i_TreeNodeRootKey);
	}
	
	
	
	private XJava(ListMap<String ,String> i_ImportList ,String i_XMLString ,String i_ClassPath ,String i_TreeNodeRootKey)
	{
		this.imports         = i_ImportList;
		this.xmlURL          = null;
		this.treeNodeRootKey = i_TreeNodeRootKey;
		
		this.xmlClassPath    = Help.NVL(i_ClassPath);
		this.parserType      = 2;
		this.xmlString       = i_XMLString;
        this.packageNames    = null;
        
        this.encrypts        = new ArrayList<XJavaEncrypt>();
        this.replaces        = new LinkedHashMap<String ,String>($XML_Replace_Keys);
        this.replaces.put($XML_CLASSPATH ,this.xmlClassPath);
	}
    
    
    
    private XJava(List<String> i_PackageNames)
    {
        this.imports         = null;
        this.xmlURL          = null;
        this.treeNodeRootKey = "";
        
        this.xmlClassPath    = "";
        this.parserType      = 3;
        this.xmlString       = null;
        this.packageNames    = Help.NVL(i_PackageNames ,new ArrayList<String>());
        
        this.encrypts        = new ArrayList<XJavaEncrypt>();
        this.replaces        = new LinkedHashMap<String ,String>($XML_Replace_Keys);
    }
    
    
    
    /**
     * 解释包名称
     * 
     * 主要用于注解功能
     * 
     * @return
     */
    private List<Class<?>> parserPackageName()
    {
        List<Class<?>> v_Classes = null;
        
        if ( Help.isNull(this.packageNames) )
        {
            v_Classes = Help.getClasses();
        }
        else
        {
            v_Classes = new ArrayList<Class<?>>();
            
            for (int i=0; i<this.packageNames.size(); i++)
            {
                if ( !Help.isNull(this.packageNames.get(i)) )
                {
                    v_Classes.addAll(Help.getClasses(this.packageNames.get(i).trim()));
                }
            }
        }
        
        return v_Classes;
    }
    
    
    
    /**
     * 解释"注解 Annotation"，转为Java对象实例
     * 
     * @return
     * @throws Exception 
     */
    private void parserAnnotations() throws Exception
    {
        List<Class<?>>                       v_Classes     = parserPackageName();
        PartitionMap<ElementType ,ClassInfo> v_Annotations = XAnnotation.getAnnotations(v_Classes);
        List<ClassInfo>                      v_ClassInfos  = null;
        ClassInfo                            v_ClassInfo   = null;
        Xjava                                v_AnnoID      = null;
        
        if ( Help.isNull(v_Annotations) )
        {
            return;
        }
        
        
        // value 注解功能的实现(默认注解关键字)
        v_ClassInfos = v_Annotations.get(ElementType.TYPE);
        if ( !Help.isNull(v_ClassInfos) )
        {
            XTypeAnno v_XTypeAnno = null;
            
            for (int i=0; i<v_ClassInfos.size(); i++)
            {
                v_ClassInfo = v_ClassInfos.get(i);
                v_AnnoID    = v_ClassInfo.getClassObj().getAnnotation(Xjava.class);
                
                try
                {
                    if ( XType.XML == v_AnnoID.value() )
                    {
                        v_XTypeAnno = new XTypeAnno(v_AnnoID.value() ,v_ClassInfo);
                        
                        v_XTypeAnno.init();
                    }
                    else if ( XType.XD == v_AnnoID.value() )
                    {
                        v_XTypeAnno = new XTypeAnno(v_AnnoID.value() ,v_ClassInfo);
                        
                        v_XTypeAnno.init();
                    }
                    // 2017-12-15 XSQL、XSQLGroup的注解。用于 xml配置 + Java接口类(无须实现类)的组合实现持久层的功能。
                    else if ( XType.XSQL == v_AnnoID.value() )
                    {
                        String v_ID = null;
                        
                        // 命名注解
                        if ( !Help.isNull(v_AnnoID.id()) )
                        {
                            v_ID = v_AnnoID.id().trim();
                        }
                        // 无命名注解
                        else
                        {
                            v_ID = v_ClassInfo.getClassObj().getSimpleName();
                        }
                        
                        TreeNode<XJavaObject> v_TreeNode = new TreeNode<XJavaObject>(v_ID ,v_ID);
                        Object                v_Obj      = null;
                        
                        v_Obj = XSQLProxy.newProxy(v_ClassInfo.getClassObj());
                        
                        v_TreeNode.setInfo(new XJavaObject(v_ID ,v_Obj ,v_AnnoID.isNew()));
                        $XML_OBJECTS.put(v_TreeNode);
                    }
                }
                catch (Exception exce)
                {
                    System.err.println("XType.XML or XType.XD or XType.XSQL [" + v_ClassInfo.getClassObj().getName() + "] is error ,maybe file or object is not find.");
                    exce.printStackTrace();
                }
            }
        }
        
        
        // id    注解功能的实现
        // isNew 注解功能的实现
        v_ClassInfos = v_Annotations.get(ElementType.TYPE);
        if ( !Help.isNull(v_ClassInfos) )
        {
            String v_ID = null;
            
            for (int i=0; i<v_ClassInfos.size(); i++)
            {
                v_ClassInfo = v_ClassInfos.get(i);
                v_AnnoID    = v_ClassInfo.getClassObj().getAnnotation(Xjava.class);
                
                if ( XType.XSQL == v_AnnoID.value() )
                {
                    continue;
                }
                
                // 命名注解
                if ( !Help.isNull(v_AnnoID.id()) )
                {
                    v_ID = v_AnnoID.id().trim();
                }
                // 无命名注解
                else
                {
                    v_ID = v_ClassInfo.getClassObj().getSimpleName();
                }
                
                Object v_TempObject = getObject(v_ID);
                if ( v_TempObject == null  )
                {
                    try
                    {
                        TreeNode<XJavaObject> v_TreeNode = new TreeNode<XJavaObject>(v_ID ,v_ID);
                        Object                v_Obj      = null;
                        
                        v_Obj = v_ClassInfo.getClassObj().newInstance();
                        
                        v_TreeNode.setInfo(new XJavaObject(v_ID ,v_Obj ,v_AnnoID.isNew()));
                        $XML_OBJECTS.put(v_TreeNode);
                    }
                    catch (Exception exce)
                    {
                        // 没有默认构造器异常
                        throw new ClassNotFoundException("New instance Annotation ID[" + v_ID + "] exception of Class[" + v_ClassInfo.getClassObj().toString() + "].\n" + exce.getMessage());
                    }
                }
                else
                {
                    // ID重复定义
                    // 不报错。原因是：允许在XML文件中定义的ID对象已被实现化后，再被"注解"定义东东使用
                    
                    // 2017-12-15 XSQL、XSQLGroup的注解。用于 xml配置 + Java接口类(无须实现类)的组合实现持久层的功能。
                    XSQLProxy v_XSQLProxy = XSQLProxy.getXSQLProxy(v_TempObject);
                    if ( v_XSQLProxy != null )
                    {
                        try
                        {
                            v_XSQLProxy.setXsqlInstace(v_ClassInfo.getClassObj().newInstance());
                        }
                        catch (Exception exce)
                        {
                            // 没有默认构造器异常
                            throw new ClassNotFoundException("New instance Annotation ID[" + v_ID + "] exception of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                        }
                    }
                }
            }
        }
        
        
        // ref "方法"注解功能的实现 
        v_ClassInfos = v_Annotations.get(ElementType.METHOD);
        if ( !Help.isNull(v_ClassInfos) )
        {
            for (int i=0; i<v_ClassInfos.size(); i++)
            {
                v_ClassInfo = v_ClassInfos.get(i);
                
                // 存在方法注解的情况
                if ( !Help.isNull(v_ClassInfo.getMethods()) )
                {
                    Object v_Object = null;
                    v_AnnoID = v_ClassInfo.getClassObj().getAnnotation(Xjava.class);
                    
                    // ref 注解依赖于："id 注解" 或 "XJava配置文件中id关键字实例化的对象"。
                    if ( v_AnnoID == null || Help.isNull(v_AnnoID.id()) )
                    {
                        v_Object = getObject(v_ClassInfo.getClassObj());
                        
                        if ( v_Object == null )
                        {
                            if ( v_AnnoID == null )
                            {
                                throw new NullPointerException("Annotation ID is not exist of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                            }
                            else
                            {
                                // ID为空异常
                                throw new NullPointerException("Annotation ID is null of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                            }
                        }
                    }
                    else
                    {
                        v_Object = getObject(v_AnnoID.id()); 
                    }
                    
                    
                    if ( v_Object != null )
                    {
                        for (int x=0; x<v_ClassInfo.getMethods().size(); x++)
                        {
                            Method v_Method    = v_ClassInfo.getMethods().get(x);
                            Xjava  v_AnnoRef   = v_Method.getAnnotation(Xjava.class);
                            Object v_ObjectRef = null; 
                            
                            if ( v_Method.getParameterTypes().length != 1 )
                            {
                                // ref注解的方法入参参数应当只能是一个
                                throw new NoSuchMethodException("Method[" + v_Method.getName() + "] parameter count is only one of Class[" + v_ClassInfo.getClassObj().toString() + "].  Annotation name is ref[" + v_AnnoRef.ref() + "]");
                            }
                            
                            // 无命名注解
                            if ( Help.isNull(v_AnnoRef.ref()) )
                            {
                                v_ObjectRef = getObject(v_Method.getParameterTypes()[0].getClass());
                                
                                // 当参数类型找不到时，按方法名称找
                                if ( v_ObjectRef == null )
                                {
                                    if ( v_Method.getName().startsWith("set") && v_Method.getName().length() > 3 )
                                    {
                                        v_ObjectRef = getObject(v_Method.getName().substring(3));
                                    }
                                    else
                                    {
                                        v_ObjectRef = getObject(v_Method.getName());
                                    }
                                }
                            }
                            // 命名注解
                            else
                            {
                                v_ObjectRef = getObject(v_AnnoRef.ref());
                            }
                            
                            if ( v_ObjectRef == null )
                            {
                                // 方法参数引用的对象没有被实例化，或者不存在
                                throw new NullPointerException("Method[" + v_Method.getName() + "] Annotation Ref[" + v_AnnoRef.ref() + "] is not exist instance object of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                            }
                            else
                            {
                                try
                                {
                                    v_Method.invoke(v_Object ,v_ObjectRef);
                                }
                                catch (Exception exce)
                                {
                                    // 执行方法注入参数异常
                                    throw new NullPointerException("Call Method[" + v_Method.getName() + "] Annotation Ref[" + v_AnnoRef.ref() + "] is error of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                                }
                            }
                        }
                    }
                    else
                    {
                        // ID对应的对象还没有被实例化，或者不存在
                        throw new NullPointerException("Annotation ID instance object is null of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                    }
                        
                }
            }
        }
        
        
        // ref "属性"注解功能的实现 
        v_ClassInfos = v_Annotations.get(ElementType.FIELD);
        if ( !Help.isNull(v_ClassInfos) )
        {
            for (int i=0; i<v_ClassInfos.size(); i++)
            {
                v_ClassInfo = v_ClassInfos.get(i);
                
                // 存在属性注解的情况
                if ( !Help.isNull(v_ClassInfo.getFields()) )
                {
                    Object v_Object = null;
                    v_AnnoID = v_ClassInfo.getClassObj().getAnnotation(Xjava.class);
                    
                    // ref 注解依赖于："id 注解" 或 "XJava配置文件中id关键字实例化的对象"。
                    if ( v_AnnoID == null || Help.isNull(v_AnnoID.id()) )
                    {
                        v_Object = getObject(v_ClassInfo.getClassObj());
                        
                        if ( v_Object == null )
                        {
                            if ( v_AnnoID == null )
                            {
                                throw new NullPointerException("Annotation ID is not exist of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                            }
                            else
                            {
                                // ID为空异常
                                throw new NullPointerException("Annotation ID is null of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                            }
                        }
                    }
                    else
                    {
                        v_Object = getObject(v_AnnoID.id()); 
                    }
                    
                    if ( v_Object != null )
                    {
                        for (int x=0; x<v_ClassInfo.getFields().size(); x++)
                        {
                            Field  v_Field     = v_ClassInfo.getFields().get(x);
                            Method v_Method    = MethodReflect.getSetMethod(v_ClassInfo.getClassObj() ,v_Field.getName() ,true);
                            Xjava  v_AnnoRef   = v_Field.getAnnotation(Xjava.class);
                            Object v_ObjectRef = null; 
                            
                            // 无命名注解
                            if ( Help.isNull(v_AnnoRef.ref()) )
                            {
                                v_ObjectRef = getObject(v_Field.getType().getSimpleName());
                                
                                if ( v_ObjectRef == null )
                                {
                                    v_ObjectRef = getObject(v_Field.getName());
                                    
                                    if ( v_ObjectRef == null )
                                    {
                                        v_ObjectRef = getObject(v_Field.getName().substring(0, 1).toUpperCase() + v_Field.getName().substring(1));
                                        
                                        // 尝试按类型匹配查找  ZhengWei(HY) Add 2017-11-24
                                        if ( v_ObjectRef == null )
                                        {
                                            v_ObjectRef = getObject(v_Field.getType());
                                        }
                                    }
                                }
                            }
                            // 命名注解
                            else
                            {
                                v_ObjectRef = getObject(v_AnnoRef.ref());
                            }
                            
                            if ( v_ObjectRef == null )
                            {
                                // 属性引用的对象没有被实例化，或者不存在
                                throw new NullPointerException("Field[" + v_Field.getName() + "] Annotation Ref[" + v_AnnoRef.ref() + "] is not exist instance object of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                            }
                            else
                            {
                                // 1. 属性没有对应的Setter方法
                                // 2. ref注解的方法入参参数应当只能是一个
                                if ( v_Method == null || v_Method.getParameterTypes().length != 1 )
                                {
                                    // ZhengWei(HY) Add 2017-11-23 支持无Setter方法时，对属性赋值
                                    try
                                    {
                                        if ( !v_Field.isAccessible() )
                                        {
                                            v_Field.setAccessible(true);
                                            v_Field.set(v_Object ,v_ObjectRef);
                                            v_Field.setAccessible(false);
                                        }
                                        else
                                        {
                                            v_Field.set(v_Object ,v_ObjectRef);
                                        }
                                    }
                                    catch (Exception exce)
                                    {
                                        throw new IllegalAccessException("Set Field's[" + v_Field.getName() + "] Annotation Ref[" + v_AnnoRef.ref() + "] is error of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                                    }
                                    // throw new NoSuchMethodException("Field's[" + v_Field.getName() + "] Setter Method is not exist of Class[" + v_ClassInfo.getClassObj().toString() + "].  Annotation name is ref[" + v_AnnoRef.ref() + "]");
                                    // throw new NoSuchMethodException("Field's[" + v_Field.getName() + "] Setter Method[" + v_Method.getName() + "] parameter count is only one of Class[" + v_ClassInfo.getClassObj().toString() + "].  Annotation name is ref[" + v_AnnoRef.ref() + "]");
                                }
                                else
                                {
                                    try
                                    {
                                        v_Method.invoke(v_Object ,v_ObjectRef);
                                    }
                                    catch (Exception exce)
                                    {
                                        // 执行属性的Setter方法注入参数异常
                                        throw new RuntimeException("Call Field's[" + v_Field.getName() + "] Setter Method[" + v_Method.getName() + "] Annotation Ref[" + v_AnnoRef.ref() + "] is error of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        // ID对应的对象还没有被实例化，或者不存在
                        throw new NullPointerException("Annotation ID instance object is null of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                    }
                    
                }
            }
        }
        
        parserAnnotations_XRequest(v_Classes);
    }
    
    
    
    /**
     * 解释XRequest注解
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-12-04
     * @version     v1.0
     *
     * @param i_Classes
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private synchronized void parserAnnotations_XRequest(List<Class<?>> i_Classes) throws Exception
    {
        if ( Help.isNull(i_Classes) )
        {
            return;
        }
        
        String                            v_AppIFsXID     = "AppInterfaces"; 
        String                            v_AppMKXID      = "AppMsgKeySSID";
        List<ClassInfo>                   v_XRequests     = ClassReflect.getAnnotationMethods(i_Classes ,XRequest.class);
        Map<String ,AppInterface>         v_AppInterfaces = (Map<String ,AppInterface>)XJava.getObject(v_AppIFsXID);
        TablePartitionRID<String ,String> v_AppMKs        = (TablePartitionRID<String ,String>)XJava.getObject(v_AppMKXID);
        
        if ( Help.isNull(v_XRequests) )
        {
            return;
        }
        
        if ( Help.isNull(v_AppInterfaces) )
        {
            v_AppInterfaces = new Hashtable<String ,AppInterface>();
            XJava.putObject(v_AppIFsXID ,v_AppInterfaces);
        }
        
        if ( Help.isNull(v_AppMKs) )
        {
            v_AppMKs = new TablePartitionRID<String ,String>();
            XJava.putObject(v_AppMKXID ,v_AppMKs);
        }
        
        for (ClassInfo v_ClassInfo : v_XRequests)
        {
            // 存在方法注解的情况
            if ( Help.isNull(v_ClassInfo.getMethods()) )
            {
                continue;
            }
            
            Object v_Object = null;
            Xjava  v_AnnoID = v_ClassInfo.getClassObj().getAnnotation(Xjava.class);
            String v_XID    = "";
            
            // ref 注解依赖于："id 注解" 或 "XJava配置文件中id关键字实例化的对象"。
            if ( v_AnnoID == null || Help.isNull(v_AnnoID.id()) )
            {
                v_Object = getObject(v_ClassInfo.getClassObj());
                
                if ( v_Object == null )
                {
                    if ( v_AnnoID == null )
                    {
                        throw new NullPointerException("Annotation ID is not exist of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                    }
                    else
                    {
                        // ID为空异常
                        throw new NullPointerException("Annotation ID is null of Class[" + v_ClassInfo.getClassObj().toString() + "].");
                    }
                }
                
                v_XID = getObjectID(v_ClassInfo.getClassObj());
            }
            else
            {
                v_Object = getObject(v_AnnoID.id()); 
                v_XID    = v_AnnoID.id();
            }
            
            if ( v_Object != null )
            {
                for (Method v_Method : v_ClassInfo.getMethods())
                {
                    XRequest v_XRequest = v_Method.getAnnotation(XRequest.class);
                    
                    if ( v_Method.getParameterTypes().length != 1 )
                    {
                        // 方法入参参数应当只能是一个
                        throw new NoSuchMethodException("Method[" + v_Method.getName() + "] parameter count is only one of Class[" + v_ClassInfo.getClassObj().toString() + "]. ");
                    }
                    
                    AppInterface v_AppInterface = new AppInterface();
                    String       v_SID          = Help.NVL(v_XRequest.value() ,v_XRequest.id());
                    
                    if ( Help.isNull(v_SID) )
                    {
                        v_AppInterface.setName(v_Method.getName());
                    }
                    else
                    {
                        v_AppInterface.setName(v_SID);
                    }
                    
                    v_AppInterface.setEmName(v_XID + "." + v_Method.getName());
                    v_AppInterface.setClassName(MethodReflect.getGenerics(v_Method).getName());
                    v_AppInterfaces.put(v_AppInterface.getName() ,v_AppInterface);
                    
                    // ZhengWei(HY) Add 2018-01-20 用注解定义各个系统的接口级消息密钥
                    if ( !Help.isNull(v_XRequest.secrets()) )
                    {
                        for (String v_SysID_Secret : v_XRequest.secrets())
                        {
                            if ( Help.isNull(v_SysID_Secret) )
                            {
                                continue;
                            }
                            
                            String [] v_Arr = StringHelp.replaceAll(v_SysID_Secret ,"=" ,":").split(":");
                            if ( v_Arr.length == 1 )
                            {
                                v_AppMKs.putRow(v_Arr[0] ,v_AppInterface.getName() ,"");
                            }
                            else
                            {
                                // 不用trim()方法去除空格，因为空格也可以是密文
                                v_AppMKs.putRow(v_Arr[0] ,v_AppInterface.getName() ,v_Arr[1]);
                            }
                        }
                    }
                }
            }
            else
            {
                // ID对应的对象还没有被实例化，或者不存在
                throw new NullPointerException("Annotation ID instance object is null of Class[" + v_ClassInfo.getClassObj().toString() + "].");
            }
        }
    }
    
    
	
	/**
	 * 解释Xml文件，并且可以直接将Xml文件内容转为Java对象实例
	 * 
	 * @return
	 * @throws Exception 
	 */
	private Object parserXml() throws Exception
	{
		DocumentBuilderFactory v_DocBuilderFactory = DocumentBuilderFactory.newInstance();   // 建立一个解析器工厂
		DocumentBuilder        v_DocBuilder        = null;                                   // 从解析器工厂生产一个解析器对象
		Document               v_Doc               = null;                                   // 解析器对具体的xml文档进行解析，得到文档对象
		
		try
		{
			v_DocBuilder = v_DocBuilderFactory.newDocumentBuilder();
			if ( this.parserType == 1 )
			{
				v_Doc = v_DocBuilder.parse(this.xmlURL.toString());
			}
			else if ( this.parserType == 2 )
			{
				v_Doc = v_DocBuilder.parse(new InputSource(new StringReader(this.xmlString)));
			}
			else
			{
				return null;
			}
		}
		catch (Exception exce)
		{
			exce.printStackTrace();
			return null;
		}
		
		
		Node     v_RootNode = v_Doc.getChildNodes().item(0);
		NodeList v_NodeList = v_RootNode.getChildNodes();
		
    	
    	for (int v_NodeIndex=0; v_NodeIndex<v_NodeList.getLength(); v_NodeIndex++)
    	{
    		Node v_Node = v_NodeList.item(v_NodeIndex);
    		
    		if ( "#".equals(v_Node.getNodeName().substring(0 ,1)) )
    		{
    			// Nothing.   过滤空标记
    		}
    		else
    		{
	    		if ( $XML_IMPORT.equals(v_Node.getNodeName().trim().toLowerCase()) )
	    		{
	    			String v_Imprt_Name  = getNodeAttribute(v_Node ,$XML_IMPORT_NAME);
	    			String v_Imprt_Class = getNodeAttribute(v_Node ,$XML_IMPORT_CLASS);
	    			
	    			if ( this.imports.containsKey(v_Imprt_Name) )
	    			{
	    				this.imports.remove(v_Imprt_Name);
	    			}
	    			this.imports.put(v_Imprt_Name ,v_Imprt_Class);
	    		}
	    		else
	    		{
	    			// 没有import元素就不解释数据。直到有import元素为止，丛import元素之后的位置开始解释
	    			if ( this.imports.size() >= 1 )
	    			{
	    				if ( this.imports.containsKey(v_Node.getNodeName()) )
	    				{
	    					Class<?> v_Class    = null;
	    					Object   v_Instance = null;
	    					
	    					try
	    					{
	    						v_Class    = Help.forName(this.imports.get(v_Node.getNodeName()));
	    						v_Instance = v_Class.newInstance();
	    					}
	    					catch (Exception exce)
	    					{
	    						exce.printStackTrace();
	    						return null;
	    					}
	    					
	    					
	    					this.setInstance(v_Class ,v_Instance ,v_Node ,new TreeNode<XJavaObject>(this.treeNodeRootKey));
	    					
	    					overWriteXml();
	    					
	    					return v_Instance;
	    				}
	    			}
	    		}
    		}
    	}
    	
    	return null;
	}
	
	
	
	/**
	 * 重写XML。重写的原因主要是因为有对属性值的加密。
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2019-09-04
	 * @version     v1.0
	 *
	 */
	private void overWriteXml()
	{
	    try
	    {
    	    if ( this.xmlURL != null && !Help.isNull(this.encrypts) )
            {
                FileHelp      v_FileHelp = new FileHelp();
                String        v_Content  = v_FileHelp.getContent(this.xmlURL ,"UTF-8" ,true);
                String []     v_Rows     = v_Content.split("\r\n");
                int           v_Index    = 0;
                
                for (int v_RI=0; v_RI<v_Rows.length; v_RI++)
                {
                    String v_Row = v_Rows[v_RI];
                    
                    if ( v_Row.indexOf($XML_OBJECT_ENCRYPT) >= 0 )
                    {
                        XJavaEncrypt v_XJE = this.encrypts.get(v_Index);
                        String       v_Old = "<" + v_XJE.getNodeName() + " " + $XML_OBJECT_ENCRYPT + "=\"" + v_XJE.getEncrypt() + "\">" +                             v_XJE.getValue()        + "</" + v_XJE.getNodeName() + ">";
                        String       v_New = "<" + v_XJE.getNodeName() + " " + $XML_OBJECT_ENCRYPT + "=\"" + v_XJE.getEncrypt() + "\">" + $XML_OBJECT_ENCRYPT + ":" + v_XJE.getValueEncrypt() + "</" + v_XJE.getNodeName() + ">";
                        
                        if ( v_Row.indexOf(v_Old) >= 0 )
                        {
                            v_Rows[v_RI] = StringHelp.replaceFirst(v_Row ,v_Old ,v_New);
                            v_Index++;
                            
                            if ( v_Index >= this.encrypts.size() )
                            {
                                break;
                            }
                        }
                    }
                }
                
                if ( v_Index > 0 )
                {
                    v_FileHelp.setOverWrite(true);
                    v_FileHelp.create(this.xmlURL ,v_Rows ,"UTF-8");
                }
            }
	    }
	    catch (Exception exce)
	    {
	        exce.printStackTrace();
	    }
	}
	
	
	
	/**
	 * 判断 IF    关键字
	 * 判断 IFNot 关键字
	 * 
	 * @param i_IFObj
	 * @param i_Value
	 * @return
	 */
	private boolean key_IF(Object i_IFObj ,boolean i_Value)
	{
	    if ( i_IFObj == null )
        {
	        return false;
        }
        else if ( i_IFObj.getClass() == Boolean.class || i_IFObj.getClass() == boolean.class )
        {
            if ( (Boolean)i_IFObj == i_Value )
            {
                return true;
            }
        }
        else if ( i_IFObj.getClass() == String.class  )
        {
            if ( (Boolean.parseBoolean(i_IFObj.toString())) == i_Value )
            {
                return true;
            }
        }
        else if ( i_IFObj.getClass() == Return.class )
        {
            if ( ((Return<?>)i_IFObj).booleanValue() == i_Value )
            {
                return true;
            }
        }
        else
        {
            return false;
        }
	    
	    return false;
	}
	
	
	
	/**
     * 判断 IF    关键字
     * 判断 IFNot 关键字
     * 
     * @param i_IFObj_Left
     * @param i_IFObj_Right
     * @return
     */
    private boolean key_IF(Object i_IFObj_Left ,Object i_IFObj_Right)
    {
        if ( i_IFObj_Left.equals(i_IFObj_Right) )
        {
            return true;
        }
        else
        {
            if ( this.key_IF(i_IFObj_Left ,true) )
            {
                if ( this.key_IF(i_IFObj_Right ,true) )
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    
    /**
     * 加密属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-09-04
     * @version     v1.0
     *
     * @param i_SuperNode      父节点对象
     * @param i_Node           本节点对象
     * @param i_NodeValue      节点的数值
     * @return
     */
    private Object encrypt(Node i_SuperNode ,Node i_Node ,Object i_NodeValue)
    {
        if ( i_NodeValue == null || Help.isNull(i_NodeValue.toString()) )
        {
            return i_NodeValue;
        }
        
        String v_Encrypt = getNodeAttribute(i_Node ,$XML_OBJECT_ENCRYPT);
        if ( Help.isNull(v_Encrypt) )
        {
            return i_NodeValue;
        }
        
        String v_ID    = getNodeAttribute(i_SuperNode ,$XML_OBJECT_ID);
        Des    v_Des   = new Des(Help.NVL(v_ID) + "-" + i_Node.getNodeName() + "-" + Help.NVL(v_Encrypt));
        String v_Value = i_NodeValue.toString();
        
        if ( v_Value.startsWith($XML_OBJECT_ENCRYPT + ":") )
        {
            return v_Des.decrypt(v_Value.substring($XML_OBJECT_ENCRYPT.length() + 1));
        }
        else
        {
            XJavaEncrypt v_XJE = new XJavaEncrypt();
            
            v_XJE.setNodeName(    i_Node.getNodeName());
            v_XJE.setSuperID(     v_ID);
            v_XJE.setEncrypt(     v_Encrypt);
            v_XJE.setValue(       v_Value);
            v_XJE.setValueEncrypt(v_Des.encrypt(v_Value));
            
            this.encrypts.add(v_XJE);
            return i_NodeValue;
        }
    }
	
	
	
	/**
	 * 设置实例的值
	 * 
	 * @param i_SuperClass
	 * @param io_SuperInstance
	 * @param i_SuperNode
	 * @param i_SuperTreeNode
	 * @return
	 * @throws Exception 
	 */
    private Object setInstance(Class<?> i_SuperClass ,Object io_SuperInstance ,Node i_SuperNode ,TreeNode<XJavaObject> i_SuperTreeNode) throws Exception
	{
		NodeList v_NodeList  = i_SuperNode.getChildNodes();
		int      v_NodeIndex = 1;
		
		
		// v_NodeIndex 为有效的节点序号
		for (int v_Index=0; v_Index<v_NodeList.getLength(); v_Index++ ,v_NodeIndex++)
		{
			Node v_Node = v_NodeList.item(v_Index);
	
			if ( "#".equals(v_Node.getNodeName().substring(0 ,1)) )
			{
				// Nothing.   过滤空标记
				v_NodeIndex--;
			}
			else
			{
				Class<?>              v_AttrClass         = null;
				Object                v_AttrInstance      = null;
				String                v_RefID             = getNodeAttribute(v_Node ,$XML_OBJECT_REF);
				String                v_ClassName         = getNodeAttribute(v_Node ,$XML_OBJECT_CLASS);
				String                v_ID                = getNodeAttribute(v_Node ,$XML_OBJECT_ID);
				String                v_This              = getNodeAttribute(v_Node ,$XML_OBJECT_THIS);
				String                v_If                = getNodeAttribute(v_Node ,$XML_OBJECT_IF);
				String                v_IfNot             = getNodeAttribute(v_Node ,$XML_OBJECT_IFNOT);
				String                v_TreeNodeOrderByID = StringHelp.lpad(v_NodeIndex ,$TREE_NODE_ORDERBYID_MAXLEN ,"0");
				TreeNode<XJavaObject> v_TreeNode          = new TreeNode<XJavaObject>(v_TreeNodeOrderByID ,v_ID ,i_SuperTreeNode);
                boolean               v_SuperInstance_New = false;    // 父类是否在本节点被实例化
                boolean               v_ThisFun           = false;    // 是否为This赋值功能 
				
                
                // 真值才解释XJava
                if ( !Help.isNull(v_If) )
                {
                    String [] v_IfORArr     = v_If.split($XML_OBJECT_IF_OR);
                    String [] v_IfEqualsArr = v_If.split($XML_OBJECT_IF_EQUALS);
                    if ( v_IfEqualsArr.length != 2 )
                    {
                        Object v_IfObj = this.getRefObject(io_SuperInstance ,v_Node ,v_If);
                        
                        if ( v_IfObj == null )
                        {
                            v_IfObj = v_If;
                        }
                        
                        if ( !this.key_IF(v_IfObj ,true) )
                        {
                            v_NodeIndex--;
                            continue;
                        }
                    }
                    else
                    {
                        Object v_IfObj_Left  = this.getRefObject(io_SuperInstance ,v_Node ,v_IfEqualsArr[0].trim());
                        Object v_IfObj_Right = this.getRefObject(io_SuperInstance ,v_Node ,v_IfEqualsArr[1].trim());
                        
                        if ( v_IfObj_Left == null )
                        {
                            v_IfObj_Left = v_IfEqualsArr[0].trim();
                        }
                        
                        if ( v_IfObj_Right == null )
                        {
                            v_IfObj_Right = v_IfEqualsArr[1].trim();
                        }
                        
                        if ( !this.key_IF(v_IfObj_Left ,v_IfObj_Right) )
                        {
                            v_NodeIndex--;
                            continue;
                        }
                    }
                }
                
                // 假值才解释XJava
                if ( !Help.isNull(v_IfNot) )
                {
                    String [] v_IfArr = v_IfNot.split($XML_OBJECT_IF_EQUALS);
                    if ( v_IfArr.length != 2 )
                    {
                        Object v_IfNotObj = this.getRefObject(io_SuperInstance ,v_Node ,v_IfNot);
                        
                        if ( v_IfNotObj == null )
                        {
                            v_IfNotObj = v_IfNot;
                        }
                        
                        if ( !this.key_IF(v_IfNotObj ,false) )
                        {
                            v_NodeIndex--;
                            continue;
                        }
                    }
                    else
                    {
                        Object v_IfObj_Left  = this.getRefObject(io_SuperInstance ,v_Node ,v_IfArr[0].trim());
                        Object v_IfObj_Right = this.getRefObject(io_SuperInstance ,v_Node ,v_IfArr[1].trim());
                        
                        if ( v_IfObj_Left == null )
                        {
                            v_IfObj_Left = v_IfArr[0].trim();
                        }
                        
                        if ( v_IfObj_Right == null )
                        {
                            v_IfObj_Right = v_IfArr[1].trim();
                        }
                        
                        if ( this.key_IF(v_IfObj_Left ,v_IfObj_Right) )
                        {
                            v_NodeIndex--;
                            continue;
                        }
                    }
                }
                
                
                // 标记有 this 属性，以实现赋值功能
                if ( !Help.isNull(v_This) )
                {
                    if ( $XML_OBJECTS.containsNodeID(v_This) )
                    {
                        v_AttrInstance = $XML_OBJECTS.getByNodeID(v_This).getInfo().getObject();
                        v_ThisFun      = true;
                    }
                    else
                    {
                        // 同时有 ID 和 This 两属性，并它两相同
                        if ( v_This.equals(v_ID) )
                        {
                            // Noting.
                        }
                        else
                        {
                            throw new Exception("This[" + v_This + "] is exist of Node[" + i_SuperNode.getParentNode().getNodeName() + "." + i_SuperNode.getNodeName() + "].");
                        }
                    }
                }
                
				
				// 标记有 id 的节点都将存入 $XML_OBJECTS 集合中
				if ( v_ID != null && !"".equals(v_ID.trim()) )
				{
					if ( $XML_OBJECTS.containsNodeID(v_ID) )
					{
					    // ID 与 This 两属性相同。
					    // 类似于Java语言中下面的情况
					    // String v_Name = "HY";
					    // v_Name = v_Name;
					    if ( v_ThisFun && v_ID.endsWith(v_This) )
					    {
					        // Nothing.
					    }
					    else
					    {
					        // ZhengWei(HY) Del 2016-01-04 删除下面的重复抛错，改为重复覆盖 
					        // throw new Exception("ID[" + v_ID + "] is exist of Node[" + i_SuperNode.getParentNode().getNodeName() + "." + i_SuperNode.getNodeName() + "].");
					        $XML_OBJECTS.put(v_TreeNode);
					    }
					}
					else
					{
					    $XML_OBJECTS.put(v_TreeNode);
					}
				}
				
				
				// 第一个子节点时
				if ( v_NodeIndex == 1 )
				{
					// 构造器关键字
					if ( $XML_OBJECT_CONSTRUCTOR.equalsIgnoreCase(v_Node.getNodeName()) )
					{
						if ( io_SuperInstance == null )
						{
							io_SuperInstance = this.constructor(i_SuperClass ,v_Node ,v_TreeNode);
                            v_SuperInstance_New = true;
						}
						else
						{
							// 实例已被构造，将不做任何处理
						}
						
						continue;
					}
					else
					{
						// 父节点的Java类，不再简单的实例化，而是先判断是否有指定的构造器，如果没有的情况下，才简单的实例化。
						if ( i_SuperClass != null && io_SuperInstance == null )
						{
							int v_SuperModifiers = i_SuperClass.getModifiers();
							
							// 判断父节点的Java类的是为接口、抽象类、静态类
							if ( !Modifier.isInterface(v_SuperModifiers) 
							  && !Modifier.isAbstract(v_SuperModifiers) 
							  && !Modifier.isStatic(v_SuperModifiers) )
							{
								if ( i_SuperClass.getDeclaredConstructors().length >= 1 )
								{
									try
									{
										io_SuperInstance = i_SuperClass.newInstance();
                                        v_SuperInstance_New = true;
									}
									catch (Exception exce)
									{
										throw new ClassNotFoundException("New instance [" + i_SuperClass.toString() + "] exception of Node[" + i_SuperNode.getParentNode().getNodeName() + "." + i_SuperNode.getNodeName() + "].");
									}
								}
							}
						}
					}
				}
				
				
				// 当节点属性有引用关键字时
				if ( v_RefID != null )
				{
					v_AttrInstance = this.getRefObject(io_SuperInstance ,v_Node ,v_RefID);
					if ( v_AttrInstance != null )
					{
						v_AttrClass = v_AttrInstance.getClass();
						
						if ( "String".equalsIgnoreCase(v_Node.getNodeName()) )
						{
						    v_TreeNode.setInfo(new XJavaObject("" ,v_AttrInstance));
						}
					}
					else
					{
					    throw new NullPointerException("RefID[" + v_RefID + "] instance object is not exist.");
					}
				}
				
				
				// 当节点为Call时
				if ( $XML_OBJECT_CALL.equalsIgnoreCase(v_Node.getNodeName()) )
				{
					if ( io_SuperInstance != null )
					{
						this.callMethod(io_SuperInstance ,v_Node ,v_TreeNode);
					}
				}
				// 标记有 this 属性，以实现赋值功能
				else if ( v_ThisFun )
				{
				    this.setInstance(v_AttrInstance.getClass() ,v_AttrInstance ,v_Node ,v_TreeNode);
				}
				// 当节点为类时 或是 String 类时
				else if ( v_ClassName != null || this.imports.containsKey(v_Node.getNodeName()) || "String".equalsIgnoreCase(v_Node.getNodeName()) )
				{
					boolean       v_IsDefaultSetMethod = false;
					MethodReflect v_Setter             = null;
					
					if ( v_ClassName == null )
					{
						if ( "String".equalsIgnoreCase(v_Node.getNodeName()) )
						{
							v_ClassName = "java.lang.String";
						}
						else
						{
							v_ClassName = this.imports.get(v_Node.getNodeName());
						}
					}
					
					if ( i_SuperClass != null )
					{
						String v_SuperSetMethodName = null;
						
						// 尝试获取指定的setter方法名称
						try
						{
							v_SuperSetMethodName = getNodeAttribute(i_SuperNode ,$XML_OBJECT_SETTER);
						}
						catch (Exception exce)
						{
							v_SuperSetMethodName = null;
						}
						
						
						if ( v_SuperSetMethodName != null )
						{
							// 尝试获取指定的setter方法
							try
							{
								v_Setter = new MethodReflect(io_SuperInstance ,v_SuperSetMethodName ,MethodReflect.$NormType_Setter);
							}
							catch (Exception exce)
							{
								throw new NoSuchMethodException("Setter method [" + v_SuperSetMethodName + "] is't exist of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
							}
						}
						
						
						if ( v_Setter == null )
						{
							// 当没有指定的setter方法时，尝试获取默认的setter方法
							try
							{
								v_Setter = new MethodReflect(io_SuperInstance ,v_Node.getNodeName() ,true ,MethodReflect.$NormType_Setter);
							}
							catch (Exception exce)
							{
								v_Setter = null;  // 允许出错，即允许没有默认的setter方法
							}
							v_IsDefaultSetMethod = true;
						}
					}
					
					
					// 当Setter的参数又是一个对象实例时，并且指定setter方法时
					if ( v_Setter != null && !v_IsDefaultSetMethod )
					{
						if ( v_AttrInstance == null )
						{
							try
							{
								v_AttrClass    = Help.forName(v_ClassName);
								
								// 这里也可以与下一个else if一样，不需要此句。
								// 但必须实现 setter 节点支持定义入参的类型及入参个数
								v_AttrInstance = v_AttrClass.newInstance();
							}
							catch (Exception exce)
							{
								throw new ClassNotFoundException("Setter method [" + v_Setter.getMethodURL() + "] is't exist of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
							}
							
							v_AttrInstance = this.setInstance(v_AttrClass ,v_AttrInstance ,v_Node ,v_TreeNode);
						}
						
						try
						{
							v_Setter.invoke(v_AttrInstance);
						}
						catch (Exception exce)
						{
							throw new NoSuchMethodException("Execute Setter method [" + v_Setter.getMethodURL() + "] is't exist of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
						}
					}
					// 当Setter的参数又是一个对象实例时，并且有默认的setter方法时
					else if ( v_Setter != null && v_IsDefaultSetMethod )
					{
						if ( v_AttrInstance == null )
						{
							try
							{
								v_AttrClass = Help.forName(v_ClassName);
							}
							catch (Exception exce)
							{
								throw new ClassNotFoundException("Setter method [" + v_Setter.getMethodURL() + "] is't exist of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
							}
							
							v_AttrInstance = this.setInstance(v_AttrClass ,null ,v_Node ,v_TreeNode);
						}
						
						try
						{
							v_Setter.invoke(v_AttrInstance);
						}
						catch (Exception exce)
						{
							throw new NoSuchMethodException("Execute Setter method [" + v_Setter.getMethodURL() + "] is't exist of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
						}
					}
					// 没有指定的setter方法，也没有匹配到默认的setter方法
					else
					{
						if ( v_AttrInstance == null )
						{
							try
							{
								v_AttrClass = Help.forName(v_ClassName);
							}
							catch (Exception exce)
							{
                                if ( v_ID != null )
                                {
                                    throw new ClassNotFoundException("Exception of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "]. ID is [" + v_ID + "]. Class is [" + v_ClassName + "]");
                                }
                                else
                                {
                                    throw new ClassNotFoundException("Exception of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "]. Class is [" + v_ClassName + "]");
                                }
							}
							
							// 字符串类型特殊的对待
							if ( String.class == v_AttrClass )
							{
								String v_NodeValue = getNodeTextContent(v_Node);
								v_NodeValue = (String)this.encrypt(i_SuperNode ,v_Node ,v_NodeValue);
								v_AttrInstance = v_AttrClass.getConstructor(String.class).newInstance(StringHelp.replaceAll(v_NodeValue ,$XML_Replace_Keys ,false).replaceAll($XML_CLASSPATH ,this.xmlClassPath));
							}
							else
							{
								v_AttrInstance = this.setInstance(v_AttrClass ,null ,v_Node ,v_TreeNode);
							}
						}
						
						if ( io_SuperInstance == null )
						{
							// 此处必须返回，即无任何setter方法，并且父节点尚未实例化时，只允许存在一个实例化对象。
							return v_AttrInstance;
						}
						else
						{
							// 当对象实例为List或Set集合
							if ( io_SuperInstance instanceof Collection )
							{
								try 
								{
									Method v_Method = i_SuperClass.getMethod($XML_LIST_DEF_SETTER ,Object.class);
									
									v_Method.invoke(io_SuperInstance ,v_AttrInstance);
								} 
								catch (Exception exce) 
								{
									throw new NoSuchMethodException("Execute List method [add] of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
								}
							}
							// 当对象实例为Map集合
							else if ( io_SuperInstance instanceof Map )
							{
								try 
								{
									String v_Key        = getNodeAttribute(i_SuperNode ,$XML_MAP_KEY);
									Method v_AttrMethod = MethodReflect.getGetMethod(v_AttrClass , v_Key ,true);
									if ( v_AttrMethod == null )
									{
										v_AttrMethod = MethodReflect.getGetMethod(v_AttrClass , v_Key ,false);
									}
									Object v_KeyValue   = v_AttrMethod.invoke(v_AttrInstance);
									Method v_Method     = i_SuperClass.getMethod($XML_MAP_DEF_SETTER ,Object.class ,Object.class);
									
									v_Method.invoke(io_SuperInstance ,v_KeyValue ,v_AttrInstance);
								} 
								catch (Exception exce) 
								{
									throw new NoSuchMethodException("Execute Map method [add] of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
								}
							}
                            // 父节的实例类型与本节点的实例类型相同时，有可能其父类的Setter的参数又是一个对象实例
                            else if ( v_AttrInstance != null && v_AttrInstance.getClass().equals(io_SuperInstance.getClass()) )
                            {
                                if ( v_SuperInstance_New )
                                {
                                    return v_AttrInstance;
                                }
                                else
                                {
                                    throw new Exception("Unknown exception.");
                                }
                            }
                            else
                            {
                                throw new Exception("Unknown Class type.");
                            }
						}
						
					}
				}
				// 使用setter方法设置对象实例的属性
				else
				{
					if ( v_AttrInstance == null || v_RefID != null )
					{
						List<Method>        v_SetMethods = MethodReflect.getSetMethods(i_SuperClass, v_Node.getNodeName() ,true);
						Object              v_ParamValue = null;
						Map<String ,Object> v_SubmitMap  = getChildObjects(this.getNodeAttribute(v_Node ,$XML_OBJECT_SUBMIT));
						
						if ( v_RefID != null )
						{
							v_ParamValue = v_AttrInstance;
						}
						// 按树目录结构，获取子树目录中 XJava 解释过的所有实例化对象信息
						else if ( !Help.isNull(v_SubmitMap) )
						{
							v_ParamValue = v_SubmitMap;
						}
						// 当节点没有明确说明Java类型时，但其节点下又有多个子节点时
						else if ( getChildNodesSize(v_Node) >= 1 )
						{
							// 本节点 v_Node 在父节点有 setter 方法时，从setter方法的入参中获取本节点的Java的Class类型 
							if ( !Help.isNull(v_SetMethods) )
							{
								try
								{
									v_AttrClass  = v_SetMethods.get(0).getParameterTypes()[0];
                                    v_ParamValue = this.setInstance(v_AttrClass ,v_AttrInstance ,v_Node ,v_TreeNode);
								}
								catch (Exception exce)
								{
									throw new InstantiationException("Instantiation error of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].\n" + exce.getMessage());
								}
							}
							else
							{
								throw new InstantiationException("Instantiation error of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "].");
							}
						}
						else
						{
                            v_ParamValue = getNodeTextContent(v_Node);
							
							// 当节点值不存在时
							if ( v_ParamValue == null || "".equals(v_ParamValue) )
							{
								v_ParamValue = null;
							}
						}
						
						Object v_EncryptValue = this.encrypt(i_SuperNode ,v_Node ,v_ParamValue);
						if ( Help.isNull(v_SetMethods) )
						{
						    // 对无Setter方法的成员属性赋值  ZhengWei(HY) Add 2017-11-24
						    Field v_Field = FieldReflect.get(i_SuperClass ,v_Node.getNodeName());
						    
						    if ( v_Field != null )
						    {
						        try
						        {
						            FieldReflect.set(v_Field ,io_SuperInstance ,v_EncryptValue ,this.replaces);
						        }
						        catch (Exception exce)
	                            {
	                                throw new IllegalAccessException("Field setter value[" + v_ParamValue + "] of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "] ,in Class[" + i_SuperClass.getName() + "].\n" + exce.getMessage());
	                            }
						    }
						}
						else
						{
						    Method v_SetMethod = null ;
						    int    v_MSize     = v_SetMethods.size();
						    if ( v_MSize > 1 )
						    {
						        // 尝试Setter方法重载时，方法参数类型的匹配 ZhengWei(HY) Add 2018-05-04
						        for (int i=0; i<v_MSize; i++)
    						    {
    						        Method v_MTemp = v_SetMethods.get(i);
    						        
    						        if ( MethodReflect.isExtendImplement(v_ParamValue ,v_MTemp.getParameterTypes()[0]) )
    						        {
    						            v_SetMethod = v_MTemp;
    						            break;
    						        }
    						    }
						        
						        if ( v_SetMethod == null )
						        {
						            if ( v_ParamValue.getClass().equals(String.class) )
						            {
						                Class<?> v_ParamClass = Help.getClass(v_ParamValue.toString());
						                
						                for (int i=0; i<v_MSize; i++)
		                                {
		                                    Method v_MTemp = v_SetMethods.get(i);
		                                    
		                                    if ( MethodReflect.isExtendImplement(v_ParamClass ,v_MTemp.getParameterTypes()[0]) )
		                                    {
		                                        v_SetMethod = v_MTemp;
		                                        break;
		                                    }
		                                }
						            }
						            else
						            {
						                // Nothing. 暂时没有想好怎么处理
						            }
						        }
						    }
						    else
						    {
						        v_SetMethod = v_SetMethods.get(0);
						    }
						    
							try
							{
							    MethodReflect.invokeSet(v_SetMethod ,io_SuperInstance ,v_EncryptValue ,this.replaces);
							}
							catch (Exception exce)
							{
							    String v_Msg = "";
							    if ( null != exce.getCause()
							      && null != exce.getCause().getMessage() )
							    {
							        v_Msg += exce.getCause().getMessage() + "\n";
							    }
							    throw new NoSuchMethodException("Execute setter value[" + v_ParamValue + "] of Node[" + v_Node.getParentNode().getNodeName() + "." + v_Node.getNodeName() + "] ,in Class[" + i_SuperClass.getName() + "].\n" + v_Msg + exce.getMessage());
							}
						}
					}
					
				}
			}
		}
		
		
		// 父节点解释出的Java类，不再简单的实例化，而是先判断是否有指定的构造器，如果没有的情况下，才简单的实例化。
		// v_NodeIndex=1表示没有有效的子节点
		if ( v_NodeIndex == 1 && i_SuperClass != null && io_SuperInstance == null )
		{
			int v_SuperModifiers = i_SuperClass.getModifiers();
			
			// 判断父节点的Java类的是为接口、抽象类、静态类
			if ( !Modifier.isInterface(v_SuperModifiers) 
			  && !Modifier.isAbstract(v_SuperModifiers) 
			  && !Modifier.isStatic(v_SuperModifiers) )
			{
				if ( i_SuperClass.getDeclaredConstructors().length >= 1 )
				{
					try
					{
						io_SuperInstance = i_SuperClass.newInstance();
					}
					catch (Exception exce)
					{
						throw new ClassNotFoundException("New instance [" + i_SuperClass.toString() + "] exception of Node[" + i_SuperNode.getParentNode().getNodeName() + "." + i_SuperNode.getNodeName() + "].\n" + exce.getMessage());
					}
				}
			}
		}
		
		
		// 标记有 id 的节点都已存入 $XML_OBJECTS 集合中，此时将 i_SuperInstance 实例化对象 setInfo() 节点中。
		if ( io_SuperInstance != null )
		{
			if ( i_SuperTreeNode != null && !Help.isNull(i_SuperTreeNode.getNodeID()) )
			{
                // 判断是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
                String  v_IsNewValue = getNodeAttribute(i_SuperNode ,$XML_OBJECT_NEWOBJECT);
                boolean v_IsNew      = false;
                
                if ( v_IsNewValue != null && !"".equals(v_IsNewValue.trim()) )
                {
                    v_IsNew = Boolean.parseBoolean(v_IsNewValue);
                }
                    
				i_SuperTreeNode.setInfo(new XJavaObject(i_SuperTreeNode.getNodeID() ,io_SuperInstance ,v_IsNew));
			}
		}
		
		return io_SuperInstance;
	}
    
    
    
    @SuppressWarnings("unused")
    private String getNodeXMLString(Node i_Node)
    {
        NodeList      v_NodeList  = i_Node.getChildNodes();
        int           v_NodeIndex = 1;
        StringBuilder v_Buffer    = new StringBuilder();
        
        
        v_Buffer.append("<").append(i_Node.getNodeName());
        
        // v_NodeIndex 为有效的节点序号
        for (int v_Index=0; v_Index<v_NodeList.getLength(); v_Index++ ,v_NodeIndex++)
        {
            Node v_Node = v_NodeList.item(v_Index);
    
            if ( "#".equals(v_Node.getNodeName().substring(0 ,1)) )
            {
                // Nothing.   过滤空标记
                v_NodeIndex--;
            }
            else
            {
                if ( v_NodeIndex == 1 )
                {
                    v_Buffer.append(">");
                }
                
                v_Buffer.append(getNodeXMLString(v_Node));
            }
        }
        
        if ( v_NodeIndex <= 1 )
        {
            v_Buffer.append(" />");
        }
        else
        {
            v_Buffer.append("</").append(i_Node.getNodeName()).append(">");
        }
        
        return v_Buffer.toString();
    }
    
    
    
    /**
     * 在桌面级应用中使用 i_Node.getTextContent() 方法就可以获取 <xxx>HY</xxx> 格式中的 HY 字符。
     * 
     * 但将 XJava 应用在 Web 程序中，上面的方法就可抛未实现异常。
     * 固改为使用 i_Node.getChildNodes().item(0).getNodeValue() 方法。
     * 
     * 异常的原因尚不明确，只是问题暂时得到了解决。
     * 
     * ZhengWei(HY) Add 2013-07-14
     * 
     * @param i_Node
     * @return
     */
    private static String getNodeTextContent(Node i_Node)
    {
        try
        {
            return i_Node.getTextContent().trim();
        }
        catch (Exception exce)
        {
            NodeList v_NodeList = i_Node.getChildNodes();
            
            if ( v_NodeList.getLength() >= 1 )
            {
                return v_NodeList.item(0).getNodeValue().trim();
            }
            else
            {
                return "";
            }
        }
    }
	
	
	
	/**
	 * 构造器。对写有 "constructor" 关键字节点的父节点进行指定构造器的构造。
	 * 
	 * @param i_ConstructorClass
	 * @param i_ConstructorNode
	 * @param i_ConstructorTreeNode
	 * @return
	 * @throws Exception
	 */
	private Object constructor(Class<?> i_ConstructorClass ,Node i_ConstructorNode ,TreeNode<XJavaObject> i_ConstructorTreeNode) throws Exception
	{
		List<Class<?>> v_ParamClassList       = new ArrayList<Class<?>>();
		List<Object>   v_ParamValueList       = new ArrayList<Object>();
		List<Boolean>  v_ParamClassChangeList = new ArrayList<Boolean>();       // 方法入参的Class类型是否可变，即可以取Class类型的父Class类型
		int            v_ParamCount           = 0;
		NodeList       v_NodeList             = i_ConstructorNode.getChildNodes();
		for (int v_Index=0; v_Index<v_NodeList.getLength(); v_Index++)
		{
			Node v_ParamNode = v_NodeList.item(v_Index);
			
			if ( "#".equals(v_ParamNode.getNodeName().substring(0 ,1)) )
			{
				// Nothing.   过滤空标记
			}
			else
			{
				v_ParamCount++;
				String   v_ParamClassName = this.getNodeAttribute(v_ParamNode ,$XML_OBJECT_CLASS);
				Class<?> v_ParamClass     = null;
				Object   v_ParamValue     = null;
				String   v_RefID          = this.getNodeAttribute(v_ParamNode ,$XML_OBJECT_REF);
				
				
				// 标记有Class节点属性的情况
				if ( v_ParamClassName != null )
				{
					try
					{
						v_ParamClass = Help.forName(v_ParamClassName);
						v_ParamClassChangeList.add(Boolean.FALSE);
					}
					catch (Exception exce)
					{
						throw exce;
					}
				}
				// 节点名称为import指定的Class类型
				else if ( this.imports.containsKey(v_ParamNode.getNodeName()) )
				{
					try
					{
						v_ParamClass = Help.forName(this.imports.get(v_ParamNode.getNodeName()));
						v_ParamClassChangeList.add(Boolean.TRUE);
					}
					catch (Exception exce)
					{
						throw exce;
					}
				}
				// 当节点属性有引用关键字时，获取调用方法的参数值
				else if ( v_RefID != null )
				{
					v_ParamValue = this.getRefObject(null ,v_ParamNode ,v_RefID);
					
					if ( v_ParamValue != null )
					{
						v_ParamClass = v_ParamValue.getClass();
						v_ParamClassChangeList.add(Boolean.TRUE);
					}
					else
					{
						throw new NullPointerException("Ref method [" + v_RefID + "] is't exist of Constructor Node[" + i_ConstructorNode.getParentNode().getNodeName() + "." + i_ConstructorNode.getNodeName() + "].");
					}
				}
				else if ( $XML_JAVA_DATATYPE_CHAR.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = char.class;
					v_ParamValue = getNodeTextContent(v_ParamNode).charAt(0);
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_INT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = int.class;
					v_ParamValue = Integer.valueOf(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_LONG.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = long.class;
                    v_ParamValue = Long.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_BIGDECIMAL.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = BigDecimal.class;
                    v_ParamValue = new BigDecimal(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_DOUBLE.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = double.class;
                    v_ParamValue = Double.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_FLOAT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = float.class;
					v_ParamValue = Double.valueOf(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_BOOLEAN.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = boolean.class;
					v_ParamValue = Boolean.valueOf(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_STRING.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = String.class;
					v_ParamValue = getNodeTextContent(v_ParamNode);
					if ( v_ParamValue != null )
					{
						v_ParamValue = StringHelp.replaceAll(v_ParamValue.toString() ,$XML_Replace_Keys ,false).replaceAll($XML_CLASSPATH ,this.xmlClassPath);
					}
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_DATE.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = Date.class;
					v_ParamValue = new Date().setDate(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.TRUE);
				}
				else if ( $XML_JAVA_DATATYPE_OBJECT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = Object.class;
					v_ParamValue = getNodeTextContent(v_ParamNode);
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_CLASS.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = Class.class;
                    v_ParamValue = getNodeTextContent(v_ParamNode);
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_BYTE.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = byte.class;
                    v_ParamValue = Byte.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_SHORT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = short.class;
                    v_ParamValue = Short.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else
				{
					// 没有找到调用方法参数的Clss类型
					throw new NullPointerException("Method param[" + v_ParamCount + "] Class Type is not find of Constructor Node[" + i_ConstructorNode.getParentNode().getNodeName() + "." + i_ConstructorNode.getNodeName() + "].");
				}
				
				
				v_ParamClassList.add(v_ParamClass);
				
				
				if ( v_ParamValue == null )
				{
					// 当class属性与ref属性同时存在时
					if ( v_RefID != null )
					{
						v_ParamValue = this.getRefObject(null ,v_ParamNode ,v_RefID);
						
						if ( v_ParamValue != null )
						{
							if ( v_ParamClass == null )
							{
								v_ParamClass = v_ParamValue.getClass();
							}
							
							v_ParamValueList.add(v_ParamValue);
						}
						else
						{
							// 引用对象不存在
							throw new NullPointerException("Ref method [" + v_RefID + "] is't exist of Constructor Node[" + i_ConstructorNode.getParentNode().getNodeName() + "." + i_ConstructorNode.getNodeName() + "].");
						}
					}
					else
					{
						v_ParamValue = this.setInstance(v_ParamClass ,null ,v_ParamNode ,i_ConstructorTreeNode);
						
						if ( v_ParamValue != null )
						{
							v_ParamValueList.add(v_ParamValue);
						}
						else
						{
							// 方法参数的值没有解释成功
							throw new NullPointerException("Method param[" + v_ParamCount + "] is null of Constructor Node[" + i_ConstructorNode.getParentNode().getNodeName() + "." + i_ConstructorNode.getNodeName() + "].");
						}
					}
				}
				else
				{
					v_ParamValueList.add(v_ParamValue);
				}
			
			}
			
		}
		
		
		Constructor<?> v_Constructor = null;
		if ( v_ParamClassList.size() == 0 )
		{
			return i_ConstructorClass.newInstance();
		}
		else
		{	
			v_Constructor = this.constructor_GetConstructor(i_ConstructorClass 
					                                       ,v_ParamClassList
					                                       ,v_ParamClassChangeList);
			
			if ( v_Constructor != null )
			{
				return v_Constructor.newInstance(v_ParamValueList.toArray());
			}
			else
			{
				// 没有匹配到对应的方法
				throw new ClassNotFoundException("Constructor not found of Constructor Node[" + i_ConstructorNode.getParentNode().getNodeName() + "." + i_ConstructorNode.getNodeName() + "].");
			}
		}
	}
	
	
	
	/**
	 * 找到对应参数数量、参数类型的构造器
	 * 
	 * @param i_Class                 类Class类型
	 * @param i_ParamClassList        方法入参的Class列表
	 * @param i_ParamClassChangeList  方法入参的Class类型是否可变，即可以取Class类型的父Class类型
	 * @return
	 * 
	 * @version     v2.0  2017-02-13  添加：对构造器的参数类型判定方法上，引用 MethodReflect.isExtendImplement(...) 方法提高识别的准确性。
	 */
	private Constructor<?> constructor_GetConstructor(Class<?> i_Class ,List<Class<?>> i_ParamClassList ,List<Boolean> i_ParamClassChangeList)
	{
		int               v_ParamSize       = i_ParamClassList.size();
		Constructor<?> [] v_ConstructorList = i_Class.getDeclaredConstructors();
		
		
		if ( v_ConstructorList.length == 0 )
		{
			return null;
		}
		else if ( v_ConstructorList.length == 1 )
		{
			return v_ConstructorList[0];
		}
		
		
		for (int i=0; i<v_ConstructorList.length; i++)
		{
			Constructor<?> v_Constructor = v_ConstructorList[i];
			Class<?> []    v_ParamArr    = v_Constructor.getParameterTypes();
			int            v_ParamIndex  = 0;
			
			if ( v_ParamArr.length == v_ParamSize )
			{
				for (; v_ParamIndex<v_ParamSize; v_ParamIndex++)
				{
					Class<?> v_ParamClass = v_ParamArr[v_ParamIndex];
					
					if ( v_ParamClass.equals(i_ParamClassList.get(v_ParamIndex)) )
					{
						// Nothing.  匹配成功
					}
					else if ( MethodReflect.isExtendImplement(i_ParamClassList.get(v_ParamIndex) ,v_ParamClass) )
					{
					    // Nothing.  匹配成功
					}
					else if ( i_ParamClassChangeList.get(v_ParamIndex) )
					{
						boolean v_MatchResult = this.constructor_ParamMatch(v_ParamClass ,i_ParamClassList.get(v_ParamIndex).getSuperclass());
						
						if ( !v_MatchResult )
						{
							v_ParamIndex = v_ParamSize + 99;
						}
					}
					else
					{
						v_ParamIndex = v_ParamSize + 99;
					}
				}
				
				if ( v_ParamIndex == v_ParamSize )
				{
					return v_Constructor;
				}
			}
		}
		
		
		return null;
	}
	
	
	
	/**
	 * 匹配入参的Class类型与XML配置的Class类型是否相同。
	 * 
	 * 此方法对 i_Matcher 实现的所有接口的Class类型进行匹配操作。
	 * 也会对 i_Matcher 的父类及父的所有接口的Class类型递归的进行匹配操作。
	 * 只要有一个匹配成功的，立刻返回true。
	 * 
	 * @param i_ParamClass  入参Class类型
	 * @param i_Matcher     XML配置的Class类型
	 * @return
	 */
	private boolean constructor_ParamMatch(Class<?> i_ParamClass ,Class<?> i_Matcher)
	{
		return this.callMethod_ParamMatch(i_ParamClass ,i_Matcher);
	}
	
	
	
	/**
	 * 调用Call节点的方法
	 * 
	 * @param i_Obj
	 * @param i_CallNode
	 * @throws Exception
	 */
    private void callMethod(Object i_Obj ,Node i_CallNode ,TreeNode<XJavaObject> i_TreeNode) throws Exception
	{
		if ( i_Obj == null )
		{
			return;
		}
		
		String v_CallMethodName = this.getNodeAttribute(i_CallNode ,$XML_OBJECT_CALL_NAME);
		
		if ( v_CallMethodName == null )
		{
			return;
		}
		
		
		List<Class<?>> v_ParamClassList       = new ArrayList<Class<?>>();
		List<Object>   v_ParamValueList       = new ArrayList<Object>();
		List<Boolean>  v_ParamClassChangeList = new ArrayList<Boolean>();       // 方法入参的Class类型是否可变，即可以取Class类型的父Class类型
		int            v_ParamCount           = 0;
		NodeList       v_NodeList             = i_CallNode.getChildNodes();
		for (int v_Index=0; v_Index<v_NodeList.getLength(); v_Index++)
		{
			Node v_ParamNode = v_NodeList.item(v_Index);
			
			if ( "#".equals(v_ParamNode.getNodeName().substring(0 ,1)) )
			{
				// Nothing.   过滤空标记
			}
			else
			{
				v_ParamCount++;
				String   v_ParamClassName = this.getNodeAttribute(v_ParamNode ,$XML_OBJECT_CLASS);
				Class<?> v_ParamClass     = null;
				Object   v_ParamValue     = null;
				String   v_RefID          = this.getNodeAttribute(v_ParamNode ,$XML_OBJECT_REF);
				
				
				// 标记有Class节点属性的情况
				if ( v_ParamClassName != null )
				{
					try
					{
						v_ParamClass = Help.forName(v_ParamClassName);
						v_ParamClassChangeList.add(Boolean.FALSE);
					}
					catch (Exception exce)
					{
						throw exce;
					}
				}
				// 节点名称为import指定的Class类型
				else if ( this.imports.containsKey(v_ParamNode.getNodeName()) )
				{
					try
					{
						v_ParamClass = Help.forName(this.imports.get(v_ParamNode.getNodeName()));
						v_ParamClassChangeList.add(Boolean.FALSE);
					}
					catch (Exception exce)
					{
						throw exce;
					}
				}
				// 当节点属性有引用关键字时，获取调用方法的参数值
				else if ( v_RefID != null )
				{
					v_ParamValue = this.getRefObject(i_Obj ,v_ParamNode ,v_RefID);
					
					if ( v_ParamValue != null )
					{
						v_ParamClass = v_ParamValue.getClass();
						v_ParamClassChangeList.add(Boolean.TRUE);
					}
					else
					{
						throw new NullPointerException("Ref method [" + v_RefID + "] is't exist of Call Node[" + i_CallNode.getParentNode().getNodeName() + "." + i_CallNode.getNodeName() + "].");
					}
				}
				else if ( $XML_JAVA_DATATYPE_CHAR.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = char.class;
					v_ParamValue = getNodeTextContent(v_ParamNode).charAt(0);
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_INT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = int.class;
					v_ParamValue = Integer.valueOf(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_LONG.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = long.class;
                    v_ParamValue = Long.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
                else if ( $XML_JAVA_DATATYPE_BIGDECIMAL.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = BigDecimal.class;
                    v_ParamValue = new BigDecimal(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_DOUBLE.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = double.class;
					v_ParamValue = Double.valueOf(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_FLOAT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = float.class;
                    v_ParamValue = Float.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_BOOLEAN.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = boolean.class;
					v_ParamValue = Boolean.valueOf(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_STRING.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = String.class;
					v_ParamValue = getNodeTextContent(v_ParamNode);
					if ( v_ParamValue != null )
					{
						v_ParamValue = StringHelp.replaceAll(v_ParamValue.toString() ,$XML_Replace_Keys ,false).replaceAll($XML_CLASSPATH ,this.xmlClassPath);
					}
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_DATE.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = Date.class;
					v_ParamValue = new Date().setDate(getNodeTextContent(v_ParamNode));
					v_ParamClassChangeList.add(Boolean.TRUE);
				}
				else if ( $XML_JAVA_DATATYPE_OBJECT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
				{
					v_ParamClass = Object.class;
					v_ParamValue = getNodeTextContent(v_ParamNode);
					v_ParamClassChangeList.add(Boolean.FALSE);
				}
				else if ( $XML_JAVA_DATATYPE_CLASS.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = Class.class;
                    v_ParamValue = Help.forName(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_BYTE.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = byte.class;
                    v_ParamValue = Byte.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else if ( $XML_JAVA_DATATYPE_SHORT.equalsIgnoreCase(v_ParamNode.getNodeName()) )
                {
                    v_ParamClass = short.class;
                    v_ParamValue = Short.valueOf(getNodeTextContent(v_ParamNode));
                    v_ParamClassChangeList.add(Boolean.FALSE);
                }
				else
				{
					// 没有找到调用方法参数的Clss类型
					throw new NullPointerException("Method param[" + v_ParamCount + "] Class Type is not find of Call Node[" + i_CallNode.getParentNode().getNodeName() + "." + i_CallNode.getNodeName() + "].");
				}
				
				
				v_ParamClassList.add(v_ParamClass);
				
				
				if ( v_ParamValue == null )
				{
					// 当class属性与ref属性同时存在时
					if ( v_RefID != null )
					{
						v_ParamValue = this.getRefObject(i_Obj ,v_ParamNode ,v_RefID);
						
						if ( v_ParamValue != null )
						{
							if ( v_ParamClass == null )
							{
								v_ParamClass = v_ParamValue.getClass();
							}
							
							v_ParamValueList.add(v_ParamValue);
						}
						else
						{
							// 引用对象不存在
							throw new NullPointerException("Ref method [" + v_RefID + "] is't exist of Call Node[" + i_CallNode.getParentNode().getNodeName() + "." + i_CallNode.getNodeName() + "].");
						}
					}
					else
					{
						v_ParamValue = this.setInstance(v_ParamClass ,null ,v_ParamNode ,i_TreeNode);
						
						if ( v_ParamValue != null )
						{
							v_ParamValueList.add(v_ParamValue);
						}
						else
						{
							// 方法参数的值没有解释成功
							throw new NullPointerException("Method param[" + v_ParamCount + "] is null of Call Node[" + i_CallNode.getParentNode().getNodeName() + "." + i_CallNode.getNodeName() + "].");
						}
					}
				}
				else
				{
					v_ParamValueList.add(v_ParamValue);
				}
			
			}
			
		}
		
		
		String    v_TrueCallMethodName = v_CallMethodName.trim();
		Method    v_TrueCallMethod     = null;
		Class<?>  v_TrueClass          = null;
		Object    v_TrueObject         = null;
		String [] v_CallMethodRefIDArr = v_TrueCallMethodName.replace("." ,"@").split("@");
		String    v_ReturnID           = this.getNodeAttribute(i_CallNode ,$XML_OBJECT_CALL_RETURNID);
		Object    v_ReturnValue        = null;
		
		
		if ( v_ReturnID == null )
		{
			// Nothing.
		}
		else if ( v_ReturnID != null && "".equals(v_ReturnID.trim()) )
		{
			v_ReturnID = null;
		}
		else
		{
			v_ReturnID = v_ReturnID.trim();
		}
		
		
		// 判断Call方法的方法名称是有引用的情况。如果有引用的情况，则获取真实调用对象、Class类型及方法
		if ( v_CallMethodRefIDArr.length > 1 )
		{
			String v_RefID = v_TrueCallMethodName.substring(0 ,v_TrueCallMethodName.length() - v_CallMethodRefIDArr[v_CallMethodRefIDArr.length - 1].length() - 1);
			
			v_TrueObject         = this.getRefObject(i_Obj ,i_CallNode ,v_RefID);
			v_TrueClass          = v_TrueObject.getClass();
			v_TrueCallMethodName = v_CallMethodRefIDArr[v_CallMethodRefIDArr.length - 1];
		}
		else
		{
			v_TrueObject = i_Obj;
			v_TrueClass  = v_TrueObject.getClass();
		}
		
		
		if ( v_ParamClassList.size() == 0 )
		{
			v_TrueCallMethod = v_TrueClass.getMethod(v_TrueCallMethodName);
			
			if ( v_ReturnID != null )
			{
				Class<?> v_ReturnClass = v_TrueCallMethod.getReturnType();
				
				if ( v_ReturnClass != java.lang.Void.TYPE )
				{
					v_ReturnValue = v_TrueCallMethod.invoke(v_TrueObject);
				}
				// 无返回的方法
				else
				{
					v_TrueCallMethod.invoke(v_TrueObject);
				}
			}
			else
			{
				v_TrueCallMethod.invoke(v_TrueObject);
			}
		}
		else
		{	
			v_TrueCallMethod = this.callMethod_GetMethod(v_TrueClass 
					                                    ,v_TrueCallMethodName 
					                                    ,v_ParamClassList
					                                    ,v_ParamClassChangeList);
			
			if ( v_TrueCallMethod != null )
			{
				if ( v_ReturnID != null )
				{
					Class<?> v_ReturnClass = v_TrueCallMethod.getReturnType();
					
					if ( v_ReturnClass != java.lang.Void.TYPE  )
					{
						v_ReturnValue = v_TrueCallMethod.invoke(v_TrueObject ,v_ParamValueList.toArray());
					}
					// 无返回的方法
					else
					{
						v_TrueCallMethod.invoke(v_TrueObject ,v_ParamValueList.toArray());
					}
				}
				else
				{
					v_TrueCallMethod.invoke(v_TrueObject ,v_ParamValueList.toArray());
				}
			}
			else
			{
				// 没有匹配到对应的方法
				throw new java.lang.NoSuchMethodException("No such method[" + v_CallMethodName + "] of Call Node[" + i_CallNode.getParentNode().getNodeName() + "." + i_CallNode.getNodeName() + "].");
			}
		}
		
		
		// Call节点调用的方法后的返回结果的ID标记，此结果也将存在 $XML_OBJECTS 中
		if ( v_ReturnID != null )
		{
			TreeNode<XJavaObject> v_TreeNode = new TreeNode<XJavaObject>(i_TreeNode.getOrderByID() ,v_ReturnID ,i_TreeNode.getSuper() ,new XJavaObject(v_ReturnID ,v_ReturnValue));
			
			if ( $XML_OBJECTS.containsNodeID(v_ReturnID) )
			{
				throw new Exception("Call returnID[" + v_ReturnID + "] is exist of Node[" + i_CallNode.getParentNode().getNodeName() + "." + i_CallNode.getNodeName() + "].");
			}
			
			$XML_OBJECTS.put(v_TreeNode);
		}
	}
	
	
	
	/**
	 * 找到对应参数数量、参数类型对应的方法对象
	 * 
	 * @param i_Class                 类Class类型
	 * @param i_MethodName            方法名称
	 * @param i_ParamClassList        方法入参的Class列表
	 * @param i_ParamClassChangeList  方法入参的Class类型是否可变，即可以取Class类型的父Class类型
	 * @return
	 */
	private Method callMethod_GetMethod(Class<?> i_Class ,String i_MethodName ,List<Class<?>> i_ParamClassList ,List<Boolean> i_ParamClassChangeList)
	{
		int          v_ParamSize  = i_ParamClassList.size();
		List<Method> v_MethodList = MethodReflect.getMethods(i_Class ,i_MethodName ,v_ParamSize);
		
		
		if ( v_MethodList.size() == 0 )
		{
			return null;
		}
		else if ( v_MethodList.size() == 1 )
		{
			return v_MethodList.get(0);
		}
		
		
		for (int i=0; i<v_MethodList.size(); i++)
		{
			Method      v_Method     = v_MethodList.get(i);
			Class<?> [] v_ParamArr   = v_Method.getParameterTypes();
			int         v_ParamIndex = 0;
			
			if ( v_ParamArr.length == v_ParamSize )
			{
				for (; v_ParamIndex<v_ParamSize; v_ParamIndex++)
				{
					Class<?> v_ParamClass = v_ParamArr[v_ParamIndex];
					
					if ( v_ParamClass.equals(i_ParamClassList.get(v_ParamIndex)) )
					{
						// Nothing.  匹配成功
					}
					else if ( i_ParamClassChangeList.get(v_ParamIndex) )
					{
						boolean v_MatchResult = this.callMethod_ParamMatch(v_ParamClass ,i_ParamClassList.get(v_ParamIndex).getSuperclass());
						
						if ( !v_MatchResult )
						{
							v_ParamIndex = v_ParamSize + 99;
						}
					}
					else
					{
						v_ParamIndex = v_ParamSize + 99;
					}
				}
				
				if ( v_ParamIndex == v_ParamSize )
				{
					return v_Method;
				}
			}
		}
		
		
		return null;
	}
	
	
	
	/**
	 * 匹配入参的Class类型与XML配置的Class类型是否相同。
	 * 
	 * 此方法对 i_Matcher 实现的所有接口的Class类型进行匹配操作。
	 * 也会对 i_Matcher 的父类及父的所有接口的Class类型递归的进行匹配操作。
	 * 只要有一个匹配成功的，立刻返回true。
	 * 
	 * @param i_ParamClass  入参Class类型
	 * @param i_Matcher     XML配置的Class类型
	 * @return
	 */
	private boolean callMethod_ParamMatch(Class<?> i_ParamClass ,Class<?> i_Matcher)
	{
		if ( i_Matcher == null )
		{
			return false;
		}
		else if ( i_ParamClass.equals(i_Matcher) )
		{
			return true;
		}
		else
		{
			Class<?> [] v_MatcherInterfaceClassArr = i_Matcher.getInterfaces();
			
			for (int i=0; i<v_MatcherInterfaceClassArr.length; i++)
			{
				if ( i_ParamClass.equals(v_MatcherInterfaceClassArr[i]) )
				{
					return true;
				}
			}
			
			return this.callMethod_ParamMatch(i_ParamClass ,i_Matcher.getSuperclass());
		}
	}
	
	
	
	/**
	 * 获取对象引用实例的方法全路径返回的实例对象
	 * 例如　v_RefID = "xxx.toString.toUpperCase" 时，
	 *      v_RefObjectID 即为 "xxx"，
	 *      v_MethodURL   即为 "toString.toUpperCase"。
     *      
     * 1. 支持 this.静态常量
     * 2. 支持 this.方法全名称
     * 3. 支持 RefID.静态常量
     * 4. 支持 RefID.方法全名称
     * 5. 支持 XJava节点标记<import>引用.静态常量
     * 6. 支持 XJava节点标记<import>引用.方法全名称
     * 7. 支持 java.sql.Types.VARCHAR 这样的静态常量全路径解释
	 * 
	 * @param i_RefID
	 * @return
	 * @throws Exception
	 */
	private Object getRefObject(Object i_This ,Node i_Node ,String i_RefID) throws Exception
	{
		String [] v_RefIDArr    = i_RefID.trim().replace("." ,"@").split("@");
		String    v_RefObjectID = v_RefIDArr[0];
		Object    v_RetObj      = null;
		
		
		if ( $XML_OBJECT_THIS.equalsIgnoreCase(v_RefObjectID) )
		{
			if ( i_This == null )
			{
				throw new NullPointerException("Ref url[" + i_RefID + "] not use 'this' keyword of Node[" + i_Node.getParentNode().getNodeName() + "." + i_Node.getNodeName() + "].");
			}
			else if ( v_RefIDArr.length >= 2 )
			{
				// 尝试获取类的静态属性
				try
				{
					v_RetObj = i_This.getClass().getDeclaredField(v_RefIDArr[1]).get(i_This);
				}
				catch (Exception exce)
				{
					v_RetObj = null;
				}
				
				if ( v_RetObj != null )
				{
					if ( v_RefIDArr.length == 2 )
					{
						// 只获取类的静态属性 
						return v_RetObj;
					}
					else
					{
						// 获取类的静态属性的对象的子方法
						String v_MethodURL = i_RefID.substring(v_RefIDArr[0].length() + 1 + v_RefIDArr[1].length() + 1);
						return getRefObject(v_RetObj ,i_Node ,"this." + v_MethodURL);
					}
				}
				else
				{
					// 尝试获取类的静态属性失败时，其后代码尝试获取getter方法
					v_RetObj = i_This;
				}
			}
			else
			{
				// 获取 this 本身时
				v_RetObj = i_This;
			}
		}
		else
		{
			v_RetObj = getObject(v_RefObjectID);
		}
		
		
		if ( v_RetObj != null )
		{
			if ( v_RefIDArr.length >= 2 )
			{
				String        v_MethodURL = i_RefID.substring(v_RefObjectID.length() + 1);
				MethodReflect v_Getter    = null;
				
				try
				{
					v_Getter = new MethodReflect(v_RetObj ,v_MethodURL ,MethodReflect.$NormType_Getter);
				}
				catch (Exception exce)
				{
                    v_Getter = null;
				}
				
                if ( v_Getter != null )
                {
    				try
    				{
    					v_RetObj = v_Getter.invoke();
    				}
    				catch (Exception exce)
    				{
    					throw new NoSuchMethodException("Ref url[" + i_RefID + "] is exception of Node[" + i_Node.getParentNode().getNodeName() + "." + i_Node.getNodeName() + "].\n" + exce.getMessage());
    				}
                }
                else
                {
                    if ( v_RefIDArr.length > 2 )
                    {
                        return this.getRefObject(v_RetObj ,i_Node ,"this." + v_MethodURL);
                    }
                    else
                    {
                        throw new NoSuchMethodException("Ref url[" + i_RefID + "] is exception of Node[" + i_Node.getParentNode().getNodeName() + "." + i_Node.getNodeName() + "].");
                    }
                }
			}
		}
		else
		{
            // 获取XJava节点标记<import>引用的相关静态常量及方法
            if ( v_RefIDArr.length >= 2 && this.imports.containsKey(v_RefObjectID) )
            {
                try
                {
                    // 尝试获取XJava节点标记引用的静态常量
                    String v_ClassURL = this.imports.get(v_RefObjectID) + "." + v_RefIDArr[1];
                    v_RetObj = StaticReflect.getStaticValue(v_ClassURL);
                }
                catch (Exception exce)
                {
                    v_RetObj = null;
                }
                
                if ( v_RetObj == null )
                {
                    // 尝试获取XJava节点标记引用的静态方法
                    try
                    {
                        Class<?>     v_Class         = Help.forName(this.imports.get(v_RefObjectID));
                        List<Method> v_StaticMethods = MethodReflect.getMethodsIgnoreCase(v_Class ,v_RefIDArr[1] ,0);
                        
                        if ( v_StaticMethods.size() == 1 )
                        {
                            v_RetObj = v_StaticMethods.get(0).invoke(v_Class);
                        }
                    }
                    catch (Exception exce)
                    {
                        throw new NoSuchMethodException("Ref url[" + i_RefID + "] is't exist of Node[" + i_Node.getParentNode().getNodeName() + "." + i_Node.getNodeName() + "].\n" + exce.getMessage());
                    }
                }
                
                if ( v_RetObj != null )
                {
                    if ( v_RefIDArr.length > 2 )
                    {
                        String v_MethodURL = i_RefID.substring(v_RefIDArr[0].length() + 1 + v_RefIDArr[1].length() + 1);
                        return this.getRefObject(v_RetObj ,i_Node ,"this." + v_MethodURL);
                    }
                    else
                    {
                        return v_RetObj;
                    }
                }
            }
            // 尝试获取静态字段全路径反射
            else
            {
                try
                {
                    v_RetObj = StaticReflect.getStaticValue(i_RefID);
                    
                    return v_RetObj;
                }
                catch (Exception exce)
                {
                    v_RetObj = null;
                }
            }
            
			throw new NoSuchMethodException("Ref url[" + i_RefID + "] is't exist of Node[" + i_Node.getParentNode().getNodeName() + "." + i_Node.getNodeName() + "].");
		}

		return v_RetObj;
	}
	
	
	
	/**
	 * 获取节点的属性值
	 * 
	 * @param i_Node
	 * @param i_AttributeName
	 * @return
	 */
	private String getNodeAttribute(Node i_Node ,String i_AttributeName)
	{
		NamedNodeMap v_NameNodeMap = i_Node.getAttributes();
		
		for (int v_Index=0; v_Index<v_NameNodeMap.getLength(); v_Index++)
		{
			Node v_NodeAttr = v_NameNodeMap.item(v_Index);
			
			if ( i_AttributeName.equals(v_NodeAttr.getNodeName().toLowerCase()) )
			{
				return v_NodeAttr.getNodeValue();
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * 获取节点有效的子节点数量
	 * 
	 * @param i_Node
	 * @return
	 */
	private int getChildNodesSize(Node i_Node)
	{
		int      v_Size     = 0;
		NodeList v_NodeList = i_Node.getChildNodes();
		
		for (int i=0; i<v_NodeList.getLength(); i++)
		{
			Node v_ChildNode = v_NodeList.item(i);
			if ( "#".equals(v_ChildNode.getNodeName().substring(0 ,1)) )
			{
				// Nothing.   过滤空标记
			}
			else
			{
				v_Size++;
			}
		}
		
		return v_Size;
	}
	
	
	
	/**
	 * 1. 深克隆：当构造一个新的实例时，实现了 java.lang.Cloneable接口，则调用克隆clone()方法克隆对象
	 * 2. 深克隆：当构造一个新的实例时，继承了 org.hy.common.xml.SerializableDef 类时，则调用克隆clone(Object)方法克隆对象
     * 3. 浅克隆：当构造一个新的实例时，如果没有clone()方法，则通过无参数的构造器new一个实例，再依次newObject.setter(oldObject.getter())
	 * 
	 * @author      ZhengWei(HY)
     * @createDate  2013-08-10
	 * @version     v1.0
	 *              v2.0  2017-01-16  添加：深克隆：当构造一个新的实例时，继承了 org.hy.common.xml.SerializableDef 类时，则调用克隆clone(Object)方法克隆对象
	 *
	 * @param i_Instance
	 * @return
	 * @throws NoSuchMethodException
	 */
	public static Object clone(Object i_Instance) throws NoSuchMethodException
	{
	    // 1. 深克隆：实现了 java.lang.Cloneable接口，则调用克隆clone()方法克隆对象
        Class<?> [] v_Inferfaces = i_Instance.getClass().getInterfaces();
        String      v_ErrorInfo  = null;
        
        for (int i=0; i<v_Inferfaces.length; i++)
        {
            if ( v_Inferfaces[i].equals(java.lang.Cloneable.class) )
            {
                try
                {
                    Method v_CloneMethod = i_Instance.getClass().getMethod("clone");
                    
                    return v_CloneMethod.invoke(i_Instance);
                }
                catch (Exception exce)
                {
                    throw new java.lang.NoSuchMethodException("XJava object clone exception.\n" + exce.getMessage());
                }
            }
        }
        
        
        // 2. 深克隆：继承了 org.hy.common.xml.SerializableDef 类时，则调用克隆clone(Object)方法克隆对象
        if ( MethodReflect.isExtendImplement(i_Instance ,SerializableDef.class) )
        {
            try
            {
                Object v_NewObj = i_Instance.getClass().newInstance();
                ((SerializableDef)i_Instance).clone(v_NewObj);
                
                return v_NewObj;
            }
            catch (Exception exce)
            {
                throw new NoSuchMethodException("XJava object newInstance exception.\n" + exce.getMessage());
            }
        }
        
        
        // 3. 浅克隆：如果没有clone()方法，则通过无参数的构造器new一个实例，再依次newObject.setter(oldObject.getter())
        try
        {
            Object    v_NewObj  = i_Instance.getClass().newInstance();
            Method [] v_Methods = i_Instance.getClass().getMethods();
            
            v_ErrorInfo = "Methods.length=" + v_Methods.length;
            
            for (int i=0; i<v_Methods.length; i++)
            {
                v_ErrorInfo = v_Methods[i].getName();
                
                // 一个入参数的 setter() 方法
                if ( v_Methods[i].getName().startsWith("set") && v_Methods[i].getParameterTypes().length == 1 )
                {
                    Method v_GetMethod = MethodReflect.getGetMethod(i_Instance.getClass() ,"g" + v_Methods[i].getName().substring(1) ,false);
                    
                    if ( v_GetMethod != null )
                    {
                        v_ErrorInfo = v_GetMethod.getName();
                        
                         // getter()方法的返回值类型与setter()方法的入参类型相同时
                        if ( v_Methods[i].getParameterTypes()[0].equals(v_GetMethod.getReturnType()) )
                        {
                            Object v_Value = v_GetMethod.invoke(i_Instance);
                            if ( v_Value == null  )
                            {
                                v_ErrorInfo += "(null)";
                            }
                            else
                            {
                                v_ErrorInfo += "(" + v_Value.toString() + ")";
                            }
                            v_Methods[i].invoke(v_NewObj ,v_Value);
                        }
                    }
                }
            }
            
            return v_NewObj;
        }
        catch (InstantiationException exce)
        {
            throw new NoSuchMethodException("XJava object newInstance exception." + v_ErrorInfo + "\n" + exce.getMessage());
        }
        catch (IllegalAccessException exce)
        {
            throw new NoSuchMethodException("XJava object newInstance exception." + v_ErrorInfo + "\n" + exce.getMessage());
        }
        catch (Exception exce)
        {
            throw new NoSuchMethodException("XJava object setter(getter()) exception." + v_ErrorInfo + "\n" + exce.getMessage());
        }
	}
    
    
    
    
    
    /**
     * XJava.$XML_OBJECTS集合中的元素对象
     * 
     * 有了此类后，就可以更加丰富XJava构造对象的多样化。
     * 
     * 1. 可从原先所有构造出的对象都是单例，变成：可单例、可多实例
     * 2. 深克隆：当构造一个新的实例时，如果有clone()方法，则调用克隆对象
     * 3. 浅克隆：当构造一个新的实例时，如果没有clone()方法，则通过无参数的构造器new一个实例，再依次newObject.setter(oldObject.getter())
     * 
     * @author      ZhengWei(HY)
     * @version     v1.0  
     * @createDate  2013-08-10
     */
    class XJavaObject
    {
        /** XJava构造出的对象实例 */
        private Object object;
        
        /** 
         * 是否每次通过 XJava.getObject(id) 获取一个全新的对象实例
         * 
         * XJava默认构造出的对象为"单例"
         */
        private boolean isNew;
        
        
        
        public XJavaObject()
        {
            this(null ,null ,false);
        }
        
        
        
        public XJavaObject(String i_XJavaID ,Object i_Object)
        {
            this(i_XJavaID ,i_Object ,false);
        }
        
        
        
        public XJavaObject(String i_XJavaID ,Object i_Object ,boolean i_IsNew)
        {
            this.object = i_Object;
            this.isNew  = i_IsNew;
            
            // 将配置在XML配置文件中的ID值，自动赋值给Java实例对象。  ZhengWei(HY) Add 2018-03-09
            if ( this.object != null )
            {
                if ( this.object instanceof XJavaID )
                {
                    ((XJavaID)this.object).setXJavaID(i_XJavaID);
                }
            }
        }

        
        public boolean isNew()
        {
            return isNew;
        }

        
        public void setNew(boolean isNew)
        {
            this.isNew = isNew;
        }
        
        
        public Object getObject() throws NoSuchMethodException
        {
            return this.getObject(this.isNew);
        }
        
        
        public Object getObject(boolean i_IsNew) throws NoSuchMethodException
        {
            if ( i_IsNew )
            {
                return XJava.clone(this.object);
            }
            else
            {
                return this.object;
            }
        }

        
        public void setObject(Object object)
        {
            this.object = object;
        }
        
    }
	
}
