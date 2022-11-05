package org.hy.common.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.ListMap;
import org.hy.common.StringHelp;
import org.hy.common.file.FileHelp;
import org.hy.common.xml.log.Logger;





/**
 * WebService服务的客户端调用(访问)类。
 * 
 * 能实现如下方便操作
 *   1. 当作为Client客服端时，请求参数可按XML模板生成
 *   2. 当作为Client客服端时，响应报文可按XJava方式构造对象
 *   3. 当作为Server服务端时，接收外界请求报文可按XJava方式构造对象
 *   4. 当作为Server服务端时，返回给外界报文可按XML模板生成
 *   5. 支持代理访问
 *   
 * 注意：
 *   1. 当作为Client客服端时，相关方法以 request \ response 命名。
 *   2. 当作为Server服务端时，相关方法以 receive \ return   命名。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2013-07-05
 * @version     v1.0  
 *              v2.0  2020-06-02  修改：request()方法直接返回响应结果信息。
 *                                      最终用户可通过getReceiveObj()方法，主动的将结果信息转换为对象。
 *              v3.0  2020-06-24  添加：通过日志引擎规范输出日志
 */
public class XWebService
{
    private final static Logger        $Logger              = new Logger(XWebService.class);
    
    private final static String        $Default_CharSetName = "UTF-8";
    
    private final static long          $Default_Timeout     = 10000;
    
    private final static String        $XJava_RootSignName  = "xws_config";
    
    private final static String        $XJava_DatasSignName = "xws_datas";
    
    private final static String        $XJava_NamingSpace   = "XWebService";
    
    
    /** 字符集 */
    private String                     charsetName;
    
    /** 超时时长(单位:毫米) */
    private long                       timeout;
    
    /** 代理 */
    private Proxy                      proxy;
    
    /** 代理服务器的IP */
    private String                     proxyHost;
    
    /** 代理服务器的端口 */
    private int                        proxyPort;
    
    /** WebService的URL地址 */
    private URL                        url;
    
    /** Soap动作 */
    private String                     soapAction;
    
    /** 请求的XML字符串 */
    private String                     requestXML;
    
    /**  
     * 响应的XML字符串的元数据信息。
     * 主要就是 XJava 中的引包 import 信息  
     */
    private ListMap<String ,String>    responseMetadata;
    
    /** 
     * 响应的XML字符串，提供给按XJava方式构造对象的XML的Root节点的名称。
     * 响应原信息中的哪个节点(或标记)做为转换成实例对象的配置信息。
     * 
     * 可为空。 
     */
    private String                     responseRootName;
    
    
    
    /**
     * 当作为服务端时，返回给外界请求的XML字符串（模板级）
     */
    private String                     returnXML;
    
    /**
     * 当作为服务端时，解释外界请求XML的元数据信息。
     * 主要就是 XJava 中的引包 import 信息  
     */
    private ListMap<String ,String>    receiveMetadata;
    
    /** 
     * 当作为服务端时，解释外界请求XML，提供给按XJava方式构造对象的XML的Root节点的名称。
     * 外界请求XML中的哪个节点(或标记)做为转换成实例对象的配置信息。
     * 
     * 可为空。 
     */
    private String                     receiveRootName;
    
    
    
    public XWebService()
    {
        this(null ,null);
    }
    
    
    
    public XWebService(URL i_URL ,String i_RequestXML)
    {
        this(i_URL ,null ,i_RequestXML);
    }
    
    
    
    public XWebService(URL i_URL ,String i_SoapAction ,String i_RequestXML)
    {
        this($Default_CharSetName ,null ,i_URL ,i_SoapAction ,i_RequestXML);
    }
    
    
    
    public XWebService(String i_CharSetName ,Proxy i_Proxy ,URL i_URL ,String i_SoapAction ,String i_RequestXML)
    {
        this.charsetName = i_CharSetName;
        this.timeout     = $Default_Timeout;
        this.proxy       = i_Proxy;
        this.url         = i_URL;
        this.soapAction  = i_SoapAction;
        this.requestXML  = i_RequestXML;
    }
    
    
    
    /**
     * 当作为服务端时，获取返回给外界请求的XML字符串
     * 
     * 通过模板生成的XML字符串
     * 
     * @param i_Params
     * @return
     */
    public String getReturnXML(XWebServiceParam i_Params)
    {
        return this.getReturnXML(i_Params.getXWSParam());
    }
    
    
    
