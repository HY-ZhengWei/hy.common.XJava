package org.hy.common.xml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.Return;
import org.hy.common.StringHelp;





/**
 * Http访问
 *
 * 1. 可按对象参数发起请求
 * 2. 可按集合参数发起请求
 * 3. 可直接按完整的URL发起请求
 * 4. 支持代理访问
 * 5. 支持HTTPS安全访问
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-08-06
 *           V2.0  2016-06-22  添加1：haveQuestionMark 参数。是否自动填充问号(?)。
 *                             添加2：当配置文件中定义的请求参数，外界没有在请求时传入值时，
 *                                   同时配置文件中也没有定义默认时，此请求参数将不再拼接到生成的URL中。
 *           V3.0  2017-02-21  修改1：将属性responseInfo(响应信息)删除，不再通过属性的方式保存响应信息。
 *                                   而是通过请求方法request(...)返回值直接返回响应信息。
 *                                   这样最大好处是：不用再每次请求时创建一个XHttp的实例。
 */
public final class XHttp 
{  
    /** 请求类型:get方式 */
    public static  final int    $Request_Type_Get  = 1;
    
    /** 请求类型:post方式 */
    public static  final int    $Request_Type_Post = 2;
    
    /** URL转义时被排除的字符 */
    private static final String $NotInString       = "=&?";
    
    /** 用于Https安全访问 */
    private static SSLContext   $SSLContext;
    
    
    
    /** 请求协议类型(http、https) */
    private String                     protocol;
    
    /** HTTP主机IP地址(此属性也可以是域名，如www.sina.com) */
    private String                     ip;
    
    /** HTTP主机端口(默认为0，表示可以不明确说明访问端口) */
    private int                        port;
    
    /** HTTP请求地址。不含"http://IP:Port/"。不含请求参数  */
    private String                     url;
    
    /** HTTP请求参数 */
    private List<XHttpParam>           httpParams; 
    
    /** 请求参数无默认值的数量 */
    @SuppressWarnings("unused")
    private int                        notDefaultParamValueCount;
    
    /** 请求类型。1:get方式(默认值)  2:post方式 */
    private int                        requestType;
    
    /** 请求内容类型(默认:text/html) */
    private String                     contentType;
    
    /** 请求字符集名称(默认:UTF-8) */
    private String                     charset;
    
    /** 是否对请求参数转义(默认:true) */
    private boolean                    isToUnicode;
    
    /** 
     * 是否自动填充问号(?) 。默认:true
     * 当生成访问URL时，是否生成如 http://ip:port/xx?yy=zz 中的问号 
     */
    private boolean                    haveQuestionMark;
    
    /** 代理 */
    private Proxy                      proxy;
    
    /** 代理服务器的IP */
    private String                     proxyHost;
    
    /** 代理服务器的端口 */
    private int                        proxyPort;
    
    /** Cookie信息 */
    private XHttpCookie                cookie;
    
    
    
    public XHttp()
    {
        this.protocol                  = "http";
        this.port                      = 0;
        this.requestType               = $Request_Type_Get;
        this.notDefaultParamValueCount = 0;
        this.contentType               = "text/html";
        this.charset                   = "UTF-8";
        this.isToUnicode               = true;
        this.haveQuestionMark          = true;
        this.proxy                     = null;
        this.cookie                    = new XHttpCookie();
    }
    
    
    
    /**
     * 发起Http请求 -- 无参数的
     * 
     * @return  返回是否请求成功
     *          Return.paramStr  保存响应信息
     *          Return.exception 保存异常信息
     */
    public Return<?> request()
    {
/*      
        // 代理服务器的IP
        String v_HttpProxy     = "";
        // 代理服务器的端口
        String v_HttpProxyPort = "";
    
        Properties v_SystemProperties = System.getProperties();
        v_SystemProperties.setProperty("http.proxyHost", v_HttpProxy);
        v_SystemProperties.setProperty("http.proxyPort", v_HttpProxyPort);
*/  
        
        return this.request((Object)null);
    }
    
    
    
