package org.hy.common.xml.plugins;

import java.util.Map;

import org.hy.common.xml.XJava;

import com.opensymphony.xwork2.ObjectFactory;





/**
 * 嵌入Struts中使用。
 * 
 * 可替换原Struts构建对象的方法。使XJava也能Spring的功能。
 * 
 * 小小嵌入，带来巨大生机。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2013-08-07
 * @version     v1.0  
 *              v1.1  2018-11-08  修改：从 XJavaObjectFactory 改名为 XJavaStrutsObjectFactory。
 */
public class XJavaStrutsObjectFactory extends ObjectFactory 
{

	private static final long serialVersionUID = -5876147447358220646L;
	
	
	
	public Class<?> getClassInstance(String i_ClassName) throws ClassNotFoundException 
	{
		Object v_ClassInstance = XJava.getObject(i_ClassName);
		
		if ( v_ClassInstance != null )
		{
			return v_ClassInstance.getClass();
		}
		else
		{
			return super.getClassInstance(i_ClassName);
		}
	}

	

	@Override
	public Object buildBean(String i_ClassName, Map<String, Object> i_ExtraContext, boolean i_InjectInternal) throws Exception 
	{
		Object v_ClassInstance = XJava.getObject(i_ClassName);
		
		if ( v_ClassInstance != null )
		{
			return v_ClassInstance;
		}
		else
		{
			return super.buildBean(i_ClassName, i_ExtraContext, i_InjectInternal);
		}
	}

}