    /**
     * 当作为服务端时，获取返回给外界请求的XML字符串
     * 
     * 通过模板生成的XML字符串
     * 
     * @param i_Params
     * @return
     */
    public String getReturnXML(Map<String ,Object> i_Params)
    {
        if ( Help.isNull(this.returnXML) )
        {
            throw new java.lang.NullPointerException("Return XML is null.");
        }
        
        return this.returnParamParse(this.returnXML ,i_Params);
    }
    
    
    
    /**
     * 获取请求时生成的报文
     * 
     * 通过模板生成的XML字符串
     * 
     * @param i_Params
     * @return
     */
    public String getRequestXML(XWebServiceParam i_Params)
    {
        return this.getRequestXML(i_Params.getXWSParam());
    }
    
    
    
    /**
     * 获取请求时生成的报文
     * 
     * 通过模板生成的XML字符串
     * 
     * @param i_Params
     * @return
     */
    public String getRequestXML(Map<String ,Object> i_Params)
    {
        if ( Help.isNull(this.requestXML) )
        {
            throw new java.lang.NullPointerException("Request XML is null.");
        }
        
        return this.requestParamParse(this.requestXML ,i_Params);
    }
    
    
    
    /**
     * 请求访问
     */
    public String request()
    {
        return this.request(new Hashtable<String ,Object>());
    }
    
    
    
    /**
     * 请求访问
     * 
     * @param i_Params
     */
    public String request(XWebServiceParam i_Params)
    {
        return this.request(i_Params.getXWSParam());
    }
    
    
    