    /**
     * 发起Http请求 -- 对象参数
     * 
     * @param   i_ParamObj  对象参数
     * @return  返回是否请求成功。
     *          Return.paramStr  保存响应信息
     *          Return.exception 保存异常信息
     */
    public Return<?> request(Object i_ParamObj)
    {
        if ( Help.isNull(this.getIp()) )
        {
            throw new NullPointerException("XHttp ip is null.");
        }
        
        Return<?>           v_Ret        = new Return<Object>().paramStr("");
        URL                 v_URL        = null;
        HttpURLConnection   v_URLConn    = null;
        BufferedReader      v_Reader     = null;
        StringBuilder       v_RespBuffer = new StringBuilder();
        
        try
        {
            String v_ParamsUrl = this.getParamsUrl(i_ParamObj);
            
            if ( this.requestType == $Request_Type_Post )
            {
                if ( this.getPort() == 0 )
                {
                    v_URL = new URL(this.getProtocol() + "://" + this.getIp() + this.getUrl());
                }
                else
                {
                    v_URL = new URL(this.getProtocol() ,this.getIp() ,this.getPort() ,this.getUrl());
                }
                
                if ( this.proxy != null )
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection(this.proxy);
                }
                else
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection();
                }
                
                if ( "https".equals(this.getProtocol()) )
                {
                    ((HttpsURLConnection)v_URLConn).setSSLSocketFactory(getSSLContext().getSocketFactory());
                    ((HttpsURLConnection)v_URLConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
                }
                
                v_URLConn.setUseCaches(false);
                v_URLConn.setDoOutput(true);
                v_URLConn.setRequestMethod("POST");
                v_URLConn.setRequestProperty("Content-Type" ,this.getContentType() + "; charset=" + this.getCharset());
                v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                v_URLConn.setRequestProperty("Cookie"       ,this.cookie.toString());
                
                v_URLConn.getOutputStream().write(v_ParamsUrl.getBytes(this.getCharset()));
                v_URLConn.getOutputStream().flush();
                v_URLConn.getOutputStream().close();
            }
            else
            {
                if ( this.isToUnicode )
                {
                    v_ParamsUrl = StringHelp.escape_toUnicode(v_ParamsUrl ,$NotInString);
                }
                
                String v_URLParamStr = this.getUrl();
                if ( this.haveQuestionMark && v_URLParamStr.indexOf("?") < 0 )
                {
                    v_URLParamStr = v_URLParamStr + "?";
                }
                
                if ( this.getPort() == 0 )
                {
                    v_URL = new URL(this.getProtocol() + "://" + this.getIp() + v_URLParamStr + v_ParamsUrl);
                }
                else
                {
                    v_URL = new URL(this.getProtocol() ,this.getIp() ,this.getPort() ,v_URLParamStr + v_ParamsUrl);
                }
                
                if ( this.proxy != null )
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection(this.proxy);
                }
                else
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection();
                }
                
                if ( "https".equals(this.getProtocol()) )
                {
                    ((HttpsURLConnection)v_URLConn).setSSLSocketFactory(getSSLContext().getSocketFactory());
                    ((HttpsURLConnection)v_URLConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
                }
                
                v_URLConn.setUseCaches(false);
                v_URLConn.setRequestMethod("GET");
                v_URLConn.setRequestProperty("Content-Type" ,this.getContentType() + "; charset=" + this.getCharset());
                v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                v_URLConn.setRequestProperty("Cookie"       ,this.cookie.toString());
                
                
                // 获取GET方式下，真实的客户端SessionID Add 2015-03-04
                String v_SessionID = "";
                String v_Key       = "";
                for (int v_KeyIndex=1; (v_Key = v_URLConn.getHeaderFieldKey(v_KeyIndex)) != null; v_KeyIndex++)
                {
                    if ( v_Key.equalsIgnoreCase("set-cookie") )
                    {
                        String v_CookieVal = v_URLConn.getHeaderField(v_KeyIndex);
                        v_CookieVal = v_CookieVal.substring(0 ,v_CookieVal.indexOf(";") > -1 ? v_CookieVal.indexOf(";") : v_CookieVal.length() - 1);
                        v_SessionID = v_SessionID + v_CookieVal + ";";
                        
                        String [] v_CookieArr = v_CookieVal.split("=");
                        this.cookie.put(v_CookieArr[0] ,v_CookieArr[1]);
                    }
                }
            }
            
            
            v_Reader = new BufferedReader(new InputStreamReader(v_URLConn.getInputStream() ,this.getCharset()));
            String v_LineData = "";
            
            while ( (v_LineData = v_Reader.readLine()) != null ) 
            {   
                v_RespBuffer.append(v_LineData);
            }  
            
