package org.hy.common.xml.plugins;

import java.util.Hashtable;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.PartitionMap;
import org.hy.common.TablePartition;
import org.hy.common.app.Param;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.AppInterface;





/**
 * 接口信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2014-11-19
 * @version     v1.0
 */
public class AppInterfaceInfo extends Param
{
    
    private static final long serialVersionUID = 2497294980260367411L;


    private static final String [] $RegisterXIDs = {"AppInterfaces" ,"AppInterfacesRetrun" ,"AppInterface_MappingError"};
    
    
    /** 接口内部编号 */
    private String innerNo;
    
    /** http访问方式 */
    private String httpMethod;
    
    /** 接口版本号 */
    private String version;
    
    /** 请求参数对象的类名称 */
    private String requestClass;
    
    /** 响应参数对象的类名称 */
    private String responseClass;
    
    /** 接口相关的错误编码的前缀 */
    private String mappingError;
    
    
    
    public AppInterfaceInfo()
    {
        initRegister();
    }
    
    
    
    /**
     * 获取：接口内部编号
     */
    public String getInnerNo()
    {
        return innerNo;
    }

    
    /**
     * 设置：接口内部编号
     * 
     * @param innerNo 
     */
    public void setInnerNo(String innerNo)
    {
        this.innerNo = innerNo;
    }

    
    /**
     * 获取：http访问方式
     */
    public String getHttpMethod()
    {
        return httpMethod;
    }

    
    /**
     * 设置：http访问方式
     * 
     * @param httpMethod 
     */
    public void setHttpMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    
    /**
     * 获取：接口版本号
     */
    public String getVersion()
    {
        return version;
    }

    
    /**
     * 设置：接口版本号
     * 
     * @param version 
     */
    public void setVersion(String version)
    {
        this.version = version;
    }


    
    /**
     * 获取：请求对象的类名称
     */
    public String getRequestClass()
    {
        return requestClass;
    }


    
    /**
     * 设置：请求对象的类名称
     * 
     * @param requestClass 
     */
    public void setRequestClass(String requestClass)
    {
        this.requestClass = requestClass;
        
        if ( !Help.isNull(this.getName()) && !Help.isNull(this.requestClass) )
        {
            registerRequestClass(this.getName() ,this.requestClass);
        }
    }


    
    /**
     * 获取：响应对象的类名称
     */
    public String getResponseClass()
    {
        return responseClass;
    }


    
    /**
     * 设置：响应对象的类名称
     * 
     * @param responseClass 
     */
    public void setResponseClass(String responseClass)
    {
        this.responseClass = responseClass;
        
        if ( !Help.isNull(this.getName()) )
        {
            registerResponseClass(this.getName() ,this.responseClass);
        }
    }
    
    
    
    /**
     * 获取：接口相关的错误编码的前缀
     */
    public String getMappingError()
    {
        return mappingError;
    }


    
    /**
     * 设置：接口相关的错误编码的前缀
     * 
     * @param mappingError 
     */
    public void setMappingError(String mappingError)
    {
        this.mappingError = mappingError;
        
        if ( !Help.isNull(this.getInnerNo()) )
        {
            registerMappingError(this.getInnerNo() ,this.mappingError);
        }
    }
    
    
    
    /**
     * 初始化注册者
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-19
     * @version     v1.0
     *
     */
    @SuppressWarnings("unchecked")
    public synchronized static void initRegister()
    {
        Map<String ,AppInterface>   v_Requests      = (Map<String ,AppInterface>)  XJava.getObject($RegisterXIDs[0]);
        Map<String ,Param>          v_Responses     = (Map<String ,Param>)         XJava.getObject($RegisterXIDs[1]);
        PartitionMap<String ,Param> v_MappingErrors = (PartitionMap<String ,Param>)XJava.getObject($RegisterXIDs[2]);
        
        if ( null == v_Requests )
        {
            v_Requests = new Hashtable<String ,AppInterface>();
            XJava.putObject($RegisterXIDs[0] ,v_Requests);
        }
        
        if ( null == v_Responses )
        {
            v_Responses = new Hashtable<String ,Param>();
            XJava.putObject($RegisterXIDs[1] ,v_Responses);
        }
        
        if ( null == v_MappingErrors )
        {
            v_MappingErrors = new TablePartition<String ,Param>();
            XJava.putObject($RegisterXIDs[2] ,v_MappingErrors);
        }
    }
    
    
    
    /**
     * 注册请求参数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-18
     * @version     v1.0
     *
     * @param i_AIFName
     * @param i_RequestClass
     */
    @SuppressWarnings("unchecked")
    public synchronized static void registerRequestClass(String i_AIFName ,String i_RequestClass)
    {
        Map<String ,AppInterface> v_Requests = (Map<String ,AppInterface>)XJava.getObject($RegisterXIDs[0]);
        AppInterface              v_AIF      = new AppInterface();
        
        v_AIF.setName(     i_AIFName);
        v_AIF.setClassName(i_RequestClass);
        
        try
        {
            Help.forName(i_RequestClass);
        }
        catch (Exception exce)
        {
            System.out.println("\njava.lang.ClassNotFoundException(" + i_RequestClass + ") for [" + i_AIFName + "].\n");
        }
        
        v_Requests.put(i_AIFName ,v_AIF);
    }
    
    
    
    /**
     * 注册响应参数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-18
     * @version     v1.0
     *
     * @param i_AIFName
     * @param i_ResponseClass
     */
    @SuppressWarnings("unchecked")
    public synchronized static void registerResponseClass(String i_AIFName ,String i_ResponseClass)
    {
        Map<String ,Param> v_Responses = (Map<String ,Param>)XJava.getObject($RegisterXIDs[1]);
        Param              v_AIF       = new Param();
        
        v_AIF.setName( i_AIFName);
        v_AIF.setValue(Help.NVL(i_ResponseClass));
        
        try
        {
            Help.forName(i_ResponseClass);
        }
        catch (Exception exce)
        {
            System.out.println("\n\njava.lang.ClassNotFoundException(" + i_ResponseClass + ") for [" + i_AIFName + "].\n");
        }
        
        v_Responses.put(i_AIFName ,v_AIF);
    }
    
    
    
    /**
     * 注册接口编号与接口错误编码的映射关系
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-02-18
     * @version     v1.0
     *
     * @param i_AIFName
     * @param i_ErrorCodePrefix
     */
    @SuppressWarnings("unchecked")
    public synchronized static void registerMappingError(String i_AIFInnerNo ,String i_ErrorCodePrefix)
    {
        PartitionMap<String ,Param> v_MappingErrors = (PartitionMap<String ,Param>)XJava.getObject($RegisterXIDs[2]);
        Param                       v_AIF           = new Param();
        
        v_AIF.setName( i_ErrorCodePrefix);
        v_AIF.setValue(i_AIFInnerNo);
        
        v_MappingErrors.putRow(i_ErrorCodePrefix ,v_AIF);
    }
    
}
