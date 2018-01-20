package org.hy.common.xml.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;
import org.glassfish.jersey.server.ContainerRequest;
import org.hy.common.xml.XJSON;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.AppInterfaces;
import org.hy.common.xml.plugins.AppMessage;

import org.hy.common.Date;
import org.hy.common.Help;





/**
 * XJava与Jersey、Restful两种技术相融合
 * 
 * @author      ZhengWei(HY)
 * @createDate  2014-09-25
 * @version     v1.0  
 *              v2.0  2018-01-20  添加：当访问路径不存时，服务端也打出日志。
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON ,"*/*"})
@Singleton
public class AppMessageBodyProvider extends AbstractMessageReaderWriterProvider<AppMessage<?>>
{
    private javax.inject.Provider<ContainerRequest> request;
    
    
    
    public AppMessageBodyProvider(@Context javax.inject.Provider<ContainerRequest> i_Request) 
    {  
        this.request = i_Request;
    } 

    
    
    public boolean isReadable(Class<?> type ,Type genericType ,Annotation [] annotations ,MediaType mediaType)
    {
        return type == AppMessage.class;
    }
    
    
    
    @SuppressWarnings("unchecked")
    public AppMessage<?> readFrom(Class<AppMessage<?>> type ,Type genericType ,Annotation [] annotations ,MediaType mediaType ,MultivaluedMap<String ,String> httpHeaders ,InputStream entityStream) throws IOException ,WebApplicationException
    {
        AppInterface  v_AppInterface = null;
        AppMessage<?> v_Ret          = null;
        
        try 
        {
            String v_Path = this.request.get().getPath(true);
            
            if ( !Help.isNull(v_Path) )
            {
                Map<String ,AppInterface> v_AppInterfaces = (Map<String ,AppInterface>)XJava.getObject("AppInterfaces");
                
                if ( !Help.isNull(v_AppInterfaces) )
                {
                    v_AppInterface = v_AppInterfaces.get(v_Path);
                    if ( v_AppInterface != null )
                    {
                        String v_MsgInfo = readFromAsString(entityStream ,mediaType);
                        v_Ret = AppInterfaces.getAppMessage(v_Path ,v_MsgInfo);
                        
                        // 请求次数++
                        if ( v_Ret == null )
                        {
                            System.err.println("\nError: " + Date.getNowTime().getFullMilli() + "：Request [" + v_Path + "] is fail.\n" + v_MsgInfo);
                            v_AppInterface.request("");
                        }
                        else
                        {
                            v_AppInterface.request(v_Ret.getSysid());
                        }
                    }
                    else
                    {
                        System.err.println("\nError: " + Date.getNowTime().getFullMilli() + "  Request path[" + v_Path + "] does not exist.");
                    }
                }
            }
            
            return v_Ret;
        } 
        catch (Exception exce) 
        {
            try
            {
                if ( v_AppInterface != null )
                {
                    // 请求次数++
                    v_AppInterface.request("");
                }
            }
            catch (Exception e)
            {
                return null;
            }
            
            // 不报错，直接返回空
            return null;
        }
    }

    
    
    public boolean isWriteable(Class<?> type ,Type genericType ,Annotation [] annotations ,MediaType mediaType)
    {
        return true;
    }

    
    
    @SuppressWarnings("unchecked")
    public void writeTo(AppMessage<?> t ,Class<?> type ,Type genericType ,Annotation [] annotations ,MediaType mediaType ,MultivaluedMap<String ,Object> httpHeaders ,OutputStream entityStream) throws IOException ,WebApplicationException
    {
        try
        {
            if ( t == null )
            {
                writeToAsString("" ,entityStream ,MediaType.TEXT_PLAIN_TYPE);
            }
            else
            {
                Map<String ,AppInterface>  v_AppInterfaces = (Map<String ,AppInterface>)XJava.getObject("AppInterfaces");
                
                if ( !Help.isNull(v_AppInterfaces) )
                {
                    if ( !Help.isNull(t.getSid()) )
                    {
                        AppInterface v_AppInterface = v_AppInterfaces.get(t.getSid());
                        if ( v_AppInterface != null )
                        {
                            if ( t.getResult() != null && t.getResult().booleanValue() )
                            {
                                // 成功返回次数++  用时时长++
                                v_AppInterface.success(t.getSysid() ,Date.getNowTime().getTime() - t.gatCreateTime().getTime());
                            }
                        }
                    }
                }
                
                XJSON v_WriteXJson = new XJSON();
                v_WriteXJson.setReturnNVL(false);
                v_WriteXJson.setAccuracy(true);
                
                if ( Help.isNull(t.getFormat()) || "json".equalsIgnoreCase(t.getFormat()) )
                {
                    writeToAsString(t.toString(v_WriteXJson) ,entityStream ,mediaType);
                }
                else
                {
                    // 其它格式先用XJson替代一下，待后期开发
                    writeToAsString(t.toString(v_WriteXJson) ,entityStream ,mediaType);
                }
            }
        }
        catch (Exception exce)
        {
            // 不报错，直接返回
        }
    }

}