            v_Reader.close();
        }
        catch (Exception exce)
        {
            return v_Ret.set(false).exception(exce);
        }
        finally
        {
            if ( v_Reader != null )
            {
                try
                {
                    v_Reader.close();
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                v_Reader = null;
            }
            
            v_URLConn = null;
            v_URL     = null;
        }

        return v_Ret.paramStr(v_RespBuffer.toString().trim()).set(true);
    }
    
    
    
    /**
     * 发起Http请求 -- 集合参数
     * 
     * @param   i_ParamValues  集合参数
     * @return  返回是否请求成功
     *          Return.paramStr  保存响应信息
     *          Return.exception 保存异常信息
     */
    public Return<?> request(Map<String ,?> i_ParamValues)
    {
        if ( Help.isNull(this.getIp()) )
        {
            throw new NullPointerException("XHttp ip is null.");
        }
        
        Return<?>         v_Ret        = new Return<Object>().paramStr("");
        URL               v_URL        = null;
        HttpURLConnection v_URLConn    = null;
        BufferedReader    v_Reader     = null;
        StringBuilder     v_RespBuffer = new StringBuilder();
        
        try
        {
            String v_ParamsUrl = this.getParamsUrl(i_ParamValues);
            
            if ( this.requestType == $Request_Type_Post )
            {
                if ( this.getPort() == 0 )
                {
                    v_URL = new URL(this.getProtocol() + "://" + this.getIp() + this.getUrl());
                }
                else
                {
                    v_URL = new URL(this.getProtocol() ,this.getIp() ,this.getPort() ,this.getUrl());
                }
                
                if ( this.proxy != null )
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection(this.proxy);
                }
                else
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection();
                }
                
                if ( "https".equals(this.getProtocol()) )
                {
                    ((HttpsURLConnection)v_URLConn).setSSLSocketFactory(getSSLContext().getSocketFactory());
                    ((HttpsURLConnection)v_URLConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
                }
                
                v_URLConn.setUseCaches(false);
                v_URLConn.setDoOutput(true);
                v_URLConn.setRequestMethod("POST");
                v_URLConn.setRequestProperty("Content-Type" ,this.getContentType() + "; charset=" + this.getCharset());
                v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                v_URLConn.setRequestProperty("Cookie"       ,this.cookie.toString());
                
                v_URLConn.getOutputStream().write(v_ParamsUrl.getBytes(this.getCharset()));
                v_URLConn.getOutputStream().flush();
                v_URLConn.getOutputStream().close();
            }
            else
            {
                if ( this.isToUnicode )
                {
                    v_ParamsUrl = StringHelp.escape_toUnicode(v_ParamsUrl ,$NotInString);
                }
                
                String v_URLParamStr = this.getUrl();
                if ( this.haveQuestionMark && v_URLParamStr.indexOf("?") < 0 )
                {
                    v_URLParamStr = v_URLParamStr + "?";
                }
                else
                {
                    if ( !Help.isNull(v_ParamsUrl) )
                    {
                        v_ParamsUrl = "&" + v_ParamsUrl;
                    }
                }
                
                if ( this.getPort() == 0 )
                {
                    v_URL = new URL(this.getProtocol() + "://" + this.getIp() + v_URLParamStr + v_ParamsUrl);
                }
                else
                {
                    v_URL = new URL(this.getProtocol() ,this.getIp() ,this.getPort() ,v_URLParamStr + v_ParamsUrl);
                }
                
                if ( this.proxy != null )
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection(this.proxy);
                }
                else
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection();
                }
                
                if ( "https".equals(this.getProtocol()) )
                {
                    ((HttpsURLConnection)v_URLConn).setSSLSocketFactory(getSSLContext().getSocketFactory());
                    ((HttpsURLConnection)v_URLConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
                }
                
                v_URLConn.setUseCaches(false);
                v_URLConn.setRequestMethod("GET");
                v_URLConn.setRequestProperty("Content-Type" ,this.getContentType() + "; charset=" + this.getCharset());
                v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/5.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                v_URLConn.setRequestProperty("Cookie"       ,this.cookie.toString());
                
                
                // 获取GET方式下，真实的客户端SessionID Add 2015-03-04
                String v_SessionID = "";
                String v_Key       = "";
                for (int v_KeyIndex=1; (v_Key = v_URLConn.getHeaderFieldKey(v_KeyIndex)) != null; v_KeyIndex++)
                {
                    if ( v_Key.equalsIgnoreCase("set-cookie") )
                    {
                        String v_CookieVal = v_URLConn.getHeaderField(v_KeyIndex);
                        v_CookieVal = v_CookieVal.substring(0 ,v_CookieVal.indexOf(";") > -1 ? v_CookieVal.indexOf(";") : v_CookieVal.length() - 1);
                        v_SessionID = v_SessionID + v_CookieVal + ";";
                        
                        String [] v_CookieArr = v_CookieVal.split("=");
                        this.cookie.put(v_CookieArr[0] ,v_CookieArr[1]);
                    }
                }
            }
            
            v_Reader = new BufferedReader(new InputStreamReader(v_URLConn.getInputStream() ,this.getCharset()));
            String v_LineData = "";
            
            while ( (v_LineData = v_Reader.readLine()) != null ) 
            {   
                v_RespBuffer.append(v_LineData);
            }
            
            v_Reader.close();
        }
        catch (Exception exce)
        {
            return v_Ret.set(false).exception(exce);
        }
        finally
        {
            if ( v_Reader != null )
            {
                try
                {
                    v_Reader.close();
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                v_Reader = null;
            }
            
            v_URLConn = null;
            v_URL     = null;
        }
        
        return v_Ret.paramStr(v_RespBuffer.toString().trim()).set(true);
    }
    
    
    
    /**
     * 发起Http请求 -- 参数字符串
     * 
     * @param   i_ParamString  参数字符串。这个字符串已被外界拼接好了。
     * @return  返回是否请求成功
     *          Return.paramStr  保存响应信息
     *          Return.exception 保存异常信息
     */
    public Return<?> request(String i_ParamString)
    {
        if ( Help.isNull(this.getIp()) )
        {
            throw new NullPointerException("XHttp ip is null.");
        }
        
        Return<?>         v_Ret        = new Return<Object>().paramStr("");
        URL               v_URL        = null;
        HttpURLConnection v_URLConn    = null;
        BufferedReader    v_Reader     = null;
        StringBuilder     v_RespBuffer = new StringBuilder();
        
        try
        {
            String v_ParamsUrl = i_ParamString.trim();
            
            if ( this.requestType == $Request_Type_Post )
            {
                if ( this.getPort() == 0 )
                {
                    v_URL = new URL(this.getProtocol() + "://" + this.getIp() + this.getUrl());
                }
                else
                {
                    v_URL = new URL(this.getProtocol() ,this.getIp() ,this.getPort() ,this.getUrl());
                }
                
                if ( this.proxy != null )
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection(this.proxy);
                }
                else
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection();
                }
                
                if ( "https".equals(this.getProtocol()) )
                {
                    ((HttpsURLConnection)v_URLConn).setSSLSocketFactory(getSSLContext().getSocketFactory());
                    ((HttpsURLConnection)v_URLConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
                }
                
                v_URLConn.setUseCaches(false);
                v_URLConn.setDoOutput(true);
                v_URLConn.setRequestMethod("POST");
                v_URLConn.setRequestProperty("Content-Type" ,this.getContentType() + "; charset=" + this.getCharset());
                v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                v_URLConn.setRequestProperty("Cookie"       ,this.cookie.toString());
                
                v_URLConn.getOutputStream().write(v_ParamsUrl.getBytes(this.getCharset()));
                v_URLConn.getOutputStream().flush();
                v_URLConn.getOutputStream().close();
            }
            else
            {
                if ( this.isToUnicode )
                {
                    v_ParamsUrl = StringHelp.escape_toUnicode(v_ParamsUrl ,$NotInString);
                }
                
                String v_URLParamStr = this.getUrl();
                if ( this.haveQuestionMark && v_URLParamStr.indexOf("?") < 0 )
                {
                    v_URLParamStr = v_URLParamStr + "?";
                }
                
                if ( this.getPort() == 0 )
                {
                    v_URL = new URL(this.getProtocol() + "://" + this.getIp() + v_URLParamStr + v_ParamsUrl);
                }
                else
                {
                    v_URL = new URL(this.getProtocol() ,this.getIp() ,this.getPort() ,v_URLParamStr + v_ParamsUrl);
                }
                
                if ( this.proxy != null )
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection(this.proxy);
                }
                else
                {
                    v_URLConn = (HttpURLConnection)v_URL.openConnection();
                }
                
                if ( "https".equals(this.getProtocol()) )
                {
                    ((HttpsURLConnection)v_URLConn).setSSLSocketFactory(getSSLContext().getSocketFactory());
                    ((HttpsURLConnection)v_URLConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
                }
                
                v_URLConn.setUseCaches(false);
                v_URLConn.setRequestMethod("GET");
                v_URLConn.setRequestProperty("Content-Type" ,this.getContentType() + "; charset=" + this.getCharset());
                v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                v_URLConn.setRequestProperty("Cookie"       ,this.cookie.toString());
                
                
                // 获取GET方式下，真实的客户端SessionID Add 2015-03-04
                String v_SessionID = "";
                String v_Key       = "";
                for (int v_KeyIndex=1; (v_Key = v_URLConn.getHeaderFieldKey(v_KeyIndex)) != null; v_KeyIndex++)
                {
                    if ( v_Key.equalsIgnoreCase("set-cookie") )
                    {
                        String v_CookieVal = v_URLConn.getHeaderField(v_KeyIndex);
                        v_CookieVal = v_CookieVal.substring(0 ,v_CookieVal.indexOf(";") > -1 ? v_CookieVal.indexOf(";") : v_CookieVal.length() - 1);
                        v_SessionID = v_SessionID + v_CookieVal + ";";
                        
                        String [] v_CookieArr = v_CookieVal.split("=");
                        this.cookie.put(v_CookieArr[0] ,v_CookieArr[1]);
                    }
                }
            }
            
            
            v_Reader = new BufferedReader(new InputStreamReader(v_URLConn.getInputStream() ,this.getCharset()));
            String v_LineData = "";
            
            while ( (v_LineData = v_Reader.readLine()) != null ) 
            {   
                v_RespBuffer.append(v_LineData);
            }  
            
            v_Reader.close();
        }
        catch (Exception exce)
        {
            return v_Ret.set(false).exception(exce);
        }
        finally
        {
            if ( v_Reader != null )
            {
                try
                {
                    v_Reader.close();
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                v_Reader = null;
            }
            
            v_URLConn = null;
            v_URL     = null;
        }
        
        return v_Ret.paramStr(v_RespBuffer.toString().trim()).set(true);
    }
    
    
    
    /**
     * 发起Http请求 -- 外界自定义URL
     * 
     * 使用Get方式发起请求
     * 
     * @param   i_URL  完整的请求URL。如http://IP:Port/xxx.do?p1=v1&p2=v2
     * @return  返回是否请求结果的字符串
     *          当异常时，返回异常信息
     */
    public static String requestGet(String i_FullURL)
    {
        return requestGet(i_FullURL ,null);
    }
    
    
    
    
    /**
     * 发起Http请求 -- 外界自定义URL
     * 
     * 使用Get方式发起请求
     * 
     * @param   i_URL    完整的请求URL。如http://IP:Port/xxx.do?p1=v1&p2=v2
     * @param   i_Proxy  代理
     * @return  返回是否请求结果的字符串
     *          当异常时，返回异常信息
     */
    public static String requestGet(String i_FullURL ,Proxy i_Proxy)
    {
        if ( Help.isNull(i_FullURL) )
        {
            throw new NullPointerException("XHttp FullURL is null.");
        }
        
        URL               v_URL        = null;
        HttpURLConnection v_URLConn    = null;
        BufferedReader    v_Reader     = null;
        StringBuilder     v_RespBuffer = new StringBuilder();
        
        try
        {
            v_URL = new URL(i_FullURL);
            if ( i_Proxy != null )
            {
                v_URLConn = (HttpURLConnection)v_URL.openConnection(i_Proxy);
            }
            else
            {
                v_URLConn = (HttpURLConnection)v_URL.openConnection();
            }
            v_URLConn.setUseCaches(false);
            v_URLConn.setRequestMethod("GET");
            v_URLConn.setRequestProperty("Content-Type" ,"text/html; charset=UTF-8");
            v_URLConn.setRequestProperty("User-Agent"   ,"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            
            
            v_Reader = new BufferedReader(new InputStreamReader(v_URLConn.getInputStream() ,"UTF-8"));
            String v_LineData = "";
            
            while ( (v_LineData = v_Reader.readLine()) != null ) 
            {   
                v_RespBuffer.append(v_LineData);
            }  
            
            v_Reader.close();
        }
        catch (Exception exce)
        {
            return exce.getMessage();
        }
        finally
        {
            if ( v_Reader != null )
            {
                try
                {
                    v_Reader.close();
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                v_Reader = null;
            }
            
            v_URLConn = null;
            v_URL     = null;
        }
        

        return v_RespBuffer.toString().trim();
    }
    
    
    
    /**
     * 获取参数拼接成的部分URL字符串。
     * 
     * 格式为：UrlParamName1=ParamValue1&UrlParamName2=ParamValue2...
     * 
     * @param i_ParamObj
     * @return
     * @throws NoSuchMethodException
     */
    private String getParamsUrl(Object i_ParamObj) throws NoSuchMethodException
    {
        if ( Help.isNull(this.httpParams) )
        {
            return "";
        }
        
        StringBuilder v_ParamBuffer = new StringBuilder();
        boolean       v_IsFirst     = true;
        
        for (int i=0; i<this.httpParams.size(); i++)
        {
            XHttpParam v_XHttpParam = this.httpParams.get(i);
            String     v_ParamValue = this.getParamValue(v_XHttpParam ,i_ParamObj);
            
            if ( Help.isNull(v_ParamValue) )
            {
                v_ParamValue = Help.NVL(v_XHttpParam.getParamValue());  // 取配置文件中的默认值
            }
            
            if ( !Help.isNull(v_ParamValue) )
            {
                if ( !v_IsFirst )
                {
                    v_ParamBuffer.append("&");
                }
                
                v_ParamBuffer.append(v_XHttpParam.getUrlParamName()).append("=").append(v_ParamValue);
                v_IsFirst = false;
            }
        }
        
        return v_ParamBuffer.toString();
    }
    
    
    
    /**
     * 获取参数拼接成的部分URL字符串。
     * 
     * 格式为：UrlParamName1=ParamValue1&UrlParamName2=ParamValue2...
     * 
     * @param i_ParamValues
     * @return
     * @throws NoSuchMethodException
     */
    private String getParamsUrl(Map<String ,?> i_ParamValues)
    {
        if ( Help.isNull(this.httpParams) )
        {
            return "";
        }
        
        StringBuilder v_ParamBuffer = new StringBuilder();
        boolean       v_IsFirst     = true;
        
        for (int i=0; i<this.httpParams.size(); i++)
        {
            XHttpParam v_XHttpParam = this.httpParams.get(i);
            String     v_ParamValue = this.getParamValue(v_XHttpParam ,i_ParamValues);
            
            if ( Help.isNull(v_ParamValue) )
            {
                v_ParamValue = Help.NVL(v_XHttpParam.getParamValue());  // 取配置文件中的默认值
            }
            
            if ( !Help.isNull(v_ParamValue) )
            {
                if ( !v_IsFirst )
                {
                    v_ParamBuffer.append("&");
                }
                
                v_ParamBuffer.append(v_XHttpParam.getUrlParamName()).append("=").append(v_ParamValue);
                v_IsFirst = false;
            }
        }
        
        return v_ParamBuffer.toString();
    }
    
    
    
    /**
     * 获取对象参数的值
     * 
     * @param i_XHttpParam
     * @param i_ParamObj
     * @return
     * @throws NoSuchMethodException
     */
    private String getParamValue(XHttpParam i_XHttpParam ,Object i_ParamObj) throws NoSuchMethodException
    {
        if ( i_ParamObj == null )
        {
            return Help.NVL(i_XHttpParam.getParamValue());
        }
        
        String v_MethodName = i_XHttpParam.getParamName();
        
        if ( !v_MethodName.startsWith("get") )
        {
            v_MethodName = "get" + v_MethodName;
        }
        
        List<Method> v_Methods = MethodReflect.getMethodsIgnoreCase(i_ParamObj.getClass() ,v_MethodName ,0);
        
        
        if ( v_Methods.size() == 1 )
        {
            try
            {
                Object v_Value = v_Methods.get(0).invoke(i_ParamObj);
                
                if ( v_Value == null )
                {
                    return Help.NVL(i_XHttpParam.getParamValue());
                }
                else
                {
                    return v_Value.toString();
                }
            }
            catch (Exception exce)
            {
                throw new NoSuchMethodException("XHttp param name [" + i_XHttpParam.getParamName() + "] is not find Object method.");
            }
        }
        else if ( v_Methods.size() >= 2 )
        {
            throw new NoSuchMethodException("XHttp param name [" + i_XHttpParam.getParamName() + "] method is not only."); 
        }
        else
        {
            return Help.NVL(i_XHttpParam.getParamValue());
        }
    }
    
    
    
    /**
     * 获取对象参数的值
     * 
     * @param i_XHttpParam
     * @param i_ParamObj
     * @return
     * @throws NoSuchMethodException
     */
    private String getParamValue(XHttpParam i_XHttpParam ,Map<String ,?> i_ParamValues)
    {
        if ( Help.isNull(i_ParamValues) )
        {
            return Help.NVL(i_XHttpParam.getParamValue());
        }
        
        
        if ( i_ParamValues.containsKey(i_XHttpParam.getParamName()) )
        {
            Object v_Value = i_ParamValues.get(i_XHttpParam.getParamName());
            
            if ( v_Value == null )
            {
                return Help.NVL(i_XHttpParam.getParamValue());
            }
            else
            {
                return v_Value.toString();
            }
        }
        else
        {
            return Help.NVL(i_XHttpParam.getParamValue());
        }
    }
    
    
    
    /**
     * 添加请求参数
     * 
     * @param i_HttpParam
     */
    public synchronized void setAddParam(XHttpParam i_HttpParam)
    {
        if ( i_HttpParam == null || !i_HttpParam.isValid() )
        {
            throw new NullPointerException("XHttp setAddParam(XHttpParam) parameter is null or not vaild.");
        }
        
        if ( this.httpParams == null )
        {
            this.httpParams = new ArrayList<XHttpParam>();
        }
        
        this.httpParams.add(i_HttpParam);
        
        if ( Help.isNull(i_HttpParam.getParamValue()) )
        {
            this.notDefaultParamValueCount++;
        }
    }
    
    
    
    public void setParams(List<XHttpParam> i_XHttpParams)
    {
        this.httpParams                = i_XHttpParams;
        this.notDefaultParamValueCount = 0;
        
        if ( !Help.isNull(i_XHttpParams) )
            
        for (XHttpParam v_XHttpParam : i_XHttpParams)
        {
            if ( Help.isNull(v_XHttpParam.getParamValue()) )
            {
                this.notDefaultParamValueCount++;
            }
        }
    }
    
    
    
    public List<XHttpParam> getParams()
    {
        return this.httpParams;
    }
    
    
    
    /**
     * 获取 Http 响应信息(只返回 Body 标签内的信息)
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-21
     * @version     v1.0
     *
     * @param i_ResponseInfo  响应信息
     * @return
     */
    public String getResponseBody(Return<?> i_ResponseInfo)
    {
        if ( i_ResponseInfo != null && !Help.isNull(i_ResponseInfo.paramStr) )
        {
            String v_Ret = StringHelp.getXMLSignContent(i_ResponseInfo.paramStr ,"body");
            
            if ( Help.isNull(v_Ret) )
            {
                return "";
            }
            else
            {
                int v_IndexOf     = v_Ret.indexOf(">");
                int v_LastIndexOf = v_Ret.lastIndexOf("<");
                if ( v_IndexOf >= 0 && v_LastIndexOf > v_IndexOf )
                {
                    return v_Ret.substring(v_IndexOf + 1 ,v_LastIndexOf);
                }
                else
                {
                    return "";
                }
            }
        }
        
        return "";
    }
    
    
    
    public String getIp()
    {
        return ip;
    }


    
    public void setIp(String ip)
    {
        this.ip = ip;
    }


    
    public int getPort()
    {
        return port;
    }


    
    public void setPort(int i_Port)
    {
        if ( 1 <= i_Port && i_Port <= 65535 )
        {
            this.port = i_Port;
        }
        else
        {
            this.port = 0;
        }
    }

    
    
    public int getRequestType()
    {
        return requestType;
    }


    
    public void setRequestType(int i_RequestType)
    {
        if ( $Request_Type_Get != i_RequestType && $Request_Type_Post != i_RequestType )
        {
            throw new IllegalArgumentException("Request type is not 1:Get or 2:Post.");
        }
        
        this.requestType = i_RequestType;
    }


    
    public String getUrl()
    {
        return url;
    }


    
    public void setUrl(String i_Url)
    {
        if ( Help.isNull(i_Url) )
        {
            this.url = "/";
        }
        
        if ( i_Url.trim().startsWith("/") )
        {
            this.url = i_Url.trim();
        }
        else
        {
            this.url = "/" + i_Url.trim();
        }
    }
    
    
    
    /**
     * 获取拼接的Http请求
     * 
     * @param i_ParamValues
     * @return
     */
    public String getRequestInfo(Map<String ,?> i_ParamValues)
    {
        StringBuilder v_Buffer = new StringBuilder();
        
        v_Buffer.append(this.getProtocol());
        v_Buffer.append("://");
        v_Buffer.append(this.getIp());
        v_Buffer.append(":");
        v_Buffer.append(this.getPort());
        v_Buffer.append(this.getUrl());
        if ( this.haveQuestionMark && !this.getUrl().endsWith("?") )
        {
            v_Buffer.append("?");
        }
        
        try
        {
            v_Buffer.append(StringHelp.escape_toUnicode(this.getParamsUrl(i_ParamValues) ,$NotInString));
        }
        catch (Exception exce)
        {
            v_Buffer.append(exce.getMessage());
        }
        
        return v_Buffer.toString();
    }
    
    
    
    /**
     * 获取拼接的Http请求
     * 
     * @param i_ParamObj
     * @return
     */
    public String getRequestInfo(Object i_ParamObj)
    {
        StringBuilder v_Buffer = new StringBuilder();
        
        v_Buffer.append(this.getProtocol());
        v_Buffer.append("://");
        v_Buffer.append(this.getIp());
        v_Buffer.append(":");
        v_Buffer.append(this.getPort());
        v_Buffer.append(this.getUrl());
        if ( this.haveQuestionMark && !this.getUrl().endsWith("?") )
        {
            v_Buffer.append("?");
        }
        
        try
        {
            v_Buffer.append(StringHelp.escape_toUnicode(this.getParamsUrl(i_ParamObj) ,$NotInString));
        }
        catch (Exception exce)
        {
            v_Buffer.append(exce.getMessage());
        }
        
        return v_Buffer.toString();
    }
    
    
    
    public String toString()
    {
        StringBuilder v_Buffer = new StringBuilder();
        
        v_Buffer.append(this.getProtocol());
        v_Buffer.append("://");
        v_Buffer.append(this.getIp());
        if ( this.getPort() > 0 )
        {
            v_Buffer.append(":");
            v_Buffer.append(this.getPort());
        }
        v_Buffer.append(this.getUrl());
        if ( this.haveQuestionMark && !this.getUrl().endsWith("?") )
        {
            v_Buffer.append("?");
        }
        
        try
        {
            v_Buffer.append(this.getParamsUrl(null));
        }
        catch (Exception exce)
        {
            v_Buffer.append(exce.getMessage());
        }
        
        return v_Buffer.toString();
    }


    
    /**
     * 获取：Cookie信息
     */
    public XHttpCookie getCookie()
    {
        return cookie;
    }
    
    
    
    /**
     * 设置：Cookie信息
     * 
     * @param cookie 
     */
    public void setCookieMap(Map<String ,?> i_CookieMap)
    {
        this.cookie.putAll(i_CookieMap);
    }


    
    /**
     * 设置：Cookie信息
     * 
     * @param cookie 
     */
    public void setCookie(XHttpCookie cookie)
    {
        this.cookie = cookie;
    }



    public String getCharset()
    {
        return charset;
    }


    
    public void setCharset(String charset)
    {
        this.charset = charset;
    }


    
    public String getContentType()
    {
        return contentType;
    }


    
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }
    
    
    
    public String getProtocol()
    {
        if ( Help.isNull(this.protocol) )
        {
            return "http";
        }
        else
        {
            return this.protocol;
        }
    }
    
    
    
    public void setProtocol(String i_Protocol)
    {
        this.protocol = Help.NVL(i_Protocol).trim();
    }
    
    
    
    public boolean isToUnicode()
    {
        return isToUnicode;
    }
    
    
    
    public boolean getToUnicode()
    {
        return this.isToUnicode;
    }

    
    
    public void setToUnicode(boolean isToUnicode)
    {
        this.isToUnicode = isToUnicode;
    }
    
    
    
    /**
     * 获取：是否自动填充问号(?) 。默认:true
     * 当生成访问URL时，是否生成如 http://ip:port/xx?yy=zz 中的问号
     */
    public boolean isHaveQuestionMark()
    {
        return haveQuestionMark;
    }


    
    /**
     * 设置：是否自动填充问号(?) 。默认:true
     * 当生成访问URL时，是否生成如 http://ip:port/xx?yy=zz 中的问号
     * 
     * @param haveQuestionMark 
     */
    public void setHaveQuestionMark(boolean haveQuestionMark)
    {
        this.haveQuestionMark = haveQuestionMark;
    }



    public Proxy getProxy()
    {
        return proxy;
    }


    
    public void setProxy(Proxy i_Proxy)
    {
        this.proxy = i_Proxy;
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

    
    
    
    
    /**
     * 类似于跳过 "继续浏览此网站(不推荐)" 这样的提醒 
     */
    private static class TrustAnyTrustManager implements X509TrustManager
    {

        public void checkClientTrusted(X509Certificate [] chain ,String authType) throws CertificateException
        {
        }



        public void checkServerTrusted(X509Certificate [] chain ,String authType) throws CertificateException
        {
        }



        public X509Certificate [] getAcceptedIssuers()
        {
            return new X509Certificate[] {};
        }
    }

    
    
    /**
     * 类似于跳过 "继续浏览此网站(不推荐)" 这样的提醒 
     */
    private static class TrustAnyHostnameVerifier implements HostnameVerifier
    {
        public boolean verify(String hostname ,SSLSession session)
        {
            return true;
        }
    }
    
    
    
    /**
     * 获取 SSLContext 对象
     * 
     * @throws KeyManagementException 
     * @throws NoSuchAlgorithmException 
     */
    private synchronized static SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException
    {
        if ( $SSLContext == null )
        {
            $SSLContext = SSLContext.getInstance("SSL");
            $SSLContext.init(null 
                            ,new TrustManager[] {new TrustAnyTrustManager()} 
                            ,new java.security.SecureRandom());
        }
        
        return $SSLContext;
    }
    
}