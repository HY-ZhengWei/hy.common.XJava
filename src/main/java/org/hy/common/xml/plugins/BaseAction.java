package org.hy.common.xml.plugins;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ApplicationAware;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.hy.common.xml.XJava;

import org.hy.common.Help;
import org.hy.common.app.Param;
import com.opensymphony.xwork2.ActionSupport;





/**
 * 基础Action接口
 * 
 * SER : 表业务层的对象
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2013-08-08
 */
public class BaseAction<SER> extends ActionSupport implements RequestAware ,SessionAware ,ApplicationAware ,ServletRequestAware ,ServletResponseAware
{
	
	private static final long serialVersionUID = -4935119095600870095L;
	
	
	protected Map<String ,Object>  request;
    
    protected Map<String ,Object>  application;
    
    protected Map<String ,Object>  session;
    
    protected HttpServletRequest   servletRequest;
    
    protected HttpServletResponse  servletResponse;
    
    protected SER                  service;
	
	
	
	/**
	 * 获取应用全局配置的参数信息
	 * 
	 * @param i_ParamName   参数名称(区分大小写)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Param getCommonParam(String i_ParamName)
	{
		if ( Help.isNull(i_ParamName) )
		{
			return null;
		}
		
		Map<String ,Param> v_ParamMap = (Map<String ,Param>)XJava.getObject("XParam");
		
		if ( v_ParamMap.containsKey(i_ParamName) )
		{
			return v_ParamMap.get(i_ParamName);
		}
		else
		{
			return null;
		}
	}
	
	
	
    /**
     * 实现了 RequestAware 接口后，Struts 会自动为我们获取 Request 对象
     */
	public void setRequest(Map<String, Object> i_Request) 
	{
		this.request = i_Request;	
	}
	
	
	
	public Map<String, Object> getRequest()
	{
		return this.request;
	}
    
    
    
    /**
     * 参数对象压入 Request 堆栈
     * 
     * @param i_Name
     * @param i_ValueObj
     */
    public synchronized void putRequest(String i_Name ,Object i_ValueObj)
    {
        if ( Help.isNull(i_Name) )
        {
            throw new NullPointerException("Name is null.");
        }
        
        if ( this.request.containsKey(i_Name) )
        {
            this.request.remove(i_Name);
        }
        
        this.request.put(i_Name ,i_ValueObj);
    }
    
    
    
    /**
     * 获取 Request 堆栈中的参数对象
     * 
     * @param i_Name
     * @return
     */
    public Object getRequestValue(String i_Name)
    {
        if ( Help.isNull(i_Name) )
        {
            throw new NullPointerException("Name is null.");
        }
        
        if ( this.request.containsKey(i_Name) )
        {
            return this.request.get(i_Name);
        }
        else
        {
            return null;
        }
    }



    /**
     * 实现了 SessionAware 接口后，Struts 会自动为我们获取 Session 对象
     */
    public void setSession(Map<String, Object> i_Session) 
    {
        this.session = i_Session;
    }
    
    
    
    public Map<String ,Object> getSession()
    {
        return this.session;
    }
    
    
    
    /**
     * 参数对象压入 Session 堆栈
     * 
     * @param i_Name
     * @param i_ValueObj
     */
    public synchronized void putSession(String i_Name ,Object i_ValueObj)
    {
        if ( Help.isNull(i_Name) )
        {
            throw new NullPointerException("Name is null.");
        }
        
        if ( this.session.containsKey(i_Name) )
        {
            this.session.remove(i_Name);
        }
        
        this.session.put(i_Name ,i_ValueObj);
    }
    
    
    
    /**
     * 获取 Session 堆栈中的参数对象
     * 
     * @param i_Name
     * @return
     */
    public Object getSessionValue(String i_Name)
    {
        if ( Help.isNull(i_Name) )
        {
            throw new NullPointerException("Name is null.");
        }
        
        if ( this.session.containsKey(i_Name) )
        {
            return this.session.get(i_Name);
        }
        else
        {
            return null;
        }
    }

    

    /**
     * 实现了 ApplicationAware 接口后，Struts 会自动为我们获取 Application 对象
     */
    public void setApplication(Map<String, Object> i_Application) 
    {
        this.application = i_Application;
    }
    
    
    
    public Map<String ,Object> getApplication()
    {
        return this.application;
    }
    
    
    
    /**
     * 参数对象压入 Application 堆栈
     * 
     * @param i_Name
     * @param i_ValueObj
     */
    public synchronized void putApplication(String i_Name ,Object i_ValueObj)
    {
        if ( Help.isNull(i_Name) )
        {
            throw new NullPointerException("Name is null.");
        }
        
        if ( this.application.containsKey(i_Name) )
        {
            this.application.remove(i_Name);
        }
        
        this.application.put(i_Name ,i_ValueObj);
    }
    
    
    
    /**
     * 获取 Application 堆栈中的参数对象
     * 
     * @param i_Name
     * @return
     */
    public Object getApplicationValue(String i_Name)
    {
        if ( Help.isNull(i_Name) )
        {
            throw new NullPointerException("Name is null.");
        }
        
        if ( this.application.containsKey(i_Name) )
        {
            return this.application.get(i_Name);
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 实现了 ServletResponseAware 接口后，Struts 会自动为我们获取 Response 对象
     */
    public void setServletResponse(HttpServletResponse i_Response)
    {
        this.servletResponse = i_Response;
    }
    
    
    
    public HttpServletResponse getServletResponse()
    {
        return this.servletResponse;
    }
    
    
    
    public void setServletRequest(HttpServletRequest i_Request)
    {
        this.servletRequest = i_Request;
    }
    
    
    
    public HttpServletRequest getServletRequest()
    {
        return this.servletRequest;
    }
    
    
    
    public SER getService() 
    {
        return service;
    }

    
    
    public void setService(SER i_Service) 
    {
        this.service = i_Service;
    }
	
}