    /**
     * 请求访问
     * 
     * @param i_Params
     */
    public String request(Map<String ,Object> i_Params)
    {
        if ( Help.isNull(this.charsetName) )
        {
            throw new java.lang.NullPointerException("CharSetName is null.");
        }
        if ( this.url == null )
        {
            throw new java.lang.NullPointerException("URL is null.");
        }
        if ( Help.isNull(this.requestXML) )
        {
            throw new java.lang.NullPointerException("Request XML is null.");
        }
        
        
        HttpURLConnection v_HttpConn    = null;
        OutputStream      v_Out         = null;
        String            v_Xml         = null;
        byte []           v_XmlBytes    = null;
        String            v_ResponseXML = "";
        
        try
        {
            v_Xml = this.requestParamParse(this.requestXML ,i_Params);
            v_XmlBytes = v_Xml.getBytes();
            
            
            if ( this.proxy == null )
            {
                v_HttpConn = (HttpURLConnection)this.url.openConnection();
            }
            else
            {
                v_HttpConn = (HttpURLConnection)this.url.openConnection(this.proxy);
            }
            v_HttpConn.setRequestProperty("Content-Length" ,String.valueOf(v_XmlBytes.length));
            v_HttpConn.setRequestProperty("Content-Type"   ,"text/xml; charset=" + this.charsetName);
            v_HttpConn.setRequestProperty("SOAPAction"     ,Help.NVL(this.soapAction ,""));
            v_HttpConn.setRequestMethod("POST");  
            v_HttpConn.setUseCaches(false);                                    // Post请求不能使用缓存 
            v_HttpConn.setDoOutput(true);
            v_HttpConn.setDoInput(true);
            v_HttpConn.setConnectTimeout((int)this.getTimeout());
            v_HttpConn.setReadTimeout((int)this.getTimeout()); 
            
            
            v_Out = v_HttpConn.getOutputStream();
            v_Out.write(v_XmlBytes);
            v_Out.close();
            
            
            v_ResponseXML = StringHelp.xmlDeCode(new String(readResponseData(v_HttpConn.getInputStream()) ,this.charsetName));
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
        finally
        {
            if ( v_Out != null )
            {
                try
                {
                    v_Out.close();
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                    exce.printStackTrace();
                }
                
                v_Out = null;
            }
            
            
            if ( v_HttpConn != null )
            {
                try
                {
                    v_HttpConn.disconnect();
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                    exce.printStackTrace();
                }
                
                v_HttpConn = null;
            }
        }
        
        return v_ResponseXML;
    }
    
    
    
    /**
     * 请求参数解释，并拼成请求报文XML
     * 
     * 通过模板生成的XML字符串
     * 
     * 这个是一个递归方法。
     *   即Map.Value也可以是一个 List<Map<String ,Object>> 类型的对象
     *   即Map.Value也可以是一个 List<XWebServiceParam>    类型的对象
     * 
     * @param i_XML_Template  请求报文XML的模板
     * @param i_Params        请求参数
     * @return
     */
    @SuppressWarnings("unchecked")
    private String requestParamParse(String i_XML_Template ,Map<String ,Object> i_Params)
    {
        Iterator<String> v_ParamNameIter = i_Params.keySet().iterator();
        String           v_RetXML        = new String(i_XML_Template);
        
        while ( v_ParamNameIter.hasNext() )
        {
            String v_ParamName = v_ParamNameIter.next();
            Object v_ParamObj  = i_Params.get(v_ParamName);
            
            if ( v_ParamObj == null )
            {
                v_RetXML = v_RetXML.replaceAll(":" +v_ParamName ,"");
            }
            else if ( v_ParamObj instanceof List )
            {
                String        v_Child_XML_Template = StringHelp.getXMLSignContent(v_RetXML ,v_ParamName);
                List<?>       v_List               = (List<?>)v_ParamObj;
                StringBuilder v_Buffer            = new StringBuilder();
                
                for (int i=0; i<v_List.size(); i++)
                {
                    Object v_ListElemp = v_List.get(i);
                    
                    // 解释 List<Map<String ,Object>> 这样的格式
                    if ( v_ListElemp instanceof Map )
                    {
                        v_Buffer.append(this.requestParamParse(v_Child_XML_Template ,(Map<String ,Object>)v_ListElemp));
                    }
                    //  解释 List<XWebServiceParam>   这样的格式
                    else if ( v_ListElemp instanceof XWebServiceParam )
                    {
                        v_Buffer.append(this.requestParamParse(v_Child_XML_Template ,((XWebServiceParam)v_ListElemp).getXWSParam()));
                    }
                }
                
                v_RetXML = v_RetXML.replaceAll(v_Child_XML_Template ,v_Buffer.toString());
            }
            else if ( v_ParamObj instanceof Date )
            {
                v_RetXML = v_RetXML.replaceAll(":" +v_ParamName ,((Date)v_ParamObj).getFull());
            }
            else if ( v_ParamObj instanceof java.util.Date )
            {
                Date v_Date = new Date((java.util.Date)v_ParamObj);
                v_RetXML = v_RetXML.replaceAll(":" +v_ParamName ,v_Date.getFull());
            }
            else
            {
                v_RetXML = v_RetXML.replaceAll(":" +v_ParamName ,v_ParamObj.toString().trim());
            }
        }
        
        return v_RetXML;
    }
    
    
    
    /**
     * 当作为服务端时，返回给外界请求的XML的参数解释，并拼成请求报文XML
     * 
     * 通过模板生成的XML字符串
     * 
     * 这个是一个递归方法。
     *   即Map.Value也可以是一个 List<Map<String ,Object>> 类型的对象
     *   即Map.Value也可以是一个 List<XWebServiceParam>    类型的对象
     * 
     * @param i_XML_Template  返回给外界请求的XML
     * @param i_Params        返回给外界请求的XML参数
     * @return
     */
    private String returnParamParse(String i_XML_Template ,Map<String ,Object> i_Params)
    {
        // 方法实现功能一样，固不再重复编写。
        return this.requestParamParse(i_XML_Template ,i_Params);
    }
    
    
    
    /**
     * 从输入流中读取数据
     * 
     * @param io_Input
     * @return
     */
    private byte [] readResponseData(InputStream io_Input)
    {
        ByteArrayOutputStream v_Out  = new ByteArrayOutputStream();
        byte []               v_Data = new byte[0];
        
        try
        {
            byte [] v_Buffer = new byte[1024];
            int     v_Len    = 0;
            
            while ( (v_Len = io_Input.read(v_Buffer)) != -1 )
            {
                v_Out.write(v_Buffer ,0 ,v_Len);
            }
            
            v_Data = v_Out.toByteArray();        // 网页的二进制数据
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
        finally
        {
            
            if ( v_Out != null )
            {
                try
                {
                    v_Out.close(); 
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                    exce.printStackTrace();
                }
                
                v_Out = null;
            }
            

            if ( io_Input != null )
            {
                try
                {
                    io_Input.close(); 
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                    exce.printStackTrace();
                }
                
                io_Input = null;
            }
        }
        
        return v_Data;
    }
    
    
    
    /**
     * 获取响应信息，并按 XJava 方式转换成实例对象返回
     *
     * @return
     */
    public Object getResponseObj(String i_ResponseXML)
    {
        return this.getResponseObj(i_ResponseXML ,this.responseRootName);
    }
    
    
    
    /**
     * 获取响应信息，并按 XJava 方式转换成实例对象返回
     * 
     * @param i_RootSignName  响应原信息中的哪个节点(或标记)做为转换成实例对象的配置信息。可忽略大小写的匹配
     * @return
     */
    public Object getResponseObj(String i_ResponseXML ,String i_RootSignName)
    {
        if ( Help.isNull(this.responseMetadata) )
        {
            return null;
        }
        
        if ( Help.isNull(i_ResponseXML) )
        {
            return null;
        }
        
        
        StringBuilder v_XJava_XML = new StringBuilder();
        v_XJava_XML.append("<").append($XJava_RootSignName).append(">");
        v_XJava_XML.append("<").append($XJava_DatasSignName).append(">");
        if ( Help.isNull(i_RootSignName) )
        {
            v_XJava_XML.append(i_ResponseXML);
        }
        else
        {
            v_XJava_XML.append(StringHelp.getXMLSignContent(i_ResponseXML ,i_RootSignName.trim()));
        }
        v_XJava_XML.append("</").append($XJava_DatasSignName).append(">");
        v_XJava_XML.append("</").append($XJava_RootSignName).append(">");
        
        try
        {
            return XJava.parserXml(this.responseMetadata ,v_XJava_XML.toString() ,"" ,$XJava_NamingSpace);
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 当作为服务端时，解释外界请求XML，并按 XJava 方式转换成实例对象返回
     * 
     * @param i_ReceiveXML    接收到的外界请求XML
     * @param i_RootSignName  外界请求XML中的哪个节点(或标记)做为转换成实例对象的配置信息。可忽略大小写的匹配
     * @return
     */
    public Object getReceiveObj(String i_ReceiveXML)
    {
        return this.getReceiveObj(i_ReceiveXML ,this.receiveRootName);
    }
    
    
    /**
     * 当作为服务端时，解释外界请求XML，并按 XJava 方式转换成实例对象返回
     * 
     * @param i_ReceiveXML    接收到的外界请求XML
     * @param i_RootSignName  外界请求XML中的哪个节点(或标记)做为转换成实例对象的配置信息。可忽略大小写的匹配
     * @return
     */
    public Object getReceiveObj(String i_ReceiveXML ,String i_RootSignName)
    {
        if ( Help.isNull(this.receiveMetadata) )
        {
            return null;
        }
        
        if ( Help.isNull(i_ReceiveXML) )
        {
            return null;
        }
        
        
        StringBuilder v_XJava_XML = new StringBuilder();
        v_XJava_XML.append("<").append($XJava_RootSignName).append(">");
        v_XJava_XML.append("<").append($XJava_DatasSignName).append(">");
        if ( Help.isNull(i_RootSignName) )
        {
            v_XJava_XML.append(i_ReceiveXML);
        }
        else
        {
            v_XJava_XML.append(StringHelp.getXMLSignContent(i_ReceiveXML ,i_RootSignName.trim()));
        }
        v_XJava_XML.append("</").append($XJava_DatasSignName).append(">");
        v_XJava_XML.append("</").append($XJava_RootSignName).append(">");
        
        try
        {
            return XJava.parserXml(this.receiveMetadata ,v_XJava_XML.toString() ,"" ,$XJava_NamingSpace);
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 添加响应元数据信息
     * 
     * @param i_XMLSignName  XML标记名称 
     * @param i_ClassName    类的全路径名称
     */
    public synchronized void addResponseMetadata(String i_XMLSignName ,String i_ClassName)
    {
        if ( Help.isNull(this.responseMetadata) )
        {
            this.responseMetadata = new ListMap<String ,String>();
            this.responseMetadata.put($XJava_DatasSignName ,"java.util.ArrayList");
        }
        
        this.responseMetadata.put(i_XMLSignName ,i_ClassName);
    }
    
    
    
    /**
     * 当作为服务端时，解释外界请求XML
     * 添加元数据信息
     * 
     * @param i_XMLSignName  XML标记名称 
     * @param i_ClassName    类的全路径名称
     */
    public synchronized void addReceiveMetadata(String i_XMLSignName ,String i_ClassName)
    {
        if ( Help.isNull(this.receiveMetadata) )
        {
            this.receiveMetadata = new ListMap<String ,String>();
            this.receiveMetadata.put($XJava_DatasSignName ,"java.util.ArrayList");
        }
        
        this.receiveMetadata.put(i_XMLSignName ,i_ClassName);
    }

    
    
    public ListMap<String ,String> getResponseMetadata()
    {
        return responseMetadata;
    }


    
    public ListMap<String ,String> getReceiveMetadata()
    {
        return receiveMetadata;
    }



    public String getCharsetName()
    {
        return charsetName;
    }
    
    
    
    public void setCharsetName(String charsetName)
    {
        this.charsetName = charsetName;
    }
    
    
    
    public Proxy getProxy()
    {
        return proxy;
    }


    
    public void setProxy(Proxy i_Proxy)
    {
        this.proxy = i_Proxy;
    }


    
    public String getRequestXML()
    {
        return requestXML;
    }


    
    public void setRequestXML(String requestXML)
    {
        this.requestXML = requestXML;
    }
    
    
    
    public void setRequestXMLByFile(String i_FileURL)
    {
        this.setRequestXMLByFile(i_FileURL ,this.charsetName);
    }
    
    
    
    public void setRequestXMLByFile(String i_FileFullName ,String i_CharSetName)
    {
        try
        {
            this.requestXML = (new FileHelp()).getContent(i_FileFullName ,i_CharSetName);
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
    }
    
    
    
    public void setReturnXML(String returnXML)
    {
        this.returnXML = returnXML;
    }
    
    
    
    public void setReturnXMLByFile(String i_FileURL)
    {
        this.setReturnXMLByFile(i_FileURL ,this.charsetName);
    }
    
    
    
    public void setReturnXMLByFile(String i_FileFullName ,String i_CharSetName)
    {
        try
        {
            this.returnXML = (new FileHelp()).getContent(i_FileFullName ,i_CharSetName);
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
    }


    
    public String getSoapAction()
    {
        return soapAction;
    }


    
    public void setSoapAction(String soapAction)
    {
        this.soapAction = soapAction;
    }


    
    public URL getUrl()
    {
        return url;
    }

    
    
    public void setUrl(String i_URL)
    {
        if ( Help.isNull(i_URL) )
        {
            throw new java.lang.NullPointerException("URL is null.");
        }
        
        try
        {
            this.url = new URL(i_URL);
        }
        catch (Exception exce)
        {
            $Logger.error(exce);
            exce.printStackTrace();
        }
    }

    
    
    public long getTimeout()
    {
        return timeout;
    }

    
    
    public void setTimeout(long timeout)
    {
        if ( timeout < 0 )
        {
            return;
        }
        else
        {
            this.timeout = timeout;
        }
    }


    
    public String getResponseRootName()
    {
        return responseRootName;
    }


    
    public void setResponseRootName(String responseRootName)
    {
        this.responseRootName = responseRootName;
    }


    
    public String getReceiveRootName()
    {
        return receiveRootName;
    }


    
    public void setReceiveRootName(String receiveRootName)
    {
        this.receiveRootName = receiveRootName;
    }


    
    public String getProxyHost()
    {
        return proxyHost;
    }


    
    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
        
        if ( !Help.isNull(this.proxyHost) && 0 < this.proxyPort && this.proxyPort < 65535 )
        {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost ,this.proxyPort));
        }
    }


    
    public int getProxyPort()
    {
        return proxyPort;
    }


    
    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
        
        if ( !Help.isNull(this.proxyHost) && 0 < this.proxyPort && this.proxyPort < 65535 )
        {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost ,this.proxyPort));
        }
    }

    

    /*
    @Override
    protected void finalize() throws Throwable
    {
        if ( this.responseMetadata != null )
        {
            this.responseMetadata.clear();
            this.responseMetadata = null;
        }
        
        super.finalize();
    }
    */
}
