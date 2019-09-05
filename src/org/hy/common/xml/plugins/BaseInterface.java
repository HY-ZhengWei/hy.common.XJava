package org.hy.common.xml.plugins;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;

import org.hy.common.StringHelp;
import org.hy.common.file.FileHelp;





/**
 * 基础接口
 * 
 * 1. 实现xd配置文件与Java类文件相同(名称及路径)后的自动加载xd配置文件的方法实现(简单又方便)
 * 2. 如果xd配置文件与Java类文件不同(名称或路径)，可以重写小部分方法后实现加载
 * 
 * 3. 实现XML配置文件与Java类文件相同(名称及路径)后的自动加载XML配置文件的方法实现(简单又方便)
 * 4. 如果XML配置文件与Java类文件不同(名称或路径)，可以重写小部分方法后实现加载
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2013-08-16
 */
public abstract class BaseInterface
{
    /**
     * XD文件中的资源是否被加载过
     * 
     * 全局防止被重复加载。之前没有这个控制时，其实例化的子类，只能使用单态模式
     */
    private static final Map<Class<?> ,Boolean> $IsInit = new Hashtable<Class<?> ,Boolean>();
    
    /**
     * 资源文件的类型
     * 0. XD文件     -- 默认值
     * 1. 普通XML文件
     */
    private   int     rtype;
    
	/**
	 * XD文件中的资源是否被加载过
	 */
	private   boolean isInit;
    
    protected String  classPath;
	
	
	
	public BaseInterface()
	{
        this.rtype  = 0;
		this.isInit = false;
	}
    
    
    
    public BaseInterface(int i_RType)
    {
        this.rtype  = i_RType;
        this.isInit = false;
    }
	
	
	
	/**
	 * 加载(初始化)XJava资源
	 */
	protected synchronized void init()
	{
		if ( this.isInit ) 
		{
			return;
		}
        
        if ( this.getXClass() != null && $IsInit.containsKey(this.getXClass()) )
        {
            return;
        }
		
		
		FileHelp v_FileHelp = new FileHelp();
		
		try 
		{
            this.isInit = true;
            $IsInit.put(this.getXClass() ,Boolean.TRUE);
            
            if ( this.rtype == 1 )
            {
                XJava.parserXml(this.getXDUrl() ,v_FileHelp.getContent(this.getXD() ,this.getCharEncoding()) ,this.getClassPath() ,this.getNamingSpace());
            }
            else
            {
                XJava.parserXml(v_FileHelp.getXD(this.getXD() ,this.getXDRName() ,this.getCharEncoding()) ,this.getClassPath() ,this.getNamingSpace());
            }
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	
	/**
	 * 获取规范化的XJava对象的元类型
	 * 
	 * 只要重写了这一个方法。
	 * 
	 * 其它方法就不用重写了，特别是：getXD()、getXDRName()、getNamingSpace()
	 * 
	 * @return
	 */
    protected Class<?> getXClass()
	{
		return null;
	}
	
	
	
	/**
	 * 获取XD文件
	 * 
	 * @return
	 */
    protected InputStream getXD()
	{
        String v_EName = ".x";
        if ( this.rtype == 1 )
        {
            return this.getXClass().getResourceAsStream(this.getXClass().getSimpleName() + v_EName + "ml");
        }
        else
        {
            return this.getXClass().getResourceAsStream(this.getXClass().getSimpleName() + v_EName + "d");
        }
	}
    
    
    
    /**
     * 获取XD文件的URL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-09-04
     * @version     v1.0
     *
     * @return
     */
    protected URL getXDUrl()
    {
        String v_EName = ".x";
        if ( this.rtype == 1 )
        {
            return this.getXClass().getResource(this.getXClass().getSimpleName() + v_EName + "ml");
        }
        else
        {
            return this.getXClass().getResource(this.getXClass().getSimpleName() + v_EName + "d");
        }
    }
	
	
	
	/**
	 * 获取XD文件中的资源名
	 * 
	 * @return
	 */
    protected String getXDRName()
	{
		return this.getXClass().getSimpleName() + ".xml";
	}
	
	
	
	/**
	 * 获取命名空间
	 * 
	 * @return
	 */
    protected String getNamingSpace()
	{
		return this.getXClass().getSimpleName();
	}
	
	
	
	/**
	 * 获取编码名称
	 * 
	 * @return
	 */
    protected String getCharEncoding()
	{
		return "UTF-8";
	}
	
	
	
	/**
	 * 获取ClassPath
	 * 
	 * @return
	 */
    protected synchronized String getClassPath()
	{
        if ( this.classPath == null )
        {
            String v_EName = ".x";
            URL    v_URL   = null;
            if ( this.rtype == 1 )
            {
                v_URL = this.getXClass().getResource(this.getXClass().getSimpleName() + v_EName + "ml");
            }
            else
            {
                v_URL = this.getXClass().getResource(this.getXClass().getSimpleName() + v_EName + "d");
            }
            
            return v_URL.toString().replaceFirst(StringHelp.getFileName(v_URL.getPath()) ,"");
        }
        else
        {
            return this.classPath;
        }
	}
	
	
	
	/**
	 * 获取XJava对象
	 * 
	 * @param i_XObjectName
	 * @return
	 */
    protected Object getObject(String i_XObjectName)
	{
		this.init();
		return XJava.getObject(i_XObjectName);
	}
	
    
    
    /**
     * 获取XSQL对象
     * 
     * @param i_XObjectName
     * @return
     */
    protected XSQL getXSQL(String i_XObjectName)
    {
        return (XSQL)this.getObject(i_XObjectName);
    }



    protected int getRtype()
    {
        return rtype;
    }


    
    protected void setRtype(int rtype)
    {
        this.rtype = rtype;
    }
    
}
